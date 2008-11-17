package org.geotools.data.wfs.v1_1_0;

import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.filter.Filter;

public class IonicStrategy extends DefaultWFSStrategy {

    private static Configuration filter_1_0_0_Configuration = new OGCConfiguration();

    /**
     * Ionic does not declare the supported output formats in the caps, yet it fails if asked
     * for {@code text/xml; subtype=gml/3.1.1} but succeeds if asked for {@code GML3}
     */
    @Override
    public String getDefaultOutputFormat(WFSProtocol wfs) {
        return "GML3";
    }

    // @Override
    // protected Configuration getFilterConfiguration() {
    // return filter_1_0_0_Configuration;
    // }

    /**
     * Ionic seems not to support any filtering well
     */
    @Override
    protected Filter[] splitFilters(WFS_1_1_0_DataStore ds, WFSProtocol wfs, Filter queryFilter) {
        return new Filter[] { Filter.INCLUDE, queryFilter };
    }
}
