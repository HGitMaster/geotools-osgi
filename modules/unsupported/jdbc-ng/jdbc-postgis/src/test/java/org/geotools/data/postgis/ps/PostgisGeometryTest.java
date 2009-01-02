package org.geotools.data.postgis.ps;

import org.geotools.data.postgis.PostgisGeometryTestSetup;
import org.geotools.jdbc.JDBCGeometryTest;
import org.geotools.jdbc.JDBCGeometryTestSetup;

public class PostgisGeometryTest extends JDBCGeometryTest {

    @Override
    protected JDBCGeometryTestSetup createTestSetup() {
        return new PostgisGeometryTestSetup(new PostGISPSTestSetup());
    }
    
    @Override
    public void testLinearRing() throws Exception {
        // linear ring type is not a supported type in postgis
    }

}
