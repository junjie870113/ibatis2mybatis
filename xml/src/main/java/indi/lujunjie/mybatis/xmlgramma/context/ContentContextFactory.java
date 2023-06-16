package indi.lujunjie.mybatis.xmlgramma.context;

import indi.lujunjie.mybatis.xmlgramma.context.impl.CDataContext;
import indi.lujunjie.mybatis.xmlgramma.context.impl.CommentContext;
import indi.lujunjie.mybatis.xmlgramma.context.impl.TextContext;
import indi.lujunjie.mybatis.xmlgramma.file.FileWrapper;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Text;

import java.io.*;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 10:16
 */
public class ContentContextFactory {

    private static Map<String, ContentContext> cache = new ConcurrentHashMap<>();

    static {
        if (cache.isEmpty()) {
            synchronized (ContentContextFactory.class) {
                if (cache.isEmpty()) {
                    ServiceLoader.load(ContentContext.class).forEach(item -> cache.put(item.name().toLowerCase(), item));
                }
            }
        }
    }

    public static ContentContext create(Element element, Text text, Comment comment, CDATA cdata, FileWrapper fileWrapper, ConcurrentHashMap<String, Map<String, Object>> context) {
        ContentContext contentContext = new ContentContext();
        if (element != null) {
            contentContext = cache.get(element.getName().toLowerCase());
        } else if (text != null) {
            contentContext = cache.get(TextContext.NAME.toLowerCase());
        } else if (comment != null) {
            contentContext = cache.get(CommentContext.NAME.toLowerCase());
        } else if (cdata != null) {
            contentContext = cache.get(CDataContext.NAME.toLowerCase());
        }
        try {
            return copy(contentContext).context(context).element(element).text(text).comment(comment).cdata(cdata).fileWrapper(fileWrapper);
        } catch (Exception e) {
            System.out.println(fileWrapper + " ### " + element.getName());
            throw new RuntimeException(e);
        }
    }

    private static <T extends Serializable> T copy(T src) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(byteArrayOutputStream).writeObject(src);

            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return (T) new ObjectInputStream(byteArrayInputStream).readObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
