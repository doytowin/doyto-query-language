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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static win.doyto.query.sql.BuildHelper.buildOrderBy;
import static win.doyto.query.sql.Constant.*;

/**
 * QLBuilder
 *
 * @author f0rb on 2022-04-01
 */
@UtilityClass
public class QLBuilder {

    public static SqlAndArgs buildQuerySql(DoytoQLRequest request, String table) {
        return SqlAndArgs.buildSqlWithArgs(args -> {
            String sql = "select * from " + table;

            sql += buildWhere(request, args);
            PageQuery pageQuery = request.getPage();
            if (pageQuery != null) {
                sql = sql + buildOrderBy(pageQuery);
                int offset = GlobalConfiguration.calcOffset(pageQuery);
                sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), offset);
            }
            return sql;
        });
    }

    private static String buildWhere(DoytoQLRequest request, List<Object> args) {
        LinkedHashMap<String, Object> filters = request.getFilters();
        if (filters == null || filters.isEmpty()) {
            return EMPTY;
        }
        return buildWhere(filters, args);
    }

    static String buildWhere(LinkedHashMap<String, Object> filters, List<Object> args) {
        StringJoiner where = new StringJoiner(" AND ", WHERE, EMPTY);
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            String key = entry.getKey();
            String condition = SqlQuerySuffix.buildConditionForField(key, args, entry.getValue());
            where.add(condition);
        }
        return where.toString();
    }

    public static SqlAndArgs buildDeleteSql(DoytoQLRequest request) {
        return SqlAndArgs.buildSqlWithArgs(args -> DELETE_FROM + request.getDomain() + buildWhere(request, args));
    }
}
