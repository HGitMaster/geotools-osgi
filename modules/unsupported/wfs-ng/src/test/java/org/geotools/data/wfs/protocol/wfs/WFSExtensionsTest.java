package org.geotools.data.wfs.protocol.wfs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.WfsFactory;

import org.eclipse.emf.ecore.EObject;
import org.geotools.data.wfs.v1_1_0.WFSNGDataStore;
import org.junit.Test;

/**
 * @author Gabriel Roldan
 * @since 2.6.x
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/wfs-ng/src/test/java/org/geotools/data/wfs/protocol/wfs/WFSExtensionsTest.java $
 * @version $Id: WFSExtensionsTest.java 35310 2010-04-30 10:32:15Z jive $
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

        public WFSResponseParser createParser( WFSNGDataStore wfs, WFSResponse response )
                throws IOException {
            throw new UnsupportedOperationException("not intended to be called for this test class");
        }
    }
}
