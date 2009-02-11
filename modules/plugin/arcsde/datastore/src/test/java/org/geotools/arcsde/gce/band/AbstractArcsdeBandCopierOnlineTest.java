package org.geotools.arcsde.gce.band;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.gce.RasterCellType;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.RasterTestData.PixelSampler;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public abstract class AbstractArcsdeBandCopierOnlineTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger("org.geotools.arcsde.gce");
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    /**
     * Implementations should provide the compatible {@link BufferedImage} for the given dimensions,
     * pixel type and number of bands the {@link ArcSDERasterBandCopier} is gonna be writing to
     * 
     * @param width
     *            the width of the image to return
     * @param height
     *            the height of the image to return
     * 
     * @param numBands
     *            the number of bands for the returned image
     * @return an image of {@code width x height} compatible with the concrete test pixel type
     *         target
     */
    protected abstract BufferedImage getTargetImage(final int width, final int height,
            final int numBands);

    protected void testArcSDEBandCopier(final int numBands, final RasterCellType pixelType,
            final IndexColorModel colorModel) throws Exception {

        final String tableName;
        if (colorModel == null) {
            tableName = rasterTestData.getRasterTableName(pixelType, numBands);
        } else {
            tableName = rasterTestData.getRasterTableName(pixelType, numBands, true);
        }
        rasterTestData.loadTestRaster(tableName, numBands, pixelType, colorModel);

        ArcSDEConnectionPool pool = rasterTestData.getConnectionPool();
        ArcSDEPooledConnection session = null;
        try {
            session = pool.getConnection();

            final SeRasterAttr rAttr;
            SeRow row;
            {
                SeQuery seQuery = new SeQuery(session, new String[] { "RASTER" },
                        new SeSqlConstruct(tableName));
                seQuery.prepareQuery();
                seQuery.execute();
                row = seQuery.fetch();
                rAttr = row.getRaster(0);

                int[] bands = new int[numBands];
                for (int bandN = 1; bandN <= numBands; bandN++) {
                    bands[bandN - 1] = bandN;
                }

                SeRasterConstraint rConstraint = new SeRasterConstraint();
                rConstraint.setBands(bands);
                rConstraint.setLevel(0);
                rConstraint.setEnvelope(0, 0, 3, 3); // which tiles...
                rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);
                seQuery.queryRasterTile(rConstraint);
            }

            final int width = rAttr.getBandWidth();
            final int height = rAttr.getBandHeight();
            final BufferedImage fromSdeImage = getTargetImage(width, height, numBands);
            final ArcSDERasterBandCopier bandCopier;
            final int tileWidth = rAttr.getTileWidth();
            final int tileHeight = rAttr.getTileHeight();
            {
                bandCopier = ArcSDERasterBandCopier.getInstance(pixelType, tileWidth, tileHeight);
            }
            SeRasterTile rTile = row.getRasterTile();
            final WritableRaster targetRaster = fromSdeImage.getRaster();
            while (rTile != null) {
                int columnIndex = rTile.getColumnIndex();
                int rowIndex = rTile.getRowIndex();
                int offsetX = tileWidth * columnIndex;
                int offsetY = tileHeight * rowIndex;

                BufferedImage subtile = fromSdeImage.getSubimage(offsetX, offsetY, tileWidth,
                        tileHeight);
                WritableRaster destinationSubTile = subtile.getRaster();

                for (int targetBand = 0; targetBand < numBands; targetBand++) {
                    bandCopier.copyPixelData(rTile, destinationSubTile, 0, 0, targetBand);
                }
                rTile = row.getRasterTile();
            }

            final PixelSampler sampler = PixelSampler.getSampler(pixelType);
            final BufferedImage expectedImage = getTargetImage(width, height, numBands);
            // fill in expected image with expected values
            sampler.setImageData(expectedImage.getRaster());
            final DataBuffer expectedData = expectedImage.getRaster().getDataBuffer();
            // final byte[] expectedData = sampler.getImgBandData(width, height, 0, numBands);
            final int dataSize = width * height;

            // fromSdeData is an array of the raster's pixelType type (ie, int[], short[], float[],
            // etc)
            final DataBuffer fromSdeData = targetRaster.getDataBuffer();

            ImageIO.write(fromSdeImage, "TIFF", new File("/tmp/testArcSDEBandCopier-" + pixelType
                    + "-" + numBands + "-band.tiff"));

            Number fromSdeBandSampleValue;
            Number expectedBandSampleValue;
            for (int bandN = 0; bandN < numBands; bandN++) {
                for (int pixelIdx = 0; pixelIdx < dataSize; pixelIdx++) {
                    fromSdeBandSampleValue = getReadPixel(fromSdeData, pixelIdx, bandN);
                    expectedBandSampleValue = getReadPixel(expectedData, pixelIdx, bandN);
                    Assert.assertEquals("Sample #" + pixelIdx + " at band " + (bandN + 1) + " of "
                            + numBands + " differs", expectedBandSampleValue,
                            fromSdeBandSampleValue);
                }
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    protected Number readPixelValue(DataInputStream expectedDataIn, RasterCellType pixelType)
            throws IOException {
        Number pixelValue;
        switch (pixelType) {
        case TYPE_8BIT_S:
            pixelValue = expectedDataIn.readByte();
            break;
        case TYPE_8BIT_U:
            pixelValue = expectedDataIn.readUnsignedByte();
            break;
        case TYPE_16BIT_S:
            pixelValue = expectedDataIn.readShort();
            break;
        case TYPE_16BIT_U:
            pixelValue = expectedDataIn.readUnsignedShort();
            break;
        case TYPE_32BIT_S:
            pixelValue = expectedDataIn.readInt();
            break;
        case TYPE_32BIT_U:
            pixelValue = expectedDataIn.readInt();
            break;
        default:
            throw new IllegalArgumentException("bytesToPixelValue not implemented for " + pixelType);
        }
        return pixelValue;
    }

    /**
     * Returns the pixel value from {@code array} at index {@code pixelIdx}
     * 
     * @param array
     *            the array of {@code pixelType} primitives as read by an
     *            {@link ArcSDERasterBandCopier} into a {@link WritableRaster}
     * @param pixelIdx
     *            the index of the pixel in the {@code array} array to return
     * @param numBank
     *            zero-indexed number of the bank (aka, image band)
     * @return
     */
    protected Number getReadPixel(final DataBuffer data, final int pixelIdx, final int numBand) {
        final int dataType = data.getDataType();
        Number fromSdePixelValue;
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_INT:
            fromSdePixelValue = data.getElem(numBand, pixelIdx);
            break;
        case DataBuffer.TYPE_FLOAT:
            fromSdePixelValue = data.getElemFloat(numBand, pixelIdx);
            break;
        case DataBuffer.TYPE_DOUBLE:
            fromSdePixelValue = data.getElemDouble(numBand, pixelIdx);
            break;
        default:
            throw new IllegalArgumentException("getReadPixel not implemented for raster data type "
                    + dataType);
        }
        return fromSdePixelValue;
    }
}