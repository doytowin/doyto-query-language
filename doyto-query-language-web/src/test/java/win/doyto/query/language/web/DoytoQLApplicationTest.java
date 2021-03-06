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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.util.BeanUtil;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DoytoQLApplicationTest
 *
 * @author f0rb on 2022-04-16
 */
@SpringBootTest(classes = DoytoQLApplication.class)
@AutoConfigureMockMvc
public abstract class DoytoQLApplicationTest {

    protected static final String DOMAIN_USER = "t_user";

    @Resource
    protected MockMvc mockMvc;

    protected ResultActions postAndSuccess(DoytoQLRequest request) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/DoytoQL/")
                .content(BeanUtil.stringify(request)).contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(requestBuilder)
                      //.andDo(print())
                      .andExpect(status().isOk())
                      .andExpect(jsonPath("$.success").value(true));
    }

}
