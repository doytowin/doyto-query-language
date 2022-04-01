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

package win.doyto.query.language.doytoql;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;
import win.doyto.query.r2dbc.R2dbcOperations;
import win.doyto.query.service.PageList;
import win.doyto.query.sql.SqlAndArgs;
import win.doyto.query.web.response.JsonBody;

/**
 * QLController
 *
 * @author f0rb on 2022-03-31
 */
@JsonBody
@RestController
@AllArgsConstructor
public class QLController {

    private R2dbcOperations r2dbcOperations;

    @SuppressWarnings("java:S1452")
    @PostMapping("DoytoQL")
    public Mono<?> execute(@RequestBody DoytoQLRequest request) {

        String table = request.getFrom();
        String sql = "select * from " + table;

        PageQuery pageQuery = request.getPage();

        if (pageQuery != null) {
            int offset = GlobalConfiguration.calcOffset(pageQuery);
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), offset);
        }

        return r2dbcOperations
                .query(new SqlAndArgs(sql), new MapRowMapper())
                .collectList()
                .zipWith(r2dbcOperations.count(new SqlAndArgs("select count(*) from " + table)))
                .map(t -> new PageList<>(t.getT1(), t.getT2()));
    }
}
