/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.arcsde.gce.imageio;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageReadParam;

import org.geotools.arcsde.pool.ArcSDEPooledConnection;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeRasterBand;

/**
 * This class represents the parameters passed to an ArcSDERasterReader each time an image is read.
 * Some parameters from the parent class are interpreted strictly in this class. <i>Required
 * parameters during a read:</i>
 * <ul>
 * <li>An open and connected {@link SeConnection} used to suck out data from ArcSDE for this image
 * read operation.(setConnection())</li>
 * <li>A {@link HashMap} with {@link Integer} keys equal to the SeObjectId.longValue() of each
 * {@link SeRasterBand} which is to be read. Values for each key should be the integer value of the
 * band in the output image to which the given SeRasterBand should be copied.<br/><br/>For
 * example, if your ArcSDE Raster contains four bands with SeRasterBand.getId().longValue()s of
 * 234,235,236 and 237, and you wish to map those bands to the R,G,B and A bands in your ARGB image,
 * you should create a map as follows:<br/> Map m = new HashMap();<br/> m.put(new Integer(234),
 * new Integer(1));<br/> m.put(new Integer(234), new Integer(2));<br/> m.put(new Integer(234), new
 * Integer(3));<br/> m.put(new Integer(234), new Integer(0));<br/> <br/> Note that the band
 * indexes you choose will depend on the type of BufferedImage you're writing to. A BufferedImage of
 * type BufferedImage.TYPE_INT_ARGB has its bands in the order "Alpha (0), Red (1), Green (2), Blue
 * (3)". A BufferedImage of type BufferedImage.TYPE_3BYTE_BGR has its bands in the order "Blue (0),
 * Green (1), Red (2)".<br/> </li>
 * <li>A (possibly null) {@link BufferedImage}, passed in via the 'setDestination()' method. Make
 * sure your bandmapper and this image contain the same number of bands, and that you've mapped your
 * output appropriately.<br/> <i><b>If you leave this parameter null, a
 * BufferedImage.TYPE_INT_ARGB will be created, of exactly the size to cover your requested source
 * region</b></i></li>
 * <li>An int[] of requested source bands from ArcSDE, set via setSourceBands(). ArcSDE expects the
 * int[] to have the same size as the number of bands you're requesting. The first band should be
 * numbered '1', the second '2', etc. So, to request the second, third and fourth bands in a raster,
 * use a setSourceBands() call like the following:<br/> param.setSourceBands(new int[] { 2, 3, 4
 * });<br/> <b>A source band with the value of zero won't work! start with 1!</b> </li>
 * <li>A {@link Rectangle} defining the source area of the raster level you wish to render. This is
 * expressed as though the entire raster level was one big seamless image of size wxh, with the
 * origin in the upper left corner. So, to request a tiny bit of the raster level, simply set your
 * sourceRegion accordingly and only the proper bit of the raster will be loaded.</li>
 * </ul>
 * 
 * @author sfarber
 */
public class ArcSDERasterImageReadParam extends ImageReadParam {

    protected ArcSDEPooledConnection connection;

    protected HashMap<Integer, Integer> bandMapper;

    public HashMap<Integer, Integer> getBandMapper() {
        return bandMapper;
    }

    public void setBandMapper(HashMap<Integer, Integer> bandMapper) {
        this.bandMapper = bandMapper;
    }

    public ArcSDEPooledConnection getConnection() {
        return connection;
    }

    public void setConnection(ArcSDEPooledConnection connection) {
        this.connection = connection;
    }
}
