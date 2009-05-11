package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCFeatureReaderTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisFeatureReaderTest extends JDBCFeatureReaderTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

}
