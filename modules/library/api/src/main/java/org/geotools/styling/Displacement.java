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
 * A Displacement gives X and Y offset displacements to use for rendering a
 * text label near a point.
 *
 *
 * @author Ian Turton, CCG
 * @version $Id: Displacement.java 31133 2008-08-05 15:20:33Z johann.sorel $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/Displacement.java $
 */
public interface Displacement extends org.opengis.style.Displacement{
    /**
     * Default Displacment instance.
     */
    static final Displacement DEFAULT = new ConstantDisplacement() {
            private void cannotModifyConstant() {
                throw new UnsupportedOperationException("Constant Stroke may not be modified");
            }

            public Expression getDisplacementX() {
                return ConstantExpression.ZERO;
            }

            public Expression getDisplacementY() {
                return ConstantExpression.ZERO;
            }

            public Object accept(StyleVisitor visitor, Object extraData) {
                cannotModifyConstant();
                return null;
            }

        };

    /**
     * Null Displacement instance.
     */
    static final Displacement NULL = new ConstantDisplacement() {
            private void cannotModifyConstant() {
                throw new UnsupportedOperationException("Constant Stroke may not be modified");
            }

            public Expression getDisplacementX() {
                return ConstantExpression.NULL;
            }

            public Expression getDisplacementY() {
                return ConstantExpression.NULL;
            }

            public Object accept(StyleVisitor visitor, Object extraData) {
                cannotModifyConstant();
                return null;
            }
        };

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setDisplacementX(Expression x);

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setDisplacementY(Expression y);

    void accept(org.geotools.styling.StyleVisitor visitor);
}


abstract class ConstantDisplacement implements Displacement {
    private void cannotModifyConstant() {
        throw new UnsupportedOperationException("Constant Displacement may not be modified");
    }

    public void setDisplacementX(Expression x) {
        cannotModifyConstant();
    }

    public void setDisplacementY(Expression y) {
        cannotModifyConstant();
    }

    public void accept(org.geotools.styling.StyleVisitor visitor) {
        cannotModifyConstant();
    }
    
    public void accept(StyleVisitor visitor) {
        cannotModifyConstant();
    }
}
;
