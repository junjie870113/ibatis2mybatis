package indi.lujunjie.mybatis.xmlgramma.processor;

import indi.lujunjie.mybatis.xmlgramma.file.FileManager;
import indi.lujunjie.mybatis.xmlgramma.file.FileWrapper;
import indi.lujunjie.mybatis.xmlgramma.monitor.DefaultWatcher;
import indi.lujunjie.mybatis.xmlgramma.visitor.SandboxVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2022-01-09 20:22
 */
public class SandboxProcessor implements Processor {

    public static final String MODE = "SANDBOX";

    @Override
    public String name() {
        return MODE;
    }

    @Override
    public void process(String projectDir) {
        List<FileWrapper> fileWrappers = FileManager.getInstance().loadXmlFiles(projectDir);
        DefaultWatcher watcher = DefaultWatcher.builder().build();
        List<FileWrapper> sqlMapConfigs = new ArrayList<>();
        System.out.println("*****************");
        System.out.println("Unsupported attribute below(maybe empty):");
        for (FileWrapper fileWrapper : fileWrappers) {
            if (fileWrapper.isIbatisSqlMapConfigFile()) {
                sqlMapConfigs.add(fileWrapper);
            }
            SandboxVisitor.builder().watcher(watcher).build().visit(fileWrapper.getDocument().getRootElement(), fileWrapper);
        }
        System.out.println("*****************");
        System.out.println("all xml label in target project:");
        for (String label : watcher.getLabelNames()) {
            System.out.println(label);
        }
        System.out.println("*****************");
    }
}
