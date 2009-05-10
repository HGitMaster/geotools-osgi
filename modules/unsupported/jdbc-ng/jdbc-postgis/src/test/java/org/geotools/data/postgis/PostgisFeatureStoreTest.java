package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.jdbc.JDBCFeatureStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisFeatureStoreTest extends JDBCFeatureStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

    @Override
    public void testAddFeatures() throws IOException {
        // wont' pass right now, see http://jira.codehaus.org/browse/GEOT-2231
    }
}
