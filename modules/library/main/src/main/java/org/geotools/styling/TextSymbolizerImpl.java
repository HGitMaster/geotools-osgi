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

import java.util.HashMap;
import java.util.Map;
import javax.measure.unit.Unit;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.util.Utilities;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.Description;
import org.opengis.style.StyleVisitor;
import org.opengis.util.Cloneable;


/**
 * Provides a Java representation of an SLD TextSymbolizer that defines how
 * text symbols should be rendered.
 *
 * @author Ian Turton, CCG
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/TextSymbolizerImpl.java $
 * @version $Id: TextSymbolizerImpl.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public class TextSymbolizerImpl implements TextSymbolizer2, Cloneable {
    
    private final Description desc;
    private final String name;
    private final Unit uom;
    private Font font;
    
    private final FilterFactory filterFactory;
    private Fill fill;
    private Halo halo;
    private LabelPlacement placement;
    private String geometryPropertyName = null;
    private Expression label = null;
    private Graphic graphic = null;
    private Expression priority = null;
    private HashMap<String,String> optionsMap = null; //null=nothing in it
    private Expression abxtract = null;
    private Expression description = null;
    private OtherText otherText = null;

    protected TextSymbolizerImpl() {
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()) );
    }
    
    /**
     * Creates a new instance of DefaultTextSymbolizer
     */
    protected TextSymbolizerImpl( FilterFactory factory ) {
        this(factory,null,null,null);
    }
    
    protected TextSymbolizerImpl( FilterFactory factory, Description desc, String name, Unit uom ) {
        this.filterFactory = factory;
        this.desc = desc;
        this.uom = uom;
        this.name = name;
        fill = new FillImpl();
        fill.setColor(filterFactory.literal("#000000")); // default text fill is black
        halo = null;
        placement = new PointPlacementImpl();
    }
    
    public String getName() {
        return name;
    }

    public Description getDescription() {
        return desc;
    }
    
    public Unit getUnitOfMeasure() {
        return uom;
    }

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used. Geometry types other
     * than inherently point types can be used. The geometryPropertyName is
     * the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @return String The name of the attribute in the feature being styled
     *         that should be used.  If null then the default geometry should
     *         be used.
     */
    public String geometryPropertyName() {
        return geometryPropertyName;
    }

    /**
     * Returns the fill to be used to fill the text when rendered.
     *
     * @return The fill to be used.
     */
    public Fill getFill() {
        return fill;
    }

    /**
     * Setter for property fill.
     *
     * @param fill New value of property fill.
     */
    @Deprecated
    public void setFill(Fill fill) {
        if (this.fill == fill) {
            return;
        }
        this.fill = fill;
    }

    public Font getFont() {
        return font;
    }
    
    /**
     * Returns a device independent Font object that is to be used to render
     * the label.
     *
     * @return Device independent Font object to be used to render the label.
     */
    @Deprecated
    public  org.geotools.styling.Font[] getFonts() {
        
        if(font == null){
            return new org.geotools.styling.Font[0];
        }else{
            return new org.geotools.styling.Font[]{(org.geotools.styling.Font)font} ;
        }
    }

    /**
     * Setter for property font.
     *
     * @param font New value of property font.
     */
    @Deprecated
    public void addFont(org.geotools.styling.Font font) {
        this.font = font;
    }

    /**
     * Sets the list of fonts in the TextSymbolizer to the provided array of
     * Fonts.
     *
     * @param fonts The array of fonts to use in the symbolizer.
     */
    @Deprecated
    public void setFonts(org.geotools.styling.Font[] fonts) {
        
        if(fonts != null && fonts.length >0){
            this.font = fonts[0]; 
        }else{
            this.font = null;
        }
    }

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     */
    public Halo getHalo() {
        return halo;
    }

    /**
     * Setter for property halo.
     *
     * @param halo New value of property halo.
     */
    @Deprecated
    public void setHalo(Halo halo) {
        if (this.halo == halo) {
            return;
        }
        this.halo = halo;
    }

    /**
     * Returns the label expression.
     *
     * @return Label expression.
     */
    public Expression getLabel() {
        return label;
    }

    /**
     * Setter for property label.
     *
     * @param label New value of property label.
     */
    @Deprecated
    public void setLabel(Expression label) {
        this.label = label;
    }

    /**
     * A pointPlacement specifies how a text element should be rendered
     * relative to its geometric point.
     *
     * @return Value of property labelPlacement.
     */
    public LabelPlacement getPlacement() {
        return placement;
    }

    /**
     * Setter for property labelPlacement.
     *
     * @param labelPlacement New value of property labelPlacement.
     */
    @Deprecated
    public void setPlacement(LabelPlacement labelPlacement) {
        if (this.placement == labelPlacement) {
            return;
        }
        this.placement = labelPlacement;
    }

    /**
     * A pointPlacement specifies how a text element should be rendered
     * relative to its geometric point.
     *
     * @return Value of property labelPlacement.
     *
     */
    public LabelPlacement getLabelPlacement() {
        return getPlacement();
    }

    /**
     * Setter for property labelPlacement.
     *
     * @param labelPlacement New value of property labelPlacement.
     *
     * @deprecated use setPlacement(LabelPlacement)
     */
    @Deprecated
    public void setLabelPlacement( org.geotools.styling.LabelPlacement labelPlacement) {
        setPlacement(labelPlacement);
    }

    /**
     * Getter for property geometryPropertyName.
     *
     * @return Value of property geometryPropertyName.
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    /**
     * Setter for property geometryPropertyName.
     *
     * @param geometryPropertyName New value of property geometryPropertyName.
     */
    @Deprecated
    public void setGeometryPropertyName(java.lang.String geometryPropertyName) {
        this.geometryPropertyName = geometryPropertyName;
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
     * Creates a deep copy clone.   TODO: Need to complete the deep copy,
     * currently only shallow copy.
     *
     * @return The deep copy clone.
     *
     * @throws AssertionError DOCUMENT ME!
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // this should never happen.
        }
    }

    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (fill != null) {
            result = (PRIME * result) + fill.hashCode();
        }

        if (font != null) {
            result = (PRIME * result) + font.hashCode();
        }
        
        if (uom != null) {
            result = (PRIME * result) + uom.hashCode();
        }
        
        if (desc != null) {
            result = (PRIME * result) + desc.hashCode();
        }
        
        if (name != null) {
            result = (PRIME * result) + name.hashCode();
        }

        if (halo != null) {
            result = (PRIME * result) + halo.hashCode();
        }

        if (placement != null) {
            result = (PRIME * result) + placement.hashCode();
        }

        if (geometryPropertyName != null) {
            result = (PRIME * result) + geometryPropertyName.hashCode();
        }

        if (label != null) {
            result = (PRIME * result) + label.hashCode();
        }

        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth instanceof TextSymbolizerImpl) {
            TextSymbolizerImpl other = (TextSymbolizerImpl) oth;

            return Utilities.equals(this.geometryPropertyName,
                other.geometryPropertyName)
            && Utilities.equals(this.label, other.label)
            && Utilities.equals(this.halo, other.halo)
            && Utilities.equals(this.font, other.font)
            && Utilities.equals(this.desc, other.desc)
            && Utilities.equals(this.uom, other.uom)
            && Utilities.equals(this.placement, other.placement)
            && Utilities.equals(this.fill, other.fill);
        }

        return false;
    }

    
    
    public void setPriority(Expression priority) {
        if (this.priority == priority) {
            return;
        }
        this.priority = priority;
    }

    public Expression getPriority() {
        return priority;
    }

    public void addToOptions(String key, String value) {
        if (optionsMap == null) {
            optionsMap = new HashMap<String,String>();
        }
        optionsMap.put(key, value.trim());
    }

    public String getOption(String key) {
        if (optionsMap == null) {
            return null;
        }

        return (String) optionsMap.get(key);
    }

    public Map<String,String> getOptions() {
        return optionsMap;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(Graphic graphic) {
        if (this.graphic == graphic) {
            return;
        }
        this.graphic = graphic;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<TextSymbolizerImp property=");
        buf.append( geometryPropertyName );
        buf.append( " label=");
        buf.append( label );
        buf.append(">");
        buf.append( this.font );
        return buf.toString();
    }
    
    public Expression getSnippet() {
        return abxtract;
    }
    
    public void setSnippet(Expression abxtract) {
        this.abxtract = abxtract;
    }
    
    public Expression getFeatureDescription() {
        return description;
    }
    
    public void setFeatureDescription(Expression description) {
        this.description = description;
    }
    
    public OtherText getOtherText() {
        return otherText;
    }
    
    public void setOtherText(OtherText otherText) {
        this.otherText = otherText;
    }

}
