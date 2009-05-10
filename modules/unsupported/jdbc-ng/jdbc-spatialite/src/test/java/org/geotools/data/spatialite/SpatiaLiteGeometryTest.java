package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCGeometryTest;
import org.geotools.jdbc.JDBCGeometryTestSetup;

public class SpatiaLiteGeometryTest extends JDBCGeometryTest {

    @Override
    protected JDBCGeometryTestSetup createTestSetup() {
        return new SpatiaLiteGeometryTestSetup();
    }
    
    @Override
    public void testLinearRing() throws Exception {
        //JD: spatialite does not do linear rings
    }

}
