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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.Utilities;

import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.StyleVisitor;
import org.opengis.style.Symbolizer;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.Cloneable;
import org.opengis.util.InternationalString;
import org.opengis.style.Description;

/**
 * DOCUMENT ME!
 *
 * @author James Macgill, CCG
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/StyleImpl.java $
 * @version $Id: StyleImpl.java 31138 2008-08-06 00:41:17Z jgarnett $
 */
public class StyleImpl implements org.geotools.styling.Style, Cloneable {
    
    /** The logger for the default core module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.styling");
    
    private final List<FeatureTypeStyle> featureTypeStyles = new ArrayList<FeatureTypeStyle>();
    
    private final DescriptionImpl description = new DescriptionImpl(
            new SimpleInternationalString("Default Styler"), 
            new SimpleInternationalString(""));
    
    private String name = "Default Styler";
    private boolean defaultB = false;
    private Symbolizer defaultSpecification = null;

    /**
     * Creates a new instance of StyleImpl
     */
    protected StyleImpl() {
    }

    public List<org.opengis.style.FeatureTypeStyle> featureTypeStyles() {
        return featureTypeStyles;
    }
    
    @Deprecated
    public org.geotools.styling.FeatureTypeStyle[] getFeatureTypeStyles() {
        final org.geotools.styling.FeatureTypeStyle[] ret;

        if ( !featureTypeStyles.isEmpty() ) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("number of fts set " + featureTypeStyles.size());
            }
            ret = new org.geotools.styling.FeatureTypeStyle[featureTypeStyles.size()];
            for(int i=0, n=featureTypeStyles.size(); i<n; i++){
                ret[i] = (org.geotools.styling.FeatureTypeStyle) featureTypeStyles.get(i);
            }
            
        }else{
            // we return a single featureTypeStyle array
            ret = new org.geotools.styling.FeatureTypeStyle[1];
            ret[0] = new FeatureTypeStyleImpl();
        }
        
        return ret;
    }

    @Deprecated
    public void setFeatureTypeStyles(org.geotools.styling.FeatureTypeStyle[] styles) {
        List<org.geotools.styling.FeatureTypeStyle> newStyles = Arrays.asList(styles);

        this.featureTypeStyles.clear();
        this.featureTypeStyles.addAll(newStyles);

        LOGGER.fine("StyleImpl added " + featureTypeStyles.size() + " feature types");
    }

    @Deprecated
    public void addFeatureTypeStyle(org.geotools.styling.FeatureTypeStyle type) {
        featureTypeStyles.add(type);
    }

    public Description getDescription() {
        return description;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Deprecated
    public String getAbstract() {
        return description.getAbstract().toString();
    }
    
    @Deprecated
    public void setAbstract(String abstractStr) {
        description.setAbstract(new SimpleInternationalString(abstractStr));
    }
    
    @Deprecated
    public String getTitle() {
        return description.getTitle().toString();
    }

    @Deprecated
    public void setTitle(String title) {
        description.setTitle(new SimpleInternationalString(title));
    }
    
    public boolean isDefault() {
        return defaultB;
    }

    public void setDefault(boolean isDefault) {
        defaultB = isDefault;
    }

    public Object accept(StyleVisitor visitor, Object data) {
        return visitor.visit(this,data);
    }
    
    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Clones the Style.  Creates deep copy clone of the style.
     *
     * @return the Clone of the style.
     *
     * @throws RuntimeException DOCUMENT ME!
     *
     * @see org.geotools.styling.Style#clone()
     */
    public Object clone() {
        Style clone;

        try {
            clone = (Style) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen since we implement Cloneable
        }

        final List<FeatureTypeStyle> ftsCopy = new ArrayList<FeatureTypeStyle>();
        
        
        for(FeatureTypeStyle fts : featureTypeStyles){
            ftsCopy.add( (FeatureTypeStyle) ((Cloneable) fts).clone() );
        }
        
        clone.featureTypeStyles().clear();
        ((List<FeatureTypeStyle>)clone.featureTypeStyles()).addAll(ftsCopy);

        return clone;
    }

    /**
     * Overrides hashcode.
     *
     * @return The hash code.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (featureTypeStyles != null) {
            result = (PRIME * result) + featureTypeStyles.hashCode();
        }

        final String abstractText = description.getAbstract().toString();
        if (abstractText != null) {
            result = (PRIME * result) + abstractText.hashCode();
        }

        if (name != null) {
            result = (PRIME * result) + name.hashCode();
        }

        final String title = description.getTitle().toString();
        if (title != null) {
            result = (PRIME * result) + title.hashCode();
        }

        result = (PRIME * result) + (defaultB ? 1 : 0);

        return result;
    }

    /**
     * Compares this Style with another.
     * 
     * <p>
     * Two StyleImpl are equal if they have the same properties and the same
     * list of FeatureTypeStyles.
     * </p>
     *
     * @param oth The object to compare with this for equality.
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof StyleImpl) {
            StyleImpl other = (StyleImpl) oth;

            return Utilities.equals(name, other.name)
            && Utilities.equals(description, other.description)
            && Utilities.equals(featureTypeStyles, other.featureTypeStyles);
        }

        return false;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append( "StyleImpl");
        buf.append( "[");
    	if( name != null ) {
    		buf.append(" name=");
    		buf.append( name );
    	}
    	else {
    		buf.append( " UNNAMED");
    	}
    	if( defaultB ) {
    		buf.append( ", DEFAULT");
    	}
        buf.append( ", "+description.toString());
    	buf.append("]");
    	return buf.toString();
    }

    public Symbolizer getDefaultSpecification() {
        return defaultSpecification;
    }

    public void setDefaultSpecification(Symbolizer symbolizer){
        this.defaultSpecification = symbolizer;
    }

}
