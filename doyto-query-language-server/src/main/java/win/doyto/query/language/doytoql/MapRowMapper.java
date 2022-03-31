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

import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Row;
import win.doyto.query.r2dbc.RowMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MapRowMapper
 *
 * @author f0rb on 2022-03-31
 */
public class MapRowMapper implements RowMapper<Map<String, Object>> {

    @Override
    public Map<String, Object> map(Row row, int rn) {
        Map<String, Object> ret = new LinkedHashMap<>();

        List<? extends ColumnMetadata> mds = row.getMetadata().getColumnMetadatas();
        for (ColumnMetadata md : mds) {
            String colName = md.getName();
            ret.put(colName.toLowerCase(), row.get(colName));
        }
        return ret;
    }
}
