/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.oracle;

import java.sql.SQLException;

import org.geotools.jdbc.JDBCGeometrylessTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class OracleGeometrylessTestSetup extends JDBCGeometrylessTestSetup {

    protected OracleGeometrylessTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }

    @Override
    protected void createPersonTable() throws Exception {
        //set up table
        run("CREATE TABLE person (fid int, id int, "
            + " name varchar(255), age int, PRIMARY KEY (fid) )");
        run("CREATE SEQUENCE person_pkey_seq START WITH 0 MINVALUE 0");
        run("CREATE TRIGGER person_pkey_trigger " + 
            "BEFORE INSERT ON person " + 
            "FOR EACH ROW " + 
              "BEGIN " + 
                "SELECT person_pkey_seq.nextval INTO :new.fid FROM dual; " + 
              "END;");
        
        // insert data
        run("INSERT INTO person(id,name,age) VALUES ( 0, 'Paul', 32)");
        run("INSERT INTO person(id,name,age) VALUES ( 1, 'Anne', 40)");
    }

    @Override
    protected void dropPersonTable() throws SQLException {
        runSafe("DROP SEQUENCE person_pkey_seq");
        runSafe("DROP TABLE person");
    }

    @Override
    protected void dropZipCodeTable() throws SQLException {
        runSafe("DROP TABLE zipcode");
    }

}
