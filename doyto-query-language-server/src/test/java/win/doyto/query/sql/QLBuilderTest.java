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

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.TestUtil;
import win.doyto.query.util.BeanUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QLBuilderTest
 *
 * @author f0rb on 2022-04-01
 */
class QLBuilderTest {

    @Test
    void supportMultiFilters() {
        String conditions = "{\"id\": 1, \"valid\": true}";
        LinkedHashMap<String, Object> filters = BeanUtil.parse(conditions, new TypeReference<>() {});
        List<Object> args = new ArrayList<>();
        String sql = QLBuilder.buildWhere(filters, args);

        assertThat(sql).isEqualTo(" WHERE id = ? AND valid = ?");
        assertThat(args).containsExactly(1, true);
    }

    @Test
    void supportGt() {
        String conditions = "{\"idGt\": 1}";
        LinkedHashMap<String, Object> filters = BeanUtil.parse(conditions, new TypeReference<>() {});
        List<Object> args = new ArrayList<>();
        String sql = QLBuilder.buildWhere(filters, args);

        assertThat(sql).isEqualTo(" WHERE id > ?");
        assertThat(args).containsExactly(1);
    }

    @Test
    void checkSupportedSuffix() {
        String conditions = "{\"idEq\": 1, \"idNot\": 2, \"idGt\": 3, \"idGe\": 4, \"idLt\": 5, \"idLe\": 6, \"idIn\": [1,2,3], \"idNotIn\": [5,6]," +
                "\"usernameContain\": \"test\", \"usernameStart\": \"test\", \"usernameNotLike\": \"test\", \"memoNull\": true, \"memoNotNull\": true}";
        LinkedHashMap<String, Object> filters = BeanUtil.parse(conditions, new TypeReference<>() {});
        List<Object> args = new ArrayList<>();
        String sql = QLBuilder.buildWhere(filters, args);

        assertThat(sql).isEqualTo(" WHERE id = ? AND id != ? AND id > ? AND id >= ? AND id < ? AND id <= ?" +
                                          " AND id IN (?, ?, ?) AND id NOT IN (?, ?)" +
                                          " AND username LIKE ? AND username LIKE ? AND username NOT LIKE ?" +
                                          " AND memo IS NULL AND memo IS NOT NULL");
        assertThat(args).containsExactly(1, 2, 3, 4, 5, 6, 1, 2, 3, 5, 6, "%test%", "test%", "%test%");
    }

    @Test
    void supportInsertMulti() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain("t_user");
        doytoQLRequest.setData(List.of(TestUtil.buildEntity("6"), TestUtil.buildEntity("7")));

        SqlAndArgs sqlAndArgs = QLBuilder.buildInsertSql(doytoQLRequest);

        assertThat(sqlAndArgs.getSql())
                .isEqualTo("INSERT INTO t_user (username, mobile, email, nickname, password, user_level, valid) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?)");
    }

    @Test
    void supportQuerySpecifiedColumns() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("select");
        doytoQLRequest.setDomain("t_user");
        doytoQLRequest.setColumns(List.of("username", "email", "mobile"));

        SqlAndArgs sqlAndArgs = QLBuilder.buildQuerySql(doytoQLRequest);

        assertThat(sqlAndArgs.getSql())
                .isEqualTo("SELECT username, email, mobile FROM t_user");
    }

    @Test
    void supportUpdate() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("select");
        doytoQLRequest.setDomain("t_user");

        LinkedHashMap<String, Object> entity = new LinkedHashMap<>();
        entity.put("nickname", "kitty");
        entity.put("valid", true);
        doytoQLRequest.setData(List.of(entity));

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("id", 1);
        doytoQLRequest.setFilters(filters);

        SqlAndArgs sqlAndArgs = QLBuilder.buildUpdateSql(doytoQLRequest);

        assertThat(sqlAndArgs.getSql())
                .isEqualTo("UPDATE t_user SET nickname = ?, valid = ? WHERE id = ?");
        assertThat(sqlAndArgs.getArgs())
                .containsExactly("kitty", true, 1);
    }
}