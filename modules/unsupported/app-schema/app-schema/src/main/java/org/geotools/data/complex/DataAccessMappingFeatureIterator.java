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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.Types;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FidFilterImpl;
import org.geotools.xlink.XLINK;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.Attributes;

/**
 * A Feature iterator that operates over the FeatureSource of a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} and produces Features of the output
 * schema by applying the mapping rules to the Features of the source schema.
 * <p>
 * This iterator acts like a one-to-one mapping, producing a Feature of the target type for each
 * feature of the source type.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * @author Rini Angreani, Curtin University of Technology
 * @author Russell Petty, GSV
 * @version $Id: DataAccessMappingFeatureIterator.java 34870 2010-02-05 11:05:15Z ang05a $
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/data/complex/DataAccessMappingFeatureIterator.java $
 * @since 2.4
 */
public class DataAccessMappingFeatureIterator extends AbstractMappingFeatureIterator {
    /**
     * Name representation of xlink:href
     */
    public static final Name XLINK_HREF_NAME = Types.toTypeName(XLINK.HREF);

    /**
     * Hold on to iterator to allow features to be streamed.
     */
    protected Iterator<Feature> sourceFeatureIterator;

    /**
     * Reprojected CRS from the source simple features, or null
     */
    private CoordinateReferenceSystem reprojection;

    /**
     * This is the feature that will be processed in next()
     */
    private Feature curSrcFeature;

    private FeatureSource<FeatureType, Feature> mappedSource;

    private FeatureCollection<FeatureType, Feature> sourceFeatures;

    /**
     * Filter that has that attributes from nested features as parameter. To be applied per feature
     * when computeNext() is called.
     */
    private Filter nestedAttributeFilter;

    /**
     * True if the query is filtered by attributes other than id, this is to cater for denormalised 
     * view where there can be multiple rows for 1 complex feature with different values. If the
     * filter is applied when the iterator is created, there's a chance some information is lost 
     * from rows from denormalised view not being incorporated into the target feature.
     */
    private boolean isFiltered;

    private ArrayList<String> filteredFeatures;

