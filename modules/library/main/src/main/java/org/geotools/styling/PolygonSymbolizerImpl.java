/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling;


import javax.measure.unit.Unit;

import org.geotools.util.Utilities;

import org.opengis.filter.expression.Expression;
import org.opengis.style.Description;
import org.opengis.style.Displacement;
import org.opengis.style.StyleVisitor;
import org.opengis.util.Cloneable;


/**
 * Provides a representation of a PolygonSymbolizer in an SLD Document.  A
 * PolygonSymbolizer defines how a polygon geometry should be rendered.
 *
 * @author James Macgill, CCG
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/PolygonSymbolizerImpl.java $
 * @version $Id: PolygonSymbolizerImpl.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public class PolygonSymbolizerImpl implements PolygonSymbolizer, Cloneable {
    
    private final Description description;
    private final String name;
    private final Expression offset;
    private final Unit uom;
    private final Displacement disp;
    
    private Fill fill = new FillImpl();
    private Stroke stroke = new StrokeImpl();
    private String geometryName = null;

    /**
     * Creates a new instance of DefaultPolygonStyler
     */
    protected PolygonSymbolizerImpl() {
        this(null,null,null,null,null,null,null,null);
    }

    protected PolygonSymbolizerImpl(Stroke stroke, 
            Fill fill, 
            Displacement disp, 
            Expression offset, 
            Unit uom, 
            String geom, 
            String name, 
            Description desc){
        this.stroke = stroke;
        this.fill = fill;
        this.disp = disp;
        this.offset = offset;
        this.uom = uom;
        this.geometryName = geom;
        this.name = name;
        this.description = desc;
    }
    
    public String getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }
    
    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used. Geometry types other
     * than inherently area types can be used. If a line is used then the line
     * string is closed for filling (only) by connecting its end point to its
     * start point. The geometryPropertyName is the name of a geometry
     * property in the Feature being styled.  Typically, features only have
     * one geometry so, in general, the need to select one is not required.
     * Note: this moves a little away from the SLD spec which provides an
     * XPath reference to a Geometry object, but does follow it in spirit.
     *
     * @return The name of the attribute in the feature being styled  that
     *         should be used.  If null then the default geometry should be
     *         used.
     */
    public String getGeometryPropertyName() {
        return geometryName;
    }

    /**
     * Sets the GeometryPropertyName.
     *
     * @param name The name of the GeometryProperty.
     *
     * @see #PolygonSymbolizerImpl.geometryPropertyName()
     */
    @Deprecated
    public void setGeometryPropertyName(String name) {
        geometryName = name;
    }

    public Unit getUnitOfMeasure() {
        return uom;
    }
    
    public Expression getPerpendicularOffset() {
        return offset;
    }
    
    public Displacement getDisplacement() {
        return disp;
    }

    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry.
     *
     * @return The Fill style to use when rendering the area.
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * Sets the graphical-symbolization parameter to use to fill the area of
     * the geometry.
     *
     * @param fill The Fill style to use when rendering the area.
     */
    @Deprecated
    public void setFill(Fill fill) {
        if (this.fill == fill) {
            return;
        }
        this.fill = fill;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the outline of
     * the Polygon.
     *
     * @return The Stroke style to use when rendering lines.
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the graphical-symbolization parameter to use for the outline of the
     * Polygon.
     *
     * @param stroke The Stroke style to use when rendering lines.
     */
    @Deprecated
    public void setStroke(Stroke stroke) {
        if (this.stroke == stroke) {
            return;
        }
        this.stroke = stroke;
    }

    /**
     * Accepts a StyleVisitor to perform some operation on this LineSymbolizer.
     *
     * @param visitor The visitor to accept.
     */
    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }

    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Creates a deep copy clone.   TODO: Need to complete the deep copy,
     * currently only shallow copy.
     *
     * @return The deep copy clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        PolygonSymbolizerImpl clone;

        try {
            clone = (PolygonSymbolizerImpl) super.clone();

            if (fill != null) {
                clone.fill = (Fill) ((Cloneable) fill).clone();
            }

            if (stroke != null) {
                clone.stroke = (Stroke) ((Cloneable) stroke).clone();
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Generates a hashcode for the PolygonSymbolizerImpl.
     *
     * @return A hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (fill != null) {
            result = (PRIME * result) + fill.hashCode();
        }

        if (stroke != null) {
            result = (PRIME * result) + stroke.hashCode();
        }

        if (geometryName != null) {
            result = (PRIME * result) + geometryName.hashCode();
        }
        
        if (description != null) {
            result = (PRIME * result) + description.hashCode();
        }
        
        if (uom != null) {
            result = (PRIME * result) + uom.hashCode();
        }
        
        if (offset != null) {
            result = (PRIME * result) + offset.hashCode();
        }

        if (disp != null) {
            result = (PRIME * result) + disp.hashCode();
        }
        
        return result;
    }

    /**
     * Compares this PolygonSymbolizerImpl with another.
     * 
     * <p>
     * Two PolygonSymbolizerImpls are equal if they have the same
     * geometryProperty, fill and stroke.
     * </p>
     *
     * @param oth the object to compare against.
     *
     * @return true if oth is equal to this object.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof PolygonSymbolizerImpl) {
            PolygonSymbolizerImpl other = (PolygonSymbolizerImpl) oth;

            return Utilities.equals(this.geometryName,
                other.geometryName)
            && Utilities.equals(fill, other.fill)
            && Utilities.equals(stroke, other.stroke)
            && Utilities.equals(description, other.description)
            && Utilities.equals(disp, other.disp)
            && Utilities.equals(offset, other.offset)
            && Utilities.equals(uom, other.uom);
        }

        return false;
    }



}
