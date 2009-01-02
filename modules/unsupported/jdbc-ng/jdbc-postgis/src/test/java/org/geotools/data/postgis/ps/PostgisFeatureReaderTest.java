package org.geotools.data.postgis.ps;

import org.geotools.jdbc.JDBCFeatureReaderTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisFeatureReaderTest extends JDBCFeatureReaderTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISPSTestSetup();
    }

}
