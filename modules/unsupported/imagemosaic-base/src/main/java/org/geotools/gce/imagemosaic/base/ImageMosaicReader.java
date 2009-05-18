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
package org.geotools.gce.imagemosaic.base;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ConstantDescriptor;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
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
public abstract class ImageMosaicReader extends AbstractGridCoverage2DReader
        implements GridCoverageReader
{

    /** Logger. */
    final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic.base");

    private static final String COVERAGE_CREATION_ERROR 
        = "Unable to create a coverage for this source";
    
    /**
     * Max number of tiles that this plugin will load.
     * 
     * If this number is exceeded, i.e. we request an area which is too large
     * instead of getting stuck with opening thousands of files I give you back
     * a fake coverage.
     */
    private int maxAllowedTiles 
        = (Integer) ImageMosaicFormat.MAX_ALLOWED_TILES.getDefaultValue();

    private ImageMosaicMetadata metadata;

    /**
     * Constructor.
     * 
     * @param source
     *            The source object.
     * @throws IOException
     * @throws UnsupportedEncodingException
     * 
     */
    public ImageMosaicReader(Object source) throws IOException
    {
        this(source, null);

    }

    /**
     * COnstructor.
     * 
     * @param source
     *            The source object.
     * @throws IOException
     * @throws UnsupportedEncodingException
     * 
     */
    public ImageMosaicReader(Object source, Hints uHints) throws IOException
    {
        // managing hints
        if (this.hints == null)
        {
            this.hints = new Hints();
        }
        
        if (uHints != null)
        {
            this.hints.add(uHints);
        }
        this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        
        // set the maximum number of tile to load
        if (hints.containsKey(Hints.MAX_ALLOWED_TILES))
        {
            maxAllowedTiles = (Integer) hints.get(Hints.MAX_ALLOWED_TILES);
        }

        this.source = source;
                
        evaluateMetadata();        
        findCoordinateReferenceSystem();
    }

    private void findCoordinateReferenceSystem()
    {
        Object tempCRS = hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
        if (tempCRS != null)
        {
            this.crs = (CoordinateReferenceSystem) tempCRS;
            LOGGER.log(Level.WARNING, 
                    "Using forced coordinate reference system " + crs.toWKT());
        }
        else
        {
            CoordinateReferenceSystem tempcrs = metadata.getCoordinateReferenceSystem();
            if (tempcrs == null)
            {
                // use the default crs
                crs = AbstractGridFormat.getDefaultCRS();
                String msg = String.format(
                        "Unable to find a CRS for this coverage, using a default one: %s",
                         crs.toWKT());
                LOGGER.log(Level.WARNING, msg);
            }
            else
            {
                crs = tempcrs;
            }
        }
    }

    /**
     * Loads the properties file that contains useful information about this
     * coverage.
     * 
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void evaluateMetadata() throws UnsupportedEncodingException,
            IOException, FileNotFoundException
    {

        // load the envelope
        this.originalEnvelope = metadata.getEnvelope();
        this.originalEnvelope.setCoordinateReferenceSystem(crs);

        // resolutions levels
        numOverviews = metadata.getNumLevels()-1;
        overViewResolutions = numOverviews >= 1 ? new double[numOverviews][2]
                : null;
        highestRes = metadata.getResolutions().get(0);

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
                    .append(" ").append(highestRes[1]).toString());

        for (int i = 1; i < numOverviews + 1; i++)
        {
            double[] res = metadata.getResolutions().get(i);
            overViewResolutions[i - 1][0] = res[0];
            overViewResolutions[i - 1][1] = res[1];
        }

        // name
        coverageName = metadata.getName();

        // original gridrange (estimated)
        long width = Math.round(originalEnvelope.getLength(0)  / highestRes[0]);
        long height = Math.round(originalEnvelope.getLength(1) / highestRes[1]);
        Rectangle rect = new Rectangle((int) width, (int) height);
        originalGridRange = new GeneralGridRange(rect);
        final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(
                originalGridRange, originalEnvelope);
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);
        raster2Model = geMapper.createTransform();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter
     * .GeneralParameterValue[])
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IOException
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine(String.format("Highest res %f %f", 
                        highestRes[0], highestRes[1]));
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Checking params
        //
        // /////////////////////////////////////////////////////////////////////
        Color inputTransparentColor = (Color) 
            ImageMosaicFormat.INPUT_TRANSPARENT_COLOR.getDefaultValue();
        Color outputTransparentColor = (Color) 
            ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR.getDefaultValue();
        
        double inputImageThreshold = (Double) 
            ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE.getDefaultValue();
        GeneralEnvelope requestedEnvelope = null;
        Rectangle dim = null;
        boolean blend = false;
        int maxNumTiles = this.maxAllowedTiles;
        OverviewPolicy overviewPolicy = null;
        if (params != null)
        {
            final int length = params.length;
            for (int i = 0; i < length; i++)
            {
                final ParameterValue<?> param = (ParameterValue<?>) params[i];
                final String name = param.getDescriptor().getName().getCode();
                if (name.equals(ImageMosaicFormat.READ_GRIDGEOMETRY2D.getName()
                        .toString()))
                {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = (GeneralEnvelope) gg.getEnvelope();
                    dim = gg.getGridRange2D().getBounds();
                    continue;
                }
                if (name.equals(ImageMosaicFormat.INPUT_TRANSPARENT_COLOR
                        .getName().toString()))
                {
                    inputTransparentColor = (Color) param.getValue();
                    continue;

                }
                if (name.equals(ImageMosaicFormat.INPUT_IMAGE_THRESHOLD_VALUE
                        .getName().toString()))
                {
                    inputImageThreshold = ((Double) param.getValue())
                            .doubleValue();
                    continue;

                }
                if (name.equals(ImageMosaicFormat.FADING.getName().toString()))
                {
                    blend = ((Boolean) param.getValue()).booleanValue();
                    continue;

                }
                if (name.equals(ImageMosaicFormat.OUTPUT_TRANSPARENT_COLOR
                        .getName().toString()))
                {
                    outputTransparentColor = (Color) param.getValue();
                    continue;

                }
                if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName()
                        .toString()))
                {
                    overviewPolicy = (OverviewPolicy) param.getValue();
                    continue;
                }
                if (name.equals(ImageMosaicFormat.MAX_ALLOWED_TILES.getName()
                        .toString()))
                {
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
            throws IOException
    {

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine(String.format(
                    "Creating mosaic to comply with envelope %s crs %s dim %s",
                    requestedOriginalEnvelope, crs.toWKT(), pixelDimension));
        }
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
        if (requestedOriginalEnvelope != null)
        {
            if (!CRS.equalsIgnoreMetadata(requestedOriginalEnvelope
                    .getCoordinateReferenceSystem(), crs))
            {
                try
                {
                    // transforming the envelope back to the dataset crs in
                    // order to interact with the original envelope for this
                    // mosaic.
                    final MathTransform transform = CRS.findMathTransform(
                            requestedOriginalEnvelope
                                    .getCoordinateReferenceSystem(), crs, true);
                    if (!transform.isIdentity())
                    {
                        requestedOriginalEnvelope = CRS.transform(transform,
                                requestedOriginalEnvelope);
                        requestedOriginalEnvelope
                                .setCoordinateReferenceSystem(crs);

                        if (LOGGER.isLoggable(Level.FINE))
                        {
                            LOGGER.fine(String.format(
                                    "Reprojected envelope %s crs %s", 
                                    requestedOriginalEnvelope, crs.toWKT()));
                        }
                    }
                }
                catch (TransformException e)
                {
                    throw new DataSourceException(COVERAGE_CREATION_ERROR, e);
                }
                catch (FactoryException e)
                {
                    throw new DataSourceException(COVERAGE_CREATION_ERROR, e);
                }
            }
            if (!requestedOriginalEnvelope.intersects(originalEnvelope, true))
            {
                if (LOGGER.isLoggable(Level.WARNING))
                {
                    LOGGER.warning(
                        "The requested envelope does not intersect " +
                        "the envelope of this mosaic, " +
                        "we will return a null coverage.");
                }
                throw new DataSourceException(COVERAGE_CREATION_ERROR);
            }
            intersectionEnvelope = new GeneralEnvelope(
                    requestedOriginalEnvelope);
            // intersect the requested area with the bounds of this layer
            intersectionEnvelope.intersect(originalEnvelope);

        }
        else
        {
            requestedOriginalEnvelope = new GeneralEnvelope(originalEnvelope);
            intersectionEnvelope = requestedOriginalEnvelope;

        }
        requestedOriginalEnvelope.setCoordinateReferenceSystem(crs);
        intersectionEnvelope.setCoordinateReferenceSystem(crs);

        // ok we got something to return, let's load records from the index
        // /////////////////////////////////////////////////////////////////////
        //
        // Prepare the filter for loading th needed layers
        //
        // /////////////////////////////////////////////////////////////////////
        final ReferencedEnvelope intersectionJTSEnvelope = 
            new ReferencedEnvelope(
                intersectionEnvelope.getMinimum(0), 
                intersectionEnvelope.getMaximum(0), 
                intersectionEnvelope.getMinimum(1),
                intersectionEnvelope.getMaximum(1), crs);

        // /////////////////////////////////////////////////////////////////////
        //
        // Load feaures from the index
        // In case there are no features under the requested bbox which is legal
        // in case the mosaic is not a real sqare, we return a fake mosaic.
        //
        // /////////////////////////////////////////////////////////////////////
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("loading tile for envelope " + intersectionJTSEnvelope);

        final List<?> features = getMatchingImageIds(intersectionJTSEnvelope);
        if (features == null || features.isEmpty())
        {
            return background(requestedOriginalEnvelope, pixelDimension,
                    outputTransparentColor);
        }

        final int size = features.size();
        if (size > maxNumTiles)
        {
            LOGGER.warning(String.format(
                "We can load at most %d tiles while there were requested %d.\n%s",
                maxNumTiles, size, 
                "I am going to print out a fake coverage, sorry about it!"));
            
            throw new DataSourceException(
                "The maximum allowed number of tiles to be loaded was exceeded.");
        }
        
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("We have " + size + " tiles to load");
        
        try
        {
            return loadRequestedTiles(requestedOriginalEnvelope,
                    intersectionEnvelope, transparentColor,
                    outputTransparentColor, intersectionJTSEnvelope, features,
                    inputImageThresholdValue, pixelDimension, size, fading,
                    overviewPolicy);
        }
        catch (TransformException e)
        {
            throw new DataSourceException(e);
        }
    }

    protected abstract List<?> getMatchingImageIds(ReferencedEnvelope env);

    private GridCoverage background(GeneralEnvelope requestedEnvelope,
            Rectangle dim, Color outputTransparentColor)
    {
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
        
        return coverageFactory.create(coverageName, constant, requestedEnvelope);
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
            GeneralEnvelope intersectionEnvelope, 
            Color transparentColor,
            Color outputTransparentColor, 
            final Envelope requestedJTSEnvelope,
            final List<?> imageRefs, 
            double inputImageThresholdValue,
            Rectangle dim, 
            int numImages, 
            boolean blend,
            OverviewPolicy overviewPolicy) 
    throws DataSourceException, TransformException
    {
        try
        {
            // if we get here we have something to load
            // //////////////////////////////////////////////////////////////////
            // ///
            //
            // prepare the params for executing a mosaic operation.
            //
            // //////////////////////////////////////////////////////////////////
            // ///
            final ParameterBlockJAI pbjMosaic = new ParameterBlockJAI("Mosaic");
            pbjMosaic.setParameter("mosaicType",
                    MosaicDescriptor.MOSAIC_TYPE_OVERLAY);

            // //////////////////////////////////////////////////////////////////
            // ///
            //
            // compute the requested resolution given the requested envelope and
            // dimension.
            //
            // //////////////////////////////////////////////////////////////////
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
            // Resolution.
            //
            // I am implicitly assuming that all the images have the same
            // resolution. In principle this is not required but in practice
            // having different resolution would surely bring to having small
            // displacements in the final mosaic which we do not wnat to happen.
            final double[] res = getResolution(readP, imageChoice);

            // Envelope of the loaded dataset and upper left corner of this
            // envelope.
            //
            // Ths envelope corresponds to the union of the envelopes of all the
            // tiles that intersect the area that was request by the user. It is
            // crucial to understand that this geographic area can be, and it
            // usually is, bigger then the requested one. This involves doing a
            // crop operation at the end of the mosaic creation.
            final Envelope loadedDataSetBound = getLoadedDataSetBoud(imageRefs);
            final Point2D ULC = new Point2D.Double(
                    loadedDataSetBound.getMinX(), loadedDataSetBound.getMaxY());

            // CORE LOOP
            //
            // Loop over the single features and load the images which
            // intersect the requested envelope. Once all of them have been
            // loaded, next step is to create the mosaic and then
            // crop it as requested.

            int i = 0;

            ImageMosaicLoader ctx = new ImageMosaicLoader(numImages);
            ctx.transparentColor = transparentColor;
            ctx.inputImageThresholdValue = inputImageThresholdValue;
            
            Iterator<?> it = imageRefs.iterator();
            while (i < numImages)
            {
                final Object imageRef = it.next();

                // //////////////////////////////////////////////////////////////
                // ///////
                //
                // Get location and envelope of the image to load.
                //
                // //////////////////////////////////////////////////////////////
                // ///////
                final ReferencedEnvelope bound = getEnvelope(imageRef); 
                    
                ctx.loadedImage = loadImage(readP, imageChoice, imageRef);

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
                if (i == 0)
                {
                    ctx.handleFirstImage();
                }

                ctx.handleAnyImage();
                
                // apply band color fixing; if application
                if (metadata.hasColorCorrection())
                {
                    double[] bandFix = new double[3];
                    bandFix[0] = metadata.getColorCorrection(0);
                    bandFix[1] = metadata.getColorCorrection(1);
                    bandFix[2] = metadata.getColorCorrection(2);

                    ParameterBlock pb = new ParameterBlock();
                    pb.addSource(ctx.loadedImage);
                    pb.add(bandFix);
                    ctx.loadedImage = JAI.create("addconst", pb, null);
                }

                // add to the mosaic collection
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Adding to mosaic image number " + i);

                ctx.addToMosaic(pbjMosaic, bound, ULC, res,  i);

                i++;
            } 

            // Create the mosaic image by doing a crop if necessary and also
            // managing the transparent color if applicablw. Be aware that
            // management of the transparent color involves removing
            // transparency information from the input images.
            return prepareMosaic(requestedOriginalEnvelope,
                    intersectionEnvelope, res, loadedDataSetBound, pbjMosaic,
                    ctx.finalLayout, outputTransparentColor);
        }
        catch (IOException e)
        {
            throw new DataSourceException("Unable to create this mosaic", e);
        }
    }
    
    


    private RenderedImage loadImage(final ImageReadParam readP,
            final Integer imageChoice, final Object imageRef) throws IOException
    {
        RenderedImage loadedImage;
        // Get the band order & add to read parameters if necessary
        if (metadata.hasBandAttributes())
        {
            int bands[] = new int[3];
            bands[0] = metadata.getBand(0);
            bands[1] = metadata.getBand(1);
            bands[2] = metadata.getBand(2);
            readP.setSourceBands(bands);
        }

        Boolean readMetadata = Boolean.FALSE;
        Boolean readThumbnails = Boolean.FALSE;
        Boolean verifyInput = Boolean.FALSE;
        final ParameterBlock pbjImageRead = new ParameterBlock();
        pbjImageRead.add(getImageInputStream(imageRef));
        pbjImageRead.add(imageChoice);
        pbjImageRead.add(readMetadata);
        pbjImageRead.add(readThumbnails);
        pbjImageRead.add(verifyInput);
        pbjImageRead.add(null);
        pbjImageRead.add(null);
        pbjImageRead.add(readP);
        pbjImageRead.add(null);
        loadedImage = JAI.create("ImageRead", pbjImageRead);
        return loadedImage;
    }

    private double[] getResolution(final ImageReadParam readP,
            final Integer imageChoice)
    {
        final double[] res;
        if (imageChoice.intValue() == 0)
        {
            res = new double[highestRes.length];
            res[0] = highestRes[0];
            res[1] = highestRes[1];
        }
        else
        {
            final double temp[] = overViewResolutions[imageChoice - 1];
            res = new double[temp.length];
            res[0] = temp[0];
            res[1] = temp[1];

        }
        // adjusting the resolution for the source subsampling
        res[0] *= readP.getSourceXSubsampling();
        res[1] *= readP.getSourceYSubsampling();
        return res;
    }
    
    protected abstract ImageInputStream getImageInputStream(Object imageId) throws IOException;

    /**
     * Retrieves the ULC of the BBOX composed by all the tiles we need to load.
     * 
     * @param double
     * @return A {@link Point2D} pointing to the ULC of the smallest area made
     *         by mosaicking all the tile that actually intersect the passed
     *         envelope.
     * @throws IOException
     */
    private Envelope getLoadedDataSetBoud(List<?> imageIds)
            throws IOException
    {
        // /////////////////////////////////////////////////////////////////////
        //
        // Load feaures and evaluate envelope
        //
        // /////////////////////////////////////////////////////////////////////
        final Envelope loadedULC = new Envelope();
        for (Object f : imageIds)
        {
            ReferencedEnvelope envelope = getEnvelope(f);
            loadedULC.expandToInclude(envelope);
        }
        return loadedULC;

    }
    
    protected abstract ReferencedEnvelope getEnvelope(Object imageId);

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
    private GridCoverage prepareMosaic(
            GeneralEnvelope requestedOriginalEnvelope,
            GeneralEnvelope intersectionEnvelope, double[] res,
            final Envelope loadedTilesEnvelope, ParameterBlockJAI pbjMosaic,
            Area finalLayout, Color outputTransparentColor)
            throws DataSourceException
    {
        GeneralEnvelope finalenvelope = null;
        PlanarImage preparationImage;
        Rectangle loadedTilePixelsBound = finalLayout.getBounds();
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine(String.format("Loaded bbox %s while requested bbox %s", 
                    loadedTilesEnvelope,
                    requestedOriginalEnvelope));
        }
        // /////////////////////////////////////////////////////////////////////
        //
        // Check if we need to do a crop on the loaded tiles or not. Keep into
        // account that most part of the time the loaded tiles will be go
        // beyond the requested area, hence there is a need for cropping them
        // while mosaicking them.
        //
        // /////////////////////////////////////////////////////////////////////
        double[] lower = new double[] { 
                loadedTilesEnvelope.getMinX(),
                loadedTilesEnvelope.getMinY() };
        
        double[] upper = new double[] {
                loadedTilesEnvelope.getMaxX(),
                loadedTilesEnvelope.getMaxY() };

        final GeneralEnvelope loadedTilesBoundEnv = 
            new GeneralEnvelope(lower, upper);
        
        loadedTilesBoundEnv.setCoordinateReferenceSystem(crs);
        final double loadedWidth = loadedTilesBoundEnv.getLength(0);
        final double loadedHeight = loadedTilesBoundEnv.getLength(1);
        double toleranceX = loadedWidth / loadedTilePixelsBound.getWidth();
        double toleranceY = loadedHeight / loadedTilePixelsBound.getHeight();
        double tolerance = Math.min(toleranceX / 2.0, toleranceY  / 2.0);
        if (!intersectionEnvelope.equals(loadedTilesBoundEnv, tolerance, false))
        {

            // //////////////////////////////////////////////////////////////////
            // ///
            //
            // CROP the mosaic image to the requested BBOX
            //
            // //////////////////////////////////////////////////////////////////
            // ///
            // intersect them
            final GeneralEnvelope intersection = new GeneralEnvelope(
                    intersectionEnvelope);
            intersection.intersect(loadedTilesBoundEnv);

            // get the transform for going from world to grid
            try
            {
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
                ImageLayout layout = new ImageLayout(
                        tempRect.x, tempRect.y,
                        tempRect.width, tempRect.height, 
                        0, 0,
                        JAI.getDefaultTileSize().width, 
                        JAI.getDefaultTileSize().height,
                        null, null);
                RenderingHints rHints = 
                    new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
                preparationImage = JAI.create("Mosaic", pbjMosaic, rHints);

                finalenvelope = intersection;

            }
            catch (MismatchedDimensionException e)
            {
                throw new DataSourceException(
                        "Problem when creating this mosaic.", e);
            }
            catch (NoninvertibleTransformException e)
            {
                throw new DataSourceException(
                        "Problem when creating this mosaic.", e);
            }
            catch (TransformException e)
            {
                throw new DataSourceException(
                        "Problem when creating this mosaic.", e);
            }

        }
        else
        {
            preparationImage = JAI.create("Mosaic", pbjMosaic);
            finalenvelope = new GeneralEnvelope(intersectionEnvelope);
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Mosaic created ");

        //
        // ///////////////////////////////////////////////////////////////////
        //
        // FINAL ALPHA
        //
        //
        // ///////////////////////////////////////////////////////////////////
        if (outputTransparentColor != null)
        {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Support for alpha");
            //
            // //////////////////////////////////////////////////////////////////
            // /
            //
            // If requested I can perform the ROI operation on the prepared ROI
            // image for building up the alpha band
            //
            //
            // //////////////////////////////////////////////////////////////////
            // /
            ImageWorker w = new ImageWorker(preparationImage);
            if (preparationImage.getColorModel() instanceof IndexColorModel)
            {
                preparationImage = w.makeColorTransparent(
                        outputTransparentColor).getPlanarImage();
            }
            else
                preparationImage = w.makeColorTransparent(
                        outputTransparentColor).getPlanarImage();

            // //////////////////////////////////////////////////////////////////
            // /
            //
            // create the coverage
            //
            //
            // //////////////////////////////////////////////////////////////////
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

    public ImageMosaicMetadata getMetadata()
    {
        return metadata;
    }

    /**
     * Number of coverages for this reader is 1
     * 
     * @return the number of coverages for this reader.
     */
    @Override
    public int getGridCoverageCount()
    {
        return 1;
    }
}
