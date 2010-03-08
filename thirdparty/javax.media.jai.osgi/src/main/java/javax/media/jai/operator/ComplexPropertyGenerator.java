/*
 * $RCSfile: ComplexPropertyGenerator.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:57:31 $
 * $State: Exp $
 */
package javax.media.jai.operator;

import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import com.sun.media.jai.util.PropertyGeneratorImpl;

/**
 * This property generator returns <code>Boolean.TRUE</code> for the
 * "COMPLEX" property for the rendered and renderable modes.
 */
class ComplexPropertyGenerator extends PropertyGeneratorImpl {

    /** Constructor. */
    public ComplexPropertyGenerator() {
        super(new String[] {"COMPLEX"},
              new Class[] {Boolean.class},
              new Class[] {RenderedOp.class, RenderableOp.class});
    }

    /**
     * Returns the specified property.
     *
     * @param name  Property name.
     * @param op Operation node.
     */
    public Object getProperty(String name,
                              Object op) {
        validate(name, op);

        return name.equalsIgnoreCase("complex") ?
            Boolean.TRUE : java.awt.Image.UndefinedProperty;
    }
}
