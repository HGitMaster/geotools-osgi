package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCGeometryTest;
import org.geotools.jdbc.JDBCGeometryTestSetup;

public class PostgisGeometryTest extends JDBCGeometryTest {

    @Override
    protected JDBCGeometryTestSetup createTestSetup() {
        return new PostgisGeometryTestSetup();
    }
    
    @Override
    public void testLinearRing() throws Exception {
        // linear ring type is not a supported type in postgis
    }

}
