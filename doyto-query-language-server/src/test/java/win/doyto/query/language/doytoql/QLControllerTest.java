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
import win.doyto.query.language.DoytoQLApplication;
import win.doyto.query.r2dbc.R2dbcOperations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

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

    private Consumer<EntityExchangeResult<byte[]>> log() {
        return entityExchangeResult -> System.out.println(entityExchangeResult.toString());
    }

    @Test
    void requestShouldReturnOK() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setFrom("t_user");

        webTestClient.post().uri("/DoytoQL/")
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(doytoQLRequest))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .consumeWith(log())
                     .jsonPath("$.success").isEqualTo(true)
                     .jsonPath("$.data.total").isEqualTo(5)
                     .jsonPath("$.data.list.size()").isEqualTo(5)
                     .jsonPath("$.data.list[0].username").isEqualTo("f0rb")
                     .jsonPath("$.data.list[1].username").isEqualTo("user2")
        ;
    }

}