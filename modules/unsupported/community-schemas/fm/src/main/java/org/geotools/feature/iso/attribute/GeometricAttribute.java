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

package org.geotools.feature.iso.attribute;

import org.geotools.feature.iso.AttributeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * TODO: rename to GeometricAttribute Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * <p>
 * </p>
 * <p>
 * Example Use:
 * 
 * <pre><code>
 *         GeometryAttributeType x = new GeometryAttributeType( ... );
 *         TODO code example
 * </code></pre>
 * 
 * </p>
 * 
 * @author Leprosy
 * @since 0.3 TODO: test wkt geometry parse.
 */
public class GeometricAttribute extends AttributeImpl implements
		GeometryAttribute {
	/** CoordianteSystem used by this GeometryAttributeType */
	protected CoordinateReferenceSystem coordinateSystem;
	private BoundingBox bounds;

	public GeometricAttribute(
		Object content, AttributeDescriptor descriptor, String id, 
		CoordinateReferenceSystem cs
	) {
		super(content, descriptor, id);
		coordinateSystem = cs;
		
		if (!(descriptor.type() instanceof GeometryType)) {
			throw new IllegalArgumentException("Expected GeometryType, got "
					+ descriptor);
		}
		
		/*
		 * geometryFactory = cs == null ? CSGeometryFactory.DEFAULT : new
		 * CSGeometryFactory(cs);
		 */
		/*
		 * coordinateSystem = (cs != null) ? cs :
		 * LocalCoordinateSystem.CARTESIAN; geometryFactory = (cs ==
		 * LocalCoordinateSystem.CARTESIAN) ? CSGeometryFactory.DEFAULT : new
		 * CSGeometryFactory(cs);
		 */
	}

	public CoordinateReferenceSystem getCRS() {
		return coordinateSystem;
	}
    
    public void setCRS(CoordinateReferenceSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }
    

	/*
	 * public GeometryFactory getGeometryFactory() { return geometryFactory; }
	 */

	protected Object parse(Object value) throws IllegalArgumentException {
		if (value == null) {
			return value;
		}

		if (value instanceof Geometry) {
			return value;
		}

		if (value instanceof String) {
			String wkt = (String) value;
			WKTReader reader = new WKTReader();
			try {
				return reader.read(wkt);
			} catch (com.vividsolutions.jts.io.ParseException pe) {
				throw new IllegalArgumentException("Could not parse the "
						+ "string: " + wkt + " to well known text");
			}
		}
		// consider wkb/gml support?
		throw new IllegalArgumentException(getClass().getName()
				+ " cannot parse " + value);
	}

	public Object /*Geometry*/ getValue() {
		return (Geometry) super.getValue();
	}

	public void set(Geometry geometry) {
		super.setValue(geometry);
	}

	/**
	 * Set the bounds for the contained geometry.
	 */
	public synchronized void setBounds( BoundingBox bbox ){
		bounds = bbox;
	}
	
	/**
	 * Returns the non null envelope of this attribute. If the attribute's
	 * geometry is <code>null</code> the returned Envelope
	 * <code>isNull()</code> is true.
	 * 
	 * @return 
	 */
	public synchronized BoundingBox getBounds() {
		if( bounds == null ){
			ReferencedEnvelope bbox = new ReferencedEnvelope(coordinateSystem);
			Geometry geom = (Geometry) getValue();
			if (geom != null) {
				bbox.expandToInclude(geom.getEnvelopeInternal());
			}
			bounds =  bbox;
		}
		return bounds;
	}

	public boolean equals(Object o) {
		if (!(o instanceof GeometricAttribute)) {
			return false;
		}
		GeometricAttribute att = (GeometricAttribute) o;

		if (!(DESCRIPTOR.equals(att.DESCRIPTOR))) {
			return false;
		}

		if (ID == null) {
			if (att.ID != null) {
				return false;
			}
		} else if (!ID.equals(att.ID)) {
			return false;
		}

		if (content == null) {
			if (att.content != null) {
				return false;
			}
		} else {
			// we need to special case Geometry
			// as JTS is broken
			// Geometry.equals( Object ) and Geometry.equals( Geometry )
			// are different
			// (We should fold this knowledge into AttributeType...)
			// 
			if (!((Geometry) content).equals((Geometry) att.content)) {
				return false;
			}
		}

		return true;
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer("GeometricAttribute[");
		sb.append("id=")
		.append(this.ID)
		.append(", type=")
		.append(this.DESCRIPTOR.getName())
		.append(", content=" + this.content)
		.append("]");
		return sb.toString();
	}		
}

/**
 * Helper class used to force CS information on JTS Geometry
 */
/*
 * class CSGeometryFactory extends GeometryFactory {
 * 
 * static public GeometryFactory DEFAULT = new GeometryFactory(); static public
 * PrecisionModel DEFAULT_PRECISON_MODEL = new PrecisionModel();
 * 
 * public CSGeometryFactory(CoordinateReferenceSystem cs) {
 * super(toPrecisionModel(cs), toSRID(cs)); }
 * 
 * public GeometryCollection createGeometryCollection(Geometry[] geometries) {
 * GeometryCollection gc = super.createGeometryCollection(geometries); // JTS14
 * //gc.setUserData( cs ); return gc; }
 * 
 * public LinearRing createLinearRing(Coordinate[] coordinates) { LinearRing lr =
 * super.createLinearRing(coordinates); // JTS14 //gc.setUserData( cs ); return
 * lr; } // // And so on // Utility Functions private static int
 * toSRID(CoordinateReferenceSystem cs) { if ((cs == null) || (cs ==
 * DefaultGeocentricCRS.CARTESIAN)) { return 0; } // not sure how to tell SRID
 * from CoordinateSystem? return 0; }
 * 
 * private static PrecisionModel toPrecisionModel(CoordinateReferenceSystem cs) {
 * if ((cs == null) || (cs == DefaultGeocentricCRS.CARTESIAN)) { return
 * DEFAULT_PRECISON_MODEL; }
 * 
 * return DEFAULT_PRECISON_MODEL; } }
 */