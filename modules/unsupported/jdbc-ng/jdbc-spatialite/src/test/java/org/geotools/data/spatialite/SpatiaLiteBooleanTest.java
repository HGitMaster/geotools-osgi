package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCBooleanTest;
import org.geotools.jdbc.JDBCBooleanTestSetup;

public class SpatiaLiteBooleanTest extends JDBCBooleanTest {

    @Override
    protected JDBCBooleanTestSetup createTestSetup() {
        return new SpatiaLiteBooleanTestSetup();
    }

}
