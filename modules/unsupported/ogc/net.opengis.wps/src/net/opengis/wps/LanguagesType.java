/**
 * <copyright>
 * </copyright>
 *
 * $Id: LanguagesType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Languages Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Identifies a list of languages supported by this service.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.LanguagesType#getLanguage <em>Language</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getLanguagesType()
 * @model extendedMetaData="name='LanguagesType' kind='elementOnly'"
 * @generated
 */
public interface LanguagesType extends EObject {
	/**
	 * Returns the value of the '<em><b>Language</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Identifier of a language supported by the service.  This language identifier shall be as specified in IETF RFC 4646.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Language</em>' attribute.
	 * @see #setLanguage(String)
	 * @see net.opengis.wps.WpsPackage#getLanguagesType_Language()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.Language" required="true"
	 *        extendedMetaData="kind='element' name='Language' namespace='http://www.opengis.net/ows/1.1'"
	 * @generated
	 */
	String getLanguage();

	/**
	 * Sets the value of the '{@link net.opengis.wps.LanguagesType#getLanguage <em>Language</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Language</em>' attribute.
	 * @see #getLanguage()
	 * @generated
	 */
	void setLanguage(String value);

} // LanguagesType
