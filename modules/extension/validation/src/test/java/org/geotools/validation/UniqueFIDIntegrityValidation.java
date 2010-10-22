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
package org.geotools.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * UniqueFIDIntegrityValidation purpose.
 * <p>
 * Description of UniqueFIDIntegrityValidation ...
 * <p>
 * Capabilities:
 * <ul>
 * </li></li>
 * </ul>
 * Example Use:
 * <pre><code>
 * UniqueFIDIntegrityValidation x = new UniqueFIDIntegrityValidation(...);
 * </code></pre>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/extension/validation/src/test/java/org/geotools/validation/UniqueFIDIntegrityValidation.java $
 * @version $Id: UniqueFIDIntegrityValidation.java 30662 2008-06-12 21:44:16Z acuster $
 */
public class UniqueFIDIntegrityValidation implements IntegrityValidation {


	private String name;
	private String description;
	private String[] typeNames;
	//private String uniqueID;
		
		
	/**
	 * UniqueFIDIntegrityValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public UniqueFIDIntegrityValidation() {
	}

	/**
	 * UniqueFIDIntegrityValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * @param name
	 * @param description
	 * @param typeNames
	 */
	public UniqueFIDIntegrityValidation(String name, String description, String[] typeNames, String uniqueID) {
		this.name = name;
		this.description = description;
		this.typeNames = typeNames;
		//this.uniqueID = uniqueID;
	}

	/**
	 * Override setName.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setName(java.lang.String)
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Override getName.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getName()
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Override setDescription.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setDescription(java.lang.String)
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Override getDescription.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getDescription()
	 * 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Override getPriority.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getPriority()
	 * 
	 */
	public int getPriority() {
		return 10;
	}

	/**
	 * Override setTypeNames.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setTypeNames(java.lang.String[])
	 * 
	 * @param names
	 */
	public void setTypeNames(String[] names) {
		this.typeNames = names;
	}

	/**
	 * Override getTypeNames.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getTypeNames()
	 * 
	 */
	public String[] getTypeRefs() {
		return typeNames;
	}


	/**
	 * Override validate.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map, com.vividsolutions.jts.geom.Envelope, org.geotools.validation.ValidationResults)
	 * 
	 * @param layers
	 * @param envelope
	 * @param results
	 */
	public boolean validate(Map layers, ReferencedEnvelope envelope, ValidationResults results) throws Exception{
		
		HashMap FIDs = new HashMap();
		boolean result = true;
		Iterator it = layers.values().iterator();
		
		while (it.hasNext())// for each layer
		{
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) it.next();
			FeatureIterator<SimpleFeature> features = featureSource.getFeatures().features();
			try {
				 
				while (features.hasNext())	// for each feature
				{
					SimpleFeature feature = features.next();
					String fid = feature.getID();
					if(FIDs.containsKey(fid))	// if a FID like this one already exists
					{
						results.error(feature, "FID already exists.");
						result = false;
					}
					else
						FIDs.put(fid, fid);
				}
			}
			finally {
				features.close();		// this is an important line	
			}

		}
		
		return result;
	}
}
