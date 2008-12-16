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
package org.geotools.gml2.bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDDerivationMethod;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.geotools.gml2.GML;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.util.logging.Logging;
import org.geotools.xlink.XLINK;
import org.geotools.xml.Encoder;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.geotools.xs.XS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Utility methods used by gml2 bindigns when encodding.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML2EncodingUtils {
    
    /** logging instance */
    static Logger LOGGER = Logging.getLogger( "org.geotools.gml");
    
    static final int LON_LAT = 0;
    static final int LAT_LON = 1;
    static final int INAPPLICABLE = 2;

    public static String epsgCode(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }

        for (Iterator i = crs.getIdentifiers().iterator(); i.hasNext();) {
            Identifier id = (Identifier) i.next();

            //return "EPSG:" + id.getCode();
            if ((id.getAuthority() != null)
                    && id.getAuthority().getTitle().equals(Citations.EPSG.getTitle())) {
                return id.getCode();
            }
        }

        return null;
    }

    /**
     * @deprecated use {@link #toURI(CoordinateReferenceSystem)}.
     */
    public static String crs(CoordinateReferenceSystem crs) {
        return toURI(crs);
    }

    /**
     * Encodes the crs object as a uri.
     */
    public static String toURI(CoordinateReferenceSystem crs) {
        return toURI(crs,false);
    }
    
    /**
     * Encodes the crs object as a uri.
     * <p>
     * The axis order of the crs determines which form of uri is used.
     * </p>
     */
    public static String toURI(CoordinateReferenceSystem crs, boolean forceOldStyle) {
        String code = epsgCode(crs);
        int axisOrder = axisOrder(crs);

        if (code != null) {
            if (forceOldStyle ||( (axisOrder == LON_LAT) || (axisOrder == INAPPLICABLE)) ) {
                return "http://www.opengis.net/gml/srs/epsg.xml#" + code;
            } else {
                //return "urn:x-ogc:def:crs:EPSG:6.11.2:" + code;
                return "urn:x-ogc:def:crs:EPSG:" + code;
            }
        }

        return null;
    }

    /**
     * Returns the axis order of the provided {@link CoordinateReferenceSystem} object.
     * @param crs
     * @return <ul>
     *         <li>LON_LAT if the axis order is longitude/latitude</li>
     *         <li>LAT_LON if the axis order is latitude/longitude</li>
     *         <li>INAPPLICABLE if the CRS does not deal with longitude/latitude
     *         (such as vertical or engineering CRS)</li>
     */
    static int axisOrder(CoordinateReferenceSystem crs) {
        CoordinateSystem cs = null;

        if (crs instanceof ProjectedCRS) {
            ProjectedCRS pcrs = (ProjectedCRS) crs;
            cs = pcrs.getBaseCRS().getCoordinateSystem();
        } else if (crs instanceof GeographicCRS) {
            cs = crs.getCoordinateSystem();
        } else {
            return INAPPLICABLE;
        }

        int dimension = cs.getDimension();
        int longitudeDim = -1;
        int latitudeDim = -1;

        for (int i = 0; i < dimension; i++) {
            AxisDirection dir = cs.getAxis(i).getDirection().absolute();

            if (dir.equals(AxisDirection.EAST)) {
                longitudeDim = i;
            }

            if (dir.equals(AxisDirection.NORTH)) {
                latitudeDim = i;
            }
        }

        if ((longitudeDim >= 0) && (latitudeDim >= 0)) {
            if (longitudeDim < latitudeDim) {
                return LON_LAT;
            } else {
                return LAT_LON;
            }
        }

        return INAPPLICABLE;
    }

    /**
     * Determines the crs of the geometry by checking {@link Geometry#getUserData()}.
     * <p>
     * This method returns <code>null</code> when no crs can be found.
     * </p>
     */
    public static CoordinateReferenceSystem getCRS(Geometry g) {
        if (g.getUserData() == null) {
            return null;
        }

        if (g.getUserData() instanceof CoordinateReferenceSystem) {
            return (CoordinateReferenceSystem) g.getUserData();
        }

        if (g.getUserData() instanceof Map) {
            Map userData = (Map) g.getUserData();

            return (CoordinateReferenceSystem) userData.get(CoordinateReferenceSystem.class);
        }

        return null;
    }

    /**
     * Determines the identifier (gml:id) of the geometry by checking
     * {@link Geometry#getUserData()}.
     * <p>
     * This method returns <code>null</code> when no id can be found.
     * </p>
     */
    public static String getID(Geometry g) {
        return getMetadata( g, "gml:id" );
    }
    
    /**
     * Determines the description (gml:description) of the geometry by checking
     * {@link Geometry#getUserData()}.
     * <p>
     * This method returns <code>null</code> when no name can be found.
     * </p>
     */
    public static String getName(Geometry g) {
        return getMetadata( g, "gml:name" );
    }
    
    /**
     * Determines the name (gml:name) of the geometry by checking
     * {@link Geometry#getUserData()}.
     * <p>
     * This method returns <code>null</code> when no description can be found.
     * </p>
     */
    public static String getDescription(Geometry g) {
        return getMetadata( g, "gml:description" );
    }
    
    static String getMetadata(Geometry g, String metadata) {
        if (g.getUserData() instanceof Map) {
            Map userData = (Map) g.getUserData();

            return (String) userData.get(metadata);
        }

        return null;
    }
    
    public static Element AbstractFeatureType_encode(Object object, Document document, Element value) {
        SimpleFeature feature = (SimpleFeature) object;
        SimpleFeatureType featureType = feature.getFeatureType();

        String namespace = featureType.getName().getNamespaceURI();
        String typeName = featureType.getTypeName();

        Element encoding = document.createElementNS(namespace, typeName);
        encoding.setAttributeNS(null, "fid", feature.getID());

        return encoding;
    }

    static Set<QName> abstractFetureTypeProperties = new HashSet();
    static {
        abstractFetureTypeProperties.add( GML.name );
        abstractFetureTypeProperties.add( GML.description );
        abstractFetureTypeProperties.add( GML.location );
       
    }
    
    public static Object AbstractFeatureType_getProperty(Object object,
            QName name) {
      
        SimpleFeature feature = (SimpleFeature) object;

        if ( abstractFetureTypeProperties.contains( name ) ) {
            return feature.getAttribute( name.getLocalPart() );
        }
      
        if (GML.boundedBy.equals(name)) {
            BoundingBox bounds = feature.getBounds();

            if (bounds.isEmpty()) {
                //do a check for the case where the feature has no geometry 
                // properties
                if (feature.getDefaultGeometry() == null) {
                    return null;
                }
            }

            return feature.getBounds();
        }
        
        return null;
    }
    
    public static List AbstractFeatureType_getProperties(Object object,XSDElementDeclaration element,SchemaIndex schemaIndex) {
        return AbstractFeatureType_getProperties(object, element, schemaIndex, new HashSet<String>(Arrays.asList("name","description","boundedBy")));
    }
    
    public static List AbstractFeatureType_getProperties(Object object,XSDElementDeclaration element,SchemaIndex schemaIndex, Set<String> toFilter) {
        SimpleFeature feature = (SimpleFeature) object;
        
        //check if this was a resolved feature, if so dont return anything
        // TODO: this is just a hack for our lame xlink implementation
        if (feature.getUserData().get("xlink:id") != null) {
            return Collections.EMPTY_LIST;
        }

        SimpleFeatureType featureType = feature.getFeatureType();

        String namespace = featureType.getName().getNamespaceURI();

        if (namespace == null) {
            namespace = element.getTargetNamespace();
        }

        String typeName = featureType.getTypeName();

        //find the type in the schema
        XSDTypeDefinition type = schemaIndex.getTypeDefinition(new QName(namespace, typeName));

        if (type == null) {
            //type not found, do a check for an element, and use its type
            XSDElementDeclaration e = schemaIndex.getElementDeclaration(new QName(namespace,
                        typeName));

            if (e != null) {
                type = e.getTypeDefinition();
            }
        }

        if (type == null) {
            //could not find the feature type in teh schema, create a mock one
            LOGGER.warning( "Could find type for " + typeName + " in the schema, generating type" + 
                    "from feature.");
            type = createXmlTypeFromFeatureType( featureType, schemaIndex, toFilter );
        }

        List particles = Schemas.getChildElementParticles(type, true);
        List properties = new ArrayList();

    O:  for (Iterator p = particles.iterator(); p.hasNext();) {
            XSDParticle particle = (XSDParticle) p.next();
            XSDElementDeclaration attribute = (XSDElementDeclaration) particle.getContent();

            if (attribute.isElementDeclarationReference()) {
                attribute = attribute.getResolvedElementDeclaration();
            }

            //ignore abstract featureType properties
            if (GML.NAMESPACE.equals(attribute.getTargetNamespace())) {
                for ( QName n : abstractFetureTypeProperties ) {
                    if ( n.getLocalPart().equals( attribute.getName() )) {
                        continue O;
                    }
                }
                
            }

            //make sure the feature type has an element
            if (featureType.getDescriptor(attribute.getName()) == null) {
                continue;
            }

            //get the value
            Object attributeValue = feature.getAttribute(attribute.getName());
            properties.add(new Object[] { particle, attributeValue });
        }

        return properties;
    }
    
    public static XSDTypeDefinition createXmlTypeFromFeatureType(SimpleFeatureType featureType, SchemaIndex schemaIndex, Set<String> toFilter ) { 
        XSDFactory f = XSDFactory.eINSTANCE;
        Document dom;
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException( e );
        }
        
        XSDComplexTypeDefinition type = f.createXSDComplexTypeDefinition();
        type.setTargetNamespace( featureType.getName().getNamespaceURI() );
        type.setName( featureType.getTypeName() + "Type" );
        type.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);
        type.setBaseTypeDefinition(schemaIndex.getTypeDefinition( GML.AbstractFeatureType ) );
                
        XSDModelGroup group = f.createXSDModelGroup();
        group.setCompositor(XSDCompositor.SEQUENCE_LITERAL);

        List attributes = featureType.getAttributeDescriptors();
        for (int i = 0; i < attributes.size(); i++) {
            AttributeDescriptor attribute = (AttributeDescriptor) attributes.get(i);

            if ( toFilter.contains( attribute.getLocalName() ) ) {
                continue;
            }
           
            XSDElementDeclaration element = f.createXSDElementDeclaration();
            element.setName(attribute.getLocalName());
            element.setNillable(attribute.isNillable());

            //check for geometry
            if ( attribute instanceof GeometryDescriptor ) {
                Class binding = attribute.getType().getBinding();
                if ( Point.class.isAssignableFrom( binding ) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.PointPropertyType));
                }
                else if ( LineString.class.isAssignableFrom( binding ) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.LineStringPropertyType));
                }
                else if ( Polygon.class.isAssignableFrom( binding) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.PolygonPropertyType));
                }
                else if ( MultiPoint.class.isAssignableFrom( binding ) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.MultiPointPropertyType));
                }
                else if ( MultiLineString.class.isAssignableFrom( binding ) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.MultiLineStringPropertyType));
                }
                else if ( MultiPolygon.class.isAssignableFrom( binding) ) {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.MultiPolygonPropertyType));
                }
                else {
                    element.setTypeDefinition( schemaIndex.getTypeDefinition(GML.GeometryPropertyType));
                }
            }
            else {
                //TODO: do a proper mapping
                element.setTypeDefinition(schemaIndex.getTypeDefinition(XS.STRING));
            }
            

            XSDParticle particle = f.createXSDParticle();
            particle.setMinOccurs(attribute.getMinOccurs());
            particle.setMaxOccurs(attribute.getMaxOccurs());
            particle.setContent(element);
            particle.setElement( dom.createElementNS( XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001, "element" ) );
            
            group.getContents().add(particle);
        }

        XSDParticle particle = f.createXSDParticle();
        particle.setContent(group);
        particle.setElement( dom.createElementNS( XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001, "sequence") );
        type.setContent(particle);
        return type;
    }

    public static Object GeometryPropertyType_getProperty(Geometry geometry,
            QName name) {
        return GeometryPropertyType_getProperty(geometry,name,true);
    }
    public static Object GeometryPropertyType_getProperty(Geometry geometry,
            QName name, boolean includeAbstractGeometry ) {
        if (GML.Point.equals( name ) || GML.LineString.equals( name ) || GML.Polygon.equals( name ) 
            || GML.MultiPoint.equals( name ) || GML.MultiLineString.equals( name ) || GML.MultiPolygon.equals( name )
            || (includeAbstractGeometry && GML._Geometry.equals(name) )) {
                //if the geometry is null, return null
                if ( isEmpty( geometry ) ) {
                    return null;
                }
                
                return geometry;
            }
            
            if (XLINK.HREF.equals(name)) {
                //only process if geometry is empty
                if ( isEmpty(geometry) ) {
                    String id = getID( geometry );
                    if ( id != null ) {
                        return "#" + id;
                    }
                }
            }

            return null;
    }

    public static List GeometryPropertyType_getProperties(Geometry geometry) {

        String id = getID( geometry );
        
        if ( !isEmpty(geometry) && id != null ) {
            // return a comment which is hte xlink href
            return Collections.singletonList(new Object[] { Encoder.COMMENT, "#" +id });            
        }
        
        return null;
    }
    
    static boolean isEmpty( Geometry geometry ) {
        if ( geometry.isEmpty() ) {
            //check for case of multi geometry, if it has > 0 goemetries 
            // we consider this to be not empty
            if ( geometry instanceof GeometryCollection ) {
                if ( ((GeometryCollection) geometry).getNumGeometries() != 0 ) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }
    
    
    
}
