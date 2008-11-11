package org.geotools.data.wfs.v1_1_0;

import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.filter.v1_0.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.opengis.filter.Filter;

public class MapServerStrategy extends DefaultWFSStrategy {

    private static Configuration filter_1_0_0_Configuration = new OGCConfiguration();

    @Override
    public String getDefaultOutputFormat(WFSProtocol wfs) {
        return "GML2";
    }

    @Override
    protected Configuration getFilterConfiguration() {
        return filter_1_0_0_Configuration;
    }

    /**
     * MapServer seems not to support any filtering
     */
    @Override
    protected Filter[] splitFilters(WFS_1_1_0_DataStore ds, WFSProtocol wfs, Filter queryFilter) {
        return new Filter[] { Filter.INCLUDE, queryFilter };
    }
}
