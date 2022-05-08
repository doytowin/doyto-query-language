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

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.PageQuery;

import java.util.LinkedHashMap;
import java.util.List;
import javax.validation.constraints.NotEmpty;

/**
 * DoytoQLRequest
 *
 * @author f0rb on 2022-03-31
 */
@Getter
@Setter
public class DoytoQLRequest {
    private static final String TABLE_FORMAT = GlobalConfiguration.instance().getTableFormat();

    @NotEmpty
    private String operation;
    @NotEmpty
    private String domain;
    private PageQuery page;
    private LinkedHashMap<String, Object> filters;
    private List<LinkedHashMap<String, Object>> data;
    private List<String> columns;
    private QLDomainRoute domainRoute;

    public String toTableName() {
        return String.format(TABLE_FORMAT, domain);
    }
}
