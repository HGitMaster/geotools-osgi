package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.spi.ServiceRegistry;

import net.opengis.wfs.BaseRequestType;

import org.geotools.data.wfs.v1_1_0.WFS_1_1_0_DataStore;
import org.geotools.factory.FactoryNotFoundException;

@SuppressWarnings("nls")
public class WFSExtensions {
    /**
     * The service registry for this manager. Will be initialized only when first needed.
     */
    private static Set<WFSResponseParserFactory> registry;

    /**
     * Processes the result of a WFS operation and returns the parsed object.
     * <p>
     * The result can either be:
     * <ul>
     * <li>a {@link WFSException} exception if the WFS response was an exception report
     * <li>a {@link GetFeatureParser} if the WFS returned a FeatureCollection
     * </p>
     * 
     * @param request the WFS request that originated the given response
     * @param response the handle to the WFS response contents
     * @return
     * @throws IOException 
     */
    public static Object process( WFS_1_1_0_DataStore wfs, WFSResponse response ) throws IOException {

        BaseRequestType originatingRequest = response.getOriginatingRequest();
        WFSResponseParserFactory pf = findParserFactory(originatingRequest);

        WFSResponseParser parser = pf.createParser(wfs, response);

        Object result = parser.parse(wfs, response);
        return result;
    }

    /**
     * @param requestType
     * @param outputFormat
     * @return
     * @throws FactoryNotFoundException
     */
    public static WFSResponseParserFactory findParserFactory( BaseRequestType request ) {
        Iterator<WFSResponseParserFactory> serviceProviders;
        serviceProviders = getServiceProviders();

        WFSResponseParserFactory factory;
        while( serviceProviders.hasNext() ) {
            factory = serviceProviders.next();
            if (factory.isAvailable()) {
                if (factory.canProcess(request)) {
                    return factory;
                }
            }
        }
        throw new FactoryNotFoundException("Can't find a response parser factory for " + request);
    }

    private static Iterator<WFSResponseParserFactory> getServiceProviders() {
        if (registry == null) {
            synchronized (WFSExtensions.class) {
                if (registry == null) {
                    Iterator<WFSResponseParserFactory> providers;
                    providers = ServiceRegistry.lookupProviders(WFSResponseParserFactory.class);
                    registry = new HashSet<WFSResponseParserFactory>();
                    while( providers.hasNext() ) {
                        WFSResponseParserFactory provider = providers.next();
                        registry.add(provider);
                    }
                }
            }
        }
        return registry.iterator();
    }

}
