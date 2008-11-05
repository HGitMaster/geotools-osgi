package org.geotools.data.wfs.protocol.wfs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.spi.ServiceRegistry;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;

public class WFSExtensions {
    /**
     * The service registry for this manager. Will be initialized only when first needed.
     */
    private static Set<WFSResponseParserFactory> registry;

    public static WFSResponseParserFactory findParserFactory( WFSResponse response ) {
        Iterator<WFSResponseParserFactory> serviceProviders;
        serviceProviders = getServiceProviders();

        WFSResponseParserFactory factory;
        while( serviceProviders.hasNext() ) {
            factory = serviceProviders.next();
            if (factory.isAvailable()) {
                if (factory.canProcess(response)) {
                    return factory;
                }
            }
        }
        throw new FactoryNotFoundException("Can't find a response parser factory for " + response);
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
