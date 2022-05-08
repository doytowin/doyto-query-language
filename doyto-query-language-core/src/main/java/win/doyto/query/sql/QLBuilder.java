/*
 *    Copyright 2022 Forb Yuan
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DomainRoute;
import win.doyto.query.core.PageQuery;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.QLDomainRoute;
import win.doyto.query.language.doytoql.QLErrorCode;
import win.doyto.query.util.CommonUtil;
import win.doyto.query.web.response.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static win.doyto.query.sql.BuildHelper.buildOrderBy;
import static win.doyto.query.sql.BuildHelper.buildPaging;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.sql.QueryBuilder.EQUALS_PLACE_HOLDER;
import static win.doyto.query.sql.SqlQuerySuffix.buildConditionForField;

/**
 * QLBuilder
 *
 * @author f0rb on 2022-04-01
 */
@UtilityClass
public class QLBuilder {

    private static final Collector<CharSequence, ?, String> COLLECTOR_WHERE = Collectors.joining(" AND ", WHERE, EMPTY);
    private static final Collector<CharSequence, ?, String> COLLECTOR_OR = Collectors.joining(SPACE_OR, "(", ")");

    private static final String QUERY_FIELD_FORMAT = "%sQuery";
    private final String joinIdFormat = GlobalConfiguration.instance().getJoinIdFormat();
    private final String tableFormat = GlobalConfiguration.instance().getTableFormat();
    private final String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();

