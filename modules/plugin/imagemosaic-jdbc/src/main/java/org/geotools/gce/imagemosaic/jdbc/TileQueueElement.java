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
package org.geotools.gce.imagemosaic.jdbc;

import java.awt.image.BufferedImage;

import org.geotools.geometry.GeneralEnvelope;

/**
 * Class for holding tile info for composing the resulting image. Objects of
 * this class will be put in the queue from ImageDecoderThread and read from
 * ImageComposerThread
 * 
 * @author mcr
 * 
 */
class TileQueueElement {

	static TileQueueElement ENDELEMENT = new TileQueueElement(null, null, null);

	/**
	 * name of the tile
	 */
	private String name;

	/**
	 * the BufferedImage
	 */
	private BufferedImage tileImage;

	/**
	 * The georeferencing information
	 */
	private GeneralEnvelope envelope;

	GeneralEnvelope getEnvelope() {
		return envelope;
	}

	TileQueueElement(String name, BufferedImage tileImage,
			GeneralEnvelope envelope) {
		this.name = name;
		this.tileImage = tileImage;
		this.envelope = envelope;
	}

	String getName() {
		return name;
	}

	BufferedImage getTileImage() {
		return tileImage;
	}

	boolean isEndElement() {
		return getName() == null && getTileImage() == null
				&& getEnvelope() == null;
	}
}
