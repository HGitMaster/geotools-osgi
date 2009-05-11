package org.geotools.data.h2;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class H2EmptyTestSetup extends JDBCEmptyTestSetup {

    protected H2EmptyTestSetup() {
        super(new H2TestSetup());
    }

    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        
        dataStore.setDatabaseSchema( null );
    }
    
    @Override
    protected void createEmptyTable() throws Exception {
        run( "CREATE TABLE \"empty\" (id int,geom blob)");
        
    }

    @Override
    protected void dropEmptyTable() throws Exception {
        runSafe( "DROP TABLE \"empty\"" );
    }

}
