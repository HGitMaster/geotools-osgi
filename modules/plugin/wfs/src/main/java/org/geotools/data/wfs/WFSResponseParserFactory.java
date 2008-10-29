package org.geotools.data.wfs;

import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;

public interface WFSResponseParserFactory {

    public boolean isAvailable();

    public boolean canProcess( WFSResponse response );

    public WFSResponseParser createParser( WFSResponse response );
}
