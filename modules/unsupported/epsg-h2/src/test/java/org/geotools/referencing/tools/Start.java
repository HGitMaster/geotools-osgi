package org.geotools.referencing.tools;

import javax.sql.DataSource;

import org.geotools.referencing.factory.epsg.ThreadedH2EpsgFactory;
import org.h2.tools.Server;

/**
 * Starts a H2 console connected to the EPSG database so that you can explore it
 * interactively
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/epsg-h2/src/test/java/org/geotools/referencing/tools/Start.java $
 */
public class Start {
    public static void main(String[] args) throws Exception {
        ThreadedH2EpsgFactory factory = new ThreadedH2EpsgFactory();
        DataSource source = factory.getDataSource();
        Server s = new Server();
        s.startWebServer(source.getConnection());
    }
}
