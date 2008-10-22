/**
 * <copyright>
 * </copyright>
 *
 * $Id: CodeTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.ows11.validation;


/**
 * A sample validator interface for {@link net.opengis.ows11.CodeType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface CodeTypeValidator {
    boolean validate();

    boolean validateValue(String value);
    boolean validateCodeSpace(String value);
}
