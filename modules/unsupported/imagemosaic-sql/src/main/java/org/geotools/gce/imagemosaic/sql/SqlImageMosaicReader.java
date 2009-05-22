package org.geotools.gce.imagemosaic.sql;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.AbstractImageMosaicReader;
import org.geotools.gce.imagemosaic.base.ImageMosaicMetadata;
import org.geotools.gce.imagemosaic.base.ImageMosaicMetadataImpl;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

@SuppressWarnings("deprecation")
public class SqlImageMosaicReader extends AbstractImageMosaicReader
{
    /** Logger. */
    private static Logger LOGGER = Logging.getLogger(SqlImageMosaicReader.class);

    private URL sourceURL;

    private boolean absolutePath; 

    private Config config;

    private ImageMosaicMetadataImpl sqlMetadata;

    private JDBCAccess jdbcAccess;

    private ImageLevelInfo levelInfo;

    

    public SqlImageMosaicReader(Object source) throws IOException
    {
        this(source, null);
    }

    public SqlImageMosaicReader(Object source, Hints hints) throws IOException
    {
        super(source, hints);
        this.source = source;

        URL url = SqlImageMosaicFormat.getURLFromSource(source);

        if (url == null)
        {
            throw new MalformedURLException(source.toString());
        }

        try
        {
            config = Config.readFrom(url);
        }
        catch (Exception e)
        {
            LOGGER.severe(e.getMessage());
            throw new IOException(e.getMessage());
        }

        try
        {
            jdbcAccess = JDBCAccessFactory.getJDBCAcess(config);
        }
        catch (Exception e1)
        {
            LOGGER.severe(e1.getLocalizedMessage());
            throw new IOException(e1.getLocalizedMessage());
        }

        findCoordinateReferenceSystem();
        buildMetadata();
    }

    private void findCoordinateReferenceSystem()
    {
        Object tempCRS = this.hints
                .get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);

        if (tempCRS != null)
        {
            this.crs = (CoordinateReferenceSystem) tempCRS;
            LOGGER.warning("Using forced coordinate reference system " + 
                    crs.toWKT());
        }
        else if (config.getCoordsys() != null)
        {
            String srsString = config.getCoordsys();

            try
            {
                crs = CRS.decode(srsString, false);
            }
            catch (Exception e)
            {
                LOGGER.log(Level.SEVERE, "Could not find " + srsString, e);
            }
        }
        else
        {
            CoordinateReferenceSystem crs = jdbcAccess.getLevelInfo(0).getCrs();

            if (crs == null)
            {
                crs = AbstractGridFormat.getDefaultCRS();
                LOGGER.warning("Unable to find a CRS for this coverage, " +
                    "using a default one: " + crs.toWKT());
            }
        }
    }

    
    
    private void buildMetadata()
    {
        int numLevels = jdbcAccess.getNumOverviews()+1;
        sqlMetadata = new ImageMosaicMetadataImpl(numLevels);
        ImageLevelInfo levelInfo = jdbcAccess.getLevelInfo(0);
        sqlMetadata.setCrs(super.crs);
        Envelope levelEnv = levelInfo.getEnvelope();
        ReferencedEnvelope env = new ReferencedEnvelope(levelEnv, super.crs);
        sqlMetadata.setEnvelope(env);
        sqlMetadata.setName(config.getCoverageName());
        
        List<double[]> resolutions = sqlMetadata.getResolutions();
        for (int i = 0; i < numLevels; i++)
        {
            double[] res = jdbcAccess.getLevelInfo(i).getResolution();
            resolutions.add(res);
        }
    }
    
    @Override
    protected ReferencedEnvelope getEnvelope(Object imageId)
    {
        org.opengis.geometry.Envelope envelope = 
            jdbcAccess.getTileEnvelope(levelInfo, imageId);
        return new ReferencedEnvelope(envelope);
    }

    @Override
    protected ImageInputStream getImageInputStream(Object imageId) 
        throws IOException
    {
        return jdbcAccess.getImageInputStream(levelInfo, imageId);
    }

    @Override
    protected List<?> getMatchingImageRefs(ReferencedEnvelope intersectionEnv)
            throws IOException
    {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("loading tile for envelope " + intersectionEnv);

        levelInfo = jdbcAccess.getLevelInfo(0);
        GeneralEnvelope envelope = new GeneralEnvelope(intersectionEnv);
        List<?> result = jdbcAccess.getMatchingTileIds(levelInfo, envelope);
        return result;
    }

    public Format getFormat()
    {
        return new SqlImageMosaicFormat();
    }
    

    @Override
    protected ImageMosaicMetadata createMetadata()
    {
        assert sqlMetadata != null;
        return sqlMetadata;
    }

    @Override
    protected int[] getBands(Object imageId)
    {
        throw new UnsupportedOperationException();
    }
}
