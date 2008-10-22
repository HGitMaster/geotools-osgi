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

/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
public abstract class JAXBContext {
    public static final String JAXB_CONTEXT_FACTORY = 
        "javax.xml.bind.context.factory";
    protected JAXBContext() {}
    public static JAXBContext newInstance( String contextPath ) throws JAXBException {
        return null;
    }
    public static JAXBContext newInstance( Class... classesToBeBound ) throws JAXBException {
        return null;
    }
    public abstract Unmarshaller createUnmarshaller() throws JAXBException;
    public abstract Marshaller createMarshaller() throws JAXBException;
}
