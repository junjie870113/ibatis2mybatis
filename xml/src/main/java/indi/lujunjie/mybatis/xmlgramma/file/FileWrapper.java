package indi.lujunjie.mybatis.xmlgramma.file;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocumentType;
import org.xml.sax.InputSource;

import java.io.*;

/**
 * @author Lu Jun Jie
 * @date 2021-09-22 14:28
 */
public class FileWrapper implements Serializable {

    public static final String CACHEMODEL_DELIMITER = "-cachemodel-";

    private static String SQL_MAP_CONFIG = "sqlMapConfig";

    private static String SQL_MAP = "sqlMap";

    private static String IBATIS = "ibatis";

    private static DefaultDocumentType mapperDocType = new DefaultDocumentType("mapper", "-//mybatis.org//DTD Mapper 3.0//EN", "http://mybatis.org/dtd/mybatis-3-mapper.dtd");

    private static DefaultDocumentType configDocType = new DefaultDocumentType("configuration", "-//mybatis.org//DTD Config 3.0//EN", "http://mybatis.org/dtd/mybatis-3-config.dtd");

    private File file;

    private Document document;

    private static OutputFormat outputFormat;

    static {
        outputFormat = new OutputFormat();
        outputFormat.setNewlines(true);
        outputFormat.setIndent("    ");
        outputFormat.setIndent(true);
        outputFormat.setSuppressDeclaration(false);
        outputFormat.setOmitEncoding(false);
    }

    public FileWrapper(File file, Document document) {
        this.file = file;
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public File file() {
        return this.file;
    }

    public File referenceFile() {
        String currentName = file.getName().substring(0, file.getName().indexOf(CACHEMODEL_DELIMITER)) + ".xml";
        File parentFile = file.getParentFile();
        return new File(parentFile, currentName);
    }

    public boolean isIbatisConfigFile() {
        return isIbatisSqlMapConfigFile() || isIbatisSqlMapFile();
    }

    public FileWrapper shiftDocType() {
        if (isIbatisSqlMapFile()) {
            document.setDocType(mapperDocType);
        } else if (isIbatisSqlMapConfigFile()) {
            document.setDocType(configDocType);
        }
        return this;
    }

    public boolean isIbatisSqlMapConfigFile() {
        if (document == null)
            return false;
        DocumentType docType = document.getDocType();
        return docType != null && SQL_MAP_CONFIG.equalsIgnoreCase(docType.getElementName()) &&
                (docType.getPublicID().toLowerCase().contains(IBATIS) || docType.getSystemID().toLowerCase().contains(IBATIS));
    }

    private boolean isIbatisSqlMapFile() {
        if (document == null)
            return false;
        DocumentType docType = document.getDocType();
        return docType != null && SQL_MAP.equalsIgnoreCase(docType.getElementName()) &&
                (docType.getPublicID().toLowerCase().contains(IBATIS) || docType.getSystemID().toLowerCase().contains(IBATIS));
    }

    public FileWrapper newAdditionalFileWrapper(String additionalFileKey, Document document) {
        String currnetName = file.getName().replaceAll("\\.xml", CACHEMODEL_DELIMITER + additionalFileKey + "\\.xml");
        File parentFile = file.getParentFile();
        return new FileWrapper(new File(parentFile, currnetName), document);
    }

    public void flushBack() {
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new FileWriter(file), outputFormat);
            writer.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FileWrapper{");
        sb.append("file=").append(file.getAbsolutePath());
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        private static SAXReader saxReader;

        private File file;

        static {
            saxReader = new SAXReader();
            saxReader.setValidation(false);
            saxReader.setEntityResolver((publicId, systemId) -> new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes())));
        }

        public Builder file(File file) {
            this.file = file;
            return this;
        }

        public FileWrapper build() {
            try {
//                System.out.println("parse:" + file.getAbsolutePath());
                Document document = saxReader.read(file);
                return new FileWrapper(file, document);
            } catch (DocumentException e) {
                e.printStackTrace();
            }

            return new FileWrapper(file, null);
        }
    }
}
