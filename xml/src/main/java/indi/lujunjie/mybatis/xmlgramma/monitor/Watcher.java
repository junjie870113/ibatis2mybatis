package indi.lujunjie.mybatis.xmlgramma.monitor;

public interface Watcher {

    void collect(Object item);

    <T> T report();
}
