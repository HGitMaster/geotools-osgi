package org.geotools.data.wfs;

public interface WFSResponseParserFactory {

    public boolean isAvailable();

    public boolean canProcess( WFSResponse response );

    public WFSResponseParser createParser( WFSResponse response );
}
