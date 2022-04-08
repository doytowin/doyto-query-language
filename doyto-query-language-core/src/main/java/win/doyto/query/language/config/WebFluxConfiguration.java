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

package win.doyto.query.language.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import win.doyto.query.r2dbc.R2dbcOperations;
import win.doyto.query.r2dbc.R2dbcTemplate;
import win.doyto.query.reactive.webflux.config.WebFluxConfigurerAdapter;

/**
 * WebFluxConfiguration
 *
 * @author f0rb on 2022-03-31
 */
@Configuration
public class WebFluxConfiguration extends WebFluxConfigurerAdapter {

    @Bean
    public R2dbcOperations r2dbcOperations(ConnectionFactory connectionFactory) {
        return new R2dbcTemplate(connectionFactory);
    }

}
