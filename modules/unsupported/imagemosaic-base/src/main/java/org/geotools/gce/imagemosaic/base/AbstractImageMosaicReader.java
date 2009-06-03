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
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ConstantDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
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
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

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
public abstract class AbstractImageMosaicReader extends AbstractGridCoverage2DReader
        implements GridCoverageReader
{

    /** Logger. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic.base");

    /**
     * Max number of tiles that this plugin will load.
     * 
     * If this number is exceeded, i.e. we request an area which is too large
     * instead of getting stuck with opening thousands of files I give you back
     * a fake coverage.
     */
    private int maxAllowedTiles 
        = AbstractImageMosaicFormat.MAX_ALLOWED_TILES.getDefaultValue();

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
    public AbstractImageMosaicReader(Object source, Hints hints) throws IOException
    {
        // managing hints
        if (this.hints == null)
        {
            this.hints = new Hints();
        }
        
        if (hints != null)
        {
            this.hints.add(hints);
        }
        this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        
        // set the maximum number of tile to load
        if (this.hints.containsKey(Hints.MAX_ALLOWED_TILES))
        {
            maxAllowedTiles = (Integer) this.hints.get(Hints.MAX_ALLOWED_TILES);
        }

        this.source = source;                
    }

    
    protected abstract ImageMosaicMetadata createMetadata();
    protected abstract List<?> getMatchingImageRefs(ReferencedEnvelope env) throws IOException;
    protected abstract ImageInputStream getImageInputStream(Object imageRef) throws IOException;
    protected abstract ReferencedEnvelope getEnvelope(Object imageRef);
    protected abstract int[] getBands(Object imageRef);
    
    
    
    /**
     * Loads the properties file that contains useful information about this
     * coverage.
     * 
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void evaluateMetadata()
    {
        this.metadata = createMetadata();

        // load the envelope
        this.originalEnvelope = new GeneralEnvelope(metadata.getEnvelope());

        // resolutions levels
        numOverviews = metadata.getNumLevels()-1;
        if (numOverviews >= 1)
        {
            overViewResolutions =  new double[numOverviews][2];
        }
        highestRes = metadata.getResolutions().get(0);

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(String.format("Highest res %f %f", 
                    highestRes[0], highestRes[1]));

        for (int i = 1; i < numOverviews + 1; i++)
        {
            overViewResolutions[i - 1] = metadata.getResolutions().get(i);
        }

        // name
        coverageName = metadata.getName();

        // original gridrange (estimated)
        long width  = Math.round(originalEnvelope.getSpan(0) / highestRes[0]);
        long height = Math.round(originalEnvelope.getSpan(1) / highestRes[1]);
        Rectangle rect = new Rectangle((int) width, (int) height);
        originalGridRange = new GeneralGridRange(rect);

        GridToEnvelopeMapper geMapper 
            = new GridToEnvelopeMapper(originalGridRange, originalEnvelope);
        
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);
        raster2Model = geMapper.createTransform();
    }

    public GridCoverage read(GeneralParameterValue[] params) throws IOException
    {
        evaluateMetadata();        

        ImageMosaicParameters mosaicParams = new ImageMosaicParameters();
        mosaicParams.setMaxNumTiles(maxAllowedTiles);
        mosaicParams.evaluate(params);

        return loadTiles(mosaicParams);
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
    private GridCoverage loadTiles(ImageMosaicParameters mp)
            throws IOException
    {

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine(String.format(
                    "Creating mosaic to comply with envelope %s crs %s bounds %s",
                    mp.getRequestedEnvelope(), crs.toWKT(), mp.getBounds()));
        }
        
        
        ReferencedEnvelope intersectionEnvelope = transformAndIntersectEnvelopes(mp); 
                
        List<?> imageRefs = getMatchingImageRefs(intersectionEnvelope);
        if (imageRefs.isEmpty())
        {
            return background(mp);
        }

        int size = imageRefs.size();
        if (size > mp.getMaxNumTiles())
        {
            throw new DataSourceException(String.format(
                    "Requested %d tiles, exceeding allowed maximum of %d.", 
                    size, mp.getMaxNumTiles()));
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("We have " + size + " tiles to load");
                
        
        mp.setMaxNumTiles(size);
        try
        {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("loading tiles for envelope " + intersectionEnvelope);

            return loadRequestedTiles(mp, intersectionEnvelope, imageRefs);
        }
        catch (TransformException e)
        {
            throw new DataSourceException(e);
        }
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
    
    private ReferencedEnvelope transformAndIntersectEnvelopes(
            ImageMosaicParameters mp) throws DataSourceException
    {
        final String COVERAGE_CREATION_ERROR 
            = "Unable to create a coverage for this source";
    
        
        GeneralEnvelope reqEnv = mp.getRequestedEnvelope();
        ReferencedEnvelope intersectionEnvelope;
        if (reqEnv == null)
        {
            reqEnv = new GeneralEnvelope(originalEnvelope);
            mp.setRequestedEnvelope(reqEnv);
            intersectionEnvelope = new ReferencedEnvelope(reqEnv);
        }
        else
        {
            ReferencedEnvelope refReqEnv = new ReferencedEnvelope(reqEnv);
            try
            {
                refReqEnv = refReqEnv.transform(crs, true);
            }
            catch (TransformException e)
            {
                throw new DataSourceException(COVERAGE_CREATION_ERROR);
            }
            catch (FactoryException e)
            {
                throw new DataSourceException(COVERAGE_CREATION_ERROR);
            }
            if (!originalEnvelope.intersects(refReqEnv, true))
            {
                LOGGER.warning("The requested envelope does not intersect "
                        + "the envelope of this mosaic, "
                        + "we will return a null coverage.");
                throw new DataSourceException(COVERAGE_CREATION_ERROR);
            }
            
            // intersect the requested area with the bounds of this layer
            GeneralEnvelope intersectionEnv = new GeneralEnvelope(refReqEnv);
            intersectionEnv.intersect(originalEnvelope);

            intersectionEnvelope = new ReferencedEnvelope(intersectionEnv);

        }
        return intersectionEnvelope;
    }
    
    

    private GridCoverage background(ImageMosaicParameters mp)
    {
        GeneralEnvelope requestedEnvelope = mp.getRequestedEnvelope();
        Rectangle dim = mp.getBounds();
        Color outputTransparentColor = mp.getOutputTransparentColor();
        
        if (outputTransparentColor == null)
            outputTransparentColor = Color.BLACK;
        
        Byte[] values = new Byte[] {
                (byte) outputTransparentColor.getRed(),
                (byte) outputTransparentColor.getGreen(),
                (byte) outputTransparentColor.getBlue(),
                (byte) outputTransparentColor.getAlpha() };
        
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
     * @return
     * @throws DataSourceException
     * @throws TransformException
     */
    private GridCoverage loadRequestedTiles(ImageMosaicParameters mp,
            ReferencedEnvelope intersectionJTSEnvelope, List<?> imageRefs)
            throws TransformException, IOException
    {
        // compute the requested resolution given the requested envelope and
        // dimension.
        ImageReadParam readP = new ImageReadParam();
        Integer imageChoice;
        if (mp.getBounds() != null)
        {
            imageChoice = setReadParams(mp.getOverviewPolicy(), readP, mp
                    .getRequestedEnvelope(), mp.getBounds());
        }
        else
        {
            imageChoice = 0;
        }

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine(String.format(
                    "Loading level %d with subsampling factors %d %d",
                    imageChoice, 
                    readP.getSourceXSubsampling(), 
                    readP.getSourceYSubsampling()));
        }

        // Resolution.
        //
        // I am implicitly assuming that all the images have the same
        // resolution. In principle this is not required but in practice
        // having different resolution would surely bring to having small
        // displacements in the final mosaic which we do not wnat to happen.
        double[] resolution = getResolution(readP, imageChoice);

        // Compute the envelope of the loaded dataset and the upper left corner
        // of this envelope.
        //
        // This envelope corresponds to the union of the envelopes of all the
        // tiles that intersect the area requested by the user. It is
        // crucial to understand that this geographic area can be, and
        // usually is, bigger than the requested one. This involves doing a
        // crop operation at the end of the mosaic creation.
        ReferencedEnvelope loadedDataSetBound = getLoadedTilesEnvelope(imageRefs);
        Point2D upperLeft = new Point2D.Double(loadedDataSetBound.getMinX(),
                loadedDataSetBound.getMaxY());

        // CORE LOOP
        //
        // Loop over the tiles intersecting the requested envelope. Once all of
        // them have been loaded, the next step is to create the mosaic and then
        // crop it if needed.

        int i = 0;

        ImageMosaicLoader loader = new ImageMosaicLoader(metadata, mp);
        Iterator<?> it = imageRefs.iterator();
        while (i < mp.getMaxNumTiles())
        {
            Object imageRef = it.next();

            // Get location and envelope of the image to load.
            ReferencedEnvelope bound = getEnvelope(imageRef);
            
            loadImage(loader, readP, imageChoice, imageRef);

            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Just read image number " + i);

            // Input alpha, ROI and transparent color management.
            //
            // Once I get the first image I can acquire all the information I
            // need in order to decide which actions to do while and after
            // loading the images.
            //
            // Specifically, I have to check if the loaded image have
            // transparency, because if we do a ROI and/or we have a
            // transparent color to set we have to remove it.
            if (i == 0)
            {
                loader.setOverallParameters();
            }

            loader.setSpecificParameters();
            loader.applyColorCorrection();

            // add to the mosaic collection
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Adding to mosaic image number " + i);

            loader.addToMosaic(bound, upperLeft, resolution, i);

            i++;
        }

        PlanarImage croppedImage = loader.cropIfNeeded(mp, intersectionJTSEnvelope, loadedDataSetBound);
        
        croppedImage = makeTransparent(mp.getOutputTransparentColor(),
                croppedImage);

        GeneralEnvelope finalEnvelope = loader.getFinalEnvelope();
        return coverageFactory.create(coverageName, croppedImage, finalEnvelope);
    }


    


    private void loadImage(ImageMosaicLoader loader, ImageReadParam readP,
            Integer imageChoice, Object imageRef) throws IOException
    {
        // Get the band order & add to read parameters if necessary
        if (metadata.hasBandAttributes())
        {
            int[] bands = getBands(imageRef);
            if (bands != null)
            {
                readP.setSourceBands(bands);
            }
        }

        ImageInputStream imageInputStream = getImageInputStream(imageRef);
        loader.loadImage(readP, imageChoice, imageInputStream);
    }

    private double[] getResolution(ImageReadParam readP, Integer imageChoice)
    {
        double[] res;
        if (imageChoice == 0)
        {
            res = new double[highestRes.length];
            res[0] = highestRes[0];
            res[1] = highestRes[1];
        }
        else
        {
            double temp[] = overViewResolutions[imageChoice - 1];
            res = new double[temp.length];
            res[0] = temp[0];
            res[1] = temp[1];

        }
        // adjusting the resolution for the source subsampling
        res[0] *= readP.getSourceXSubsampling();
        res[1] *= readP.getSourceYSubsampling();
        return res;
    }
    

    /**
     * Retrieves the ULC of the BBOX composed by all the tiles we need to load.
     * 
     * @param double
     * @return A {@link Point2D} pointing to the ULC of the smallest area made
     *         by mosaicking all the tile that actually intersect the passed
     *         envelope.
     * @throws IOException
     */
    private ReferencedEnvelope getLoadedTilesEnvelope(List<?> imageRefs)
            throws IOException
    {
        ReferencedEnvelope result = null;
        for (Object imageRef : imageRefs)
        {
            ReferencedEnvelope tileEnvelope = getEnvelope(imageRef);
            if (result == null)
            {
                result = tileEnvelope;
            }
            else
            {
                result.expandToInclude(tileEnvelope);
            }
        }
        return result;

    }
    

    private PlanarImage makeTransparent(Color transparentColor,
            PlanarImage image)
    {
        if (transparentColor != null)
        {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Making image transparent");

            ImageWorker w = new ImageWorker(image);
            w.makeColorTransparent(transparentColor);
            image = w.getPlanarImage();
        }
        return image;
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


    protected GeneralEnvelope transformEnvelope(GeneralEnvelope env) throws DataSourceException
    {
        CoordinateReferenceSystem reqCrs = env.getCoordinateReferenceSystem();
        if (!CRS.equalsIgnoreMetadata(reqCrs, this.crs))
        {
            return env;
        }
        
        try
        {
            // transforming the envelope back to the dataset crs in
            // order to interact with the original envelope for this
            // mosaic.
            MathTransform transform = CRS.findMathTransform(reqCrs, crs, true);
            if (!transform.isIdentity())
            {
                env = CRS.transform(transform, env);
                env.setCoordinateReferenceSystem(this.crs);
    
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine(String.format("Reprojected envelope %s crs %s",
                            env, crs.toWKT()));
            }
        }
        catch (TransformException e)
        {
            throw new DataSourceException(
                    "Unable to create a coverage for this source", e);
        }
        catch (FactoryException e)
        {
            throw new DataSourceException(
                    "Unable to create a coverage for this source", e);
        }
        return env;
    }
}
