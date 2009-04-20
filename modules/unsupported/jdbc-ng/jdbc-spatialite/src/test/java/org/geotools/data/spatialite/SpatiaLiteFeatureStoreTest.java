package org.geotools.data.spatialite;

import java.io.IOException;

import org.geotools.jdbc.JDBCFeatureStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteFeatureStoreTest extends JDBCFeatureStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }

    @Override
    public void testAddNullAttributes() throws IOException {
        //JD: as far as I can tell you can't have null geometries
        // in spatialite...
    }
}
