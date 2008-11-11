package org.geotools.data.wfs.v1_1_0;

import java.util.Set;

import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.xml.Configuration;

@SuppressWarnings( { "nls", "unchecked" })
public class GeoServerStrategy extends DefaultWFSStrategy {

    private static Configuration filter_1_0_0_Configuration = new OGCConfiguration();

    private static final String GEOSERVER_WRONG_FORMAT_NAME = "text/gml; subtype=gml/3.1.1";

    /**
     * GeoServer versions prior to 2.0 state {@code text/gml; subtype=gml/3.1.1} instead of {@code
     * text/xml; subtype=gml/3.1.1}
     */
    public String getDefaultOutputFormat(WFSProtocol wfs) {
        try {
            return super.getDefaultOutputFormat(wfs);
        } catch (IllegalArgumentException e) {
            Set<String> supportedOutputFormats = wfs.getSupportedGetFeatureOutputFormats();
            if (supportedOutputFormats.contains(GEOSERVER_WRONG_FORMAT_NAME)) {
                return DefaultWFSStrategy.DEFAULT_OUTPUT_FORMAT;
            }
            throw new IllegalArgumentException("Server does not support '" + DEFAULT_OUTPUT_FORMAT
                    + "' output format: " + supportedOutputFormats);
        }
    }

    /**
     * GeoServer versions lower than 2.0 can only parse Filter 1.0
     */
    @Override
    protected Configuration getFilterConfiguration() {
        return filter_1_0_0_Configuration;
    }

}
