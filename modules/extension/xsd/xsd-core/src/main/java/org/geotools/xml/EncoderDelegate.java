package org.geotools.xml;

import java.io.IOException;

import org.xml.sax.ContentHandler;

/**
 * An interface used to signal to the {@link Encoder} that it should delegate
 * to the object itself to encode, rather than work the object through the typical
 * encoding routine.
 *  
 * @author Justin Deoliveira, OpenGEO
 *
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/xsd/xsd-core/src/main/java/org/geotools/xml/EncoderDelegate.java $
 */
public interface EncoderDelegate {

    /**
     * Encodes content to an output stream.
     */
    void encode( ContentHandler output ) throws Exception;
}
