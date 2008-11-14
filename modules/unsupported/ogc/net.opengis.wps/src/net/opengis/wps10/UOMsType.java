/**
 * <copyright>
 * </copyright>
 *
 * $Id: UOMsType.java 31841 2008-11-14 13:21:26Z jdeolive $
 */
package net.opengis.wps10;

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
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps10.UOMsType#getUOM <em>UOM</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps10.Wps10Package#getUOMsType()
 * @model extendedMetaData="name='UOMsType' kind='elementOnly'"
 * @generated
 */
public interface UOMsType extends EObject {
    /**
     * Returns the value of the '<em><b>UOM</b></em>' containment reference list.
     * The list contents are of type {@link net.opengis.ows11.DomainMetadataType}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Reference to a UOM supported for this input or output.
     * <!-- end-model-doc -->
     * @return the value of the '<em>UOM</em>' containment reference list.
     * @see net.opengis.wps10.Wps10Package#getUOMsType_UOM()
     * @model type="net.opengis.ows11.DomainMetadataType" containment="true" required="true"
     *        extendedMetaData="kind='element' name='UOM' namespace='http://www.opengis.net/ows/1.1'"
     * @generated
     */
    EList getUOM();

} // UOMsType
