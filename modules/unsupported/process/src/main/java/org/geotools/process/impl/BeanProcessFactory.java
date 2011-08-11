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

import java.awt.RenderingHints.Key;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.opengis.util.InternationalString;

/**
 * Reflective implementation of a {@link SingleProcessFactory} that will embed in the
 * same entity both the process and the factory.
 * The process is supposed to take a bean as a parameter and return a bean as a result. 
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/process/src/main/java/org/geotools/process/impl/BeanProcessFactory.java $
 */
public abstract class BeanProcessFactory implements ProcessFactory {
    
    public Process create() {
        return new SimpleProcess(this){
            public void process() throws Exception {
                BeanProcessFactory.this.process( input, result );
            }
        };
    }

    public InternationalString getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public Map<String, Parameter< ? >> getParameterInfo() {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo( getInputBean() );
        } catch (IntrospectionException e) {
            return null;
        }
        Map<String,Parameter<?>> parameterInfo = new HashMap<String, Parameter<?>>();
        
        for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ){
            Method getter = descriptor.getReadMethod();
            if( getter == null ) continue;
            
            Parameter<?> parameter = new Parameter(descriptor.getName(), descriptor.getPropertyType(),
            		Text.text(descriptor.getDisplayName()),
            		Text.text(descriptor.getShortDescription()) );
            
            parameterInfo.put( descriptor.getName(), parameter );            
        }
        return parameterInfo;
    }

    public Map<String, Parameter< ? >> getResultInfo() {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo( getResultBean() );
        } catch (IntrospectionException e) {
            return null;
        }
        Map<String,Parameter<?>> parameterInfo = new HashMap<String, Parameter<?>>();
        
        for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ){
            Method setter = descriptor.getWriteMethod();
            if( setter == null ) continue;
            
            Parameter<?> parameter = new Parameter(descriptor.getName(), descriptor.getPropertyType(),
                    Text.text(descriptor.getDisplayName()),
                    Text.text(descriptor.getShortDescription()) );
            
            parameterInfo.put( descriptor.getName(), parameter );            
        }
        return parameterInfo;
    }
    public Map<String, Parameter< ? >> getResultInfo( Map<String, Object> parameters )
            throws IllegalArgumentException {
        return null;
    }

    public InternationalString getTitle() {
        return Text.text( getClass().getSimpleName() );
    }

    protected void process( Map<String, Object> inputMap, Map<String, Object> resultMap ) throws Exception {
        BeanInfo inputInfo = Introspector.getBeanInfo( getInputBean() );
        Object inputBean = inputInfo.getBeanDescriptor().getBeanClass().getConstructor( new Class[0]);
        // should use commons beans here ....
        configure( inputInfo, inputMap, inputBean );
        
        
        Object resultBean = process( inputBean );        
        BeanInfo resultInfo = Introspector.getBeanInfo( getResultBean(), Object.class );
        
        results( resultMap, resultBean, resultInfo );        
    }
    
    private void results( Map<String, Object> resultMap, Object bean, BeanInfo info ) {
        for( PropertyDescriptor property : info.getPropertyDescriptors() ){
            if( resultMap.containsKey( property.getName() )){
                Method setter = property.getReadMethod();
                try {
                    Object value = setter.invoke( bean );
                    resultMap.put( property.getName(), value );
                } catch (Exception e) {
                    // ignore for right now .. TODO WARNING
                }
            }
        }
    }

    private void configure( BeanInfo info, Map<String, Object> inputMap, Object bean ) {
        for( PropertyDescriptor property : info.getPropertyDescriptors() ){
            if( inputMap.containsKey( property.getName() )){
                Method setter = property.getWriteMethod();
                try {
                    setter.invoke( bean, inputMap.get( property.getName() ));
                } catch (Exception e) {
                    // ignore for right now .. TODO WARNING
                }
            }
        }
    }
    
    /** 
     * Default Implementation return true
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * The default implementation returns an empty map.
     */
    public Map<Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

    /** 
     * Please return us an instanceof the bean you expect for input.
     * <p>
     * We will generate the correct process api input parameters to reflect
     * your choice.
     * @return bean used for input
     */
    protected abstract Class<?> getInputBean();
    protected abstract Class<?> getResultBean();
    protected abstract Object process( Object input );
}
