/*
 * $RCSfile: JPEGDescriptor.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:57:37 $
 * $State: Exp $
 */
package javax.media.jai.operator;
import com.sun.media.jai.codec.SeekableStream;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An <code>OperationDescriptor</code> describing the "JPEG" operation.
 *
 * <p> The "JPEG" operation reads an image from a JPEG (JFIF) stream.
 *
 * <p><b> The classes in the <code>com.sun.media.jai.codec</code>
 * package are not a committed part of the JAI API.  Future releases
 * of JAI will make use of new classes in their place.  This
 * class will change accordingly.</b>
 * 
 * <p><table border=1>
 * <caption>Resource List</caption>
 * <tr><th>Name</th>        <th>Value</th></tr>
 * <tr><td>GlobalName</td>  <td>JPEG</td></tr>
 * <tr><td>LocalName</td>   <td>JPEG</td></tr>
 * <tr><td>Vendor</td>      <td>com.sun.media.jai</td></tr>
 * <tr><td>Description</td> <td>Reads an image from a JFIF (JPEG) stream.</td></tr>
 * <tr><td>DocURL</td>      <td>http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/JPEGDescriptor.html</td></tr>
 * <tr><td>Version</td>     <td>1.0</td></tr>
 * <tr><td>arg0Desc</td>    <td>The SeekableStream to read from.</td></tr>
 * </table></p>
 *
 * <p><table border=1>
 * <caption>Parameter List</caption>
 * <tr><th>Name</th>    <th>Class Type</th>
 *                      <th>Default Value</th></tr>
 * <tr><td>stream</td>  <td>com.sun.media.jai.codec.SeekableStream</td>
 *                      <td>NO_PARAMETER_DEFAULT</td>
 * </table></p>
 *
 * @see com.sun.media.jai.codec.SeekableStream
 * @see javax.media.jai.OperationDescriptor
 */
public class JPEGDescriptor extends OperationDescriptorImpl {

    /**
     * The resource strings that provide the general documentation and
     * specify the parameter list for the "JPEG" operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "JPEG"},
        {"LocalName",   "JPEG"},
        {"Vendor",      "com.sun.media.jai"},
        {"Description", JaiI18N.getString("JPEGDescriptor0")},
        {"DocURL",      "http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/JPEGDescriptor.html"},
        {"Version",     JaiI18N.getString("DescriptorVersion")},
        {"arg0Desc",    JaiI18N.getString("JPEGDescriptor1")},
    };

    /** The parameter names for the "JPEG" operation. */
    private static final String[] paramNames = {
        "stream"
    };

    /** The parameter class types for the "JPEG" operation. */
    private static final Class[] paramClasses = {
	com.sun.media.jai.codec.SeekableStream.class
    };

    /** The parameter default values for the "JPEG" operation. */
    private static final Object[] paramDefaults = {
        NO_PARAMETER_DEFAULT
    };

    /** Constructor. */
    public JPEGDescriptor() {
        super(resources, 0, paramClasses, paramNames, paramDefaults);
    }


    /**
     * Reads a standard JFIF (JPEG) stream.
     *
     * <p>Creates a <code>ParameterBlockJAI</code> from all
     * supplied arguments except <code>hints</code> and invokes
     * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
     *
     * @see JAI
     * @see ParameterBlockJAI
     * @see RenderedOp
     *
     * @param stream The SeekableStream to read from.
     * @param hints The <code>RenderingHints</code> to use.
     * May be <code>null</code>.
     * @return The <code>RenderedOp</code> destination.
     * @throws IllegalArgumentException if <code>stream</code> is <code>null</code>.
     */
    public static RenderedOp create(SeekableStream stream,
                                    RenderingHints hints)  {
        ParameterBlockJAI pb =
            new ParameterBlockJAI("JPEG",
                                  RenderedRegistryMode.MODE_NAME);

        pb.setParameter("stream", stream);

        return JAI.create("JPEG", pb, hints);
    }
}
