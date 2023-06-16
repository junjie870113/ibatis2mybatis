package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 17:42
 */
public class FlushOnExecuteContext extends ContentContext {

    private static final String FLUSH_ON_EXECUTE = "flushonexecute";

    public FlushOnExecuteContext() {
        super(FLUSH_ON_EXECUTE);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitFlushOnExecute(this) : visitor.visitChildren(this);
    }
}
