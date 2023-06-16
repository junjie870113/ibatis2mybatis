package indi.lujunjie.mybatis.xmlgramma.context;

import indi.lujunjie.mybatis.xmlgramma.file.FileWrapper;
import indi.lujunjie.mybatis.xmlgramma.visitor.AbstractDFSVisitor;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Text;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 09:37
 */
public class ContentContext implements Removable, Serializable {

    private static final String DEFAULT_GROUP = "default";

    protected String name;

    /**
     * <group, <key, value>>
     */
    protected ConcurrentHashMap<String, Map<String, Object>> context = new ConcurrentHashMap<>();

    protected Element element;

    protected Text text;

    protected Comment comment;

    protected CDATA cdata;

    private boolean removeSelf;

    private boolean removeRecursive;

    private FileWrapper fileWrapper;

    private List<FileWrapper> additionalFileWrapper = new ArrayList<>();

    public ContentContext() {}

    public ContentContext(String name) {
        this.name = name;
    }

    public ContentContext context(ConcurrentHashMap<String, Map<String, Object>> context) {
        this.context = context;
        return this;
    }

    public ContentContext element(Element element) {
        this.element = element;
        return this;
    }

    public ContentContext text(Text text) {
        this.text = text;
        return this;
    }

    public ContentContext comment(Comment comment) {
        this.comment = comment;
        return this;
    }

    public ContentContext cdata(CDATA cdata) {
        this.cdata = cdata;
        return this;
    }

    public ContentContext fileWrapper(FileWrapper fileWrapper) {
        this.fileWrapper = fileWrapper;
        return this;
    }

    public ContentContext additionalFileWrapper(List<FileWrapper> fileWrappers) {
        this.additionalFileWrapper.addAll(fileWrappers);
        return this;
    }

    public ContentContext additionalFileWrapper(FileWrapper fileWrapper) {
        this.additionalFileWrapper.add(fileWrapper);
        return this;
    }

    public String name() {
        return name;
    }

    public Element element() {
        return element;
    }

    public Text text() {
        return text;
    }

    public Comment comment() {
        return comment;
    }

    public CDATA cdata() {
        return cdata;
    }

    public FileWrapper fileWrapper() {
        return fileWrapper;
    }

    public List<FileWrapper> additionalFileWrapper() {
        return this.additionalFileWrapper;
    }

    public List subContents() {
        return element == null ? new ArrayList() : element.content();
    }

    public void setSubContents(List subContents) {
        if (element != null) {
            element.setContent(subContents);
        }
    }

    public void set(String key, Object value) {
        this.set(DEFAULT_GROUP, key, value);
    }

    public void set(String group, String key, Object value) {
        Map<String, Object> subContext = context.get(group);
        if (subContext == null)
            subContext = new ConcurrentHashMap<>();
        subContext.put(key, value);
        context.put(group, subContext);
    }

    public Map<String, Object> getGroup(String group) {
        Map<String, Object> subContext = context.get(group);
        return subContext == null ? new HashMap<>() : subContext;
    }

    public Object get(String key) {
        return this.get(DEFAULT_GROUP, key);
    }

    public Object get(String group, String key) {
        Map<String, Object> subContext = context.get(group);
        return subContext == null ? null : subContext.get(key);
    }

    public Object remove(String key) {
        return remove(DEFAULT_GROUP, key);
    }

    public Object remove(String group, String key) {
        Map<String, Object> subContext = context.get(group);
        return subContext == null ? null : subContext.remove(key);
    }

    public ConcurrentHashMap<String, Map<String, Object>> getContext() {
        return context;
    }

    public <T extends Removable> T accept(AbstractDFSVisitor<T> visitor) {
        return visitor.visitChildren(this);
    }

    @Override
    public ContentContext removeSelf(boolean needRemove) {
        this.removeSelf = needRemove;
        return this;
    }

    @Override
    public boolean removeSelf() {
        return removeSelf;
    }

    @Override
    public ContentContext removeRecursive(boolean removeRecursive) {
        this.removeRecursive = removeRecursive;
        return this;
    }

    @Override
    public boolean removeRecursive() {
        return this.removeRecursive;
    }

    public Comparator subContentComparator() {
        return (Object o1, Object o2) -> 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Element element;

        private Text text;

        private Comment comment;

        private CDATA cdata;

        private FileWrapper fileWrapper;

        private ContentContext parentContext;

        public Builder element(Element element) {
            this.element = element;
            return this;
        }

        public Builder text(Text text) {
            this.text = text;
            return this;
        }

        public Builder comment(Comment comment) {
            this.comment = comment;
            return this;
        }

        public Builder cdata(CDATA cdata) {
            this.cdata = cdata;
            return this;
        }

        public Builder fileWrapper(FileWrapper fileWrapper) {
            this.fileWrapper = fileWrapper;
            return this;
        }

        public Builder parentContext(ContentContext parentContext) {
            this.parentContext = parentContext;
            this.fileWrapper = parentContext.fileWrapper();
            return this;
        }

        public ContentContext build() {
            return ContentContextFactory.create(element, text, comment, cdata, fileWrapper, parentContext == null ? new ConcurrentHashMap<>() : parentContext.getContext());
        }
    }
}
