package indi.lujunjie.mybatis.xmlgramma.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2021-09-28 09:46
 */
public class CacheModel {

    private String id;

    private String type;

    private Boolean readOnly;

    private Boolean serialize;

    private FlushInterval flushInterval;

    private List<FlushOnExecute> flushOnExecutes;

    private List<Property> properties;

    public String id() {
        return this.id;
    }

    public String type() {
        return this.type;
    }

    public Boolean readOnly() {
        return this.readOnly;
    }

    public Boolean serialize() {
        return this.serialize;
    }

    public FlushInterval flushInterval() {
        return this.flushInterval;
    }

    public List<FlushOnExecute> flushOnExecutes() {
        return this.flushOnExecutes;
    }

    public List<Property> properties() {
        return this.properties;
    }

    public CacheModel id(String id) {
        this.id = id;
        return this;
    }

    public CacheModel type(String type) {
        this.type = type;
        return this;
    }

    public CacheModel readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public CacheModel serialize(Boolean serialize) {
        this.serialize = serialize;
        return this;
    }

    public CacheModel flushInterval(FlushInterval flushInterval) {
        this.flushInterval = flushInterval;
        return this;
    }

    public CacheModel flushOnExecute(FlushOnExecute flushOnExecute) {
        if (this.flushOnExecutes == null) {
            this.flushOnExecutes = new ArrayList<>();
        }
        this.flushOnExecutes.add(flushOnExecute);
        return this;
    }

    public CacheModel property(Property property) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(property);
        return this;
    }
}
