package org.geotools.data.spatialite;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

public class SpatiaLiteDataStoreFactory extends JDBCDataStoreFactory {

    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new SpatiaLiteDialect( dataStore );
    }

    @Override
    protected String getDatabaseID() {
        return "spatialite";
    }
    
    @Override
    protected String getDriverClassName() {
        return "SQLite.JDBCDriver";
    }
    
    public String getDescription() {
        return "SpatiaLite";
    }

    @Override
    protected String getValidationQuery() {
        return null;
    }
}
