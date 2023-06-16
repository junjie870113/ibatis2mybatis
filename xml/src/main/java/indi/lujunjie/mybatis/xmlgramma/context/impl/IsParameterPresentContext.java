package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:21
 */
public class IsParameterPresentContext extends ContentContext {

    private static final String IS_PARAMETER_PRESENT = "isparameterpresent";

    public IsParameterPresentContext() {
        super(IS_PARAMETER_PRESENT);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitIsParameterPresent(this) : visitor.visitChildren(this);
    }
}
