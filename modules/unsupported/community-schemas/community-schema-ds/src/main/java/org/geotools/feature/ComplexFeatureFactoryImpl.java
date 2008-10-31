package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;

import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * FeatureFactory that does not choke on null gml ids.
 * 
 */
public class ComplexFeatureFactoryImpl extends ValidatingFeatureFactoryImpl {

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createAttribute(java.lang.Object,
     *      org.opengis.feature.type.AttributeDescriptor, java.lang.String)
     */
    @Override
    public Attribute createAttribute(Object value, AttributeDescriptor descriptor, String id) {
        return new AttributeImpl(value, descriptor, buildSafeGmlObjectID(id));
    }

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createGeometryAttribute(java.lang.Object,
     *      org.opengis.feature.type.GeometryDescriptor, java.lang.String,
     *      org.opengis.referencing.crs.CoordinateReferenceSystem)
     */
    @Override
    public GeometryAttribute createGeometryAttribute(Object value, GeometryDescriptor descriptor,
            String id, CoordinateReferenceSystem crs) {

        return new GeometryAttributeImpl(value, descriptor, buildSafeGmlObjectID(id));
    }

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createComplexAttribute(java.util.Collection,
     *      org.opengis.feature.type.AttributeDescriptor, java.lang.String)
     */
    @Override
    public ComplexAttribute createComplexAttribute(Collection value,
            AttributeDescriptor descriptor, String id) {
        return new ComplexAttributeImpl(buildCollectionIfNull(value), descriptor,
                buildSafeGmlObjectID(id));
    }

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createComplexAttribute(java.util.Collection,
     *      org.opengis.feature.type.ComplexType, java.lang.String)
     */
    @Override
    public ComplexAttribute createComplexAttribute(Collection value, ComplexType type, String id) {
        return new ComplexAttributeImpl(buildCollectionIfNull(value), type,
                buildSafeGmlObjectID(id));
    }

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createFeature(java.util.Collection,
     *      org.opengis.feature.type.AttributeDescriptor, java.lang.String)
     */
    @Override
    public Feature createFeature(Collection value, AttributeDescriptor descriptor, String id) {
        return new FeatureImpl(buildCollectionIfNull(value), descriptor, ff.featureId(id));
    }

    /*
     * @see org.geotools.feature.AbstractFeatureFactoryImpl#createFeature(java.util.Collection,
     *      org.opengis.feature.type.FeatureType, java.lang.String)
     */
    @Override
    public Feature createFeature(Collection value, FeatureType type, String id) {
        return new FeatureImpl(buildCollectionIfNull(value), type, ff.featureId(id));
    }

    private GmlObjectId buildSafeGmlObjectID(String id) {
        if (id == null) {
            return null;
        } else {
            return ff.gmlObjectId(id);
        }
    }

    private Collection<Property> buildCollectionIfNull(Collection<Property> value) {
        if (value == null) {
            return new ArrayList<Property>();
        } else {
            return value;
        }
    }

}
