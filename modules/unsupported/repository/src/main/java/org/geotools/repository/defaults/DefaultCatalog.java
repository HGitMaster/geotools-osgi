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
package org.geotools.repository.defaults;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geotools.repository.AbstractCatalog;
import org.geotools.repository.Catalog;
import org.geotools.repository.CatalogInfo;
import org.geotools.repository.GeoResource;
import org.geotools.repository.GeoResourceInfo;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveChangeListener;
import org.geotools.repository.ResolveDelta;
import org.geotools.repository.Service;
import org.geotools.repository.ServiceInfo;
import org.geotools.util.ListenerList;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Default Catalog implementation. All services are stored in memory.
 * 
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, jdeolive@openplans.org
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/defaults/DefaultCatalog.java $
 */
public class DefaultCatalog extends AbstractCatalog {
    private HashSet services = new HashSet();
    private CatalogInfo metadata;
    private ListenerList catalogListeners;

    public DefaultCatalog() {
        DefaultCatalogInfo metadata = new DefaultCatalogInfo(){};
        metadata.setTitle("Default Catalog"); //$NON-NLS-1$
        try {
			metadata.setSource(new URI("http://localhost"));
		} 
        catch (URISyntaxException e) {
        	//do nothing
		} 
        
        this.metadata = metadata;
        catalogListeners = new ListenerList();
    }

    public DefaultCatalog( CatalogInfo metadata ) {
        this();
        this.metadata = metadata;
    }

    /**
     * @see net.refractions.udig.catalog.ICatalog#addCatalogListener(net.refractions.udig.catalog.ICatalog.ICatalogListener)
     * @param listener
     */
    public void addCatalogListener( ResolveChangeListener listener ) {
        catalogListeners.add(listener);
    }

    /**
     * @see net.refractions.udig.catalog.ICatalog#removeCatalogListener(net.refractions.udig.catalog.ICatalog.ICatalogListener)
     * @param listener
     */
    public void removeCatalogListener( ResolveChangeListener listener ) {
        catalogListeners.remove(listener);
    }
    
    /**
     * @see net.refractions.udig.catalog.ICatalog#add(net.refractions.udig.catalog.IService)
     * @param entry
     * @throws UnsupportedOperationException
     */
    public void add( Service entry ) throws UnsupportedOperationException {
        if (entry == null || entry.getIdentifier() == null)
            throw new NullPointerException("Cannot have a null id"); //$NON-NLS-1$
        services.add(entry);
        ResolveDelta deltaAdded = new DefaultResolveDelta(entry, ResolveDelta.Kind.ADDED);
        ResolveDelta deltaChanged = new DefaultResolveDelta(this, Collections.singletonList(deltaAdded));
        fire(new DefaultResolveChangeEvent(DefaultCatalog.this, ResolveChangeEvent.Type.POST_CHANGE,
                deltaChanged));
    }

    /**
     * @see net.refractions.udig.catalog.ICatalog#remove(net.refractions.udig.catalog.IService)
     * @param entry
     * @throws UnsupportedOperationException
     */
    public void remove( Service entry ) throws UnsupportedOperationException {
        if (entry == null || entry.getIdentifier() == null)
            throw new NullPointerException("Cannot have a null id"); //$NON-NLS-1$
        ResolveDelta deltaRemoved = new DefaultResolveDelta(entry, ResolveDelta.Kind.REMOVED);
        ResolveDelta deltaChanged = new DefaultResolveDelta(this, Collections.singletonList(deltaRemoved));
        fire(new DefaultResolveChangeEvent(DefaultCatalog.this, ResolveChangeEvent.Type.PRE_DELETE,
                deltaChanged));
        services.remove(entry);
        fire(new DefaultResolveChangeEvent(DefaultCatalog.this, ResolveChangeEvent.Type.POST_CHANGE,
                deltaRemoved));
    }

