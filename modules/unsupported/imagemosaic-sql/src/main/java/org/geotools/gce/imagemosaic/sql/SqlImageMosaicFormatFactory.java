package org.geotools.gce.imagemosaic.sql;

import org.geotools.gce.imagemosaic.base.AbstractImageMosaicFormatFactory;
import org.opengis.coverage.grid.Format;

@SuppressWarnings("deprecation")
public class SqlImageMosaicFormatFactory extends AbstractImageMosaicFormatFactory
{

    public Format createFormat()
    {
        return new SqlImageMosaicFormat();
    }

}
