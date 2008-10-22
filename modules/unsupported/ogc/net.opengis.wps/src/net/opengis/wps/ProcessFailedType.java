/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProcessFailedType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import net.opengis.ows11.ExceptionReportType;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Failed Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Indicator that the process has failed to execute successfully. The reason for failure is given in the exception report.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ProcessFailedType#getExceptionReport <em>Exception Report</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getProcessFailedType()
 * @model extendedMetaData="name='ProcessFailedType' kind='elementOnly'"
 * @generated
 */
public interface ProcessFailedType extends EObject {
	/**
	 * Returns the value of the '<em><b>Exception Report</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Report message returned to the client that requested any OWS operation when the server detects an error while processing that operation request.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Exception Report</em>' containment reference.
	 * @see #setExceptionReport(ExceptionReportType)
	 * @see net.opengis.wps.WpsPackage#getProcessFailedType_ExceptionReport()
	 * @model containment="true" required="true"
	 *        extendedMetaData="kind='element' name='ExceptionReport' namespace='http://www.opengis.net/ows/1.1'"
	 * @generated
	 */
	ExceptionReportType getExceptionReport();

	/**
	 * Sets the value of the '{@link net.opengis.wps.ProcessFailedType#getExceptionReport <em>Exception Report</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Exception Report</em>' containment reference.
	 * @see #getExceptionReport()
	 * @generated
	 */
	void setExceptionReport(ExceptionReportType value);

} // ProcessFailedType
