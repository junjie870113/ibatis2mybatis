package indi.lujunjie.mybatis.xmlgramma.cache;

/**
 * @author Lu Jun Jie
 * @date 2021-09-28 09:49
 */
public class Property {

    private String name;

    private String value;

    public String name() {
        return this.name;
    }

    public String value() {
        return this.value;
    }

    public Property name(String name) {
        this.name = name;
        return this;
    }

    public Property value(String value) {
        this.value = value;
        return this;
    }
}
