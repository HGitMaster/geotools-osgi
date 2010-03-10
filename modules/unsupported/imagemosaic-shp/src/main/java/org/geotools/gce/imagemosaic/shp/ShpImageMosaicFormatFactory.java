package org.geotools.gce.imagemosaic.shp;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.imagemosaic.base.AbstractImageMosaicFormatFactory;

public class ShpImageMosaicFormatFactory extends AbstractImageMosaicFormatFactory
{

    public AbstractGridFormat createFormat()
    {
        return new ShpImageMosaicFormat();
    }

}
