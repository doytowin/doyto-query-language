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

package win.doyto.query.language.webflux;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.PageQuery;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.QLErrorCode;
import win.doyto.query.language.test.TestUtil;
import win.doyto.query.web.response.PresetErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static win.doyto.query.language.test.TestUtil.DOMAIN_USER;

/**
 * QLControllerTest
 *
 * @author f0rb on 2022-03-31
 */
@SuppressWarnings("java:S2699")
class QLControllerTest extends DoytoQLApplicationTest {

    @Test
    void requestShouldReturnOK() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        postAndSuccess(doytoQLRequest)
                .jsonPath("$.data.total").isEqualTo(5)
                .jsonPath("$.data.list.size()").isEqualTo(5)
                .jsonPath("$.data.list[0].username").isEqualTo("f0rb")
                .jsonPath("$.data.list[1].username").isEqualTo("user2")
        ;
    }

    @Test
    void shouldSupportPageQuery() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        doytoQLRequest.setPage(PageQuery.builder().pageNumber(2).pageSize(2).build());

        postAndSuccess(doytoQLRequest)
                .jsonPath("$.data.total").isEqualTo(5)
                .jsonPath("$.data.list.size()").isEqualTo(2)
                .jsonPath("$.data.list[*].id").value(containsInRelativeOrder(3, 4))
        ;
    }

    @Test
    void shouldSupportOrderBy() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        doytoQLRequest.setPage(PageQuery.builder().sort("id,desc").build());

        postAndSuccess(doytoQLRequest)
                .jsonPath("$.data.list.size()").isEqualTo(5)
                .jsonPath("$.data.list[*].id").value(containsInRelativeOrder(5, 4, 3, 2, 1))
        ;
    }

    @Test
    void shouldSupportFilters() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("id", 1);
        doytoQLRequest.setFilters(filters);

        postAndSuccess(doytoQLRequest)
                .jsonPath("$.data.total").isEqualTo(1)
                .jsonPath("$.data.list.size()").isEqualTo(1)
                .jsonPath("$.data.list[*].id").value(containsInRelativeOrder(1))
        ;
    }

    @Test
    void shouldSupportDelete() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("delete");
        doytoQLRequest.setDomain(DOMAIN_USER);

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("idLt", 3);
        doytoQLRequest.setFilters(filters);

        postAndSuccess(doytoQLRequest).jsonPath("$.data").isEqualTo(2);
    }

    @Test
    void shouldSupportInsert() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain(DOMAIN_USER);
        doytoQLRequest.setData(List.of(TestUtil.buildEntity("6")));

        postAndSuccess(doytoQLRequest).jsonPath("$.data").isEqualTo(1);
    }

    @Test
    void shouldSupportInsertMulti() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain(DOMAIN_USER);
        doytoQLRequest.setData(List.of(TestUtil.buildEntity("6"), TestUtil.buildEntity("7")));

        postAndSuccess(doytoQLRequest).jsonPath("$.data").isEqualTo(2);
    }

    @Test
    void shouldSupportUpdate() {
        postAndSuccess(TestUtil.buildUpdateRequest())
                .jsonPath("$.data").isEqualTo(1);
    }

    @Test
    void shouldProvideOperation() {
        DoytoQLRequest request = new DoytoQLRequest();

        postAndFail(request)
                .jsonPath("$.code").isEqualTo(PresetErrorCode.ARGUMENT_VALIDATION_FAILED.getCode());
    }

    @Test
    void shouldProvideDomain() {
        DoytoQLRequest request = new DoytoQLRequest();
        request.setOperation("query");

        postAndFail(request)
                .jsonPath("$.code").isEqualTo(PresetErrorCode.ARGUMENT_VALIDATION_FAILED.getCode());
    }

    @Test
    void shouldProvideSupportedOperation() {
        DoytoQLRequest request = new DoytoQLRequest();
        request.setOperation("unknown");
        request.setDomain(DOMAIN_USER);

        postAndFail(request)
                .jsonPath("$.code").isEqualTo(QLErrorCode.OPERATION_NOT_SUPPORTED.getCode());
    }

    @Test
    void supportOrQuery() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("accountOr", Map.of("username", "f0rb", "email", "f0rb"));
        filters.put("valid", true);
        doytoQLRequest.setFilters(filters);

        postAndSuccess(doytoQLRequest)
                .jsonPath("$.data.total").isEqualTo(1)
                .jsonPath("$.data.list[0].id").isEqualTo(1);
    }
}