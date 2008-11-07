package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;

import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;

import net.opengis.wfs.BaseRequestType;

public interface WFSResponseParserFactory {

    public boolean isAvailable();

    public boolean canProcess( BaseRequestType request );

    public WFSResponseParser createParser( WFS_1_1_0_DataStore wfs, WFSResponse response ) throws IOException;

}
