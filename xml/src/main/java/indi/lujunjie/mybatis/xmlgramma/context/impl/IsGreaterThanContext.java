package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:11
 */
public class IsGreaterThanContext extends ContentContext {

    private static final String IS_GREATER_THAN = "isgreaterthan";

    public IsGreaterThanContext() {
        super(IS_GREATER_THAN);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor instanceof BaseVisitor ? ((BaseVisitor<T>) visitor).visitIsGreaterThan(this) : visitor.visitChildren(this);
    }
}
