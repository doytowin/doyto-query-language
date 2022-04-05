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
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.QLErrorCode;
import win.doyto.query.util.CommonUtil;
import win.doyto.query.web.response.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public static SqlAndArgs buildQuerySql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> {
            List<String> columns = request.getColumns();
            String columnStr = columns != null ? String.join(SEPARATOR, columns) : "*";
            String sql = SELECT + columnStr + FROM + request.getDomain();

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
                FROM + request.getDomain() + buildWhere(request, args));
    }

    private static String buildWhere(DoytoQLRequest request, List<Object> args) {
        LinkedHashMap<String, Object> filters = request.getFilters();
        if (filters == null || filters.isEmpty()) {
            return EMPTY;
        }
        return buildWhere(filters, args);
    }

    static String buildWhere(LinkedHashMap<String, Object> filters, List<Object> args) {
        return filters.entrySet().stream()
                      .map(e -> {
                          if (e.getKey().endsWith("Or")) {
                              return buildConditionForOr(args, (LinkedHashMap<String, Object>) e.getValue());
                          }
                          return buildConditionForField(e.getKey(), args, e.getValue());
                      })
                      .collect(COLLECTOR_WHERE);
    }

    private static String buildConditionForOr(List<Object> args, LinkedHashMap<String, Object> orConditions) {
        return orConditions.entrySet().stream()
                           .map(orCondition -> buildConditionForField(orCondition.getKey(), args, orCondition.getValue()))
                           .collect(COLLECTOR_OR);
    }

    public static SqlAndArgs buildDeleteSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> DELETE_FROM + request.getDomain() + buildWhere(request, args));
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
            return CrudBuilder.buildInsertSql(request.getDomain(), columns, placeholders.toString());
        });
    }

    public static SqlAndArgs buildUpdateSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            List<LinkedHashMap<String, Object>> data = request.getData();

            ErrorCode.assertNotNull(data, QLErrorCode.DATA_SHOULD_NOT_BE_NULL);
            ErrorCode.assertFalse(data.isEmpty(), QLErrorCode.DATA_SHOULD_NOT_BE_EMPTY);

            LinkedHashMap<String, Object> target = data.get(0);
            ErrorCode.assertFalse(target.isEmpty(), QLErrorCode.DATA_SHOULD_CONTAIN_AT_LEAST_ONE_FIELD);

            String domain = request.getDomain();
            String setClause = readValueToArgList(target, argList);
            String whereClause = buildWhere(request, argList);
            return CrudBuilder.buildUpdateSql(domain, setClause) + whereClause;
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
}
