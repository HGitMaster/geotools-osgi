package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCEmptyTestSetup;

public class SpatiaLiteEmptyTestSetup extends JDBCEmptyTestSetup {

    protected SpatiaLiteEmptyTestSetup() {
        super(new SpatiaLiteTestSetup());
    }

    @Override
    protected void createEmptyTable() throws Exception {
        run( "CREATE TABLE empty( id INTEGER )" );
        run( "SELECT AddGeometryColumn('empty','geom',4326,'POINT',2)");
    }

    @Override
    protected void dropEmptyTable() throws Exception {
        run( "DROP TABLE empty");
        run( "DELETE FROM geometry_columns where f_table_name = 'empty'");
    }

}
