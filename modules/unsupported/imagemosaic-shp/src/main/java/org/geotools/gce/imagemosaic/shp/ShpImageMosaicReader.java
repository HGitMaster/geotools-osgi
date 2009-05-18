package org.geotools.gce.imagemosaic.shp;

import java.awt.Rectangle;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.ImageMosaicReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.opengis.coverage.grid.Format;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.datum.PixelInCell;

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

    private boolean expandMe;

    private boolean absolutePath; 

    private String locationAttributeName;

    private String bandSelectAttributeName;

    private String colorCorrectionAttributeName;
    
    private Map<String, SimpleFeature> tileMap;

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
        loadProperties();

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
        // /////////////////////////////////////////////////////////////////////
        //
        // Load tiles information, especially the bounds, which will be
        // reused
        //
        // /////////////////////////////////////////////////////////////////////
        ShapefileDataStoreFactory sf = new ShapefileDataStoreFactory();
        tileIndexStore = sf.createDataStore(this.sourceURL);

        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine("Connected mosaic reader to its data store "
                    + sourceURL.toString());
        }
        final String[] typeNames = tileIndexStore.getTypeNames();
        if (typeNames.length <= 0)
        {
            throw new IllegalArgumentException("No typenames for index schema");
        }
        typeName = typeNames[0];
        featureSource = tileIndexStore.getFeatureSource(typeName);
        
        Iterator<SimpleFeature> it = featureSource.getFeatures().iterator();
        while (it.hasNext())
        {
            SimpleFeature feature = it.next();
            String location = (String) feature.getAttribute(locationAttributeName);
            tileMap.put(location, feature);
        }
    }

    private void loadProperties() throws IOException
    {
        String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
        final int index = temp.lastIndexOf(".");
        if (index != -1)
            temp = temp.substring(0, index);
        final File propertiesFile = new File(temp + ".properties");
        if (!propertiesFile.exists() || !propertiesFile.isFile())
        {
            throw new FileNotFoundException(
                    "The properties file descibing the " +
                    "ShpImageMosaic does not exist: "
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
        for (int i = 0; i < 2; i++)
        {
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

        for (int i = 1; i < numOverviews + 1; i++)
        {
            pair = pairs[i].split(",");
            overViewResolutions[i - 1][0] = Double.parseDouble(pair[0]);
            overViewResolutions[i - 1][1] = Double.parseDouble(pair[1]);
        }

        // name
        coverageName = properties.getProperty("Name");

        // need a color expansion?
        // this is a newly added property we have to be ready to the case where
        // we do not find it.
        try
        {
            expandMe = properties.getProperty("ExpandToRGB").equalsIgnoreCase(
                    "true");
        }
        catch (Throwable t)
        {
            expandMe = false;
        }

        originalGridRange = new GeneralGridRange(
                new Rectangle((int) Math.round(originalEnvelope.getLength(0)
                        / highestRes[0]), (int) Math.round(originalEnvelope
                        .getLength(1)
                        / highestRes[1])));
        final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(
                originalGridRange, originalEnvelope);
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);
        raster2Model = geMapper.createTransform();

        String absPathProp = properties.getProperty("", "False");
        absolutePath = Boolean.parseBoolean(absPathProp);

        locationAttributeName = properties.getProperty("LocationAttribute");

        bandSelectAttributeName = 
            properties.getProperty("ChannelSelectAttribute");

        colorCorrectionAttributeName = 
            properties.getProperty("ColorCorrectionAttribute");
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
        ////////////////////////////////////////////////////////////////
        // ///////
        //
        // Load a tile from disk as requested.
        //
        ////////////////////////////////////////////////////////////////
        // ///////
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.fine("About to read image " + imageId);
        }
        final File tempFile = new File(this.sourceURL.getFile());
        final String parentLocation = tempFile.getParent();
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
        
        
        // If the tile is not there, dump a message and continue
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
    protected List<?> getMatchingImageIds(ReferencedEnvelope env)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Format getFormat()
    {
        return new ShpImageMosaicFormat();
    }

}
