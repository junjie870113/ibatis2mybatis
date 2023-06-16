package indi.lujunjie.mybatis.xmlgramma.visitor;

import indi.lujunjie.mybatis.xmlgramma.context.Removable;
import indi.lujunjie.mybatis.xmlgramma.context.impl.*;

/**
 * @author Lu Jun Jie
 * @date 2021-09-23 10:08
 */
public class BaseVisitor<T extends Removable> extends AbstractDFSVisitor<T> {

    @Override
    public T defaultResult() {
        return null;
    }

    @Override
    public T aggregateResult(T aggregate, T nextResult) {
        return nextResult;
    }

    public T visitSqlMap(SqlMapContext sqlMapContext) {
        return this.visitChildren(sqlMapContext);
    }

    public T visitTypeAlias(TypeAliasContext typeAliasContext) {
        return this.visitChildren(typeAliasContext);
    }

    public T visitSelect(SelectContext selectContext) {
        return this.visitChildren(selectContext);
    }

    public T visitUpdate(UpdateContext updateContext) {
        return this.visitChildren(updateContext);
    }

    public T visitInsert(InsertContext insertContext) {
        return this.visitChildren(insertContext);
    }

    public T visitDelete(DeleteContext deleteContext) {
        return this.visitChildren(deleteContext);
    }

    public T visitIsNotEmpty(IsNotEmptyContext isNotEmptyContext) {
        return this.visitChildren(isNotEmptyContext);
    }

    public T visitIsEmpty(IsEmptyContext isEmptyContext) {
        return this.visitChildren(isEmptyContext);
    }

    public T visitIsNotNull(IsNotNullContext isNotNullContext) {
        return this.visitChildren(isNotNullContext);
    }

    public T visitIsNull(IsNullContext isNullContext) {
        return this.visitChildren(isNullContext);
    }

    public T visitIsEqual(IsEqualContext isEqualContext) {
        return this.visitChildren(isEqualContext);
    }

    public T visitIsNotEqual(IsNotEqualContext isNotEqualContext) {
        return this.visitChildren(isNotEqualContext);
    }

    public T visitIterate(IterateContext iterateContext) {
        return this.visitChildren(iterateContext);
    }

    public T visitSelectKey(SelectKeyContext selectKeyContext) {
        return this.visitChildren(selectKeyContext);
    }

    public T visitDynamic(DynamicContext dynamicContext) {
        return this.visitChildren(dynamicContext);
    }

    public T visitInclude(IncludeContext includeContext) {
        return this.visitChildren(includeContext);
    }

    public T visitIsGreaterEqual(IsGreaterEqualContext isGreaterEqualContext) {
        return this.visitChildren(isGreaterEqualContext);
    }

    public T visitIsGreaterThan(IsGreaterThanContext isGreaterThanContext) {
        return this.visitChildren(isGreaterThanContext);
    }

    public T visitIsLessEqual(IsLessEqualContext isLessEqualContext) {
        return this.visitChildren(isLessEqualContext);
    }

    public T visitIsLessThan(IsLessThanContext isLessThanContext) {
        return this.visitChildren(isLessThanContext);
    }

    public T visitIsNotParameterPresent(IsNotParameterPresentContext isNotParameterPresentContext) {
        return this.visitChildren(isNotParameterPresentContext);
    }

    public T visitIsNotPropertyAvailable(IsNotPropertyAvailableContext isNotPropertyAvailableContext) {
        return this.visitChildren(isNotPropertyAvailableContext);
    }

    public T visitIsParameterPresent(IsParameterPresentContext isParameterPresentContext) {
        return this.visitChildren(isParameterPresentContext);
    }

    public T visitIsPropertyAvailable(IsPropertyAvailableContext isPropertyAvailableContext) {
        return this.visitChildren(isPropertyAvailableContext);
    }

    public T visitProcedure(ProcedureContext procedureContext) {
        return this.visitChildren(procedureContext);
    }

    public T visitParameterMap(ParameterMapContext parameterMapContext) {
        return this.visitChildren(parameterMapContext);
    }

    public T visitParameter(ParameterContext parameterContext) {
        return this.visitChildren(parameterContext);
    }

    public T visitSql(SqlContext sqlContext) {
        return this.visitChildren(sqlContext);
    }

    public T visitResultMap(ResultMapContext resultMapContext) {
        return this.visitChildren(resultMapContext);
    }

    public T visitCacheModel(CacheModelContext cacheModelContext) {
        return this.visitChildren(cacheModelContext);
    }

    public T visitStatement(StatementContext statementContext) {
        return this.visitChildren(statementContext);
    }

    public T visitProperty(PropertyContext propertyContext) {
        return this.visitChildren(propertyContext);
    }

    public T visitFlushInterval(FlushIntervalContext flushIntervalContext) {
        return this.visitChildren(flushIntervalContext);
    }

    public T visitFlushOnExecute(FlushOnExecuteContext flushOnExecuteContext) {
        return this.visitChildren(flushOnExecuteContext);
    }

    public T visitResult(ResultContext resultContext) {
        return this.visitChildren(resultContext);
    }

    public T visitDiscriminator(DiscriminatorContext discriminatorContext) {
        return this.visitChildren(discriminatorContext);
    }

    public T visitSubMap(SubMapContext subMapContext) {
        return this.visitChildren(subMapContext);
    }

    public T visitProperties(PropertiesContext propertiesContext) {
        return this.visitChildren(propertiesContext);
    }

    public T visitResultObjectFactory(ResultObjectFactoryContext resultObjectFactoryContext) {
        return this.visitChildren(resultObjectFactoryContext);
    }

    public T visitSetting(SettingContext settingContext) {
        return this.visitChildren(settingContext);
    }

    public T visitSettings(SettingsContext settingsContext) {
        return this.visitChildren(settingsContext);
    }

    public T visitTransactionManager(TransactionManagerContext transactionManagerContext) {
        return this.visitChildren(transactionManagerContext);
    }

    public T visitDatasource(DatasourceContext datasourceContext) {
        return this.visitChildren(datasourceContext);
    }

    public T visitTypeHandler(TypeHandlerContext typeHandlerContext) {
        return this.visitChildren(typeHandlerContext);
    }

    public T visitSqlMapConfig(SqlMapConfigContext sqlMapConfigContext) {
        return this.visitChildren(sqlMapConfigContext);
    }

    public T visitText(TextContext textContext) {
        return this.visitChildren(textContext);
    }

    public T visitComment(CommentContext commentContext) {
        return this.visitChildren(commentContext);
    }

    public T visitCData(CDataContext cDataContext) {
        return this.visitChildren(cDataContext);
    }

    public T visitMappers(MappersContext mappersContext) {
        return this.visitChildren(mappersContext);
    }
}
