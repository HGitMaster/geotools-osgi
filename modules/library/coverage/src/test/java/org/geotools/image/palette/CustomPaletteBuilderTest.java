package org.geotools.image.palette;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.Viewer;
import org.geotools.image.ImageWorker;

import junit.framework.TestCase;

/**
 * Testing custom code for color reduction.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class CustomPaletteBuilderTest extends TestCase {

    public void testOneColorBug() {
        // build a transparent image
        BufferedImage image = new BufferedImage(256, 256,
                BufferedImage.TYPE_4BYTE_ABGR);
        
        // create a palette out of it
        CustomPaletteBuilder builder = new CustomPaletteBuilder(image, 256, 1,1, 1);
        builder.buildPalette();
        RenderedImage indexed = builder.getIndexedImage();
        assertTrue(indexed.getColorModel() instanceof IndexColorModel);
        IndexColorModel icm = (IndexColorModel) indexed.getColorModel();
        
        // png encoder go mad if they get a one element palette, we need at least two
        assertEquals(2, icm.getMapSize());
    }
    
    public void testFourColor() {
        // build a transparent image
        BufferedImage image = new BufferedImage(256, 256,
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 10, 10);
        g.setColor(Color.RED);
        g.fillRect(10, 0, 10, 10);
        g.setColor(Color.BLUE);
        g.fillRect(20, 0, 10, 10);
        g.setColor(Color.GREEN);
        g.fillRect(30, 0, 10, 10);
        g.dispose();
        
        //
        // create a palette out of it
        //
        CustomPaletteBuilder builder = new CustomPaletteBuilder(image, 255, 1,1, 1);
        builder.buildPalette();
        RenderedImage indexed = builder.getIndexedImage();
        assertTrue(indexed.getColorModel() instanceof IndexColorModel);
        IndexColorModel icm = (IndexColorModel) indexed.getColorModel();
        
        // make sure we have 4 colors + transparent one
        assertEquals(5, icm.getMapSize());
        
        //
        // now use the JAI op
        //
        assertNotNull((OperationDescriptor)
	     JAI.getDefaultInstance().getOperationRegistry().
             getDescriptor(OperationDescriptor.class, "org.geotools.ColorReduction"));
        ParameterBlockJAI pbj = new ParameterBlockJAI("org.geotools.ColorReduction");
        // I will tile the image in 4 tiles and force parallelism here
        JAI.getDefaultInstance().getTileScheduler().setParallelism(4);
        pbj.addSource(
        		new ImageWorker(image).
        			setRenderingHint(JAI.KEY_IMAGE_LAYOUT, 
        							 new ImageLayout(image).setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(64).setTileWidth(64)
        		).tile().getRenderedImage()
        );
        pbj.setParameter( "numColors", 255);
        pbj.setParameter("alphaThreshold", 1);
        pbj.setParameter("subsampleX", 1);
        pbj.setParameter("subsampleY", 1);
        indexed = JAI.create("org.geotools.ColorReduction", pbj);
        PlanarImage.wrapRenderedImage(indexed).getTiles();
        assertTrue(indexed.getColorModel() instanceof IndexColorModel);

        // check that we get the same results
        assertEquals(indexed.getColorModel(), icm);
        icm = (IndexColorModel) indexed.getColorModel();
        
        // make sure we have 4 colors + transparent one
        assertEquals(5, icm.getMapSize());
        assertEquals(5, icm.getMapSize());
        
        
        //
        // now use the inversion of color
        //
        assertNotNull((OperationDescriptor)
       	     JAI.getDefaultInstance().getOperationRegistry().
                    getDescriptor(OperationDescriptor.class, "org.geotools.ColorInversion"));
        pbj = new ParameterBlockJAI("org.geotools.ColorInversion");
        pbj.addSource(
        		new ImageWorker(image).
        			setRenderingHint(JAI.KEY_IMAGE_LAYOUT, 
        							 new ImageLayout(image).setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(64).setTileWidth(64)
        		).tile().getRenderedImage()
        );
        pbj.setParameter("quantizationColors", InverseColorMapRasterOp.DEFAULT_QUANTIZATION_COLORS);
        pbj.setParameter("alphaThreshold", 1);
        pbj.setParameter("IndexColorModel", icm);
        indexed = JAI.create("org.geotools.ColorInversion", pbj);
        PlanarImage.wrapRenderedImage(indexed).getTiles();   
        assertTrue(indexed.getColorModel() instanceof IndexColorModel);    
        
        // check that we get the same results
        assertEquals(indexed.getColorModel(), icm);
        icm = (IndexColorModel) indexed.getColorModel();
        
        // make sure we have 4 colors + transparent one
        assertEquals(5, icm.getMapSize());
        assertEquals(5, icm.getMapSize());        
    }
}
