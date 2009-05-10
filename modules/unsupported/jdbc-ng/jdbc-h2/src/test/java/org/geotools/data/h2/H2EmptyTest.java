package org.geotools.data.h2;

import org.geotools.jdbc.JDBCEmptyTest;
import org.geotools.jdbc.JDBCEmptyTestSetup;

public class H2EmptyTest extends JDBCEmptyTest {

    @Override
    protected JDBCEmptyTestSetup createTestSetup() {
        return new H2EmptyTestSetup();
    }

}
