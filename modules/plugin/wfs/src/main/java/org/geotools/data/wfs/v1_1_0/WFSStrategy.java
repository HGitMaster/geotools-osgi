/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs.v1_1_0;

import java.io.IOException;

import net.opengis.wfs.GetFeatureType;

import org.geotools.data.Query;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * An interface to allow plugging different strategy objects into a {@link WFSDataStore} to take
 * care of specific WFS implementations limitations or deviations from the spec.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: WFSStrategy.java 31823 2008-11-11 16:11:49Z groldan $
 * @since 2.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/v1_1_0/WFSStrategy.java $
 * @see WFSDataStoreFactory
 * @see DefaultWFSStrategy
 * @see CubeWerxStrategy
 */
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

    /**
     * Creates a GetFeature request that the server implementation this strategy works upon can deal
     * with, and returns both the appropriate request to send to the server as well as the
     * {@link Filter} that should be post processed at runtime once the server response is obtained,
     * in order to match the actual {@code query}.
     * 
     * @param ds
     *            the data store issueing the request, may be needed to grab the feature type,
     *            feature type crs, or any other means the strategy needs
     * @param wfs
     *            the WFS protocol handler from which the strategy may need to grab some feature
     *            type metadata not available through the datastore interface, or even perform some
     *            test request.
     * @param query
     *            the GeoTools query to create the server request and post-processing filter for
     * @param outputFormat
     *            the output format indentifier that the request needs to be sent for. Shall be
     *            supported by the server for the requested feature type.
     * @return a handle to the request and post-processing filter appropriate to attend the given
     *         {@code query}
     * @throws IOException
     */
    public RequestComponents createGetFeatureRequest(WFS_1_1_0_DataStore ds, WFSProtocol wfs,
            Query query, String outputFormat) throws IOException;

    /**
     * Holds the components needed by the data store to issue and post process a GetFeature request.
     * 
     * @author Gabriel Roldan (OpenGeo)
     * @since 2.6
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
