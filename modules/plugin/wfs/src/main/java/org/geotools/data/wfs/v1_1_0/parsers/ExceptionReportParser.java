package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.opengis.ows10.ExceptionReportType;
import net.opengis.ows10.ExceptionType;
import net.opengis.wfs.BaseRequestType;

import org.geotools.data.wfs.protocol.wfs.WFSException;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.ows.OWS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;

@SuppressWarnings({"nls", "unchecked"})
public class ExceptionReportParser implements WFSResponseParser {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * @see WFSResponseParser#parse(WFSProtocol, WFSResponse)
     */
    public Object parse( WFS_1_1_0_DataStore wfs, WFSResponse response ) {
        WFSConfiguration configuration = new WFSConfiguration();
        Parser parser = new Parser(configuration);
        InputStream responseStream = response.getInputStream();
        Charset responseCharset = response.getCharacterEncoding();
        Reader reader = new InputStreamReader(responseStream, responseCharset);
        Object parsed;
        try {
            parsed = parser.parse(reader);
            if (!(parsed instanceof net.opengis.ows10.ExceptionReportType)) {
                return new IOException("Unrecognized server error");
            }
        } catch (Exception e) {
            return new WFSException("Exception parsing server exception report", e);
        }
        net.opengis.ows10.ExceptionReportType report = (ExceptionReportType) parsed;
        List<ExceptionType> exceptions = report.getException();

        BaseRequestType originatingRequest = response.getOriginatingRequest();
        StringBuilder msg = new StringBuilder("WFS returned an exception");
        if (originatingRequest != null) {
            Encoder encoder = new Encoder(configuration);
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                encoder.encode(originatingRequest, OWS.ExceptionReport, out);
                String requestStr = out.toString();
                msg.append(". Originating request is: \n").append(requestStr).append("\n");
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error encoding request for exception report", e);
            }
        }
        WFSException result = new WFSException(msg.toString());
        for( ExceptionType ex : exceptions ) {
            result.addExceptionReport(String.valueOf(ex.getExceptionText()));
        }
        return result;
    }
}