    /**
     * 
     * @param store
     * @param mapping
     *            place holder for the target type, the surrogate FeatureSource and the mappings
     *            between them.
     * @param query
     *            the query over the target feature type, that is to be unpacked to its equivalent
     *            over the surrogate feature type.
     * @throws IOException
     */
    public DataAccessMappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping,
            Query query) throws IOException {
        super(store, mapping, query);
    }

    public boolean hasNext() {
        if (isHasNextCalled()) {
            return curSrcFeature != null;
        }

        boolean exists = false;

        if ((curSrcFeature != null || sourceFeatureIterator != null)
                && featureCounter < maxFeatures) {
            boolean hasNextSrc = (curSrcFeature != null || sourceFeatureIterator.hasNext());

            while (hasNextSrc) {
                if (this.curSrcFeature == null) {
                    this.curSrcFeature = sourceFeatureIterator.next();
                }
                if (isFiltered
                        && filteredFeatures.contains(extractIdForFeature(this.curSrcFeature))) {
                    // get the next one as this row would've been already added to the target
                    // feature
                    // from setNextFilteredFeature
                    hasNextSrc = sourceFeatureIterator.hasNext();
                    curSrcFeature = null;
                } else if (nestedAttributeFilter != null
                        && !nestedAttributeFilter.evaluate(curSrcFeature)) {
                    // get the next one
                    hasNextSrc = sourceFeatureIterator.hasNext();
                    curSrcFeature = null;
                } else {
                    // either there's no filter or that it passed the filter, so return it
                    exists = true;
                    hasNextSrc = false;
                }
            }
        }

        if (!exists) {
            LOGGER.finest("no more features, produced " + featureCounter);
            close();
            curSrcFeature = null;
        }
        setHasNextCalled(true);
        return exists;
    }

    protected Iterator<Feature> getSourceFeatureIterator() {
        return sourceFeatureIterator;
    }

    protected boolean isSourceFeatureIteratorNull() {
        return getSourceFeatureIterator() == null;
    }

    protected void initialiseSourceFeatures(FeatureTypeMapping mapping, Query query)
            throws IOException {
        mappedSource = mapping.getSource();
        this.reprojection = query.getCoordinateSystemReproject();
        Filter filter = getAttributeFilter(query);
        if (filter != null) {
            isFiltered = true;
            filteredFeatures = new ArrayList<String>();
        }
        try {
            sourceFeatures = mappedSource.getFeatures(query);
            this.sourceFeatureIterator = sourceFeatures.iterator();
        } catch (IllegalArgumentException e) {
            // HACK HACK HACK
            // This is the only way we can check if the filter attributes are nested or not
            // since there's no expression getter method for every implementation of filters.
            // Remove the suspected filter from the query, and run it against each complex feature later
            // because JDBCFeatureSource cannot handle nested queries, since it translates
            // it to SQL first, then run the big query, but there's no way it can find the nested
            // feature type name from there.
            // Whereas with PropertyFeatureSource, it just runs the query first with no filters, then
            // check every row against the filter, which is what we're trying to do here.
            if (filter != null) {
                ((DefaultQuery) query).setFilter(Filter.INCLUDE);
                nestedAttributeFilter = filter;
                sourceFeatures = mappedSource.getFeatures(query);
                this.sourceFeatureIterator = sourceFeatures.iterator();
                return;
            }
            throw e;
        }
    }

    private Filter getAttributeFilter(Query query) {
        if (query instanceof DefaultQuery) {
            Filter filter = ((DefaultQuery) query).getFilter();
            if (filter != null && filter != Filter.INCLUDE && !(filter instanceof FidFilterImpl)) {
                return filter;
            }
        }
        return null;
    }

    protected boolean unprocessedFeatureExists() {

        boolean exists = sourceFeatureIterator.hasNext();
        if (exists && this.curSrcFeature == null) {
            this.curSrcFeature = sourceFeatureIterator.next();
        }

        return exists;
    }

    protected String extractIdForFeature() {
        return extractIdForFeature(curSrcFeature);
    }

    private String extractIdForFeature(Feature feature) {
        ComplexAttribute sourceInstance = (ComplexAttribute) feature;
        return (String) featureFidMapping.evaluate(sourceInstance, String.class);
    }

    protected String extractIdForAttribute(final Expression idExpression, Object sourceInstance) {
        String value = (String) idExpression.evaluate(sourceInstance, String.class);
        return value;
    }

    protected boolean isNextSourceFeatureNull() {
        return curSrcFeature == null;
    }

    protected boolean sourceFeatureIteratorHasNext() {
        return getSourceFeatureIterator().hasNext();
    }

    protected Object getValues(boolean isMultiValued, Expression expression,
            Object sourceFeatureInput) {
        if (isMultiValued && sourceFeatureInput instanceof FeatureImpl
                && expression instanceof AttributeExpressionImpl) {
            // RA: Feature Chaining
            // complex features can have multiple nodes of the same attribute.. and if they are used
            // as input to an app-schema data access to be nested inside another feature type of a
            // different XML type, it has to be mapped like this:
            // <AttributeMapping>
            // <targetAttribute>
            // gsml:composition
            // </targetAttribute>
            // <sourceExpression>
            // <inputAttribute>mo:composition</inputAttribute>
            // <linkElement>gsml:CompositionPart</linkElement>
            // <linkField>gml:name</linkField>
            // </sourceExpression>
            // <isMultiple>true</isMultiple>
            // </AttributeMapping>
            // As there can be multiple nodes of mo:composition in this case, we need to retrieve
            // all of them
            AttributeExpressionImpl attribExpression = ((AttributeExpressionImpl) expression);
            String xpath = attribExpression.getPropertyName();
            ComplexAttribute sourceFeature = (ComplexAttribute) sourceFeatureInput;
            StepList xpathSteps = XPath.steps(sourceFeature.getDescriptor(), xpath, namespaces);
            return getProperties(sourceFeature, xpathSteps);
        }
        return expression.evaluate(sourceFeatureInput);
    }

    /**
     * Sets the values of grouping attributes.
     * 
     * @param sourceFeature
     * @param groupingMappings
     * @param targetFeature
     * 
     * @return Feature. Target feature sets with simple attributes
     */
    protected void setAttributeValue(Feature target, final ComplexAttribute source,
            final AttributeMapping attMapping) throws IOException {

        final Expression sourceExpression = attMapping.getSourceExpression();
        final AttributeType targetNodeType = attMapping.getTargetNodeInstance();
        final StepList xpath = attMapping.getTargetXPath();
        Map<Name, Expression> clientPropsMappings = attMapping.getClientProperties();

        boolean isNestedFeature = attMapping.isNestedAttribute();
        Object value = getValues(attMapping.isMultiValued(), sourceExpression, source);
        boolean isHRefLink = isByReference(clientPropsMappings, isNestedFeature);
        if (isNestedFeature) {
            // get built feature based on link value
            if (value instanceof Collection) {
                ArrayList<Feature> nestedFeatures = new ArrayList<Feature>(((Collection) value)
                        .size());
                for (Object val : (Collection) value) {
                    if (val instanceof Attribute) {
                        val = ((Attribute) val).getValue();
                        if (val instanceof Collection) {
                            val = ((Collection) val).iterator().next();
                        }
                        while (val instanceof Attribute) {
                            val = ((Attribute) val).getValue();
                        }
                    }
                    if (isHRefLink) {
                        // get the input features to avoid infinite loop in case the nested
                        // feature type also have a reference back to this type
                        // eg. gsml:GeologicUnit/gsml:occurence/gsml:MappedFeature
                        // and gsml:MappedFeature/gsml:specification/gsml:GeologicUnit
                        nestedFeatures.addAll(((NestedAttributeMapping) attMapping)
                                .getInputFeatures(val, reprojection));
                    } else {
                        nestedFeatures.addAll(((NestedAttributeMapping) attMapping).getFeatures(
                                val, reprojection));
                    }
                }
                value = nestedFeatures;
            } else if (isHRefLink) {
                // get the input features to avoid infinite loop in case the nested
                // feature type also have a reference back to this type
                // eg. gsml:GeologicUnit/gsml:occurence/gsml:MappedFeature
                // and gsml:MappedFeature/gsml:specification/gsml:GeologicUnit
                value = ((NestedAttributeMapping) attMapping).getInputFeatures(value, reprojection);
            } else {
                value = ((NestedAttributeMapping) attMapping).getFeatures(value, reprojection);
            }
            if (isHRefLink) {
                // only need to set the href link value, not the nested feature properties
                setXlinkReference(target, clientPropsMappings, value, xpath, targetNodeType);
                return;
            }
        }
        String id = null;
        if (Expression.NIL != attMapping.getIdentifierExpression()) {
            id = extractIdForAttribute(attMapping.getIdentifierExpression(), source);
        }
        if (isNestedFeature) {
            assert (value instanceof Collection);
        }
        if (value instanceof Collection) {
            // nested feature type could have multiple instances as the whole purpose
            // of feature chaining is to cater for multi-valued properties
            Map<Object, Object> userData;
            Map<Name, Expression> valueProperties = new HashMap<Name, Expression>();
            for (Object singleVal : (Collection) value) {
                ArrayList valueList = new ArrayList();
                // copy client properties from input features if they're complex features
                // wrapped in app-schema data access
                if (singleVal instanceof Attribute) {
                    // copy client properties from input features if they're complex features
                    // wrapped in app-schema data access
                    valueProperties = getClientProperties((Attribute) singleVal);
                    if (!valueProperties.isEmpty()) {
                        valueProperties.putAll(clientPropsMappings);
                    }
                }
                if (!isNestedFeature) {
                    if (singleVal instanceof Attribute) {
                        singleVal = ((Attribute) singleVal).getValue();
                        if (singleVal instanceof Collection) {
                            valueList.addAll((Collection) singleVal);
                        } else {
                            valueList.add(singleVal);
                        }
                    }
                } else {
                    valueList.add(singleVal);
                }
                Attribute instance = xpathAttributeBuilder.set(target, xpath, valueList, id,
                        targetNodeType, false);
                setClientProperties(instance, source, valueProperties);
            }
        } else {
            if (value instanceof Attribute) {
                // copy client properties from input features if they're complex features
                // wrapped in app-schema data access
                Map<Name, Expression> newClientProps = getClientProperties((Attribute) value);
                if (!newClientProps.isEmpty()) {
                    newClientProps.putAll(clientPropsMappings);
                    clientPropsMappings = newClientProps;
                }
                value = ((Attribute) value).getValue();
            }
            Attribute instance = xpathAttributeBuilder.set(target, xpath, value, id,
                    targetNodeType, false);
            setClientProperties(instance, source, clientPropsMappings);
        }
    }

    /**
     * Set xlink:href client property for multi-valued chained features. This has to be specially
     * handled because we don't want to encode the nested features attributes, since it's already an
     * xLink. Also we need to eliminate duplicates.
     * 
     * @param target
     *            The target feature
     * @param clientPropsMappings
     *            Client properties mappings
     * @param value
     *            Nested features
     * @param xpath
     *            Attribute xPath where the client properties are to be set
     * @param targetNodeType
     *            Target node type
     */
    protected void setXlinkReference(Feature target, Map<Name, Expression> clientPropsMappings,
            Object value, StepList xpath, AttributeType targetNodeType) {
        // Make sure the same value isn't already set
        // in case it comes from a denormalized view for many-to-many relationship.
        // (1) Get the first existing value
        Property existingAttribute = getProperty(target, xpath);

        if (existingAttribute != null) {
            Object existingValue = existingAttribute.getUserData().get(Attributes.class);
            if (existingValue != null) {
                assert existingValue instanceof HashMap;
                existingValue = ((Map) existingValue).get(XLINK_HREF_NAME);
            }
            if (existingValue != null) {
                Expression linkExpression = clientPropsMappings.get(XLINK_HREF_NAME);
                for (Object singleVal : (Collection) value) {
                    assert singleVal instanceof Feature;
                    assert linkExpression != null;
                    Object hrefValue = linkExpression.evaluate(singleVal);
                    if (hrefValue != null && hrefValue.equals(existingValue)) {
                        // (2) if one of the new values matches the first existing value, 
                        // that means this comes from a denormalized view,
                        // and this set has already been set
                        return;
                    }
                }
            }
        }

        for (Object singleVal : (Collection) value) {
            assert singleVal instanceof Feature;
            Attribute instance = xpathAttributeBuilder.set(target, xpath, null, null,
                    targetNodeType, true);
            setClientProperties(instance, singleVal, clientPropsMappings);
        }
    }

    protected void setClientProperties(final Attribute target, final Object source,
            final Map<Name, Expression> clientProperties) {
        if (clientProperties.size() == 0) {
            return;
        }
        final Map<Name, Object> targetAttributes = new HashMap<Name, Object>();
        for (Map.Entry<Name, Expression> entry : clientProperties.entrySet()) {
            Name propName = entry.getKey();
            Object propExpr = entry.getValue();
            Object propValue;
            if (propExpr instanceof Expression) {
                propValue = getValue((Expression) propExpr, source);
            } else {
                propValue = propExpr;
            }
            targetAttributes.put(propName, propValue);
        }
        // FIXME should set a child Property
        target.getUserData().put(Attributes.class, targetAttributes);
    }

    private Map getClientProperties(Attribute attribute) throws DataSourceException {
        Map<Object, Object> userData = attribute.getUserData();
        Map clientProperties = new HashMap<Name, Expression>();
        if (userData != null && userData.containsKey(Attributes.class)) {
            Map props = (Map) userData.get(Attributes.class);
            if (!props.isEmpty()) {
                clientProperties.putAll(props);
            }
        }
        return clientProperties;
    }

    private void setNextFilteredFeature(String fId, ArrayList<Feature> features) throws IOException {
        FeatureCollection<FeatureType, Feature> matchingFeatures;
        FeatureId featureId = namespaceAwareFilterFactory.featureId(fId);
        DefaultQuery query = new DefaultQuery();
        if (reprojection != null) {
            query.setCoordinateSystemReproject(reprojection);
        }
        if (featureFidMapping instanceof PropertyName
                && ((PropertyName) featureFidMapping).getPropertyName().equals("@id")) {
            // no real feature id mapping,
            // so trying to find it when the filter's evaluated will result in exception
            Set<FeatureId> ids = new HashSet<FeatureId>();
            ids.add(featureId);
            query.setFilter(namespaceAwareFilterFactory.id(ids));
            matchingFeatures = this.mappedSource.getFeatures(query);
        } else {
            // in case the expression is wrapped in a function, eg. strConcat
            // that's why we don't always filter by id, but do a PropertyIsEqualTo
            query.setFilter(namespaceAwareFilterFactory.equals(
                    featureFidMapping, namespaceAwareFilterFactory.literal(featureId)));
            matchingFeatures = this.mappedSource.getFeatures(query);
        }

        Iterator<Feature> iterator = matchingFeatures.iterator();

        while (iterator.hasNext()) {
            features.add(iterator.next());
        }

        if (features.size() < 1) {
            LOGGER.warning("This shouldn't have happened."
                    + "There should be at least 1 features with id='" + fId + "'.");
        }
        filteredFeatures.add(fId);

        matchingFeatures.close(iterator);

        curSrcFeature = null;
    }

    private void setNextFeature(String fId, ArrayList<Feature> features) {
        features.add(curSrcFeature);

        curSrcFeature = null;

        while (sourceFeatureIterator.hasNext()) {
            Feature next = sourceFeatureIterator.next();
            // RA: apply filters that involve attributes from nested features here.
            // Because for JDBCFeatureSource, if the filter is included in the feature source query,
            // it will try to translate the filter to SQL form, which will fail because the
            // attributes may come from a different table (for nested feature type).
            if (extractIdForFeature(next).equals(fId)) {
                features.add(next);
            } else {
                curSrcFeature = next;
                break;
            }
        }
    }

    protected Feature computeNext() throws IOException {
        if (this.curSrcFeature == null) {
            LOGGER.warning("hasNext not called before calling next() in the iterator!");
        }

        ArrayList<Feature> sources = new ArrayList<Feature>();
        String id = extractIdForFeature(curSrcFeature);

        if (isFiltered) {
            setNextFilteredFeature(id, sources);
        } else {
            setNextFeature(id, sources);
        }

        if (sources.isEmpty()) {
            LOGGER.warning("No features found in next()."
                    + "This wouldn't have happenned if hasNext() was called beforehand.");
        }
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List<AttributeMapping> mappings = mapping.getAttributeMappings();

        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setDescriptor(targetNode);
        Feature target = (Feature) builder.build(id);

        for (AttributeMapping attMapping : mappings) {
            StepList targetXpathProperty = attMapping.getTargetXPath();
            if (targetXpathProperty.size() == 1) {
                Step rootStep = (Step) targetXpathProperty.get(0);
                QName stepName = rootStep.getName();
                if (Types.equals(targetNodeName, stepName)) {
                    // ignore the top level mapping for the Feature itself
                    // as it was already set
                    continue;
                }
            }
            // extract the values from multiple source features of the same id
            // and set them to one built feature
            if (attMapping.isMultiValued()) {
                for (Feature source : sources) {
                    setAttributeValue(target, source, attMapping);
                }
            } else {
                setAttributeValue(target, sources.get(0), attMapping);
            }
        }
        if (target.getDefaultGeometryProperty() == null) {
            setGeometry(target);
        }
        return target;
    }

    protected Feature populateFeatureData(String id) throws IOException {
        throw new UnsupportedOperationException("populateFeatureData should not be called!");
    }

    protected void closeSourceFeatures() {
        if (sourceFeatures != null && getSourceFeatureIterator() != null) {
            sourceFeatures.close(sourceFeatureIterator);
            sourceFeatureIterator = null;
            sourceFeatures = null;
            filteredFeatures = null;
        }
    }

    protected Object getValue(final Expression expression, Object sourceFeature) {
        Object value;
        value = expression.evaluate(sourceFeature);
        if (value instanceof Attribute) {
            value = ((Attribute) value).getValue();
        }
        return value;
    }

    /**
     * Returns first matching attribute from provided root and xPath.
     * 
     * @param root
     *            The root attribute to start searching from
     * @param xpath
     *            The xPath matching the attribute
     * @return The first matching attribute
     */
    private Property getProperty(ComplexAttribute root, StepList xpath) {
        Property property = root;

        final StepList steps = new StepList(xpath);

        Iterator<Step> stepsIterator = steps.iterator();

        while (stepsIterator.hasNext()) {
            assert property instanceof ComplexAttribute;
            Step step = stepsIterator.next();
            property = ((ComplexAttribute) property).getProperty(Types.toTypeName(step.getName()));
            if (property == null) {
                return null;
            }
        }
        return property;
    }

    /**
     * Return all matching properties from provided root attribute and xPath.
     * 
     * @param root
     *            The root attribute to start searching from
     * @param xpath
     *            The xPath matching the attribute
     * @return The matching attributes collection
     */
    private Collection<Property> getProperties(ComplexAttribute root, StepList xpath) {

        final StepList steps = new StepList(xpath);

        Iterator<Step> stepsIterator = steps.iterator();
        Collection<Property> properties = null;
        Step step = null;
        if (stepsIterator.hasNext()) {
            step = stepsIterator.next();
            properties = ((ComplexAttribute) root).getProperties(Types.toTypeName(step.getName()));
        }

        while (stepsIterator.hasNext()) {
            step = stepsIterator.next();
            Collection<Property> nestedProperties = new ArrayList<Property>();
            for (Property property : properties) {
                assert property instanceof ComplexAttribute;
                Collection<Property> tempProperties = ((ComplexAttribute) property)
                        .getProperties(Types.toTypeName(step.getName()));
                if (!tempProperties.isEmpty()) {
                    nestedProperties.addAll(tempProperties);
                }
            }
            properties.clear();
            if (nestedProperties.isEmpty()) {
                return properties;
            }
            properties.addAll(nestedProperties);
        }
        return properties;
    }

    /**
     * Checks if client property has xlink:ref in it, if the attribute is for chained features.
     * 
     * @param clientPropsMappings
     *            the client properties mappings
     * @param isNested
     *            true if we're dealing with chained/nested features
     * @return
     */
    protected boolean isByReference(Map<Name, Expression> clientPropsMappings, boolean isNested) {
        // only care for chained features
        return isNested ? (clientPropsMappings.isEmpty() ? false : (clientPropsMappings
                .get(XLINK_HREF_NAME) == null) ? false : true) : false;
    }

}
