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

import static win.doyto.query.sql.BuildHelper.buildOrderBy;
import static win.doyto.query.sql.Constant.WHERE;

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

            LinkedHashMap<String, Object> filters = request.getFilters();

            if (filters != null && !filters.isEmpty()) {
                String key = filters.keySet().stream().findFirst().get();
                sql += WHERE + key + " = ?";
                args.add(filters.get(key));
            }
            PageQuery pageQuery = request.getPage();
            if (pageQuery != null) {
                sql = sql + buildOrderBy(pageQuery);
                int offset = GlobalConfiguration.calcOffset(pageQuery);
                sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), offset);
            }
            return sql;
        });
    }
}
