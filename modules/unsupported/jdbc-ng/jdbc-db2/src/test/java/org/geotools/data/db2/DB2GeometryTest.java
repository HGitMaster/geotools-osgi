package org.geotools.data.db2;

import org.geotools.jdbc.JDBCGeometryTest;
import org.geotools.jdbc.JDBCGeometryTestSetup;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

public class DB2GeometryTest extends JDBCGeometryTest {

    @Override
    protected JDBCGeometryTestSetup createTestSetup() {
        return new DB2GeometryTestSetup();
    }

    public void testLinearRing() throws Exception {
        assertEquals(LineString.class, checkGeometryType(LinearRing.class));
    }

}
