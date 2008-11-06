package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;

import org.geotools.data.wfs.protocol.wfs.GetFeatureParser;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.opengis.feature.simple.SimpleFeatureType;

public class FeatureCollectionParser implements WFSResponseParser {

    public Object parse( WFS_1_1_0_DataStore wfs, WFSResponse response ) throws IOException {

        GetFeatureType request = (GetFeatureType) response.getOriginatingRequest();
        QueryType queryType = (QueryType) request.getQuery().get(0);
        String prefixedTypeName = (String) queryType.getTypeName().get(0);
        SimpleFeatureType schema = wfs.getSchema(prefixedTypeName);
        QName featureName = wfs.getFeatureTypeName(prefixedTypeName);
        InputStream in = response.getInputStream();
        
        GetFeatureParser featureReader = new XmlSimpleFeatureParser(in, schema, featureName);
        return featureReader;
    }
}
