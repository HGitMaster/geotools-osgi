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
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.operator.ConstantDescriptor;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This reader is repsonsible for providing access to mosaic of georeferenced
 * images. Citing JAI documentation:
 * 
 * The "Mosaic" operation creates a mosaic of two or more source images. This
 * operation could be used for example to assemble a set of overlapping
 * geospatially rectified images into a contiguous image. It could also be used
 * to create a montage of photographs such as a panorama.
 * 
 * All source images are assumed to have been geometrically mapped into a common
 * coordinate space. The origin (minX, minY) of each image is therefore taken to
 * represent the location of the respective image in the common coordinate
 * system of the source images. This coordinate space will also be that of the
 * destination image.
 * 
 * All source images must have the same data type and sample size for all bands
 * and have the same number of bands as color components. The destination will
 * have the same data type, sample size, and number of bands and color
 * components as the sources.
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
@SuppressWarnings("deprecation")
public final class ImageMosaicReader extends AbstractGridCoverage2DReader
		implements GridCoverageReader {

	/**
	 * Releases resources held by this reader.
	 * 
	 */
	public synchronized void dispose() {
		super.dispose();
		try {
			tileIndexStore.dispose();
		} catch (Throwable e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
		}
	}

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging
			.getLogger("org.geotools.gce.imagemosaic");

	/**
	 * The source {@link URL} pointing to the index shapefile for this
	 * {@link ImageMosaicReader}.
	 */
	private final URL sourceURL;

	/** {@link DataStore} pointed to the index shapefile. */
	private final DataStore tileIndexStore;

	/** {@link SoftReference} to the index holding the tiles' envelopes. */
	private SoftReference<MemorySpatialIndex> index;

	/**
	 * The typename of the chems inside the {@link ShapefileDataStore} that
	 * contains the index for this {@link ImageMosaicReader}.
	 */
	private final String typeName;

	/** {@link FeatureSource} for the shape index. */
	private final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

	private boolean expandMe;

	/**
	 * I <code>true</code> it tells us if the mosaic points to absolute paths or
	 * to relative ones. (in case of <code>false</code>).
	 */
	private boolean absolutePath;

	/**
	 * Max number of tiles that this plugin will load.
	 * 
	 * If this number is exceeded, i.e. we request an area which is too large
	 * instead of getting stuck with opening thousands of files I give you back
	 * a fake coverage.
	 */
	private int maxAllowedTiles = ((Integer) ImageMosaicFormat.MAX_ALLOWED_TILES.getDefaultValue()).intValue();

	private String locationAttributeName;
	private String bandSelectAttributeName; 		//name of the shapefile attribute that contains the band selection (may be null)
	private String colorCorrectionAttributeName;	//name of the shapefile attribute attribute that contains the color corrections (may be null)

	private boolean hasBandSelectAttribute = false;			//cached version of if the shapefile has band select attribute (set on creating the shapefile index) 
	private boolean hasColorCorrectionAttribute = false; 	//cached version of if the shapefile has band select attribute (set on creating the shapefile index)
	
	/**
	 * COnstructor.
	 * 
	 * @param source
	 *            The source object.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public ImageMosaicReader(Object source, Hints uHints) throws IOException {
		// //
		//
		// managing hints
		//
		// //
		if (this.hints == null)
			this.hints = new Hints();
		if (uHints != null) {
			this.hints.add(uHints);
		}
		this.coverageFactory = CoverageFactoryFinder
				.getGridCoverageFactory(this.hints);
		// set the maximum number of tile to load
		if (this.hints.containsKey(Hints.MAX_ALLOWED_TILES))
			this.maxAllowedTiles = ((Integer) this.hints
					.get(Hints.MAX_ALLOWED_TILES)).intValue();
		if (this.hints.containsKey(Hints.MOSAIC_LOCATION_ATTRIBUTE))
			this.locationAttributeName = ((String) this.hints
					.get(Hints.MOSAIC_LOCATION_ATTRIBUTE));
		if (this.hints.containsKey(Hints.MOSAIC_BANDSELECTION_ATTRIBUTE))
			this.bandSelectAttributeName = ((String) this.hints
					.get(Hints.MOSAIC_BANDSELECTION_ATTRIBUTE));
		if (this.hints.containsKey(Hints.MOSAIC_COLORCORRECTION_ATTRIBUTE))
			this.colorCorrectionAttributeName = ((String)this.hints.get(Hints.MOSAIC_COLORCORRECTION_ATTRIBUTE));

		// /////////////////////////////////////////////////////////////////////
		//
		// Check source
		//
		// /////////////////////////////////////////////////////////////////////
		if (source == null) {

			final IOException ex = new IOException(
					"ImageMosaicReader:No source set to read this coverage.");
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
			throw new DataSourceException(ex);
		}
		this.source = source;
		if (source instanceof File)
			this.sourceURL = ((File) source).toURL();
		else if (source instanceof URL)
			this.sourceURL = (URL) source;
		else if (source instanceof String) {
			final File tempFile = new File((String) source);
			if (tempFile.exists()) {
				this.sourceURL = tempFile.toURL();
			} else
				try {
					this.sourceURL = new URL(URLDecoder.decode((String) source,
							"UTF8"));
					if (this.sourceURL.getProtocol() != "file") {
						throw new IllegalArgumentException(
								"This plugin accepts only File,  URL and String pointing to a file");

					}
				} catch (MalformedURLException e) {
					if (LOGGER.isLoggable(Level.WARNING))
						LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
					throw new IllegalArgumentException(
							"This plugin accepts only File,  URL and String pointing to a file");

				} catch (UnsupportedEncodingException e) {
					if (LOGGER.isLoggable(Level.WARNING))
						LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
					throw new IllegalArgumentException(
							"This plugin accepts only File,  URL and String pointing to a file");

				}

		} else
			throw new IllegalArgumentException(
					"This plugin accepts only File, URL and String pointing to a file");

		// /////////////////////////////////////////////////////////////////////
		//
		// Load tiles informations, especially the bounds, which will be
		// reused
		//
		// /////////////////////////////////////////////////////////////////////
		ShapefileDataStoreFactory sf = new ShapefileDataStoreFactory();	
		tileIndexStore = sf.createDataStore(this.sourceURL);
		
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Connected mosaic reader to its data store "
					+ sourceURL.toString());
		final String[] typeNames = tileIndexStore.getTypeNames();
		if (typeNames.length <= 0)
			throw new IllegalArgumentException(
					"Problems when opening the index, no typenames for the schema are defined");

		typeName = typeNames[0];
		featureSource = tileIndexStore.getFeatureSource(typeName);
		
		// //
		//
		// get the crs if able to
		//
		// //
		final Object tempCRS = this.hints
				.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
		if (tempCRS != null) {
			this.crs = (CoordinateReferenceSystem) tempCRS;
			LOGGER.log(Level.WARNING, new StringBuffer(
					"Using forced coordinate reference system ").append(
					crs.toWKT()).toString());
		} else {
			final CoordinateReferenceSystem tempcrs = featureSource.getSchema()
					.getGeometryDescriptor().getCoordinateReferenceSystem();
			if (tempcrs == null) {
				// use the default crs
				crs = AbstractGridFormat.getDefaultCRS();
				LOGGER
						.log(
								Level.WARNING,
								new StringBuffer(
										"Unable to find a CRS for this coverage, using a default one: ")
										.append(crs.toWKT()).toString());
			} else
				crs = tempcrs;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Load properties file with information about levels and envelope
		//
		// /////////////////////////////////////////////////////////////////////
		// property file
		loadProperties();

		// determine location attribute name
		//if no supplied in properties file or hints then
		//"guess" at the first string attribute
		final SimpleFeatureType schema = featureSource.getSchema();
		if (this.locationAttributeName == null) {
			// get the first string
			for (AttributeDescriptor attribute : schema
					.getAttributeDescriptors()) {
				if (attribute.getType().getBinding().equals(String.class))
					this.locationAttributeName = attribute.getName().toString();
			}
		}
		if (schema.getDescriptor(this.locationAttributeName) == null)
			throw new DataSourceException(
					"The provided name for the location attribute is invalid.");

		
		// //
		//
		// Load all the features inside the index
		//
		// //
		createIndex();
	}

	/**
	 * Loads the properties file that contains useful information about this
	 * coverage.
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void loadProperties() throws UnsupportedEncodingException,
			IOException, FileNotFoundException {

		String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
		final int index = temp.lastIndexOf(".");
		if (index != -1)
			temp = temp.substring(0, index);
		final File propertiesFile = new File(new StringBuffer(temp).append(
				".properties").toString());
		if (!propertiesFile.exists() || !propertiesFile.isFile()) {
			throw new FileNotFoundException(
					"Properties file, descibing the ImageMoasic, does not exist:"
							+ propertiesFile);
		}
		final Properties properties = new Properties();
		properties.load(new BufferedInputStream(new FileInputStream(
				propertiesFile)));

		// load the envelope
		final String envelope = properties.getProperty("Envelope2D");
		String[] pairs = envelope.split(" ");
		final double cornersV[][] = new double[2][2];
		String pair[];
		for (int i = 0; i < 2; i++) {
			pair = pairs[i].split(",");
			cornersV[i][0] = Double.parseDouble(pair[0]);
			cornersV[i][1] = Double.parseDouble(pair[1]);
		}
		this.originalEnvelope = new GeneralEnvelope(cornersV[0], cornersV[1]);
		this.originalEnvelope.setCoordinateReferenceSystem(crs);

		// resolutions levels
		numOverviews = Integer.parseInt(properties.getProperty("LevelsNum")) - 1;
		final String levels = properties.getProperty("Levels");
		pairs = levels.split(" ");
		overViewResolutions = numOverviews >= 1 ? new double[numOverviews][2]
				: null;
		pair = pairs[0].split(",");
		highestRes = new double[2];
		highestRes[0] = Double.parseDouble(pair[0]);
		highestRes[1] = Double.parseDouble(pair[1]);

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
					.append(" ").append(highestRes[1]).toString());

		for (int i = 1; i < numOverviews + 1; i++) {
			pair = pairs[i].split(",");
			overViewResolutions[i - 1][0] = Double.parseDouble(pair[0]);
			overViewResolutions[i - 1][1] = Double.parseDouble(pair[1]);
		}

		// name
		coverageName = properties.getProperty("Name");

		// need a color expansion?
		// this is a newly added property we have to be ready to the case where
		// we do not find it.
		try {
			expandMe = properties.getProperty("ExpandToRGB").equalsIgnoreCase(
					"true");
		} catch (Throwable t) {
			expandMe = false;
		}

		// original gridrange (estimated)
		originalGridRange = new GeneralGridRange(
				new Rectangle((int) Math.round(originalEnvelope.getLength(0)
						/ highestRes[0]), (int) Math.round(originalEnvelope
						.getLength(1)
						/ highestRes[1])));
		final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(
				originalGridRange, originalEnvelope);
		geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);
		raster2Model = geMapper.createTransform();

		// absolute or relative path
		absolutePath = Boolean
				.parseBoolean(properties.getProperty("", "False"));

		// locationAttribute
		locationAttributeName = properties.getProperty("LocationAttribute");

		// band selection attribute
		bandSelectAttributeName = properties.getProperty("ChannelSelectAttribute");
		
		//the color correction attribute
		colorCorrectionAttributeName = properties.getProperty("ColorCorrectionAttribute");
	}

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source object.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public ImageMosaicReader(Object source) throws IOException {
		this(source, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		return new ImageMosaicFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter
	 * .GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params) throws IOException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Reading mosaic from " + sourceURL.toString());
			LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
					.append(" ").append(highestRes[1]).toString());
		}
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Checking params
		//
		// /////////////////////////////////////////////////////////////////////
		Color inputTransparentColor = (Color) ImageMosaicFormat.INPUT_TRANSPARENT_COLOR
				.getDefaultValue();
		Color outputTransparentColor = (Color) ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
				.getDefaultValue();
		double inputImageThreshold = ((Double) ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
				.getDefaultValue()).doubleValue();
		GeneralEnvelope requestedEnvelope = null;
		Rectangle dim = null;
		boolean blend = false;
		int maxNumTiles = this.maxAllowedTiles;
		OverviewPolicy overviewPolicy = null;
		if (params != null) {
			final int length = params.length;
			for (int i = 0; i < length; i++) {
				final ParameterValue<?> param = (ParameterValue<?>) params[i];
				final String name = param.getDescriptor().getName().getCode();
				if (name.equals(ImageMosaicFormat.READ_GRIDGEOMETRY2D.getName()
						.toString())) {
					final GridGeometry2D gg = (GridGeometry2D) param.getValue();
					requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
					dim = gg.getGridRange2D().getBounds();
					continue;
				}
				if (name.equals(ImageMosaicFormat.INPUT_TRANSPARENT_COLOR
						.getName().toString())) {
					inputTransparentColor = (Color) param.getValue();
					continue;

				}
				if (name.equals(ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
						.getName().toString())) {
					inputImageThreshold = ((Double) param.getValue())
							.doubleValue();
					continue;

				}
				if (name.equals(ImageMosaicFormat.FADING.getName().toString())) {
					blend = ((Boolean) param.getValue()).booleanValue();
					continue;

				}
				if (name.equals(ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
						.getName().toString())) {
					outputTransparentColor = (Color) param.getValue();
					continue;

				}
				if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName()
						.toString())) {
					overviewPolicy = (OverviewPolicy) param.getValue();
					continue;
				}
				if (name.equals(ImageMosaicFormat.MAX_ALLOWED_TILES.getName()
						.toString())) {
					maxNumTiles = param.intValue();
					continue;
				}

			}
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Loading tiles trying to optimize as much as possible
		//
		// /////////////////////////////////////////////////////////////////////
		return loadTiles(requestedEnvelope, inputTransparentColor,
				outputTransparentColor, inputImageThreshold, dim, blend,
				overviewPolicy, maxNumTiles);
	}

	/**
	 * Loading the tiles which overlap with the requested envelope with control
	 * over the <code>inputImageThresholdValue</code>, the fading effect between
	 * different images, abd the <code>transparentColor</code> for the input
	 * images.
	 * 
	 * @param requestedOriginalEnvelope
	 *            bounds the tiles that we will load. Tile outside ths
	 *            {@link GeneralEnvelope} won't even be considered.
	 * 
	 * 
	 * @param transparentColor
	 *            should be used to control transparency on input images.
	 * @param outputTransparentColor
	 * @param inputImageThresholdValue
	 *            should be used to create ROIs on the input images
	 * @param pixelDimension
	 *            is the dimension in pixels of the requested coverage.
	 * @param fading
	 *            tells to ask for {@link MosaicDescriptor#MOSAIC_TYPE_BLEND}
	 *            instead of the classic
	 *            {@link MosaicDescriptor#MOSAIC_TYPE_OVERLAY}.
	 * @param overviewPolicy
	 * @param maxNumTiles
	 * @return a {@link GridCoverage2D} matching as close as possible the
	 *         requested {@link GeneralEnvelope} and <code>pixelDimension</code>
	 *         , or null in case nothing existed in the requested area.
	 * @throws IOException
	 */
	private GridCoverage loadTiles(GeneralEnvelope requestedOriginalEnvelope,
			Color transparentColor, Color outputTransparentColor,
			double inputImageThresholdValue, Rectangle pixelDimension,
			boolean fading, OverviewPolicy overviewPolicy, int maxNumTiles)
			throws IOException {

		if (LOGGER.isLoggable(Level.FINE))
			LOGGER
					.fine(new StringBuffer(
							"Creating mosaic to comply with envelope ")
							.append(
									requestedOriginalEnvelope != null ? requestedOriginalEnvelope
											.toString()
											: null).append(" crs ").append(
									crs.toWKT()).append(" dim ").append(
									pixelDimension == null ? " null"
											: pixelDimension.toString())
							.toString());
		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we have something to load by intersecting the requested
		// envelope with the bounds of the data set.
		//
		// If the requested envelope is not in the same crs of the data set crs
		// we have to perform a conversion towards the latter crs before
		// intersecting anything.
		//
		// /////////////////////////////////////////////////////////////////////
		GeneralEnvelope intersectionEnvelope = null;
		if (requestedOriginalEnvelope != null) {
			if (!CRS.equalsIgnoreMetadata(requestedOriginalEnvelope
					.getCoordinateReferenceSystem(), this.crs)) {
				try {
					// transforming the envelope back to the dataset crs in
					// order to interact with the original envelope for this
					// mosaic.
					final MathTransform transform = CRS.findMathTransform(
							requestedOriginalEnvelope
									.getCoordinateReferenceSystem(), crs, true);
					if (!transform.isIdentity()) {
						requestedOriginalEnvelope = CRS.transform(transform,
								requestedOriginalEnvelope);
						requestedOriginalEnvelope
								.setCoordinateReferenceSystem(this.crs);

						if (LOGGER.isLoggable(Level.FINE))
							LOGGER.fine(new StringBuffer(
									"Reprojected envelope ").append(
									requestedOriginalEnvelope.toString())
									.append(" crs ").append(crs.toWKT())
									.toString());
					}
				} catch (TransformException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				} catch (FactoryException e) {
					throw new DataSourceException(
							"Unable to create a coverage for this source", e);
				}
			}
			if (!requestedOriginalEnvelope.intersects(this.originalEnvelope,
					true)) {
				if (LOGGER.isLoggable(Level.WARNING))
					LOGGER
							.warning("The requested envelope does not intersect the envelope of this mosaic, we will return a null coverage.");
				throw new DataSourceException(
						"Unable to create a coverage for this source");
			}
			intersectionEnvelope = new GeneralEnvelope(
					requestedOriginalEnvelope);
			// intersect the requested area with the bounds of this layer
			intersectionEnvelope.intersect(originalEnvelope);

		} else {
			requestedOriginalEnvelope = new GeneralEnvelope(originalEnvelope);
			intersectionEnvelope = requestedOriginalEnvelope;

		}
		requestedOriginalEnvelope.setCoordinateReferenceSystem(this.crs);
		intersectionEnvelope.setCoordinateReferenceSystem(this.crs);
		// ok we got something to return, let's load records from the index
		// /////////////////////////////////////////////////////////////////////
		//
		// Prepare the filter for loading th needed layers
		//
		// /////////////////////////////////////////////////////////////////////
		final ReferencedEnvelope intersectionJTSEnvelope = new ReferencedEnvelope(
				intersectionEnvelope.getMinimum(0), intersectionEnvelope
						.getMaximum(0), intersectionEnvelope.getMinimum(1),
				intersectionEnvelope.getMaximum(1), crs);
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Load feaures from the index
		// In case there are no features under the requested bbox which is legal
		// in case the mosaic is not a real sqare, we return a fake mosaic.
		//
		// /////////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("loading tile for envelope "
					+ intersectionJTSEnvelope.toString());
		
		final List<SimpleFeature> features = getFeaturesFromIndex(intersectionJTSEnvelope);
		if (features == null || features.size() == 0) {
			return background(requestedOriginalEnvelope, pixelDimension,outputTransparentColor);
		}
		// do we have any feature to load
		final Iterator<SimpleFeature> it = features.iterator();
		if (!it.hasNext())
			throw new DataSourceException(
					"No data was found to match the actual request");
		final int size = features.size();
		if (size > maxNumTiles) {
			LOGGER
					.warning(new StringBuffer("We can load at most ")
							.append(maxNumTiles)
							.append(" tiles while there were requested ")
							.append(size)
							.append(
									"\nI am going to print out a fake coverage, sorry about it!")
							.toString());
			throw new DataSourceException(
					"The maximum allowed number of tiles to be loaded was exceeded.");
		}
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("We have " + size + " tiles to load");
		try {
			return loadRequestedTiles(requestedOriginalEnvelope,
					intersectionEnvelope, transparentColor,
					outputTransparentColor, intersectionJTSEnvelope, features,
					it, inputImageThresholdValue, pixelDimension, size, fading,
					overviewPolicy);
		} catch (TransformException e) {
			throw new DataSourceException(e);
		}

	}

	private GridCoverage background(GeneralEnvelope requestedEnvelope,
			Rectangle dim, Color outputTransparentColor) {
		if (outputTransparentColor == null)
			outputTransparentColor = Color.BLACK;
		Byte[] values = new Byte[] {
				new Byte((byte) outputTransparentColor.getRed()),
				new Byte((byte) outputTransparentColor.getGreen()),
				new Byte((byte) outputTransparentColor.getBlue()),
				new Byte((byte) outputTransparentColor.getAlpha()) };
		RenderedImage constant = ConstantDescriptor.create(
				new Float(dim.width), new Float(dim.height), values,
				ImageUtilities.NOCACHE_HINT);
		return coverageFactory
				.create(coverageName, constant, requestedEnvelope);
	}

	/**
	 * This method loads the tiles which overlap the requested
	 * {@link GeneralEnvelope} using the provided values for alpha and input
	 * ROI.
	 * 
	 * @param requestedOriginalEnvelope
	 * @param intersectionEnvelope
	 * @param transparentColor
	 * @param outputTransparentColor
	 * @param requestedJTSEnvelope
	 * @param features
	 * @param it
	 * @param inputImageThresholdValue
	 * @param dim
	 * @param numImages
	 * @param blend
	 * @param overviewPolicy
	 * @return
	 * @throws DataSourceException
	 * @throws TransformException
	 */
	private GridCoverage loadRequestedTiles(
			GeneralEnvelope requestedOriginalEnvelope,
			GeneralEnvelope intersectionEnvelope, Color transparentColor,
			Color outputTransparentColor, final Envelope requestedJTSEnvelope,
			final List<SimpleFeature> features,
			final Iterator<SimpleFeature> it, double inputImageThresholdValue,
			Rectangle dim, int numImages, boolean blend,
			OverviewPolicy overviewPolicy) throws DataSourceException,
			TransformException {

		try {
			// if we get here we have something to load
			////////////////////////////////////////////////////////////////////
			// ///
			//
			// prepare the params for executing a mosaic operation.
			//
			////////////////////////////////////////////////////////////////////
			// ///
			final ParameterBlockJAI pbjMosaic = new ParameterBlockJAI("Mosaic");
			pbjMosaic.setParameter("mosaicType",
					MosaicDescriptor.MOSAIC_TYPE_OVERLAY);

			////////////////////////////////////////////////////////////////////
			// ///
			//
			// compute the requested resolution given the requested envelope and
			// dimension.
			//
			////////////////////////////////////////////////////////////////////
			// ///
			final ImageReadParam readP = new ImageReadParam();
			final Integer imageChoice;
			if (dim != null)
				imageChoice = setReadParams(overviewPolicy, readP,
						requestedOriginalEnvelope, dim);
			else
				imageChoice = new Integer(0);
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Loading level ").append(
						imageChoice.toString()).append(
						" with subsampling factors ").append(
						readP.getSourceXSubsampling()).append(" ").append(
						readP.getSourceYSubsampling()).toString());
			////////////////////////////////////////////////////////////////////
			// ///
			//
			// Resolution.
			//
			// I am implicitly assuming that all the images have the same
			// resolution. In principle this is not required but in practice
			// having different resolution would surely bring to having small
			// displacements in the final mosaic which we do not wnat to happen.
			//
			////////////////////////////////////////////////////////////////////
			// ///
			final double[] res;
			if (imageChoice.intValue() == 0) {
				res = new double[highestRes.length];
				res[0] = highestRes[0];
				res[1] = highestRes[1];
			} else {
				final double temp[] = overViewResolutions[imageChoice
						.intValue() - 1];
				res = new double[temp.length];
				res[0] = temp[0];
				res[1] = temp[1];

			}
			// adjusting the resolution for the source subsampling
			res[0] *= readP.getSourceXSubsampling();
			res[1] *= readP.getSourceYSubsampling();
			////////////////////////////////////////////////////////////////////
			// ///
			//
			// Envelope of the loaded dataset and upper left corner of this
			// envelope.
			//
			// Ths envelope corresponds to the union of the envelopes of all the
			// tiles that intersect the area that was request by the user. It is
			// crucial to understand that this geographic area can be, and it
			// usually is, bigger then the requested one. This involves doing a
			// crop operation at the end of the mosaic creation.
			//
			////////////////////////////////////////////////////////////////////
			final Envelope loadedDataSetBound = getLoadedDataSetBoud(features);
			final Point2D ULC = new Point2D.Double(loadedDataSetBound.getMinX(), loadedDataSetBound.getMaxY());

			////////////////////////////////////////////////////////////////////
			// 
			//
			// CORE LOOP
			//
			// Loop over the single features and load the images which
			// intersect the requested envelope. Once all of them have been
			// loaded, next step is to create the mosaic and then
			// crop it as requested.
			//
			////////////////////////////////////////////////////////////////////

			final File tempFile = new File(this.sourceURL.getFile());
			final String parentLocation = tempFile.getParent();
			final ROI[] rois = new ROI[numImages];
			final PlanarImage[] alphaChannels = new PlanarImage[numImages];
			final Area finalLayout = new Area();

			// reusable parameters
			boolean alphaIn = false;
			boolean doTransparentColor = false;
			boolean doInputImageThreshold = false;
			int[] alphaIndex = null;
			int i = 0;
			Boolean readMetadata = Boolean.FALSE;
			Boolean readThumbnails = Boolean.FALSE;
			Boolean verifyInput = Boolean.FALSE;
			ColorModel model = null;
			RenderedImage loadedImage = null;
			String location = null;
			// //
			//
			// 0==relative, 1==absolute, -1==uninitialized
			//
			// //
			//
			// In case we set the absolute path we used it here, if not we do
			// not make any assumption and we check what we can do so that we
			// are back compatible.
			//
			// //
			int relativePath = -1;
			if (absolutePath)
				relativePath = 1;

			do {

				////////////////////////////////////////////////////////////////
				// ///////
				//
				// Get location and envelope of the image to load.
				//
				////////////////////////////////////////////////////////////////
				// ///////
				final SimpleFeature feature = (SimpleFeature) it.next();
				location = (String) feature.getAttribute(this.locationAttributeName);
				final ReferencedEnvelope bound = ReferencedEnvelope.reference(feature.getBounds());

				// Get the band order & add to read parameters if necessary
				if (hasBandSelectAttribute){
					String bandSelect = (String) feature.getAttribute(this.bandSelectAttributeName);
					// trim out the integers
					String[] sbands = bandSelect.split(",");
					if (sbands.length == 3) {
						int bands[] = new int[3];
						bands[0] = Integer.parseInt(sbands[0]);
						bands[1] = Integer.parseInt(sbands[1]);
						bands[2] = Integer.parseInt(sbands[2]);
						readP.setSourceBands(bands);
					}
				}
				
				////////////////////////////////////////////////////////////////
				// ///////
				//
				// Load a tile from disk as requested.
				//
				////////////////////////////////////////////////////////////////
				// ///////
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("About to read image number " + i);
				File imageFile = new File(relativePath == 1 ? location
						: new StringBuffer(parentLocation).append(
								File.separatorChar).append(location).toString());

				// //
				//
				// If the tile is not there, dump a message and continue
				//
				// //
				if (!imageFile.exists() || !imageFile.canRead()
						|| !imageFile.isFile()) {

					// check if we need to switch to absolute path
					if (relativePath == -1) {
						// create an absolute path
						imageFile = new File(location);
						if (!imageFile.exists() || !imageFile.canRead()
								|| !imageFile.isFile()) {
							// file does not exist this way either, let's bypass
							// it WITHOUT setting relativePath
							if (LOGGER.isLoggable(Level.INFO))
								LOGGER.info("Unable to read image for file "
										+ imageFile.getAbsolutePath());
							i++;
							continue;
						} else
							relativePath = 1;
					} else {
						if (LOGGER.isLoggable(Level.INFO))
							LOGGER.info("Unable to read image for file "
									+ imageFile.getAbsolutePath());
						i++;
						continue;
					}
				}
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("File found");

				final ParameterBlock pbjImageRead = new ParameterBlock();
				pbjImageRead.add(ImageIO.createImageInputStream(imageFile));
				pbjImageRead.add(imageChoice);
				pbjImageRead.add(readMetadata);
				pbjImageRead.add(readThumbnails);
				pbjImageRead.add(verifyInput);
				pbjImageRead.add(null);
				pbjImageRead.add(null);
				pbjImageRead.add(readP);
				pbjImageRead.add(null);
				loadedImage = JAI.create("ImageRead", pbjImageRead);

				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Just read image number " + i);

				// /////////////////////////////////////////////////////////////
				//
				// Input alpha, ROI and transparent color management.
				//
				// Once I get the first image Ican acquire all the information I
				// need in order to decide which actions to while and after
				// loading the images.
				//
				// Specifically, I have to check if the loaded image have
				// transparency, because if we do a ROI and/or we have a
				// transparent color to set we have to remove it.
				//
				// /////////////////////////////////////////////////////////////
				if (i == 0) {
					// //
					//
					// We check here if the images have an alpha channel or some
					// other sort of transparency. In case we have transparency
					// I also save the index of the transparent channel.
					//
					// //
					model = loadedImage.getColorModel();
					alphaIn = model.hasAlpha();
					if (alphaIn)
						alphaIndex = new int[] { model.getNumComponents() - 1 };

					// //
					//
					// ROI has to be computed depending on the value of the
					// input threshold and on the data type of the images.
					//
					// If I request a threshod of 0 on a byte image, I can skip
					// doing the ROI!
					//
					// //
					doInputImageThreshold = checkIfThresholdIsNeeded(
							loadedImage, inputImageThresholdValue);

					// //
					//
					// Checking if we have to do something against the final
					// transparent color.
					//
					// If we have a valid transparent color we have to remove
					// the input alpha information.
					//
					// However a possible optimization is to check for index
					// color model images with transparency where the
					// transparent color is the same requested here and no ROIs
					// requested.
					//
					// //
					if (transparentColor != null) {
						// paranoiac check on the provided transparent color
						transparentColor = new Color(transparentColor.getRed(),
								transparentColor.getGreen(), transparentColor
										.getBlue());
						doTransparentColor = true;
						//
						// If the images use an IndexColorModel Bitamsk where
						// the transparent color is the same that was requested,
						// the optimization is to avoid removing the alpha
						// information just to readd it at the end. We can
						// simply go with what we have from the input.
						//
						// However, we have to take into account that no action
						// has to be take if a ROI is requested on the input
						// images since that would imply doing an RGB
						// conversion.
						//
						//
						if (model instanceof IndexColorModel
								&& alphaIn
								&& model.getTransparency() == Transparency.BITMASK) {
							final IndexColorModel icm = (IndexColorModel) model;
							final int transparentPixel = icm
									.getTransparentPixel();
							if (transparentPixel != -1) {
								final int oldTransparentColor = icm
										.getRGB(transparentPixel);
								if (oldTransparentColor == transparentColor
										.getRGB()) {
									doTransparentColor = false;
								}

							}

						}

					}

				}

				
				////////////////////////////////////////////////////////////////
				// ///////
				//
				// apply band color fixing; if application
				//
				////////////////////////////////////////////////////////////////
				if (hasColorCorrectionAttribute){
					String sbandFix = (String)feature.getAttribute(this.colorCorrectionAttributeName);
					// trim out the doubles
					String[] sfix = sbandFix.split(",");
					if (sfix.length == 3) {
						double[] bandFix = new double[3];
						bandFix[0] = Double.parseDouble(sfix[0]);
						bandFix[1] = Double.parseDouble(sfix[1]);
						bandFix[2] = Double.parseDouble(sfix[2]);
						
						 ParameterBlock pb = new ParameterBlock();
					     pb.addSource(loadedImage);
					     pb.add(bandFix);
					     loadedImage = JAI.create("addconst", pb, null);
					}	
				}

				
				
				////////////////////////////////////////////////////////////////
				// ///////
				//
				// add to the mosaic collection
				//
				////////////////////////////////////////////////////////////////
				// ///////
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Adding to mosaic image number " + i);
				addToMosaic(pbjMosaic, bound, ULC, res, loadedImage,
						doInputImageThreshold, rois, i,
						inputImageThresholdValue, alphaIn, alphaIndex,
						alphaChannels, finalLayout, imageFile,
						doTransparentColor, transparentColor);

				i++;
			} while (i < numImages);

			////////////////////////////////////////////////////////////////////
			// ///
			//
			// Prepare the last parameters for the mosaic.
			//
			// First of all we set the input threshold accordingly to the input
			// image data type. I find the default value (which is 0) very bad
			// for data type other than byte and ushort. With float and double
			// it can cut off a large par of fthe dynamic.
			//
			// Second step is the the management of the input threshold that is
			// converted into a roi because the way we want to manage such
			// threshold is by applying it on the intensitiy of the input image.
			// Note that this ROI has to be mutually exclusive with the alpha
			// management due to the rules of the JAI Mosaic Operation which
			// ignore the ROIs in case an alpha information is provided for the
			// input images.
			//
			// Third step is the management of the alpha information which can
			// be the result of a masking operation upong the request for a
			// transparent color or the result of input images with internal
			// transparency.
			//
			// Fourth step is the blending for having nice Fading effect at
			// overlapping regions.
			//
			////////////////////////////////////////////////////////////////////
			// ///
			double th = getThreshold(loadedImage.getSampleModel().getDataType());
			pbjMosaic
					.setParameter("sourceThreshold", new double[][] { { th } });
			if (doInputImageThreshold) {
				// //
				//
				// Set the ROI parameter in case it was requested by setting a
				// threshold.
				// 
				// //
				pbjMosaic.setParameter("sourceROI", rois);

			} else if (alphaIn || doTransparentColor) {
				// //
				//
				// In case the input images have transparency information this
				// way we can handle it.
				//
				// //
				pbjMosaic.setParameter("sourceAlpha", alphaChannels);

			}
			// //
			//
			// It might important to set the mosaic tpe to blend otherwise
			// sometimes strange results jump in.
			// 
			// //
			if (blend) {
				pbjMosaic.setParameter("mosaicType",
						MosaicDescriptor.MOSAIC_TYPE_BLEND);

			}

			////////////////////////////////////////////////////////////////////
			// ///
			//
			// Create the mosaic image by doing a crop if necessary and also
			// managing the transparent color if applicablw. Be aware that
			// management of the transparent color involves removing
			// transparency information from the input images.
			// 
			////////////////////////////////////////////////////////////////////
			// ///
			return prepareMosaic(location, requestedOriginalEnvelope,
					intersectionEnvelope, res, loadedDataSetBound, pbjMosaic,
					finalLayout, outputTransparentColor);
		} catch (IOException e) {
			throw new DataSourceException("Unable to create this mosaic", e);
		}
	}

	/**
	 * ROI has to be computed depending on the value of the input threshold and
	 * on the data type of the images.
	 * 
	 * If I request a threshod of 0 on a byte image, I can skip doing the ROI!
	 * 
	 * @param loadedImage
	 *            to check before applying a threshold.
	 * @param thresholdValue
	 *            is the value that is suggested to be used for the threshold.
	 * @return true in case the threshold is to be performed, false otherwise.
	 */
	private boolean checkIfThresholdIsNeeded(RenderedImage loadedImage,
			double thresholdValue) {
		if (Double.isNaN(thresholdValue) || Double.isInfinite(thresholdValue))
			return false;
		switch (loadedImage.getSampleModel().getDataType()) {
		case DataBuffer.TYPE_BYTE:
			int bTh = (int) thresholdValue;
			if (bTh <= 0 || bTh >= 255)
				return false;
		case DataBuffer.TYPE_USHORT:
			int usTh = (int) thresholdValue;
			if (usTh <= 0 || usTh >= 65535)
				return false;
		case DataBuffer.TYPE_SHORT:
			int sTh = (int) thresholdValue;
			if (sTh <= Short.MIN_VALUE || sTh >= Short.MAX_VALUE)
				return false;
		case DataBuffer.TYPE_INT:
			int iTh = (int) thresholdValue;
			if (iTh <= Integer.MIN_VALUE || iTh >= Integer.MAX_VALUE)
				return false;
		case DataBuffer.TYPE_FLOAT:
			float fTh = (float) thresholdValue;
			if (fTh <= -Float.MAX_VALUE || fTh >= Float.MAX_VALUE
					|| Float.isInfinite(fTh) || Float.isNaN(fTh))
				return false;
		case DataBuffer.TYPE_DOUBLE:
			double dTh = (double) thresholdValue;
			if (dTh <= -Double.MAX_VALUE || dTh >= Double.MAX_VALUE
					|| Double.isInfinite(dTh) || Double.isNaN(dTh))
				return false;

		}
		return true;
	}

	/**
	 * Returns a suitable threshold depending on the {@link DataBuffer} type.
	 * 
	 * <p>
	 * Remember that the threshold works with >=.
	 * 
	 * @param dataType
	 *            to create a low threshold for.
	 * @return a minimum threshold value suitable for this data type.
	 */
	private double getThreshold(int dataType) {
		switch (dataType) {
		case DataBuffer.TYPE_BYTE:
		case DataBuffer.TYPE_USHORT:
			// XXX change to zero when bug fixed
			return 1.0;
		case DataBuffer.TYPE_INT:
			return Integer.MIN_VALUE;
		case DataBuffer.TYPE_SHORT:
			return Short.MIN_VALUE;
		case DataBuffer.TYPE_DOUBLE:
			return -Double.MAX_VALUE;
		case DataBuffer.TYPE_FLOAT:
			return -Float.MAX_VALUE;
		}
		return 0;
	}

	/**
	 * Retrieves the ULC of the BBOX composed by all the tiles we need to load.
	 * 
	 * @param double
	 * @return A {@link Point2D} pointing to the ULC of the smallest area made by
	 *         mosaicking all the tile that actually intersect the passed
	 *         envelope.
	 * @throws IOException
	 */
	private Envelope getLoadedDataSetBoud(List<SimpleFeature> features)
			throws IOException {
		// /////////////////////////////////////////////////////////////////////
		//
		// Load feaures and evaluate envelope
		//
		// /////////////////////////////////////////////////////////////////////
		final Envelope loadedULC = new Envelope();
		for (SimpleFeature f : features) {
			loadedULC.expandToInclude(((Geometry) f.getDefaultGeometry())
					.getEnvelopeInternal());
		}
		return loadedULC;

	}

	/**
	 * Retrieves the list of features that intersect the provided evelope
	 * loadinf them inside an index in memory where beeded.
	 * 
	 * @param envelope
	 *            Envelope for selectig features that intersect.
	 * @return A list of fetaures.
	 * @throws IOException
	 *             In case loading the needed features failes.
	 */
	private List<SimpleFeature> getFeaturesFromIndex(final Envelope envelope)
			throws IOException {
		List<SimpleFeature> features = null;
		Object o;

		synchronized (index) {			
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Trying to  use the index...");
			o = index.get();
			if (o != null) {
				// need to see if the index is still valid and recreate it if necessary
				//this is currently done by comparing the date the index was created to
				//the date the shapefile was last modified
				File f = new File(this.sourceURL.getFile());
				if (((MemorySpatialIndex) o).getCreatedDate().before(new Date(f.lastModified()))) {
					o = createIndex();
				} else if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Index does not need to be created...");

			} else {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine("Index needa to be recreated...");
				o = new MemorySpatialIndex(featureSource.getFeatures());
			}
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine("Index Loaded");
		}
		features = ((MemorySpatialIndex) o).findFeatures(envelope);
		if (features != null)
			return features;
		else
			return Collections.emptyList();
	}

	/*
	 * Builds the shapefile index
	 */
	private Object createIndex() throws IOException {
		MemorySpatialIndex o;
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("About to create index");
		// compare the created date of the index with the date on
		// the shapefile
		o = new MemorySpatialIndex(featureSource.getFeatures());

		if (index == null){
			index = new SoftReference<MemorySpatialIndex>(o);
		}
		hasBandSelectAttribute = featureSource.getSchema().getDescriptor(bandSelectAttributeName) != null;
		hasColorCorrectionAttribute = featureSource.getSchema().getDescriptor(colorCorrectionAttributeName) != null;
		
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Created index");
		return o;
	}

	/**
	 * Once we reach this method it means that we have loaded all the images
	 * which were intersecting the requested envelope. Next step is to create
	 * the final mosaic image and cropping it to the exact requested envelope.
	 * 
	 * @param location
	 * 
	 * @param envelope
	 * @param requestedEnvelope
	 * @param intersectionEnvelope
	 * @param res
	 * @param loadedTilesEnvelope
	 * @param pbjMosaic
	 * @param transparentColor
	 * @param doAlpha
	 * @param doTransparentColor
	 * @param finalLayout
	 * @param outputTransparentColor
	 * @param singleImageROI
	 * @return A {@link GridCoverage}, well actually a {@link GridCoverage2D}.
	 * @throws IllegalArgumentException
	 * @throws FactoryRegistryException
	 * @throws DataSourceException
	 */
	private GridCoverage prepareMosaic(String location,
			GeneralEnvelope requestedOriginalEnvelope,
			GeneralEnvelope intersectionEnvelope, double[] res,
			final Envelope loadedTilesEnvelope, ParameterBlockJAI pbjMosaic,
			Area finalLayout, Color outputTransparentColor)
			throws DataSourceException {
		GeneralEnvelope finalenvelope = null;
		PlanarImage preparationImage;
		Rectangle loadedTilePixelsBound = finalLayout.getBounds();
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Loaded bbox ").append(
					loadedTilesEnvelope.toString()).append(
					" while requested bbox ").append(
					requestedOriginalEnvelope.toString()).toString());

		// /////////////////////////////////////////////////////////////////////
		//
		// Check if we need to do a crop on the loaded tiles or not. Keep into
		// account that most part of the time the loaded tiles will be go
		// beyoind the requested area, hence there is a need for cropping them
		// while mosaicking them.
		//
		// /////////////////////////////////////////////////////////////////////
		final GeneralEnvelope loadedTilesBoundEnv = new GeneralEnvelope(
				new double[] { loadedTilesEnvelope.getMinX(),
						loadedTilesEnvelope.getMinY() }, new double[] {
						loadedTilesEnvelope.getMaxX(),
						loadedTilesEnvelope.getMaxY() });
		loadedTilesBoundEnv.setCoordinateReferenceSystem(crs);
		final double loadedTilesEnvelopeDim0 = loadedTilesBoundEnv.getLength(0);
		final double loadedTilesEnvelopeDim1 = loadedTilesBoundEnv.getLength(1);
		if (!intersectionEnvelope.equals(loadedTilesBoundEnv, Math
				.min((loadedTilesEnvelopeDim0 / loadedTilePixelsBound
						.getWidth()) / 2.0,
						(loadedTilesEnvelopeDim1 / loadedTilePixelsBound
								.getHeight()) / 2.0), false)) {

			////////////////////////////////////////////////////////////////////
			// ///
			//
			// CROP the mosaic image to the requested BBOX
			//
			////////////////////////////////////////////////////////////////////
			// ///
			// intersect them
			final GeneralEnvelope intersection = new GeneralEnvelope(
					intersectionEnvelope);
			intersection.intersect(loadedTilesBoundEnv);

			// get the transform for going from world to grid
			try {
				final GridToEnvelopeMapper gridToEnvelopeMapper = new GridToEnvelopeMapper(
						new GeneralGridRange(loadedTilePixelsBound),
						loadedTilesBoundEnv);
				gridToEnvelopeMapper.setGridType(PixelInCell.CELL_CORNER);
				final MathTransform transform = gridToEnvelopeMapper
						.createTransform().inverse();
				final GeneralGridRange finalRange = new GeneralGridRange(CRS
						.transform(transform, intersection));
				// CROP
				finalLayout.intersect(new Area(finalRange.toRectangle()));
				final Rectangle tempRect = finalLayout.getBounds();
				preparationImage = JAI.create("Mosaic", pbjMosaic,
						new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
								new ImageLayout(tempRect.x, tempRect.y,
										tempRect.width, tempRect.height, 0, 0,
										JAI.getDefaultTileSize().width, JAI
												.getDefaultTileSize().height,
										null, null)));

				finalenvelope = intersection;

			} catch (MismatchedDimensionException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			} catch (NoninvertibleTransformException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			} catch (TransformException e) {
				throw new DataSourceException(
						"Problem when creating this mosaic.", e);
			}

		} else {
			preparationImage = JAI.create("Mosaic", pbjMosaic);
			finalenvelope = new GeneralEnvelope(intersectionEnvelope);
		}
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Mosaic created ").toString());

		//
		// ///////////////////////////////////////////////////////////////////
		//
		// FINAL ALPHA
		//
		//
		// ///////////////////////////////////////////////////////////////////
		if (outputTransparentColor != null) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Support for alpha").toString());
			//
			////////////////////////////////////////////////////////////////////
			// /
			//
			// If requested I can perform the ROI operation on the prepared ROI
			// image for building up the alpha band
			//
			//
			////////////////////////////////////////////////////////////////////
			// /
			ImageWorker w = new ImageWorker(preparationImage);
			if (preparationImage.getColorModel() instanceof IndexColorModel) {
				preparationImage = w.maskIndexColorModelByte(
						outputTransparentColor).getPlanarImage();
			} else
				preparationImage = w.maskComponentColorModelByte(
						outputTransparentColor).getPlanarImage();

			////////////////////////////////////////////////////////////////////
			// /
			//
			// create the coverage
			//
			//
			////////////////////////////////////////////////////////////////////
			// /
			return coverageFactory.create(coverageName, preparationImage,
					finalenvelope);
		}
		// ///////////////////////////////////////////////////////////////////
		//		
		// create the coverage
		//		
		// ///////////////////////////////////////////////////////////////////
		return coverageFactory.create(coverageName, preparationImage,
				finalenvelope);

	}

	/**
	 * Adding an image which intersect the requested envelope to the final
	 * moisaic. This operation means computing the translation factor keeping
	 * into account the resolution of the actual image, the envelope of the
	 * loaded dataset and the envelope of this image.
	 * 
	 * @param pbjMosaic
	 * @param bound
	 *            Lon-Lat bounds of the loaded image
	 * @param ulc
	 * @param res
	 * @param loadedImage
	 * @param removeAlpha
	 * @param rois
	 * @param i
	 * @param inputImageThresholdValue
	 * @param alphaChannels
	 * @param alphaIndex
	 * @param alphaIn
	 * @param finalLayout
	 * @param imageFile
	 * @param transparentColor
	 * @param doTransparentColor
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void addToMosaic(ParameterBlockJAI pbjMosaic, Envelope bound,
			Point2D ulc, double[] res, RenderedImage loadedImage,
			boolean doInputImageThreshold, ROI[] rois, int i,
			double inputImageThresholdValue, boolean alphaIn, int[] alphaIndex,
			PlanarImage[] alphaChannels, Area finalLayout, File imageFile,
			boolean doTransparentColor, Color transparentColor) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Computing TRANSLATION AND SCALING FACTORS
		//
		// Using the spatial resolution we compute the translation factors for
		// positioning the actual image correctly in final mosaic.
		//
		// /////////////////////////////////////////////////////////////////////
		RenderedImage readyToMosaicImage = scaleAndTranslate(bound, ulc, res,
				loadedImage);

		// ///////////////////////////////////////////////////////////////////
		//
		// INDEX COLOR MODEL EXPANSION
		//
		// Take into account the need for an expansions of the original color
		// model.
		//
		// If the original color model is an index color model an expansion
		// might be requested in case the differemt palettes are not all the
		// same. In this case the mosaic operator from JAI would provide wrong
		// results since it would take the first palette and use that one for
		// all the other images.
		//
		// There is a special case to take into account here. In case the input
		// images use an IndexColorModel t might happen that the transparent
		// color is present in some of them while it is not present in some
		// others. This case is the case where for sure a color expansion is
		// needed. However we have to take into account that during the masking
		// phase the images where the requested transparent color was present
		// willl have 4 bands, the other 3. If we want the mosaic to work we
		// have to add na extra band to the latter type of images for providing
		// alpha information to them.
		//
		//
		// ///////////////////////////////////////////////////////////////////
		if (expandMe
				&& readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
			readyToMosaicImage = new ImageWorker(readyToMosaicImage)
					.forceComponentColorModel().getPlanarImage();
		}

		// ///////////////////////////////////////////////////////////////////
		//
		// TRANSPARENT COLOR MANAGEMENT
		//
		//
		// ///////////////////////////////////////////////////////////////////
		if (doTransparentColor) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer(
						"Support for alpha on input image number " + i)
						.toString());
			////////////////////////////////////////////////////////////////////
			// ///
			//
			// If requested I can perform the ROI operation on the prepared ROI
			// image for building up the alpha band
			//
			////////////////////////////////////////////////////////////////////
			// ///
			ImageWorker w = new ImageWorker(readyToMosaicImage);
			if (readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
				readyToMosaicImage = w
						.maskIndexColorModelByte(transparentColor)
						.getPlanarImage();
			} else
				readyToMosaicImage = w.maskComponentColorModelByte(
						transparentColor).getPlanarImage();
			alphaIndex = new int[] { readyToMosaicImage.getColorModel()
					.getNumComponents() - 1 };

		}
		// ///////////////////////////////////////////////////////////////////
		//
		// ROI
		//
		// ///////////////////////////////////////////////////////////////////
		if (doInputImageThreshold) {
			ImageWorker w = new ImageWorker(readyToMosaicImage);
			w.tileCacheEnabled(false).intensity().binarize(
					inputImageThresholdValue);
			rois[i] = w.getImageAsROI();

		} else if (alphaIn || doTransparentColor) {
			ImageWorker w = new ImageWorker(readyToMosaicImage);
			////////////////////////////////////////////////////////////////////
			// ///
			//
			// ALPHA in INPUT
			//
			// I have to select the alpha band and provide it to the final
			// mosaic operator. I have to force going to ComponentColorModel in
			// case the image is indexed.
			//
			////////////////////////////////////////////////////////////////////
			// ///
			if (readyToMosaicImage.getColorModel() instanceof IndexColorModel) {
				alphaChannels[i] = w.forceComponentColorModel()
						.retainLastBand().getPlanarImage();
			}

			else
				alphaChannels[i] = w.retainBands(alphaIndex).getPlanarImage();

		}

		// /////////////////////////////////////////////////////////////////////
		//
		// ADD TO MOSAIC
		//
		// /////////////////////////////////////////////////////////////////////
		pbjMosaic.addSource(readyToMosaicImage);
		finalLayout.add(new Area(PlanarImage.wrapRenderedImage(
				readyToMosaicImage).getBounds()));

	}

	/**
	 * Computing TRANSLATION AND SCALING FACTORS
	 * 
	 * Using the spatial resolution we compute the translation factors for
	 * positioning the actual image correctly in final mosaic.
	 * 
	 * @param bound
	 * @param ulc
	 * @param res
	 * @param image
	 * @return
	 */
	private RenderedImage scaleAndTranslate(Envelope bound, Point2D ulc,
			double[] res, RenderedImage image) {
		// evaluate translation and scaling factors.
		double resX = (bound.getMaxX() - bound.getMinX()) / image.getWidth();
		double resY = (bound.getMaxY() - bound.getMinY()) / image.getHeight();
		double scaleX = 1.0, scaleY = 1.0;
		double xTrans = 0.0, yTrans = 0.0;
		if (Math.abs((resX - res[0]) / resX) > EPS
				|| Math.abs(resY - res[1]) > EPS) {
			scaleX = res[0] / resX;
			scaleY = res[1] / resY;

		}
		xTrans = (bound.getMinX() - ulc.getX()) / res[0];
		yTrans = (ulc.getY() - bound.getMaxY()) / res[1];
		// build an image layout that will make the tiles match exactly the
		// transformed image
		ImageLayout layout = new ImageLayout();
		layout.setTileGridXOffset((int) Math.round(xTrans));
		layout.setTileGridYOffset((int) Math.round(yTrans));
		//
		// Optimising scale and translate.
		//
		// In case the scale factors are very close to 1 we have two
		// optimizations: if the translation factors are close to zero we do
		// thing, otherwise if they are integers we do a simple translate.
		//
		// In the general case when we have translation and scaling we do a
		// warp affine which is the most precise operation we can perform.
		//
		// //
		final ParameterBlock pbjAffine = new ParameterBlock();
		if (Math.abs(xTrans - (int) xTrans) < Math.pow(10, -3)
				&& Math.abs(yTrans - (int) yTrans) < Math.pow(10, -3)
				&& Math.abs(scaleX - 1) < Math.pow(10, -6)
				&& Math.abs(scaleY - 1) < Math.pow(10, -6)) {

			// return the original image
			if (Math.abs(xTrans) < Math.pow(10, -3)
					&& Math.abs(yTrans) < Math.pow(10, -3)) {
				return image;

			}

			// translation
			pbjAffine.addSource(image).add(new Float(xTrans)).add(
					new Float(yTrans)).add(
					ImageUtilities.NN_INTERPOLATION_HINT
							.get(JAI.KEY_INTERPOLATION));
			// avoid doing the color expansion now since it might not be needed
			final RenderingHints hints = (RenderingHints) ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL
					.clone();
			hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
			return JAI.create("Translate", pbjAffine, hints);

		}
		// translation and scaling
		final AffineTransform tx = new AffineTransform(scaleX, 0, 0, scaleY,
				xTrans, yTrans);
		pbjAffine.addSource(image).add(tx)
				.add(
						ImageUtilities.NN_INTERPOLATION_HINT
								.get(JAI.KEY_INTERPOLATION));
		// avoid doing the color expansion now since it might not be needed
		final RenderingHints hints = (RenderingHints) ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL
				.clone();
		// adding the capability to do a border extension which is great when
		// doing
		hints.add(ImageUtilities.EXTEND_BORDER_BY_COPYING);
		hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
		return JAI.create("Affine", pbjAffine, hints);

	}

	/**
	 * Updates the band selection and color correction for the features which match the filter
	 * 
	 * Will do nothing if the current shapefile does not have a
	 * bandSelectAttribute or colorCorrectionAttribute
	 * 
	 * @param filter
	 * @param newbands - a 3 element array containing the band index of the red, green and blue channels
	 * @param colorCorr - a 3 element array of color correction value to apply to the red, green, and blue channels
	 */
	public void updateBandSelection(Filter filter, int[] newbands, double colorCorr[]) {
	
		// first check if the current shapefile has the necessary attributes
		AttributeDescriptor bandDescriptor = featureSource.getSchema().getDescriptor(this.bandSelectAttributeName);
		AttributeDescriptor colorCorrDescription = featureSource.getSchema().getDescriptor(this.colorCorrectionAttributeName);
		
		if (bandDescriptor == null || colorCorrDescription == null) {
			// cannot update
			return;
		}
		
		// convert bands & color correction to a string
		StringBuilder newb = new StringBuilder();
		StringBuilder newcolor = new StringBuilder();
		for (int i = 0; i < newbands.length - 1; i++) {
			newb.append(newbands[i]);
			newb.append(",");
			
			newcolor.append(colorCorr[i]);
			newcolor.append(",");
		}
		if (newbands.length > 0) {
			newb.append(newbands[newbands.length - 1]);
			newcolor.append(colorCorr[newbands.length - 1]);
		}

		synchronized (index) {
			//update features in transaction block
			Transaction t= new DefaultTransaction();
			FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) featureSource;
			try {
				featureStore.setTransaction(t);
				featureStore.modifyFeatures(new AttributeDescriptor[]{bandDescriptor, colorCorrDescription}, new Object[]{newb.toString(), newcolor.toString()}, filter);
				t.commit();
			} catch (Exception ex) {
					try {
						t.rollback();
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Cannot update feature store.");
					}
				LOGGER.log(Level.SEVERE, "Error updating feature store", ex);
			}finally{
				featureStore.setTransaction(Transaction.AUTO_COMMIT);
			}
		}
	}

	/**
	 * Returns the first file associated with the first feature that matched the
	 * filter.
	 * 
	 * @param filter the filter to load the queries from 
	 * @return the first file associated with the first feature that matched the
	 * filter or <code>null</code> if no file matches the provided criteria
	 */
	public File getImageFile(Filter filter) throws IOException {
		if (filter==null)
			throw new IllegalArgumentException("The provided filter argument is null");
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(filter);
		
		if (features.size() == 0) {
			return null;
		} else {
			FeatureIterator<SimpleFeature> it = features.features();
			try {
				SimpleFeature f = it.next();
				String location = (String) f.getAttribute(this.locationAttributeName);

				final String parentLocation = (new File(this.sourceURL
						.getFile())).getParent();
				File imageFile = new File(absolutePath ? location
						: new StringBuffer(parentLocation).append(
								File.separatorChar).append(location).toString());

				if (imageFile.exists() && imageFile.canRead()
						&& imageFile.isFile()) {
					return imageFile;
				} else if (!absolutePath) {
					imageFile = new File(location);
					if (imageFile.exists() && imageFile.canRead()
							&& imageFile.isFile()) {
						return imageFile;
					}
				}
			} finally {
				it.close();
			}
		}
		
		// cannot file a file for given feature
		return null;
	}

	/**
	 * 
	 * @return the name of the band selection attribute from the shapefile
	 */
	public String getBandsAttributeName() {
		return this.bandSelectAttributeName;
	}
	
	/**
	 * 
	 * @return the name of the color correction attribute from the shapefile
	 */
	public String getColorCorrectionAttributeName(){
		return this.colorCorrectionAttributeName;
	}
	
	/**
	 * 
	 * @return the name of the location attribute from the shapefile
	 */
	public String getLocationAttributeName(){
		return this.locationAttributeName;
	}
	
	/**
	 * 
	 * @return true if the particular shapefile index has the band 
	 * selection and color correction attributes 
	 */
	public boolean hasBandColorAttributes(){
		if (this.colorCorrectionAttributeName != null
				&& this.bandSelectAttributeName != null
				&& featureSource.getSchema().getDescriptor(
						this.bandSelectAttributeName) != null
				&& featureSource.getSchema().getDescriptor(
						this.colorCorrectionAttributeName) != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Number of coverages for this reader is 1
	 * 
	 * @return the number of coverages for this reader.
	 */
	@Override
	public int getGridCoverageCount() {
		return 1;
	}
}
