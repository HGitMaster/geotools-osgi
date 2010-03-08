/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gce.imagemosaic.jdbc;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geotools.data.jdbc.datasource.DBCPDataSourceFactory;
import org.geotools.data.jdbc.datasource.JNDIDataSourceFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class for holding the config info read from the xml config file
 * 
 * @author mcr
 * 
 */
class Config {
	static private Map<String, Config> ConfigMap = new Hashtable<String, Config>(); // Hashtable

	// is
	// synchronized
	private String xmlUrl;

	private String coverageName;

	private String coordsys;

	private SpatialExtension spatialExtension;

	private String dstype;

	private String username;

	private String password;

	private String jdbcUrl;

	private String driverClassName;

	private Integer maxActive;

	private Integer maxIdle;

	private String jndiReferenceName;

	private String coverageNameAttribute;

	private String blobAttributeNameInTileTable;

	private String keyAttributeNameInTileTable;

	private String keyAttributeNameInSpatialTable;

	private String geomAttributeNameInSpatialTable;

	private String maxXAttribute;

	private String maxYAttribute;

	private String minXAttribute;

	private String minYAttribute;

	private String masterTable;

	private String resXAttribute;

	private String resYAttribute;

	private String tileTableNameAtribute;

	private String spatialTableNameAtribute;

	private String sqlUpdateMosaicStatement;

	private String sqlSelectCoverageStatement;

	private String sqlUpdateResStatement;

	private Boolean verifyCardinality;

	private Integer interpolation;

	private String tileMaxXAttribute;

	private String tileMaxYAttribute;

	private String tileMinXAttribute;

	private String tileMinYAttribute;

	protected Config() {
	}

	static Config readFrom(URL xmlURL) throws Exception {
		Config result = ConfigMap.get(xmlURL.toString());

		if (result != null) {
			return result;
		}

		InputStream in = xmlURL.openStream();
		InputSource input = new InputSource(xmlURL.toString());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);

		DocumentBuilder db = dbf.newDocumentBuilder();

		// db.setEntityResolver(new ConfigEntityResolver(xmlURL));
		Document dom = db.parse(input);
		in.close();

		result = new Config();

		result.xmlUrl = xmlURL.toString();

		result.dstype = readValueString(dom, "dstype");
		result.username = readValueString(dom, "username");
		result.password = readValueString(dom, "password");
		result.jdbcUrl = readValueString(dom, "jdbcUrl");
		result.driverClassName = readValueString(dom, "driverClassName");
		result.jndiReferenceName = readValueString(dom, "jndiReferenceName");
		result.maxActive = readValueInteger(dom, "maxActive");
		result.maxIdle = readValueInteger(dom, "maxIdle");

		result.coordsys = readNameString(dom.getDocumentElement(), "coordsys");
		result.coverageName = readNameString(dom.getDocumentElement(),
				"coverageName");

		// db mapping
		result.spatialExtension = SpatialExtension.fromString(readNameString(
				dom.getDocumentElement(), "spatialExtension"));
		result.masterTable = readNameString(dom.getDocumentElement(),
				"masterTable");

		Element masterTableElem = (Element) dom.getElementsByTagName(
				"masterTable").item(0);
		result.coverageNameAttribute = readNameString(masterTableElem,
				"coverageNameAttribute");
		result.maxXAttribute = readNameString(masterTableElem, "maxXAttribute");
		result.maxYAttribute = readNameString(masterTableElem, "maxYAttribute");
		result.minXAttribute = readNameString(masterTableElem, "minXAttribute");
		result.minYAttribute = readNameString(masterTableElem, "minYAttribute");
		result.resXAttribute = readNameString(masterTableElem, "resXAttribute");
		result.resYAttribute = readNameString(masterTableElem, "resYAttribute");
		result.tileTableNameAtribute = readNameString(masterTableElem,
				"tileTableNameAtribute");
		result.spatialTableNameAtribute = readNameString(masterTableElem,
				"spatialTableNameAtribute");

