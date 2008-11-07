/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.DataAccess;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.ServiceInfo;
import org.geotools.data.complex.filter.UnmappingFilterVisitor;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Types;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: AppSchemaDataAccess.java 31784 2008-11-06 06:20:21Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/ComplexDataStore.java $
 * @since 2.4
 */
public class AppSchemaDataAccess implements DataAccess<FeatureType, Feature> {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(AppSchemaDataAccess.class.getPackage().getName());

    private Map<Name, FeatureTypeMapping> mappings = Collections.emptyMap();

    private FilterFactory filterFac = CommonFactoryFinder.getFilterFactory(null);

    /**
     * 
     * @param mappings
     *                a Set containing a {@linkplain FeatureTypeMapping} for each FeatureType this
     *                DataStore is going to hold.
     */
    public AppSchemaDataAccess(Set<FeatureTypeMapping> mappings) {
        this.mappings = new HashMap<Name, FeatureTypeMapping>();
        for (FeatureTypeMapping mapping : mappings) {
            Name mappedElement = mapping.getTargetFeature().getName();
            this.mappings.put(mappedElement, mapping);
        }
    }

    /**
     * Returns the set of target type names this DataStore holds, where the term 'target type name'
     * refers to the name of one of the types this datastore produces by mapping another ones
     * through the definitions stored in its {@linkplain FeatureTypeMapping}s
     */
    public Name[] getTypeNames() throws IOException {
        Name[] typeNames = new Name[mappings.size()];
        this.mappings.keySet().toArray(typeNames);
        return typeNames;
    }

    /**
     * Finds the target FeatureType named <code>typeName</code> in this ComplexDatastore's
     * internal list of FeatureType mappings and returns it.
     */
    public FeatureType getSchema(Name typeName) throws IOException {
        return (FeatureType) getMapping(typeName).getTargetFeature().getType();
    }

    /**
     * Returns the mapping suite for the given target type name.
     * 
     * <p>
     * Note this method is public just for unit testing pourposes
     * </p>
     * 
     * @param typeName
     * @return
     * @throws IOException
     */
    public FeatureTypeMapping getMapping(Name typeName) throws IOException {
        FeatureTypeMapping mapping = (FeatureTypeMapping) this.mappings.get(typeName);
        if (mapping == null) {
            StringBuffer availables = new StringBuffer("[");
            for (Iterator it = mappings.keySet().iterator(); it.hasNext();) {
                availables.append(it.next());
                availables.append(it.hasNext() ? ", " : "");
            }
            availables.append("]");
            throw new DataSourceException(typeName + " not found " + availables);
        }
        return mapping;
    }

    /**
     * Computes the bounds of the features for the specified feature type that satisfy the query
     * provided that there is a fast way to get that result.
     * <p>
     * Will return null if there is not fast way to compute the bounds. Since it's based on some
     * kind of header/cached information, it's not guaranteed to be real bound of the features
     * </p>
     * 
     * @param query
     * @return the bounds, or null if too expensive
     * @throws SchemaNotFoundException
     * @throws IOException
     */
    protected ReferencedEnvelope getBounds(Query query) throws IOException {
        FeatureTypeMapping mapping = getMapping(getName(query));
        Query unmappedQuery = unrollQuery(query, mapping);
        FeatureSource mappedSource = mapping.getSource();
        return mappedSource.getBounds(unmappedQuery);
    }

    /**
     * Gets the number of the features that would be returned by this query for the specified
     * feature type.
     * <p>
     * If getBounds(Query) returns <code>-1</code> due to expense consider using
     * <code>getFeatures(Query).getCount()</code> as a an alternative.
     * </p>
     * 
     * @param targetQuery
     *                Contains the Filter and MaxFeatures to find the bounds for.
     * @return The number of Features provided by the Query or <code>-1</code> if count is too
     *         expensive to calculate or any errors or occur.
     * @throws IOException
     * 
     * @throws IOException
     *                 if there are errors getting the count
     */
    protected int getCount(final Query targetQuery) throws IOException {
        final String typeName = targetQuery.getTypeName();
        final FeatureTypeMapping mapping = getMapping(getName(targetQuery));
        final FeatureSource mappedSource = mapping.getSource();

        Query unmappedQuery = unrollQuery(targetQuery, mapping);

        ((DefaultQuery) unmappedQuery).setMaxFeatures(targetQuery.getMaxFeatures());
        return mappedSource.getCount(unmappedQuery);
    }

    private Name getName(Query query) {
        return Types.typeName(query.getNamespace().toString(), query.getTypeName());
    }

    /**
     * Returns <code>Filter.INCLUDE</code>, as the whole filter is unrolled and passed back to
     * the underlying DataStore to be treated.
     * 
     * @return <code>Filter.INLCUDE</code>
     */
    protected Filter getUnsupportedFilter(String typeName, Filter filter) {
        return Filter.INCLUDE;
    }

