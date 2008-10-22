/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.impl;

import org.geotools.process.ProcessFactory;

/**
 * Super class that provides additional helper methods
 * useful when implementing your own ProcessFactory.
 *
 * @author gdavis
 */
public abstract class AbstractProcessFactory implements ProcessFactory {

    public String getName() {
        String factoryName = this.getClass().getSimpleName();
        if( factoryName.endsWith("Factory")){
            return factoryName.substring(0, factoryName.length()-7);
        }
        return factoryName;
    }
    
}
