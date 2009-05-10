package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class SpatiaLiteNoPrimaryKeyTestSetup extends JDBCNoPrimaryKeyTestSetup {

    protected SpatiaLiteNoPrimaryKeyTestSetup() {
        super(new SpatiaLiteTestSetup());
    }

    @Override
    protected void createLakeTable() throws Exception {
        run( "CREATE TABLE lake (id INTEGER )");
        run( "SELECT AddGeometryColumn('lake','geom',4326,'POLYGON',2)");
        run( "ALTER TABLE lake add name VARCHAR");
        
        run( "INSERT INTO lake VALUES (0," +
            "GeomFromText('POLYGON((12 6, 14 8, 16 6, 16 4, 14 4, 12 6))',4326),'muddy')");
    }

    @Override
    protected void dropLakeTable() throws Exception {
        run( "DROP TABLE lake");
        run( "DELETE FROM geometry_columns WHERE f_table_name = 'lake'");
    }

}
