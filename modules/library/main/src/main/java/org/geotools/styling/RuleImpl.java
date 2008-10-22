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

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.util.Utilities;
import org.geotools.util.SimpleInternationalString;
import org.opengis.filter.Filter;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.GraphicLegend;
import org.opengis.style.StyleVisitor;
import org.opengis.util.Cloneable;
import org.opengis.util.InternationalString;
import org.opengis.style.Description;

/**
 * Provides the default implementation of Rule.
 *
 * @author James Macgill
 * @author Johann Sorel (Geomatys)
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/RuleImpl.java $
 * @version $Id: RuleImpl.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public class RuleImpl implements org.geotools.styling.Rule, Cloneable {
    private List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
    
    private final DescriptionImpl description = new DescriptionImpl(
            new SimpleInternationalString("title"), 
            new SimpleInternationalString("abstract"));
    
    private List<org.geotools.styling.Graphic> legends = new ArrayList<org.geotools.styling.Graphic>();
    
    private String name;
    private Filter filter = null;
    private boolean hasElseFilter = false;
    private double maxScaleDenominator = Double.POSITIVE_INFINITY;
    private double minScaleDenominator = 0.0;
    private OnLineResource online = null;
    
    /**
     * Creates a new instance of DefaultRule
     */
    protected RuleImpl() {
    }

    /**
     * Creates a new instance of DefaultRule
     *
     * @param symbolizers DOCUMENT ME!
     */
    protected RuleImpl(Symbolizer[] symbolizers) {
        this.symbolizers.addAll(Arrays.asList(symbolizers));
    }
    
    protected RuleImpl(org.geotools.styling.Symbolizer[] symbolizers, 
                        Description desc, 
                        org.geotools.styling.Graphic[] legends,
                        String name,
                        Filter filter,
                        boolean isElseFilter,
                        double maxScale,
                        double minScale){
        setSymbolizers(symbolizers);
        description.setAbstract(desc.getAbstract());
        description.setTitle(desc.getTitle());
        setLegendGraphic(legends);
        this.name = name;
        this.filter = filter;
        hasElseFilter = isElseFilter;
        this.maxScaleDenominator = maxScale;
        this.minScaleDenominator = minScale;
        
    }
    

    public org.geotools.styling.Graphic[] getLegendGraphic() {
        return legends.toArray(new org.geotools.styling.Graphic[0]);
    }
    
    @Deprecated
    public void addLegendGraphic(org.geotools.styling.Graphic graphic) {
        legends.add(graphic);
    }
    
    /**
     * A set of equivalent Graphics in different formats which can be used as a
     * legend against features stylized by the symbolizers in this rule.
     *
     * @param graphics An array of Graphic objects, any of which can be used as
     *        the legend.
     */
    @Deprecated
    public void setLegendGraphic(org.geotools.styling.Graphic[] graphics) {
        List<org.geotools.styling.Graphic> graphicList = Arrays.asList(graphics);
        this.legends = new ArrayList<Graphic>(graphicList);
//        this.legends.clear();
//        this.legends.addAll(graphicList);
    }
    
    public GraphicLegend getLegend() {
        if(legends.isEmpty()) return null;
        else return legends.get(0);
    }

    public List<Symbolizer> symbolizers() {
        return symbolizers;
    }
    
    @Deprecated
    public void addSymbolizer(org.geotools.styling.Symbolizer symb) {
        this.symbolizers.add(symb);
    }

    @Deprecated
    public void setSymbolizers(org.geotools.styling.Symbolizer[] syms) {
        List<org.geotools.styling.Symbolizer> symbols = Arrays.asList(syms);
        this.symbolizers = new ArrayList<Symbolizer>(symbols);
//        this.symbolizers.clear();
//        this.symbolizers.addAll(symbols);
    }

    @Deprecated
    public org.geotools.styling.Symbolizer[] getSymbolizers() {
        
        final org.geotools.styling.Symbolizer[] ret;

        ret = new org.geotools.styling.Symbolizer[symbolizers.size()];
        for(int i=0, n=symbolizers.size(); i<n; i++){
            ret[i] = (org.geotools.styling.Symbolizer) symbolizers.get(i);
        }
        
        return ret;
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

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean isElseFilter() {
        return hasElseFilter;
    }
    
    /**
     * @deprecated use isElseFilter instead.
     */
    @Deprecated
    public boolean hasElseFilter() {
        return hasElseFilter;
    }

    public void setIsElseFilter(boolean flag) {
        hasElseFilter = flag;        
    }

    /**
     * 
     * @deprecated use setIsElseFilter(true)
     */
    @Deprecated
    public void setHasElseFilter() {
        hasElseFilter = true;        
    }

    /**
     * Getter for property maxScaleDenominator.
     *
     * @return Value of property maxScaleDenominator.
     */
    public double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Setter for property maxScaleDenominator.
     *
     * @param maxScaleDenominator New value of property maxScaleDenominator.
     */
    public void setMaxScaleDenominator(double maxScaleDenominator) {
        this.maxScaleDenominator = maxScaleDenominator;
    }

    /**
     * Getter for property minScaleDenominator.
     *
     * @return Value of property minScaleDenominator.
     */
    public double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Setter for property minScaleDenominator.
     *
     * @param minScaleDenominator New value of property minScaleDenominator.
     */
    public void setMinScaleDenominator(double minScaleDenominator) {
        this.minScaleDenominator = minScaleDenominator;
    }

    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }

    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Creates a deep copy clone of the rule.
     *
     * @see org.geotools.styling.Rule#clone()
     */
    public Object clone() {
        try {
            RuleImpl clone = (RuleImpl) super.clone();
                        
            clone.name = name;
            clone.description.setAbstract(description.getAbstract());
            clone.description.setTitle(description.getTitle());
            if( filter == null ){
                clone.filter = null;
            }else{
                DuplicatingFilterVisitor visitor = new DuplicatingFilterVisitor();
                clone.filter = (Filter) filter.accept(visitor, CommonFactoryFinder.getFilterFactory2(null));
            }
            clone.hasElseFilter = hasElseFilter;
            clone.legends = new ArrayList<Graphic>(legends);

            clone.symbolizers = new ArrayList<Symbolizer>(symbolizers);

            clone.maxScaleDenominator = maxScaleDenominator;
            clone.minScaleDenominator = minScaleDenominator;
            
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This will never happen", e);
        }
    }

    /**
     * Generates a hashcode for the Rule.
     * 
     * <p>
     * For complex styles this can be an expensive operation since the hash
     * code is computed using all the hashcodes of the object within the
     * style.
     * </p>
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        result = (PRIME * result) + symbolizers.hashCode();

        result = (PRIME * result) + legends.hashCode();

        if (name != null) {
            result = (PRIME * result) + name.hashCode();
        }

        result = (PRIME * result) + description.hashCode();
        
        if (filter != null) {
            result = (PRIME * result) + filter.hashCode();
        }

        result = (PRIME * result) + (hasElseFilter ? 1 : 0);

        long temp = Double.doubleToLongBits(maxScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);
        temp = Double.doubleToLongBits(minScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);

        return result;
    }

    /**
     * Compares this Rule with another for equality.
     * 
     * <p>
     * Two RuleImpls are equal if all their properties are equal.
     * </p>
     * 
     * <p>
     * For complex styles this can be an expensive operation since it checks
     * all objects for equality.
     * </p>
     *
     * @param oth The other rule to compare with.
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof RuleImpl) {
            RuleImpl other = (RuleImpl) oth;

            return Utilities.equals(name, other.name)
            && Utilities.equals(description, other.description)
            && Utilities.equals(filter, other.filter)
            && (hasElseFilter == other.hasElseFilter)
            && Utilities.equals(legends, other.legends)
            && Utilities.equals(symbolizers, other.symbolizers)
            && (Double.doubleToLongBits(maxScaleDenominator) == Double
            .doubleToLongBits(other.maxScaleDenominator))
            && (Double.doubleToLongBits(minScaleDenominator) == Double
            .doubleToLongBits(other.minScaleDenominator));
        }

        return false;
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append( "<RuleImpl");
        if( name != null ){
            buf.append(":");
            buf.append( name );
        }
        buf.append("> ");
        buf.append( filter );
        if( symbolizers != null ){
            buf.append( "\n" );
            for( Symbolizer symbolizer : symbolizers ){
                buf.append( "\t");
                buf.append( symbolizer );
                buf.append( "\n");            
            }
        }
        return buf.toString();
    }

    public OnLineResource getOnlineResource() {
        return online;
    }

    public void setOnlineResource(OnLineResource online) {
        this.online = online;
    }

}
