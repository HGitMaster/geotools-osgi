package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCFeatureCollectionTest;
import org.geotools.jdbc.JDBCTestSetup;


public class PostgisFeatureCollectionTest extends JDBCFeatureCollectionTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

}
