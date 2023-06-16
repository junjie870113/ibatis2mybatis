package indi.lujunjie.mybatis.xmlgramma.visitor;

import indi.lujunjie.mybatis.xmlgramma.cache.CacheModel;
import indi.lujunjie.mybatis.xmlgramma.cache.FlushInterval;
import indi.lujunjie.mybatis.xmlgramma.cache.FlushOnExecute;
import indi.lujunjie.mybatis.xmlgramma.cache.Property;
import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.impl.*;
import indi.lujunjie.mybatis.xmlgramma.monitor.Watcher;
import indi.lujunjie.mybatis.xmlgramma.util.StringUtils;
import org.dom4j.*;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:29
 */
public class IBatisVisitor extends BaseVisitor<ContentContext> {

    private Pattern variablePattern = Pattern.compile("([#\\$])([A-Za-z0-9.\\[\\]\\s]+)([#\\$])");

    private Pattern hashPattern = Pattern.compile("[^#\\$]");

    private static final String CURRENT = "current";

    private static final String CONTEXT_GROUP_TYPEALIAS = "typealias";

    private static final String CONTEXT_GROUP_CACHEMODEL = "cachemodel";

    private static final String CONTEXT_GROUP_ITERATE = "iterate";

    private static final String CONTEXT_GROUP_DYNAMIC = "dynamic";

    private static final String CONTEXT_GROUP_PRIMITIVE_PARAMETER = "primitiveparameter";

    private static HashSet<String> primitiveTypes = new HashSet<>();

    static {
        primitiveTypes.add(String.class.getName().toLowerCase());
        primitiveTypes.add("string");
        primitiveTypes.add(Integer.class.getName().toLowerCase());
        primitiveTypes.add("int");
        primitiveTypes.add("integer");
        primitiveTypes.add(Long.class.getName().toLowerCase());
        primitiveTypes.add("long");
        primitiveTypes.add(Double.class.getName().toLowerCase());
        primitiveTypes.add("double");
        primitiveTypes.add(Float.class.getName().toLowerCase());
        primitiveTypes.add("float");
        primitiveTypes.add(Boolean.class.getName().toLowerCase());
        primitiveTypes.add("boolean");
        primitiveTypes.add(Character.class.getName().toLowerCase());
        primitiveTypes.add("char");
        primitiveTypes.add("character");
        primitiveTypes.add(Short.class.getName().toLowerCase());
        primitiveTypes.add("short");
        primitiveTypes.add(Byte.class.getName().toLowerCase());
        primitiveTypes.add("byte");
    }

    protected IBatisVisitor(Watcher watcher) {
        super();
        setWatcher(watcher);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Watcher watcher;

        public Builder watcher(Watcher watcher) {
            this.watcher = watcher;
            return this;
        }

        public IBatisVisitor build() {
            return new IBatisVisitor(watcher);
        }
    }

    @Override
    public ContentContext defaultResult() {
        return ContentContext.builder().build();
    }

    @Override
    public ContentContext aggregateResult(ContentContext aggregate, ContentContext nextResult) {
        if (aggregate == null)
            return nextResult;
        if (nextResult == null)
            return aggregate;
        if (nextResult != null && aggregate != null) {
            nextResult.getContext().putAll(aggregate.getContext());
            nextResult.additionalFileWrapper(aggregate.additionalFileWrapper());
        }

        return nextResult;
    }

    /**
     * xmlns:fo CDATA #IMPLIED --- TODO
     * namespace CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <sqlMap namespace="indi.lujunjie">
     * </sqlMap>
     * ```
     *
     * ### after
     *
     * ```
     * <mapper namespace="indi.lujunjie">
     * </mapper>
     * ```
     *
     * @param sqlMapContext
     * @return
     */
    @Override
    public ContentContext visitSqlMap(SqlMapContext sqlMapContext) {
        sqlMapContext.element().setName("mapper");
        return super.visitSqlMap(sqlMapContext);
    }

