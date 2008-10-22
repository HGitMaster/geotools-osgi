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
package org.geotools.repository;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.util.ProgressListener;


/**
 * Abstract implementation of GeoResource.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/AbstractGeoResource.java $
 */
public abstract class AbstractGeoResource implements GeoResource {
    
	/**
	 * logger
	 */
	protected static Logger logger = org.geotools.util.logging.Logging.getLogger( "org.geotools.catalog" );
	
	/**
	 * Error message
	 */
	private Throwable msg;
	
	/**
     * This method is shorthand for
     * <pre>
     * 	<code>
     * 		return (Service) resolve(Service.class, monitor);
     * 	</code>
     * </pre>
     *
     * @param monitor DOCUMENT ME!
     *
     * @return The service containg the resource, an object of type Service.
     *
     * @throws IOException DOCUMENT ME!
     *
     * @see AbstractGeoResource#resolve(Class, ProgressListener)
     */
    public Resolve parent(ProgressListener monitor) throws IOException {
        return (Service) resolve(Service.class, monitor);
    }

    /**
     * return null ... almost always a leaf
     *
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.ProgressListener)
     */
    public List members(ProgressListener monitor) {
        return null;
    }
    
    /**
     * @return The cached error message.
     */
    public Throwable getMessage() {
    		return msg;
    }
    
    /**
     * Sets the cached error message.
     * 
     * @param msg An exception which occured when connecting to the service.
     */
    protected void setMessage( Throwable msg ) {
    		this.msg = msg;
    }
    
    /**
     * This should represent the identifier
     *
     * @param other
     *
     *
     * @see Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if ((other != null) && other instanceof GeoResource) {
            GeoResource resource = (GeoResource) other;

            if ((getIdentifier() != null) && (resource.getIdentifier() != null)) {
                return getIdentifier().equals(resource.getIdentifier());
            }
        }

        return false;
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param listener DOCUMENT ME!
     */
    public void addListener(ResolveChangeListener listener) {
        // do nothing
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param listener DOCUMENT ME!
     */
    public void removeListener(ResolveChangeListener listener) {
        // do nothing
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param event DOCUMENT ME!
     */
    public void fire(ResolveChangeEvent event) {
        // do nothing
    }

    /**
     * This should represent the identified
     *
     *
     * @see Object#hashCode()
     */
    public int hashCode() {
        if (getIdentifier() != null) {
            return getIdentifier().hashCode();
        }

        return super.hashCode();
    }

    /**
     * Non blocking label used by LabelProvider. public static final String
     * getGenericLabel(IGeoResource resource){ assert resource.getIdentifier()
     * != null; return resource==null ||
     * resource.getIdentifier()==null?"Resource":resource.getIdentifier().toString();
     * }
     *
     * @return DOCUMENT ME!
     */
    /**
     * Non blocking icon used by LabelProvider. public static final
     * ImageDescriptor getGenericIcon(IGeoResource resource){ if(resource
     * !=null){ assert resource.getIdentifier() != null;
     * if(resource.canResolve(FeatureSource.class)){ // default feature return
     * Images.getDescriptor(ISharedImages.FEATURE_OBJ); }
     * if(resource.canResolve(GridCoverage.class)){ // default raster return
     * Images.getDescriptor(ISharedImages.GRID_OBJ); }} return
     * Images.getDescriptor(ISharedImages.RESOURCE_OBJ); }
     *
     * @return DOCUMENT ME!
     */
    /**
     * Indicate class and id.
     *
     * @return string representing this IResolve
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        String classname = getClass().getName();
        String name = classname.substring(classname.lastIndexOf('.') + 1);
        buf.append(name);
        buf.append("("); //$NON-NLS-1$
        buf.append(getIdentifier());
        buf.append(")"); //$NON-NLS-1$

        return buf.toString();
    }
}
