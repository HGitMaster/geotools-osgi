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
 */
package org.geotools.feature.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;


/**
 * Generates a list of unique values from a collection
 *
 * @author Cory Horner, Refractions
 *
 * @since 2.2.M2
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/main/src/main/java/org/geotools/feature/visitor/UniqueVisitor.java $
 */
public class UniqueVisitor implements FeatureCalc {
    private Expression expr;
    Set set = new HashSet();

    public UniqueVisitor(String attributeTypeName) {
        FilterFactory factory = CommonFactoryFinder.getFilterFactory(null);
        expr = factory.property(attributeTypeName);
    }

    public UniqueVisitor(int attributeTypeIndex, SimpleFeatureType type)
        throws IllegalFilterException {
        FilterFactory factory = CommonFactoryFinder.getFilterFactory(null);
        expr = factory.property(type.getDescriptor(attributeTypeIndex).getLocalName());
    }

    public UniqueVisitor(String attrName, SimpleFeatureType type)
        throws IllegalFilterException {
        FilterFactory factory = CommonFactoryFinder.getFilterFactory(null);
        expr = factory.property(type.getDescriptor(attrName).getLocalName());
    }

    public UniqueVisitor(Expression expr) {
        this.expr = expr;
    }

    public void init(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
    	//do nothing
    }
    
    public void visit(SimpleFeature feature) {
        visit(feature);
    }
    public void visit(Feature feature) {
        //we ignore null attributes
        Object value = expr.evaluate(feature);
        if (value != null) {
        	set.add(value);
        }
    }

    public Expression getExpression() {
        return expr;
    }

    public Set getUnique() {
        /**
         * Return a list of unique values from the collection
         */
        return set;
    }

    public void setValue(Object newSet) {
    	if (newSet instanceof ArrayList) { //convert to set
    		ArrayList newList = (ArrayList) newSet;
    		Set anotherSet = new HashSet();
    		for (ListIterator i = newList.listIterator(); i.hasNext();) {
        		anotherSet.add(i.next());
    		}
    		this.set = anotherSet;
    	} else {
        	this.set = (Set) newSet;
    	}
    }
    
    public void reset() {
        /**
         * Reset the unique and current minimum for the features in the
         * collection
         */
        this.set = new HashSet();
    }

    public CalcResult getResult() {
        if (set.size() < 1) {
            return null;
        }
        return new UniqueResult(set);
    }

    public static class UniqueResult extends AbstractCalcResult {
        private Set unique;

        public UniqueResult(Set newSet) {
            unique = newSet;
        }

        public Object getValue() {
        	return new HashSet(unique);
        }
        
        public boolean isCompatible(CalcResult targetResults) {
            //list each calculation result which can merge with this type of result
        	if (targetResults instanceof UniqueResult) return true;
        	return false;
        }

        public CalcResult merge(CalcResult resultsToAdd) {
            if (!isCompatible(resultsToAdd)) {
                throw new IllegalArgumentException(
                    "Parameter is not a compatible type");
            }

            if (resultsToAdd instanceof UniqueResult) {
            	//add one set to the other (to create one big unique list)
            	Set newSet = new HashSet(unique);
                newSet.addAll((Set) resultsToAdd.getValue());
                return new UniqueResult(newSet);
            } else {
            	throw new IllegalArgumentException(
				"The CalcResults claim to be compatible, but the appropriate merge method has not been implemented.");
            }
        }
    }
}

