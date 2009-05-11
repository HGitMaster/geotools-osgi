package org.geotools.data.postgis.ps;

import java.io.IOException;
import java.util.Properties;

import org.geotools.data.postgis.PostGISDialect;
import org.geotools.data.postgis.PostGISPSDialect;
import org.geotools.data.postgis.PostGISTestSetup;
import org.geotools.jdbc.JDBCDataStore;

public class PostGISPSTestSetup extends PostGISTestSetup {

    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        
        // for this test we need a PS based dialect
        PostGISPSDialect dialect = new PostGISPSDialect(dataStore, (PostGISDialect) dataStore.getSQLDialect());
        dialect.setLooseBBOXEnabled(false);
        dataStore.setSQLDialect(dialect);
    }

    @Override
    protected void fillConnectionProperties(Properties db) throws IOException {
        // override to use the same property file as the non ps path
        db.load( PostGISTestSetup.class.getResourceAsStream( "db.properties") );
    }
}
