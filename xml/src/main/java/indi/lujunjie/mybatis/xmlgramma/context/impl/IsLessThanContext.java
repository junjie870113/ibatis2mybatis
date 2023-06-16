package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:11
 */
public class IsLessThanContext extends ContentContext {

    private static final String IS_LESS_THAN = "islessthan";

    public IsLessThanContext() {
        super(IS_LESS_THAN);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor instanceof BaseVisitor ? ((BaseVisitor<T>) visitor).visitIsLessThan(this) : visitor.visitChildren(this);
    }
}
