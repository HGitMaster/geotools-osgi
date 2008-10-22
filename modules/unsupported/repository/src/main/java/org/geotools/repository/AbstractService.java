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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.util.ProgressListener;


/**
 * Abstract implementation of Service.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/AbstractService.java $
 */
public abstract class AbstractService implements Service {
	/**
	 * Logger
	 */
	protected static Logger logger = org.geotools.util.logging.Logging.getLogger( "org.geotools.catalog" );
	
    /** 
     * parent catalog containing the service 
     */
    private Catalog parent;
    /** 
     * connection paramters 
     */
    private Map params;
    /** 
     * error message 
     */
    private Throwable msg;
    /**
     * cached geo resource members 
     */
    private List members;
    
    /**
     * Creates a new service handle contained within a catalog.
     *
     * @param parent The catalog containg the service.
     */
    public AbstractService(Catalog parent) {
        this.parent = parent;
    }
    
    /**
     * Creates a new service handle contained within a catalog, with a 
     * set of connection paramters.
     * 
     * @param parent The catalog containing the service.
     * @param params The connection params used to connect to the service.
     */
    public AbstractService( Catalog parent, Map params ) {
    		this( parent );
    		this.params = params;
    }

    /**
     * @param monitor Progress monitor for blocking call.
     *
     * @return he parent Catalog.
     */
    public Resolve parent(ProgressListener monitor) {
        return parent;
    }
    
    /**
     * @return Connection parameters, possibly null.
     */
    public Map getConnectionParams() {
    		return params;
    }
    
    /**
     * Sets the connection params for the service handle.
     * 
     * @param params Map of connection paramters.
     */
    protected void setConnectionParams( Map params ){
    		this.params = params;
    }

    /**
     * Sets the cached value of the members of the service.
     * 
     * @param members List of {@link GeoResource}.
     */
    protected void setMembers( List members ) {
    		this.members = members;
    }
    
    /**
     * @return The cached members.
     */
    protected List getMembers() {
    		return members;
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
     * Default implementation of getStatus.
     * <p>
     * The following rules are used to determine the status:
     * <ol>
     * 	<li>If {@link #msg} is non-null, then the service handle is 
     * {@link Status#BROKEN}.
     * 	<li>If above is false, then if {@link #members} is non-null then the 
     * service handle is {@link Status#CONNECTED} 
     *  <li>If non of the above hold, the service handle is {@link Status#NOTCONNECTED}.
     * </ol>
     * </p>
     * 
     * <p>
     * Subclasses can control this method by setting the members {@link #msg} 
     * and {@link #members} with {@link #setMessage(Throwable)} and 
     * {@link #setMembers(List)} respectivley. Or subclasses may wish to 
     * override this method entirley.
     * </p>
     *
     */
    public Status getStatus() {
    		if ( msg != null ) {
    			return Status.BROKEN;
    		}
    		
		if ( members != null ) {
			return Status.CONNECTED;
		}
    		
    		return Status.NOTCONNECTED;
    }
    
    
    /**
     * This should represent the identifier
     *
     * @param other
     *
     *
     * @see Object#equals(java.lang.Object)
     */
    public final boolean equals(Object other) {
        if ((other != null) && other instanceof Service) {
            Service service = (Service) other;

            if ((getIdentifier() != null) && (service.getIdentifier() != null)) {
                return getIdentifier().equals(service.getIdentifier());
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
    public final int hashCode() {
        if (getIdentifier() != null) {
            return getIdentifier().hashCode();
        }

        return super.hashCode();
    }

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