    public void replace( URI id, Service entry ) throws UnsupportedOperationException {
        if (entry == null || entry.getIdentifier() == null || id == null)
            throw new NullPointerException("Cannot have a null id"); //$NON-NLS-1$

        List victims = findService(id,null);

        List changes = new ArrayList();
        for( Iterator vitr = victims.iterator(); vitr.hasNext(); ) {
        		Service service = (Service) vitr.next();
                List childChanges = new ArrayList();
                try {
                	List newChildren = entry.members(null);
                	List oldChildren = service.members(null);
                    if( oldChildren!=null )
                    for(  Iterator oitr = oldChildren.iterator(); oitr.hasNext(); ) {
                    	GeoResource oldChild = (GeoResource)oitr.next();
                        String oldName = oldChild.getIdentifier().toString();
                        for( Iterator citr = newChildren.iterator(); citr.hasNext();) {
                        	GeoResource child = (GeoResource)citr.next();
                            String name = child.getIdentifier().toString();
                            if (oldName.equals(name)) {
                                childChanges.add(new DefaultResolveDelta(child, oldChild,
                                        ResolveDelta.NO_CHILDREN));
                                break;
                            }
                        }
                    }
                } catch (IOException ignore) {
                    // no children? Not a very good entry ..
                }
                changes.add(new DefaultResolveDelta(service, entry, childChanges));
        }
        ResolveDelta deltas = new DefaultResolveDelta(this, changes);
        ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                ResolveChangeEvent.Type.PRE_DELETE, deltas);
        fire(event);

