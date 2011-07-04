package org.geotools.data.wfs.protocol.wfs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.WfsFactory;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.junit.Test;

/**
 * @author Gabriel Roldan
 * @since 2.6.x
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/wfs/src/test/java/org/geotools/data/wfs/protocol/wfs/WFSExtensionsTest.java $
 * @version $Id: WFSExtensionsTest.java 37306 2011-05-25 06:13:21Z mbedward $
 */
@SuppressWarnings("nls")
public class WFSExtensionsTest {

    @Test
    public void testFindParserFactory() {
        GetFeatureType request = WfsFactory.eINSTANCE.createGetFeatureType();
        request.setOutputFormat("application/fakeFormat");
        WFSResponseParserFactory factory = WFSExtensions.findParserFactory(request);
        assertNotNull(factory);
        assertTrue(factory instanceof TestParserFactory);
    }

    public static class TestParserFactory implements WFSResponseParserFactory {

        public boolean canProcess( EObject request ) {
            return request instanceof GetFeatureType
                    && "application/fakeFormat"
                            .equals(((GetFeatureType) request).getOutputFormat());
        }
        public boolean isAvailable() {
            return true;
        }

        public WFSResponseParser createParser( WFS_1_1_0_DataStore wfs, WFSResponse response )
                throws IOException {
            throw new UnsupportedOperationException("not intended to be called for this test class");
        }
    }
}
