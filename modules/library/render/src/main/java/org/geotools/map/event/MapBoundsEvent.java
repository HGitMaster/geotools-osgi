/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.map.event;

import java.util.EventObject;

import org.geotools.map.MapContext;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Event object for MapContext area of interest and coordinate system changes.
 *
 * @author wolf
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/render/src/main/java/org/geotools/map/event/MapBoundsEvent.java $
 */
public class MapBoundsEvent extends EventObject {
    /** Area of interest changed */
    public static final int AREA_OF_INTEREST_MASK = 1;

    /** Coordinate system changed */
    public static final int COORDINATE_SYSTEM_MASK = 2;

    /** Used to check that the type flag is acceptable */
    private static final int NEXT_FLAG = 4;

    /** Holds value of property type. */
    private int type;

    /** Holds value of property oldCoordinateReferenceSystem. */
    private CoordinateReferenceSystem oldCoordinateReferenceSystem;

    /** Holds value of property oldAreaOfInterest. */
    private Envelope oldAreaOfInterest;

    /**
     * Creates a new instance of BoundsEvent
     *
     * @param source DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param oldAreaOfInterest DOCUMENT ME!
     * @param oldCoordinateReferenceSystem DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public MapBoundsEvent(MapContext source, int type, Envelope oldAreaOfInterest,
        CoordinateReferenceSystem oldCoordinateReferenceSystem) {
        super(source);

        if (type >= NEXT_FLAG) {
            throw new IllegalArgumentException("Type is not acceptable, maximum value is "
                + (NEXT_FLAG - 1) + ", passed value is " + type);
        }

        this.type = type;
        this.oldAreaOfInterest = oldAreaOfInterest;
        this.oldCoordinateReferenceSystem = oldCoordinateReferenceSystem;
    }

    /**
     * Getter for property type. The type is a bitwise or of the masks defined above.
     *
     * @return Value of property type.
     */
    public int getType() {
        return this.type;
    }

    /**
     * Getter for property oldCoordinateReferenceSystem.
     *
     * @return Value of property oldCoordinateReferenceSystem.
     */
    public CoordinateReferenceSystem getOldCoordinateReferenceSystem() {
        return this.oldCoordinateReferenceSystem;
    }

    /**
     * Getter for property oldAreaOfInterest.
     *
     * @return Value of property oldAreaOfInterest.
     */
    public Envelope getOldAreaOfInterest() {
        return this.oldAreaOfInterest;
    }
}
