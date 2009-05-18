package org.geotools.gce.imagemosaic.shp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.ImageMosaicFormat;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

@SuppressWarnings("deprecation")
public class ShpImageMosaicFormat extends ImageMosaicFormat
{
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic.shp");

    @Override
    public boolean accepts(Object source)
    {
        try
        {
            URL sourceURL = getSourceUrl(source);
            if (sourceURL == null)
            {
                return false;
            }
            
            if (!checkShapefile(sourceURL))
            {
                return false;
            }
            
            Properties properties = loadProperties(sourceURL);
            checkEnvelope(properties);
            boolean result = checkLevels(properties);
            return result;
        }
        catch (Exception e)
        {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            return false;
        }

    }

    private Properties loadProperties(URL sourceURL)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException
    {
        String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
        final int index = temp.lastIndexOf(".");
        if (index != -1)
            temp = temp.substring(0, index);
        final File propertiesFile = new File(temp + ".properties");
        
        if (!propertiesFile.exists() || !propertiesFile.isFile())
        {
            String msg = "The properties file describing the ShpImageMosaic " +
            		 "does not exist: " + propertiesFile;
            throw new FileNotFoundException(msg);
        }

        final Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(propertiesFile);
        properties.load(new BufferedInputStream(fis));
        return properties;
    }

    
    private void checkEnvelope(Properties properties) throws IOException
    {
        final String envelope = properties.getProperty("Envelope2D");
        try
        {
            String[] pairs = envelope.split(" ");
            final double cornersV[][] = new double[2][2];
            String pair[];
            for (int i = 0; i < 2; i++)
            {
                pair = pairs[i].split(",");
                cornersV[i][0] = Double.parseDouble(pair[0]);
                cornersV[i][1] = Double.parseDouble(pair[1]);
            }
        }
        catch (IndexOutOfBoundsException formatException)
        {
            throw (IOException) new IOException(
                    "Could not parse Envelope2D=" + envelope
                            + "(check uses of space and comma)")
                    .initCause(formatException);
        }
        catch (Exception unExpected)
        {
            throw (IOException) new IOException(
                    "Could not parse Envelope2D=" + envelope)
                    .initCause(unExpected);
        }        
    }

    
    private boolean checkLevels(Properties properties) throws IOException
    {
        final String levels = properties.getProperty("Levels");
        try
        {
            Integer.parseInt(properties.getProperty("LevelsNum"));
            String[] pairs = levels.split(" ");
            String[] pair = pairs[0].split(",");
            Double.parseDouble(pair[0]);
            Double.parseDouble(pair[1]);
            if (!properties.containsKey("Name"))
                return false;

            if (!properties.containsKey("ExpandToRGB"))
            {
                if (LOGGER.isLoggable(Level.INFO))
                {
                    LOGGER.info("Unable to find ExpandToRGB field. " +
                        "This mosaic may malfunction " +
                        "if the field was forgotten.");
                }
                return true;
            }

            return true;
        }
        catch (IndexOutOfBoundsException formatException)
        {
            throw (IOException) new IOException("Could not parse Levels="
                    + levels + "(check uses of space and comma)")
                    .initCause(formatException);
        }
        catch (Exception unExpected)
        {
            throw (IOException) new IOException(
                    "Could not parse LevelsNum and Levels information")
                    .initCause(unExpected);
        }        
    }
    
    URL getSourceUrl(Object source) throws MalformedURLException
    {
        if (source instanceof File)
        {
            return ((File) source).toURL();
        }
        
        if (source instanceof URL)
            return (URL) source;
        
        
        if (source instanceof String)
        {
            final File tempFile = new File((String) source);
            if (tempFile.exists())
            {
                return tempFile.toURL();
            }
            
            try
            {
                URL sourceURL = new URL(URLDecoder.decode((String) source,
                        "UTF8"));
                if (sourceURL.getProtocol() == "file")
                {
                    return sourceURL;
                }
                else
                {
                    return null;
                }
            }
            catch (MalformedURLException e)
            {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
                return null;

            }
            catch (UnsupportedEncodingException e)
            {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
                return null;
            }
        }
        return null;
    }
    
    
    private boolean checkShapefile(URL sourceURL) throws IOException
    {
        final ShapefileDataStore tileIndexStore = new ShapefileDataStore(
                sourceURL);
        final String[] typeNames = tileIndexStore.getTypeNames();
        if (typeNames.length <= 0)
            return false;
        final String typeName = typeNames[0];
        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = 
            tileIndexStore.getFeatureSource(typeName);
        final SimpleFeatureType schema = featureSource.getSchema();
        // looking for the location attribute
        boolean result = (schema.getDescriptor("location") != null);
        return result;
    }
    

    @Override
    public GridCoverageReader getReader(Object source, Hints hints)
    {
        try
        {
            return new ShpImageMosaicReader(source, hints);
        }
        catch (MalformedURLException e)
        {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        }
        catch (IOException e)
        {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        }
    }
}
