package org.geotools.data.wfs.protocol.wfs;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Gabriel Roldan
 * @since 2.6.x
 * @version $Id: WFSExtensionsTest.java 31769 2008-11-05 15:21:49Z groldan $
 */
public class WFSExtensionsTest {

    @Test
    public void testFindParserFactory() {
        WFSResponse response = new WFSResponse(null, "application/testException", null);
        WFSResponseParserFactory factory = WFSExtensions.findParserFactory(response);
        assertNotNull(factory);
        assertTrue(factory instanceof TestExceptionParserFactory);
    }

    public static class TestExceptionParserFactory implements ExceptionParserFactory {

        public ExceptionReportParser createParser( WFSResponse response ) {
            return null;
        }

        public boolean canProcess( WFSResponse response ) {
            String contentType = response.getContentType();
            return "application/testException".equals(contentType);
        }

        public boolean isAvailable() {
            return true;
        }

    }
}
