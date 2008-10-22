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
package org.geotools.gpx;

import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.binding.BoundsTypeBinding;
import org.geotools.gpx.binding.CopyrightTypeBinding;
import org.geotools.gpx.binding.DegreesTypeBinding;
import org.geotools.gpx.binding.DgpsStationTypeBinding;
import org.geotools.gpx.binding.EmailTypeBinding;
import org.geotools.gpx.binding.ExtensionsTypeBinding;
import org.geotools.gpx.binding.FixTypeBinding;
import org.geotools.gpx.binding.GpxTypeBinding;
import org.geotools.gpx.binding.LatitudeTypeBinding;
import org.geotools.gpx.binding.LinkTypeBinding;
import org.geotools.gpx.binding.LongitudeTypeBinding;
import org.geotools.gpx.binding.MetadataTypeBinding;
import org.geotools.gpx.binding.PersonTypeBinding;
import org.geotools.gpx.binding.PtTypeBinding;
import org.geotools.gpx.binding.PtsegTypeBinding;
import org.geotools.gpx.binding.RteTypeBinding;
import org.geotools.gpx.binding.TrkTypeBinding;
import org.geotools.gpx.binding.TrksegTypeBinding;
import org.geotools.gpx.binding.WptTypeBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;


/**
 * Parser configuration for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public class GPXConfiguration extends Configuration {
    /**
     * Creates a new configuration.
     *
     * @generated
     */
    public GPXConfiguration() {
        super(GPX.getInstance());
    }

    protected void registerBindings(MutablePicoContainer container) {
        //Types
        container.registerComponentImplementation(GPX.boundsType, BoundsTypeBinding.class);
        container.registerComponentImplementation(GPX.copyrightType, CopyrightTypeBinding.class);
        container.registerComponentImplementation(GPX.degreesType, DegreesTypeBinding.class);
        container.registerComponentImplementation(GPX.dgpsStationType, DgpsStationTypeBinding.class);
        container.registerComponentImplementation(GPX.emailType, EmailTypeBinding.class);
        container.registerComponentImplementation(GPX.extensionsType, ExtensionsTypeBinding.class);
        container.registerComponentImplementation(GPX.fixType, FixTypeBinding.class);
        container.registerComponentImplementation(GPX.gpxType, GpxTypeBinding.class);
        container.registerComponentImplementation(GPX.latitudeType, LatitudeTypeBinding.class);
        container.registerComponentImplementation(GPX.linkType, LinkTypeBinding.class);
        container.registerComponentImplementation(GPX.longitudeType, LongitudeTypeBinding.class);
        container.registerComponentImplementation(GPX.metadataType, MetadataTypeBinding.class);
        container.registerComponentImplementation(GPX.personType, PersonTypeBinding.class);
        container.registerComponentImplementation(GPX.ptsegType, PtsegTypeBinding.class);
        container.registerComponentImplementation(GPX.ptType, PtTypeBinding.class);
        container.registerComponentImplementation(GPX.rteType, RteTypeBinding.class);
        container.registerComponentImplementation(GPX.trksegType, TrksegTypeBinding.class);
        container.registerComponentImplementation(GPX.trkType, TrkTypeBinding.class);
        container.registerComponentImplementation(GPX.wptType, WptTypeBinding.class);
    }
    
    protected void configureContext(MutablePicoContainer container) {
        container.registerComponentImplementation(ObjectFactory.class);
    }

    
}
