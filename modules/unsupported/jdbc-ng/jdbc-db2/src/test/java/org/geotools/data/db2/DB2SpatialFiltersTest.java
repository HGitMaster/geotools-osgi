package org.geotools.data.db2;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCSpatialFiltersTest;

public class DB2SpatialFiltersTest extends JDBCSpatialFiltersTest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new DB2DataStoreAPITestSetup();
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dataStore.setDatabaseSchema("geotools");
	}

}
