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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AppSchemaFeatureFactoryImpl;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.Types;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Geometry;

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
 * @version $Id: MappingFeatureIterator.java 32923 2009-05-04 02:13:34Z ang05a $
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/MappingFeatureIterator.java $
 * @since 2.4
 */
public class MappingFeatureIterator implements Iterator<Feature>, FeatureIterator<Feature> {

    /**
     * The mappings for the source and target schemas
     */
    protected FeatureTypeMapping mapping;

    /**
     * Expression to evaluate the feature id
     */
    protected Expression featureFidMapping;

    /**
     * Factory used to create the target feature and attributes
     */
    protected FeatureFactory attf;

    protected FeatureCollection<FeatureType, Feature> sourceFeatures;

    private FeatureSource<FeatureType, Feature> mappedSource;

    /**
     * Hold on to iterator to allow features to be streamed.
     */
    protected Iterator<Feature> sourceFeatureIterator;

    protected AppSchemaDataAccess store;

    final protected XPath xpathAttributeBuilder;

    protected FilterFactory namespaceAwareFilterFactory;

    /**
     * maxFeatures restriction value as provided by query
     */
    private final int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    private int featureCounter;

    /**
     * Map of processed features by mapped id, so multiple instances are regarded as the same
     * feature
     */
    private HashMap<String, Feature> processedFeatures;

    /**
     * Next feature that doesn't already exist in processedFeatures map
     */
    private Feature nextSrcFeature;

    /**
     * True if hasNext has been called prior to calling next()
     */
    private boolean hasNextCalled;