    public static SqlAndArgs buildQuerySql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> {
            List<String> columns = request.getColumns();
            String columnStr = columns != null ? String.join(SEPARATOR, columns) : "*";
            String sql = SELECT + columnStr + FROM + request.toTableName();

            sql += buildWhere(request, args);
            PageQuery pageQuery = request.getPage();
            if (pageQuery != null) {
                sql = sql + buildOrderBy(pageQuery);
                sql = buildPaging(sql, pageQuery);
            }
            return sql;
        });
    }

    public static SqlAndArgs buildCountSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> SELECT + COUNT +
                FROM + request.toTableName() + buildWhere(request, args));
    }

    static String buildWhere(DoytoQLRequest request, List<Object> args) {
        StringJoiner joiner = new StringJoiner(AND);
        QLDomainRoute domainRoute = request.getDomainRoute();
        if (domainRoute != null) {
            String nestedQuery = buildNestedQuery(domainRoute, args);
            joiner.add(nestedQuery);
        }

        LinkedHashMap<String, Object> filters = request.getFilters();
        if (filters != null && !filters.isEmpty()) {
            String and = buildWhereStream(filters, args).collect(Collectors.joining(AND));
            joiner.add(and);
        }
        return joiner.length() == 0 ? "": WHERE + joiner;
    }

    static String buildWhere(LinkedHashMap<String, Object> filters, List<Object> args) {
        return buildWhereStream(filters, args).collect(COLLECTOR_WHERE);
    }

    @SuppressWarnings("unchecked")
    private static Stream<String> buildWhereStream(LinkedHashMap<String, Object> filters, List<Object> args) {
        return filters.entrySet().stream()
                      .map(e -> {
                          if (e.getKey().endsWith("Or")) {
                              Object value = e.getValue();
                              ErrorCode.assertTrue(value instanceof LinkedHashMap, QLErrorCode.TYPE_OF_OR_FILTER_SHOULD_BE_OBJECT);
                              LinkedHashMap<String, Object> orConditions = (LinkedHashMap<String, Object>) value;
                              ErrorCode.assertFalse(orConditions.isEmpty(), QLErrorCode.OR_FILTER_SHOULD_CONTAIN_AT_LEAST_ONE_CONDITION);
                              return buildConditionForOr(args, orConditions);
                          }
                          return buildConditionForField(e.getKey(), args, e.getValue());
                      });
    }

    private static String buildConditionForOr(List<Object> args, LinkedHashMap<String, Object> orConditions) {
        return orConditions.entrySet().stream()
                           .map(orCondition -> buildConditionForField(orCondition.getKey(), args, orCondition.getValue()))
                           .collect(COLLECTOR_OR);
    }

    public static SqlAndArgs buildDeleteSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> DELETE_FROM + request.toTableName() + buildWhere(request, args));
    }

    public static SqlAndArgs buildInsertSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            LinkedHashMap<String, Object> first = request.getData().get(0);
            String columns = first.keySet().stream().collect(CommonUtil.CLT_COMMA_WITH_PAREN);
            String wildInsertValue = first.values().stream().map(i -> PLACE_HOLDER).collect(CommonUtil.CLT_COMMA_WITH_PAREN);

            StringJoiner placeholders = new StringJoiner(SEPARATOR);
            for (LinkedHashMap<String, Object> datum : request.getData()) {
                argList.addAll(datum.values());
                placeholders.add(wildInsertValue);
            }
            return CrudBuilder.buildInsertSql(request.toTableName(), columns, placeholders.toString());
        });
    }

    public static SqlAndArgs buildUpdateSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            List<LinkedHashMap<String, Object>> data = request.getData();

            ErrorCode.assertNotNull(data, QLErrorCode.DATA_SHOULD_NOT_BE_NULL);
            ErrorCode.assertFalse(data.isEmpty(), QLErrorCode.DATA_SHOULD_NOT_BE_EMPTY);

            LinkedHashMap<String, Object> target = data.get(0);
            ErrorCode.assertFalse(target.isEmpty(), QLErrorCode.DATA_SHOULD_CONTAIN_AT_LEAST_ONE_FIELD);

            String tableName = request.toTableName();
            String setClause = readValueToArgList(target, argList);
            String whereClause = buildWhere(request, argList);
            return CrudBuilder.buildUpdateSql(tableName, setClause) + whereClause;
        });
    }

    private static String readValueToArgList(LinkedHashMap<String, Object> target, List<Object> argList) {
        StringJoiner setClauses = new StringJoiner(SEPARATOR);
        for (Map.Entry<String, Object> entry : target.entrySet()) {
            String column = GlobalConfiguration.dialect().wrapLabel(entry.getKey());
            setClauses.add(column + EQUALS_PLACE_HOLDER);
            argList.add(entry.getValue());
        }
        return setClauses.toString();
    }

    public static String buildNestedQuery(QLDomainRoute domainRoute, List<Object> argList) {
        List<String> domains = domainRoute.getPath();
        String[] domainIds = prepareDomainIds(domains);
        String[] joinTables = prepareJoinTables(domains);
        String lastDomain;
        if (domainRoute.isReverse()) {
            lastDomain = domains.get(0);
        } else {
            ArrayUtils.reverse(domainIds);
            ArrayUtils.reverse(joinTables);
            lastDomain = domains.get(domains.size() - 1);
        }
        return buildClause(lastDomain, domains, domainIds, joinTables, argList, domainRoute);
    }

    private String[] prepareDomainIds(List<String> domains) {
        return domains.stream().map(domain -> String.format(joinIdFormat, domain)).toArray(String[]::new);
    }

    private String[] prepareJoinTables(List<String> domains) {
        return IntStream.range(0, domains.size() - 1)
                        .mapToObj(i -> String.format(joinTableFormat, domains.get(i), domains.get(i + 1)))
                        .toArray(String[]::new);
    }

    private String buildClause(
            String lastDomain, List<String> domains, String[] domainIds, String[] joinTables,
            List<Object> argList, QLDomainRoute domainRoute) {
        int current = domainIds.length - 1;
        StringBuilder subQueryBuilder = new StringBuilder();
        subQueryBuilder.append(ID).append(IN).append("(");
        while (true) {
            buildStartForCurrentDomain(subQueryBuilder, domainIds[current], joinTables[current - 1]);
            if (--current <= 0) {
                break;
            }
            buildWhereForCurrentDomain(subQueryBuilder, domainIds[current]);
            buildQueryForCurrentDomain(subQueryBuilder, domains.get(current), domainRoute.getFilters(), argList);
        }
        buildQueryForLastDomain(subQueryBuilder, lastDomain, domainIds, argList, domainRoute);
        appendTailParenthesis(subQueryBuilder, joinTables.length);
        return subQueryBuilder.toString();
    }

    private void buildWhereForCurrentDomain(StringBuilder subQueryBuilder, String domainIds) {
        subQueryBuilder.append(WHERE).append(domainIds).append(IN).append("(");
    }

    private void buildStartForCurrentDomain(StringBuilder subQueryBuilder, String domainId, String joinTable) {
        subQueryBuilder.append(SELECT).append(domainId).append(FROM).append(joinTable);
    }

    @SuppressWarnings("unchecked")
    private void buildQueryForCurrentDomain(StringBuilder subQueryBuilder, String currentDomain, Map<String, Object> filters, List<Object> argList) {
        String queryName = String.format(QUERY_FIELD_FORMAT, currentDomain);
        if (filters.containsKey(queryName)) {
            LinkedHashMap<String, Object> queryForDomain = (LinkedHashMap<String, Object>) filters.get(queryName);
            String where = buildWhere(queryForDomain, argList);
            String table = String.format(tableFormat, currentDomain);
            subQueryBuilder.append(SELECT).append(ID).append(FROM).append(table).append(where);
            subQueryBuilder.append(" INTERSECT ");
        }
    }

    @SuppressWarnings("unchecked")
    private void buildQueryForLastDomain(
            StringBuilder subQueryBuilder, String lastDomain, String[] domainIds,
            List<Object> argList, QLDomainRoute qlDomainRoute
    ) {
        for (Map.Entry<String, Object> entry : qlDomainRoute.getFilters().entrySet()) {
            if (entry.getKey().startsWith(lastDomain)) {
                Object value = entry.getValue();
                if (value instanceof LinkedHashMap<?, ?>) {
                    buildSubQueryForLastDomain(
                            subQueryBuilder, lastDomain, domainIds, argList,
                            qlDomainRoute, (LinkedHashMap<String, Object>) value);
                } else {
                    buildWhereForLastDomain(subQueryBuilder, argList, entry.getKey(), value);
                }
                break;
            }
        }
    }

    private void buildSubQueryForLastDomain(
            StringBuilder subQueryBuilder, String lastDomain, String[] domainIds,
            List<Object> argList, DomainRoute domainRoute, LinkedHashMap<String, Object> value
    ) {
        String table = String.format(tableFormat, lastDomain);
        String where = buildWhere(value, argList);
        if (domainIds.length > 1) {
            subQueryBuilder.append(WHERE).append(domainIds[0]);
        }
        subQueryBuilder.append(IN).append("(")
                       .append(SELECT).append(domainRoute.getLastDomainIdColumn())
                       .append(FROM).append(table).append(where)
                       .append(")");
    }

    private void buildWhereForLastDomain(StringBuilder subQueryBuilder, List<Object> args, String key, Object value) {
        String clause = buildConditionForField(key, args, value);
        subQueryBuilder.append(WHERE).append(clause);
    }

    private void appendTailParenthesis(StringBuilder subQueryBuilder, int count) {
        subQueryBuilder.append(StringUtils.repeat(')', count));
    }

}
