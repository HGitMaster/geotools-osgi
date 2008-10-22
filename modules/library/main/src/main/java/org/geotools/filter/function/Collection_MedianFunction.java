/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
 * Created on May 11, 2005, 6:21 PM
 */
package org.geotools.filter.function;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.MedianVisitor;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;


/**
 * Calculates the median value of an attribute for a given FeatureCollection
 * and Expression.
 * 
 * @author Cory Horner
 * @since 2.2M2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/filter/function/Collection_MedianFunction.java $
 */
public class Collection_MedianFunction extends FunctionExpressionImpl
    implements FunctionExpression {
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.filter.function");
    FeatureCollection<? extends FeatureType, ? extends Feature> previousFeatureCollection = null;
    Object median = null;

    /**
     * Creates a new instance of Collection_MedianFunction
     */
    public Collection_MedianFunction() {
        super("Collection_Median");
    }

    public int getArgCount() {
        return 1;
    }

    /**
     * Calculate median (using FeatureCalc) - only one parameter is used.
     *
     * @param collection collection to calculate the median
     * @param expression Single Expression argument
     *
     * @return An object containing the median value of the attributes
     *
     * @throws IllegalFilterException
     * @throws IOException 
     */
    static CalcResult calculateMedian(FeatureCollection<? extends FeatureType, ? extends Feature> collection,
        Expression expression) throws IllegalFilterException, IOException {
        MedianVisitor medianVisitor = new MedianVisitor(expression);
        collection.accepts(medianVisitor, null);

        return medianVisitor.getResult();
    }

    /**
     * The provided arguments are evaulated with respect to the
     * FeatureCollection.
     * 
     * <p>
     * For an aggregate function (like median) please use the WFS mandated XPath
     * syntax to refer to featureMember content.
     * </p>
     * 
     * <p>
     * To refer to all 'X': <code>featureMember/asterisk/X</code>
     * </p>
     *
     * @param args DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setParameters(List args) {
        super.setParameters(args);

        Expression expr = (Expression) getExpression(0);

        // if we see "featureMembers/*/ATTRIBUTE" change to "ATTRIBUTE"
        expr.accept(new AbstractFilterVisitor() {
                public void visit(AttributeExpression expression) {
                    String xpath = expression.getAttributePath();

                    if (xpath.startsWith("featureMembers/*/")) {
                        xpath = xpath.substring(17);
                    } else if (xpath.startsWith("featureMember/*/")) {
                        xpath = xpath.substring(16);
                    }

                    try {
                        expression.setAttributePath(xpath);
                    } catch (IllegalFilterException e) {
                        // ignore
                    }
                }
            });
    }

    public Object evaluate(Object feature) {
		if (feature == null) {
			return new Integer(0); // no features were visited in the making of this answer
		}
                Expression expr = (Expression) getExpression(0);
		FeatureCollection<? extends FeatureType, ? extends Feature> featureCollection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) feature;
		synchronized (featureCollection) {
			if (featureCollection != previousFeatureCollection) {
				previousFeatureCollection = featureCollection;
				median = null;
				try {
					CalcResult result = calculateMedian(featureCollection, expr);
					if (result != null) {
						median = result.getValue();
					}
				} catch (IllegalFilterException e) {
					LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					LOGGER.log(Level.FINER, e.getLocalizedMessage(), e);
				}
			}
		}
		return median;
    }

    public void setExpression(Expression e) {
        setParameters(Collections.singletonList(e));
    }
}
