package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCNoPrimaryKeyTest;
import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class PostgisNoPrimaryKeyTest extends JDBCNoPrimaryKeyTest {

    @Override
    protected JDBCNoPrimaryKeyTestSetup createTestSetup() {
        return new PostgisNoPrimaryKeyTestSetup();
    }

}
