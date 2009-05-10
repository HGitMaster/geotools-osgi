package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCPrimaryKeyTest;
import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;

public class PostgisPrimaryKeyTest extends JDBCPrimaryKeyTest {

    @Override
    protected JDBCPrimaryKeyTestSetup createTestSetup() {
        return new PostgisPrimaryKeyTestSetup(new PostGISTestSetup());
    }

}
