/*
 * This class is derived from JDK 7 source code, which is licensed under
 * the GPL version 2 with classpath exception. See the OpenJDK project.
 *
 * This is a temporary file with no purpose other than getting GeoTools code to
 * compile with Java 5. This class is not used for execution, is not distributed
 * in any of the released GeoTools JAR files, and will be deleted as soon as
 * GeoTools moves to target Java 6.
 */
package javax.xml.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
@Retention(SOURCE) @Target({FIELD, METHOD})
public @interface XmlElement {
    String name() default "##default";
    boolean nillable() default false;
    boolean required() default false;
    String namespace() default "##default";
    String defaultValue() default "\u0000";
    Class type() default DEFAULT.class;
    static final class DEFAULT {}
}