    /**
     * alias CDATA #REQUIRED --- DONE
     * type CDATA #REQUIRED --- DONE
     *
     * @param typeAliasContext
     * @return
     */
    @Override
    public ContentContext visitTypeAlias(TypeAliasContext typeAliasContext) {
        Element element = typeAliasContext.element();
        typeAliasContext.set(CONTEXT_GROUP_TYPEALIAS, element.attributeValue("alias"), element.attributeValue("type"));
        typeAliasContext.set(CONTEXT_GROUP_TYPEALIAS, element.attributeValue("alias").toLowerCase(), element.attributeValue("type"));
        typeAliasContext.removeSelf(true);
        return typeAliasContext;
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * parameterMap CDATA #IMPLIED --- DONE
     * parameterClass CDATA #IMPLIED --- DONE
     * resultMap CDATA #IMPLIED --- DONE
     * resultClass CDATA #IMPLIED --- DONE
     * cacheModel CDATA #IMPLIED --- DONE
     * Since Mybatis Cache is subItem of Sqlmap instead of an attribute of select.
     * If we wanna migrate this attribute, it's required to create new sqlmap definition separately
     * sqlmap-original.xml -> sqlmap-original.xml + sqlmap-oroginal-select-with-modelcache${N}.xml
     *
     * resultSetType (FORWARD_ONLY | SCROLL_INSENSITIVE | SCROLL_SENSITIVE) #IMPLIED --- DONE
     * fetchSize CDATA #IMPLIED --- DONE
     * xmlResultName CDATA #IMPLIED --- TODO
     * remapResults (true|false) #IMPLIED --- DONE
     * timeout CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <select id="find" resultClass="String" parameterClass="long" remapResults="true">
     * ```
     *
     * ### after
     *
     * ```
     * <select id="find" resultType="String" parameterType="long" statementType="STATEMENT">
     * ```
     *
     * @param selectContext
     * @return
     */
    @Override
    public ContentContext visitSelect(SelectContext selectContext) {
        Element element = selectContext.element();
        Attribute resultClassAttribute = element.attribute("resultClass");
        String alias, realType;
        if (resultClassAttribute != null) {
            alias = resultClassAttribute.getValue().trim();
            realType = (String) selectContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) selectContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            element.addAttribute("resultType", realType);
            element.remove(resultClassAttribute);
        }

        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue().trim();
            realType = (String) selectContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) selectContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                selectContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }
        ContentContext contentContext = super.visitSelect(selectContext);
        if (selectContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            selectContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        Attribute remapResults = element.attribute("remapResults");
        if (remapResults != null) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(remapResults.getValue())) {
                element.addAttribute("statementType", "STATEMENT");
            }
            element.remove(remapResults);
        }
        Attribute cacheModelAttribute = element.attribute("cacheModel");
        if (cacheModelAttribute == null) {
            return contentContext;
        } else {
            element.remove(cacheModelAttribute);
            CacheModel cacheModel = (CacheModel) selectContext.get(CONTEXT_GROUP_CACHEMODEL, cacheModelAttribute.getValue());
            if (cacheModel != null) {
                element.addAttribute("useCache", "true");
                for (FlushOnExecute flushOnExecute : cacheModel.flushOnExecutes()) {
                    if (element.attributeValue("id").equalsIgnoreCase(flushOnExecute.statement())) {
                        element.addAttribute("flushCache", "true");
                    }
                }
            }
            Document document = (Document) element.getDocument().clone();
            DefaultElement cache = new DefaultElement("cache");
            if (cacheModel.type() != null) {
                cache.addAttribute("type", cacheModel.type());
            }
            if (cacheModel.readOnly() != null) {
                cache.addAttribute("readOnly", String.valueOf(cacheModel.readOnly()));
            }
            if (cacheModel.flushInterval() != null) {
                cache.addAttribute("flushInterval", cacheModel.flushInterval().getTime());
            }
            for (Property property : cacheModel.properties()) {
                Element e = new DefaultElement("property");
                e.addAttribute("name", property.name());
                e.addAttribute("value", property.value());
                cache.add(e);
            }
            Element sqlMapElementClone = document.getRootElement();
            sqlMapElementClone.clearContent();
            sqlMapElementClone.add(cache);
            sqlMapElementClone.add((Element) element.clone());
            selectContext.additionalFileWrapper(selectContext.fileWrapper().newAdditionalFileWrapper(element.attributeValue("id"), document));
            selectContext.removeRecursive(true);
            return selectContext;
        }
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * parameterMap CDATA #IMPLIED --- DONE
     * parameterClass CDATA #IMPLIED --- DONE
     * timeout CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <update id="update" parameterClass="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * ### after
     *
     * ```
     * <update id="update" parameterType="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * @param updateContext
     * @return
     */
    @Override
    public ContentContext visitUpdate(UpdateContext updateContext) {
        Element element = updateContext.element();
        String alias, realType;
        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue();
            realType = (String) updateContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) updateContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                updateContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }
        ContentContext contentContext = super.visitUpdate(updateContext);
        if (updateContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            updateContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        return contentContext;
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * parameterMap CDATA #IMPLIED --- DONE
     * parameterClass CDATA #IMPLIED --- DONE
     * timeout CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <insert id="insert" parameterClass="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * ### after
     *
     * ```
     * <insert id="insert" parameterType="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * @param insertContext
     * @return
     */
    @Override
    public ContentContext visitInsert(InsertContext insertContext) {
        Element element = insertContext.element();
        String alias, realType;
        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue();
            realType = (String) insertContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) insertContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                insertContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }
        ContentContext contentContext = super.visitInsert(insertContext);
        if (insertContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            insertContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        return contentContext;
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * parameterMap CDATA #IMPLIED --- DONE
     * parameterClass CDATA #IMPLIED --- DONE
     * timeout CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <delete id="insert" parameterClass="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * ### after
     *
     * ```
     * <delete id="insert" parameterType="indi.lujunjie.xxx.DTO" >
     * ```
     *
     * @param deleteContext
     * @return
     */
    @Override
    public ContentContext visitDelete(DeleteContext deleteContext) {
        Element element = deleteContext.element();
        String alias, realType;
        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue();
            realType = (String) deleteContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) deleteContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                deleteContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }
        ContentContext contentContext = super.visitDelete(deleteContext);
        if (deleteContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            deleteContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        return contentContext;
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isEmpty property="name" prepend="and" open="(" close=")">
     *   name = #name#
     * </isEmpty>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="name == null or '' == name">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isEmpty prepend="and" open="(" close=")">
     *   name = #value#
     * </isEmpty>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter == null or '' == _parameter)">
     *   and ( name = #value# )
     * </if>
     * ```
     * @param isEmptyContext
     * @return
     */
    @Override
    public ContentContext visitIsEmpty(IsEmptyContext isEmptyContext) {
        Element element = isEmptyContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        if (property == null) {
            element.addAttribute("test", "_parameter == null or _parameter == ''");
        } else {
            String propertyValue = this.handlePropertyTextCDataWithIterateContext(property.getValue(), isEmptyContext);
            element.addAttribute("test", propertyValue + " == null or '' == " + propertyValue);
            element.remove(property);
        }
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
            if (isEmptyContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isEmptyContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsEmpty(isEmptyContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isNotEmpty open="and (" property="name" close=")">
     *   name = #name#
     *   <isNotEmpty prepend="or" property="name2">
     *     name = #name2#
     *   </isNotEmpty>
     * </isNotEmpty>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="name != null and '' != name">
     *   and ( name = #name#
     *   <if test="name2 != null and '' != name2">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isNotEmpty open="and (" close=")">
     *   name = #name#
     * </isNotEmpty>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter != null and _parameter != ''">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isNotEmptyContext
     * @return
     */
    @Override
    public ContentContext visitIsNotEmpty(IsNotEmptyContext isNotEmptyContext) {
        Element element = isNotEmptyContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        if (property == null) {
            element.addAttribute("test", "_parameter != null and _parameter != ''");
        } else {
            String propertyValue = this.handlePropertyTextCDataWithIterateContext(property.getValue(), isNotEmptyContext);
            element.addAttribute("test", propertyValue + " != null and '' != " + propertyValue);
            element.remove(property);
        }
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
            if (isNotEmptyContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNotEmptyContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNotEmpty(isNotEmptyContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE(*)
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isNull open="and (" property="name" close=")">
     *   name = #name#
     *   <isNull prepend="or" property="name2">
     *     name = #name2#
     *   </isNull>
     * </isNull>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="name == null">
     *   and ( name = #name#
     *   <if test="name2 == null">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isNull open="and (" close=")">
     *   name = #name#
     * </isNull>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter == null">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isNullContext
     * @return
     */
    @Override
    public ContentContext visitIsNull(IsNullContext isNullContext) {
        Element element = isNullContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        if (property == null) {
            element.addAttribute("test", "_parameter == null");
        } else {
            element.addAttribute("test", this.handlePropertyTextCDataWithIterateContext(property.getValue(), isNullContext) + " == null");
            element.remove(property);
        }
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
            if (isNullContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNullContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNull(isNullContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE(*)
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isNotNull open="and (" property="name" close=")">
     *   name = #name#
     *   <isNotNull prepend="or" property="name2">
     *     name = #name2#
     *   </isNotNull>
     * </isNotNull>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="name != null">
     *   and ( name = #name#
     *   <if test="name2 != null">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isNotNull open="and (" close=")">
     *   name = #name#
     * </isNotNull>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter != null">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isNotNullContext
     * @return
     */
    @Override
    public ContentContext visitIsNotNull(IsNotNullContext isNotNullContext) {
        Element element = isNotNullContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        if (property == null) {
            element.addAttribute("test", "_parameter != null");
        } else {
            element.addAttribute("test", this.handlePropertyTextCDataWithIterateContext(property.getValue(), isNotNullContext) + " != null");
            element.remove(property);
        }
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
            if (isNotNullContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNotNullContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNotNull(isNotNullContext);
    }

    /**
     * https://ibatis.apache.org/docs/dotnet/datamapper/ch03s09.html
     * Binary Conditional Elements
     *
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isEqual open="and (" property="name" compareValue="foo" close=")">
     *   name = #name#
     *   <isEqual prepend="or" property="name2" compareProperty="compareName2">
     *     name = #name2#
     *   </isEqual>
     * </isEqual>
     * ```
     *
     * ### after
     *
     * ```
     *<if test="name != null and name == 'foo'.toString()">
     *   and ( name = #name#
     *   <if test="name2 != null and compareName2 != null and name2 == compareName2">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isEqual open="and (" compareValue="foo" close=")">
     *   name = #name#
     * </isEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter != null and _parameter == 'foo'.toString()">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isEqualContext
     * @return
     */
    @Override
    public ContentContext visitIsEqual(IsEqualContext isEqualContext) {
        Element element = isEqualContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isEqualContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " != null and " + compareProperty.getValue() + " != null and " + propertyValue + " == " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " != null and " + propertyValue + " == '" + compareValue.getValue() + "'.toString()");
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addAttribute("test", propertyValue + " != null and " + compareProperty.getValue() + " != null and " + propertyValue + " == " + compareProperty.getValue()
                                                + " and " + propertyValue + " == '" + compareValue.getValue() + "'.toString()");
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsEqual(isEqualContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isNotEqual open="and (" property="name" compareValue="foo" close=")">
     *   name = #name#
     *   <isNotEqual prepend="or" property="name2" compareProperty="compareName2">
     *     name = #name2#
     *   </isNotEqual>
     * </isNotEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="name != null and name != 'foo'.toString()">
     *   and ( name = #name#
     *   <if test="name2 != null and compareName2 != null and name2 != compareName2">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isNotEqual open="and (" compareValue="foo" close=")">
     *   name = #name#
     * </isNotEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter != null and _parameter != 'foo'.toString()">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isNotEqualContext
     * @return
     */
    @Override
    public ContentContext visitIsNotEqual(IsNotEqualContext isNotEqualContext) {
        Element element = isNotEqualContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isNotEqualContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " != null and " + compareProperty.getValue() + " != null and " + propertyValue + " != " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " != null and " + propertyValue + " != '" + compareValue.getValue() + "'.toString()");
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addAttribute("test", propertyValue + " != null and " + compareProperty.getValue() + " != null and " + propertyValue + " != " + compareProperty.getValue()
                    + " and " + propertyValue + " != '" + compareValue.getValue() + "'.toString()");
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isNotEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNotEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNotEqual(isNotEqualContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #REQUIRED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isPropertyAvailable open="and (" property="name" close=")">
     *   name = #name#
     *   <isPropertyAvailable prepend="or" property="name2">
     *     name = #name2#
     *   </isPropertyAvailable>
     * </isPropertyAvailable>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="(!_parameter instanceof java.util.Map) or (_parameter instanceof java.util.Map and _parameter.containsKey('name'))">
     *   and ( name = #name#
     *   <if test="(!_parameter instanceof java.util.Map) or (_parameter instanceof java.util.Map and _parameter.containsKey('name2'))">
     *     or name = #name2#
     *   </if> )
     * </if>
     * ```
     *
     * * ## case 2
     *
     * ### before
     *
     * ```
     * <isPropertyAvailable open="and (" close=")">
     *   name = #name#
     * </isPropertyAvailable>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="(!_parameter instanceof java.util.Map) or (_parameter instanceof java.util.Map and _parameter.containsKey('_parameter'))">
     *   and ( name = #name# )
     * </if>
     * ```
     *
     * @param isPropertyAvailableContext
     * @return
     */
    @Override
    public ContentContext visitIsPropertyAvailable(IsPropertyAvailableContext isPropertyAvailableContext) {
        Element element = isPropertyAvailableContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isPropertyAvailableContext);

        element.addAttribute("test", "(!_parameter instanceof java.util.Map) or (_parameter instanceof java.util.Map and _parameter.containsKey('" + propertyValue + "'))");
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
            if (isPropertyAvailableContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isPropertyAvailableContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsPropertyAvailable(isPropertyAvailableContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #REQUIRED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isNotPropertyAvailable open="and (" property="name" close=")">
     *   name = #name3#
     *   <isNotPropertyAvailable prepend="or" property="name2">
     *     name = #name4#
     *   </isNotPropertyAvailable>
     * </isNotPropertyAvailable>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter instanceof java.util.Map and !_parameter.containsKey('name')">
     *   and ( name = #name3#
     *   <if test="_parameter instanceof java.util.Map and !_parameter.containsKey('name2')">
     *     or name = #name4#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isNotPropertyAvailable open="and (" close=")">
     *   name = #name3#
     * </isNotPropertyAvailable>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter instanceof java.util.Map and !_parameter.containsKey('_parameter')">
     *   and ( name = #name3# )
     * </if>
     * ```
     *
     * @param isNotPropertyAvailableContext
     * @return
     */
    @Override
    public ContentContext visitIsNotPropertyAvailable(IsNotPropertyAvailableContext isNotPropertyAvailableContext) {
        Element element = isNotPropertyAvailableContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isNotPropertyAvailableContext);
        element.addAttribute("test", "_parameter instanceof java.util.Map and !_parameter.containsKey('" + propertyValue + "')");
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
            if (isNotPropertyAvailableContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNotPropertyAvailableContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNotPropertyAvailable(isNotPropertyAvailableContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isGreaterThan open="and (" property="id" compareValue="1" close=")">
     *   id = #id#
     *   <isGreaterThan prepend="or" property="id2" compareProperty="compareId2">
     *     id = #id2#
     *   </isGreaterThan>
     * </isGreaterThan>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="id gt 1">
     *   and ( id = #id#
     *   <if test="id2 gt compareId2))">
     *     or id = #id2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isGreaterThan open="and (" compareValue="1" close=")">
     *   id = #id#
     * </isGreaterThan>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter gt 1">
     *   and ( id = #id# )
     * </if>
     * ```
     *
     * @param isGreaterThanContext
     * @return
     */
    @Override
    public ContentContext visitIsGreaterThan(IsGreaterThanContext isGreaterThanContext) {
        Element element = isGreaterThanContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isGreaterThanContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " gt " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " gt " + compareValue.getValue());
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addElement("test", propertyValue + " gt " + compareProperty.getValue() + " and " + propertyValue + " gt " + compareValue.getValue());
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isGreaterThanContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isGreaterThanContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsGreaterThan(isGreaterThanContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isGreaterEqual open="and (" property="id" compareValue="1" close=")">
     *   id = #id#
     *   <isGreaterEqual prepend="or" property="id2" compareProperty="compareId2">
     *     id = #id2#
     *   </isGreaterEqual>
     * </isGreaterEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="id gte 1">
     *   and ( id = #id#
     *   <if test="id2 gte compareId2))">
     *     or id = #id2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isGreaterEqual open="and (" compareValue="1" close=")">
     *   id = #id#
     * </isGreaterEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter gte 1">
     *   and ( id = #id# )
     * </if>
     * ```
     *
     * @param isGreaterEqualContext
     * @return
     */
    @Override
    public ContentContext visitIsGreaterEqual(IsGreaterEqualContext isGreaterEqualContext) {
        Element element = isGreaterEqualContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isGreaterEqualContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " gte " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " gte " + compareValue.getValue());
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addElement("test", propertyValue + " gte " + compareProperty.getValue() + " and " + propertyValue + " gte " + compareValue.getValue());
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isGreaterEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isGreaterEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsGreaterEqual(isGreaterEqualContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isLessThan open="and (" property="id" compareValue="1" close=")">
     *   id = #id#
     *   <isLessThan prepend="or" property="id2" compareProperty="compareId2">
     *     id = #id2#
     *   </isLessThan>
     * </isLessThan>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="id lt 1">
     *   and ( id = #id#
     *   <if test="id2 lt compareId2))">
     *     or id = #id2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isLessThan open="and (" compareValue="1" close=")">
     *   id = #id#
     * </isLessThan>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter lt 1">
     *   and ( id = #id# )
     * </if>
     * ```
     * 
     * @param isLessThanContext
     * @return
     */
    @Override
    public ContentContext visitIsLessThan(IsLessThanContext isLessThanContext) {
        Element element = isLessThanContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isLessThanContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " lt " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " lt " + compareValue.getValue());
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addElement("test", propertyValue + " lt " + compareProperty.getValue() + " and " + propertyValue + " lt " + compareValue.getValue());
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isLessThanContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isLessThanContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsLessThan(isLessThanContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * property CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     * compareProperty CDATA #IMPLIED --- DONE
     * compareValue CDATA #IMPLIED --- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <isLessEqual open="and (" property="id" compareValue="1" close=")">
     *   id = #id#
     *   <isLessEqual prepend="or" property="id2" compareProperty="compareId2">
     *     id = #id2#
     *   </isLessEqual>
     * </isLessEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="id lte 1">
     *   and ( id = #id#
     *   <if test="id2 lte compareId2))">
     *     or id = #id2#
     *   </if> )
     * </if>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <isLessEqual open="and (" compareValue="1" close=")">
     *   id = #id#
     * </isLessEqual>
     * ```
     *
     * ### after
     *
     * ```
     * <if test="_parameter lte 1">
     *   and ( id = #id# )
     * </if>
     * ```
     *
     * @param isLessEqualContext
     * @return
     */
    @Override
    public ContentContext visitIsLessEqual(IsLessEqualContext isLessEqualContext) {
        Element element = isLessEqualContext.element();
        element.setName("if");
        Attribute property = element.attribute("property");
        String propertyValue = "_parameter";
        if (property != null) {
            propertyValue = property.getValue();
            element.remove(property);
        }
        propertyValue = this.handlePropertyTextCDataWithIterateContext(propertyValue, isLessEqualContext);

        Attribute compareProperty = element.attribute("compareProperty");
        Attribute compareValue = element.attribute("compareValue");
        if (compareProperty != null && compareValue == null) {
            element.addAttribute("test", propertyValue + " lte " + compareProperty.getValue());
            element.remove(compareProperty);
        } else if (compareProperty == null && compareValue != null) {
            element.addAttribute("test", propertyValue + " lte " + compareValue.getValue());
            element.remove(compareValue);
        } else if (compareProperty != null && compareValue != null) {
            element.addElement("test", propertyValue + " lte " + compareProperty.getValue() + " and " + propertyValue + " lte " + compareValue.getValue());
            element.remove(compareProperty);
            element.remove(compareValue);
        }
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
            if (isLessEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isLessEqualContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsLessEqual(isLessEqualContext);
    }

    /**
     * prepend CDATA #IMPLIED ---- DONE
     * property CDATA #IMPLIED ----DONE
     * removeFirstPrepend (true|false|iterate) #IMPLIED  ----TODO
     * open CDATA #IMPLIED  ---- DONE
     * close CDATA #IMPLIED  ---- DONE
     * conjunction CDATA #IMPLIED ---- DONE
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * <iterate property="list" open="(" close=") " conjunction=" OR " >
     *   (name = #list[].name#
     *   <iterate property="list[].subList" prepend="id IN" open="(" close=") " conjunction="," >
     *     #list[].subList[].id#
     *   </iterate>
     *   )
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = #{item0.name}
     *   <foreach item="item1" collection="item0.subList" open="id IN (" close=") " separator="," >
     *     #item1.id#
     *   </foreach>
     *   )
     * </foreach>
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * <iterate open="(" close=") " conjunction=" OR " >
     *   (name = #[].name#)
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = #{item0.name})
     * </foreach>
     * ```
     *
     * ## case 3
     *
     * ### before
     *
     * ```
     * <iterate open="(" close=") " conjunction=" OR " >
     *   (name = #anyName[]#)
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = #{item0})
     * </foreach>
     * ```
     *
     * ## case 4
     *
     * ### before
     *
     * ```
     * <iterate open="(" close=") " conjunction=" OR " >
     *   (name = #anyName[].name#)
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = #item0.name#)
     * </foreach>
     * ```
     *
     * ## case 5
     *
     * ### before
     *
     * ```
     * <iterate open="(" close=") " conjunction=" OR " >
     *   (name = $anyName[]$)
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = ${item0})
     * </foreach>
     * ```
     *
     * ## case 6
     *
     * ### before
     *
     * ```
     * <iterate open="(" close=") " conjunction=" OR " >
     *   (name = $anyName[].name$)
     * </iterate>
     * ```
     *
     * ### after
     *
     * ```
     * <foreach item="item0" collection="list" open="(" close=") " separator=" OR " >
     *   (name = ${item0.name})
     * </foreach>
     * ```
     *
     * @param iterateContext
     * @return
     */
    @Override
    public ContentContext visitIterate(IterateContext iterateContext) {
        Element element = iterateContext.element();
        element.setName("foreach");
        Attribute conjunction = element.attribute("conjunction");
        if(conjunction != null){
            element.addAttribute("separator", conjunction.getValue());
            element.remove(conjunction);
        }
        Attribute prepend = element.attribute("prepend");
        Attribute open = element.attribute("open");
        if (prepend != null) {
            if (iterateContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) iterateContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            if (open == null) {
                element.addAttribute("open", prepend.getValue());
                element.remove(prepend);
            } else {
                open.setValue(prepend.getValue() + " " + open.getValue());
                element.remove(prepend);
            }
        }

        Attribute property = element.attribute("property");
        String iBatisIterateProperty;
        String mybatisIterateItem;
        if (property == null) {
            iBatisIterateProperty = "";
            mybatisIterateItem = "item0";
            element.addAttribute("collection", "list");
            element.addAttribute("item", mybatisIterateItem);
        } else {
            iBatisIterateProperty = property.getValue();
            mybatisIterateItem = "item" + iterateContext.getGroup(CONTEXT_GROUP_ITERATE).size();
            element.addAttribute("collection", this.handlePropertyTextCDataWithIterateContext(iBatisIterateProperty, iterateContext));
            element.addAttribute("item", mybatisIterateItem);
            element.remove(property);
        }
        iterateContext.set(CONTEXT_GROUP_ITERATE, iBatisIterateProperty, mybatisIterateItem);
        super.visitChildren(iterateContext);
        iterateContext.remove(CONTEXT_GROUP_ITERATE, iBatisIterateProperty);
        return iterateContext;
    }

    /**
     * resultClass CDATA #IMPLIED --- DONE
     * keyProperty CDATA #IMPLIED --- DONE
     * type (pre|post) #IMPLIED --- DONE
     *
     * @param selectKeyContext
     * @return
     */
    @Override
    public ContentContext visitSelectKey(SelectKeyContext selectKeyContext) {
        Element element = selectKeyContext.element();
        Attribute resultClassAttribute = element.attribute("resultClass");
        String alias, realType;
        if (resultClassAttribute != null) {
            alias = resultClassAttribute.getValue().trim();
            realType = (String) selectKeyContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) selectKeyContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            element.addAttribute("resultType", realType);
            element.remove(resultClassAttribute);
        }
        Attribute type = element.attribute("type");
        if (type != null) {
            if ("pre".equalsIgnoreCase(type.getValue())) {
                element.addAttribute("order", "BEFORE");
            } else if ("post".equalsIgnoreCase(type.getValue())) {
                element.addAttribute("order", "AFTER");
            }
            element.remove(type);
        }

        return super.visitSelectKey(selectKeyContext);
    }

    /**
     *
     * ## case 1
     *
     * ### before
     *
     * ```
     * #id#
     * ```
     *
     * ### after
     *
     * ```
     * #{id}
     * ```
     *
     * ## case 2
     *
     * ### before
     *
     * ```
     * $id$
     * ```
     *
     * ### after
     *
     * ```
     * ${id}
     *
     *
     * ## case 3
     *
     * when `parameterType` is primitive or wrapper class in <select>, <insert>, <update> , <delete> , <statement>, <procedure> tag
     *
     * ### before
     *
     * ```
     * <select id="find" parameterType="String" resultType="Map">
     *     ・・・
     *     #value#
     *     $value$
     *     ・・・
     * </select>
     * ```
     *
     * ### after
     *
     * ```
     * <select id="find" parameterType="String" resultType="Map">
     *     ・・・
     *     #{_parameter}
     *     ${_parameter}
     *     ・・・
     * </select>
     * ```
     *
     * @param textContext
     * @return
     */
    @Override
    public ContentContext visitText(TextContext textContext) {
        Text text = textContext.text();
        StringBuffer buffer = (StringBuffer) textContext.get(CONTEXT_GROUP_ITERATE, CURRENT);
        buffer = buffer == null ? new StringBuffer() : buffer;
        buffer.append(text.getText());
        Matcher hashM = hashPattern.matcher(buffer.toString());
        if (hashM.replaceAll("").length() % 2 == 0) {
            textContext.remove(CONTEXT_GROUP_ITERATE, CURRENT);
            StringBuffer textBuffer = new StringBuffer();
            Matcher variableM = variablePattern.matcher(buffer);
            while (variableM.find()) {
                variableM.start();
                variableM.end();
                String variable = this.handlePropertyTextCDataWithIterateContext(variableM.group(2), textContext);
                String replacementPrefix = "$".equalsIgnoreCase(variableM.group(1)) ? "\\$" : variableM.group(1);
                variableM.appendReplacement(textBuffer, replacementPrefix + "\\{" + variable + "\\}");
            }
            variableM.appendTail(textBuffer);
            text.setText(textBuffer.toString());
        } else {
            textContext.set(CONTEXT_GROUP_ITERATE, CURRENT, buffer);
            textContext.removeSelf(true);
        }
        return textContext;
    }

    @Override
    public ContentContext visitCData(CDataContext cDataContext) {
        CDATA cdata = cDataContext.cdata();
        StringBuffer cdataBuffer = new StringBuffer();
        Matcher variableM = variablePattern.matcher(cdata.getText());
        while (variableM.find()) {
            variableM.start();
            variableM.end();
            String variable = this.handlePropertyTextCDataWithIterateContext(variableM.group(2), cDataContext);
            String replacementPrefix = "$".equalsIgnoreCase(variableM.group(1)) ? "\\$" : variableM.group(1);
            variableM.appendReplacement(cdataBuffer, replacementPrefix + "\\{" + variable + "\\}");
        }
        variableM.appendTail(cdataBuffer);
        cdata.setText(cdataBuffer.toString());
        return cDataContext;
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     *
     * @param dynamicContext
     * @return
     */
    @Override
    public ContentContext visitDynamic(DynamicContext dynamicContext) {
        Element element = dynamicContext.element();
        element.setName("trim");
        String prefix = null;
        Attribute prepend = element.attribute("prepend");
        Attribute open = element.attribute("open");
        if (prepend != null && open == null) {
            prefix = prepend.getValue();
            element.remove(prepend);
        } else if (prepend == null && open != null) {
            prefix = open.getValue();
            element.remove(open);
        } else if (prepend != null && open != null) {
            prefix = prepend.getValue() + " " + open.getValue();
            element.remove(prepend);
            element.remove(open);
        }
        if (prefix != null) {
            element.addAttribute("prefix", prefix);
        }

        Attribute close = element.attribute("close");
        if (close != null) {
            element.addAttribute("suffix", close.getValue());
            element.remove(close);
        }

        dynamicContext.set(CONTEXT_GROUP_DYNAMIC, CURRENT, new HashSet<>());
        super.visitDynamic(dynamicContext);
        Set<String> subPrepends = (Set<String>) dynamicContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT);
        dynamicContext.remove(CONTEXT_GROUP_DYNAMIC, CURRENT);
        if (subPrepends.size() > 0) {
            element.addAttribute("prefixOverrides", StringUtils.join(subPrepends, "|"));
        }
        return dynamicContext;
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * class CDATA #REQUIRED --- DONE
     * extends CDATA #IMPLIED --- DONE
     * xmlName CDATA #IMPLIED --- DONE
     * groupBy CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <resultMap id="findResult" class="HashMap" groupBy="id">
     * </resultMap>
     * ```
     *
     * ### after
     *
     * ```
     * <resultMap id="findResult" type="HashMap">
     * </resultMap>
     * ```
     *
     * @param resultMapContext
     * @return
     */
    @Override
    public ContentContext visitResultMap(ResultMapContext resultMapContext) {
        Element element = resultMapContext.element();
        Attribute clazz = element.attribute("class");
        String alias, realType;
        if (clazz != null) {
            alias = clazz.getValue();
            realType = (String) resultMapContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) resultMapContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            element.addAttribute("type", realType);
            element.remove(clazz);
        }
        Attribute xmlName = element.attribute("xmlName");
        if (xmlName != null) {
            element.remove(xmlName);
        }
        Attribute groupBy = element.attribute("groupBy");
        if (groupBy != null) {
            element.remove(groupBy);
        }
        return super.visitResultMap(resultMapContext);
    }

    /**
     * property CDATA #REQUIRED --- DONE
     * javaType CDATA #IMPLIED --- DONE
     * column CDATA #IMPLIED --- DONE
     * columnIndex CDATA #IMPLIED --- TODO
     * jdbcType CDATA #IMPLIED --- DONE
     * nullValue CDATA #IMPLIED --- TODO
     * notNullColumn CDATA #IMPLIED --- TODO
     * select CDATA #IMPLIED --- DONE
     * resultMap CDATA #IMPLIED ---DONE
     * typeHandler CDATA #IMPLIED --- DONE
     *
     * ### before
     *
     * ```
     * <resultMap id="result" class="Map">
     *   <result property="id" column="id" javaType="int" />
     *   <result property="details" javaType="List" resultMap="indi.lujunjie.xxx.subRM" />
     *   <result property="address" resultMap="indi.lujunjie.xxx.subRM" />
     *   <result property="address" select="indi.lujunjie.xxx.subStatement" column="columnId"/>
     * </resultMap>
     * <resultMap id="subRM" class="Map">
     *   <result property="id" column="id" javaType="int" />
     *   <result property="name" column="name" javaType="String" />
     * </resultMap>
     * ```
     *
     * ### after
     *
     * ```
     * <resultMap id="result" class="Map">
     *   <result property="id" column="id" javaType="int" />
     *   <collection property="details" javaType="List" resultMap="subRM" />
     *   <collection property="address" resultMap="indi.lujunjie.xxx.subRM" />
     *   <association property="address" select="indi.lujunjie.xxx.subStatement" column="columnId"/>
     * </resultMap>
     * <resultMap id="subRM" class="Map">
     *   <result property="id" column="id" javaType="int" />
     *   <result property="name" column="name" javaType="String" />
     * </resultMap>
     * ```
     *
     * @param resultContext
     * @return
     */
    @Override
    public ContentContext visitResult(ResultContext resultContext) {
        Element element = resultContext.element();
        Attribute resultMap = element.attribute("resultMap");
        if (resultMap != null) {
            element.setName("collection");
        }
        Attribute select = element.attribute("select");
        if (select != null) {
            element.setName("association");
        }
        return super.visitResult(resultContext);
    }

    /**
     * id CDATA #REQUIRED --- DONE
     *
     * @param sqlContext
     * @return
     */
    @Override
    public ContentContext visitSql(SqlContext sqlContext) {
        return super.visitSql(sqlContext);
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * class CDATA #REQUIRED --- DONE
     *
     * @param parameterMapContext
     * @return
     */
    @Override
    public ContentContext visitParameterMap(ParameterMapContext parameterMapContext) {
        Element element = parameterMapContext.element();
        Attribute clazz = element.attribute("class");
        if (clazz != null) {
            element.addAttribute("type", clazz.getValue());
            element.remove(clazz);
        }
        return super.visitParameterMap(parameterMapContext);
    }

    /**
     * property CDATA #REQUIRED --- DONE
     * javaType CDATA #IMPLIED --- DONE
     * jdbcType CDATA #IMPLIED --- DONE
     * typeName CDATA #IMPLIED --- TODO
     * nullValue CDATA #IMPLIED --- TODO
     * mode (IN | OUT | INOUT) #IMPLIED --- DONE
     * typeHandler CDATA #IMPLIED --- DONE
     * resultMap CDATA #IMPLIED --- DONE
     * numericScale CDATA #IMPLIED --- DONE
     *
     * @param parameterContext
     * @return
     */
    @Override
    public ContentContext visitParameter(ParameterContext parameterContext) {
        Element element = parameterContext.element();
        Attribute numericScale = element.attribute("numericScale");
        if (numericScale != null) {
            element.addAttribute("scale", numericScale.getValue());
            element.remove(numericScale);
        }
        return super.visitParameter(parameterContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * @param isParameterPresentContext
     * @return
     */
    @Override
    public ContentContext visitIsParameterPresent(IsParameterPresentContext isParameterPresentContext) {
        Element element = isParameterPresentContext.element();
        element.setName("if");
        element.addAttribute("test", "_parameter != null");
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
            if (isParameterPresentContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isParameterPresentContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsParameterPresent(isParameterPresentContext);
    }

    /**
     * prepend CDATA #IMPLIED --- DONE
     * open CDATA #IMPLIED --- DONE
     * close CDATA #IMPLIED --- DONE
     * removeFirstPrepend (true|false) #IMPLIED --- TODO
     *
     * @param isNotParameterPresentContext
     * @return
     */
    @Override
    public ContentContext visitIsNotParameterPresent(IsNotParameterPresentContext isNotParameterPresentContext) {
        Element element = isNotParameterPresentContext.element();
        element.setName("if");
        element.addAttribute("test", "_parameter == null");
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
            if (isNotParameterPresentContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT) != null) {
                ((Set) isNotParameterPresentContext.get(CONTEXT_GROUP_DYNAMIC, CURRENT)).add(prepend.getValue());
            }
            element.content().add(0, new DefaultText(element, prepend.getValue()));
            element.remove(prepend);
        }
        return super.visitIsNotParameterPresent(isNotParameterPresentContext);
    }

    /**
     * xmlns:fo CDATA #IMPLIED --- TODO
     *
     * @param sqlMapConfigContext
     * @return
     */
    @Override
    public ContentContext visitSqlMapConfig(SqlMapConfigContext sqlMapConfigContext) {
        Element element = sqlMapConfigContext.element();
        element.setName("configuration");

        List content = element.content();
        List<Object> mapperList = new ArrayList<>();
        List<Object> newContent = new ArrayList<>();
        Element mappersElement = null;
        for (Object item : content) {
            if (item instanceof Element && "sqlmap".equalsIgnoreCase(((Element) item).getName())) {
                if (mapperList.isEmpty()) {
                    mappersElement = new DefaultElement("mappers");
                    newContent.add(mappersElement);
                }
                Element itemElement = (Element) item;
                itemElement.setParent(mappersElement);
                mapperList.add(itemElement);
            } else if (!((item instanceof Text) && ((Text) item).getText().trim().isEmpty())){
                newContent.add(item);
            }
        }
        if (mappersElement != null) {
            mappersElement.setContent(mapperList);
        }
        element.setContent(newContent);
        return super.visitSqlMapConfig(sqlMapConfigContext);
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * parameterMap CDATA #IMPLIED --- DONE
     * parameterClass CDATA #IMPLIED --- DONE
     * resultMap CDATA #IMPLIED --- DONE
     * resultClass CDATA #IMPLIED --- DONE
     * cacheModel CDATA #IMPLIED --- DONE
     * fetchSize CDATA #IMPLIED --- DONE
     * xmlResultName CDATA #IMPLIED --- TODO
     * remapResults (true|false) #IMPLIED --- TODO
     * conflict with procedure. statementType = Statement or Callable?
     * timeout CDATA #IMPLIED --- DONE
     *
     * @param procedureContext
     * @return
     */
    @Override
    public ContentContext visitProcedure(ProcedureContext procedureContext) {
        Element element = procedureContext.element();
        element.setName("select");
        Attribute resultClassAttribute = element.attribute("resultClass");
        String alias, realType;
        if (resultClassAttribute != null) {
            alias = resultClassAttribute.getValue().trim();
            realType = (String) procedureContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) procedureContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            element.addAttribute("resultType", realType);
            element.remove(resultClassAttribute);
        }

        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue().trim();
            realType = (String) procedureContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) procedureContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                procedureContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }

        ContentContext contentContext = super.visitProcedure(procedureContext);
        if (procedureContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            procedureContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        Attribute remapResults = element.attribute("remapResults");
        if (remapResults != null) {
//            if (Boolean.TRUE.toString().equalsIgnoreCase(remapResults.getValue())) {
//                element.addAttribute("statementType", "STATEMENT");
//            }
            element.remove(remapResults);
        }
        element.addAttribute("statementType", "CALLABLE");
        Attribute cacheModelAttribute = element.attribute("cacheModel");
        if (cacheModelAttribute == null) {
            return contentContext;
        } else {
            element.remove(cacheModelAttribute);
            CacheModel cacheModel = (CacheModel) procedureContext.get(CONTEXT_GROUP_CACHEMODEL, cacheModelAttribute.getValue());
            if (cacheModel != null) {
                element.addAttribute("useCache", "true");
                for (FlushOnExecute flushOnExecute : cacheModel.flushOnExecutes()) {
                    if (element.attributeValue("id").equalsIgnoreCase(flushOnExecute.statement())) {
                        element.addAttribute("flushCache", "true");
                    }
                }
            }
            Document document = (Document) element.getDocument().clone();
            DefaultElement cache = new DefaultElement("cache");
            if (cacheModel.type() != null) {
                cache.addAttribute("type", cacheModel.type());
            }
            if (cacheModel.readOnly() != null) {
                cache.addAttribute("readOnly", String.valueOf(cacheModel.readOnly()));
            }
            if (cacheModel.flushInterval() != null) {
                cache.addAttribute("flushInterval", cacheModel.flushInterval().getTime());
            }
            for (Property property : cacheModel.properties()) {
                Element e = new DefaultElement("property");
                e.addAttribute("name", property.name());
                e.addAttribute("value", property.value());
                cache.add(e);
            }
            Element sqlMapElementClone = document.getRootElement();
            sqlMapElementClone.clearContent();
            sqlMapElementClone.add(cache);
            sqlMapElementClone.add((Element) element.clone());
            procedureContext.additionalFileWrapper(procedureContext.fileWrapper().newAdditionalFileWrapper(element.attributeValue("id"), document));
            procedureContext.removeRecursive(true);
            return procedureContext;
        }
    }

    /**
     * javaType CDATA #REQUIRED --- DONE
     * column CDATA #IMPLIED --- DONE
     * columnIndex CDATA #IMPLIED --- TODO
     * jdbcType CDATA #IMPLIED --- DONE
     * nullValue CDATA #IMPLIED --- TODO
     * typeHandler CDATA #IMPLIED --- DONE
     *
     * @param discriminatorContext
     * @return
     */
    @Override
    public ContentContext visitDiscriminator(DiscriminatorContext discriminatorContext) {
        return super.visitDiscriminator(discriminatorContext);
    }

    /**
     * value CDATA #REQUIRED --- DONE
     * resultMap CDATA #REQUIRED --- DONE
     *
     * @param subMapContext
     * @return
     */
    @Override
    public ContentContext visitSubMap(SubMapContext subMapContext) {
        Element element = subMapContext.element();
        element.setName("case");
        return super.visitSubMap(subMapContext);
    }

    /**
     * refid CDATA #REQUIRED --- DONE
     *
     * @param includeContext
     * @return
     */
    @Override
    public ContentContext visitInclude(IncludeContext includeContext) {
        return super.visitInclude(includeContext);
    }

    /**
     * {@link IBatisVisitor#visitSelect(SelectContext)}
     *
     * @param statementContext
     * @return
     */
    @Override
    public ContentContext visitStatement(StatementContext statementContext) {
        Element element = statementContext.element();
        element.setName("select");
        Attribute resultClassAttribute = element.attribute("resultClass");
        String alias, realType;
        if (resultClassAttribute != null) {
            alias = resultClassAttribute.getValue().trim();
            realType = (String) statementContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) statementContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            if (isPrimitive(realType)) {
                statementContext.set(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT, Boolean.TRUE);
            }
            element.addAttribute("resultType", realType);
            element.remove(resultClassAttribute);
        }

        Attribute parameterClassAttribute = element.attribute("parameterClass");
        if (parameterClassAttribute != null) {
            alias = parameterClassAttribute.getValue().trim();
            realType = (String) statementContext.get(CONTEXT_GROUP_TYPEALIAS, alias);
            if (realType == null) {
                realType = (String) statementContext.get(CONTEXT_GROUP_TYPEALIAS, alias.toLowerCase());
                realType = realType == null ? alias : realType;
            }
            element.addAttribute("parameterType", realType);
            element.remove(parameterClassAttribute);
        }
        ContentContext contentContext = super.visitStatement(statementContext);
        if (statementContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null) {
            statementContext.remove(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT);
        }
        Attribute remapResults = element.attribute("remapResults");
        if (remapResults != null) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(remapResults.getValue())) {
                element.addAttribute("statementType", "STATEMENT");
            }
            element.remove(remapResults);
        }
        Attribute cacheModelAttribute = element.attribute("cacheModel");
        if (cacheModelAttribute == null) {
            return contentContext;
        } else {
            element.remove(cacheModelAttribute);
            CacheModel cacheModel = (CacheModel) statementContext.get(CONTEXT_GROUP_CACHEMODEL, cacheModelAttribute.getValue());
            if (cacheModel != null) {
                element.addAttribute("useCache", "true");
                for (FlushOnExecute flushOnExecute : cacheModel.flushOnExecutes()) {
                    if (element.attributeValue("id").equalsIgnoreCase(flushOnExecute.statement())) {
                        element.addAttribute("flushCache", "true");
                    }
                }
            }
            Document document = (Document) element.getDocument().clone();
            DefaultElement cache = new DefaultElement("cache");
            if (cacheModel.type() != null) {
                cache.addAttribute("type", cacheModel.type());
            }
            if (cacheModel.readOnly() != null) {
                cache.addAttribute("readOnly", String.valueOf(cacheModel.readOnly()));
            }
            if (cacheModel.flushInterval() != null) {
                cache.addAttribute("flushInterval", cacheModel.flushInterval().getTime());
            }
            for (Property property : cacheModel.properties()) {
                Element e = new DefaultElement("property");
                e.addAttribute("name", property.name());
                e.addAttribute("value", property.value());
                cache.add(e);
            }
            Element sqlMapElementClone = document.getRootElement();
            sqlMapElementClone.clearContent();
            sqlMapElementClone.add(cache);
            sqlMapElementClone.add((Element) element.clone());
            statementContext.additionalFileWrapper(statementContext.fileWrapper().newAdditionalFileWrapper(element.attributeValue("id"), document));
            statementContext.removeRecursive(true);
            return statementContext;
        }
    }

    /**
     * id CDATA #REQUIRED --- DONE
     * type CDATA #REQUIRED --- DONE
     * readOnly (true | false) #IMPLIED --- DONE
     * serialize (true | false) #IMPLIED --- DONE
     *
     * @param cacheModelContext
     * @return
     */
    @Override
    public ContentContext visitCacheModel(CacheModelContext cacheModelContext) {
        Element element = cacheModelContext.element();
        String id = element.attributeValue("id");
        String type = element.attributeValue("type");
        Boolean readOnly = element.attribute("readOnly") == null ? null : Boolean.valueOf(element.attributeValue("readOnly"));
        Boolean serialize = element.attribute("serialize") == null ? null : Boolean.valueOf(element.attributeValue("serialize"));
        cacheModelContext.set(CONTEXT_GROUP_CACHEMODEL, CURRENT, new CacheModel().id(id).type(type).readOnly(readOnly).serialize(serialize));
        super.visitCacheModel(cacheModelContext);
        CacheModel cacheModel = (CacheModel) cacheModelContext.get(CONTEXT_GROUP_CACHEMODEL, CURRENT);
        cacheModelContext.set(CONTEXT_GROUP_CACHEMODEL, cacheModel.id(), cacheModel);
        cacheModelContext.remove(CONTEXT_GROUP_CACHEMODEL, CURRENT);
        cacheModelContext.removeRecursive(true);
        return cacheModelContext;
    }

    /**
     * milliseconds CDATA #IMPLIED --- DONE
     * seconds CDATA #IMPLIED --- DONE
     * minutes CDATA #IMPLIED --- DONE
     * hours CDATA #IMPLIED --- DONE
     *
     * @param flushIntervalContext
     * @return
     */
    @Override
    public ContentContext visitFlushInterval(FlushIntervalContext flushIntervalContext) {
        Element element = flushIntervalContext.element();
        FlushInterval flushInterval = new FlushInterval().milliseconds(element.attributeValue("milliseconds"))
                                                        .seconds(element.attributeValue("seconds"))
                                                        .minutes(element.attributeValue("minutes"))
                                                        .hours(element.attributeValue("hours"));
        CacheModel current = (CacheModel) flushIntervalContext.get(CONTEXT_GROUP_CACHEMODEL, CURRENT);
        flushIntervalContext.set(CONTEXT_GROUP_CACHEMODEL, CURRENT, current.flushInterval(flushInterval));
        flushIntervalContext.removeSelf(true);
        return super.visitFlushInterval(flushIntervalContext);
    }

    /**
     * statement CDATA #REQUIRED --- DONE
     *
     * @param flushOnExecuteContext
     * @return
     */
    @Override
    public ContentContext visitFlushOnExecute(FlushOnExecuteContext flushOnExecuteContext) {
        Element element = flushOnExecuteContext.element();
        FlushOnExecute flushOnExecute = new FlushOnExecute().statement(element.attributeValue("statement"));
        CacheModel current = (CacheModel) flushOnExecuteContext.get(CONTEXT_GROUP_CACHEMODEL, CURRENT);
        flushOnExecuteContext.set(CONTEXT_GROUP_CACHEMODEL, CURRENT, current.flushOnExecute(flushOnExecute));
        flushOnExecuteContext.removeSelf(true);
        return super.visitFlushOnExecute(flushOnExecuteContext);
    }

    /**
     * a[].b[].c -> get("a[].b")+".c"
     *
     * [] -> get("")
     *
     * a[] -> get("a")
     *
     * a -> a
     *
     * @param content
     * @return
     */
    private String handlePropertyTextCDataWithIterateContext(String content, ContentContext contentContext) {
        String result;
        if (!content.contains("[]")) {
            result = content;
        } else {
            String prefix = content.substring(0, content.lastIndexOf("[]"));
            String suffix = content.substring(content.lastIndexOf("[]") + 2);
            String mybatisItem = (String) contentContext.get(CONTEXT_GROUP_ITERATE, prefix);
            //parent's iterate context doesn't specify property attribute, we use "" as default
            mybatisItem = mybatisItem == null ? (String) contentContext.get(CONTEXT_GROUP_ITERATE, "") : mybatisItem;
            result = mybatisItem + suffix;
        }
        return contentContext.get(CONTEXT_GROUP_PRIMITIVE_PARAMETER, CURRENT) != null ? "_parameter" : result;
    }

    private boolean isPrimitive(String type) {
        return type == null ? false : primitiveTypes.contains(type.toLowerCase());
    }

    //************************** sql map config ******************************
    //************************** start ***************************************

    /**
     * resource CDATA #IMPLIED --- DONE
     * url CDATA #IMPLIED --- DONE
     *
     * @param propertiesContext
     * @return
     */
    @Override
    public ContentContext visitProperties(PropertiesContext propertiesContext) {
        return super.visitProperties(propertiesContext);
    }

    /**
     * name CDATA #REQUIRED --- DONE
     * value CDATA #REQUIRED --- DONE
     *
     * @param propertyContext
     * @return
     */
    @Override
    public ContentContext visitProperty(PropertyContext propertyContext) {
        CacheModel cacheModel = (CacheModel) propertyContext.get(CONTEXT_GROUP_CACHEMODEL, CURRENT);
        if (cacheModel != null); {
            Element element = propertyContext.element();
            cacheModel.property(new Property().name(element.attributeValue("name"))
                                            .value(element.attributeValue("value")));
            propertyContext.set(CONTEXT_GROUP_CACHEMODEL, CURRENT, cacheModel);
            propertyContext.removeSelf(true);
        }
        return super.visitProperty(propertyContext);
    }

    /**
     * https://mybatis.org/mybatis-3/configuration.html#settings
     *
     * unable to adjust all attributes
     *
     * classInfoCacheEnabled (true | false) #IMPLIED --- TODO
     * lazyLoadingEnabled (true | false) #IMPLIED --- DONE
     * statementCachingEnabled (true | false) #IMPLIED --- TODO (localCacheScope=STATEMENT ?)
     * cacheModelsEnabled (true | false) #IMPLIED --- DONE
     * enhancementEnabled (true | false) #IMPLIED --- TODO
     * errorTracingEnabled (true | false) #IMPLIED --- TODO
     * useStatementNamespaces (true | false) #IMPLIED --- TODO
     * useColumnLabel (true | false) #IMPLIED --- DONE
     * forceMultipleResultSetSupport (true | false) #IMPLIED --- DONE
     * maxSessions CDATA #IMPLIED --- TODO
     * maxTransactions CDATA #IMPLIED --- TODO
     * maxRequests CDATA #IMPLIED --- TODO
     * defaultStatementTimeout CDATA #IMPLIED --- DONE
     *
     * @param settingsContext
     * @return
     */
    @Override
    public ContentContext visitSettings(SettingsContext settingsContext) {
        Element element = settingsContext.element();
        Function<SettingWrapper, Object> fn = settingWrapper -> {
            if (settingWrapper.getAttribute() != null) {
                if (settingWrapper.getAlias() != null) {
                    DefaultElement setting = new DefaultElement("setting");
                    setting.addAttribute("name", settingWrapper.getAlias());
                    setting.addAttribute("value", settingWrapper.getAttribute().getValue());
                    element.content().add(0, setting);
                }
                element.remove(settingWrapper.getAttribute());
            }
            return null;
        };
        fn.apply(new SettingWrapper(element.attribute("classInfoCacheEnabled"), null));
        fn.apply(new SettingWrapper(element.attribute("lazyLoadingEnabled"), "lazyLoadingEnabled"));
        fn.apply(new SettingWrapper(element.attribute("statementCachingEnabled"), null));
        fn.apply(new SettingWrapper(element.attribute("cacheModelsEnabled"), "cacheEnabled"));
        fn.apply(new SettingWrapper(element.attribute("enhancementEnabled"), null));
        fn.apply(new SettingWrapper(element.attribute("errorTracingEnabled"), null));
        fn.apply(new SettingWrapper(element.attribute("useStatementNamespaces"), null));
        fn.apply(new SettingWrapper(element.attribute("useColumnLabel"), "useColumnLabel"));
        fn.apply(new SettingWrapper(element.attribute("forceMultipleResultSetSupport"), "multipleResultSetsEnabled"));
        fn.apply(new SettingWrapper(element.attribute("maxSessions"), null));
        fn.apply(new SettingWrapper(element.attribute("maxTransactions"), null));
        fn.apply(new SettingWrapper(element.attribute("maxRequests"), null));
        fn.apply(new SettingWrapper(element.attribute("defaultStatementTimeout"), "defaultStatementTimeout"));
        return super.visitSettings(settingsContext);
    }

    class SettingWrapper {

        private Attribute attribute;

        private String alias;

        public SettingWrapper(Attribute attribute, String alias) {
            this.attribute = attribute;
            this.alias = alias;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public String getAlias() {
            return alias;
        }
    }

    /**
     * type CDATA #REQUIRED --- DONE
     *
     * @param resultObjectFactoryContext
     * @return
     */
    @Override
    public ContentContext visitResultObjectFactory(ResultObjectFactoryContext resultObjectFactoryContext) {
        Element element = resultObjectFactoryContext.element();
        element.setName("objectFactory");
        return super.visitResultObjectFactory(resultObjectFactoryContext);
    }

    /**
     * javaType CDATA #REQUIRED --- DONE
     * jdbcType CDATA #IMPLIED --- DONE
     * callback CDATA #REQUIRED --- DONE
     *
     * @param typeHandlerContext
     * @return
     */
    @Override
    public ContentContext visitTypeHandler(TypeHandlerContext typeHandlerContext) {
        Element element = typeHandlerContext.element();
        Attribute callback = element.attribute("callback");
        element.addAttribute("handler", callback.getValue());
        element.remove(callback);
        return super.visitTypeHandler(typeHandlerContext);
    }

    /**
     * type CDATA #REQUIRED --- DONE
     * commitRequired (true | false) #IMPLIED --- DONE
     *
     * @param transactionManagerContext
     * @return
     */
    @Override
    public ContentContext visitTransactionManager(TransactionManagerContext transactionManagerContext) {
        Element element = transactionManagerContext.element();
        Attribute commitRequired = element.attribute("commitRequired");
        if (commitRequired != null) {
            DefaultElement setting = new DefaultElement("property");
            setting.addAttribute("name", "commitRequired");
            setting.addAttribute("value", commitRequired.getValue());
            element.content().add(0, setting);
            element.remove(commitRequired);
        }
        return super.visitTransactionManager(transactionManagerContext);
    }

    //************************** end   ***************************************
}
