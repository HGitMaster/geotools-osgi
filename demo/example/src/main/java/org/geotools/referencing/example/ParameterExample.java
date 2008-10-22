/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.referencing.example;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.SI;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterValue;
import org.opengis.util.CodeList;

public class ParameterExample {

    public static void main( String[] args ) {
        final DefaultParameterDescriptor RANGE = new DefaultParameterDescriptor("Range", 15.0, -30.0, +40.0, null);
        
        System.out.println( RANGE.getMinimumValue().compareTo( new Double(2) ) );
        System.out.println( RANGE.getMaximumValue().compareTo( new Double(20) ) );
        
        Identifier name = RANGE.getName();
        System.out.println( name );
        
        ParameterValue value = (ParameterValue) RANGE.createValue();
        value.setValue( 2.0 );  // good
        
        value.setValue( 20.0 ); // invalid
        value.setValue( 2 ); // invalid
        
        
        final DefaultParameterDescriptor STATUS = new DefaultParameterDescriptor("Status",Status.GOOD );
        ParameterValue status = (ParameterValue) STATUS.createValue();
        
        final DefaultParameterDescriptor LIMIT =
            new DefaultParameterDescriptor( Citations.GEOTOOLS, "Limit", Double.class, null, null, null, null, SI.METER.divide(SI.SECOND), true);
        
        final DefaultParameterDescriptor PREFIX =
            new DefaultParameterDescriptor( Citations.GEOTOOLS, "Perfix", String.class, null, null, null, null, null, true);
        final DefaultParameterDescriptor NAMESPACE =
            new DefaultParameterDescriptor( Citations.GEOTOOLS, "Namespace", URI.class, null, null, null, null, null, true);        

        final DefaultParameterDescriptorGroup REFERENCES = new DefaultParameterDescriptorGroup(Citations.GEOTOOLS, "Referneces", new DefaultParameterDescriptor[]{PREFIX,NAMESPACE});
        
        Map metadata = new HashMap();
        metadata.put( "authority", System.getProperties().get("user.name"));
        metadata.put( "name", "References2");
        metadata.put( "alias", "References II");
        final DefaultParameterDescriptorGroup REFERENCES2 = new DefaultParameterDescriptorGroup(metadata, 0, Integer.MAX_VALUE, new DefaultParameterDescriptor[]{PREFIX,NAMESPACE});        
        
        
    }
    
    static class Status extends CodeList<Status> {
        private static final long serialVersionUID = 0L;        
        static ArrayList<Status> values = new ArrayList<Status>();
        static Status GOOD = new Status("GOOD");
        static Status BAD = new Status("BAD");
        static Status UGLY = new Status("UGLY");
        private Status( String name){
            super( name, values );
        }
        public Status[] family() {
            return (Status[]) values.toArray( new Status[ values.size()]);
        }
    }
}


