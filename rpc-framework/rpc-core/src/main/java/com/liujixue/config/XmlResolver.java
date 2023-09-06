package com.liujixue.config;

import com.liujixue.IdGenerator;
import com.liujixue.ProtocolConfig;
import com.liujixue.compress.Compressor;
import com.liujixue.compress.CompressorFactory;
import com.liujixue.discovery.RegistryConfig;
import com.liujixue.loadbalancer.LoadBalancer;
import com.liujixue.serialize.Serializer;
import com.liujixue.serialize.SerializerFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @Author LiuJixue
 * @Date 2023/9/5 16:06
 * @PackageName:com.liujixue.config
 * @ClassName: XmlResolver
 * @Description: xml解析方式
 */
@Data
@Slf4j
public class XmlResolver {
    void loadFromXml(Configuration configuration) {
        try {
            // 1. 创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用DTD校验：可以通过 setValidating(false) 方法来禁用 DTD 校验
            factory.setValidating(false);
            // 禁用外部实体解析: 可以通过调用 setFeature(String name,boolean value) 方法
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);
            // 2. 获取一个xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            // 3.解析所有的标签
            configuration.setPort(resolvePort(xPath, doc));
            configuration.setAppName(resolveAppName(xPath, doc));
            configuration.setIdGenerator(resolveIdGenerator(xPath, doc));
            configuration.setRegistryConfig(resolveRegistryConfig(xPath, doc));
            // 处理使用的压缩方式和序列化方式，配置新的压缩方式和序列化方式并将其放入工厂中
            configuration.setCompressType(resolveCompressType(xPath, doc));
            configuration.setSerializeType(resolveSerializeType(xPath, doc));
            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(xPath, doc);
            CompressorFactory.addCompressor(compressorObjectWrapper);
            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(xPath, doc);
            SerializerFactory.addSerializer(serializerObjectWrapper);

            configuration.setLoadBalancer(resolveLoadBalancer(xPath, doc));
            // 如果有新增的标签这里继续修改
        }catch (ParserConfigurationException | IOException | SAXException e){
            log.info("No relevant configuration files were found or an exception occurred while parsing the configuration files. The default configuration will be used",e);
        }

    }

    /**
     * 解析序列化器
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return 序列化器
     */
    private ObjectWrapper<Serializer> resolveSerializer(XPath xPath, Document doc) {
        String expression = "/configuration/serializer";
        Serializer serializer = pathObject(xPath, doc, expression, null);
        Byte code= Byte.valueOf(Objects.requireNonNull(pathString(xPath, doc, expression, "code"))) ;
        String name = pathString(xPath, doc, expression, "name");
        ObjectWrapper<Serializer> serializerObjectWrapper = new ObjectWrapper<>(code,name,serializer);
        return serializerObjectWrapper;

    }

    /**
     * 解析压缩的具体实现
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return ObjectWrapper<Compressor>
     */
    private ObjectWrapper<Compressor> resolveCompressor(XPath xPath, Document doc) {
        String expression = "/configuration/compressor";
        Compressor compressor = pathObject(xPath, doc, expression, null);
        Byte code = Byte.valueOf(Objects.requireNonNull(pathString(xPath, doc, expression, "code")));
        String name = pathString(xPath, doc, expression, "name");
        ObjectWrapper<Compressor> objectObjectWrapper = new ObjectWrapper<>(code,name,compressor);
        return objectObjectWrapper;
    }
    /**
     * 解析序列化方式
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return 返回序列化方式
     */
    private String resolveSerializeType(XPath xPath, Document doc) {
        String expression = "/configuration/serializeType";
        return pathString(xPath, doc, expression,"type");
    }

    /**
     * 解析压缩方式
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return
     */
    private String resolveCompressType(XPath xPath, Document doc) {
        String expression = "/configuration/compressType";
        return pathString(xPath, doc, expression,"type");
    }

    /**
     * 解析负载均衡策略
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return
     */
    private LoadBalancer resolveLoadBalancer(XPath xPath, Document doc) {
        String expression = "/configuration/loadBalancer";
        return pathObject(xPath, doc, expression,null);
    }
    /**
     * 解析注册中心
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return
     */
    private RegistryConfig resolveRegistryConfig(XPath xPath, Document doc) {
        String expression = "/configuration/registry";
        String url = pathString(xPath, doc, expression,"url");
        return new RegistryConfig(url);
    }
    /**
     * 解析 ID 生成器
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @return
     */
    private IdGenerator resolveIdGenerator(XPath xPath, Document doc) {
        String expression = "/configuration/idGenerator";
        String aClass = pathString(xPath, doc, expression, "class");
        String dataCenterId = pathString(xPath, doc, expression, "dataCenterId");
        String machineId = pathString(xPath, doc, expression, "machineId");
        try {
            // TODO 不灵活，待改造
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator)instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 解析应用名称
     * @param xPath xpath解析器
     * @param doc 文档
     * @return 应用名
     */
    private String resolveAppName(XPath xPath, Document doc) {
        String expression = "/configuration/appName";
        return pathString(xPath, doc, expression);
    }

    /**
     * 解析端口号
     * @param xPath xpath解析器
     * @param doc 文档
     * @return 端口号
     */
    private int resolvePort(XPath xPath, Document doc) {
        String expression = "/configuration/port";
        String portString = pathString(xPath, doc, expression);
        return Integer.parseInt(portString);
    }
    /**
     * 获得一个节点属性的值
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @param AttributeName 节点名称
     * @return 节点的值
     */
    private String pathString(XPath xPath, Document doc,String expression,String AttributeName) {
        try {
            XPathExpression expr = xPath.compile(expression);
            // 表达式可以帮助获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
            return className;
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression",e);
        }
        return null;
    }
    /**
     * 获得一个节点的文本 <port num="6666"></>
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String pathString(XPath xPath, Document doc,String expression) {
        try {
            XPathExpression expr = xPath.compile(expression);
            // 表达式可以帮助获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression",e);
        }
        return null;
    }

    /**
     * 解析一个节点, 返回一个实例
     * @param xPath xpath解析器
     * @param doc 文档对象
     * @param expression xpath 表达式
     * @param paramType 参数列表
     * @param param 参数
     * @return 配置的实例
     * @param <T> 泛型
     */
    private <T> T pathObject(XPath xPath, Document doc,String expression,Class<?>[] paramType,Object...param) {
        try {
            XPathExpression expr = xPath.compile(expression);
            // 表达式可以帮助获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null) {
                instant = aClass.getConstructor().newInstance();
            }else {
                instant = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T)instant;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression",e);
        }
        return null;
    }

    // 代码配置由引导程序进行完成
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}
