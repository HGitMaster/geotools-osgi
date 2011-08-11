package org.geotools.thirdparty.jai.imageio;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sun.media.imageioimpl.plugins.bmp.BMPImageReaderSpi;
import com.sun.media.imageioimpl.plugins.bmp.BMPImageWriterSpi;
import com.sun.media.imageioimpl.plugins.gif.GIFImageWriterSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReaderSpi;
import com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriterSpi;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLibSpi;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;
import com.sun.media.imageioimpl.plugins.png.CLibPNGImageReaderSpi;
import com.sun.media.imageioimpl.plugins.png.CLibPNGImageWriterSpi;
import com.sun.media.imageioimpl.plugins.pnm.PNMImageReaderSpi;
import com.sun.media.imageioimpl.plugins.pnm.PNMImageWriterSpi;
import com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi;
import com.sun.media.imageioimpl.plugins.raw.RawImageWriterSpi;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import com.sun.media.imageioimpl.plugins.wbmp.WBMPImageReaderSpi;
import com.sun.media.imageioimpl.plugins.wbmp.WBMPImageWriterSpi;
import com.sun.media.imageioimpl.stream.ChannelImageInputStreamSpi;
import com.sun.media.imageioimpl.stream.ChannelImageOutputStreamSpi;
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
        
        IIORegistry iioRegistry = IIORegistry.getDefaultInstance();
        iioRegistry.registerServiceProvider(new ChannelImageInputStreamSpi(), ImageInputStreamSpi.class);
        iioRegistry.registerServiceProvider(new ChannelImageOutputStreamSpi(), ImageOutputStreamSpi.class);

        iioRegistry.registerServiceProvider(new CLibJPEGImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new CLibPNGImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new J2KImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new J2KImageReaderCodecLibSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new WBMPImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new BMPImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new PNMImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new RawImageReaderSpi(), ImageReaderSpi.class);
        iioRegistry.registerServiceProvider(new TIFFImageReaderSpi(), ImageReaderSpi.class);
        
        iioRegistry.registerServiceProvider(new CLibJPEGImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new CLibPNGImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new J2KImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new J2KImageWriterCodecLibSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new WBMPImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new BMPImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new GIFImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new PNMImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new RawImageWriterSpi(), ImageWriterSpi.class);
        iioRegistry.registerServiceProvider(new TIFFImageWriterSpi(), ImageWriterSpi.class);
    }

    public void stop(BundleContext context) throws Exception {
        // JAI does not provide an unregister method
    }

}
