package org.geotools.data.wfs.v1_1_0;

import java.io.IOException;

import net.opengis.wfs.GetFeatureType;

import org.geotools.data.Query;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public interface WFSStrategy {

    /**
     * Returns the feature type that shall result of issueing the given request, adapting the
     * original feature type for the request's type name in terms of the query CRS and requested
     * attributes
     * 
     * @param query
     * @return
     * @throws IOException
     */
    public SimpleFeatureType getQueryType(final WFS_1_1_0_DataStore ds, final Query query)
            throws IOException;

    public RequestComponents createGetFeatureRequest(WFS_1_1_0_DataStore ds, WFSProtocol wfs,
            Query query, String outputFormat) throws IOException;

    /**
     * Holds the components needed by the data store to issue and post process a GetFeature request
     */
    public class RequestComponents {

        /**
         * The filter to post-process (the one not supported by the server)
         */
        private Filter postFilter = Filter.EXCLUDE;

        /**
         * The GetFeature request to issue to the WFS
         */
        private GetFeatureType serverRequest;

        public Filter getPostFilter() {
            return postFilter;
        }

        public void setPostFilter(Filter postFilter) {
            this.postFilter = postFilter;
        }

        public GetFeatureType getServerRequest() {
            return serverRequest;
        }

        public void setServerRequest(GetFeatureType serverRequest) {
            this.serverRequest = serverRequest;
        }
    }
}
