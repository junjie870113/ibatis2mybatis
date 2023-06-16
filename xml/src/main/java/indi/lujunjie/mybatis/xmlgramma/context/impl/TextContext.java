package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;

/**
 * This context is specified against DefaultText
 *
 * @author Lu Jun Jie
 * @date 2021-09-24 15:35
 */
public class TextContext extends ContentContext {

    public static final String NAME = "text";

    public TextContext() {
        super(NAME);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitText(this) : visitor.visitChildren(this);
    }
}
