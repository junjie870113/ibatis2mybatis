package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:06
 */
public class IncludeContext extends ContentContext {

    public static final String INCLUDE = "include";

    public IncludeContext() {
        super(INCLUDE);
    }

    @Override
    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor instanceof BaseVisitor ? ((BaseVisitor<T>) visitor).visitInclude(this) : visitor.visitChildren(this);
    }
}
