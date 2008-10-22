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

package org.geotools.feature.iso.simple;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.GeometryAttribute;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

class IndexGeometryAttribute extends IndexAttribute implements
		GeometryAttribute {
	
	ArraySimpleFeature feature;
	
	BoundingBox bounds = null;

	public IndexGeometryAttribute(ArraySimpleFeature feature, int index) {
		super(feature, index);
		this.feature = feature;
	}

	public CoordinateReferenceSystem getCRS() {
		return this.feature.type.getCRS();
	}

	public void setCRS(CoordinateReferenceSystem crs) {
		if (!this.feature.type.getCRS().equals(crs)) {
			throw new IllegalArgumentException(
					"Provided crs does not match");
		}
	}
	public void setBounds(BoundingBox bbox) {
		bounds = bbox;
	}
	public synchronized BoundingBox getBounds() {
		if (bounds == null) {
			if (this.feature.values[index] instanceof Geometry) {
				Geometry geometry = (Geometry) this.feature.values[index];
				bounds = new ReferencedEnvelope(geometry
						.getEnvelopeInternal(), this.feature.type.getCRS());
			}
		}
		return bounds;
	}

	public void setValue(Object geom) {
		this.feature.values[index] = geom;
	}

}