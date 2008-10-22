/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 */
package org.geotools.arcsde.data.view;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.geotools.arcsde.pool.ISession;

/**
 * Fully qualifies a table names.
 * <p>
 * {@link net.sf.jsqlparser.schema.Table} has provitions only to store schema and table names, in
 * the traditional sense. ArcSDE uses fully qualified names formed by
 * "databaseName"."userName"."tableName". Though "databaseName" is optional in some ArcSDE systems
 * (sql server, for example), it is required in Oracle. Schema and table stands for user and table
 * in sde land. So this visitor will create new Tables where schema if formed by SDE's
 * "databaseName"."userName"
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: FromItemQualifier.java 30722 2008-06-13 18:15:42Z acuster $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/view/FromItemQualifier.java $
 * @since 2.3.x
 */
class FromItemQualifier implements FromItemVisitor {
    /** DOCUMENT ME! */
    private ISession session;

    /** DOCUMENT ME! */
    private FromItem qualifiedFromItem;

    /**
     * Creates a new FromItemQualifier object.
     * 
     * @param session DOCUMENT ME!
     * @throws IllegalStateException DOCUMENT ME!
     */
    private FromItemQualifier(ISession session) throws IllegalStateException {
        this.session = session;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param session DOCUMENT ME!
     * @param fromItem DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static FromItem qualify(ISession session, FromItem fromItem) {
        if (fromItem == null) {
            return null;
        }

        FromItemQualifier qualifier = new FromItemQualifier(session);
        fromItem.accept(qualifier);

        return qualifier.qualifiedFromItem;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param tableName DOCUMENT ME!
     */
    public void visit(Table tableName) {
        qualifiedFromItem = TableQualifier.qualify(session, tableName);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param subSelect DOCUMENT ME!
     */
    public void visit(SubSelect subSelect) {
        this.qualifiedFromItem = SubSelectQualifier.qualify(session, subSelect);
    }
}
