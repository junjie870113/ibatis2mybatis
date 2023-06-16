package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-24 09:06
 */
public class ResultObjectFactoryContext extends ContentContext {

    private static final String RESULT_OBJECT_FACTORY = "resultobjectfactory";

    public ResultObjectFactoryContext() {
        super(RESULT_OBJECT_FACTORY);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitResultObjectFactory(this) : visitor.visitChildren(this);
    }
}
