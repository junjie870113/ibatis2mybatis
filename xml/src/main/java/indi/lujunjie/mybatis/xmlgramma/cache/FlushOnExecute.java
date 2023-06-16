package indi.lujunjie.mybatis.xmlgramma.cache;

/**
 * @author Lu Jun Jie
 * @date 2021-09-28 09:49
 */
public class FlushOnExecute {

    private String statement;

    public FlushOnExecute statement(String statement) {
        this.statement = statement;
        return this;
    }

    public String statement() {
        return this.statement;
    }
}
