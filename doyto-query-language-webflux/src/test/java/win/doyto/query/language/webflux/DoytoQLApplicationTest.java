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

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.r2dbc.R2dbcOperations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * DoytoQLApplicationTest
 *
 * @author f0rb on 2022-04-16
 */
@SpringBootTest(classes = DoytoQLApplication.class)
@AutoConfigureWebTestClient
public abstract class DoytoQLApplicationTest {

    protected static final String DOMAIN_USER = "t_user";

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void setUp(@Autowired R2dbcOperations r2dbcOperations) throws IOException {
        var schema = StreamUtils.copyToString(
                this.getClass().getResourceAsStream("/schema.sql"),
                Charset.defaultCharset()
        );
        r2dbcOperations.update(schema).block();
    }

    protected WebTestClient.BodyContentSpec postAndSuccess(DoytoQLRequest body) {
        return post(body).jsonPath("$.success").isEqualTo(true);
    }

    protected WebTestClient.BodyContentSpec postAndFail(DoytoQLRequest body) {
        return post(body).jsonPath("$.success").isEqualTo(false);
    }

    private WebTestClient.BodyContentSpec post(DoytoQLRequest body) {
        return webTestClient.post().uri("/DoytoQL/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(body))
                            .exchange()
                            .expectStatus().isOk()
                            .expectBody()
                            .consumeWith(log());
    }

    private Consumer<EntityExchangeResult<byte[]>> log() {
        return entityExchangeResult -> System.out.println(entityExchangeResult.toString());
    }
}
