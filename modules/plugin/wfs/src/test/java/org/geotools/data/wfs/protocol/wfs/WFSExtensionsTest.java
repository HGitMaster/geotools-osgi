package org.geotools.data.wfs.protocol.wfs;

import static org.junit.Assert.*;

import java.io.IOException;

import net.opengis.wfs.BaseRequestType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.WfsFactory;

import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.junit.Test;

/**
 * @author Gabriel Roldan
 * @since 2.6.x
 * @version $Id: WFSExtensionsTest.java 31792 2008-11-06 19:17:35Z groldan $
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

        public boolean canProcess( BaseRequestType request ) {
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
