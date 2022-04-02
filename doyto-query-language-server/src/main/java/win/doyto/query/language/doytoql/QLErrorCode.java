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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import win.doyto.query.web.response.ErrorCode;

/**
 * QLErrorCode
 *
 * @author f0rb on 2022-04-02
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum QLErrorCode implements ErrorCode {
    SUCCESS,
    DATA_SHOULD_NOT_BE_NULL(1000),
    DATA_SHOULD_NOT_BE_EMPTY,
    ;

    private final Integer code;

    QLErrorCode() {
        this.code = Index.count++;
    }

    QLErrorCode(int index) {
        Index.count = index;
        this.code = Index.count++;
    }

    public String getMessage() {
        return super.name();
    }

    private static class Index {
        private static int count = 0;
    }
}
