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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
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
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    public void testJai_1bit_1Band() throws Exception {
        testJai(RasterCellType.TYPE_1BIT, 1);
    }

    @Test
    public void testJai_1bit_7Band() throws Exception {
        testJai(RasterCellType.TYPE_1BIT, 7);
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
    public void testJai_8bit_U_7Bands() throws Exception {
        testJai(RasterCellType.TYPE_8BIT_U, 7);
    }

    @Test
    public void testJai_16bit_U_1Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 1);
    }

    @Test
    public void testJai_16bit_U_3Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 3);
    }

    @Test
    public void testJai_16bit_U_4Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 4);
    }

    @Test
    public void testJai_16bit_U_7Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_U, 7);
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

    @Test
    @Ignore
    public void testJai_4bit_1Band() throws Exception {
        testJai(RasterCellType.TYPE_4BIT, 1);
    }

    @Test
    public void testJai_16bit_S_1Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_S, 1);
    }

    @Test
    public void testJai_16bit_S_2Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_S, 2);
    }

    @Test
    public void testJai_16bit_S_3Band() throws Exception {
        testJai(RasterCellType.TYPE_16BIT_S, 3);
    }

    private void testJai(final RasterCellType pixelType, final int numberOfBands) throws Exception {
        final String tableName = testData.getRasterTableName(pixelType, numberOfBands);
        testData.loadTestRaster(tableName, numberOfBands, pixelType, null);

        testJai(0, tableName, numberOfBands, pixelType);
        testJai(1, tableName, numberOfBands, pixelType);
        testJai(2, tableName, numberOfBands, pixelType);
        testJai(3, tableName, numberOfBands, pixelType);
        testJai(4, tableName, numberOfBands, pixelType);
    }

    @Test
    public void testJai_RGBSampleImage() throws Exception {
        final String tableName = testData.loadRGBRaster();
        testJai(0, tableName, 3, RasterCellType.TYPE_8BIT_U);
        //testJai(1, tableName, 3, RasterCellType.TYPE_8BIT_U);
//        testJai(2, tableName, 3, RasterCellType.TYPE_8BIT_U);
//        testJai(3, tableName, 3, RasterCellType.TYPE_8BIT_U);
    }

    @Test
    public void testJai_RGBSampleImage_1Band() throws Exception {
        final String tableName = testData.loadRGBRaster();
        testJai(0, tableName, 1, RasterCellType.TYPE_8BIT_U);
        testJai(1, tableName, 1, RasterCellType.TYPE_8BIT_U);
        testJai(2, tableName, 1, RasterCellType.TYPE_8BIT_U);
        testJai(3, tableName, 1, RasterCellType.TYPE_8BIT_U);
    }

    private void testJai(final int pyramidLevel, final String tableName, final int numberOfBands,
            final RasterCellType pixelType) throws Exception {

        SeConnection conn = testData.getConnectionPool().getConnection();
        final SeRow row;

        final int tileW, tileH;
        final Rectangle imageSize;
        try {
            final ArcSDEPyramid pyramidInfo;
            final ArcSDEPyramidLevel level;
            final SeRasterAttr rAttr;
            SeQuery seQuery = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(
                    tableName));
            seQuery.prepareQuery();
            seQuery.execute();
            row = seQuery.fetch();
            rAttr = row.getRaster(0);

            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
            pyramidInfo = new ArcSDEPyramid(rAttr, crs);
            level = pyramidInfo.getPyramidLevel(pyramidLevel);
            tileW = rAttr.getTileWidth();
            tileH = rAttr.getTileHeight();

            SeRasterConstraint rConstraint = new SeRasterConstraint();
            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }
            // if(numberOfBands == 3){
            // bandsToQuery = new int[]{1,2,3};
            // }else{
            // bandsToQuery = new int[]{1};
            // }

            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);

            rConstraint.setEnvelope(0, 0, 5, 5);
            imageSize = new Rectangle(pyramidInfo.getTileWidth() * 6,
                    pyramidInfo.getTileHeight() * 6);
            // rConstraint.setEnvelope(0, 0, level.getNumTilesWide() - 1, level.getNumTilesHigh() -
            // 1); // which
            // tiles
            // ...
            int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;
            rConstraint.setInterleave(interleaveType);
            seQuery.queryRasterTile(rConstraint);

        } catch (SeException se) {
            conn.close();
            throw new ArcSdeException(se);
        }

        final ImageInputStream in;

        try {
            TileReader reader = TileReader.getInstance(conn, pixelType, row, numberOfBands, tileW,
                    tileH);
            in = new ArcSDETiledImageInputStream(reader);
        } catch (IOException e) {
            conn.close();
            throw e;
        }

        try {
            // Prepare temporaray colorModel and sample model, needed to build the final
            // ArcSDEPyramidLevel level;

            // RawImageInputStream
            final ImageTypeSpecifier its;

            its = RasterUtils.createImageTypeSpec(pixelType, numberOfBands, imageSize.width,
                    imageSize.height, tileW, tileH);
            ColorModel colorModel = its.getColorModel();
            SampleModel sampleModel;
            {
                SampleModel sm = its.getSampleModel();
                sampleModel = sm.createCompatibleSampleModel(imageSize.width, imageSize.height);

                int[] bankIndices = new int[numberOfBands];
                int[] bandOffsets = new int[numberOfBands];

                int bandOffset = (tileW * tileH * pixelType.getBitsPerSample()) / 8;

                for (int i = 0; i < numberOfBands; i++) {
                    bankIndices[i] = i;
                    bandOffsets[i] = 0;// (i * bandOffset);
                }
                sampleModel = new BandedSampleModel(pixelType.getDataBufferType(), imageSize.width,
                        imageSize.height, tileW, bankIndices, bandOffsets);
            }

            // Finally, build the image input stream
            // final RawImageInputStream raw = new RawImageInputStream(in, its, new long[] { 0 },
            // new Dimension[] { level.getSize() });

            final RawImageInputStream raw = new RawImageInputStream(in, sampleModel,
                    new long[] { 0 }, new Dimension[] { new Dimension(imageSize.width,
                            imageSize.height) });

            // building the final image layout
            final ImageLayout imageLayout;
            {
                int minX = 0;
                int minY = 0;
                int width = imageSize.width;
                int height = imageSize.height;
                // case 1:
                // its = ImageTypeSpecifier.createGrayscale(pixelType.getBitsPerSample(), pixelType
                // .getDataBufferType(), pixelType.isSigned());
                // break;
                // case 3:
                // ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                int tileGridXOffset = 0;//level.getXOffset();
                int tileGridYOffset = 0;//level.getYOffset();

                if (tileGridXOffset != 0 || tileGridYOffset != 0) {
                    System.out.println("here");
                }
                imageLayout = new ImageLayout(minX, minY, width, height, tileGridXOffset,
                        tileGridYOffset, tileW, tileH, sampleModel, colorModel);
            }

            // First operator: read the image
            final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);

            ParameterBlock pb = new ParameterBlock();
            pb.add(raw);
            /*
             * image index, always 0 since we're already fetching the required pyramid level
             */
            pb.add(Integer.valueOf(0));
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

            String file = System.getProperty("user.home");
            file += File.separator + "arcsde_test" + File.separator + "_jai" + pixelType
                    + "-Level_" + pyramidLevel + "-" + numberOfBands + "-band.tiff";
            File path = new File(file);
            path.getParentFile().mkdirs();
            System.out.println("\n --- Writing to " + file);
            try {
                ImageIO.write(image, "TIFF", path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Assert.assertEquals(-1, in.read());

        } finally {
            in.close();
        }
    }
}
