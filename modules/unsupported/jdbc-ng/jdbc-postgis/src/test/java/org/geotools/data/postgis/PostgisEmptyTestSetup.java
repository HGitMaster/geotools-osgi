package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCEmptyTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisEmptyTestSetup extends JDBCEmptyTestSetup {

    public PostgisEmptyTestSetup(JDBCTestSetup delegate) {
        super(delegate);
        
    }

    @Override
    protected void createEmptyTable() throws Exception {
        run("CREATE TABLE \"empty\"(\"key\" serial primary key)");
        run("SELECT AddGeometryColumn('empty', 'geom', -1, 'GEOMETRY', 2)");
    }

    @Override
    protected void dropEmptyTable() throws Exception {
        runSafe("DELETE GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'empty'");
        runSafe("DROP TABLE \"empty\"");
    }

}
