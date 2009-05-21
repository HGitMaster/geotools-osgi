package org.geotools.gce.imagemosaic.shp;

import org.geotools.gce.imagemosaic.base.AbstractImageMosaicFormatFactory;
import org.opengis.coverage.grid.Format;

@SuppressWarnings("deprecation")
public class ShpImageMosaicFormatFactory extends AbstractImageMosaicFormatFactory
{

    public Format createFormat()
    {
        return new ShpImageMosaicFormat();
    }

}
