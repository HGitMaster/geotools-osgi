package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCEmptyTestSetup;

public class PostgisEmptyTestSetup extends JDBCEmptyTestSetup {

    protected PostgisEmptyTestSetup() {
        super(new PostGISTestSetup());
        
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
