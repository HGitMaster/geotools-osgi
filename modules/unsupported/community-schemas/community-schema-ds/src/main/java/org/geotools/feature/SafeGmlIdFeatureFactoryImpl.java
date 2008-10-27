package org.geotools.feature;

import java.util.ArrayList;
import java.util.Collection;

import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.identity.GmlObjectId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * FeatureFactory that does not choke on null gml ids.
 *
 */
public class SafeGmlIdFeatureFactoryImpl extends ValidatingFeatureFactoryImpl {

    @Override
    public Attribute createAttribute(Object value, AttributeDescriptor descriptor, String id) {
        return new AttributeImpl(value, descriptor, buildSafeGmlObjectID(id));
    }

    @Override
    public GeometryAttribute createGeometryAttribute(Object value, GeometryDescriptor descriptor,
            String id, CoordinateReferenceSystem crs) {

        return new GeometryAttributeImpl(value, descriptor, buildSafeGmlObjectID(id));
    }

    @Override
    public ComplexAttribute createComplexAttribute(Collection value,
            AttributeDescriptor descriptor, String id) {
        return new ComplexAttributeImpl(buildCollectionIfNull(value), descriptor,
                buildSafeGmlObjectID(id));
    }

    @Override
    public ComplexAttribute createComplexAttribute(Collection value, ComplexType type, String id) {
        return new ComplexAttributeImpl(buildCollectionIfNull(value), type,
                buildSafeGmlObjectID(id));
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
