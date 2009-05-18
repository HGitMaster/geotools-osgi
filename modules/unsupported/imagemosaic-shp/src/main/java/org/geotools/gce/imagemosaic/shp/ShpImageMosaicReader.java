package org.geotools.gce.imagemosaic.shp;

import java.io.IOException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.base.ImageMosaicReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.coverage.grid.Format;

@SuppressWarnings("deprecation")
public class ShpImageMosaicReader extends ImageMosaicReader
{

    public ShpImageMosaicReader(Object source) throws IOException
    {
        super(source);
        // TODO Auto-generated constructor stub
    }

    
    public ShpImageMosaicReader(Object source, Hints hints) throws IOException
    {
        super(source, hints);
    }

    
    
    @Override
    protected ReferencedEnvelope getEnvelope(Object imageId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ImageInputStream getImageInputStream(Object imageId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<?> getMatchingImageIds(ReferencedEnvelope env)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Format getFormat()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
