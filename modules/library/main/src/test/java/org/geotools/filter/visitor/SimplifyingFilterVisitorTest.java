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
package org.geotools.filter.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.SimplifyingFilterVisitor.FIDValidator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.identity.Identifier;

public class SimplifyingFilterVisitorTest extends TestCase {
    
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    Id emptyFid;
    SimplifyingFilterVisitor visitor;
    PropertyIsEqualTo property;
    
    @Override
    protected void setUp() throws Exception {
        emptyFid = ff.id(new HashSet<Identifier>());
        property = ff.equal(ff.property("test"), ff.literal("oneTwoThree"), false);        
        visitor = new SimplifyingFilterVisitor();
    }

    public void testIncludeAndInclude() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, Filter.INCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testIncludeAndExclude() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testExcludeAndExclude() {
        Filter result = (Filter) ff.and(Filter.EXCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testIncludeAndProperty() {
        Filter result = (Filter) ff.and(Filter.INCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testExcludeAndProperty() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testIncludeOrInclude() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, Filter.INCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testIncludeOrExclude() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testExcludeOrExclude() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, Filter.EXCLUDE).accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);
    }
    
    public void testIncludeOrProperty() {
        Filter result = (Filter) ff.or(Filter.INCLUDE, property).accept(visitor, null);
        assertEquals(Filter.INCLUDE, result);
    }
    
    public void testExcludeOrProperty() {
        Filter result = (Filter) ff.or(Filter.EXCLUDE, property).accept(visitor, null);
        assertEquals(property, result);
    }
    
    public void testEmptyFid() {
        Filter result = (Filter) emptyFid.accept(visitor, null);
        assertEquals(Filter.EXCLUDE, result);    
    }
    
    public void testRecurseAnd() {
        Filter test = ff.and(Filter.INCLUDE, ff.or(property, Filter.EXCLUDE));
        assertEquals(property, test.accept(visitor, null));
    }
    
    public void testRecurseOr() {
        Filter test = ff.or(Filter.EXCLUDE, ff.and(property, Filter.INCLUDE));
        assertEquals(property, test.accept(visitor, null));
    }
    
    public void testFidValidity() {
        visitor.setFIDValidator(new SimplifyingFilterVisitor.FIDValidator() {
            public boolean isValid(String fid) {
                return fid.startsWith("pass");
            }
        });

        Set<Identifier> ids = new HashSet<Identifier>();
        ids.add(ff.featureId("notPass"));
        Id filter = ff.id(ids);

        assertEquals(Filter.EXCLUDE, filter.accept(visitor, null));

        ids.add(ff.featureId("pass1"));
        ids.add(ff.featureId("pass2"));
        filter = ff.id(ids);

        Set<Identifier> validIds = new HashSet<Identifier>();
        validIds.add(ff.featureId("pass2"));
        validIds.add(ff.featureId("pass1"));
        Filter expected = ff.id(validIds);
        assertEquals(expected, filter.accept(visitor, null));
    }

    public void testRegExFIDValidator() {
        FIDValidator validator = new SimplifyingFilterVisitor.RegExFIDValidator("abc\\.\\d+");
        visitor.setFIDValidator(validator);

        Set<Identifier> ids = new HashSet<Identifier>();
        ids.add(ff.featureId("abc.."));
        ids.add(ff.featureId(".abc.1"));
        ids.add(ff.featureId("abc.123"));
        ids.add(ff.featureId("abc.ax"));
        Id filter = ff.id(ids);
        Filter result = (Filter) filter.accept(visitor, null);
        Filter expected = ff.id(Collections.singleton(ff.featureId("abc.123")));
        
        assertEquals(expected, result);
    }
    
    public void testTypeNameDotNumberValidator() {
        final String typeName = "states";
        FIDValidator validator;
        validator = new SimplifyingFilterVisitor.TypeNameDotNumberFidValidator(typeName);
        visitor.setFIDValidator(validator);

        Set<Identifier> ids = new HashSet<Identifier>();
        ids.add(ff.featureId("_states"));
        ids.add(ff.featureId("states.abc"));
        ids.add(ff.featureId("states.."));
        ids.add(ff.featureId("states.123"));
        Id filter = ff.id(ids);
        Filter result = (Filter) filter.accept(visitor, null);
        Filter expected = ff.id(Collections.singleton(ff.featureId("states.123")));
        
        assertEquals(expected, result);
    }
    
    public void testSingleNegation() {
    	Filter f = ff.not(ff.equals(ff.property("prop"), ff.literal(10)));
    	Filter result = (Filter) f.accept(visitor, null);
    	assertEquals(f, result);
    }
    
    public void testDoubleNegation() {
    	PropertyIsEqualTo equal = ff.equals(ff.property("prop"), ff.literal(10));
		Filter f = ff.not(ff.not(equal));
    	Filter result = (Filter) f.accept(visitor, null);
    	assertEquals(equal, result);
    }
    
    public void testTripleNegation() {
    	PropertyIsEqualTo equal = ff.equals(ff.property("prop"), ff.literal(10));
		Filter f = ff.not(ff.not(ff.not(equal)));
    	Filter result = (Filter) f.accept(visitor, null);
    	assertEquals(ff.not(equal), result);
    }
}
