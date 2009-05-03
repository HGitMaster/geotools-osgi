package org.geotools.thirdparty.jai.imageio;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationRegistrySpi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;

public class Activator implements BundleActivator {

    /**
     * The META-INF/services registration does not work in OSGi.
     * Thus, we register our operations explicitly.
     *
     */
    public void start(BundleContext context) throws Exception {		
	OperationRegistry opRegistry = JAI.getDefaultInstance().getOperationRegistry();
	new ImageReadWriteSpi().updateRegistry(opRegistry);
    }

    public void stop(BundleContext context) throws Exception {
	// JAI does not provide an unregister method
    }

}
