package org.geotools.data.wfs.protocol.wfs;

public interface WFSResponseParserFactory {

    public boolean isAvailable();

    public boolean canProcess( WFSResponse response );

}
