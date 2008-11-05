package org.geotools.data.wfs.protocol.wfs;

public interface ExceptionParserFactory extends WFSResponseParserFactory {

    public ExceptionReportParser createParser( WFSResponse response );
}
