/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.geomedia;

/**
 * Internal class used to cache metadata from the GeoMedia GFeatures tables. An
 * instance of this class represents a GeoMedia Feature class.
 *
 * @author Julian J. Ray
 *
 * @todo Add CSGUID information to suppot SRIDs
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/GFeatureType.java $
 */
class GFeatureType {
    private String mTypeName;
    private String mGeoColName;
    private int mFeatureType;
    private String mDescription;

    /**
     * Returns the name of this Feature Class
     *
     * @return String
     */
    public String getTypeName() {
        return mTypeName;
    }

    /**
     * Sets the TypeName for this feature class.
     *
     * @param name String
     */
    public void setTypeName(String name) {
        mTypeName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getGeomColName() {
        return mGeoColName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     */
    public void setGeomColName(String name) {
        mGeoColName = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getGeoMediaFeatureType() {
        return mFeatureType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param val DOCUMENT ME!
     */
    public void setGeoMediaFeatureType(int val) {
        mFeatureType = val;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     */
    public void setDescription(String name) {
        mDescription = name;
    }
}
