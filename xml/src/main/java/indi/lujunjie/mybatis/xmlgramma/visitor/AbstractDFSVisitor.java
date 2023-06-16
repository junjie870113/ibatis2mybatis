package indi.lujunjie.mybatis.xmlgramma.visitor;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.file.FileWrapper;
import indi.lujunjie.mybatis.xmlgramma.monitor.Watcher;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 09:25
 */
public abstract class AbstractDFSVisitor<T extends Removable> {

    private Watcher watcher;

    protected AbstractDFSVisitor() {

    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    public abstract T defaultResult();

    public abstract T aggregateResult(T aggregate, T nextResult);

    public T visit(Element element, FileWrapper fileWrapper) {
        return ContentContext.builder().element(element).fileWrapper(fileWrapper).build().accept(this);
    }

    public T visitChildren(ContentContext elementContext) {
        if (watcher != null) {
            watcher.collect(elementContext.name());
        }
        T result = this.defaultResult();
        List<Object> subContents = new ArrayList<>();
        for (Object content : elementContext.subContents()) {
            T childResult = null;
            if (content instanceof Element) {
                childResult = ContentContext.builder().element((Element) content).parentContext(elementContext).build().accept(this);
            } else if (content instanceof Text) {
                childResult = ContentContext.builder().text((Text) content).parentContext(elementContext).build().accept(this);
            } else if (content instanceof Comment) {
                childResult = ContentContext.builder().comment((Comment) content).parentContext(elementContext).build().accept(this);
            } else if (content instanceof CDATA) {
                childResult = ContentContext.builder().cdata((CDATA) content).parentContext(elementContext).build().accept(this);
            }
            result = this.aggregateResult(result, childResult);

            if (childResult != null && (childResult.removeSelf() || childResult.removeRecursive())) {
                if (content instanceof Element) {
                    if (childResult.removeSelf()) {
                        subContents.addAll(((Element) content).content());
                    }
                }
            } else if (!((content instanceof Text) && ((Text) content).getText().trim().isEmpty())){
                subContents.add(content);
            }
        }
        Collections.sort(subContents, elementContext.subContentComparator());
        elementContext.setSubContents(subContents);
        return result;
    }

}
