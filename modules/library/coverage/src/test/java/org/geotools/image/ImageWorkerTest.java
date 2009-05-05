/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.Viewer;
import org.geotools.test.TestData;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the {@link ImageWorker} implementation.
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/coverage/src/test/java/org/geotools/image/ImageWorkerTest.java $
 * @version $Id: ImageWorkerTest.java 32940 2009-05-05 06:18:23Z simonegiannecchini $
 * @author Simone Giannecchini (GeoSolutions)
 * @author Martin Desruisseaux (Geomatys)
 */
public final class ImageWorkerTest {
    /**
     * Image to use for testing purpose.
     */
    private static RenderedImage sstImage, worldImage, chlImage, bathy, smallWorld, gray, grayAlpha;

    /**
     * {@code true} if the image should be visualized.
     */
    private static final boolean SHOW = TestData.isInteractiveTest();

    /**
     * Loads the image (if not already loaded) and creates the worker instance.
     *
     * @throws IOException If the image was not found.
     */
    @Before
    public void setUp() throws IOException {
        if (sstImage == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "QL95209.png");
            sstImage = ImageIO.read(input);
            input.close();
        }
        if (worldImage == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "world.png");
            worldImage = ImageIO.read(input);
            input.close();
        }
        if (chlImage == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "CHL01195.png");
            chlImage = ImageIO.read(input);
            input.close();
        }
        if (bathy == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "BATHY.png");
            bathy = ImageIO.read(input);
            input.close();
        }
        
        if (smallWorld == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "small_world.png");
            smallWorld = ImageIO.read(input);
            input.close();
        }        

        if (gray == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "gray.png");
            gray = ImageIO.read(input);
            input.close();
        }   
        
        if (grayAlpha == null) {
            final InputStream input = TestData.openStream(GridCoverage2D.class, "gray-alpha.png");
            grayAlpha = ImageIO.read(input);
            input.close();
        }          
    }

    /**
     * Tests capability to write GIF image.
     *
     * @throws IOException If an error occured while writting the image.
     */
    @Test
    public void testGIFImageWrite() throws IOException {
        // Get the image of the world with transparency.
        ImageWorker worker = new ImageWorker(worldImage);
        show(worker, "Input GIF");
        if (false) {
            final RenderedImage image = worker.getRenderedImage();
            final ColorModel cm = image.getColorModel();
            assertTrue("wrong color model", cm instanceof IndexColorModel);
            assertEquals("wrong transparency model", Transparency.BITMASK, cm.getTransparency());
            assertEquals("wrong transparency index", 255, ((IndexColorModel) cm).getTransparentPixel());
        }
        // Writes it out as GIF on a file using index color model with floyd stenberg algorithm.
        final File outFile = TestData.temp(this, "temp.gif");
        worker.forceIndexColorModelForGIF(true);
        worker.writeGIF(outFile, "LZW", 0.75f);

        // Read it back
        final ImageWorker readWorker = new ImageWorker(ImageIO.read(outFile));
        show(readWorker, "GIF to file");
        if (false) {
            final RenderedImage image = readWorker.getRenderedImage();
            final ColorModel cm = image.getColorModel();
            assertTrue("wrong color model", cm instanceof IndexColorModel);
            assertEquals("wrong transparency model", Transparency.BITMASK, cm);
            assertEquals("wrong transparency index", 255, ((IndexColorModel) cm).getTransparentPixel());
        }
        // Write on an output streams.
        ImageIO.setUseCache(true);
        final OutputStream os = new FileOutputStream(outFile);
        worker = new ImageWorker(worldImage);
        worker.forceIndexColorModelForGIF(true);
        worker.writeGIF(os, "LZW", 0.75f);

        // Read it back.
        readWorker.setImage(ImageIO.read(outFile));
        show(readWorker, "GIF to output stream");
        if (false) {
            final RenderedImage image = readWorker.getRenderedImage();
            final ColorModel cm = image.getColorModel();
            assertTrue("wrong color model", cm instanceof IndexColorModel);
            assertEquals("wrong transparency model", Transparency.BITMASK, cm);
            assertEquals("wrong transparency index", 255, ((IndexColorModel) cm).getTransparentPixel());
        }
        outFile.delete();
    }

    /**
     * Testing JPEG capabilities.
     *
     * @throws IOException If an error occured while writting the image.
     */
    @Test
    public void testJPEGWrite() throws IOException {
        // get the image of the world with transparency
        final ImageWorker worker = new ImageWorker(worldImage);
        show(worker, "Input JPEG");

        // /////////////////////////////////////////////////////////////////////
        // nativeJPEG  with compression JPEG-LS
        // TODO: Disabled for now, because Continuum fails in this case.
        // /////////////////////////////////////////////////////////////////////
        final File outFile = TestData.temp(this, "temp.jpeg");
        if (false) {
            worker.writeJPEG(outFile, "JPEG-LS", 0.75f, true);
            final ImageWorker readWorker = new ImageWorker(ImageIO.read(outFile));
            show(readWorker, "Native JPEG LS");
        }

        // /////////////////////////////////////////////////////////////////////
        // native JPEG compression
        // /////////////////////////////////////////////////////////////////////
        worker.setImage(worldImage);
        worker.writeJPEG(outFile, "JPEG", 0.75f, true);
        final ImageWorker readWorker = new ImageWorker(ImageIO.read(outFile));
        show(readWorker, "native JPEG");

        // /////////////////////////////////////////////////////////////////////
        // pure java JPEG compression
        // /////////////////////////////////////////////////////////////////////
        worker.setImage(worldImage);
        worker.writeJPEG(outFile, "JPEG", 0.75f, false);
        readWorker.setImage(ImageIO.read(outFile));
        show(readWorker, "Pure Java JPEG");
        outFile.delete();
    }

    /**
     * Testing PNG capabilities.
     *
     * @throws IOException If an error occured while writting the image.
     */
    @Test
    public void testPNGWrite() throws IOException {
        // Get the image of the world with transparency.
        final ImageWorker worker = new ImageWorker(worldImage);
        show(worker, "Input file");

        // /////////////////////////////////////////////////////////////////////
        // native png filtered compression 24 bits
        // /////////////////////////////////////////////////////////////////////
        final File outFile = TestData.temp(this, "temp.png");
        worker.writePNG(outFile, "FILTERED", 0.75f, true,false);
        final ImageWorker readWorker = new ImageWorker(ImageIO.read(outFile));
        show(readWorker, "Native PNG24");

        // /////////////////////////////////////////////////////////////////////
        // native png filtered compression 8 bits
        // /////////////////////////////////////////////////////////////////////
        worker.setImage(worldImage);
        worker.writePNG(outFile, "FILTERED", 0.75f, true,true);
        readWorker.setImage(ImageIO.read(outFile));
        show(readWorker, "native PNG8");

        // /////////////////////////////////////////////////////////////////////
        // pure java png 24
        // /////////////////////////////////////////////////////////////////////
        worker.setImage(worldImage);
        worker.writePNG(outFile, "FILTERED", 0.75f, false,false);
        readWorker.setImage(ImageIO.read(outFile));
        show(readWorker, "Pure  PNG24");

        // /////////////////////////////////////////////////////////////////////
        // pure java png 8
        // /////////////////////////////////////////////////////////////////////
        worker.setImage(worldImage);
        worker.writePNG(outFile, "FILTERED", 0.75f, false,true);
        readWorker.setImage(ImageIO.read(outFile));
        show(readWorker, "Pure  PNG8");
        outFile.delete();
    }

    /**
     * Tests the conversion between RGB and indexed color model.
     */
    public void testRGB2Palette(){
        final ImageWorker worker = new ImageWorker(worldImage);
        show(worker, "Input file");
        worker.forceIndexColorModelForGIF(true);

        // Convert to to index color bitmask
        if (false) {
            final ColorModel cm = worker.getRenderedImage().getColorModel();
            assertTrue("wrong color model", cm instanceof IndexColorModel);
            assertEquals("wrong transparency model", Transparency.BITMASK, cm.getTransparency());
            assertEquals("wrong transparency index", 255, ((IndexColorModel) cm).getTransparentPixel());
        }
        show(worker, "Paletted bitmask");

        // Go back to rgb.
        worker.forceComponentColorModel();
        if (false) {
            final ColorModel cm = worker.getRenderedImage().getColorModel();
            assertTrue("wrong color model", cm instanceof ComponentColorModel);
            assertEquals("wrong bands number", 4, cm.getNumComponents());
            assertEquals("wrong transparency model", Transparency.TRANSLUCENT, cm.getTransparency());
        }
        show(worker, "RGB translucent");
    }
    /**
     * Tests the {@link ImageWorker#makeColorTransparent} methods.
     * Some trivial tests are performed before.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws IllegalStateException 
     */
    @Test
    public void testMakeColorTransparent() throws IllegalStateException, FileNotFoundException, IOException {
        assertTrue("Assertions should be enabled.", ImageWorker.class.desiredAssertionStatus());
        ImageWorker worker = new ImageWorker(sstImage);
        
        assertSame(sstImage, worker.getRenderedImage());
        assertEquals(  1, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse (     worker.isBinary());
        assertTrue  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertFalse (     worker.isTranslucent());

        assertSame("Expected no operation.", sstImage, worker.rescaleToBytes()           .getRenderedImage());
        assertSame("Expected no operation.", sstImage, worker.forceIndexColorModel(false).getRenderedImage());
        assertSame("Expected no operation.", sstImage, worker.forceIndexColorModel(true ).getRenderedImage());
        assertSame("Expected no operation.", sstImage, worker.forceColorSpaceRGB()       .getRenderedImage());
        assertSame("Expected no operation.", sstImage, worker.retainFirstBand()          .getRenderedImage());
        assertSame("Expected no operation.", sstImage, worker.retainLastBand()           .getRenderedImage());

        // Following will change image, so we need to test after the above assertions.
        assertEquals(  0, worker.getMinimums()[0], 0);
        assertEquals(255, worker.getMaximums()[0], 0);
        assertNotSame(sstImage, worker.getRenderedImage());
        assertSame("Expected same databuffer, i.e. pixels should not be duplicated.",
                   sstImage.getTile(0,0).getDataBuffer(),
                   worker.getRenderedImage().getTile(0,0).getDataBuffer());

        assertSame(worker, worker.makeColorTransparent(Color.WHITE));
        assertEquals(255,  worker.getTransparentPixel());
        assertFalse (      worker.isTranslucent());
        assertSame("Expected same databuffer, i.e. pixels should not be duplicated.",
                   sstImage.getTile(0,0).getDataBuffer(),
                   worker.getRenderedImage().getTile(0,0).getDataBuffer());
        

        // INDEX TO INDEX-ALPHA
        worker=new ImageWorker(chlImage).makeColorTransparent(Color.black);
        show(worker,  "CHL01195.png");
        assertEquals(  1, worker.getNumBands());
        assertEquals( 0, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertTrue  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertFalse (     worker.isTranslucent());        
        RenderedImage image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof IndexColorModel);
        IndexColorModel iColorModel=(IndexColorModel) image.getColorModel();
        int transparentColor=iColorModel.getRGB(worker.getTransparentPixel())&0x00ffffff;
        assertTrue  (     transparentColor==0);
        

        
        // INDEX TO INDEX-ALPHA
        worker=new ImageWorker(bathy).makeColorTransparent(Color.WHITE);
        show(worker,  "BATHY.png");
        assertEquals(  1, worker.getNumBands());
        assertEquals( 206, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertTrue  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertFalse (     worker.isTranslucent());        
        image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof IndexColorModel);
        iColorModel=(IndexColorModel) image.getColorModel();
        transparentColor=iColorModel.getRGB(worker.getTransparentPixel())&0x00ffffff;
        assertTrue  (     transparentColor==(Color.WHITE.getRGB()&0x00ffffff));        
        
        // RGB TO RGBA
        worker=new ImageWorker(smallWorld).makeColorTransparent(new Color(11,10,50));
        show(worker,  "small_world.png");
        assertEquals(  4, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertTrue (     worker.isTranslucent());        
        image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof ComponentColorModel);  
                
        
        // RGBA to RGBA
        worker=new ImageWorker(worldImage).makeColorTransparent(Color.white);
        show(worker,  "world.png");
        assertEquals(  4, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse  (     worker.isIndexed());
        assertTrue  (     worker.isColorSpaceRGB());
        assertFalse (     worker.isColorSpaceGRAYScale());
        assertTrue (     worker.isTranslucent());        
        image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof ComponentColorModel);  
        
        
        // GRAY TO GRAY-ALPHA
        worker=new ImageWorker(gray).makeColorTransparent(Color.black);
        show(worker,  "gray.png");
        assertEquals(  2, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse  (     worker.isIndexed());
        assertFalse  (     worker.isColorSpaceRGB());
        assertTrue (     worker.isColorSpaceGRAYScale());
        assertTrue (     worker.isTranslucent());        
        image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof ComponentColorModel);  
        
        // GRAY-ALPHA TO GRAY-ALPHA.
        worker=new ImageWorker(grayAlpha).makeColorTransparent(Color.black);
        show(worker,  "gray-alpha.png");  
        assertEquals(  2, worker.getNumBands());
        assertEquals( -1, worker.getTransparentPixel());
        assertTrue  (     worker.isBytes());
        assertFalse  (     worker.isIndexed());
        assertFalse  (     worker.isColorSpaceRGB());
        assertTrue (     worker.isColorSpaceGRAYScale());
        assertTrue (     worker.isTranslucent());        
        image= worker.getRenderedImage();
        assertTrue  (     image.getColorModel() instanceof ComponentColorModel);         
        
        
    }
    /**
     * Visualize the content of given image if {@link #SHOW} is {@code true}.
     *
     * @param worker The worker for which to visualize the image.
     * @param title  The title to be given to the windows.
     */
    private static void show(final ImageWorker worker, final String title) {
        if (SHOW) {
            Viewer.show(worker.getRenderedImage(), title);
        } else {
            assertNotNull(worker.getRenderedImage().getTile(worker.getRenderedImage().getMinTileX(), worker.getRenderedImage().getMinTileY())); // Force computation.
        }
    }
}
