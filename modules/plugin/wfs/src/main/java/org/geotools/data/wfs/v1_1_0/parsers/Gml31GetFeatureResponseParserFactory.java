package org.geotools.data.wfs.v1_1_0.parsers;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.geotools.data.wfs.protocol.wfs.GetFeatureParser;
import org.geotools.data.wfs.protocol.wfs.GetFeatureResponseParserFactory;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.v1_1_0.XmlSimpleFeatureParser;
import org.opengis.feature.simple.SimpleFeatureType;

@SuppressWarnings("nls")
public class Gml31GetFeatureResponseParserFactory implements GetFeatureResponseParserFactory {

    private static final String SUPPORTED_OUTPUT_FORMAT = "text/xml;subtype=gml/3.1.1";

    public boolean isAvailable() {
        return true;
    }

    public boolean canProcess( WFSResponse response ) {
        String contentType = response.getContentType();
        if (contentType == null) {
            return false;
        }
        contentType = contentType.replaceAll(" ", "");
        boolean supported = contentType.startsWith(SUPPORTED_OUTPUT_FORMAT);
        return supported;
    }

    public GetFeatureParser createParser( SimpleFeatureType requestType, WFSResponse response,
            QName featureDescriptorName ) throws IOException {
        if (!canProcess(response)) {
            throw new IllegalArgumentException(
                    "response can't be processed.. checked with canProcess() before?");
        }

        InputStream in = response.getInputStream();
        XmlSimpleFeatureParser getFeatureParser = new XmlSimpleFeatureParser(in, requestType, featureDescriptorName);
        return getFeatureParser;
    }

}
