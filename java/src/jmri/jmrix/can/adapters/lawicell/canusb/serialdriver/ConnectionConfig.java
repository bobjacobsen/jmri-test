// ConnectionConfig.java

package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection
 * via a Canusb CanUsbDriverAdapter object.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2008
 * @author      Andrew Crosland 2008
 * @version	    $Revision$
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification="name assigned historically")
public class ConnectionConfig extends jmri.jmrix.can.adapters.ConnectionConfig {

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        super(p);
    }

    // Needed for instantiation by reflection, do not remove.
    public ConnectionConfig() {
        super();
    }

    public String name() { return "CAN via Lawicell CANUSB"; }
    
    protected void setInstance() { 
        if(adapter ==null){
            adapter = new CanUsbDriverAdapter();
        }
    }
}

