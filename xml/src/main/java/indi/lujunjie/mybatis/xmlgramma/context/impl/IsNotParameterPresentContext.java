package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:16
 */
public class IsNotParameterPresentContext extends ContentContext {

    private static final String IS_NOT_PARAMETER_PRESENT = "isNotParameterPresent";

    public IsNotParameterPresentContext() {
        super(IS_NOT_PARAMETER_PRESENT);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitIsNotParameterPresent(this) : visitor.visitChildren(this);
    }
}
