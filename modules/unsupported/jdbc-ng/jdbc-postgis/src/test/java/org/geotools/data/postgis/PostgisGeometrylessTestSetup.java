package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCGeometrylessTestSetup;

public class PostgisGeometrylessTestSetup extends JDBCGeometrylessTestSetup {

    protected PostgisGeometrylessTestSetup() {
        super(new PostGISTestSetup());
    }

    @Override
    protected void createPersonTable() throws Exception {
        run("CREATE TABLE \"person\"(\"fid\" serial PRIMARY KEY, \"id\" int, "
                + "\"name\" varchar, \"age\" int)");
        run("INSERT INTO \"person\" (\"id\",\"name\",\"age\") VALUES (0,'Paul',32)");
        run("INSERT INTO \"person\" (\"id\",\"name\",\"age\") VALUES (0,'Anne',40)");
    }

    @Override
    protected void dropPersonTable() throws Exception {
        run("DROP TABLE \"person\"");
    }

    @Override
    protected void dropZipCodeTable() throws Exception {
        run("DROP TABLE \"zipcode\"");
    }

}
