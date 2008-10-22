/**
 * <copyright>
 * </copyright>
 *
 * $Id: CRSsType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>CR Ss Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Identifies a Coordinate Reference System (CRS) supported for this input or output.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.CRSsType#getCRS <em>CRS</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getCRSsType()
 * @model extendedMetaData="name='CRSsType' kind='elementOnly'"
 * @generated
 */
public interface CRSsType extends EObject {
	/**
	 * Returns the value of the '<em><b>CRS</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to a CRS supported for this Input/Output.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>CRS</em>' attribute.
	 * @see #setCRS(String)
	 * @see net.opengis.wps.WpsPackage#getCRSsType_CRS()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='CRS'"
	 * @generated
	 */
	String getCRS();

	/**
	 * Sets the value of the '{@link net.opengis.wps.CRSsType#getCRS <em>CRS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>CRS</em>' attribute.
	 * @see #getCRS()
	 * @generated
	 */
	void setCRS(String value);

} // CRSsType
