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
package org.geotools.arcsde.gce.producer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferFloat;

import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConsumer;
import com.esri.sde.sdk.client.SeRasterRenderedImage;
import com.esri.sde.sdk.client.SeRasterScanLineGenerator;

public class ArcSDERasterFloatProducerImpl extends ArcSDERasterProducer {

    public ArcSDERasterFloatProducerImpl() {
        super(null, null, SeRasterScanLineGenerator.MASK_ALL_ON);
    }

    public ArcSDERasterFloatProducerImpl(SeRasterAttr attr, BufferedImage sourceImage, int maskType) {
        super(attr, sourceImage, maskType);
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        // TODO: check that the image is compatible
        this.sourceImage = sourceImage;

    }

    public void startProduction(final SeRasterConsumer consumer) {
        Thread runme;
        if (consumer instanceof SeRasterRenderedImage) {

            runme = new Thread() {
                @Override
                public void run() {
                    try {
                        final int imageHeight = sourceImage.getHeight();

                        // for each band...
                        for (int i = 0; i < sourceImage.getData().getNumBands(); i++) {
                            // get the data as floats, then convert to four-byte segments
                            byte[] sdeRasterData = new byte[sourceImage.getWidth() * 4
                                    * sourceImage.getHeight()];
                            float[] srcImgData = ((DataBufferFloat) sourceImage.getData()
                                    .getDataBuffer()).getData();
                            for (int y = 0; y < sourceImage.getHeight(); y++) {
                                // byte[] curRow = new byte[sourceImage.getWidth() * 4];
                                for (int x = 0; x < sourceImage.getWidth(); x++) {
                                    // final float sample = sourceImage.getData().getSampleFloat(x,
                                    // y, i);
                                    final float sample = srcImgData[y * sourceImage.getWidth() + x];
                                    // convert float to bytes
                                    int bits = Float.floatToIntBits(sample);
                                    sdeRasterData[y * sourceImage.getWidth() * 4 + x * 4] = (byte) ((bits & 0xff000000) >> 24);
                                    sdeRasterData[y * sourceImage.getWidth() * 4 + x * 4 + 1] = (byte) ((bits & 0x00ff0000) >> 16);
                                    sdeRasterData[y * sourceImage.getWidth() * 4 + x * 4 + 2] = (byte) ((bits & 0x0000ff00) >> 8);
                                    sdeRasterData[y * sourceImage.getWidth() * 4 + x * 4 + 3] = (byte) (bits & 0x000000ff);
                                }
                            }

                            consumer.setScanLines(imageHeight, sdeRasterData, null);
                            consumer.rasterComplete(SeRasterConsumer.SINGLEFRAMEDONE);
                        }
                        consumer.rasterComplete(SeRasterConsumer.STATICIMAGEDONE);
                    } catch (Exception se) {
                        se.printStackTrace();
                        consumer.rasterComplete(SeRasterConsumer.IMAGEERROR);
                    }
                }
            };
        } else {
            throw new IllegalArgumentException("You must set SeRasterAttr.setImportMode(false) to "
                    + "load data using this SeProducer implementation.");
        }

        runme.start();

    }
}
