package indi.lujunjie.mybatis.xmlgramma.visitor;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.impl.DynamicContext;
import indi.lujunjie.mybatis.xmlgramma.monitor.Watcher;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Element;
import org.dom4j.tree.DefaultText;

import java.util.List;

/**
 * @author Lu Jun Jie
 * @date 2021-10-12 14:55
 */
public class ExperimentalVisitor extends IBatisVisitor {

    protected ExperimentalVisitor(Watcher watcher) {
        super(watcher);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     *
     * should convert to '<trim>' tag according to semantic translation like below
     *
     * ## case where
     *
     * ### before
     *
     * ```
     * <dynamic prepend="where">
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </dynamic>
     * ```
     *
     * ### after
     *
     * ```
     * <trim prefix="where" prefixOverrides="AND |OR ">
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </trim>
     * ```
     *
     * ## case set
     *
     * ### before
     *
     * ```
     * UPDATE testTable
     * <dynamic prepend="set">
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </dynamic>
     * WHERE
     *   key = #key#
     * ```
     *
     * ### after
     *
     * ```
     * UPDATE testTable
     * <trim prefix="set" prefixOverrides=", |AND |OR ">
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </trim>
     * WHERE
     *   key = #key#
     * ```
     *
     * HOWEVER, author try to translate in another way which is more meaningful
     *
     * ## case where
     *
     * ### before
     *
     * ```
     * <dynamic prepend="where">
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </dynamic>
     * ```
     *
     * ### after
     *
     * ```
     * <where>
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </where>
     * ```
     * ### before
     *
     * ```
     * where
     * <dynamic prepend=" ">
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </dynamic>
     * ```
     *
     * ### after
     *
     * ```
     * <where>
     *   <isEqual prepend="and" property="id" compareValue="1">
     *     id = #id#
     *   </isEqual>
     *   <isEqual prepend="and" property="name" compareValue="foo">
     *     name = #name#
     *   </isEqual>
     * </where>
     * ```
     *
     * ## case set
     *
     * ### before
     *
     * ```
     * UPDATE table
     * <dynamic prepend="set">
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </dynamic>
     * WHERE
     *   key = #key#
     * ```
     *
     * ### after
     *
     * ```
     * UPDATE table
     * <set>
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </set>
     * WHERE
     *   key = #key#
     * ```
     *
     * ### before
     *
     * ```
     * UPDATE table
     * set
     * <dynamic prepend=" ">
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </dynamic>
     * WHERE
     *   key = #key#
     * ```
     *
     * ### after
     *
     * ```
     * UPDATE table
     * <set>
     *   <isNotNull prepend="," property="id">
     *     id = #id#
     *   </isNotNull>
     *   <isNotNull prepend="," property="name">
     *     name = #name#
     *   </isNotNull>
     * </set>
     * WHERE
     *   key = #key#
     * ```
     *
     * @param dynamicContext
     * @return
     */
    @Override
    public ContentContext visitDynamic(DynamicContext dynamicContext) {
        Element element = dynamicContext.element();
        Attribute open = element.attribute("open");
        if (open != null) {
            element.content().add(0, new DefaultText(element, " " + open.getValue() + " "));
            element.remove(open);
        }
        Attribute close = element.attribute("close");
        if (close != null) {
            element.content().add(element.content().size(), new DefaultText(element, " " + close.getValue() + " "));
            element.remove(close);
        }
        Attribute prepend = element.attribute("prepend");
        if (prepend != null) {
            if (prepend.getValue().trim().length() > 0) {
                //<dynamic prepend="where">  <dynamic prepend="set">
                element.setName(prepend.getValue().toLowerCase());
                element.remove(prepend);
            } else {
                //'where
                // <dynamic prepend=" ">'
                //
                // =>
                //
                // '<where>'
                //
                // we need look back
                // if the text is 'where' \ 'set', we need adjust
                List brothers = element.getParent().content();
                for (int i = 1; i < brothers.size(); i++) {
                    if (brothers.get(i).equals(element)) {
                        //find first not empty pre brothers
                        Object pre = null;
                        for (int j = i - 1; j >= 0; j--) {
                            pre = brothers.get(j);
                            if ((pre instanceof DefaultText && !((DefaultText) pre).getText().trim().isEmpty()) ||
                                    (pre instanceof CDATA && !((CDATA) pre).getText().trim().isEmpty()))
                                break;
                        }
                        if (pre == null) {
                            break;
                        }

                        //find curr element and previous brother node.
                        String lastToken = null;
                        if (pre instanceof DefaultText) {
                            // change previous node from 'XXX where' to 'XXX ' \ 'XXX set' to 'XXX '
                            String previousText = ((DefaultText) pre).getText().trim();
                            String[] tokens = previousText.split(" ");
                            lastToken = tokens[tokens.length - 1];
                            if ("where".equalsIgnoreCase(lastToken) || "set".equalsIgnoreCase(lastToken)) {
                                ((DefaultText) pre).setText(((DefaultText) pre).getText().replaceAll(lastToken, ""));
                            }
                        } else if (pre instanceof CDATA) {
                            // change previous node from '<![CDATA[XXX where]]>' to '<![CDATA[XXX ]]' \ '<![CDATA[XXX set]]' to '<![CDATA[XXX ]]'
                            String previousText = ((CDATA) pre).getText().trim();
                            String[] tokens = previousText.split(" ");
                            lastToken = tokens[tokens.length - 1];
                            if ("where".equalsIgnoreCase(lastToken) || "set".equalsIgnoreCase(lastToken)) {
                                ((CDATA) pre).setText(((CDATA) pre).getText().replaceAll(lastToken, ""));
                            }
                        }

                        if ("where".equalsIgnoreCase(lastToken) || "set".equalsIgnoreCase(lastToken)) {
                            //<dynamic prepend=" "> => <${lastToken}>
                            element.setName(lastToken.toLowerCase());
                            element.remove(prepend);
                        }
                        break;
                    }
                }
            }
        } else {
            dynamicContext.removeSelf(true);
        }
        super.visitDynamic(dynamicContext);
        return dynamicContext;
    }
}
