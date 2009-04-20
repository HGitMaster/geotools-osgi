package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCGeometryTestSetup;

public class SpatiaLiteGeometryTestSetup extends JDBCGeometryTestSetup {

    protected SpatiaLiteGeometryTestSetup() {
        super(new SpatiaLiteTestSetup());
    }

    @Override
    protected void dropSpatialTable(String tableName) throws Exception {
        run( "DROP TABLE " + tableName );
        run( "DELETE FROM geometry_columns WHERE f_table_name = '" + tableName + "'");
    }

}
