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

package win.doyto.query.language.test;

import win.doyto.query.language.doytoql.DoytoQLRequest;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * TestUtil
 *
 * @author f0rb on 2022-04-02
 */
@SuppressWarnings("java:S1118")
public class TestUtil {

    @SuppressWarnings("java:S1319")
    public static LinkedHashMap<String, Object> buildEntity(String index) {
        LinkedHashMap<String, Object> entity = new LinkedHashMap<>();
        entity.put("username", "user" + index);
        entity.put("mobile", "1777888888" + index);
        entity.put("email", "test" + index + "@qq.com");
        entity.put("nickname", "测试" + index);
        entity.put("password", "123456");
        entity.put("user_level", "普通");
        entity.put("valid", false);
        return entity;
    }

    public static DoytoQLRequest buildUpdateRequest() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("update");
        doytoQLRequest.setDomain("t_user");

        LinkedHashMap<String, Object> entity = new LinkedHashMap<>();
        entity.put("nickname", "kitty");
        entity.put("valid", true);
        doytoQLRequest.setData(List.of(entity));

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("id", 1);
        doytoQLRequest.setFilters(filters);
        return doytoQLRequest;
    }
}
