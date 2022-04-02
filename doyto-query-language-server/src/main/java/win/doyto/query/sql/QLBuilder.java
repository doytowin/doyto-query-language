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
import win.doyto.query.core.PageQuery;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.util.CommonUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static win.doyto.query.sql.BuildHelper.buildOrderBy;
import static win.doyto.query.sql.BuildHelper.buildPaging;
import static win.doyto.query.sql.Constant.*;

/**
 * QLBuilder
 *
 * @author f0rb on 2022-04-01
 */
@UtilityClass
public class QLBuilder {

    public static SqlAndArgs buildQuerySql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> {
            String sql = "select * from " + request.getDomain();

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
                      .map(e -> SqlQuerySuffix.buildConditionForField(e.getKey(), args, e.getValue()))
                      .collect(Collectors.joining(" AND ", WHERE, EMPTY));
    }

    public static SqlAndArgs buildDeleteSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> DELETE_FROM + request.getDomain() + buildWhere(request, args));
    }

    public static SqlAndArgs buildInsertSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            LinkedHashMap<String, Object> data = request.getData().get(0);
            String columns = data.keySet().stream().collect(CommonUtil.CLT_COMMA_WITH_PAREN);
            String wildInsertValue = data.values().stream().map(i -> PLACE_HOLDER).collect(CommonUtil.CLT_COMMA_WITH_PAREN);

            argList.addAll(data.values());
            return CrudBuilder.buildInsertSql(request.getDomain(), columns, wildInsertValue);
        });
    }

}
