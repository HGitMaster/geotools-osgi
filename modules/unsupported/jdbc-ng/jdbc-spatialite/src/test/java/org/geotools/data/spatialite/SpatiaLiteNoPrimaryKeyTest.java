package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCNoPrimaryKeyTest;
import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class SpatiaLiteNoPrimaryKeyTest extends JDBCNoPrimaryKeyTest {

    @Override
    protected JDBCNoPrimaryKeyTestSetup createTestSetup() {
        return new SpatiaLiteNoPrimaryKeyTestSetup();
    }

}
