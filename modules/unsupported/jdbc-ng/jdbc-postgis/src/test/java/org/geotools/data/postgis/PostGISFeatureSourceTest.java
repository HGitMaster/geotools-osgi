package org.geotools.data.postgis;
import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;


public class PostGISFeatureSourceTest extends JDBCFeatureSourceTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

}
