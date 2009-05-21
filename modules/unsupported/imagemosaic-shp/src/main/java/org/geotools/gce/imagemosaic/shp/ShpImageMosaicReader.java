package org.geotools.gce.imagemosaic.shp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.ImageMosaicMetadata;
import org.geotools.gce.imagemosaic.base.ImageMosaicMetadataImpl;
import org.geotools.gce.imagemosaic.base.ImageMosaicReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

@SuppressWarnings("deprecation")
public class ShpImageMosaicReader extends ImageMosaicReader
{
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic.shp");

    private URL sourceURL;

    private DataStore tileIndexStore;

    private String typeName;

    private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

    private boolean absolutePath; 

    private String locationAttrName;

    private String bandSelectAttrName;

    private String colorCorrectionAttrName;
    
    private Map<String, SimpleFeature> tileMap;

    private SoftReference<MemorySpatialIndex> indexRef;
    
    private ImageMosaicMetadataImpl shpMetadata;

    

    public ShpImageMosaicReader(Object source) throws IOException
    {
        this(source, null);
    }

    public ShpImageMosaicReader(Object source, Hints hints) throws IOException
    {
        super(source, hints);
        this.tileMap = new HashMap<String, SimpleFeature>();
        checkSource(source);
        openShapefile();
        Properties props = loadProperties();
        findCoordinateReferenceSystem();
        buildMetadata(props);
        loadShapefile();
        createIndex();
    }

    private void checkSource(Object source) throws DataSourceException,
            MalformedURLException
    {
        ShpImageMosaicFormat format = new ShpImageMosaicFormat();
        sourceURL = format.getSourceUrl(source);
        if (sourceURL == null)
        {
            throw new DataSourceException("Invalid source: " + source);
        }
    }

