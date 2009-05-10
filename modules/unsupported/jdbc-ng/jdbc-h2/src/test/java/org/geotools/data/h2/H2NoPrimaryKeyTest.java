package org.geotools.data.h2;

import org.geotools.jdbc.JDBCNoPrimaryKeyTest;
import org.geotools.jdbc.JDBCNoPrimaryKeyTestSetup;

public class H2NoPrimaryKeyTest extends JDBCNoPrimaryKeyTest {
    
    

    @Override
    protected JDBCNoPrimaryKeyTestSetup createTestSetup() {
        return new H2NoPrimaryKeyTestSetup();
    }

}
