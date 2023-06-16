package indi.lujunjie.mybatis.xmlgramma.visitor;

import indi.lujunjie.mybatis.xmlgramma.context.ContentContext;
import indi.lujunjie.mybatis.xmlgramma.context.impl.*;
import indi.lujunjie.mybatis.xmlgramma.monitor.Watcher;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 14:29
 */
public class SandboxVisitor extends BaseVisitor<ContentContext> {

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

    protected SandboxVisitor(Watcher watcher) {
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

        public SandboxVisitor build() {
            return new SandboxVisitor(watcher);
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
        Attribute attribute = sqlMapContext.element().attribute("xmlns:fo");
        if (attribute != null) {
            System.out.println("sqlMap xmlns:fo");
        }
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
        return super.visitTypeAlias(typeAliasContext);
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
        Attribute xmlResultNameAttribute = selectContext.element().attribute("xmlResultName");
        if (xmlResultNameAttribute != null) {
            System.out.println("select xmlResultName");
        }
        return super.visitSelect(selectContext);
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
        return super.visitUpdate(updateContext);
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
        return super.visitInsert(insertContext);
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
        return super.visitDelete(deleteContext);
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
        if (isEmptyContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isEmpty removeFirstPrepend");
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
        if (isNotEmptyContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNotEmpty removeFirstPrepend");
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
        if (isNullContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNull removeFirstPrepend");
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
        if (isNotNullContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNotNull removeFirstPrepend");
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
        if (isEqualContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isEqual removeFirstPrepend");
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
        if (isNotEqualContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNotEqual removeFirstPrepend");
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
        if (isPropertyAvailableContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isPropertyAvailable removeFirstPrepend");
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
        if (isNotPropertyAvailableContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNotPropertyAvailable removeFirstPrepend");
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
        if (isGreaterThanContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isGreaterThan removeFirstPrepend");
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
        if (isGreaterEqualContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isGreaterEqual removeFirstPrepend");
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
        if (isLessThanContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isLessThan removeFirstPrepend");
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
        if (isLessEqualContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isLessEqual removeFirstPrepend");
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
        if (iterateContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("iterate removeFirstPrepend");
        }
        return super.visitIterate(iterateContext);
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
        return super.visitText(textContext);
    }

    @Override
    public ContentContext visitCData(CDataContext cDataContext) {
        return super.visitCData(cDataContext);
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
        return super.visitDynamic(dynamicContext);
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
     * select CDATA #IMPLIED --- TODO
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
        if (resultContext.element().attribute("columnIndex") != null) {
            System.out.println("result columnIndex");
        }
        if (resultContext.element().attribute("nullValue") != null) {
            System.out.println("result nullValue");
        }
        if (resultContext.element().attribute("notNullColumn") != null) {
            System.out.println("result notNullColumn");
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
        if (parameterContext.element().attribute("typeName") != null) {
            System.out.println("parameter typeName");
        }
        if (parameterContext.element().attribute("nullValue") != null) {
            System.out.println("parameter nullValue");
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
        if (isParameterPresentContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isParameterPresent removeFirstPrepend");
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
        if (isNotParameterPresentContext.element().attribute("removeFirstPrepend") != null) {
            System.out.println("isNotParameterPresent removeFirstPrepend");
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
        if (sqlMapConfigContext.element().attribute("xmlns:fo") != null) {
            System.out.println("sqlMapConfig xmlns:fo");
        }
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
        if (procedureContext.element().attribute("xmlResultName") != null) {
            System.out.println("procedure xmlResultName");
        }
        if (procedureContext.element().attribute("remapResults") != null) {
            System.out.println("procedure remapResults");
        }
        return super.visitProcedure(procedureContext);
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
        if (discriminatorContext.element().attribute("columnIndex") != null) {
            System.out.println("discriminator columnIndex");
        }
        if (discriminatorContext.element().attribute("nullValue") != null) {
            System.out.println("discriminator nullValue");
        }
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
     * {@link SandboxVisitor#visitSelect(SelectContext)}
     *
     * @param statementContext
     * @return
     */
    @Override
    public ContentContext visitStatement(StatementContext statementContext) {
        Attribute xmlResultNameAttribute = statementContext.element().attribute("xmlResultName");
        if (xmlResultNameAttribute != null) {
            System.out.println("statement xmlResultName");
        }
        return super.visitStatement(statementContext);
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
        return super.visitCacheModel(cacheModelContext);
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
        return super.visitFlushOnExecute(flushOnExecuteContext);
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
        Element settings = settingsContext.element();
        if (settings.attribute("classInfoCacheEnabled") != null) {
            System.out.println("settings classInfoCacheEnabled");
        }
        if (settings.attribute("statementCachingEnabled") != null) {
            System.out.println("settings statementCachingEnabled");
        }
        if (settings.attribute("enhancementEnabled") != null) {
            System.out.println("settings enhancementEnabled");
        }
        if (settings.attribute("errorTracingEnabled") != null) {
            System.out.println("settings errorTracingEnabled");
        }
        if (settings.attribute("useStatementNamespaces") != null) {
            System.out.println("settings useStatementNamespaces");
        }
        if (settings.attribute("maxSessions") != null) {
            System.out.println("settings maxSessions");
        }
        if (settings.attribute("maxTransactions") != null) {
            System.out.println("settings maxTransactions");
        }
        if (settings.attribute("maxRequests") != null) {
            System.out.println("settings maxRequests");
        }
        return super.visitSettings(settingsContext);
    }

    /**
     * type CDATA #REQUIRED --- DONE
     *
     * @param resultObjectFactoryContext
     * @return
     */
    @Override
    public ContentContext visitResultObjectFactory(ResultObjectFactoryContext resultObjectFactoryContext) {
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
        return super.visitTransactionManager(transactionManagerContext);
    }

    //************************** end   ***************************************
}
