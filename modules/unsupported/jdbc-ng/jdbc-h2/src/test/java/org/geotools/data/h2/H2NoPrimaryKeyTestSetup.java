package org.geotools.data.h2;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class H2NoPrimaryKeyTestSetup extends JDBCNoPrimaryKeyTestSetup {

    protected H2NoPrimaryKeyTestSetup() {
        super(new H2TestSetup());
    }
    
    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        
        dataStore.setDatabaseSchema( null );
    }

    @Override
    protected void createLakeTable() throws Exception {
        run("CREATE TABLE \"lake\"(\"id\" int, \"geom\" blob, \"name\" varchar )");

        run("INSERT INTO \"lake\" (\"id\",\"geom\",\"name\") VALUES ( 0,"
                + "GeomFromText('POLYGON((12 6, 14 8, 16 6, 16 4, 14 4, 12 6))',4326),"
                + "'muddy')");
    }

    @Override
    protected void dropLakeTable() throws Exception {
        run("DROP TABLE \"lake\"");
    }

}
