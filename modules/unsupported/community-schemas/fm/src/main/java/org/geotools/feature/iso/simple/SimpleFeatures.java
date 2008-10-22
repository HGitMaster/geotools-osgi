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
 */

package org.geotools.feature.iso.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geotools.feature.iso.Types;
import org.geotools.util.GTContainer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Convenience class for working with or implementing with simple features.
 * <p>
 * Method that involve creation must be called in a non-static context
 * and only after the class has been injected with a 
 * {@link org.geotools.feature.simple.SimpleFeatureBuilder}
 * </p>
 * FIXME Inject with factory not builder!
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class SimpleFeatures {

	//
	// FIXME This code moved from FeatureTypes shares flaw of GTContainer use
	// 
    final private static SimpleFeatureType emptySimpleFeatureType;
    final private static SimpleFeatureCollectionType emptySimpleFeatureCollectionType;  
    static {
    	DefaultPicoContainer container = GTContainer.simple();
    	SimpleTypeFactory factory = 
    		(SimpleTypeFactory) container.getComponentInstanceOfType(SimpleTypeFactory.class);

    	emptySimpleFeatureType = factory.createSimpleFeatureType(
        		Types.typeName("EmptySimpleFeatureType"), Collections.EMPTY_LIST, null,
        		null,Collections.EMPTY_SET, null
        	);
    	emptySimpleFeatureCollectionType = factory.createSimpleFeatureCollectionType(
			Types.typeName("EmptySimpleFeatureCollectionType"), emptySimpleFeatureType,  null );
    }
    /**
     * Injected factory
     */
	SimpleFeatureFactory factory;
    
    /**
     * Builder used to create simple features.
     */
    SimpleFeatureBuilder builder;
    
    /**
     * Creates a new instance satisfying the factory dependency.
     * 
     * @param builder
     */
    public SimpleFeatures(SimpleFeatureFactory factory) {
        this.factory = factory;
        builder = new SimpleFeatureBuilder(factory);
    }
  
    public void setSimpleFeatureFactory( SimpleFeatureFactory factory ){
    	this.factory = factory;
    	builder.setSimpleFeatureFactory( factory );
    }
    /**
     * Creates a new simple feature from a type and array of values.
     * <p>
     * The order of items in <code>values</code> must correspond to the 
     * order of attributes defined in <code>type</code>
     * </p>
     * @param type The simple feature type.
     * @param values The values of the created feature.
     * @param fid The feature id, may be null.
     * 
     * @return The created feature.
     */
    public SimpleFeature create(SimpleFeatureType type, Object[] values, String fid) {
        return create(type,Arrays.asList(values),fid);
    }

    
    /**
     * Creates a new simple feature from a type and list of values.
     * <p>
     * The order of items in <code>values</code> must correspond to the 
     * order of attributes defined in <code>type</code>
     * </p>
     * @param type The simple feature type.
     * @param values The values of the created feature.
     * @param fid The feature id, may be null.
     * 
     * @return The created feature.
     */
    public SimpleFeature create(SimpleFeatureType type, List values, String fid) {
        builder.init();
        builder.setType(type);
        
        for (int i = 0; i < values.size(); i++) {
            builder.add(values.get(i));
        }        
        return builder.feature(fid);
    }
 
    /**
     * Copies the values from one feature to another.
     * 
     * @param source The feature being copied from. 
     * @param target The feature being copied to.
      
     */
    public static void copy(SimpleFeature source, SimpleFeature target) {
        target.setValue(source.getValue());
        target.setDefaultGeometry(source.getDefaultGeometry());
        target.setCRS(source.getCRS());
    }
}