    /**
     * Creates a <code>org.geotools.data.Query</code> that operates over the surrogate DataStore,
     * by unrolling the <code>org.geotools.filter.Filter</code> contained in the passed
     * <code>query</code>, and replacing the list of required attributes by the ones of the
     * mapped FeatureType.
     * 
     * @param query
     * @param mapping
     * @return
     */
    public Query unrollQuery(Query query, FeatureTypeMapping mapping) {
        Query unrolledQuery = Query.ALL;
        FeatureSource source = mapping.getSource();

        if (!Query.ALL.equals(query)) {
            Filter complexFilter = query.getFilter();
            Filter unrolledFilter = AppSchemaDataAccess.unrollFilter(complexFilter, mapping);

            List propNames = getSurrogatePropertyNames(query.getPropertyNames(), mapping);

            DefaultQuery newQuery = new DefaultQuery();

            String name = source.getName().getLocalPart();
            newQuery.setTypeName(name);
            newQuery.setFilter(unrolledFilter);
            newQuery.setPropertyNames(propNames);
            newQuery.setCoordinateSystem(query.getCoordinateSystem());
            newQuery.setCoordinateSystemReproject(query.getCoordinateSystemReproject());
            newQuery.setHandle(query.getHandle());
            newQuery.setMaxFeatures(query.getMaxFeatures());

            unrolledQuery = newQuery;
        }
        return unrolledQuery;
    }

    /**
     * 
     * @param mappingProperties
     * @param mapping
     * @return <code>null</code> if all surrogate attributes shall be queried, else the list of
     *         needed surrogate attributes to satisfy the mapping of prorperties in
     *         <code>mappingProperties</code>
     */
    private List getSurrogatePropertyNames(String[] mappingProperties, FeatureTypeMapping mapping) {
        List propNames = null;

        final AttributeDescriptor targetDescriptor = mapping.getTargetFeature();
        final FeatureType mappedType = (FeatureType) targetDescriptor.getType();

        if (mappingProperties != null && mappingProperties.length > 0) {
            Set requestedSurrogateProperties = new HashSet();

            // add all surrogate attributes involved in mapping of the requested
            // target schema attributes
            List attMappings = mapping.getAttributeMappings();
            List/* <String> */requestedProperties = Arrays.asList(mappingProperties);

            for (Iterator itr = requestedProperties.iterator(); itr.hasNext();) {
                String requestedPropertyXPath = (String) itr.next();
                StepList requestedPropertySteps;
                NamespaceSupport namespaces = mapping.getNamespaces();
                requestedPropertySteps = XPath.steps(targetDescriptor, requestedPropertyXPath,
                        namespaces);

                for (Iterator aitr = attMappings.iterator(); aitr.hasNext();) {
                    final AttributeMapping entry = (AttributeMapping) aitr.next();
                    final StepList targetSteps = entry.getTargetXPath();
                    final Expression sourceExpression = entry.getSourceExpression();
                    final Expression idExpression = entry.getIdentifierExpression();

                    // i.e.: requested "measurement", found mapping of
                    // "measurement/result".
                    // "result" must be included to create "measurement"
                    if (targetSteps.containsAll(requestedPropertySteps)) {
                        FilterAttributeExtractor extractor = new FilterAttributeExtractor();
                        sourceExpression.accept(extractor, null);
                        idExpression.accept(extractor, null);

                        Set exprAtts = extractor.getAttributeNameSet();

                        for (Iterator eitr = exprAtts.iterator(); eitr.hasNext();) {
                            String mappedAtt = (String) eitr.next();
                            PropertyName propExpr = filterFac.property(mappedAtt);
                            Object object = propExpr.evaluate(mappedType);
                            AttributeDescriptor mappedAttribute = (AttributeDescriptor) object;

                            if (mappedAttribute != null) {
                                requestedSurrogateProperties.add(mappedAtt);
                            } else {
                                LOGGER.info("mapped type does not contains property " + mappedAtt);
                            }
                        }
                        LOGGER.fine("adding atts needed for : " + exprAtts);
                    }
                }
            }
            propNames = new ArrayList(requestedSurrogateProperties);
        }
        return propNames;
    }

    /**
     * Takes a filter that operates against a {@linkplain FeatureTypeMapping}'s target FeatureType,
     * and unrolls it creating a new Filter that operates against the mapping's source FeatureType.
     * 
     * @param complexFilter
     * @return TODO: implement filter unrolling
     */
    public static Filter unrollFilter(Filter complexFilter, FeatureTypeMapping mapping) {
        UnmappingFilterVisitor visitor = new UnmappingFilterVisitor(mapping);
        Filter unrolledFilter = (Filter) complexFilter.accept(visitor, null);
        return unrolledFilter;
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public ServiceInfo getInfo() {
        throw new UnsupportedOperationException();
    }

    public List getNames() {
        List names = new LinkedList();
        for (Iterator it = mappings.values().iterator(); it.hasNext();) {
            FeatureTypeMapping mapping = (FeatureTypeMapping) it.next();
            Name name = mapping.getTargetFeature().getName();
            names.add(name);
        }
        return names;
    }

    public void createSchema(FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException();
    }

    public FeatureSource<FeatureType, Feature> getFeatureSource(Name typeName) throws IOException {
        // TODO Auto-generated method stub
        return new MappingFeatureSource(this, getMapping(typeName));
    }

    public void updateSchema(Name typeName, FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException();
    }

}
