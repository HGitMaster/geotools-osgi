/*
 * Back ported from JTS 1.14
 *
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.operation.buffer;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class SubgraphDepthLocater
{
    private Collection subgraphs;

    private LineSegment seg = new LineSegment();

    private CGAlgorithms cga = new CGAlgorithms();

    public SubgraphDepthLocater(List subgraphs)
    {
        this.subgraphs = subgraphs;
    }

    public int getDepth(Coordinate p)
    {
        List stabbedSegments = this.findStabbedSegments(p);
        if (stabbedSegments.size() == 0)
        {
            return 0;
        }
        else
        {
            DepthSegment ds = (DepthSegment) Collections.min(stabbedSegments);
            return ds.leftDepth;
        }
    }

    private List findStabbedSegments(Coordinate stabbingRayLeftPt)
    {
        List stabbedSegments = new ArrayList();
        Iterator i = this.subgraphs.iterator();

        while (i.hasNext())
        {
            BufferSubgraph bsg = (BufferSubgraph) i.next();
            Envelope env = bsg.getEnvelope();
            if (!(stabbingRayLeftPt.y < env.getMinY())
                    && !(stabbingRayLeftPt.y > env.getMaxY()))
            {
                this.findStabbedSegments(stabbingRayLeftPt,
                        (List) bsg.getDirectedEdges(), stabbedSegments);
            }
        }

        return stabbedSegments;
    }

    private void findStabbedSegments(Coordinate stabbingRayLeftPt,
            List dirEdges, List stabbedSegments)
    {
        Iterator i = dirEdges.iterator();

        while (i.hasNext())
        {
            DirectedEdge de = (DirectedEdge) i.next();
            if (de.isForward())
            {
                this.findStabbedSegments(stabbingRayLeftPt, de,
                        stabbedSegments);
            }
        }

    }

    private void findStabbedSegments(Coordinate stabbingRayLeftPt,
            DirectedEdge dirEdge, List stabbedSegments)
    {
        Coordinate[] pts = dirEdge.getEdge().getCoordinates();

        for (int i = 0; i < pts.length - 1; ++i)
        {
            this.seg.p0 = pts[i];
            this.seg.p1 = pts[i + 1];
            if (this.seg.p0.y > this.seg.p1.y)
            {
                this.seg.reverse();
            }

            double maxx = Math.max(this.seg.p0.x, this.seg.p1.x);
            if (!(maxx < stabbingRayLeftPt.x) && !this.seg.isHorizontal()
                    && !(stabbingRayLeftPt.y < this.seg.p0.y)
                    && !(stabbingRayLeftPt.y > this.seg.p1.y)
                    && CGAlgorithms.computeOrientation(this.seg.p0, this.seg.p1, stabbingRayLeftPt) != -1)
            {
                int depth = dirEdge.getDepth(1);
                if (!this.seg.p0.equals(pts[i]))
                {
                    depth = dirEdge.getDepth(2);
                }

                DepthSegment ds = new DepthSegment(this.seg, depth);
                stabbedSegments.add(ds);
            }
        }

    }

    static class DepthSegment implements Comparable
    {
        private LineSegment upwardSeg;

        private int leftDepth;

        public DepthSegment(LineSegment seg, int depth)
        {
            this.upwardSeg = new LineSegment(seg);
            this.leftDepth = depth;
        }

        public int compareTo(Object obj)
        {
            DepthSegment other = (DepthSegment) obj;
            if (this.upwardSeg.minX() >= other.upwardSeg.maxX())
            {
                return 1;
            }
            else if (this.upwardSeg.maxX() <= other.upwardSeg.minX())
            {
                return -1;
            }
            else
            {
                int orientIndex = this.upwardSeg
                        .orientationIndex(other.upwardSeg);
                if (orientIndex != 0)
                {
                    return orientIndex;
                }
                else
                {
                    orientIndex = -1 * other.upwardSeg.orientationIndex(this.upwardSeg);
                    return orientIndex != 0 ? orientIndex : this.upwardSeg.compareTo(other.upwardSeg);
                }
            }
        }

        private int compareX(LineSegment seg0, LineSegment seg1)
        {
            int compare0 = seg0.p0.compareTo(seg1.p0);
            return compare0 != 0 ? compare0 : seg0.p1.compareTo(seg1.p1);
        }

        public String toString()
        {
            return this.upwardSeg.toString();
        }
    }
}

