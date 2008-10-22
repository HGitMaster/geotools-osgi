/**
 * <copyright>
 * </copyright>
 *
 * $Id: ComplexDataType.java 30810 2008-06-25 17:29:43Z jdeolive $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Complex Data Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Complex data (such as an image), including a definition of the complex value data structure (i.e., schema, format, and encoding).  May be an ows:Manifest data structure.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ComplexDataType#getMixed <em>Mixed</em>}</li>
 *   <li>{@link net.opengis.wps.ComplexDataType#getEncoding <em>Encoding</em>}</li>
 *   <li>{@link net.opengis.wps.ComplexDataType#getMimeType <em>Mime Type</em>}</li>
 *   <li>{@link net.opengis.wps.ComplexDataType#getSchema <em>Schema</em>}</li>
 *   <li>{@link net.opengis.wps.ComplexDataType#getData <em>Data</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getComplexDataType()
 * @model extendedMetaData="name='ComplexDataType' kind='mixed'"
 * @generated
 */
public interface ComplexDataType extends EObject {
    /**
     * The contents of the data type.
     * 
     * @model type="java.lang.Object"
     */
    EList getData();
    
	/**
     * Returns the value of the '<em><b>Mixed</b></em>' attribute list.
     * The list contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
     * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mixed</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
     * @return the value of the '<em>Mixed</em>' attribute list.
     * @see net.opengis.wps.WpsPackage#getComplexDataType_Mixed()
     * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry" many="true"
     *        extendedMetaData="kind='elementWildcard' name=':mixed'"
     * @generated
     */
	FeatureMap getMixed();

	/**
     * Returns the value of the '<em><b>Encoding</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * The encoding of this input or requested for this output (e.g., UTF-8). This "encoding" shall be included whenever the encoding required is not the default encoding indicated in the Process full description. When included, this encoding shall be one published for this output or input in the Process full description.
     * <!-- end-model-doc -->
     * @return the value of the '<em>Encoding</em>' attribute.
     * @see #setEncoding(String)
     * @see net.opengis.wps.WpsPackage#getComplexDataType_Encoding()
     * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
     *        extendedMetaData="kind='attribute' name='encoding'"
     * @generated
     */
	String getEncoding();

	/**
     * Sets the value of the '{@link net.opengis.wps.ComplexDataType#getEncoding <em>Encoding</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @param value the new value of the '<em>Encoding</em>' attribute.
     * @see #getEncoding()
     * @generated
     */
	void setEncoding(String value);

	/**
     * Returns the value of the '<em><b>Mime Type</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * The Format of this input or requested for this output (e.g., text/xml). This element shall be omitted when the Format is indicated in the http header of the output. When included, this format shall be one published for this output or input in the Process full description.
     * <!-- end-model-doc -->
     * @return the value of the '<em>Mime Type</em>' attribute.
     * @see #setMimeType(String)
     * @see net.opengis.wps.WpsPackage#getComplexDataType_MimeType()
     * @model dataType="net.opengis.ows11.MimeType"
     *        extendedMetaData="kind='attribute' name='mimeType'"
     * @generated
     */
	String getMimeType();

	/**
     * Sets the value of the '{@link net.opengis.wps.ComplexDataType#getMimeType <em>Mime Type</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @param value the new value of the '<em>Mime Type</em>' attribute.
     * @see #getMimeType()
     * @generated
     */
	void setMimeType(String value);

	/**
     * Returns the value of the '<em><b>Schema</b></em>' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Web-accessible XML Schema Document that defines the content model of this complex resource (e.g., encoded using GML 2.2 Application Schema).  This reference should be included for XML encoded complex resources to facilitate validation.
     * PS I changed the name of this attribute to be consistent with the ProcessDescription.  The original was giving me validation troubles in XMLSpy.
     * <!-- end-model-doc -->
     * @return the value of the '<em>Schema</em>' attribute.
     * @see #setSchema(String)
     * @see net.opengis.wps.WpsPackage#getComplexDataType_Schema()
     * @model dataType="org.eclipse.emf.ecore.xml.type.AnyURI"
     *        extendedMetaData="kind='attribute' name='schema'"
     * @generated
     */
	String getSchema();

	/**
     * Sets the value of the '{@link net.opengis.wps.ComplexDataType#getSchema <em>Schema</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @param value the new value of the '<em>Schema</em>' attribute.
     * @see #getSchema()
     * @generated
     */
	void setSchema(String value);

} // ComplexDataType
