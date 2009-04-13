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
 * Created on 13 November 2002, 13:52
 */
package org.geotools.styling;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.style.ContrastMethod;
import org.opengis.style.StyleVisitor;


/**
 * The ContrastEnhancement object defines contrast enhancement for a channel of
 * a false-color image or for a color image. Its format is:
 * <pre>
 * &lt;xs:element name="ContrastEnhancement"&gt;
 *   &lt;xs:complexType&gt;
 *     &lt;xs:sequence&gt;
 *       &lt;xs:choice minOccurs="0"&gt;
 *         &lt;xs:element ref="sld:Normalize"/&gt;
 *         &lt;xs:element ref="sld:Histogram"/&gt;
 *       &lt;/xs:choice&gt;
 *       &lt;xs:element ref="sld:GammaValue" minOccurs="0"/&gt;
 *     &lt;/xs:sequence&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="Normalize"&gt;
 *   &lt;xs:complexType/&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="Histogram"&gt;
 *   &lt;xs:complexType/&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="GammaValue" type="xs:double"/&gt;
 * </pre>
 * In the case of a color image, the relative grayscale brightness of a pixel
 * color is used. ?Normalize? means to stretch the contrast so that the
 * dimmest color is stretched to black and the brightest color is stretched to
 * white, with all colors in between stretched out linearly. ?Histogram? means
 * to stretch the contrast based on a histogram of how many colors are at each
 * brightness level on input, with the goal of producing equal number of
 * pixels in the image at each brightness level on output.  This has the
 * effect of revealing many subtle ground features. A ?GammaValue? tells how
 * much to brighten (value greater than 1.0) or dim (value less than 1.0) an
 * image. The default GammaValue is 1.0 (no change). If none of Normalize,
 * Histogram, or GammaValue are selected in a ContrastEnhancement, then no
 * enhancement is performed.
 *
 * @author iant
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/styling/ContrastEnhancementImpl.java $
 */
public class ContrastEnhancementImpl implements ContrastEnhancement {
    private FilterFactory filterFactory;
    private Expression gamma;
    private Expression type;
    private final ContrastMethod method;

    public ContrastEnhancementImpl() {
        this( CommonFactoryFinder.getFilterFactory(null));
    }

    public ContrastEnhancementImpl(FilterFactory factory) {
        this(factory,null);
    }
    
    public ContrastEnhancementImpl(FilterFactory factory,ContrastMethod method) {
        filterFactory = factory;
        this.method = method;
    }

    public ContrastEnhancementImpl(org.opengis.style.ContrastEnhancement contrastEnhancement) {
        filterFactory = CommonFactoryFinder.getFilterFactory2(null);
        this.method = contrastEnhancement.getMethod();
        this.gamma = contrastEnhancement.getGammaValue();
    }

    public ContrastEnhancementImpl(FilterFactory2 factory, Expression gamma, ContrastMethod method) {
        this.filterFactory = factory;
        this.gamma = gamma;
        this.method = method;
        this.type = factory.literal( method.name() );
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
    }

    public Expression getGammaValue() {
        return gamma;
    }

    public Expression getType() {
        return type;
    }

    public void setGammaValue(Expression gamma) {
        this.gamma = gamma;
    }

    public void setHistogram() {
        type = filterFactory.literal("Histogram");
        // method = ContrastMethod.HISTOGRAM;
    }

    public void setNormalize() {
        type = filterFactory.literal("Normalize");
        // method = ContrastMethod.NORMALIZE;
    }

    public void setLogarithmic() {
        type = filterFactory.literal("Logarithmic");
        // method = ContrastMethod.NONE;
    }

    public void setExponential() {
        type = filterFactory.literal("Exponential");
    }

    public void setType(Expression type) {
        this.type = type;
    }

    public ContrastMethod getMethod() {
        return method;
    }
    
    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }

    public void accept(org.geotools.styling.StyleVisitor visitor) {
        visitor.visit(this);
    }
}
