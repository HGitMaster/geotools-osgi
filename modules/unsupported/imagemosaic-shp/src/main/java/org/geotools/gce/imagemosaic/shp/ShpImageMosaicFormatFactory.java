package org.geotools.gce.imagemosaic.shp;

import org.geotools.gce.imagemosaic.base.ImageMosaicFormatFactory;
import org.opengis.coverage.grid.Format;

@SuppressWarnings("deprecation")
public class ShpImageMosaicFormatFactory extends ImageMosaicFormatFactory
{

    public Format createFormat()
    {
        return new ShpImageMosaicFormat();
    }

}
