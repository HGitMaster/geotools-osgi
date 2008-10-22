/*
 * This class is derived from JDK 7 source code, which is licensed under
 * the GPL version 2 with classpath exception. See the OpenJDK project.
 *
 * This is a temporary file with no purpose other than getting GeoTools code to
 * compile with Java 5. This class is not used for execution, is not distributed
 * in any of the released GeoTools JAR files, and will be deleted as soon as
 * GeoTools moves to target Java 6.
 */
package javax.xml.bind.annotation.adapters;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.PACKAGE;

/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
@Retention(SOURCE) @Target({PACKAGE,FIELD,METHOD,TYPE,PARAMETER})        
public @interface XmlJavaTypeAdapter {
    Class<? extends XmlAdapter> value();
    Class type() default DEFAULT.class;
    static final class DEFAULT {}    
}
