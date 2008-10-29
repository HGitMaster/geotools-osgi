package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;


public interface ExceptionReportParser extends WFSResponseParser {

    public IOException parse( WFSResponse response );
}
