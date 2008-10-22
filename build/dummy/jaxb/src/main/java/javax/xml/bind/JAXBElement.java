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

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * All classes in the Geotools jaxb modules are place holders for the JAXB API 
 * used only for testing on Java 5 JVM platforms. These classes will be removed 
 * once Geotools targets the Java 6 platform since that includes the JAXB API 
 * by default.
 *
 */
public class JAXBElement<T> implements Serializable {
    final protected QName name;
    final protected Class<T> declaredType;
    final protected Class scope;
    protected T value;
    protected boolean nil = false;
    public static final class GlobalScope {}
    public JAXBElement(QName name, Class<T> declaredType, Class scope, T value) {
        if(declaredType==null || name==null)
            throw new IllegalArgumentException();
        this.declaredType = declaredType;
        if(scope==null)     scope = GlobalScope.class;
        this.scope = scope;
        this.name = name;
        setValue(value);
    }
    public JAXBElement(QName name, Class<T> declaredType, T value ) {
        this(name,declaredType,GlobalScope.class,value);
    }
    public Class<T> getDeclaredType() {
        return declaredType;
    }
    public QName getName() { return name; }
    public void setValue(T t) { this.value = t; }
    public T getValue() { return value; }
    public Class getScope() { return scope; }
    public boolean isNil() { return (value == null) || nil; }
    public void setNil(boolean value) { this.nil = value; }    
    public boolean isGlobalScope() { return this.scope == GlobalScope.class; }
    public boolean isTypeSubstituted() {
        if(value==null)     return false;
        return value.getClass() != declaredType;
    }
    private static final long serialVersionUID = 1L;
}
