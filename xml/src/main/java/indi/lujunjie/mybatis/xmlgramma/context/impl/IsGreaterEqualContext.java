package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:09
 */
public class IsGreaterEqualContext extends ContentContext {

    public static final String IS_GREATER_EQUAL = "isgreaterequal";

    public IsGreaterEqualContext() {
        super(IS_GREATER_EQUAL);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor instanceof BaseVisitor ? ((BaseVisitor<T>) visitor).visitIsGreaterEqual(this) : visitor.visitChildren(this);
    }

}
