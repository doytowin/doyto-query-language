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

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import win.doyto.query.r2dbc.R2dbcOperations;
import win.doyto.query.service.PageList;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorCodeException;
import win.doyto.query.web.response.JsonBody;

import static win.doyto.query.sql.QLBuilder.*;

/**
 * QLController
 *
 * @author f0rb on 2022-03-31
 */
@JsonBody
@RestController
@AllArgsConstructor
public class QLController {

    private R2dbcOperations r2dbcOperations;

    @SuppressWarnings("java:S1452")
    @PostMapping("DoytoQL")
    @Transactional
    public Mono<?> execute(@RequestBody DoytoQLRequest request) {
        String operation = request.getOperation();
        ErrorCode.assertNotNull(operation, QLErrorCode.OPERATION_SHOULD_NOT_BE_NULL);
        ErrorCode.assertNotNull(request.getDomain(), QLErrorCode.DOMAIN_SHOULD_NOT_BE_NULL);
        return switch (operation) {
            case "delete" -> r2dbcOperations.update(buildDeleteSql(request));
            case "insert" -> r2dbcOperations.update(buildInsertSql(request));
            case "update" -> r2dbcOperations.update(buildUpdateSql(request));
            case "query" -> r2dbcOperations
                    .query(buildQuerySql(request), new MapRowMapper())
                    .collectList()
                    .zipWith(r2dbcOperations.count(buildCountSql(request)))
                    .map(t -> new PageList<>(t.getT1(), t.getT2()));
            default -> throw new ErrorCodeException(QLErrorCode.OPERATION_NOT_SUPPORTED);
        };
    }
}
