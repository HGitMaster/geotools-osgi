package org.geotools.gce.imagemosaic.sql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.AbstractImageMosaicFormat;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.GridCoverageReader;

@SuppressWarnings("deprecation")
public class SqlImageMosaicFormat extends AbstractImageMosaicFormat
{
    /** Logger. */
    private final static Logger LOGGER = 
        Logging.getLogger(SqlImageMosaicFormat.class);

    public SqlImageMosaicFormat()
    {
        setInfo();
    }

    @Override
    protected void setInfo()
    {
        super.setInfo();
        Map<String, String> info = new HashMap<String, String>();
        info.put("name", "ShpImageMosaic");
        info.put("description", "Image Mosaics via SQL Database");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", "2.6.0");
        mInfo = info;
    }

    @Override
    public boolean accepts(Object source)
    {
        if (source == null)
        {
            return false;
        }

        URL sourceUrl = getURLFromSource(source);

        if (sourceUrl == null)
        {
            return false;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            InputStream in = (InputStream) sourceUrl.getContent();
            int c;

            while ((c = in.read()) != -1)
                out.write(c);

            in.close();
            out.close();
        }
        catch (IOException e)
        {
            return false;
        }

        return out.toString().indexOf("coverageName") != -1;
    }

    public static URL getURLFromSource(Object source)
    {
        if (source == null)
        {
            return null;
        }

        URL sourceURL = null;

        try
        {
            if (source instanceof File)
            {
                sourceURL = ((File) source).toURI().toURL();
            }
            else if (source instanceof URL)
            {
                sourceURL = (URL) source;
            }
            else if (source instanceof String)
            {
                final File tempFile = new File((String) source);

                if (tempFile.exists())
                {
                    sourceURL = tempFile.toURI().toURL();
                }
                else
                {
                    sourceURL = new URL(URLDecoder.decode((String) source,
                            "UTF8"));
                }
            }
        }
        catch (Exception e)
        {
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }

            return null;
        }

        return sourceURL;
    }

    @Override
    public GridCoverageReader getReader(Object source, Hints hints)
    {
        try
        {
            return new SqlImageMosaicReader(source, hints);
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
