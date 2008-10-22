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

package org.geotools.feature.iso;

import java.util.Collection;

import org.geotools.feature.iso.attribute.GeometricAttribute;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Ian Schneider
 * @author Gabriel Roldan
 * @author Justin Deoliveira
 * 
 * @version $Id: AttributeFactoryImpl.java 31374 2008-09-03 07:26:50Z bencd $
 */
public class AttributeFactoryImpl implements FeatureFactory {
 
    CRSFactory crsFactory;
    GeometryFactory  geometryFactory;
    
    public CRSFactory getCRSFactory() {
        return crsFactory;
    }

    public void setCRSFactory(CRSFactory crsFactory) {
        this.crsFactory = crsFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }
    
    public Association createAssociation(Attribute related, AssociationDescriptor descriptor) {
        return new AssociationImpl(related,descriptor);
    }
	
	public Attribute createAttribute(
		Object value, AttributeDescriptor descriptor, String id
	) {
		return new AttributeImpl(value,descriptor,id);
	}
	
	public GeometryAttribute createGeometryAttribute(
		Object value, AttributeDescriptor desc, String id, CoordinateReferenceSystem crs) {
	
		return new GeometricAttribute(value,desc,id,crs);
	}
	
	public ComplexAttribute createComplexAttribute(
		Collection value, AttributeDescriptor desc, String id
	) {
		return new ComplexAttributeImpl(value, desc, id );
	}

	public ComplexAttribute createComplexAttribute(
		Collection value, ComplexType type, String id
	) {
		return new ComplexAttributeImpl(value, type, id );
	}
	
	public Feature createFeature(Collection value, AttributeDescriptor desc, String id) {
		return new FeatureImpl(value, desc, id);
	}

	public Feature createFeature(Collection value, FeatureType type, String id) {
		return new FeatureImpl(value, type, id);
	}
	
	public FeatureCollection createFeatureCollection(Collection value, AttributeDescriptor desc, String id) {
		return new FeatureCollectionImpl(value, desc, id);
	}
	
	public FeatureCollection createFeatureCollection(Collection value, FeatureCollectionType type, String id) {
		return new FeatureCollectionImpl(value, type, id);
	}
   
}

