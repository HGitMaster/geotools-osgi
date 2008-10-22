/**
 * <copyright>
 * </copyright>
 *
 * $Id: InputReferenceTypeImpl.java 29861 2008-04-09 04:55:54Z jdeolive $
 */
package net.opengis.wps.impl;

import java.util.Collection;

import net.opengis.wps.BodyReferenceType;
import net.opengis.wps.HeaderType;
import net.opengis.wps.InputReferenceType;
import net.opengis.wps.MethodType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Input Reference Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getHeader <em>Header</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getBody <em>Body</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getBodyReference <em>Body Reference</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getEncoding <em>Encoding</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getHref <em>Href</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getMethod <em>Method</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getMimeType <em>Mime Type</em>}</li>
 *   <li>{@link net.opengis.wps.impl.InputReferenceTypeImpl#getSchema <em>Schema</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class InputReferenceTypeImpl extends EObjectImpl implements InputReferenceType {
	/**
	 * The cached value of the '{@link #getHeader() <em>Header</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeader()
	 * @generated
	 * @ordered
	 */
	protected EList header;

	/**
	 * The cached value of the '{@link #getBody() <em>Body</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBody()
	 * @generated
	 * @ordered
	 */
	protected EObject body;

	/**
	 * The cached value of the '{@link #getBodyReference() <em>Body Reference</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBodyReference()
	 * @generated
	 * @ordered
	 */
	protected BodyReferenceType bodyReference;

	/**
	 * The default value of the '{@link #getEncoding() <em>Encoding</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncoding()
	 * @generated
	 * @ordered
	 */
	protected static final String ENCODING_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEncoding() <em>Encoding</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEncoding()
	 * @generated
	 * @ordered
	 */
	protected String encoding = ENCODING_EDEFAULT;

	/**
	 * The default value of the '{@link #getHref() <em>Href</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHref()
	 * @generated
	 * @ordered
	 */
	protected static final String HREF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getHref() <em>Href</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHref()
	 * @generated
	 * @ordered
	 */
	protected String href = HREF_EDEFAULT;

	/**
	 * The default value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected static final MethodType METHOD_EDEFAULT = MethodType.GET_LITERAL;

	/**
	 * The cached value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected MethodType method = METHOD_EDEFAULT;

	/**
	 * This is true if the Method attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean methodESet;

	/**
	 * The default value of the '{@link #getMimeType() <em>Mime Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMimeType()
	 * @generated
	 * @ordered
	 */
	protected static final String MIME_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMimeType() <em>Mime Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMimeType()
	 * @generated
	 * @ordered
	 */
	protected String mimeType = MIME_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSchema() <em>Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected static final String SCHEMA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSchema() <em>Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSchema()
	 * @generated
	 * @ordered
	 */
	protected String schema = SCHEMA_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected InputReferenceTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getInputReferenceType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList getHeader() {
		if (header == null) {
			header = new EObjectContainmentEList(HeaderType.class, this, WpsPackage.INPUT_REFERENCE_TYPE__HEADER);
		}
		return header;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject getBody() {
		return body;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBody(EObject newBody, NotificationChain msgs) {
		EObject oldBody = body;
		body = newBody;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__BODY, oldBody, newBody);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBody(EObject newBody) {
		if (newBody != body) {
			NotificationChain msgs = null;
			if (body != null)
				msgs = ((InternalEObject)body).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.INPUT_REFERENCE_TYPE__BODY, null, msgs);
			if (newBody != null)
				msgs = ((InternalEObject)newBody).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.INPUT_REFERENCE_TYPE__BODY, null, msgs);
			msgs = basicSetBody(newBody, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__BODY, newBody, newBody));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BodyReferenceType getBodyReference() {
		return bodyReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBodyReference(BodyReferenceType newBodyReference, NotificationChain msgs) {
		BodyReferenceType oldBodyReference = bodyReference;
		bodyReference = newBodyReference;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE, oldBodyReference, newBodyReference);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBodyReference(BodyReferenceType newBodyReference) {
		if (newBodyReference != bodyReference) {
			NotificationChain msgs = null;
			if (bodyReference != null)
				msgs = ((InternalEObject)bodyReference).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE, null, msgs);
			if (newBodyReference != null)
				msgs = ((InternalEObject)newBodyReference).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE, null, msgs);
			msgs = basicSetBodyReference(newBodyReference, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE, newBodyReference, newBodyReference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEncoding(String newEncoding) {
		String oldEncoding = encoding;
		encoding = newEncoding;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__ENCODING, oldEncoding, encoding));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getHref() {
		return href;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHref(String newHref) {
		String oldHref = href;
		href = newHref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__HREF, oldHref, href));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MethodType getMethod() {
		return method;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMethod(MethodType newMethod) {
		MethodType oldMethod = method;
		method = newMethod == null ? METHOD_EDEFAULT : newMethod;
		boolean oldMethodESet = methodESet;
		methodESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__METHOD, oldMethod, method, !oldMethodESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void unsetMethod() {
		MethodType oldMethod = method;
		boolean oldMethodESet = methodESet;
		method = METHOD_EDEFAULT;
		methodESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, WpsPackage.INPUT_REFERENCE_TYPE__METHOD, oldMethod, METHOD_EDEFAULT, oldMethodESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSetMethod() {
		return methodESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMimeType(String newMimeType) {
		String oldMimeType = mimeType;
		mimeType = newMimeType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__MIME_TYPE, oldMimeType, mimeType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSchema(String newSchema) {
		String oldSchema = schema;
		schema = newSchema;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.INPUT_REFERENCE_TYPE__SCHEMA, oldSchema, schema));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WpsPackage.INPUT_REFERENCE_TYPE__HEADER:
				return ((InternalEList)getHeader()).basicRemove(otherEnd, msgs);
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY:
				return basicSetBody(null, msgs);
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE:
				return basicSetBodyReference(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case WpsPackage.INPUT_REFERENCE_TYPE__HEADER:
				return getHeader();
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY:
				return getBody();
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE:
				return getBodyReference();
			case WpsPackage.INPUT_REFERENCE_TYPE__ENCODING:
				return getEncoding();
			case WpsPackage.INPUT_REFERENCE_TYPE__HREF:
				return getHref();
			case WpsPackage.INPUT_REFERENCE_TYPE__METHOD:
				return getMethod();
			case WpsPackage.INPUT_REFERENCE_TYPE__MIME_TYPE:
				return getMimeType();
			case WpsPackage.INPUT_REFERENCE_TYPE__SCHEMA:
				return getSchema();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case WpsPackage.INPUT_REFERENCE_TYPE__HEADER:
				getHeader().clear();
				getHeader().addAll((Collection)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY:
				setBody((EObject)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE:
				setBodyReference((BodyReferenceType)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__ENCODING:
				setEncoding((String)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__HREF:
				setHref((String)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__METHOD:
				setMethod((MethodType)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__MIME_TYPE:
				setMimeType((String)newValue);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__SCHEMA:
				setSchema((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case WpsPackage.INPUT_REFERENCE_TYPE__HEADER:
				getHeader().clear();
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY:
				setBody((EObject)null);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE:
				setBodyReference((BodyReferenceType)null);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__ENCODING:
				setEncoding(ENCODING_EDEFAULT);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__HREF:
				setHref(HREF_EDEFAULT);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__METHOD:
				unsetMethod();
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__MIME_TYPE:
				setMimeType(MIME_TYPE_EDEFAULT);
				return;
			case WpsPackage.INPUT_REFERENCE_TYPE__SCHEMA:
				setSchema(SCHEMA_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case WpsPackage.INPUT_REFERENCE_TYPE__HEADER:
				return header != null && !header.isEmpty();
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY:
				return body != null;
			case WpsPackage.INPUT_REFERENCE_TYPE__BODY_REFERENCE:
				return bodyReference != null;
			case WpsPackage.INPUT_REFERENCE_TYPE__ENCODING:
				return ENCODING_EDEFAULT == null ? encoding != null : !ENCODING_EDEFAULT.equals(encoding);
			case WpsPackage.INPUT_REFERENCE_TYPE__HREF:
				return HREF_EDEFAULT == null ? href != null : !HREF_EDEFAULT.equals(href);
			case WpsPackage.INPUT_REFERENCE_TYPE__METHOD:
				return isSetMethod();
			case WpsPackage.INPUT_REFERENCE_TYPE__MIME_TYPE:
				return MIME_TYPE_EDEFAULT == null ? mimeType != null : !MIME_TYPE_EDEFAULT.equals(mimeType);
			case WpsPackage.INPUT_REFERENCE_TYPE__SCHEMA:
				return SCHEMA_EDEFAULT == null ? schema != null : !SCHEMA_EDEFAULT.equals(schema);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (encoding: ");
		result.append(encoding);
		result.append(", href: ");
		result.append(href);
		result.append(", method: ");
		if (methodESet) result.append(method); else result.append("<unset>");
		result.append(", mimeType: ");
		result.append(mimeType);
		result.append(", schema: ");
		result.append(schema);
		result.append(')');
		return result.toString();
	}

} //InputReferenceTypeImpl
