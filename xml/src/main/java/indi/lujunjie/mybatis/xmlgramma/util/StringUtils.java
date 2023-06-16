package indi.lujunjie.mybatis.xmlgramma.util;

import java.util.Collection;

/**
 * @author Lu Jun Jie
 * @date 2021-12-27 09:19
 */
public class StringUtils {

    public static String join(Collection<String> items, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        for (String item : items) {
            if (buffer.length() != 0) {
                buffer.append(delimiter);
            }
            buffer.append(item);
        }

        return buffer.toString();
    }
}
