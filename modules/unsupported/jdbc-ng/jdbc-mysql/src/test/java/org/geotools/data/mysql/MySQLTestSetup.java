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
package org.geotools.data.mysql;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.SQLDialect;


/**
 * Test harness for mysql.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class MySQLTestSetup extends JDBCTestSetup {
  
    @Override
    protected void initializeDataSource(BasicDataSource ds, Properties db) {
        super.initializeDataSource(ds, db);
        
        ds.setDefaultTransactionIsolation( Connection.TRANSACTION_READ_COMMITTED );
    }
    
    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return new MySQLDataStoreFactory();
    }
    
    protected void setUpData() throws Exception {
        //drop old data
        try {
            run("DROP TABLE geotools.ft1;");
        } catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            run("DROP TABLE geotools.ft2;");
        } catch (Exception e) {
            //e.printStackTrace();
        }

        //create some data
        StringBuffer sb = new StringBuffer();
        //JD: COLLATE latin1_general_cs is neccesary to ensure case-sensitive string comparisons
        sb.append("CREATE TABLE geotools.ft1 ").append("(id int AUTO_INCREMENT PRIMARY KEY , ")
          .append("geometry POINT, intProperty int, ")
          .append("doubleProperty double, stringProperty varchar(255) COLLATE latin1_general_cs) ENGINE=InnoDB;");
        run(sb.toString());

        sb = new StringBuffer();
        sb.append("INSERT INTO geotools.ft1 VALUES (")
          .append("0,GeometryFromText('POINT(0 0)',4326), 0, 0.0,'zero');");
        run(sb.toString());

        sb = new StringBuffer();
        sb.append("INSERT INTO geotools.ft1 VALUES (")
          .append("0,GeometryFromText('POINT(1 1)',4326), 1, 1.1,'one');");
        run(sb.toString());

        sb = new StringBuffer();
        sb.append("INSERT INTO geotools.ft1 VALUES (")
          .append("0,GeometryFromText('POINT(2 2)',4326), 2, 2.2,'two');");
        run(sb.toString());
    }
}
