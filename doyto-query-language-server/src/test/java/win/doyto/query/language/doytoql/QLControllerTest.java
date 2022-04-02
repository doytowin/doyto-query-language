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

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;
import win.doyto.query.core.PageQuery;
import win.doyto.query.language.DoytoQLApplication;
import win.doyto.query.r2dbc.R2dbcOperations;
import win.doyto.query.web.response.ErrorCodeException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInRelativeOrder;

/**
 * QLControllerTest
 *
 * @author f0rb on 2022-03-31
 */
@SpringBootTest(classes = DoytoQLApplication.class)
@AutoConfigureWebTestClient
class QLControllerTest {

    @Resource
    protected WebTestClient webTestClient;

    @BeforeEach
    void setUp(@Autowired R2dbcOperations r2dbcOperations) throws IOException {
        var schema = StreamUtils.copyToString(
                this.getClass().getResourceAsStream("/schema.sql"),
                Charset.defaultCharset()
        );
        r2dbcOperations.update(schema).block();
    }

    private WebTestClient.BodyContentSpec postAndSuccess(DoytoQLRequest body) {
        return webTestClient.post().uri("/DoytoQL/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(body))
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .consumeWith(log())
                            .jsonPath("$.success").isEqualTo(true);
    }

    private Consumer<EntityExchangeResult<byte[]>> log() {
        return entityExchangeResult -> System.out.println(entityExchangeResult.toString());
    }

    @Test
    void requestShouldReturnOK() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain("t_user");

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
        doytoQLRequest.setDomain("t_user");

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
        doytoQLRequest.setDomain("t_user");

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
        doytoQLRequest.setDomain("t_user");

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
        doytoQLRequest.setDomain("t_user");

        LinkedHashMap<String, Object> filters = new LinkedHashMap<>();
        filters.put("idLt", 3);
        doytoQLRequest.setFilters(filters);

        postAndSuccess(doytoQLRequest).jsonPath("$.data").isEqualTo(2);
    }

    @Test
    void shouldSupportInsert() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain("t_user");
        doytoQLRequest.setData(List.of(TestUtil.buildEntity("6")));

        postAndSuccess(doytoQLRequest).jsonPath("$.data").isEqualTo(1);
    }

    @Test
    void shouldSupportInsertMulti() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain("t_user");
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
        QLController qlController = new QLController(null);
        DoytoQLRequest request = new DoytoQLRequest();

        assertThatThrownBy(() -> qlController.execute(request))
                .isInstanceOf(ErrorCodeException.class)
                .hasMessage("OPERATION_SHOULD_NOT_BE_NULL");
    }

}