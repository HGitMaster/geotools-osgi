/*$************************************************************************************************
 **
 ** $Id: MultiPointImpl.java 28267 2007-12-06 02:11:00Z desruisseaux $
 **
 ** $Source: /cvs/ctree/LiteGO1/src/jar/com/polexis/lite/spatialschema/geometry/aggregate/MultiPointImpl.java,v $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.geotools.geometry.jts.spatialschema.geometry.aggregate;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.aggregate.MultiPoint;

/**
 */
public class MultiPointImpl extends AggregateImpl 
	implements MultiPoint {

    public MultiPointImpl() {
        this(null);
    }

    public MultiPointImpl(final CoordinateReferenceSystem crs) {
        super(crs);
    }

    public MultiPointImpl clone() {
        return (MultiPointImpl) super.clone();
    }
}
