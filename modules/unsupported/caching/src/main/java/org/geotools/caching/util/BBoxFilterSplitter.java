/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.geotools.filter.FilterFactoryImpl;


/** The purpose of this class is to split any Filter into two filters :
 *  <ol><ul> a SpatialRestriction
 *      <ul> and an OtherAttributesRestriction
 *  <ol>
 *  so we have :
 *  OriginalFilter = SpatialRestriction && OtherAttributeRestriction
 *
 *  SpatialRestriction may actually be a rough approximation of OtherAttributeRestriction
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class BBoxFilterSplitter implements FilterVisitor {
    private Stack envelopes = new Stack();
    private Stack otherRestrictions = new Stack();
    private String geom = null;
    private String srs = null;

    //private Stack notEnvelopes = new Stack() ;
    public Object visit(ExcludeFilter arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(IncludeFilter arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(And f, Object arg1) {
        int envSize = envelopes.size();
        int othSize = otherRestrictions.size();

        for (Iterator it = f.getChildren().iterator(); it.hasNext();) {
            Filter child = (Filter) it.next();
            child.accept(this, arg1);
        }

        if (envelopes.size() >= (envSize + 2)) {
            Envelope e = (Envelope) envelopes.pop();

            for (int i = envelopes.size(); i > envSize; i--) {
                e = e.intersection((Envelope) envelopes.pop());
            }

            envelopes.push(e);
        }

        if (otherRestrictions.size() >= (othSize + 2)) {
            List pops = new ArrayList();

            for (int i = otherRestrictions.size(); i > othSize; i--) {
                pops.add((Filter) otherRestrictions.pop());
            }

            otherRestrictions.push(new FilterFactoryImpl().and(pops));
        }

        return null;
    }

    public Object visit(Id f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Not f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Or f, Object arg1) {
        int envSize = envelopes.size();
        int othSize = otherRestrictions.size();

        for (Iterator it = f.getChildren().iterator(); it.hasNext();) {
            Filter child = (Filter) it.next();
            child.accept(this, arg1);
        }

        if (envelopes.size() > (envSize + 1)) {
            Envelope e = (Envelope) envelopes.pop();

            for (int i = envelopes.size(); i > envSize; i--) {
                e.expandToInclude((Envelope) envelopes.pop());
            }

            envelopes.push(e);
        } else if (envelopes.size() == (envSize + 1)) {
            // the trick is we cannot separate this filter in the form of SpatialRestriction && OtherRestriction
            // so we add this part to OtherRestriction
            envelopes.pop();
        }

        // in all case, we'll need original filter as computed SpatialRestriction is a rough approximation
        multiplePop(otherRestrictions, othSize);
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsBetween f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsEqualTo f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsNotEqualTo f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsGreaterThan f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsGreaterThanOrEqualTo f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsLessThan f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsLessThanOrEqualTo f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsLike f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(PropertyIsNull f, Object arg1) {
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(BBOX f, Object arg1) {
        if (geom == null) {
            geom = f.getPropertyName();
            srs = f.getSRS();
        } else if ((geom != f.getPropertyName()) || (srs != f.getSRS())) {
            throw new UnsupportedOperationException(
                "This splitter can not be used against a filter where different BBOX filters refer to different Geometry attributes.");
        }

        Envelope e = new Envelope(f.getMinX(), f.getMaxX(), f.getMinY(), f.getMaxY());
        envelopes.push(e);

        return null;
    }

    public Object visit(Beyond f, Object arg1) {
        // we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Contains f, Object arg1) {
        // we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Crosses f, Object arg1) {
        // we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Disjoint f, Object arg1) {
        // we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(DWithin f, Object arg1) {
        // we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    public Object visit(Equals f, Object arg1) {
        //		 we don't know how to handle this geometric restriction as a BBox
        // so we treat this as an attribute filter
        otherRestrictions.push(f);

        return null;
    }

    protected void traverse(BinarySpatialOperator f) {
        if (f.getExpression1() instanceof Literal) {
            Literal l = (Literal) f.getExpression1();
            Geometry g = (Geometry) l.getValue();
            envelopes.push(g.getEnvelopeInternal());
        } else if (f.getExpression2() instanceof Literal) {
            Literal l = (Literal) f.getExpression2();
            Geometry g = (Geometry) l.getValue();
            envelopes.push(g.getEnvelopeInternal());
        }

        otherRestrictions.push(f);
    }

    public Object visit(Intersects f, Object arg1) {
        traverse(f);

        return null;
    }

    public Object visit(Overlaps f, Object arg1) {
        traverse(f);

        return null;
    }

    public Object visit(Touches f, Object arg1) {
        traverse(f);

        return null;
    }

    public Object visit(Within f, Object arg1) {
        traverse(f);

        return null;
    }

    public Object visitNullFilter(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Envelope getEnvelope() {
        assert (envelopes.size() < 2);

        if (envelopes.isEmpty()) {
            return null;
        } else {
            return (Envelope) envelopes.peek();
        }
    }

    /** Return the bbox part of original filter :
     *  filter == (1) AND (2), where
     *  (1) = BBOXImpl
     *  (2) = other filter
     *
     * @return filter part (1)
     */
    public Filter getFilterPre() {
        Envelope e = getEnvelope();

        if (e == null) {
            return Filter.INCLUDE;
        } else {
            return new FilterFactoryImpl().bbox(geom, e.getMinX(), e.getMinY(), e.getMaxX(),
                e.getMaxY(), srs);
        }
    }

    /** Return the non bbox part (2) of original filter :
     *  filter == (1) AND (2), where
     *  (1) = BBOXImpl
     *  (2) = other filter
     *
     * @return filter part (2)
     */
    public Filter getFilterPost() {
        if (otherRestrictions.isEmpty()) {
            return Filter.INCLUDE;
        } else if (otherRestrictions.size() == 1) {
            return (Filter) otherRestrictions.peek();
        } else {
            return new FilterFactoryImpl().and(otherRestrictions.subList(0,
                    otherRestrictions.size() - 1));
        }
    }

    private void multiplePop(Stack s, int downsize) {
        for (int i = s.size(); i > downsize; i--) {
            s.pop();
        }
    }
}
