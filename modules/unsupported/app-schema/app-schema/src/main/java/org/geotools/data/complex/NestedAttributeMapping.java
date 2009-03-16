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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;

/**
 * This class represents AttributeMapping for attributes that are nested inside another complex
 * attribute. The nested attributes would be features, or fake features, ie. complex attributes
 * which types are wrapped with NonFeatureTypeProxy instances. The purpose of this class is to store
 * nested built features so they can be retrieved when the parent feature is being built. Simple
 * features are also stored for caching if a filter involving these nested features is run.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class NestedAttributeMapping extends AttributeMapping {
    /**
     * Nested features mapped id expression
     */
    private Expression nestedIdExpression;

    /**
     * Input feature source of the nested features
     */
    private FeatureSource<FeatureType, Feature> source;

    /**
     * Mapped feature source of the nested features
     */
    private FeatureSource<FeatureType, Feature> mappingSource;

    /**
     * Name of the nested features element
     */
    private final Name nestedFeatureType;

    /**
     * Target xpath that links to nested features
     */
    private final StepList nestedTargetXPath;

    /**
     * Source expression of the nested features
     */
    private Expression nestedExpression;

    /**
     * Stored simple features
     */
    private ArrayList<Feature> simpleFeatures;

    /**
     * Stored built features
     */
    private ArrayList<Feature> features;

    /**
     * Parent source features
     */
    private ArrayList<Feature> parentFeatures;

    /**
     * Filter factory
     */
    private FilterFactory filterFac = CommonFactoryFinder.getFilterFactory(null);

    /**
     * Sole constructor
     * 
     * @param idExpression
     * @param parentExpression
     * @param targetXPath
     * @param targetNodeInstance
     * @param isMultiValued
     * @param clientProperties
     * @param sourceElement
     *            parent feature element type
     * @param sourcePath
     *            XPath link to nested feature
     * @param parentSource
     *            parent feature source
     * @throws IOException
     */
    public NestedAttributeMapping(Expression idExpression, Expression parentExpression,
            StepList targetXPath, boolean isMultiValued, Map<Name, Expression> clientProperties,
            Name sourceElement, StepList sourcePath,
            FeatureSource<FeatureType, Feature> parentSource) throws IOException {
        super(idExpression, parentExpression, targetXPath, null, isMultiValued, clientProperties);
        this.nestedTargetXPath = sourcePath;
        this.nestedFeatureType = sourceElement;
        // get all parent source features
        parentFeatures = new ArrayList<Feature>();
        DefaultQuery parentQuery = new DefaultQuery();
        parentQuery.setPropertyNames(new String[] { parentExpression.toString() });
        FeatureCollection<FeatureType, Feature> parentFeatureCollection = parentSource
                .getFeatures(parentQuery);
        Iterator<Feature> iterator = parentFeatureCollection.iterator();
        while (iterator.hasNext()) {
            parentFeatures.add(iterator.next());
        }
        parentFeatureCollection.close(iterator);
    }

    @Override
    /*
     * @see org.geotools.data.complex.AttributeMapping#isNestedAttribute()
     */
    public boolean isNestedAttribute() {
        return true;
    }

    /**
     * Return an iterator of the simple features that this attribute mapping represents. If this is
     * the first time this is called, it would initiate the simple features list. This has to be
     * done after the feature type mapping is complete.
     * 
     * @return simple feature iterator
     * @throws IOException
     */
    private synchronized Iterator<Feature> simpleIterator() throws IOException {
        if (simpleFeatures == null) {
            // We can't initiate this in the constructor because the feature type mapping
            // might not be built yet.
            simpleFeatures = new ArrayList<Feature>();
            FeatureTypeMapping featureTypeMapping = AppSchemaDataAccessRegistry
                    .getMapping(nestedFeatureType);
            assert featureTypeMapping != null;

            source = featureTypeMapping.getSource();
            assert source != null;

            // find source expression on nested features side
            AttributeMapping mapping = featureTypeMapping
                    .getAttributeMapping(this.nestedTargetXPath);
            assert mapping != null;
            nestedExpression = mapping.getSourceExpression();

            // get all the nested values from all parent features so we only have to run
            // this big query once, rather than running multiple simple ones
            List<Filter> filters = new ArrayList<Filter>();
            // HashSet is used to make sure the values are unique, no repeats
            Set<Object> values = new HashSet<Object>();
            for (Feature parentFeature : parentFeatures) {
                Object value = this.getSourceExpression().evaluate(parentFeature);
                if (value instanceof Attribute) {
                    value = ((Attribute) value).getValue();
                }
                if (value instanceof Collection) {
                    for (Object val : (Collection) value) {
                        values.add(val);
                    }
                } else {
                    values.add(value);
                }
            }

            for (Object value : values) {
                filters.add(filterFac.equals(this.nestedExpression, filterFac.literal(value)));
            }

            Filter filter = filterFac.or(filters);
            // get all the nested features based on the link values
            FeatureCollection<FeatureType, Feature> fCollection = source.getFeatures(filter);
            Iterator<Feature> iterator = fCollection.iterator();
            while (iterator.hasNext()) {
                simpleFeatures.add(iterator.next());
            }
            fCollection.close(iterator);
        }
        return simpleFeatures.iterator();
    }

    /**
     * Return an iterator of the built features that this attribute mapping represents. If this is
     * the first time this is called, it would initiate the built features list. This has to be done
     * after the feature type mapping is complete.
     * 
     * @return built features iterator
     * @throws IOException
     */
    private synchronized Iterator<Feature> iterator() throws IOException {
        if (features == null) {
            features = new ArrayList<Feature>();

            assert simpleFeatures != null;

            // this cannot be set in the constructor since it might not exist yet
            mappingSource = DataAccessRegistry.getFeatureSource(nestedFeatureType);
            assert mappingSource != null;

            // get mapped attribute name on nested features side
            Expression nestedPath = filterFac.property(this.nestedTargetXPath.toString());

            // get all the nested values from all parent features so we only have to run
            // this big query once, rather than running multiple simple ones
            List<Filter> filters = new ArrayList<Filter>();
            for (Feature parentFeature : parentFeatures) {
                Object value = this.getSourceExpression().evaluate(parentFeature);
                if (value instanceof Attribute) {
                    value = ((Attribute) value).getValue();
                }
                if (value instanceof Collection) {
                    for (Object val : (Collection) value) {
                        filters.add(filterFac.equals(nestedPath, filterFac.literal(val)));
                    }
                } else {
                    filters.add(filterFac.equals(nestedPath, filterFac.literal(value)));
                }
            }

            Filter filter = filterFac.or(filters);
            // get all the mapped nested features based on the link values
            MappingFeatureIterator iterator = (MappingFeatureIterator) mappingSource.getFeatures(
                    filter).iterator();
            nestedIdExpression = iterator.featureFidMapping;
            while (iterator.hasNext()) {
                features.add(iterator.next());
            }
        }
        return features.iterator();
    }

    /**
     * Get a simple feature that is stored in this mapping using a supplied link value.
     * 
     * @param foreignKeyValue
     * @return The matching simple feature
     * @throws IOException
     * @throws IOException
     */
    public Feature getInputFeature(Object foreignKeyValue) throws IOException {
        Iterator<Feature> it = simpleIterator();
        Feature feature;
        while (it.hasNext()) {
            feature = it.next();
            Object value = this.nestedExpression.evaluate(feature);
            if (value != null && value.equals(foreignKeyValue)) {
                return feature;
            }
        }
        throw new DataSourceException("Nested feature not found! Feature type: '"
                + this.nestedFeatureType + "', where " + this.nestedExpression + " = '"
                + foreignKeyValue + "'");
    }

    /**
     * Get a built feature that is stored in this mapping using a supplied link value
     * 
     * @param foreignKeyValue
     * @return The matching simple feature
     * @throws IOException
     */
    public Feature getFeature(Object foreignKeyValue) throws IOException {
        // get an input feature first since it uses the unique source expression which can be
        // evaluated to the correct value, whereas with built features, we have to go through
        // possibly multiple properties that has the same target XPath
        Feature inputFeature = getInputFeature(foreignKeyValue);
        Iterator<Feature> it = iterator();
        // this should be set in iterator()
        if (this.nestedIdExpression == null) {
            throw new UnsupportedOperationException(
                    "Iterator() should be called before getFeature(Object) in NestedAttributeMapping!");
        }
        while (it.hasNext()) {
            Feature feature = it.next();
            FeatureId featureId = feature.getIdentifier();
            Object inputFeatureId = this.nestedIdExpression.evaluate(inputFeature);
            if (inputFeatureId == null) {
                throw new RuntimeException(
                        "This shouldn't have happened. This feature does not have an id!");
            }
            if (inputFeatureId instanceof FeatureId) {
                if (featureId.equals(inputFeatureId)) {
                    // this is when the input features are complex features
                    // as a result of a data access being the input data source
                    return feature;
                }
            } else {
                if (featureId.toString().equals(inputFeatureId)) {
                    // this is when input features are simple features
                    // when a data store is the input data source
                    return feature;
                }
            }
        }
        throw new DataSourceException("Nested feature not found! Feature type: '"
                + this.nestedFeatureType + "', where " + this.nestedExpression + " = '"
                + foreignKeyValue + "'");
    }
}
