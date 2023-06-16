package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:16
 */
public class IsNotPropertyAvailableContext extends ContentContext {

    private static final String IS_NOT_PROPERTY_AVAILABLE = "isNotPropertyAvailable";

    public IsNotPropertyAvailableContext() {
        super(IS_NOT_PROPERTY_AVAILABLE);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitIsNotPropertyAvailable(this) : visitor.visitChildren(this);
    }
}
