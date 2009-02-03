package org.geotools.jdbc;

import java.sql.SQLException;

public abstract class JDBCNoPrimaryKeyTestSetup extends JDBCDelegatingTestSetup {

    protected JDBCNoPrimaryKeyTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        try {
            dropLakeTable();
        } catch (SQLException e) {
        }

        //create all the data
        createLakeTable();
    }
    
    /**
     * Creates a table with the following schema:
     * <p>
     * lake( id:Integer; geom:Polygon; name:String )
     * </p>
     * <p>
     * The table should be populated with the following data:
     * <pre>
     * 0 | POLYGON((12 6, 14 8, 16 6, 16 4, 14 4, 12 6));srid=4326 | "muddy"
     * </pre>
     * </p>
     * <p>
     * For this test make sure the table has no primary key whatsoever.
     * </p>
     */
    protected abstract void createLakeTable() throws Exception;
    
    /**
     * Drops the "lake" table.
     */
    protected abstract void dropLakeTable() throws Exception;

}
