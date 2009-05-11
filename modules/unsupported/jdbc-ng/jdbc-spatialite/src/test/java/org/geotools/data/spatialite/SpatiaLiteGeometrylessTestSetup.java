package org.geotools.data.spatialite;


public class SpatiaLiteGeometrylessTestSetup extends org.geotools.jdbc.JDBCGeometrylessTestSetup {

    protected SpatiaLiteGeometrylessTestSetup() {
        super(new SpatiaLiteTestSetup());
    }

    @Override
    protected void createPersonTable() throws Exception {
        run( "CREATE TABLE person (id INTEGER, name VARCHAR, age INTEGER)");
        run( "INSERT INTO person VALUES (0, 'Paul', 32)");
        run( "INSERT INTO person VALUES (1, 'Anne', 40)");
    }

    @Override
    protected void dropPersonTable() throws Exception {
        run( "DROP TABLE person");
    }

    @Override
    protected void dropZipCodeTable() throws Exception {
        run( "DROP TABLE zipcode");
    }

}
