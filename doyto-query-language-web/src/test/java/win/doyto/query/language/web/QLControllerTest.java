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
import org.springframework.test.annotation.Rollback;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.QLDomainRoute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * QLControllerTest
 *
 * @author f0rb on 2022-03-31
 */
@SuppressWarnings("java:S2699")
@Rollback
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

    /**
     * User and role have following relations:
     * (1, 1)
     * (1, 2)
     * (3, 3)
     * (4, 2)
     * <p>
     * Role and Perm have following relations:
     * (1, 1)
     * (1, 2)
     * (1, 3)
     * (1, 4)
     * (2, 1)
     * (2, 2)
     * (3, 1)
     * (4, 1)
     * (5, 1)
     * <p>
     * Then User-Role-Perm have relations described by following 3D coordinate points:
     * (User, Role, Perm)
     * ------------------
     * (   1,    1,   1 )
     * (   1,    1,   2 )
     * (   1,    1,   3 )
     * (   1,    1,   4 )
     * (   1,    2,   1 )
     * (   1,    2,   2 )
     * (   3,    3,   1 )
     * (   4,    2,   1 )
     * (   4,    2,   2 )
     * (   0,    4,   1 )
     * (   0,    5,   1 )
     * <p>
     * So Perm[1] is assigned to User[1,3,4] via Role[1,2,3]
     */
    @Test
    void queryUserByPerm1() throws Exception {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);
        doytoQLRequest.setColumns(List.of("id", "username"));

        QLDomainRoute qlDomainRoute = QLDomainRoute
                .builder().path(List.of("user", "role", "perm"))
                .build()
                .add("perm_id", 1);
        doytoQLRequest.setDomainRoute(qlDomainRoute);

        postAndSuccess(doytoQLRequest)
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list..id", containsInRelativeOrder(1, 3, 4)));
    }

    /**
     * `valid` of Role[3] is false, so Perm[1] is only
     * assigned to User[1,4] via Role[1,2]
     */
    @Test
    void queryUserByPerm1WithValidRole() throws Exception {
        DoytoQLRequest doytoQLRequest = queryUserByPerm1AndValidRole();

        postAndSuccess(doytoQLRequest)
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list..id", containsInRelativeOrder(1, 4)));
    }

    /**
     * Grand Role[4] to User[3], then Perm[1] is assigned to User[1,4] via Role[1,2,4]
     */
    @Test
    void queryUserByPerm1WithValidRoleAfterGrant() throws Exception {
        DoytoQLRequest grantRequest = new DoytoQLRequest();
        grantRequest.setOperation("insert");
        grantRequest.setDomain("j_user_and_role");
        LinkedHashMap<String, Object> e1 = new LinkedHashMap<>();
        e1.put("user_id", 3);
        e1.put("role_id", 4);
        grantRequest.setData(List.of(e1));
        postAndSuccess(grantRequest);

        DoytoQLRequest doytoQLRequest = queryUserByPerm1AndValidRole();

        postAndSuccess(doytoQLRequest)
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list..id", containsInRelativeOrder(1, 3, 4)));
    }

    private DoytoQLRequest queryUserByPerm1AndValidRole() {
        DoytoQLRequest doytoQLRequest = new DoytoQLRequest();
        doytoQLRequest.setOperation("query");
        doytoQLRequest.setDomain(DOMAIN_USER);
        doytoQLRequest.setColumns(List.of("id", "username"));

        QLDomainRoute qlDomainRoute = QLDomainRoute
                .builder().path(List.of("user", "role", "perm"))
                .build()
                .add("roleQuery", Map.of("valid", true))
                .add("perm_id", 1);
        doytoQLRequest.setDomainRoute(qlDomainRoute);
        return doytoQLRequest;
    }

}