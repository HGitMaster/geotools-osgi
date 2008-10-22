/**
 * <copyright>
 * </copyright>
 *
 * $Id: UOMsType.java 30760 2008-06-18 14:28:24Z desruisseaux $
 */
package net.opengis.wps;

import java.util.List;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UO Ms Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Identifies a UOM supported for this input or output.
 * <!-- end-model-doc -->
 *
 *
 * @see net.opengis.wps.WpsPackage#getUOMsType()
 * @model extendedMetaData="name='UOMsType' kind='elementOnly'"
 * @generated
 */
public interface UOMsType extends EObject {
	/**
	 * Returns the value of the '<em><b>UOM</b></em>' containment reference list.
	 * The list contents are of type {@link javax.measure.unit.Unit}.
	 * <!-- begin-user-doc -->
	 * 1. Changed net.opengis.ows11.DomainMetadataType to javax.measure.unit.Unit
	 * 2. Removed all annotations and changed EList return type to just List and
	 * also updated the Impl class the same way.
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to a UOM supported for this input or output.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>UOM</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getUOMsType_UOM()
	 * 
	 */
	List getUOM();

} // UOMsType
