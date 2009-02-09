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
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.Types;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
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
 * @version $Id: MappingFeatureIterator.java 32432 2009-02-09 04:07:41Z bencaradocdavies $
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

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> sourceFeatures;

    /**
     * Hold on to iterator to allow features to be streamed.
     */
    protected Iterator<SimpleFeature> sourceFeatureIterator;

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
    private SimpleFeature nextSrcFeature;

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

        FeatureSource<SimpleFeatureType, SimpleFeature> mappedSource = mapping.getSource();

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

        Object value = getValue(sourceExpression, source);
        if (attMapping.isNestedAttribute()) {
            // get built feature based on link value
            value = ((NestedAttributeMapping) attMapping).getFeature(value);
        }
        String id = null;
        if (Expression.NIL != attMapping.getIdentifierExpression()) {
            id = extractIdForAttribute(attMapping.getIdentifierExpression(), source);
        }
        Attribute instance = xpathAttributeBuilder.set(target, xpath, value, id, targetNodeType,
                attMapping.isMultiValued());
        Map<Name, Expression> clientPropsMappings = attMapping.getClientProperties();
        setClientProperties(instance, source, clientPropsMappings);
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
            SimpleFeature next = sourceFeatureIterator.next();
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
        // automatically group by mapped id
        Iterator<SimpleFeature> iterator = this.sourceFeatures.iterator();
        ArrayList<SimpleFeature> sources = new ArrayList<SimpleFeature>();
        while (iterator.hasNext()) {
            SimpleFeature next = iterator.next();
            if (extractIdForFeature(next).equals(id)) {
                sources.add(next);
            }
        }
        this.sourceFeatures.close(iterator);
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
            // extract the values from multiple simple features of the same id
            // and set them to one built feature
            for (SimpleFeature source : sources) {
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
