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


import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.geotools.util.SimpleInternationalString;
import org.geotools.util.Utilities;

import org.opengis.style.Description;
import org.opengis.style.StyleVisitor;
import org.opengis.util.Cloneable;


/**
 * Provides a Java representation of the PointSymbolizer. This defines how
 * points are to be rendered.
 *
 * @author Ian Turton, CCG
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/PointSymbolizerImpl.java $
 * @version $Id: PointSymbolizerImpl.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public class PointSymbolizerImpl implements PointSymbolizer, Cloneable {
    
    private final Description description;
    private final String name;
    private final Unit uom;
    private String geometryPropertyName = null;
    private Graphic graphic = new GraphicImpl();

    /**
     * Creates a new instance of DefaultPointSymbolizer
     */
    protected PointSymbolizerImpl() {
        this(new GraphicImpl(), 
                NonSI.PIXEL,
                null,
                null,
                new DescriptionImpl(
                    new SimpleInternationalString("title"), 
                    new SimpleInternationalString("abstract")));
    }

    protected PointSymbolizerImpl(Graphic graphic, Unit uom, String geom, String name, Description desc){
        this.graphic = graphic;
        this.uom = uom;
        this.geometryPropertyName = geom;
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
     * geometry property of the feature should be used.  Geometry types other
     * than inherently linear types can be used.  If a point geometry is used,
     * it should be interpreted as a line of zero length and two end caps.  If
     * a polygon is used (or other "area" type) then its closed outline should
     * be used as the line string (with no end caps). The geometryPropertyName
     * is the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @return The name of the attribute in the feature being styled  that
     *         should be used.  If null then the default geometry should be
     *         used.
     */
    public String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    @Deprecated
    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
    }

    public Unit getUnitOfMeasure() {
        return uom;
    }

    /**
     * Provides the graphical-symbolization parameter to use for the point
     * geometry.
     *
     * @return The Graphic to be used when drawing a point
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Setter for property graphic.
     *
     * @param graphic New value of property graphic.
     */
    @Deprecated
    public void setGraphic(Graphic graphic) {
        if (this.graphic == graphic) {
            return;
        }
        this.graphic = graphic;
    }

    /**
     * Accept a StyleVisitor to perform an operation on this symbolizer.
     *
     * @param visitor The StyleVisitor to accept.
     */
    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }
    
    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone.
     *
     * @return The deep copy clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        PointSymbolizerImpl clone;

        try {
            clone = (PointSymbolizerImpl) super.clone();
            if(graphic != null) clone.graphic = (Graphic) ((Cloneable) graphic).clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Generates the hashcode for the PointSymbolizer
     *
     * @return the hashcode
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (geometryPropertyName != null) {
            result = (PRIME * result) + geometryPropertyName.hashCode();
        }

        if (graphic != null) {
            result = (PRIME * result) + graphic.hashCode();
        }
        
        if(name != null){
            result = (PRIME * result) + name.hashCode();
        }
        
        if(uom != null){
            result = (PRIME * result) + uom.hashCode();
        }
        
        if(description != null){
            result = (PRIME * result) + description.hashCode();
        }

        return result;
    }

    /**
     * Checks this PointSymbolizerImpl with another for equality.
     * 
     * <p>
     * Two PointSymbolizers are equal if the have the same geometry property
     * name and their graphic object is equal.
     * </p>
     * 
     * <p>
     * Note: this method only works for other instances of PointSymbolizerImpl,
     * not other implementors of PointSymbolizer
     * </p>
     *
     * @param oth The object to compare with this PointSymbolizerImpl for
     *        equality.
     *
     * @return True of oth is a PointSymbolizerImpl that is equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof PointSymbolizerImpl) {
            PointSymbolizerImpl other = (PointSymbolizerImpl) oth;

            return Utilities.equals(geometryPropertyName,other.geometryPropertyName)
            && Utilities.equals(graphic, other.graphic)
            && Utilities.equals(uom, other.uom)
            && Utilities.equals(description, other.description);
        }

        return false;
    }



}
