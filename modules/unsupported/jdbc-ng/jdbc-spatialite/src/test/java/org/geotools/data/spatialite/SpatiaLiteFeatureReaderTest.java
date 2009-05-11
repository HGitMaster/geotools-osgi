package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCFeatureReaderTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteFeatureReaderTest extends JDBCFeatureReaderTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }

}
