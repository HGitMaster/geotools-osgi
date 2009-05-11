package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCPrimaryKeyTest;
import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;

public class SpatiaLitePrimaryKeyTest extends JDBCPrimaryKeyTest {

    @Override
    protected JDBCPrimaryKeyTestSetup createTestSetup() {
        return new SpatiaLitePrimaryKeyTestSetup();
    }

}
