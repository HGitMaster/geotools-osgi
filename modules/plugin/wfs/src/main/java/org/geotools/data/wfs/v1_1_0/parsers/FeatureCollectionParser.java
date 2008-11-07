package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;

import org.geotools.data.DataUtilities;
import org.geotools.data.wfs.protocol.wfs.GetFeatureParser;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.protocol.wfs.WFSResponseParser;
import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;

public class FeatureCollectionParser implements WFSResponseParser {

    public Object parse( WFS_1_1_0_DataStore wfs, WFSResponse response ) throws IOException {

        GetFeatureType request = (GetFeatureType) response.getOriginatingRequest();
        QueryType queryType = (QueryType) request.getQuery().get(0);
        String prefixedTypeName = (String) queryType.getTypeName().get(0);
        SimpleFeatureType schema = wfs.getSchema(prefixedTypeName);
        List<String> propertyNames = queryType.getPropertyName();
        if (propertyNames.size() > 0) {
            // the expected schema may contain less properties than the full schema. Let's say it to
            // the parser so it does not parse unnecessary attributes in case the WFS returns more
            // than requested
            String[] properties = propertyNames.toArray(new String[propertyNames.size()]);
            try {
                schema = DataUtilities.createSubType(schema, properties);
            } catch (SchemaException e) {
                throw (RuntimeException) new RuntimeException().initCause(e);
            }
        }
        QName featureName = wfs.getFeatureTypeName(prefixedTypeName);
        InputStream in = response.getInputStream();

        GetFeatureParser featureReader = new XmlSimpleFeatureParser(in, schema, featureName);
        return featureReader;
    }
}
