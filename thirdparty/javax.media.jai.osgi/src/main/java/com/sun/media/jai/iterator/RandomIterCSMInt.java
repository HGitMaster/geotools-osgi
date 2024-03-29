/*
 * $RCSfile: RandomIterCSMInt.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:42 $
 * $State: Exp $
 */
package com.sun.media.jai.iterator;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import javax.media.jai.iterator.RandomIter;

/**
 * @since EA2
 */
public class RandomIterCSMInt extends RandomIterCSM {

    public RandomIterCSMInt(RenderedImage im, Rectangle bounds) {
        super(im, bounds);
    }

    public final int getSample(int x, int y, int b) {
        return 0;
    }
}

