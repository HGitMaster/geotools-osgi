package org.geotools.data.wfs.protocol.wfs;

import java.io.IOException;

public class WFSException extends IOException {

    private StringBuilder msg;

    public WFSException( String msg ) {
        this(msg, null);
    }

    public WFSException( String msg, Throwable cause ) {
        super(msg, cause);
        this.msg = new StringBuilder();
        if (msg != null) {
            this.msg.append(msg);
        }
    }

    public void addExceptionReport( String report ) {
        msg.append("\n\t[").append(report).append("]");
    }

    @Override
    public String getMessage() {
        return msg.toString();
    }

    @Override
    public String getLocalizedMessage() {
        return msg.toString();
    }
}
