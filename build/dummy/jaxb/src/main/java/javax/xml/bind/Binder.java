/*
 * This class is derived from JDK 7 source code, which is licensed under
 * the GPL version 2 with classpath exception. See the OpenJDK project.
 *
 * This is a temporary file with no purpose other than getting GeoTools code to
 * compile with Java 5. This class is not used for execution, is not distributed
 * in any of the released Geotools JAR files, and will be deleted as soon as
 * Geotools moves to target Java 6.
 */
package javax.xml.bind;

import org.w3c.dom.Node;
/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
public abstract class Binder<XmlNode> {
    public abstract Object unmarshal( XmlNode xmlNode ) throws JAXBException;
    public abstract <T> JAXBElement<T>
	unmarshal( XmlNode xmlNode, Class<T> declaredType )
	throws JAXBException;
    public abstract void marshal( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;
    public abstract XmlNode getXMLNode( Object jaxbObject );
    public abstract Object getJAXBNode( XmlNode xmlNode );
    public abstract XmlNode updateXML( Object jaxbObject ) throws JAXBException;
    public abstract XmlNode updateXML( Object jaxbObject, XmlNode xmlNode ) throws JAXBException;
    public abstract Object updateJAXB( XmlNode xmlNode ) throws JAXBException;
}
