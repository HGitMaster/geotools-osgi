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
package org.geotools.gce.imagemosaic;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.media.jai.JAI;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.geotools.coverage.AbstractCoverage;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.test.TestData;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 * Testing {@link ImageMosaicReader}.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public class ImageMosaicReaderTest extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new ImageMosaicReaderTest("testDefaultParameterValue"));
		suite.addTest(new ImageMosaicReaderTest("testInputAlpha"));
		suite.addTest(new ImageMosaicReaderTest("testInputImageROI"));
		suite.addTest(new ImageMosaicReaderTest("testCrop"));
		suite.addTest(new ImageMosaicReaderTest("testErrors"));
		suite.addTest(new ImageMosaicReaderTest("testUpdateBand"));
		

		return suite;
	}

	private URL rgbURL;

	private URL indexURL;

	private URL indexAlphaURL;

	private URL grayURL;

	private URL index_unique_paletteAlphaURL;

	private URL rgbAURL;

	private URL bandsURL;
	
	// private URL morandini;

	private boolean interactive;

	public ImageMosaicReaderTest(String string) {
		super(string);
	}

	/**
	 * Testing input thresholds.
	 * 
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 */
	public void testInputImageROI() throws MismatchedDimensionException,
			NoSuchAuthorityCodeException, IOException {

		 // //
		 //
		 // This image is RGB. If we do thresholding with a value of 100 we will
		 // just throw away a lot of values and we will replace them with 0.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(rgbURL, Double.NaN, null,null,
		 "testInputImageROI-rgbURL", false);
		 imageMosaicSimpleParamsTest(rgbURL, 100, null,null,
		 "testInputImageROI-rgbURL", false);
		
		 // //
		 //
		 // This image has borders that are transparent black which means
		 // (0,0,0,0) hence if we want the mosaic to come out clean and nice we
		 // have to do Overlay with ROI to get superimposition, otherwise blend
		 // with ROI 1 or Blend without ROI to get average!
		 //
		 // //
		 if (interactive) {
		 imageMosaicSimpleParamsTest(rgbAURL, Double.NaN, null,null,
		 "testInputImageROI-rgbAURL-original-superimposition-bad",
		 false);
		 imageMosaicSimpleParamsTest(rgbAURL, 1, null,null,
		 "testInputImageROI-rgbAURL-original-superimposition-good",
		 false);
		 }
		 imageMosaicSimpleParamsTest(rgbAURL, 1, null,null,
		 "testInputImageROI-rgbAURL-fading-ROI", true);
		 imageMosaicSimpleParamsTest(rgbAURL, Double.NaN, null,null,
		 "testInputImageROI-rgbAURL-fading-intrinsic-alpha", true);
		
		 // //
		 //
		 // This images have borders that are black and have a color model that
		 // is IndexColorModel but all with different palette hence a color
		 // conversion will be applied.
		 //
		 // The provided threshold will result in having most part of the input
		 // images replaced by the default background values.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexURL, Double.NaN, null,null,
		 "testInputImageROI-indexURL-original", false);
		 imageMosaicSimpleParamsTest(indexURL, 100, null,null,
		 "testInputImageROI-indexURL", false);
		
		 // //
		 //
		 // This images have borders that are transparent black which means
		 // (0,0,0,0) and have a color model that
		 // is IndexColorModel but all with different palette hence a color
		 // conversion will be applied.
		 //
		 // The provided threshold will result in having most part of the input
		 // images replaced by the default background values which means the
		 // replaced part will be transparent.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexAlphaURL, Double.NaN, null,null,
		 "testInputImageROI-indexAlphaURL-original", false);
		 imageMosaicSimpleParamsTest(indexAlphaURL, 100, null,null,
		 "testInputImageROI-indexAlphaURL", false);
		
		 // //
		 //
		 // Grayscale images. The ROIs will just make them darker because they
		 // will replace part of the input values with zero.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(grayURL, Double.NaN, null,null,
		 "testInputImageROI-grayURL-original", false);
		 imageMosaicSimpleParamsTest(grayURL, 100, null,null,
		 "testInputImageROI-grayURL", false);
		
		 // //
		 //
		 // Grayscale images with index color model with alpha. The ROIs will
		 // just make them more transparent because they
		 // will replace part of the input values with zero.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(index_unique_paletteAlphaURL,
		 Double.NaN, null,null,
		 "testInputImageROI-index_unique_paletteAlphaURL-original",
		 false);
		 imageMosaicSimpleParamsTest(index_unique_paletteAlphaURL, 100, null,null,
		 "testInputImageROI-index_unique_paletteAlphaURL", false);

	}

	/**
	 * Testing both input threshold and alpha.
	 * 
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 * @throws IOException
	 */
	public void testROIAlpha() throws MismatchedDimensionException,
			NoSuchAuthorityCodeException, IOException {

		 boolean interactive = TestData.isInteractiveTest();
		 if (interactive)
		 imageMosaicSimpleParamsTest(rgbURL, 100, null,null,
		 "testROIAlpha-rgbURL-original", false);
		 imageMosaicSimpleParamsTest(rgbURL, 100, Color.black,null,
		 "testROIAlpha-rgbURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(rgbAURL, 5, null,null,
		 "testROIAlpha-rgbAURL-original", false);
		 imageMosaicSimpleParamsTest(rgbAURL, 5, new Color(35, 34, 25),null,
		 "testROIAlpha-rgbAURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexURL, 5, null,null,
		 "testROIAlpha-indexURL-original", false);
		 imageMosaicSimpleParamsTest(indexURL, 5, new Color(58, 49, 8),null,
		 "testROIAlpha-indexURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexAlphaURL, 30, null,null,
		 "testROIAlpha-indexAlphaURL-original", false);
		 imageMosaicSimpleParamsTest(indexAlphaURL, 30, new Color(41, 41, 33),null,
		 "testROIAlpha-indexAlphaURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(grayURL, 100, null,null,
		 "testROIAlpha-grayURL-original", false);
		 imageMosaicSimpleParamsTest(grayURL, 100, Color.black,null,
		 "testROIAlpha-grayURL", false);

	}

	/**
	 * Testing crop capabilities.
	 * 
	 * @throws MismatchedDimensionException
	 * @throws IOException
	 * @throws FactoryException
	 */
	public void testCrop() throws MismatchedDimensionException, IOException,
			FactoryException {
		imageMosaicCropTest(rgbURL, "crop-rgbURL");
		imageMosaicCropTest(indexURL, "crop-indexURL");
		imageMosaicCropTest(grayURL, "crop-grayURL");
		imageMosaicCropTest(indexAlphaURL, "crop-indexAlphaURL");
		imageMosaicCropTest(rgbAURL, "crop-rgbAURL");
		imageMosaicCropTest(index_unique_paletteAlphaURL,
				"crop-index_unique_paletteAlphaURL");

	}

	/**
	 * Tests the {@link ImageMosaicReader} with default parameters for the
	 * various input params.
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 */
	public void testInputAlpha() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {

		 if (interactive)
		 imageMosaicSimpleParamsTest(rgbURL, Double.NaN, null,null,
		 "testFinalAlpha-rgbURL-original", false);
		 imageMosaicSimpleParamsTest(rgbURL, Double.NaN, Color.black,Color.black,
		 "testFinalAlpha-rgbURL", false);
		
		 if (interactive)
		 // the input images have transparency and they do overlap, we need
		 // to ask for blending mosaic.
		 imageMosaicSimpleParamsTest(rgbAURL, Double.NaN, null,null,
		 "testFinalAlpha-rgbAURL-original", true);
		 imageMosaicSimpleParamsTest(rgbAURL, Double.NaN, Color.black,Color.black,// new
		 // Color(35,
		 // 34,
		 // 25),
		 "testFinalAlpha-rgbAURL", false);
		
		 // //
		 //
		 // This images have borders that are black and have a color model that
		 // is IndexColorModel but all with different palette hence a color
		 // conversion will be applied to go to RGB.
		 //
		 // When we do the input transparent color we will add transparency to
		 // the images but only where the transparent color resides. Moreover the
		 // background will be trasparent.
		 //
		 // //
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexURL, Double.NaN, null,null,
		 "testFinalAlpha-indexURL-original", false);
		 imageMosaicSimpleParamsTest(indexURL, Double.NaN, new Color(58, 49,
		 8),Color.black,
		 "testFinalAlpha-indexURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(indexAlphaURL, Double.NaN, null,null,
		 "testFinalAlpha-indexAlphaURL-original", false);
		 imageMosaicSimpleParamsTest(indexAlphaURL, Double.NaN, new Color(41,
		 41, 33),Color.black, "testFinalAlpha-indexAlphaURL", false);
		
		 if (interactive)
		 imageMosaicSimpleParamsTest(grayURL, Double.NaN, null,null,
		 "testFinalAlpha-grayURL-original", false);
		 imageMosaicSimpleParamsTest(grayURL, Double.NaN, Color.black,Color.black,
		 "testFinalAlpha-grayURL", false);
		
		 //
		 // if (interactive)
		 // imageMosaicSimpleParamsTest(morandini, Double.NaN, null,
		 // "testFinalAlpha-morandini-original", false);
		 // imageMosaicSimpleParamsTest(morandini, Double.NaN, Color.white,
		 // "testFinalAlpha-morandini", false);

	}

	/**
	 * Tests the {@link ImageMosaicReader} with default parameters for the
	 * various input params.
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 */
	public void testDefaultParameterValue() throws IOException,
			MismatchedDimensionException, NoSuchAuthorityCodeException {

		imageMosaicSimpleParamsTest(rgbURL, Double.NaN, null, Color.black,
				"testDefaultParameterValue", false);
		 imageMosaicSimpleParamsTest(indexURL, Double.NaN, null, null,
		 "testDefaultParameterValue", false);
		 imageMosaicSimpleParamsTest(grayURL, Double.NaN, null, null,
		 "testDefaultParameterValue", false);
		 imageMosaicSimpleParamsTest(indexAlphaURL, Double.NaN, null, null,
		 "testDefaultParameterValue", false);
	}
	
	
	//
	//test reading the bands in a different order & updating the bands
	public void testUpdateBand() {
		final AbstractGridFormat format = getFormat(bandsURL);
		final ImageMosaicReader reader = getReader(bandsURL, format);
		
		
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
		Set<FeatureId> fids = new HashSet<FeatureId>();
		
        fids.add(ff.featureId("1"));
        Filter filter = ff.id(fids);

        reader.updateBandSelection(filter, new int[]{2, 0, 1}, new double[]{0,0,0});
        try{
        	AbstractCoverage coverage = (AbstractCoverage) reader.read(new GeneralParameterValue[]{});
        	assertNotNull(coverage);
        }catch (Throwable e){
        	assertTrue(false);
        }
	}

	public void testErrors() {
		//error for location attribute
		try{
			((AbstractGridFormat) GridFormatFinder
			.findFormat(rgbURL)).getReader(rgbURL,new Hints(Hints.MOSAIC_LOCATION_ATTRIBUTE,"aaaa"));
			assertTrue(false);
		}catch (Throwable e) {
			
		}
		
		try{
			((AbstractGridFormat) GridFormatFinder
					.findFormat(rgbURL)).getReader(rgbURL,new Hints(Hints.MOSAIC_LOCATION_ATTRIBUTE,"location"));
			assertTrue(true);
		}catch (Throwable e) {
			assertTrue(false);
		}
		
		//error for num tiles
		try{
			((AbstractGridFormat) GridFormatFinder
					.findFormat(rgbURL)).getReader(rgbURL,new Hints(Hints.MAX_ALLOWED_TILES,new Integer(2))).read(null);
			assertTrue(false);
		}catch (Throwable e) {
			assertTrue(true);
		}
		
		try{
			((AbstractGridFormat) GridFormatFinder
					.findFormat(rgbURL)).getReader(rgbURL,new Hints(Hints.MAX_ALLOWED_TILES,new Integer(1000))).read(null);
			assertTrue(true);
		}catch (Exception e) {
		    fail( e.getLocalizedMessage() );
		}
	}

	/**
	 * Tests the {@link ImageMosaicReader}
	 * 
	 * @param title
	 * 
	 * @param threshold
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws NoSuchAuthorityCodeException
	 */
	private void imageMosaicSimpleParamsTest(URL testURL, double roithreshold,
			Color inputTransparent, Color outputTransparent, String title,
			boolean blend) throws IOException, MismatchedDimensionException,
			NoSuchAuthorityCodeException {

		// /////////////////////////////////////////////////////////////////
		//
		// Get the resources as needed.
		//
		//
		// /////////////////////////////////////////////////////////////////
		assertNotNull(testURL);
		final AbstractGridFormat format = getFormat(testURL);
		final ImageMosaicReader reader = getReader(testURL, format);

		// /////////////////////////////////////////////////////////////////
		//
		// limit yourself to reading just a bit of it
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue roiThreshold = (ParameterValue) ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
				.createValue();
		roiThreshold.setValue(new Double(roithreshold));
		final ParameterValue inTransp = (ParameterValue) ImageMosaicFormat.INPUT_TRANSPARENT_COLOR
				.createValue();
		inTransp.setValue(inputTransparent);
		final ParameterValue outTransp = (ParameterValue) ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
				.createValue();
		outTransp.setValue(outputTransparent);
		final ParameterValue blendPV = (ParameterValue) ImageMosaicFormat.FADING
				.createValue();
		blendPV.setValue(blend);

		// /////////////////////////////////////////////////////////////////
		//
		// Test the output coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		testCoverage(reader, new GeneralParameterValue[] { roiThreshold,
				inTransp, blendPV, outTransp }, title);
	}

	/**
	 * Tests the creation of a {@link GridCoverage2D} using the provided
	 * {@link ImageMosaicReader} as well as the provided {@link ParameterValue}.
	 * 
	 * @param reader
	 *            to use for creating a {@link GridCoverage2D}.
	 * @param value
	 *            that control the actions to take for creating a
	 *            {@link GridCoverage2D}.
	 * @param title
	 *            to print out as the head of the frame in case we visualize it.
	 * @throws IOException
	 */
	private void testCoverage(final ImageMosaicReader reader,
			GeneralParameterValue[] values, String title) throws IOException {
		// /////////////////////////////////////////////////////////////////
		//
		// Test the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		AbstractCoverage coverage = (AbstractCoverage) reader.read(values);
		assertNotNull(coverage);
		if (TestData.isInteractiveTest())
			show(((GridCoverage2D) coverage).getRenderedImage(), title);
		else
			((GridCoverage2D) coverage).getRenderedImage().getData();
	}

	/**
	 * Shows the provided {@link RenderedImage} ina {@link JFrame} using the
	 * provided <code>title</code> as the frame's title.
	 * 
	 * @param image
	 *            to show.
	 * @param title
	 *            to use.
	 */
	static void show(RenderedImage image, String title) {
		final JFrame jf = new JFrame(title);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(new ScrollingImagePanel(image, 800, 800));
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				jf.pack();
				jf.setVisible(true);

			}
		});

	}

	/**
	 * returns an {@link AbstractGridCoverage2DReader} for the provided
	 * {@link URL} and for the providede {@link AbstractGridFormat}.
	 * 
	 * @param testURL
	 *            points to a valid object to create an
	 *            {@link AbstractGridCoverage2DReader} for.
	 * @param format
	 *            to use for instantiating such a reader.
	 * @return a suitable {@link ImageMosaicReader}.
	 */
	private ImageMosaicReader getReader(URL testURL,
			final AbstractGridFormat format) {
		return getReader(testURL, format, null);
		
	}

	private ImageMosaicReader getReader(URL testURL,
			final AbstractGridFormat format,Hints hints) {
		final ImageMosaicReader reader = (ImageMosaicReader) format
				.getReader(testURL,hints);
		assertNotNull(reader);
		return reader;
	}
	/**
	 * Tries to get an {@link AbstractGridFormat} for the provided URL.
	 * 
	 * @param testURL
	 *            points to a shapefile that is the index of a certain mosaic.
	 * @return a suitable {@link AbstractGridFormat}.
	 */
	private AbstractGridFormat getFormat(URL testURL) {

		// /////////////////////////////////////////////////////////////////
		//
		// Get a reader
		//
		//
		// /////////////////////////////////////////////////////////////////
		final AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder
				.findFormat(testURL);
		assertNotNull(format);
		assertFalse("UknownFormat",format instanceof UnknownFormat);
		return format;
	}

	/**
	 * Testes {@link ImageMosaicReader} asking to crop the lower left quarter of
	 * the input coverage.
	 * 
	 * @param title
	 *            to use when showing image.
	 * 
	 * @throws IOException
	 * @throws MismatchedDimensionException
	 * @throws FactoryException
	 */
	private void imageMosaicCropTest(URL testURL, String title)
			throws IOException, MismatchedDimensionException, FactoryException {

		// /////////////////////////////////////////////////////////////////
		//
		// Get the resources as needed.
		//
		//
		// /////////////////////////////////////////////////////////////////
		assertNotNull(testURL);
		final AbstractGridFormat format = getFormat(testURL);
		final ImageMosaicReader reader = getReader(testURL, format);

		//
		// /////////////////////////////////////////////////////////////////
		//
		// crop
		//
		//
		// /////////////////////////////////////////////////////////////////
		final ParameterValue gg = (ParameterValue) ImageMosaicFormat.READ_GRIDGEOMETRY2D
				.createValue();
		final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
				oldEnvelope.getLowerCorner().getOrdinate(0)
						+ oldEnvelope.getLength(0) / 2,
				oldEnvelope.getLowerCorner().getOrdinate(1)
						+ oldEnvelope.getLength(1) / 2 }, new double[] {
				oldEnvelope.getUpperCorner().getOrdinate(0),
				oldEnvelope.getUpperCorner().getOrdinate(1) });
		cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());
		gg.setValue(new GridGeometry2D(new GeneralGridRange(new Rectangle(0, 0,
				600, 300)), cropEnvelope));
		final ParameterValue outTransp = (ParameterValue) ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
				.createValue();
		outTransp.setValue(Color.black);

		// /////////////////////////////////////////////////////////////////
		//
		// Show the coverage
		//
		//
		// /////////////////////////////////////////////////////////////////
		testCoverage(reader, new GeneralParameterValue[] { gg, outTransp },
				title);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImageMosaicReaderTest.suite());

	}

	protected void setUp() throws Exception {
		super.setUp();
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				256 * 1024 * 1024);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(5);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(5);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);

		bandsURL = TestData.url(this, "bands/mosaic.shp");
		rgbURL = TestData.url(this, "rgb/mosaic.shp");
		rgbAURL = TestData.url(this, "rgba/modis.shp");
		indexURL = TestData.url(this, "index/modis.shp");
		indexAlphaURL = TestData.url(this, "index_alpha/modis.shp");
		grayURL = TestData.url(this, "gray/dof.shp");
		index_unique_paletteAlphaURL = TestData.url(this,
				"index_alpha_unique_palette/dof.shp");
		//
		// morandini = new File("C:\\work\\data\\m\\m.shp").toURL();

		interactive = TestData.isInteractiveTest();

	}

}
