package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;

import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;


public interface WFSResponseParser {

    Object parse( WFS_1_1_0_DataStore wfs, WFSResponse response) throws IOException;

}
