package org.geotools.data.db2;

import org.geotools.jdbc.JDBCFeatureLockingTest;
import org.geotools.jdbc.JDBCTestSetup;

public class DB2FeatureLockingTest extends JDBCFeatureLockingTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new DB2TestSetup();
    }

}
