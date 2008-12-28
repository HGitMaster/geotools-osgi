package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCGeometrylessTestSetup;

public class PostgisGeometrylessTestSetup extends JDBCGeometrylessTestSetup {

    protected PostgisGeometrylessTestSetup() {
        super(new PostGISTestSetup());
        
    }

    @Override
    protected void createPersonTable() throws Exception {
    }

    @Override
    protected void dropPersonTable() throws Exception {
    }

    @Override
    protected void dropZipCodeTable() throws Exception {
    }

}
