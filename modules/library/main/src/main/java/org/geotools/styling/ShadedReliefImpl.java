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
 *
 * Created on 13 November 2002, 13:59
 */
package org.geotools.styling;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.StyleVisitor;

/**
 * Default implementation of ShadedRelief.
 *
 * @author iant
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/ShadedReliefImpl.java $
 */
public class ShadedReliefImpl implements ShadedRelief {
    private FilterFactory filterFactory;
    private Expression reliefFactor;
    private boolean brightness = false;

    public ShadedReliefImpl(){
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints()));
    }

    public ShadedReliefImpl(FilterFactory factory) {
        filterFactory = factory;
        reliefFactor = filterFactory.literal(55);
    }

    /**
     * The ReliefFactor gives the amount of exaggeration to use for the height
     * of the ?hills.?  A value of around 55 (times) gives reasonable results
     * for Earth-based DEMs. The default value is system-dependent.
     *
     * @return an expression which evaluates to a double.
     */
    public Expression getReliefFactor() {
        return reliefFactor;
    }

    /**
     * indicates if brightnessOnly is true or false. Default is false.
     *
     * @return boolean brightnessOn.
     */
    public boolean isBrightnessOnly() {
        return brightness;
    }

    /**
     * turns brightnessOnly on or off depending on value of flag.
     *
     * @param flag boolean
     */
    public void setBrightnessOnly(boolean flag) {
        brightness = flag;
    }

    /**
     * The ReliefFactor gives the amount of exaggeration to use for the height
     * of the ?hills.?  A value of around 55 (times) gives reasonable results
     * for Earth-based DEMs. The default value is system-dependent.
     *
     * @param reliefFactor an expression which evaluates to a double.
     */
    public void setReliefFactor(Expression reliefFactor) {
        this.reliefFactor = reliefFactor;
    }

    public Object accept(StyleVisitor visitor,Object data) {
	return visitor.visit(this,data);
    }
    
    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }
}
