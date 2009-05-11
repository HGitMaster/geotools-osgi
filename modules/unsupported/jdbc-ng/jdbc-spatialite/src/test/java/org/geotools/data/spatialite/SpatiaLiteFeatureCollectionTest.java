package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCFeatureCollectionTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteFeatureCollectionTest extends JDBCFeatureCollectionTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }

}
