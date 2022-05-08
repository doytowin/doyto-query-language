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

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import win.doyto.query.language.doytoql.DoytoQLRequest;
import win.doyto.query.language.doytoql.QLErrorCode;
import win.doyto.query.r2dbc.R2dbcOperations;
import win.doyto.query.service.PageList;
import win.doyto.query.web.response.ErrorCodeException;
import win.doyto.query.web.response.JsonBody;

import javax.validation.Valid;

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
    private static final MapRowMapper ROW_MAPPER = new MapRowMapper();
    private R2dbcOperations r2dbcOperations;

    @SuppressWarnings("java:S1452")
    @PostMapping("DoytoQL")
    @Transactional
    public Mono<?> execute(@RequestBody @Valid DoytoQLRequest request) {
        return switch (request.getOperation()) {
            case "delete" -> r2dbcOperations.update(buildDeleteSql(request));
            case "insert" -> r2dbcOperations.update(buildInsertSql(request));
            case "update" -> r2dbcOperations.update(buildUpdateSql(request));
            case "query" -> r2dbcOperations
                    .query(buildQuerySql(request), ROW_MAPPER)
                    .collectList()
                    .zipWith(r2dbcOperations.count(buildCountSql(request)))
                    .map(t -> new PageList<>(t.getT1(), t.getT2()));
            default -> throw new ErrorCodeException(QLErrorCode.OPERATION_NOT_SUPPORTED);
        };
    }
}
