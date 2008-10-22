/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 */package org.geotools.filter.visitor;

import java.util.ArrayList;
import java.util.List;

import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Or;

 /**
  * Takes a filter and returns a simplified, equivalent one.
  * At the moment the filter simplifies out {@link Filter#INCLUDE}
  * and {@link Filter#EXCLUDE} and deal with empty FID filters.
  * @author Andrea Aime - OpenGeo
  *
  */
public class SimplifyingFilterVisitor extends DuplicatingFilterVisitor {
    
    @Override
    public Object visit(And filter, Object extraData) {
        // scan, clone and simplify the children
        List<Filter> newChildren = new ArrayList<Filter>(filter.getChildren().size());
        for (Filter child : filter.getChildren()) {
            Filter cloned = (Filter) child.accept(this, extraData);
            
            // if any of the child filters is exclude, 
            // the whole chain of AND is equivalent to 
            // EXCLUDE
            if(cloned == Filter.EXCLUDE)
                return Filter.EXCLUDE;
            
            // these can be skipped
            if(cloned == Filter.INCLUDE)
                continue;
            
            newChildren.add(cloned);
        }
        
        // we might end up with an empty list
        if(newChildren.size() == 0)
            return Filter.INCLUDE;
        
        // remove the logic we have only one filter
        if(newChildren.size() == 1)
            return newChildren.get(0);
        
        // else return the cloned and simplified up list
        return getFactory(extraData).and(newChildren);
    }
    
    @Override
    public Object visit(Or filter, Object extraData) {
     // scan, clone and simplify the children
        List<Filter> newChildren = new ArrayList<Filter>(filter.getChildren().size());
        for (Filter child : filter.getChildren()) {
            Filter cloned = (Filter) child.accept(this, extraData);
            
            // if any of the child filters is include, 
            // the whole chain of OR is equivalent to 
            // INCLUDE
            if(cloned == Filter.INCLUDE)
                return Filter.INCLUDE;
            
            // these can be skipped
            if(cloned == Filter.EXCLUDE)
                continue;
            
            newChildren.add(cloned);
        }
        
        // we might end up with an empty list
        if(newChildren.size() == 0)
            return Filter.EXCLUDE;
        
        // remove the logic we have only one filter
        if(newChildren.size() == 1)
            return newChildren.get(0);
        
        // else return the cloned and simplified up list
        return getFactory(extraData).or(newChildren);
    }
    
    @Override
    public Object visit(Id filter, Object extraData) {
        // if the set of ID is empty, it's actually equivalent to Filter.EXCLUDE 
        if(filter.getIDs().size() == 0)
            return Filter.EXCLUDE;
        return super.visit(filter, extraData);
    }
}
