package org.geotools.data.wfs.protocol.wfs;

import java.util.Arrays;
import java.util.Iterator;

import org.geotools.data.wfs.WFSResponseParserFactory;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;

public class WFSExtensions {
    /**
     * The service registry for this manager. Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    public static WFSResponseParser findParser( WFSResponse response ) {
        FactoryRegistry serviceRegistry = getServiceRegistry();
        Iterator<WFSResponseParserFactory> serviceProviders;
        serviceProviders = serviceRegistry.getServiceProviders(WFSResponseParserFactory.class,
                false);

        WFSResponseParserFactory factory;
        while( serviceProviders.hasNext() ) {
            factory = serviceProviders.next();
            if (factory.isAvailable()) {
                if (factory.canProcess(response)) {
                    WFSResponseParser parser = factory.createParser(response);
                    return parser;
                }
            }
        }
        throw new FactoryNotFoundException("Can't find a response parser factory for " + response);
    }

    /**
     * Returns the service registry. The registry will be created the first time this method is
     * invoked.
     */
    private static synchronized FactoryRegistry getServiceRegistry() {
        if (registry == null) {
            registry = new FactoryCreator(Arrays
                    .asList(new Class< ? >[]{WFSResponseParserFactory.class}));
        }
        return registry;
    }
}
