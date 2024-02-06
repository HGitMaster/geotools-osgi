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

package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.DD;

public class CGAlgorithmsDD {
    private static final double DP_SAFE_EPSILON = 1.0E-15D;

    public CGAlgorithmsDD() {
    }

    public static int orientationIndex(Coordinate p1, Coordinate p2, Coordinate q) {
        int index = orientationIndexFilter(p1, p2, q);
        if (index <= 1) {
            return index;
        } else {
            DD dx1 = DD.valueOf(p2.x).selfAdd(-p1.x);
            DD dy1 = DD.valueOf(p2.y).selfAdd(-p1.y);
            DD dx2 = DD.valueOf(q.x).selfAdd(-p2.x);
            DD dy2 = DD.valueOf(q.y).selfAdd(-p2.y);
            return dx1.selfMultiply(dy2).selfSubtract(dy1.selfMultiply(dx2)).signum();
        }
    }

    public static int signOfDet2x2(DD x1, DD y1, DD x2, DD y2) {
        DD det = x1.multiply(y2).selfSubtract(y1.multiply(x2));
        return det.signum();
    }

    private static int orientationIndexFilter(Coordinate pa, Coordinate pb, Coordinate pc) {
        double detleft = (pa.x - pc.x) * (pb.y - pc.y);
        double detright = (pa.y - pc.y) * (pb.x - pc.x);
        double det = detleft - detright;
        double detsum;
        if (detleft > 0.0D) {
            if (detright <= 0.0D) {
                return signum(det);
            }

            detsum = detleft + detright;
        } else {
            if (!(detleft < 0.0D)) {
                return signum(det);
            }

            if (detright >= 0.0D) {
                return signum(det);
            }

            detsum = -detleft - detright;
        }

        double errbound = 1.0E-15D * detsum;
        return !(det >= errbound) && !(-det >= errbound) ? 2 : signum(det);
    }

    private static int signum(double x) {
        if (x > 0.0D) {
            return 1;
        } else {
            return x < 0.0D ? -1 : 0;
        }
    }

    public static Coordinate intersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
        DD denom1 = DD.valueOf(q2.y).selfSubtract(q1.y).selfMultiply(DD.valueOf(p2.x).selfSubtract(p1.x));
        DD denom2 = DD.valueOf(q2.x).selfSubtract(q1.x).selfMultiply(DD.valueOf(p2.y).selfSubtract(p1.y));
        DD denom = denom1.subtract(denom2);
        DD numx1 = DD.valueOf(q2.x).selfSubtract(q1.x).selfMultiply(DD.valueOf(p1.y).selfSubtract(q1.y));
        DD numx2 = DD.valueOf(q2.y).selfSubtract(q1.y).selfMultiply(DD.valueOf(p1.x).selfSubtract(q1.x));
        DD numx = numx1.subtract(numx2);
        double fracP = numx.selfDivide(denom).doubleValue();
        double x = DD.valueOf(p1.x).selfAdd(DD.valueOf(p2.x).selfSubtract(p1.x).selfMultiply(fracP)).doubleValue();
        DD numy1 = DD.valueOf(p2.x).selfSubtract(p1.x).selfMultiply(DD.valueOf(p1.y).selfSubtract(q1.y));
        DD numy2 = DD.valueOf(p2.y).selfSubtract(p1.y).selfMultiply(DD.valueOf(p1.x).selfSubtract(q1.x));
        DD numy = numy1.subtract(numy2);
        double fracQ = numy.selfDivide(denom).doubleValue();
        double y = DD.valueOf(q1.y).selfAdd(DD.valueOf(q2.y).selfSubtract(q1.y).selfMultiply(fracQ)).doubleValue();
        return new Coordinate(x, y);
    }
}

