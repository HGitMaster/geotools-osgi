package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.opengis.feature.simple.SimpleFeatureType;

public interface GetFeatureResponseParserFactory extends WFSResponseParserFactory {

    public GetFeatureParser createParser( SimpleFeatureType requestType, WFSResponse response,
            QName featureDescriptorName ) throws IOException;
}
