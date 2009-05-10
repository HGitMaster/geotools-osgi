package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCSpatialFiltersTest;

public class SpatiaLiteSpatialFiltersTest extends JDBCSpatialFiltersTest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new SpatiaLiteDataStoreAPITestSetup();
    }

}