		Element tileTableElem = (Element) dom.getElementsByTagName("tileTable")
				.item(0);
		result.blobAttributeNameInTileTable = readNameString(tileTableElem,
				"blobAttributeName");
		result.keyAttributeNameInTileTable = readNameString(tileTableElem,
				"keyAttributeName");

		Element spatialTableElem = (Element) dom.getElementsByTagName(
				"spatialTable").item(0);
		result.keyAttributeNameInSpatialTable = readNameString(
				spatialTableElem, "keyAttributeName");
		result.geomAttributeNameInSpatialTable = readNameString(
				spatialTableElem, "geomAttributeName");
		result.tileMaxXAttribute = readNameString(spatialTableElem,
				"tileMaxXAttribute");
		result.tileMaxYAttribute = readNameString(spatialTableElem,
				"tileMaxYAttribute");
		result.tileMinXAttribute = readNameString(spatialTableElem,
				"tileMinXAttribute");
		result.tileMinYAttribute = readNameString(spatialTableElem,
				"tileMinYAttribute");

		// end db mapping
		Node tmp = dom.getElementsByTagName("scaleop").item(0);
		NamedNodeMap map = tmp.getAttributes();
		String s = map.getNamedItem("interpolation").getNodeValue();
		result.interpolation = new Integer(s);

		tmp = dom.getElementsByTagName("verify").item(0);
		map = tmp.getAttributes();
		s = map.getNamedItem("cardinality").getNodeValue();
		result.verifyCardinality = new Boolean(s);

		result.initStatements();

		ConfigMap.put(xmlURL.toString(), result);

