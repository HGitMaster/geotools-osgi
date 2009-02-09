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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

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
     * Simple feature source of the nested features
     */
    private FeatureSource<SimpleFeatureType, SimpleFeature> source;

    /**
     * Mapped feature source of the nested features
     */
    private MappingFeatureSource mappingSource;

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
    private ArrayList<SimpleFeature> simpleFeatures;

    /**
     * Stored built features
     */
    private ArrayList<Feature> features;

    /**
     * Parent simple features
     */
    private ArrayList<SimpleFeature> parentFeatures;

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
            Name sourceElement, StepList sourcePath, FeatureSource parentSource) throws IOException {
        super(idExpression, parentExpression, targetXPath, null, isMultiValued, clientProperties);
        this.nestedTargetXPath = sourcePath;
        this.nestedFeatureType = sourceElement;
        // get all parent simple features
        parentFeatures = new ArrayList<SimpleFeature>();
        DefaultQuery parentQuery = new DefaultQuery();
        parentQuery.setPropertyNames(new String[] { parentExpression.toString() });
        FeatureCollection<SimpleFeatureType, SimpleFeature> parentFeatureCollection = parentSource
                .getFeatures(parentQuery);
        Iterator<SimpleFeature> iterator = parentFeatureCollection.iterator();
        while (iterator.hasNext()) {
            parentFeatures.add(iterator.next());
        }
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
    private Iterator<SimpleFeature> simpleIterator() throws IOException {
        if (simpleFeatures == null) {
            // We can't initiate this in the constructor because the feature type mapping
            // might not be built yet.
            simpleFeatures = new ArrayList<SimpleFeature>();
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
            for (SimpleFeature parentFeature : parentFeatures) {
                Object value = this.getSourceExpression().evaluate(parentFeature);
                filters.add(filterFac.equals(this.nestedExpression, filterFac.literal(value)));
            }

            Filter filter = filterFac.or(filters);
            // get all the nested features based on the link values
            Iterator<SimpleFeature> iterator = source.getFeatures(filter).iterator();
            while (iterator.hasNext()) {
                simpleFeatures.add(iterator.next());
            }
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
    private Iterator<Feature> iterator() throws IOException {
        if (features == null) {
            features = new ArrayList<Feature>();

            assert simpleFeatures != null;

            // this cannot be set in the constructor since it might not exist yet
            mappingSource = AppSchemaDataAccessRegistry.getMappingFeatureSource(nestedFeatureType);
            assert mappingSource != null;

            // get mapped attribute name on nested features side
            Expression nestedPath = filterFac.property(this.nestedTargetXPath.toString());

            // get all the nested values from all parent features so we only have to run
            // this big query once, rather than running multiple simple ones
            List<Filter> filters = new ArrayList<Filter>();
            for (SimpleFeature parentFeature : parentFeatures) {
                Object value = this.getSourceExpression().evaluate(parentFeature);
                filters.add(filterFac.equals(nestedPath, filterFac.literal(value)));
            }

            Filter filter = filterFac.or(filters);
            // get all the mapped nested features based on the link values
            Iterator<Feature> iterator = mappingSource.getFeatures(filter).iterator();
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
    public SimpleFeature getSimpleFeature(Object foreignKeyValue) throws IOException {
        Iterator<SimpleFeature> it = simpleIterator();
        SimpleFeature feature;
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
        // get a simple feature first since it uses the unique source expression which can be
        // evaluated to the correct value, whereas with built features, we have to go through
        // possibly multiple properties that has the same target xpath
        SimpleFeature simpleFeature = getSimpleFeature(foreignKeyValue);
        Iterator<Feature> it = iterator();
        while (it.hasNext()) {
            Feature feature = it.next();
            if (feature.getIdentifier().equals(simpleFeature.getIdentifier())) {
                return feature;
            }
        }
        throw new DataSourceException("Nested feature not found! Feature type: '"
                + this.nestedFeatureType + "', where " + this.nestedExpression + " = '"
                + foreignKeyValue + "'");
    }
}
