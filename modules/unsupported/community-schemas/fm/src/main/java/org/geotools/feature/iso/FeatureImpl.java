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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * While in transition to the new FM, this class implements the deprecated
 * {@linkplain org.opengis.feature.Feature} in order to no have to touch all the
 * library.
 * <p>
 * NOTE all the methods from the old geotools Feature interface throws
 * UnsupportedOperationException
 * </p>
 * 
 * @author gabriel
 */
public class FeatureImpl extends ComplexAttributeImpl implements Feature {

	/**
	 * Optional, may be null
	 */
	CoordinateReferenceSystem crs;

	/**
	 * Optional, may be null
	 */
	GeometryAttribute defaultGeometry;

	/**
	 * Create a Feature with the following content.
	 * 
	 * @param values Collectio of Properties (aka Attributes and/or Associations)
	 * @param desc Nested descriptor
	 * @param id Feature ID
	 */
	public FeatureImpl(Collection values, AttributeDescriptor desc, String id) {
		super(values, desc, id);
		// super takes care of checking id since type is always
		// identified
	}
	/**
	 * Create a Feature with the following content.
	 * 
	 * @param values Collectio of Properties (aka Attributes and/or Associations)
	 * @param type Type of feature to be created
	 * @param id Feature ID
	 */
	public FeatureImpl(Collection values, FeatureType type, String id) {
		super(values, type, id);
	}

	public void setCRS(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		// JD: commenting out the implementation of this method, there is too
		// much jumping through hoops here, this is a data object, it should
		// contain this logic. The logic should be present in the object
		// constructing the feature.
		return crs;
		// FeatureType type = (FeatureType)getType();
		// CoordinateReferenceSystem crs = type.getCRS();
		// if (crs != null)
		// return crs;
		//        
		// GeometryType defaultGeomType =
		// (GeometryType) type.getDefaultGeometry().getType();
		//        
		// if (defaultGeomType != null) {
		// // use the value of the Attribute, if found. Else
		// // the one of the default type
		// Geometry geom = (Geometry)getDefaultGeometry();
		// if (geom != null) {
		// Object instanceMetadata = geom.getUserData();
		// if (instanceMetadata instanceof CoordinateReferenceSystem) {
		// crs = (CoordinateReferenceSystem) instanceMetadata;
		// }
		// }
		//			 
		// if (crs == null) {
		// crs = defaultGeomType.getCRS();
		// }
		// }
		// return crs;
	}

	/**
	 * Get the total bounds of this feature which is calculated by doing a union
	 * of the bounds of each geometry this feature is associated with.
	 * 
	 * @return An Envelope containing the total bounds of this Feature.
	 * 
	 * @task REVISIT: what to return if there are no geometries in the feature?
	 *       For now we'll return a null envelope, make this part of interface?
	 *       (IanS - by OGC standards, all Feature must have geom)
	 */
	public BoundingBox getBounds() {

		ReferencedEnvelope bounds = new ReferencedEnvelope(getCRS());
		if (((FeatureType) getType()).getDefaultGeometry() != null) {
			for (Iterator itr = attributes().iterator(); itr.hasNext();) {
				Attribute attribute = (Attribute) itr.next();
				if (attribute instanceof GeometryAttribute) {
					// JD: unsafe cast to geometry
					Geometry geom = (Geometry) ((GeometryAttribute) attribute)
							.getValue();
					if (geom != null) {
						bounds.expandToInclude(geom.getEnvelopeInternal());
					}
				}
			}
		}
		return bounds;
	}

	public GeometryAttribute getDefaultGeometry() {
		return defaultGeometry;

		// GeometryType geometryType = (GeometryType)
		// ((FeatureType)getType()).getDefaultGeometry().getType();
		//		
		// if (geometryType != null) {
		// for (Iterator itr = attribtues.iterator(); itr.hasNext();) {
		// Attribute attribute = (Attribute) itr.next();
		// if (attribute instanceof GeometryAttribute) {
		// if (attribute.getType().equals(geometryType)) {
		// return (GeometryAttribute)attribute;
		// }
		// }
		// }
		// }
		//        
		// return null;
	}

	public void setDefaultGeometry(GeometryAttribute defaultGeometry) {
		this.defaultGeometry = defaultGeometry;
	}

	public void setDefaultGeometry(Geometry g) {
		AttributeDescriptor geometry = ((FeatureType) getType())
				.getDefaultGeometry();
		if (geometry == null) {
			throw new IllegalArgumentException(
					"FeatureType has no default geometry attribute");
		}

		List/* <Attribute> */geoms = get(geometry.getName());
		if (geoms.size() > 0) {
			Attribute att = (Attribute) geoms.get(0);
			att.setValue(g);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		Collection/* <Attribute> */atts = this.properties;

		sb.append("<").append(this.ID);
		if (DESCRIPTOR != null) {
			sb.append(",").append(DESCRIPTOR.getName().getLocalPart());
		}
		sb.append(">");
		
		sb.append(getType().getName().getLocalPart()).append("=[");
		for (Iterator itr = atts.iterator(); itr.hasNext();) {
			Attribute att = (Attribute) itr.next();
			sb.append(att.getDescriptor().getName().getLocalPart());
			if( !(att instanceof ComplexAttribute)){
				sb.append('=');
				sb.append(att.getValue());
			}
			if( itr.hasNext()) sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	// public Object clone(){
	// //TODO: use builder to create new feature
	// FeatureImpl copy = new FeatureImpl(ID, DESCRIPTOR,builder);
	// copy.set(get());
	// copy.setProperties(new HashMap(properties));
	// return copy;
	// }
}
