package org.geotools.data.db2;

import org.geotools.jdbc.JDBCNoPrimaryKeyTest;
import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class DB2NoPrimaryKeyTest extends JDBCNoPrimaryKeyTest {

    @Override
    protected JDBCNoPrimaryKeyTestSetup createTestSetup() {
        return new DB2NoPrimaryKeyTestSetup();
    }

}
