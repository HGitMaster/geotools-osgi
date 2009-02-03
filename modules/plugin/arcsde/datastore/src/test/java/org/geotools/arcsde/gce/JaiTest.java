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
 *
 */
package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import junit.framework.Assert;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi;

public class JaiTest {

    private static Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    static RasterTestData testData;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (testData == null) {
            testData = new RasterTestData();
            testData.setUp();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // testData.tearDown();
    }

    @Test
    public void testJai_8bit_U_1Band() throws Exception {
        testJai(RasterCellType.TYPE_8BIT_U, 1);
    }

    @Test
    public void testJai_8bit_U_3Bands() throws Exception {
        testJai(RasterCellType.TYPE_8BIT_U, 3);
    }

    @Test
    public void testJai_8bit_U_4Bands() throws Exception {
        testJai(RasterCellType.TYPE_8BIT_U, 4);
    }

    @Test
    public void testJai_16bit_U_1Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 1);
    }

    @Test
    public void testJai_16bit_U_3Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 1);
    }

    @Test
    public void testJai_16bit_U_4Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 4);
    }

    @Test
    public void testJai_32bit_real_1Band() throws Exception {
        testJai(RasterCellType.TYPE_32BIT_REAL, 1);
    }

    @Test
    public void testJai_32bit_real_2Band() throws Exception {
        testJai(RasterCellType.TYPE_32BIT_REAL, 2);
    }

    @Test
    public void testJai_32bit_real_3Band() throws Exception {
        testJai(RasterCellType.TYPE_32BIT_REAL, 3);
    }

    private void testJai(final RasterCellType pixelType, final int numberOfBands) throws Exception {
        final String tableName = testData.getRasterTableName(pixelType, numberOfBands);
        testData.loadTestRaster(tableName, numberOfBands, pixelType, null);

        testJai(0, tableName, numberOfBands, pixelType);
    }

    private void testJai(final int pyramidLevel, String tableName, int numberOfBands,
            RasterCellType pixelType) throws Exception {

        SeConnection conn = testData.getConnectionPool().getConnection();
        final SeRasterAttr rAttr;
        final SeRow row;
        final ArcSDEPyramid pyramidInfo;
        final ArcSDEPyramidLevel level;

        try {
            SeQuery seQuery = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(
                    tableName));
            seQuery.prepareQuery();
            seQuery.execute();
            row = seQuery.fetch();
            rAttr = row.getRaster(0);

            pyramidInfo = new ArcSDEPyramid(rAttr, CRS.decode("EPSG:4326"));
            level = pyramidInfo.getPyramidLevel(pyramidLevel);

            SeRasterConstraint rConstraint = new SeRasterConstraint();
            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }
            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);

            rConstraint.setEnvelope(0, 0, level.getNumTilesWide() - 1, level.getNumTilesHigh() - 1); // which
            // tiles
            // ...
            int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BSQ;
            rConstraint.setInterleave(interleaveType);
            seQuery.queryRasterTile(rConstraint);

        } catch (SeException se) {
            conn.close();
            throw new ArcSdeException(se);
        }

        final ImageInputStream in;

        try {
            in = new ArcSDETiledImageInputStream(conn, pixelType, rAttr, row);
        } catch (IOException e) {
            conn.close();
            throw e;
        }

        try {
            int b;
            int readByteCount = 0;
            // ByteArrayOutputStream out = new ByteArrayOutputStream();
            // while ((b = in.readByte()) != -1) {
            // readByteCount++;
            // out.write(b);
            // }
            // System.out.println(readByteCount);
            // int expectedCount = numberOfBands * (pixelType.getBitsPerSample() / 8) * tileWidth
            // * tileHeight;
            // Assert.assertEquals(expectedCount, readByteCount);

            // Prepare temporaray colorModel and sample model, needed to build the
            // RawImageInputStream
            ColorSpace colorSpace = ColorSpace.getInstance(numberOfBands == 1 ? ColorSpace.CS_GRAY
                    : ColorSpace.CS_sRGB);
            ColorModel colorModel = new ComponentColorModel(colorSpace, false, false,
                    Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            // SampleModel sampleModel =
            // colorModel.createCompatibleSampleModel(level.getSize().width,
            // level.getSize().height);

            // final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);

            final ImageTypeSpecifier its;
            // if (numberOfBands == 1) {
            // boolean isAlphaPremultiplied = false;
            // its = ImageTypeSpecifier.createGrayscale(pixelType.getBitsPerSample(), pixelType
            // .getDataBufferType(), pixelType.isSigned(), isAlphaPremultiplied);
            // } else {
            // boolean hasAlpha = false;
            // boolean isAlphaPremultiplied = false;
            // int[] bankIndices = new int[numberOfBands];
            // for (int bankIndex = 0; bankIndex <= numberOfBands; bankIndex++) {
            // bankIndices[bankIndex] = bankIndex;
            // }
            // int[] bandOffsets = null;
            // its = ImageTypeSpecifier.createBanded(colorSpace, bankIndices, bandOffsets,
            // pixelType.getDataBufferType(), hasAlpha, isAlphaPremultiplied);
            // }
            its = RasterUtils.createImageTypeSpec(pixelType, numberOfBands, level.getSize().width,
                    level.getSize().height);
            colorModel = its.getColorModel();
            SampleModel sampleModel = its.getSampleModel();
            // {
            // int dataType = DataBuffer.TYPE_BYTE;
            // int w = level.getSize().width;
            // int h = level.getSize().height;
            // int pixelStride = 1;
            // int scanlineStride = w;
            // int[] bandOffsets = {0};
            // sampleModel = new ComponentSampleModel(dataType, w, h, pixelStride, scanlineStride,
            // bandOffsets);
            // }
            // Finally, build the image input stream
            final RawImageInputStream raw = new RawImageInputStream(in, its, new long[] { 0 },
                    new Dimension[] { level.getSize() });

            // building the final image layout
            // final Dimension tileSize = ImageUtilities.toTileSize(new Dimension(imageWidth,
            // imageHeight));
            final ImageLayout il = new ImageLayout(0, 0, level.getSize().width,
                    level.getSize().height, level.getXOffset(), level.getYOffset(), pyramidInfo
                            .getTileWidth(), pyramidInfo.getTileHeight(), sampleModel, colorModel);

            // First operator: read the image
            final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);

            // if we don't provide an ImageLayout to the JAI ImageRead
            // operation, it'll try to read the entire raster layer!
            // It's only a slight abuse of the semantics of the word "tile"
            // when we tell JAI that it can tile our image at exactly the
            // size of the section of the raster layer we're looking to render.
            // final ImageLayout layout = new ImageLayout();
            // layout.setTileWidth(tileWidth);
            // layout.setTileHeight(tileHeight);

            ParameterBlock pb = new ParameterBlock();
            pb.add(raw);
            pb.add(new Integer(0));// pyramidLevel
            pb.add(Boolean.FALSE);
            pb.add(Boolean.FALSE);
            pb.add(Boolean.FALSE);
            pb.add(null);
            pb.add(null);
            final ImageReadParam rParam = new ImageReadParam();
            pb.add(rParam);
            RawImageReaderSpi imageIOSPI = new RawImageReaderSpi();
            ImageReader readerInstance = imageIOSPI.createReaderInstance();
            pb.add(readerInstance);

            RenderedOp image = JAI.create("ImageRead", pb, hints);
            Assert.assertNotNull(image);

            File path = new File("/home/groldan/tmp/img/_jai" + pixelType + "-Level_"
                    + pyramidLevel + "-" + numberOfBands + "-band.tiff");
            try {
                ImageIO.write(image, "TIFF", path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            in.close();
        }
    }
}
