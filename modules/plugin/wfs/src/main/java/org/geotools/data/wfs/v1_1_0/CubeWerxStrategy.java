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
import java.util.ArrayList;
import java.util.List;

import net.opengis.wfs.GetFeatureType;

import org.geotools.data.wfs.protocol.wfs.GetFeature;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Or;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * An strategy object to deal in querying a CubeWerx WFS 1.1 server
 * <p>
 * This strategy was created as per the limitations encountered at the CubeWerx server being tested
 * while developing this plugin.
 * </p>
 * <p>
 * For instance, the following issues were found:
 * <ul>
 * <li>resultType parameter is not supported in GetFeature
 * <li>logically grouped spatial filters can't be handled
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: CubeWerxStrategy.java 31902 2008-11-22 00:37:35Z groldan $
 * @since 2.6
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data
 *         /wfs/v1_1_0/CubeWerxStrategy.java $
 */
public class CubeWerxStrategy extends DefaultWFSStrategy {

    /**
     * Addresses the following issues with the CubeWerx WFS server:
     * <p>
     * <ul>
     * <li>The request fails if the {@code resultType} parameter is set, either if the value is hits
     * or results, so it sets {@link GetFeatureType#setResultType(net.opengis.wfs.ResultTypeType)}
     * to {@code null}
     * 
     * <li>CubeWerx does not support filtering logical filters containing mixed geometry filters
     * (eg, AND(BBOX, Intersects)), no matter what the capabilities doc says
     * </ul>
     * </p>
     */
    @Override
    public RequestComponents createGetFeatureRequest(WFSProtocol wfs, GetFeature query)
            throws IOException {
        RequestComponents parts = super.createGetFeatureRequest(wfs, query);

        GetFeatureType serverRequest = parts.getServerRequest();

        // CubeWerx fails if the _mandatory_ resultType attribute is sent
        serverRequest.setResultType(null);

        return parts;
    }

    @Override
    public Filter[] splitFilters(final WFSProtocol wfs, final Filter queryFilter) {

        if (!(queryFilter instanceof BinaryLogicOperator)) {
            return super.splitFilters(wfs, queryFilter);
        }

        int spatialFiltersCount = 0;
        // if a logical operator, check no more than one geometry filter is enclosed on it
        List<Filter> children = ((BinaryLogicOperator) queryFilter).getChildren();
        for (Filter f : children) {
            if (f instanceof BinarySpatialOperator) {
                spatialFiltersCount++;
            }
        }
        if (spatialFiltersCount <= 1) {
            return super.splitFilters(wfs, queryFilter);
        }

        Filter serverFilter;
        Filter postFilter;
        if (queryFilter instanceof Or) {
            // can't know...
            serverFilter = Filter.INCLUDE;
            postFilter = queryFilter;
        } else {
            // its an And..
            List<Filter> serverChild = new ArrayList<Filter>();
            List<Filter> postChild = new ArrayList<Filter>();
            boolean spatialAdded = false;
            for (Filter f : children) {
                if (f instanceof BinarySpatialOperator) {
                    if (spatialAdded) {
                        postChild.add(f);
                    } else {
                        serverChild.add(f);
                        spatialAdded = true;
                    }
                } else {
                    serverChild.add(f);
                }
            }
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            serverFilter = ff.and(serverChild);
            postFilter = ff.and(postChild);
            SimplifyingFilterVisitor sfv = new SimplifyingFilterVisitor();
            serverFilter = (Filter) serverFilter.accept(sfv, null);
            postFilter = (Filter) postFilter.accept(sfv, null);
        }

        return new Filter[] { serverFilter, postFilter };
    }

}
