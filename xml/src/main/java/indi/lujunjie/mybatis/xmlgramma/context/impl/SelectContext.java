package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 10:33
 */
public class SelectContext extends ContentContext {

    private static final String SELECT = "select";

    public SelectContext() {
        super(SELECT);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitSelect(this) : visitor.visitChildren(this);
    }
}
