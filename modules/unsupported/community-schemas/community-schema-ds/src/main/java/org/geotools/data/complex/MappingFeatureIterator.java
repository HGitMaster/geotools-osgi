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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.ComplexFeatureFactoryImpl;
import org.geotools.feature.Types;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A Feature iterator that operates over the FeatureSource of a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} and produces Features of the output
 * schema by applying the mapping rules to the Features of the source schema.
 * <p>
 * This iterator acts like a one-to-one mapping, producing a Feature of the target type for each
 * feature of the source type.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: MappingFeatureIterator.java 31741 2008-10-31 03:49:45Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/AbstractMappingFeatureIterator.java $
 * @since 2.4
 */
public class MappingFeatureIterator implements Iterator<Feature>, FeatureIterator<Feature> {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(MappingFeatureIterator.class.getPackage().getName());

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

    protected FeatureCollection features;

    protected Iterator sourceFeatures;

    protected ComplexDataStore store;

    protected FeatureSource featureSource;

    final protected XPath xpathAttributeBuilder;

    protected FilterFactory namespaceAwareFilterFactory;

    // this(store, mapping, query, new AttributeFactoryImpl());

    /**
     * maxFeatures restriction value as provided by query
     */
    private final int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    private int featureCounter;

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
    public MappingFeatureIterator(ComplexDataStore store, FeatureTypeMapping mapping, Query query)
            throws IOException {
        this.store = store;
        this.attf = new ComplexFeatureFactoryImpl();
        Name name = mapping.getTargetFeature().getName();
        this.featureSource = store.getFeatureSource(name);

        List attributeMappings = mapping.getAttributeMappings();

        for (Iterator it = attributeMappings.iterator(); it.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) it.next();
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

        FeatureSource mappedSource = mapping.getSource();

        features = (FeatureCollection) mappedSource.getFeatures(filter);

        this.sourceFeatures = features.iterator();

        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(attf);
        NamespaceSupport namespaces = mapping.getNamespaces();
        namespaceAwareFilterFactory = new FilterFactoryImplNamespaceAware(namespaces);
        xpathAttributeBuilder.setFilterFactory(namespaceAwareFilterFactory);
        this.maxFeatures = query.getMaxFeatures();

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
        if (features != null && sourceFeatures != null) {
            features.close(sourceFeatures);
            sourceFeatures = null;
            features = null;
        }
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
    protected void setSingleValuedAttribute(final Feature target, final ComplexAttribute source,
            final AttributeMapping attMapping) throws IOException {

        final Expression sourceExpression = attMapping.getSourceExpression();
        final AttributeType targetNodeType = attMapping.getTargetNodeInstance();
        final StepList xpath = attMapping.getTargetXPath();

        Object value = getValue(sourceExpression, source);

        String id = null;
        if (Expression.NIL != attMapping.getIdentifierExpression()) {
            id = extractIdForAttribute(attMapping.getIdentifierExpression(), source);
        }
        Attribute instance = xpathAttributeBuilder.set(target, xpath, value, id, targetNodeType);
        Map clientPropsMappings = attMapping.getClientProperties();
        setClientProperties(instance, source, clientPropsMappings);
    }

    private void setClientProperties(final Attribute target, final Object source,
            final Map clientProperties) {
        if (clientProperties.size() == 0) {
            return;
        }
        final Map targetAttributes = new HashMap();
        for (Iterator it = clientProperties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            org.opengis.feature.type.Name propName = (org.opengis.feature.type.Name) entry.getKey();
            Expression propExpr = (Expression) entry.getValue();
            Object propValue = getValue(propExpr, source);
            targetAttributes.put(propName, propValue);
        }
        target.getUserData().put(Attributes.class, targetAttributes);
    }

    public Feature next() {
        try {
            return computeNext();
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext() {
        return featureCounter < maxFeatures && sourceFeatures != null && sourceFeatures.hasNext();
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
        ComplexAttribute sourceInstance = (ComplexAttribute) sourceFeatures.next();
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List mappings = mapping.getAttributeMappings();
        String id = extractIdForFeature(sourceInstance);
        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setDescriptor(targetNode);
        Feature target = (Feature) builder.build(id);
        for (Iterator itr = mappings.iterator(); itr.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) itr.next();
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
            setSingleValuedAttribute(target, sourceInstance, attMapping);
        }
        featureCounter++;
        if (!hasNext()) {
            close();
        }
        return target;
    }

}
