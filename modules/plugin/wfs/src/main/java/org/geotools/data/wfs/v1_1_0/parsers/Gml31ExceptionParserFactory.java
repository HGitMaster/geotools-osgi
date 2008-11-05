package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.IOException;

import org.geotools.data.wfs.protocol.wfs.ExceptionParserFactory;
import org.geotools.data.wfs.protocol.wfs.ExceptionReportParser;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;

public class Gml31ExceptionParserFactory implements ExceptionParserFactory {

    public boolean isAvailable() {
        return true;
    }

    public boolean canProcess( WFSResponse response ) {
        String contentType = response.getContentType();
        return false;
    }

    public ExceptionReportParser createParser( WFSResponse response ) {
        return new ExceptionParser();
    }

    private static class ExceptionParser implements ExceptionReportParser {
        public IOException parse( WFSResponse response ) {
            return null;
        }

    }
}
