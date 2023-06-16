package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 17:42
 */
public class FlushIntervalContext extends ContentContext {

    private static final String FLUSH_INTERVAL = "flushinterval";

    public FlushIntervalContext() {
        super(FLUSH_INTERVAL);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitFlushInterval(this) : visitor.visitChildren(this);
    }
}
