/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.hsql.fidmapper;

import java.io.IOException;
import java.sql.Connection;

import org.geotools.data.jdbc.fidmapper.DefaultFIDMapperFactory;
import org.geotools.data.jdbc.fidmapper.FIDMapper;

/**
 * This factory is only needed so it can be used as a hook to call 
 * the HsqlFIDMapper.
 * 
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/fidmapper/HsqlFIDMapperFactory.java $
 */
public class HsqlFIDMapperFactory extends DefaultFIDMapperFactory {

	/**
     * Gets the appropriate FIDMapper for the specified table.
     *
     * @param catalog
     * @param schema
     * @param tableName
     * @param connection the active database connection to get table key
     *        information
     *
     * @return the appropriate FIDMapper for the specified table.
     *
     * @throws IOException if any error occurs.
     */
    public FIDMapper getMapper(String catalog, String schema, String tableName,
        Connection connection) throws IOException {
        ColumnInfo[] colInfos = getPkColumnInfo(catalog, schema, tableName,
                connection);
        FIDMapper mapper = null;

        if (colInfos.length == 0) {
            mapper = buildNoPKMapper(schema, tableName, connection);
        } else if (colInfos.length > 1) {
            mapper = buildMultiColumnFIDMapper(schema, tableName, connection,
                    colInfos);
        } else {
            ColumnInfo ci = colInfos[0];

            mapper = buildSingleColumnFidMapper(schema, tableName, connection,
                    ci);
        }

        if (mapper == null) {
            mapper = buildLastResortFidMapper(schema, tableName, connection,
                    colInfos);

            if (mapper == null) {
                throw new IOException(
                    "Cannot map primary key to a FID mapper, primary key columns are:\n"
                    + getColumnInfoList(colInfos));
            }
        }
        return new HsqlFIDMapper(mapper, tableName);
    }
}
