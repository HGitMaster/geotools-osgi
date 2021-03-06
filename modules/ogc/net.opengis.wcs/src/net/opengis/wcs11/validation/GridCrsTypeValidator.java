/**
 * <copyright>
 * </copyright>
 *
 * $Id: GridCrsTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.wcs11.validation;


/**
 * A sample validator interface for {@link net.opengis.wcs11.GridCrsType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface GridCrsTypeValidator {
    boolean validate();

    boolean validateSrsName(Object value);
    boolean validateGridBaseCRS(String value);
    boolean validateGridType(String value);
    boolean validateGridOrigin(Object value);
    boolean validateGridOffsets(Object value);
    boolean validateGridCS(String value);
    boolean validateId(Object value);
}
