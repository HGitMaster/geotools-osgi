/**
 * <copyright>
 * </copyright>
 *
 * $Id: DefaultType.java 29861 2008-04-09 04:55:54Z jdeolive $
 */
package net.opengis.wps;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Default Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.DefaultType#getCRS <em>CRS</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getDefaultType()
 * @model extendedMetaData="name='Default_._1_._type' kind='elementOnly'"
 * @generated
 */
public interface DefaultType extends EObject {
	/**
	 * Returns the value of the '<em><b>CRS</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the default CRS supported for this Input/Output
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>CRS</em>' attribute.
	 * @see #setCRS(String)
	 * @see net.opengis.wps.WpsPackage#getDefaultType_CRS()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI" required="true"
	 *        extendedMetaData="kind='element' name='CRS'"
	 * @generated
	 */
	String getCRS();

	/**
	 * Sets the value of the '{@link net.opengis.wps.DefaultType#getCRS <em>CRS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>CRS</em>' attribute.
	 * @see #getCRS()
	 * @generated
	 */
	void setCRS(String value);

} // DefaultType