		return result;
	}

	private void initStatements() {
		StringBuffer buff = null;
		buff = new StringBuffer("update ").append(masterTable).append(" set ");
		buff.append(maxXAttribute).append(" = ?,");
		buff.append(maxYAttribute).append(" = ?,");
		buff.append(minXAttribute).append(" = ?,");
		buff.append(minYAttribute).append(" = ?");
		buff.append(" where ").append(coverageNameAttribute).append(" = ? ");
		buff.append(" and ").append(tileTableNameAtribute).append(" = ? ");
		buff.append(" and ").append(spatialTableNameAtribute).append(" = ? ");
		sqlUpdateMosaicStatement = buff.toString();

		buff = new StringBuffer("select * from ").append(masterTable).append(
				" where ").append(coverageNameAttribute).append(" = ? ");
		sqlSelectCoverageStatement = buff.toString();

		buff = new StringBuffer("update ").append(masterTable).append(" set ");
		buff.append(resXAttribute).append(" = ?,");
		buff.append(resYAttribute).append(" = ? ");
		buff.append(" where ").append(coverageNameAttribute).append(" = ? ");
		buff.append(" and ").append(tileTableNameAtribute).append(" = ? ");
		buff.append(" and ").append(spatialTableNameAtribute).append(" = ? ");
		sqlUpdateResStatement = buff.toString();
	}

	static private String readValueString(Document dom, String elemName) {
		Node n = readValueAttribute(dom, elemName);

		if (n == null) {
			return null;
		}

		return n.getNodeValue();
	}

	static private String readNameString(Element elem, String elemName) {
		Node n = readNameAttribute(elem, elemName);

		if (n == null) {
			return null;
		}

		return n.getNodeValue();
	}

	static private Integer readValueInteger(Document dom, String elemName) {
		Node n = readValueAttribute(dom, elemName);

		if (n == null) {
			return null;
		}

		return new Integer(n.getNodeValue());
	}

	static private Node readValueAttribute(Document dom, String elemName) {
		NodeList list = dom.getElementsByTagName(elemName);
		Node n = list.item(0);

		if (n == null) {
			return null;
		}

		return n.getAttributes().getNamedItem("value");
	}

	static private Node readNameAttribute(Element elem, String elemName) {
		NodeList list = elem.getElementsByTagName(elemName);
		Node n = list.item(0);

		if (n == null) {
			return null;
		}

		return n.getAttributes().getNamedItem("name");
	}

	Map<String, Object> getDataSourceParams() {
		Map<String, Object> result = new HashMap<String, Object>();

		if ("DBCP".equals(dstype)) {
			result.put(DBCPDataSourceFactory.DSTYPE.key, dstype);
			result.put(DBCPDataSourceFactory.USERNAME.key, username);
			result.put(DBCPDataSourceFactory.PASSWORD.key, password);
			result.put(DBCPDataSourceFactory.JDBC_URL.key, jdbcUrl);
			result.put(DBCPDataSourceFactory.DRIVERCLASS.key, driverClassName);
			result.put(DBCPDataSourceFactory.MAXACTIVE.key, maxActive);
			result.put(DBCPDataSourceFactory.MAXIDLE.key, maxIdle);
		}

		if ("JNDI".equals(dstype)) {
			result.put(JNDIDataSourceFactory.DSTYPE.key, dstype);
			result.put(JNDIDataSourceFactory.JNDI_REFNAME.key,
					jndiReferenceName);
		}

		return result;
	}

	String getBlobAttributeNameInTileTable() {
		return blobAttributeNameInTileTable;
	}

	String getKeyAttributeNameInSpatialTable() {
		return keyAttributeNameInSpatialTable;
	}

	String getKeyAttributeNameInTileTable() {
		return keyAttributeNameInTileTable;
	}

	String getJdbcUrl() {
		return jdbcUrl;
	}

	String getJndiReferenceName() {
		return jndiReferenceName;
	}

	String getSqlUpdateMosaicStatement() {
		return sqlUpdateMosaicStatement;
	}

	String getSqlSelectCoverageStatement() {
		return sqlSelectCoverageStatement;
	}

	String getSpatialTableNameAtribute() {
		return spatialTableNameAtribute;
	}

	String getTileTableNameAtribute() {
		return tileTableNameAtribute;
	}

	String getSqlUpdateResStatement() {
		return sqlUpdateResStatement;
	}

	// String getTileTableSelectString(String tileTableName) {
	// StringBuffer buff = new StringBuffer ("select
	// ").append(blobAttributeNameInTileTable ).append(" from ")
	// .append(tileTableName).append(" where ")
	// .append(keyAttributeNameInTileTable).append( " = ? ");
	// return buff.toString();
	// }
	String getMaxXAttribute() {
		return maxXAttribute;
	}

	String getMaxYAttribute() {
		return maxYAttribute;
	}

	String getMinXAttribute() {
		return minXAttribute;
	}

	String getMinYAttribute() {
		return minYAttribute;
	}

	String getResXAttribute() {
		return resXAttribute;
	}

	String getResYAttribute() {
		return resYAttribute;
	}

	String getCoordsys() {
		return coordsys;
	}

	String getCoverageName() {
		return coverageName;
	}

	Integer getInterpolation() {
		return interpolation;
	}

	String getXmlUrl() {
		return xmlUrl;
	}

	Boolean getVerifyCardinality() {
		return verifyCardinality;
	}

	String getDriverClassName() {
		return driverClassName;
	}

	String getMasterTable() {
		return masterTable;
	}

	String getCoverageNameAttribute() {
		return coverageNameAttribute;
	}

	String getPassword() {
		return password;
	}

	String getUsername() {
		return username;
	}

	String getTileMaxXAttribute() {
		return tileMaxXAttribute;
	}

	String getTileMaxYAttribute() {
		return tileMaxYAttribute;
	}

	String getTileMinXAttribute() {
		return tileMinXAttribute;
	}

	String getTileMinYAttribute() {
		return tileMinYAttribute;
	}

	String getGeomAttributeNameInSpatialTable() {
		return geomAttributeNameInSpatialTable;
	}

	SpatialExtension getSpatialExtension() {
		return spatialExtension;
	}
}
