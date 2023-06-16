package indi.lujunjie.mybatis.xmlgramma.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2021-09-22 14:05
 */
public class FileManager {

    private static FileManager instance;

    private static String XML = "xml";

    public static FileManager getInstance() {
        if (instance == null) {
            synchronized(FileManager.class) {
                if (instance == null)
                    instance = new FileManager();
            }
        }
        return instance;
    }

    public List<FileWrapper> loadXmlFiles(String projectPath) {
        return doGetXmlFiles(new File(projectPath));
    }

    private List<FileWrapper> doGetXmlFiles(File file) {
        List<FileWrapper> result = new ArrayList<>();
        //validate
        if (file == null || !file.exists())
            return result;
        if (file.isFile() && file.getName().endsWith(XML)) {
            FileWrapper fileWrapper = FileWrapper.builder().file(file).build();
            if (fileWrapper.isIbatisConfigFile()) {
                result.add(fileWrapper);
            }
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            for (File item : subFiles) {
                if (item.isFile() && item.getName().endsWith(XML)) {
                    FileWrapper fileWrapper = FileWrapper.builder().file(item).build();
                    if (fileWrapper.isIbatisConfigFile()) {
                        result.add(fileWrapper);
                    }
                } else if (item.isDirectory()) {
                    result.addAll(doGetXmlFiles(item));
                }
            }
        }
        return result;
    }

    public void flushBack(List<FileWrapper> fileWrappers) {
        for (FileWrapper fileWrapper : fileWrappers) {
            fileWrapper.flushBack();
        }
    }
}
