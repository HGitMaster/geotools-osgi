package org.geotools.data.postgis.ps;

import org.geotools.data.postgis.PostgisGeometrylessTestSetup;
import org.geotools.jdbc.JDBCGeometrylessTest;
import org.geotools.jdbc.JDBCGeometrylessTestSetup;

public class PostgisGeometrylessTest extends JDBCGeometrylessTest {

    @Override
    protected JDBCGeometrylessTestSetup createTestSetup() {
        return new PostgisGeometrylessTestSetup(new PostGISPSTestSetup());
    }

}
