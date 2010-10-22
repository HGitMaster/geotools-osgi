/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AppSchemaFeatureFactoryImpl;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.Types;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.geotools.xlink.XLINK;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Base class for several MappingFeatureImplementation's. 
 * 
 * @author Russell Petty, GSV
 * @version $Id: AbstractMappingFeatureIterator.java 35836 2010-07-05 07:49:35Z ang05a $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/AbstractMappingFeatureIterator.java $
 */
public abstract class AbstractMappingFeatureIterator implements IMappingFeatureIterator {
    /** The logger for the filter module. */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.data.complex");

    /**
     * Name representation of xlink:href
     */
    public static final Name XLINK_HREF_NAME = Types.toTypeName(XLINK.HREF);

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

    protected AppSchemaDataAccess store;

    final protected XPath xpathAttributeBuilder;

    protected FilterFactory namespaceAwareFilterFactory;

    /**
     * maxFeatures restriction value as provided by query
     */
    protected final int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    protected int featureCounter;

    protected NamespaceSupport namespaces;

    /**
     * True if hasNext has been called prior to calling next()
     */
    private boolean hasNextCalled = false;

    public AbstractMappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping,
            Query query) throws IOException {
        this(store, mapping, query, false);
    }
    
    public AbstractMappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping,
            Query query, boolean isQueryUnrolled) throws IOException {
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
        this.maxFeatures = query.getMaxFeatures();
                
        if (!isQueryUnrolled) {
            query = getUnrolledQuery(query);
        }                
        initialiseSourceFeatures(mapping, query);
        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(attf);
        namespaces = mapping.getNamespaces();
        namespaceAwareFilterFactory = new FilterFactoryImplNamespaceAware(namespaces);
        xpathAttributeBuilder.setFilterFactory(namespaceAwareFilterFactory);
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
        closeSourceFeatures();
    }

    /**
     * Based on the set of xpath expression/id extracting expression, finds the ID for the attribute
     * <code>idExpression</code> from the source complex attribute.
     * 
     * @param idExpression
     *            the location path of the attribute to be created, for which to obtain the id by
     *            evaluating the corresponding <code>org.geotools.filter.Expression</code> from
     *            <code>sourceInstance</code>.
     * @param sourceInstance
     *            a complex attribute which is the source of the mapping.
     * @return the ID to be applied to a new attribute instance addressed by
     *         <code>attributeXPath</code>, or <code>null</code> if there is no an id mapping for
     *         that attribute.
     */
    protected abstract String extractIdForAttribute(final Expression idExpression,
            Object sourceInstance);

    /**
     * Return next feature.
     * 
     * @see java.util.Iterator#next()
     */
    public Feature next() {      
        if (!isHasNextCalled()) {
            LOGGER.warning("hasNext not called before calling next() in the iterator!");
            if (!hasNext()) {
                return null;
            }
        }
        Feature next;
        try {
            next = computeNext();
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
        ++featureCounter;
        return next;
    }

    /**
     * Return true if there are more features.
     * 
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        if (hasNextCalled) {
            return !isNextSourceFeatureNull();
        }
                
        boolean exists = false;
        
        if (featureCounter >= maxFeatures) {
            return false;
        }
        if (isSourceFeatureIteratorNull()) {
            return false;
        }
        // make sure features are unique by mapped id
        exists = unprocessedFeatureExists();

        if (!exists) {        
            LOGGER.finest("no more features, produced " + featureCounter);
            close();
        }
        
        setHasNextCalled(true);
        
        return exists;
    }

    /**
     * Return a query appropriate to its underlying feature source.
     * 
     * @param query
     *            the original query against the output schema
     * @return a query appropriate to be executed over the underlying feature source.
     */
    protected Query getUnrolledQuery(Query query) {
        return store.unrollQuery(query, mapping);
    }

    protected Feature computeNext() throws IOException {
        if (!hasNextCalled) {
            // hasNext needs to be called to set nextSrcFeature
            if (!hasNext()) {
                return null;
            }
        }
        hasNextCalled = false;
        if (isNextSourceFeatureNull()) {
            throw new UnsupportedOperationException("No more features produced!");
        }

        String id = extractIdForFeature();
        Feature target = populateFeatureData(id);

        if (target.getDefaultGeometryProperty() == null) {
            setGeometry(target);
        }

        return target;

    }

    /**
     * Set the feature geometry to that of the first property bound to a JTS geometry
     * 
     * @param feature
     */
    protected void setGeometry(Feature feature) {
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


    protected boolean isHasNextCalled() {
        return hasNextCalled;
    }
    
    protected void setHasNextCalled(boolean hasNextCalled) {
        this.hasNextCalled = hasNextCalled;
    }

    protected abstract void closeSourceFeatures();

    protected abstract Iterator<Feature> getSourceFeatureIterator();

    protected abstract void initialiseSourceFeatures(FeatureTypeMapping mapping, Query query)
            throws IOException;

    protected abstract boolean unprocessedFeatureExists();

    protected abstract boolean sourceFeatureIteratorHasNext();

    protected abstract String extractIdForFeature();

    protected abstract boolean isNextSourceFeatureNull();

    protected abstract Feature populateFeatureData(String id) throws IOException;

    protected abstract Object getValue(Expression expression, Object sourceFeature);

    protected abstract boolean isSourceFeatureIteratorNull();

    abstract protected void setClientProperties(final Attribute target, final Object source,
            final Map<Name, Expression> clientProperties);
}
