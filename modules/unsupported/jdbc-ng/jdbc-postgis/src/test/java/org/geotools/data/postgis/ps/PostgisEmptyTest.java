package org.geotools.data.postgis.ps;

import org.geotools.data.postgis.PostgisEmptyTestSetup;
import org.geotools.jdbc.JDBCEmptyTest;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class PostgisEmptyTest extends JDBCEmptyTest {

    @Override
    protected JDBCEmptyTestSetup createTestSetup() {
        return new PostgisEmptyTestSetup(new PostGISPSTestSetup());
    }

}
