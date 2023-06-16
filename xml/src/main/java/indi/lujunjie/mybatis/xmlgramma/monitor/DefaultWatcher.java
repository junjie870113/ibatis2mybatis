package indi.lujunjie.mybatis.xmlgramma.monitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lu Jun Jie
 * @date 2021-09-27 10:43
 */
public class DefaultWatcher implements Watcher {

    private String name;

    private Set<String> labelNames;

    private DefaultWatcher(String name) {
        this.name = name;
        this.labelNames = new HashSet<>();
    }

    public Set<String> getLabelNames() {
        return labelNames;
    }

    public void setLabelNames(Set<String> labelNames) {
        this.labelNames = labelNames;
    }

    @Override
    public void collect(Object item) {
        labelNames.add(String.valueOf(item));
    }

    @Override
    public Set<String> report() {
        return labelNames;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static final String NAME = "default";

        public DefaultWatcher build() {
            return new DefaultWatcher(NAME);
        }
    }
}
