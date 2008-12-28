package org.geotools.data.postgis;
import org.geotools.jdbc.JDBCGeometryTestSetup;


public class PostgisGeometryTestSetup extends JDBCGeometryTestSetup {

    protected PostgisGeometryTestSetup() {
        super(new PostGISTestSetup());
    }

    @Override
    protected void dropSpatialTable(String tableName) throws Exception {
        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '" + tableName + "'");
        runSafe("DROP TABLE \"" + tableName + "\"");
        
    }

}
