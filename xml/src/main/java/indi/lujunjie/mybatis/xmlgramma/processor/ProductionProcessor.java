package indi.lujunjie.mybatis.xmlgramma.processor;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.file.FileManager;
import indi.lujunjie.mybatis.xmlgramma.file.FileWrapper;
import indi.lujunjie.mybatis.xmlgramma.monitor.DefaultWatcher;
import indi.lujunjie.mybatis.xmlgramma.visitor.IBatisVisitor;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Lu Jun Jie
 * @date 2022-01-09 20:22
 */
public class ProductionProcessor implements Processor {

    public static final String MODE = "PRODUCTION";

    @Override
    public String name() {
        return MODE;
    }

    @Override
    public void process(String projectDir) {
        List<FileWrapper> fileWrappers = FileManager.getInstance().loadXmlFiles(projectDir);
        DefaultWatcher watcher = DefaultWatcher.builder().build();
        List<FileWrapper> additionalFileWrappers = new ArrayList<>();
        List<FileWrapper> sqlMapConfigs = new ArrayList<>();
        for (FileWrapper fileWrapper : fileWrappers) {
            if (fileWrapper.isIbatisSqlMapConfigFile()) {
                sqlMapConfigs.add(fileWrapper);
            }
            fileWrapper.shiftDocType();
            ContentContext contentContext = IBatisVisitor.builder().watcher(watcher).build().visit(fileWrapper.getDocument().getRootElement(), fileWrapper);
            if (contentContext.additionalFileWrapper() != null) {
                additionalFileWrappers.addAll(contentContext.additionalFileWrapper());
            }
        }

        this.adjustSqlMapConfigGlobally(sqlMapConfigs, additionalFileWrappers);

        System.out.println("*****************");
        System.out.println("all xml elements in target project:");
        for (String label : watcher.getLabelNames()) {
            System.out.println(label);
        }
        System.out.println("*****************");
        FileManager.getInstance().flushBack(fileWrappers);
        FileManager.getInstance().flushBack(additionalFileWrappers);
    }

    /**
     * additional filewrapper should be include in sqlmapconfig files
     *
     * e.g.
     *
     * we have a sql map config below with additional sqlmapper files(sqlmapper-1-cachemodel-${select-statement-id1}.xml, sqlmapper-1-cachemodel-${select-statement-id2}.xml)
     *
     * <configuration>
     *     <mapper resource="a/b/c/sqlmapper-1.xml"/>
     *     <mapper url="http://a/b/c/sqlmapper-1.xml"/>
     * </configuration>
     *
     * we will change it to
     *
     * <configuration>
     *     <mapper resource="a/b/c/sqlmapper-1-cachemodel-${select-statement-id1}.xml"/>
     *     <mapper resource="a/b/c/sqlmapper-1-cachemodel-${select-statement-id2}.xml"/>
     *     <mapper resource="a/b/c/sqlmapper-1.xml"/>
     *     <mapper url="http://a/b/c/sqlmapper-1-cachemodel-${select-statement-id1}.xml"/>
     *     <mapper url="http://a/b/c/sqlmapper-1-cachemodel-${select-statement-id2}.xml"/>
     *     <mapper url="http://a/b/c/sqlmapper-1.xml"/>
     * </configuration>
     *
     * @param sqlMapConfigs
     * @param additionalFileWrappers
     */
    private void adjustSqlMapConfigGlobally(List<FileWrapper> sqlMapConfigs, List<FileWrapper> additionalFileWrappers) {
        Map<String, List<String>> nameMapping = new HashMap<>();
        for (FileWrapper additionalFileWrapper : additionalFileWrappers) {
            List<String> addNameList = nameMapping.get(additionalFileWrapper.referenceFile().getName());
            addNameList = addNameList == null ? new ArrayList<>() : addNameList;
            addNameList.add(additionalFileWrapper.file().getName());
            nameMapping.put(additionalFileWrapper.referenceFile().getName(), addNameList);
        }

        for (FileWrapper sqlMapConfig : sqlMapConfigs) {
            Element sqlMapConfigElement = sqlMapConfig.getDocument().getRootElement();
            List<Object> newContent = new ArrayList<>();
            for (Object item : sqlMapConfigElement.content()) {
                if (item instanceof Element && "mapper".equalsIgnoreCase(((Element) item).getName())) {
                    Attribute resource = ((Element) item).attribute("resource");
                    Attribute url = ((Element) item).attribute("url");
                    Function<Attribute, List<DefaultElement>> fn = attribute -> {
                        if (attribute == null) {
                            return new ArrayList<>();
                        }
                        String[] attributeValueItem = attribute.getValue().split("/");
                        List<String> mappingNames = nameMapping.get(attributeValueItem[attributeValueItem.length - 1]);
                        if (mappingNames == null) {
                            return new ArrayList<>();
                        }
                        return mappingNames.stream().map(name -> {
                            DefaultElement mapper = new DefaultElement("mapper");
                            mapper.addAttribute(attribute.getName(), attribute.getValue().replace(attributeValueItem[attributeValueItem.length - 1], name));
                            return mapper;
                        }).collect(Collectors.toList());
                    };
                    newContent.addAll(fn.apply(resource));
                    newContent.addAll(fn.apply(url));
                }
                newContent.add(item);
            }
            sqlMapConfigElement.setContent(newContent);
        }
    }
}
