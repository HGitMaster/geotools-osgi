/*$************************************************************************************************
 **
 ** $Id: MultiPointImpl.java 34133 2009-10-12 08:14:05Z mbedward $
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
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/jts-wrapper/src/main/java/org/geotools/geometry/jts/spatialschema/geometry/aggregate/MultiPointImpl.java $
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