    /**
     * 
     * @param store
     * @param mapping
     *                place holder for the target type, the surrogate FeatureSource and the mappings
     *                between them.
     * @param query
     *                the query over the target feature type, that is to be unpacked to its
     *                equivalent over the surrogate feature type.
     * @throws IOException
     */
    public MappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping, Query query)
            throws IOException {
        this.store = store;
        this.attf = new AppSchemaFeatureFactoryImpl();
        Name name = mapping.getTargetFeature().getName();

        List<AttributeMapping> attributeMappings = mapping.getAttributeMappings();

        for (AttributeMapping attMapping : attributeMappings) {
            StepList targetXPath = attMapping.getTargetXPath();
            if (targetXPath.size() > 1) {
                continue;
            }
            Step step = (Step) targetXPath.get(0);
            QName stepName = step.getName();
            if (Types.equals(name, stepName)) {
                featureFidMapping = attMapping.getIdentifierExpression();
                break;
            }
        }

        this.mapping = mapping;

        if (featureFidMapping == null || Expression.NIL.equals(featureFidMapping)) {
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            featureFidMapping = ff.property("@id");
        }

        Query unrolledQuery = getUnrolledQuery(query);
        Filter filter = unrolledQuery.getFilter();

        mappedSource = mapping.getSource();

        sourceFeatures = mappedSource.getFeatures(filter);

        this.sourceFeatureIterator = sourceFeatures.iterator();

        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(attf);
        NamespaceSupport namespaces = mapping.getNamespaces();
        namespaceAwareFilterFactory = new FilterFactoryImplNamespaceAware(namespaces);
        xpathAttributeBuilder.setFilterFactory(namespaceAwareFilterFactory);
        this.maxFeatures = query.getMaxFeatures();
        this.processedFeatures = new HashMap<String, Feature>();

    }

    /**
     * Shall not be called, just throws an UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes the underlying FeatureIterator
     */
    public void close() {
        if (sourceFeatures != null && sourceFeatureIterator != null) {
            sourceFeatures.close(sourceFeatureIterator);
            sourceFeatureIterator = null;
            sourceFeatures = null;
        }
        processedFeatures.clear();
    }

    /**
     * Based on the set of xpath expression/id extracting expression, finds the ID for the attribute
     * <code>attributeXPath</code> from the source complex attribute.
     * 
     * @param attributeXPath
     *                the location path of the attribute to be created, for which to obtain the id
     *                by evaluating the corresponding <code>org.geotools.filter.Expression</code>
     *                from <code>sourceInstance</code>.
     * @param sourceInstance
     *                a complex attribute which is the source of the mapping.
     * @return the ID to be applied to a new attribute instance addressed by
     *         <code>attributeXPath</code>, or <code>null</code> if there is no an id mapping
     *         for that attribute.
     */
    protected String extractIdForAttribute(final Expression idExpression,
            ComplexAttribute sourceInstance) {
        String value = (String) idExpression.evaluate(sourceInstance, String.class);
        return value;
    }

    protected String extractIdForFeature(ComplexAttribute sourceInstance) {
        String fid = (String) featureFidMapping.evaluate(sourceInstance, String.class);
        return fid;
    }

    protected Object getValue(Expression expression, Object sourceFeature) {
        Object value;
        value = expression.evaluate(sourceFeature);
        if (value instanceof Attribute) {
            value = ((Attribute) value).getValue();
        }
        return value;
    }

    protected Object getValues(Expression expression, Object sourceFeature, boolean isNestedFeature) {
        if (sourceFeature instanceof FeatureImpl && isNestedFeature) {
            // RA: Feature Chaining HACK
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
            // all
            // of them.. modifying FeaturePropertyAccessorFactory.get() to use
            // JXPathContext.iterate()
            // instead of JXPathContext.getValue() to get all matching nodes returns the same node
            // multiple times.
            // Even successfully, it could result in getting the children nodes that we don't want,
            // eg. mo:form/.../.../mo:form/....
            assert expression instanceof AttributeExpressionImpl;
            AttributeExpressionImpl attribExpression = ((AttributeExpressionImpl) expression);
            ArrayList valueList = new ArrayList();
            String xpath = attribExpression.getPropertyName();
            if (xpath.endsWith("]")) {
                // get a particularly indexed path
                return getValue(expression, sourceFeature);
            }
            Object value = getValue(expression, sourceFeature);
            // starts with 2, since the first would've been returned above
            int i = 2;
            while (value != null) {
                if (value instanceof Collection) {
                    valueList.addAll((Collection) value);
                } else {
                    valueList.add(value);
                }
                attribExpression.setPropertyName(xpath + "[" + i + "]");
                try {
                    value = getValue(attribExpression, sourceFeature);
                } finally {
                    // there's no clone method and there's no getter for hints
                    // so use original attributeExpression and set the value back
                    // to original after use
                    attribExpression.setPropertyName(xpath);
                }
                i++;
            }
            return valueList;
        }
        return getValue(expression, sourceFeature);
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

        boolean isNestedFeature = attMapping.isNestedAttribute();
        Object value = getValues(sourceExpression, source, isNestedFeature);
        if (isNestedFeature) {
            // get built feature based on link value
            if (value instanceof Collection) {
                ArrayList<Feature> nestedFeatures = new ArrayList<Feature>(((Collection) value)
                        .size());
                for (Object val : (Collection) value) {
                    while (val instanceof Attribute) {
                        val = ((Attribute) val).getValue();
                    }
                    nestedFeatures.addAll(((NestedAttributeMapping) attMapping).getFeatures(val));
                }
                value = nestedFeatures;
            } else {
                value = ((NestedAttributeMapping) attMapping).getFeatures(value);
            }
        }
        String id = null;
        if (Expression.NIL != attMapping.getIdentifierExpression()) {
            id = extractIdForAttribute(attMapping.getIdentifierExpression(), source);
        }
        if (isNestedFeature) {
            assert (value instanceof Collection);
            // nested feature type could have multiple instances as the whole purpose
            // of feature chaining is to cater for multi-valued properties
            for (Object singleVal : (Collection) value) {
                ArrayList<Feature> valueList = new ArrayList<Feature>();
                valueList.add((Feature) singleVal);
                Attribute instance = xpathAttributeBuilder.set(target, xpath, valueList, id,
                        targetNodeType);
                Map<Name, Expression> clientPropsMappings = attMapping.getClientProperties();
                setClientProperties(instance, source, clientPropsMappings);
            }
        } else {
            Attribute instance = xpathAttributeBuilder
                    .set(target, xpath, value, id, targetNodeType);
            Map<Name, Expression> clientPropsMappings = attMapping.getClientProperties();
            setClientProperties(instance, source, clientPropsMappings);
        }
    }

    private void setClientProperties(final Attribute target, final Object source,
            final Map<Name, Expression> clientProperties) {
        if (clientProperties.size() == 0) {
            return;
        }
        final Map<Name, Object> targetAttributes = new HashMap<Name, Object>();
        for (Map.Entry<Name, Expression> entry : clientProperties.entrySet()) {
            Name propName = entry.getKey();
            Expression propExpr = entry.getValue();
            Object propValue = getValue(propExpr, source);
            targetAttributes.put(propName, propValue);
        }
        // FIXME should set a child Property
        target.getUserData().put(Attributes.class, targetAttributes);
    }

    /**
     * Return next feature.
     * 
     * @see java.util.Iterator#next()
     */
    public Feature next() {
        try {
            return computeNext();
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    /**
     * Return true if there are more features.
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        hasNextCalled = true;
        if (featureCounter >= maxFeatures) {
            return false;
        }
        if (sourceFeatureIterator == null) {
            return false;
        }
        // make sure features are unique by mapped id
        while (sourceFeatureIterator.hasNext()) {
            Feature next = sourceFeatureIterator.next();
            if (!processedFeatures.containsKey(extractIdForFeature(next))) {
                nextSrcFeature = next;
                return true;
            }
        }
        // no more features.. close the source
        close();
        return false;
    }

    /**
     * Return a query appropriate to its underlying feature source.
     * 
     * @param query
     *                the original query against the output schema
     * @return a query appropriate to be executed over the underlying feature source.
     */
    protected Query getUnrolledQuery(Query query) {
        return store.unrollQuery(query, mapping);
    }

    private Feature computeNext() throws IOException {
        if (!hasNextCalled) {
            // hasNext needs to be called to set nextSrcFeature
            if (!hasNext()) {
                return null;
            }
        }
        hasNextCalled = false;
        if (nextSrcFeature == null) {
            throw new UnsupportedOperationException("No more features produced!");
        }
        ComplexAttribute sourceInstance = (ComplexAttribute) nextSrcFeature;
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List<AttributeMapping> mappings = mapping.getAttributeMappings();
        String id = extractIdForFeature(sourceInstance);
        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setDescriptor(targetNode);
        Feature target = (Feature) builder.build(id);
        // Run another query to find same features, in case they're from a denormalized view
        // ie. having many to many relationship.
        // This is so we can encode the same features as one complex feature.
        // FIXME: Perhaps this can be optimized in the future.. other options:
        // - enforce an "ORDER BY" rule in the denormalized view, so everything is ordered,
        // so we can just keep calling next until the next one is different.
        // - use "sortBy" when running the main query, but not possible at the moment..
        // - store these features in a list.. but not a good memory management, especially if
        // the features could be deeply nested, ie. storing numerous features per iterator
        ArrayList<Feature> sources = new ArrayList<Feature>();

        FeatureCollection<FeatureType, Feature> matchingFeatures = this.mappedSource
                .getFeatures(namespaceAwareFilterFactory.equals(this.featureFidMapping,
                        namespaceAwareFilterFactory.literal(target.getIdentifier())));

        Iterator<Feature> iterator = matchingFeatures.iterator();

        while (iterator.hasNext()) {
            sources.add(iterator.next());
        }

        matchingFeatures.close(iterator);

        assert sources.size() >= 1; // there should be at least the current feature

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
            for (Feature source : sources) {
                setAttributeValue(target, source, attMapping);
            }
        }
        // hasNext() should already ensure that this shouldn't happen
        assert !processedFeatures.containsKey(id);
        featureCounter++;
        if (target.getDefaultGeometryProperty() == null) {
            setGeometry(target);
        }
        processedFeatures.put(id, target);
        if (!sourceFeatureIterator.hasNext()) {
            close();
        }
        return target;
    }

    /**
     * Set the feature geometry to that of the first property bound to a JTS geometry
     * 
     * @param feature
     */
    private void setGeometry(Feature feature) {
        // FIXME an ugly, ugly hack to smuggle a geometry into a feature
        // FeatureImpl.getBounds and GMLSchema do not work together
        for (final Property property : feature.getProperties()) {
            if (Geometry.class.isAssignableFrom(property.getType().getBinding())) {
                // need to manufacture a GeometryDescriptor so we can make a GeometryAttribute
                // in which we can store the Geometry
                AttributeType type = (AttributeType) property.getType();
                GeometryType geometryType = new GeometryTypeImpl(type.getName(), type.getBinding(),
                        null, type.isIdentified(), type.isAbstract(), type.getRestrictions(), type
                                .getSuper(), type.getDescription());
                AttributeDescriptor descriptor = (AttributeDescriptor) property.getDescriptor();
                GeometryDescriptor geometryDescriptor = new GeometryDescriptorImpl(geometryType,
                        descriptor.getName(), descriptor.getMinOccurs(), descriptor.getMaxOccurs(),
                        property.isNillable(), null);
                GeometryAttribute geometryAttribute = new GeometryAttributeImpl(
                        property.getValue(), geometryDescriptor, null);
                List<Property> properties = new ArrayList<Property>(feature.getProperties());
                properties.remove(property);
                properties.add(geometryAttribute);
                feature.setValue(properties);
                feature.setDefaultGeometryProperty(geometryAttribute);
                break;
            }
        }
    }
}
