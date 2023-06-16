package indi.lujunjie.mybatis.xmlgramma.context.impl;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import indi.lujunjie.mybatis.xmlgramma.visitor.BaseVisitor;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 17:30
 */
public class ResultMapContext extends ContentContext {

    //constructor?,id*,result*,association*,collection*, discriminator
    private static List<String> sequence = Arrays.asList("constructor", "id", "result", "association", "collection", "discriminator");

    private static final String RESULT_MAP = "resultmap";

    public ResultMapContext() {
        super(RESULT_MAP);
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return (visitor instanceof BaseVisitor) ? ((BaseVisitor<T>) visitor).visitResultMap(this) : visitor.visitChildren(this);
    }

    public Comparator subContentComparator() {
        return (Object from, Object to) -> {
            if (!(from instanceof Element))
                return -1;
            if (!(to instanceof Element))
                return 1;
            return sequence.indexOf(((Element) from).getName()) - sequence.indexOf(((Element) to).getName());
        };
    }
}
