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
package org.geotools.renderer.crs;

import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * Builds {@link ProjectionHandler} instances
 * WARNING: this API is not finalized and is meant to be used by StreamingRenderer only
 * 
 * @author Andrea Aime - OpenGeo
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/render/src/main/java/org/geotools/renderer/crs/ProjectionHandlerFactory.java $
 */
public interface ProjectionHandlerFactory {

    /**
     * Returns an handler capable of dealing with the specified envelope, or null if this factory
     * cannot create one
     * 
     * @param renderingEnvelope
     * @param wrap enables map wrapping, if it's possible and makes sense for this projection
     * @return
     */
    ProjectionHandler getHandler(ReferencedEnvelope renderingEnvelope, boolean wrap);
}
