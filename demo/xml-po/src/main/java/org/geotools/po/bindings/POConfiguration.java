/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.po.bindings;

import org.eclipse.xsd.util.XSDSchemaLocationResolver;	
import org.geotools.po.ObjectFactory;
import org.geotools.xml.BindingConfiguration;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for the http://www.geotools.org/po schema.
 *
 * @generated
 */
public class POConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     * 
     * @generated
     */     
    public POConfiguration() {
       super();
       
       //TODO: add dependencies here
    }
    
    /**
     * @return the schema namespace uri: http://www.geotools.org/po.
     * @generated
     */
    public String getNamespaceURI() {
    	return PO.NAMESPACE;
    }
    
    /**
     * @return the uri to the the po.xsd .
     * @generated
     */
    public String getSchemaFileURL() {
        return getSchemaLocationResolver().resolveSchemaLocation( 
           null, getNamespaceURI(), "po.xsd"
        );
    }
    
    /**
     * @return new instanceof {@link POBindingConfiguration%>}.
     */    
    public BindingConfiguration getBindingConfiguration() {
     	return new POBindingConfiguration();
    }
    
    /**
     * @return A new instance of {@link POSchemaLocationResolver%>}.
     */
    public XSDSchemaLocationResolver getSchemaLocationResolver() {
    	return new POSchemaLocationResolver();
    }
    
    /**
     * Registers an instance of {@link ObjectFactory}.
     */
    protected void configureContext(MutablePicoContainer context) {
    	context.registerComponentImplementation( ObjectFactory.class );
    }
    
} 
