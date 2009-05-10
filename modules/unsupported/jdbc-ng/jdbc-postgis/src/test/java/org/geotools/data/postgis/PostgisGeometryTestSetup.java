package org.geotools.data.postgis;
import org.geotools.jdbc.JDBCGeometryTestSetup;
import org.geotools.jdbc.JDBCTestSetup;


public class PostgisGeometryTestSetup extends JDBCGeometryTestSetup {

    public PostgisGeometryTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }

    @Override
    protected void dropSpatialTable(String tableName) throws Exception {
        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '" + tableName + "'");
        runSafe("DROP TABLE \"" + tableName + "\"");
        
    }

}
