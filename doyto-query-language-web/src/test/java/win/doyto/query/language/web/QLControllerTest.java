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

package win.doyto.query.language.web;

import org.junit.jupiter.api.Test;
import win.doyto.query.language.doytoql.DoytoQLRequest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * QLControllerTest
 *
 * @author f0rb on 2022-03-31
 */
@SuppressWarnings("java:S2699")
class QLControllerTest extends DoytoQLApplicationTest {

    @Test
    void requestShouldReturnOK() throws Exception {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);

        postAndSuccess(doytoQLRequest)
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.list.size()").value(5))
                .andExpect(jsonPath("$.data.list[0].username").value("f0rb"))
                .andExpect(jsonPath("$.data.list[1].username").value("user2"))
        ;
    }

}