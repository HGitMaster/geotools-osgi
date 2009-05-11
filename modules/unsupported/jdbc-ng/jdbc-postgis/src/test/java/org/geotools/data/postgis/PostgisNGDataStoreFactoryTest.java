package org.geotools.data.postgis;

import static org.geotools.data.postgis.PostgisNGDataStoreFactory.PORT;
import static org.geotools.jdbc.JDBCDataStoreFactory.DATABASE;
import static org.geotools.jdbc.JDBCDataStoreFactory.DBTYPE;
import static org.geotools.jdbc.JDBCDataStoreFactory.HOST;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;
import static org.geotools.jdbc.JDBCDataStoreFactory.USER;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTestSupport;

public class PostgisNGDataStoreFactoryTest extends JDBCTestSupport {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }
    
    public void testCreateConnection() throws Exception {
        PostgisNGDataStoreFactory factory = new PostgisNGDataStoreFactory();
        
        Properties db = new Properties();
        db.load(getClass().getResourceAsStream("factory.properties"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(HOST.key, db.getProperty(HOST.key));
        params.put(DATABASE.key, db.getProperty(DATABASE.key));
        params.put(PORT.key, db.getProperty(PORT.key));
        params.put(USER.key, db.getProperty(USER.key));
        params.put(PASSWD.key, db.getProperty(PASSWD.key));
        
        params.put(DBTYPE.key, factory.getDatabaseID());

        assertTrue(factory.canProcess(params));
        JDBCDataStore store = factory.createDataStore(params);
        assertNotNull(store);
        try {
            // check dialect
            assertTrue(store.getSQLDialect() instanceof PostGISDialect);
            // force connection usage
            assertNotNull(store.getSchema(tname("ft1")));
        } finally {
            store.dispose();
        }
    }

}
