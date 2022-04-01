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

}