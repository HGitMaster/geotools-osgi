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

import org.geotools.filter.ConstantExpression;
import org.opengis.filter.expression.Expression;
import org.opengis.style.StyleVisitor;


/**
 * An AnchorPoint identifies the location inside a textlabel to use as an
 * "anchor" for positioning it relative to a point geometry.
 *
 * @author Ian Turton
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/AnchorPoint.java $
 * @version $Id: AnchorPoint.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public interface AnchorPoint extends org.opengis.style.AnchorPoint{

    static final AnchorPoint DEFAULT = new AnchorPoint() {
        private void cannotModifyConstant() {
            throw new UnsupportedOperationException("Constant Stroke may not be modified");
        }
        
        public void setAnchorPointX(Expression x) {
            cannotModifyConstant();
        }

        public void setAnchorPointY(Expression y) {
            cannotModifyConstant();
        }

        public void accept(org.geotools.styling.StyleVisitor visitor) {
            cannotModifyConstant();
        }
        
        public Object accept(org.opengis.style.StyleVisitor visitor, Object data) {
            cannotModifyConstant();
            return null;
        }

        public Expression getAnchorPointX() {
            return ConstantExpression.constant(0.5);
        }

        public Expression getAnchorPointY() {
            return ConstantExpression.constant(0.5);
        }
    };
    
    /**
     * set the X coordinate for the anchor point
     *
     * @param x an expression which represents the X coordinate
     * 
     * @deprecated symbolizers and underneath classes are immutable .
     */
    @Deprecated
    void setAnchorPointX(Expression x);

    /**
     * set the Y coordinate for the anchor point
     *
     * @param y an expression which represents the Y coordinate
     * 
     * @deprecated symbolizers and underneath classes are immutable .
     */
    @Deprecated
    void setAnchorPointY(Expression y);

    /**
     * calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    void accept(org.geotools.styling.StyleVisitor visitor);

}
