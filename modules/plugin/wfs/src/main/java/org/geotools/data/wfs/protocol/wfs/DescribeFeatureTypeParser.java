package org.geotools.data.wfs.protocol.wfs;

import org.opengis.feature.simple.SimpleFeatureType;

public interface DescribeFeatureTypeParser extends WFSResponseParser {

    public SimpleFeatureType parse( WFSResponse response, WFSProtocol protocol );

}
