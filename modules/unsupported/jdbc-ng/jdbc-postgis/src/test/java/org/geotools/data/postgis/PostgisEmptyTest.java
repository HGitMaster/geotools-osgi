package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCEmptyTest;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class PostgisEmptyTest extends JDBCEmptyTest {

    @Override
    protected JDBCEmptyTestSetup createTestSetup() {
        return new PostgisEmptyTestSetup(new PostGISTestSetup());
    }

}
