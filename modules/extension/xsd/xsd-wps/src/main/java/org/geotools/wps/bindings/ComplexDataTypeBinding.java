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

package org.geotools.wps.bindings;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wps10.ComplexDataType;
import net.opengis.wps10.Wps10Factory;

import org.geotools.gml2.GML;
import org.geotools.wps.WPS;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ComplexDataTypeBinding extends AbstractComplexBinding
{
    private Wps10Factory factory;

    public ComplexDataTypeBinding(Wps10Factory factory)
    {
        this.factory = factory;
    }
    
    public QName getTarget()
    {
        return WPS.ComplexDataType;
    }

    public Class<?> getType()
    {
        return ComplexDataType.class;
    }

    public int getExecutionMode() {
        return OVERRIDE;
    }
    
    
    public Object getProperty(Object object, QName name) throws Exception {
        ComplexDataType data = (ComplexDataType) object;
        
        if ( "schema".equals( name.getLocalPart() ) ) {
            return data.getSchema();
        }
        
        if ( "mimeType".equals( name.getLocalPart() ) ) {
            return data.getMimeType();
        }
        
        if ( "encoding".equals( name.getLocalPart() ) ) {
            return data.getEncoding();
        }
        
        return super.getProperty(object, name);
    }
    
    /*
    	Would need to look at the contained values to detect the correct
    	order.
    */
    public List<List<Object>> getProperties(Object obj)
    {
    	ComplexDataType data = (ComplexDataType)obj;
    	
    	List/*<List<Object>>*/ properties = new ArrayList/*<List<Object>>*/();

    	/*
    	List<?> features = data.getData();

    	for(Object obj0 : features)	// XXX TEST
    	{
    		// put all the possible values mapped to qnames.
    		// for now just support the main GML 2 types
            if ( obj0 instanceof Point ) {
                properties.add( new Object[]{ GML.Point, obj0 } );
            }
            else if ( obj0 instanceof Polygon ) {
                properties.add( new Object[]{ GML.Polygon, obj0 } );
            }
            else if ( obj0 instanceof LinearRing ) {
                properties.add( new Object[]{ GML.LinearRing, obj0 } );
            }             
            else if ( obj0 instanceof LineString ) {
                properties.add( new Object[]{ GML.LineString, obj0 } );
            }   
            else if ( obj0 instanceof MultiLineString ) {
                properties.add( new Object[]{ GML.MultiLineString, obj0 } );
            }      
            else if ( obj0 instanceof MultiPoint ) {
                properties.add( new Object[]{ GML.MultiPoint, obj0 } );
            }          
            else if ( obj0 instanceof MultiPolygon ) {
                properties.add( new Object[]{ GML.MultiPolygon, obj0 } );
            }             
    	}
    	*/
    	return properties;
    }

    /*
    	NodeImpl -> JTS.Polygon
    */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception
    {
        ComplexDataType data = factory.createComplexDataType();
        
        if ( node.hasAttribute( "schema" ) ) {
            data.setSchema( node.getAttributeValue( "schema").toString() );
        }
        if ( node.hasAttribute( "mimeType" ) ) {
            data.setMimeType( node.getAttributeValue( "mimeType").toString() );
        }
        if ( node.hasAttribute( "encoding" ) ) {
            data.setEncoding( node.getAttributeValue( "encoding").toString() );
        }
        
        /*
        for ( Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
            Node c = (Node) i.next();
            data.getData().add( c.getValue() );
        }
        */
        
        return data;
    }
}