    private void openShapefile() throws IOException
    {
        ShapefileDataStoreFactory sf = new ShapefileDataStoreFactory();
        tileIndexStore = sf.createDataStore(this.sourceURL);

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine("Connected to shapefile " + sourceURL);
        }
        String[] typeNames = tileIndexStore.getTypeNames();
        if (typeNames.length <= 0)
        {
            throw new IllegalArgumentException("No typenames for index schema");
        }
        typeName = typeNames[0];
        featureSource = tileIndexStore.getFeatureSource(typeName);
    }

    private void loadShapefile() throws IOException
    {
        Iterator<SimpleFeature> it = featureSource.getFeatures().iterator();
        while (it.hasNext())
        {
            SimpleFeature feature = it.next();
            String location = (String) feature.getAttribute(locationAttrName);
            tileMap.put(location, feature);
        }
    }

    private Properties loadProperties() throws IOException
    {
        String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
        int index = temp.lastIndexOf(".");
        if (index != -1)
            temp = temp.substring(0, index);
        File propertiesFile = new File(temp + ".properties");
        if (!propertiesFile.exists() || !propertiesFile.isFile())
        {
            throw new FileNotFoundException(
                    "ShpImageMosaic properties not found: " + propertiesFile);
        }
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(
                propertiesFile)));
        return properties;
    }
    
    private void findCoordinateReferenceSystem()
    {
        Object tempCRS = hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
        if (tempCRS != null)
        {
            this.crs = (CoordinateReferenceSystem) tempCRS;
            LOGGER.warning("Using forced coordinate reference system " + crs.toWKT());
        }
        else
        {
            GeometryDescriptor gd = featureSource.getSchema()
                .getGeometryDescriptor();
            crs = gd.getCoordinateReferenceSystem();
            if (crs == null)
            {
                // use the default crs
                crs = AbstractGridFormat.getDefaultCRS();
                String msg = String.format(
                        "Unable to find a CRS for this coverage, " +
                        "using a default one: %s",
                         crs.toWKT());
                LOGGER.warning(msg);
            }
        }
    }

    
    
    private void buildMetadata(Properties properties)
    {
        int numLevels = Integer.parseInt(properties.getProperty("LevelsNum"));

        shpMetadata = new ImageMosaicMetadataImpl(numLevels);
        

        // load the envelope
        String envelope = properties.getProperty("Envelope2D");
        String[] pairs = envelope.split(" ");
        double cornersV[][] = new double[2][2];
        String pair[];
        for (int i = 0; i < 2; i++)
        {
            pair = pairs[i].split(",");
            cornersV[i][0] = Double.parseDouble(pair[0]);
            cornersV[i][1] = Double.parseDouble(pair[1]);
        }
        GeneralEnvelope env = new GeneralEnvelope(cornersV[0], cornersV[1]);
        env.setCoordinateReferenceSystem(crs);
        shpMetadata.setEnvelope(env);

        
        
        
        String levels = properties.getProperty("Levels");
        pairs = levels.split(" ");
        
        List<double[]> resolutions = shpMetadata.getResolutions();
        for (int level = 0; level < numLevels; level++)
        {
            double[] res = new double[2];
            pair = pairs[0].split(",");
            res[0] = Double.parseDouble(pair[0]);
            res[1] = Double.parseDouble(pair[1]);
            resolutions.add(res);

            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.fine(String.format("Resolution %f %f", res[0], res[1]));
            }
        }
        
        shpMetadata.setName(properties.getProperty("Name"));

        String colorExpansion = properties.getProperty("ExpandToRGB", "false");
        if (colorExpansion.equalsIgnoreCase("true"))
        {
            shpMetadata.setColorModelExpansion(true);
        }

        String absPathProp = properties.getProperty("AbsolutePath", "false");
        absolutePath = Boolean.parseBoolean(absPathProp);

        locationAttrName = properties.getProperty("LocationAttribute");
        bandSelectAttrName = properties.getProperty("ChannelSelectAttribute");
        colorCorrectionAttrName = properties.getProperty("ColorCorrectionAttribute");
    }
    
    @Override
    protected ReferencedEnvelope getEnvelope(Object imageId)
    {
        SimpleFeature feature = tileMap.get(imageId);
        ReferencedEnvelope env = ReferencedEnvelope.reference(feature.getBounds());
        return env;
    }

    @Override
    protected ImageInputStream getImageInputStream(Object imageId) throws IOException
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine("About to read image " + imageId);
        }
        
        String location = (String) imageId;
        
        File imageFile;
        if (absolutePath)
        {
            imageFile = new File(location);
        }
        else
        {
            String dir = new File(sourceURL.getFile()).getParent();
            imageFile = new File(dir, location);
        }
        
        
        if (!imageFile.exists() || !imageFile.canRead() || !imageFile.isFile())
        {
            return null;
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("File found");

        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        return iis;
    }

    @Override
    protected List<?> getMatchingImageRefs(ReferencedEnvelope env)
            throws IOException
    {
        GeneralEnvelope originalEnv = shpMetadata.getEnvelope();
        GeneralEnvelope requestedOriginalEnv = null;
        GeneralEnvelope intersectionEnv = null;
        if (requestedOriginalEnv != null)
        {
            requestedOriginalEnv = transformEnvelope(requestedOriginalEnv);
            if (!requestedOriginalEnv.intersects(originalEnv, true))
            {
                LOGGER.warning("The requested envelope does not intersect " +
                    "the envelope of this mosaic, " +
                    "we will return a null coverage.");
                throw new DataSourceException(
                        "Unable to create a coverage for this source");
            }
            intersectionEnv = new GeneralEnvelope(
                    requestedOriginalEnv);
            // intersect the requested area with the bounds of this layer
            intersectionEnv.intersect(originalEnv);
        }
        else
        {
            requestedOriginalEnv = new GeneralEnvelope(originalEnv);
            intersectionEnv = requestedOriginalEnv;
        }
        requestedOriginalEnv.setCoordinateReferenceSystem(this.crs);
        intersectionEnv.setCoordinateReferenceSystem(this.crs);

        // ok we got something to return, let's load records from the index
        // Prepare the filter for loading th needed layers
        ReferencedEnvelope intersectionJTSEnvelope = new ReferencedEnvelope(
                intersectionEnv.getMinimum(0), intersectionEnv
                        .getMaximum(0), intersectionEnv.getMinimum(1),
                intersectionEnv.getMaximum(1), crs);

        // Load features from the index
        // In case there are no features under the requested bbox which is legal
        // in case the mosaic is not a real sqare, we return a fake mosaic.
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("loading tile for envelope "
                    + intersectionJTSEnvelope.toString());

        List<SimpleFeature> features = getFeaturesFromIndex(intersectionJTSEnvelope);
        if (features.isEmpty())
        {
            return null;
        }

        List<String> locations = new ArrayList<String>(features.size());
        for (SimpleFeature f : features)
        {
            String location = (String) f.getAttribute(locationAttrName);
            locations.add(location);
        }
        
        return locations;
    }

    private GeneralEnvelope transformEnvelope(GeneralEnvelope env) throws DataSourceException
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

    public Format getFormat()
    {
        return new ShpImageMosaicFormat();
    }
    
    private void createIndex() throws IOException
    {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("About to create index");
        // compare the created date of the index with the date on
        // the shapefile
        MemorySpatialIndex index = new MemorySpatialIndex(featureSource.getFeatures());

        if (indexRef == null)
        {
            indexRef = new SoftReference<MemorySpatialIndex>(index);
        }
        SimpleFeatureType schema = featureSource.getSchema();
        
        if (schema.getDescriptor(bandSelectAttrName) != null)
        {
            shpMetadata.setHasBandAttributes(true);
        }
        if (schema.getDescriptor(colorCorrectionAttrName) != null)
        {
            shpMetadata.setHasColorCorrection(true);
        }

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Created index");
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
    private List<SimpleFeature> getFeaturesFromIndex(Envelope envelope)
            throws IOException
    {
        List<SimpleFeature> features = null;
        MemorySpatialIndex index;

        synchronized (indexRef)
        {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Trying to use the index...");
            index = indexRef.get();
            if (index != null)
            {
                // need to see if the index is still valid and recreate it if
                // necessary
                // this is currently done by comparing the date the index was
                // created to
                // the date the shapefile was last modified
                File f = new File(sourceURL.getFile());
                if (index.getCreatedDate().before(new Date(f.lastModified())))
                {
                    createIndex();
                }
                else if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Index does not need to be created...");

            }
            else
            {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Index needs to be recreated...");
                index = new MemorySpatialIndex(featureSource.getFeatures());
            }
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Index loaded");
        }
        features = index.findFeatures(envelope);
        if (features != null)
            return features;
        else
            return Collections.emptyList();
    }

    @Override
    protected ImageMosaicMetadata createMetadata()
    {
        return shpMetadata;
    }

    @Override
    protected int[] getBands(Object imageId)
    {
        SimpleFeature feature = tileMap.get(imageId);
        String attr = (String) feature.getAttribute(bandSelectAttrName);
        String[] sbands = attr.split(",");
        if (sbands.length == 3)
        {
            int bands[] = new int[3];
            bands[0] = Integer.parseInt(sbands[0]);
            bands[1] = Integer.parseInt(sbands[1]);
            bands[2] = Integer.parseInt(sbands[2]);
            return bands;
        }

        return null;
    }
}
