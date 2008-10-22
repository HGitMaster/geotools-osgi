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

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.StyleVisitor;

/**
 * Default implementation of SelectedChannelType.
 * 
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/SelectedChannelTypeImpl.java $
 */
public class SelectedChannelTypeImpl implements SelectedChannelType {
    private FilterFactory filterFactory;

    //private Expression contrastEnhancement;
    private ContrastEnhancement contrastEnhancement;
    private String name = "channel";

    
    public SelectedChannelTypeImpl(){
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()));
    }

    public SelectedChannelTypeImpl(FilterFactory factory) {
        filterFactory = factory;
        contrastEnhancement = contrastEnhancement(filterFactory
                .literal(1.0));
    }
    public SelectedChannelTypeImpl(FilterFactory factory, ContrastEnhancement contrast ) {
        filterFactory = factory;
        contrastEnhancement = contrast;
    }

    public String getChannelName() {
        return name;
    }

    public ContrastEnhancement getContrastEnhancement() {
        return contrastEnhancement;
    }

    public void setChannelName(String name) {
        this.name = name;
    }

    public void setContrastEnhancement(ContrastEnhancement enhancement) {
        this.contrastEnhancement = enhancement;
    }

    public void setContrastEnhancement(Expression gammaValue) {
        contrastEnhancement.setGammaValue(gammaValue);
    }

    protected ContrastEnhancement contrastEnhancement(Expression expr) {
        ContrastEnhancement enhancement = new ContrastEnhancementImpl();
        enhancement.setGammaValue(filterFactory.literal(1.0));

        return enhancement;
    }

    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }

     public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }

}
