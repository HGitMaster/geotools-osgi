package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Logger;

import net.opengis.wfs.BaseRequestType;
import net.opengis.wfs.GetFeatureType;

import org.geotools.data.wfs.protocol.wfs.WFSOperationType;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParserFactory;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.util.logging.Logging;

@SuppressWarnings("nls")
public class Gml31GetFeatureResponseParserFactory implements WFSResponseParserFactory {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private static final String SUPPORTED_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";

    /**
     * @see WFSResponseParserFactory#isAvailable()
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @see WFSResponseParserFactory#canProcess(WFSOperationType, String)
     */
    public boolean canProcess( BaseRequestType request ) {
        if (!(request instanceof GetFeatureType)) {
            return false;
        }
        String outputFormat = ((GetFeatureType) request).getOutputFormat();
        boolean matches = SUPPORTED_OUTPUT_FORMAT.equals(outputFormat);
        return matches;
    }

    public WFSResponseParser createParser( WFS_1_1_0_DataStore wfs, WFSResponse response )
            throws IOException {
        final WFSResponseParser parser;
        final String contentType = response.getContentType();
        if (SUPPORTED_OUTPUT_FORMAT.equals(contentType)) {
            parser = new FeatureCollectionParser();
        } else {
            // We can't rely on the server returning the correct output format. Some, for example
            // CubeWerx, upon a successful GetFeature request, set the response's content-type
            // header to plain "text/xml" instead of "text/xml;subtype=gml/3.1.1". So we'll do a bit
            // of heuristics to find out what it actually returned
            final int buffSize = 256;
            PushbackInputStream pushbackIn = new PushbackInputStream(response.getInputStream(),
                    buffSize);
            byte[] buff = new byte[buffSize];
            int readCount = 0;
            int r;
            while( (r = pushbackIn.read(buff, readCount, buffSize - readCount)) != -1 ) {
                readCount += r;
                if (readCount == buffSize) {
                    break;
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(buff), response.getCharacterEncoding()));
            StringBuilder sb = new StringBuilder();
            String line;
            while( (line = reader.readLine()) != null ) {
                sb.append(line).append('\n');
            }
            String head = sb.toString();
            LOGGER.fine("response head: " + head);

            pushbackIn.unread(buff, 0, readCount);
            response.setInputStream(pushbackIn);

            if (head.contains("FeatureCollection")) {
                parser = new FeatureCollectionParser();
            } else if (head.contains("ExceptionReport")) {
                parser = new ExceptionReportParser();
            } else {
                throw new IllegalStateException("Unkown server response: " + head);
            }
        }
        return parser;
    }
}
