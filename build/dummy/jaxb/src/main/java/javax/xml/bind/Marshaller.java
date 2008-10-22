/*
 * This class is derived from JDK 7 source code, which is licensed under
 * the GPL version 2 with classpath exception. See the OpenJDK project.
 *
 * This is a temporary file with no purpose other than getting GeoTools code to
 * compile with Java 5. This class is not used for execution, is not distributed
 * in any of the released GeoTools JAR files, and will be deleted as soon as
 * GeoTools moves to target Java 6.
 */
package javax.xml.bind;

import java.io.File;

/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
public interface Marshaller {
    public static final String JAXB_ENCODING = "jaxb.encoding";
    public static final String JAXB_FORMATTED_OUTPUT = "jaxb.formatted.output";
    public static final String JAXB_SCHEMA_LOCATION = "jaxb.schemaLocation";
    public static final String JAXB_NO_NAMESPACE_SCHEMA_LOCATION = 
            "jaxb.noNamespaceSchemaLocation";
    public static final String JAXB_FRAGMENT = "jaxb.fragment";
    public void marshal( Object jaxbElement, java.io.OutputStream os ) throws JAXBException;
    public void marshal( Object jaxbElement, File output ) throws JAXBException;
    public void marshal( Object jaxbElement, java.io.Writer writer ) throws JAXBException;
    public void setProperty( String name, Object value );
    public static abstract class Listener {
        public void beforeMarshal(Object source) {}
        public void afterMarshal(Object source) {}
    }
    public void setListener(Listener listener);
    public Listener getListener();
}
