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

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebExchangeBindException;
import win.doyto.query.language.doytoql.QLErrorCode;
import win.doyto.query.web.component.ErrorCodeI18nService;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.ErrorResponse;
import win.doyto.query.web.response.PresetErrorCode;


/**
 * QLExceptionHandler
 *
 * @author f0rb on 2022-04-16
 */
@Slf4j
@AllArgsConstructor
@ControllerAdvice
@ResponseBody
class QLExceptionHandler {

    private ErrorCodeI18nService errorCodeI18nService;

    @ExceptionHandler(R2dbcDataIntegrityViolationException.class)
    public ErrorCode handleR2dbcDataIntegrityViolationException(R2dbcDataIntegrityViolationException e) {
        log.error("R2dbcDataIntegrityViolationException: " + e.getMessage(), e.getCause());
        String errorMessage = e.getMessage().substring(0, e.getMessage().indexOf(':'));
        return errorCodeI18nService.buildErrorCode(QLErrorCode.DATA_INTEGRITY_VIOLATION, e.getSqlState(), errorMessage);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ErrorCode handleWebExchangeBindException(WebExchangeBindException e) {
        log.error("WebExchangeBindException: {}", e.getMessage());
        return new ErrorResponse(this.errorCodeI18nService.buildErrorCode(PresetErrorCode.ARGUMENT_VALIDATION_FAILED), e.getBindingResult());
    }

}
