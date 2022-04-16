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

package win.doyto.query.language.component;

import org.junit.jupiter.api.Test;
import win.doyto.query.DoytoQLApplicationTest;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.TestUtil;

import java.util.List;

/**
 * QLExceptionHandlerTest
 *
 * @author f0rb on 2022-04-16
 */
class QLExceptionHandlerTest extends DoytoQLApplicationTest {

    @Test
    void shouldHandleR2dbcDataIntegrityViolationException() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("insert");
        doytoQLRequest.setDomain(DOMAIN_USER);
        doytoQLRequest.setData(List.of(TestUtil.buildEntity("5")));

        postAndFail(doytoQLRequest)
                .jsonPath("$.message").isEqualTo("Data integrity violation: [23505]Unique index or primary key violation.");
    }

}