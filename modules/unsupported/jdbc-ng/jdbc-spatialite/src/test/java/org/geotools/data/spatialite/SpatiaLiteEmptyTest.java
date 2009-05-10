package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCEmptyTest;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class SpatiaLiteEmptyTest extends JDBCEmptyTest {

    @Override
    protected JDBCEmptyTestSetup createTestSetup() {
        return new SpatiaLiteEmptyTestSetup();
    }

}
