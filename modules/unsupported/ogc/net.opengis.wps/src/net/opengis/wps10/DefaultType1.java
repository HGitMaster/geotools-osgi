/**
 * <copyright>
 * </copyright>
 *
 * $Id: DefaultType1.java 31841 2008-11-14 13:21:26Z jdeolive $
 */
package net.opengis.wps10;

import net.opengis.ows11.DomainMetadataType;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Default Type1</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps10.DefaultType1#getUOM <em>UOM</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps10.Wps10Package#getDefaultType1()
 * @model extendedMetaData="name='Default_._2_._type' kind='elementOnly'"
 * @generated
 */
public interface DefaultType1 extends EObject {
    /**
     * Returns the value of the '<em><b>UOM</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Reference to the default UOM supported for this Input/Output
     * <!-- end-model-doc -->
     * @return the value of the '<em>UOM</em>' containment reference.
     * @see #setUOM(DomainMetadataType)
     * @see net.opengis.wps10.Wps10Package#getDefaultType1_UOM()
     * @model containment="true" required="true"
     *        extendedMetaData="kind='element' name='UOM' namespace='http://www.opengis.net/ows/1.1'"
     * @generated
     */
    DomainMetadataType getUOM();

    /**
     * Sets the value of the '{@link net.opengis.wps10.DefaultType1#getUOM <em>UOM</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>UOM</em>' containment reference.
     * @see #getUOM()
     * @generated
     */
    void setUOM(DomainMetadataType value);

} // DefaultType1