        services.removeAll(victims);
        services.add(entry);
    }

    /**
     * Quick search by url match.
     * @param query
     * 
     * @see net.refractions.udig.catalog.ICatalog#search(org.geotools.filter.Filter)
     * @return List<IResolve>
     * @throws IOException
     */
    public List find( URI query, ProgressListener monitor ) {
        List list = new ArrayList();
        try {
			URL qurl = query.toURL();
			for( Iterator itr = services.iterator(); itr.hasNext(); ) {
				Service service = (Service)itr.next();
				URL url = service.getIdentifier().toURL();
			    if (url.getProtocol().equalsIgnoreCase(qurl.getProtocol())
			            && ((url.getHost() == null || "".equals(url.getHost())) || (url.getHost() != null && url.getHost().equalsIgnoreCase(qurl.getHost()))) && //$NON-NLS-1$
			            ((url.getPath() == null || "".equals(url.getPath())) || (url.getPath() != null && url.getPath().equalsIgnoreCase(qurl.getPath()))) && //$NON-NLS-1$
			            ((url.getQuery() == null || "".equals(url.getQuery())) || (url.getQuery() != null && url.getQuery().equalsIgnoreCase(qurl.getQuery()))) && //$NON-NLS-1$
			            ((url.getAuthority() == null || "".equals(url.getAuthority())) || (url.getAuthority() != null && url.getAuthority().equalsIgnoreCase(qurl.getAuthority())))) { //$NON-NLS-1$
			        Iterator i;
			    	if( qurl.getRef()==null ){
			    		list.add(service);
			    	}else
			        try {
			        	
			            /*
			             * Although the following is a 'blocking' call, we have deemed it safe based on
			             * the following reasons: This will only be called for Identifiers which are
			             * well known. The Services being checked have already been screened, and only a
			             * limited number of services (ussually 1) will be called. 1) The Id was aquired
			             * from the catalog ... and this is a look-up, in which case the uri exists. 2)
			             * The Id was persisted. In the future this will also be free, as we plan on
			             * caching the equivalent of a getCapabilities document between runs (will have
			             * to be updated too as the app has time).
			             */
			            List t = service.members(monitor);
			            i = t == null ? null : t.iterator();
			            if (qurl.getRef() != null) {
			                // it's a resource
			                while( i.hasNext() ) {
			                    GeoResource res = (GeoResource) i.next();
			                    if (qurl.getRef().equals(res.getIdentifier().toURL().getRef()))
			                        list.add(res);
			                }
			            } else {
			                while( i != null && i.hasNext() ) {
			                    list.add(i.next());
			                }
			            }
			            
			        } catch (IOException e) {
			        	e.printStackTrace();
			        	//TODO: log
			        }

			    }
			}
		} 
        catch (MalformedURLException e) {
        	e.printStackTrace();
        	//TODO: log
		}
        return list;
    }

    /**
     * Quick search by url match.
     * 
     * @see net.refractions.udig.catalog.ICatalog#search(org.geotools.filter.Filter)
     * @param query
     * @return List<IResolve>
     * @throws IOException
     */
    public List findService( URI query, ProgressListener monitor ) {
    	
    	
    	
        List list = new ArrayList();
        
        try {
			URL qurl = query.toURL();
			if( qurl.getRef()!=null )
				return list;
			for( Iterator itr = services.iterator(); itr.hasNext();) {
				Service service = (Service) itr.next();
				
				URL url = service.getIdentifier().toURL();
				if (url.getProtocol().equalsIgnoreCase(qurl.getProtocol())
				        && ((url.getHost() == null || "".equals(url.getHost())) || (url.getHost() != null && url.getHost().equalsIgnoreCase(qurl.getHost()))) && //$NON-NLS-1$
				        ((url.getPath() == null || "".equals(url.getPath())) || (url.getPath() != null && url.getPath().equalsIgnoreCase(qurl.getPath()))) && //$NON-NLS-1$
				        ((url.getQuery() == null || "".equals(url.getQuery())) || (url.getQuery() != null && url.getQuery().equalsIgnoreCase(qurl.getQuery()))) && //$NON-NLS-1$
				        ((url.getAuthority() == null || "".equals(url.getAuthority())) || (url.getAuthority() != null && url.getAuthority().equalsIgnoreCase(qurl.getAuthority())))) { //$NON-NLS-1$
				            list.add(service);
				}
				
			}
		} 
        catch (MalformedURLException e) {
        	//TODO: log this
        	e.printStackTrace();
		}
        return list;
    }

    /**
     * Performs a search on this catalog based on the specified inputs. The pattern uses the
     * following conventions: use " " to surround a phase use + to represent 'AND' use - to
     * represent 'OR' use ! to represent 'NOT' use ( ) to designate scope The bbox provided shall be
     * in Lat - Long, or null if the search is not to be contained within a specified area.
     * 
     * @see net.refractions.udig.catalog.ICatalog#search(java.lang.String,
     *      com.vividsolutions.jts.geom.Envelope)
     * @param pattern
     * @param bbox used for an intersection test
     */
    public synchronized List search( String pattern, Envelope bbox,
            ProgressListener monitor ) {
    	
        if ((pattern == null || "".equals(pattern.trim()))
        		&& (bbox==null || bbox.isNull())) //$NON-NLS-1$
            return new LinkedList();

        AST ast=null;
        if ( pattern!=null && !"".equals(pattern.trim()))
        ast= ASTFactory.parse(pattern);

        // TODO check cuncurrency issues here

        List result = new LinkedList();
        HashSet tmp = new HashSet();
        tmp.addAll(this.services);
        try{
        	
        Iterator services = tmp.iterator();
        if (services != null) {
            while( services.hasNext() ) {
                Service service = (Service) services.next();
                if (check(service, ast)) {
                    result.add(service);
                }
                Iterator resources;
            	
                try {
                    List t = service.members(monitor);
                    resources = t == null ? null : t.iterator();
                    while( resources != null && resources.hasNext() ) {
                        GeoResource resource = (GeoResource) resources.next();
                        if (check(resource, ast, bbox)) {
                            result.add(resource);
                        }
                    }
                } catch (IOException e) {
                    //TODO log this
                }finally{
                	
                }
            }
        }
        return result;
        }finally{
        	
        }
    }

    /* check the fields we catre about */
    protected static boolean check( Service service, AST pattern ) {
        if( pattern==null ){
        	return false;
        }
        ServiceInfo info;
        try {
            info = service == null ? null : service.getInfo(null);
        } catch (IOException e) {
            info = null;
            e.printStackTrace();
            //TODO: log this
        }
        boolean t = false;
        if (info != null) {
            if (info.getTitle() != null)
                t = pattern.accept(info.getTitle());
            if (!t && info.getKeywords() != null) {
                String[] keys = info.getKeywords();
                for( int i = 0; !t && i < keys.length; i++ )
                    if (keys[i] != null)
                        t = pattern.accept(keys[i]);
            }
            if (!t && info.getSchema() != null)
                t = pattern.accept(info.getSchema().toString());
            if (!t && info.getAbstract() != null)
                t = pattern.accept(info.getAbstract());
            if (!t && info.getDescription() != null)
                t = pattern.accept(info.getDescription());
        }
        return t;
    }

    /* check the fields we catre about */
    protected static boolean check( GeoResource resource, AST pattern ) {
    	if( pattern==null )
    		return true;
        GeoResourceInfo info;
        try {
            info = (resource == null ? null : resource.getInfo(null));
        } catch (IOException e) {
            //TODO: log this
            info = null;
            e.printStackTrace();
        }
        boolean t = false;
        if (info != null) {
            if (info.getTitle() != null)
                t = pattern.accept(info.getTitle());
            if (!t && info.getName() != null)
                t = pattern.accept(info.getName());
            if (!t && info.getKeywords() != null) {
                String[] keys = info.getKeywords();
                for( int i = 0; !t && i < keys.length; i++ )
                    if (keys[i] != null)
                        t = pattern.accept(keys[i]);
            }
            if (!t && info.getSchema() != null)
                t = pattern.accept(info.getSchema().toString());
            if (!t && info.getDescription() != null)
                t = pattern.accept(info.getDescription());
        }
        return t;
    }

    protected static boolean check( GeoResource resource, AST pattern, Envelope bbox ) {
        if (!check(resource, pattern))
            return false;
        if (bbox == null || bbox.isNull())
            return true; // no checking here
        try {
            return bbox.intersects(resource.getInfo(null).getBounds());
        } catch (IOException e) {
        	//TODO: log this
        	e.printStackTrace();
            return false;
        }
    }

    /**
     * Fire a resource changed event, these may be batched into one delta for performance.
     * 
     * @param event the event to be fired
     * 
     * @throws IOException protected void fireResourceEvent( IGeoResource resource,
     *         IResolveDelta.Kind kind ) throws IOException { Object[] listeners =
     *         catalogListeners.getListeners(); if( listeners.length == 0 ) return;
     *         GeoReferenceDelta rDelta = new GeoReferenceDelta( resource, kind ); ServiceDelta
     *         sDelta = new ServiceDelta( resource.getService(null), IDelta.Kind.NO_CHANGE,
     *         Collections.singletonList( rDelta ) ); CatalogDelta cDelta = new CatalogDelta(
     *         Collections.singletonList( (IDelta)sDelta ) ); fire( new CatalogChangeEvent(
     *         resource, ICatalogChangeEvent.Type.POST_CHANGE, cDelta ) ); }
     */

    public void fire( ResolveChangeEvent event ) {
        Object[] listeners = catalogListeners.getListeners();
        if (listeners.length == 0)
            return;

        for( int i = 0; i < listeners.length; ++i ) {
            try {
                ((ResolveChangeListener) listeners[i]).changed(event);
            } 
            catch (Throwable die) {
            	die.printStackTrace();
            	//TODO: log this
            }
        }
    }
    /*
     * protected void fireServiceChanged( Object source, ServiceDelta sDelta ) { Object[] listeners =
     * catalogListeners.getListeners(); if( listeners.length == 0 ) return; CatalogDelta cDelta =
     * new CatalogDelta( Collections.singletonList( (IDelta)sDelta ) ); fire( new
     * CatalogChangeEvent( source , ICatalogChangeEvent.Type.POST_CHANGE, cDelta ) ); }
     */

    /**
     * @see net.refractions.udig.catalog.ICatalog#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public Object resolve( Class adaptee, ProgressListener monitor ) {
    	
    	if (adaptee == null)
            return null;
        if (adaptee.isAssignableFrom(Catalog.class))
            return this;
        if (adaptee.isAssignableFrom(CatalogInfo.class))
            return metadata;
        if (adaptee.isAssignableFrom(services.getClass()))
            return services;
        if (adaptee.isAssignableFrom(List.class))
            return new LinkedList(services);
        if (adaptee.isAssignableFrom(catalogListeners.getClass()))
            return catalogListeners;
	    	
        return null;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public boolean canResolve( Class adaptee ) {
        Object value = resolve(adaptee, null);
        return value != null;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#members(org.eclipse.core.runtime.IProgressMonitor)
     */
    public List members( ProgressListener monitor ) {
    	return new LinkedList(services);
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return Status.CONNECTED;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return null;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URI getIdentifier() {
        return metadata.getSource();
    }
    
//    static class DefaultCatalogInfo extends CatalogInfo {
//    	
//    	 DefaultCatalogInfo() {
//	        super(null, null, null, null);
//	    }
//
//	    /**
//	     * Construct <code>CatalogInfoImpl</code>.
//	     * 
//	     * @param title
//	     * @param description
//	     * @param source
//	     * @param keywords
//	     */
//	    public DefaultCatalogInfo( String title, String description, URI source, String[] keywords ) {
//	        super(title, description, source, keywords);
//	    }
//	    /**
//	     * @param desc The desc to set.
//	     */
//	    void setDesc( String desc ) {
//	        this.description = desc;
//	    }
//	    /**
//	     * @param keywords The keywords to set.
//	     */
//	    void setKeywords( String[] keywords ) {
//	        this.keywords = keywords;
//	    }
//	    /**
//	     * @param source The source to set.
//	     */
//	    void setSource( URI source ) {
//	        this.source = source;
//	    }
//	    /**
//	     * @param title The title to set.
//	     */
//	    void setTitle( String title ) {
//	        this.title = title;
//	    }
//    }
}
