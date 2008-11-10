package org.geotools.data.wfs.v1_1_0;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;

import org.geotools.data.Query;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Or;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: CubeWerxStrategy.java 31817 2008-11-10 22:21:18Z groldan $
 * @since 1.6
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
    public RequestComponents createGetFeatureRequest(WFS_1_1_0_DataStore ds, WFSProtocol wfs,
            Query query, String outputFormat) throws IOException {
        RequestComponents parts = super.createGetFeatureRequest(ds, wfs, query, outputFormat);

        GetFeatureType serverRequest = parts.getServerRequest();

        // CubeWerx fails if the _mandatory_ resultType attribute is sent
        serverRequest.setResultType(null);

        return parts;
    }

    @Override
    protected Filter[] splitFilters(final WFS_1_1_0_DataStore ds, final WFSProtocol wfs,
            final Filter queryFilter) {

        if (!(queryFilter instanceof BinaryLogicOperator)) {
            return super.splitFilters(ds, wfs, queryFilter);
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
            return super.splitFilters(ds, wfs, queryFilter);
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
