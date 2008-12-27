package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

public class PostGISTestSetup extends JDBCTestSetup {

    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        dataStore.setDatabaseSchema("public");
    }

    @Override
    protected void setUpData() throws Exception {
        runSafe("DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ft1'");
        runSafe("DROP TABLE \"ft1\"");
        runSafe("DROP TABLE \"ft2\"");
        
        run("CREATE TABLE \"ft1\"(" //
                + "\"id\" serial primary key, " //
                + "\"geometry\" geometry, " //
                + "\"intProperty\" int," //
                + "\"doubleProperty\" double precision, " // 
                + "\"stringProperty\" varchar)");
        run("INSERT INTO GEOMETRY_COLUMNS VALUES('', 'public', 'ft1', 'geometry', 2, '4326', 'POINT')");
        run("CREATE INDEX FT1_GEOMETRY_INDEX ON \"ft1\" USING GIST (\"geometry\") ");
        
        run("INSERT INTO \"ft1\" VALUES(0, GeometryFromText('POINT(0 0)', 4326), 0, 0.0, 'zero')"); 
        run("INSERT INTO \"ft1\" VALUES(1, GeometryFromText('POINT(1 1)', 4326), 1, 1.1, 'one')");
        run("INSERT INTO \"ft1\" VALUES(2, GeometryFromText('POINT(2 2)', 4326), 2, 2.2, 'two')");
    }

    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return new PostgisNGDataStoreFactory();
    }

}
