package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:09
 */
public class IsLessEqualContext extends ContentContext {

    public static final String IS_LESS_EQUAL = "islessequal";

    public IsLessEqualContext() {
        super(IS_LESS_EQUAL);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor instanceof BaseVisitor ? ((BaseVisitor<T>) visitor).visitIsLessEqual(this) : visitor.visitChildren(this);
    }

}
