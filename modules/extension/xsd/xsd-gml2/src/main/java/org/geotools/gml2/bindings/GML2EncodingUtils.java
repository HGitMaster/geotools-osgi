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

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.gml2.GML;
import org.geotools.metadata.iso.citation.Citations;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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


/**
 * Utility methods used by gml2 bindigns when encodding.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML2EncodingUtils {
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
     * <p>
     * The axis order of the crs determines which form of uri is used.
     * </p>
     */
    public static String toURI(CoordinateReferenceSystem crs) {
        String code = epsgCode(crs);
        int axisOrder = axisOrder(crs);

        if (code != null) {
            if ((axisOrder == LON_LAT) || (axisOrder == INAPPLICABLE)) {
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
        encoding.setAttributeNS(GML.NAMESPACE, "id", feature.getID());

        return encoding;
    }

    public static Object AbstractFeatureType_getProperty(Object object,
            QName name) {
      //JD: here we only handle the "GML" attributes, all the application 
        // schema attributes are handled by FeaturePropertyExtractor

        //JD: TODO: handle all properties here and kill FeautrePropertyExtractor
        SimpleFeature feature = (SimpleFeature) object;

        if (GML.name.equals(name)) {
            return feature.getAttribute("name");
        }

        if (GML.description.equals(name)) {
            return feature.getAttribute("description");
        }

        if (GML.location.equals(name)) {
            return feature.getAttribute("location");
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
}
