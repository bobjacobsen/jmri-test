// ConfigurationManager.java

package jmri.jmrix.can;

import java.util.ResourceBundle;

/**
 * Does configuration for various CAN-based communications
 * implementations.
 *<p>
 * It would be good to replace this with properties-based 
 * method for redirecting to classes in particular subpackages.
 *
 * @author		Bob Jacobsen  Copyright (C) 2009
 * @version     $Revision$
 */
abstract public class ConfigurationManager {

    final public static String MERGCBUS = "MERG CBUS";
    final public static String OPENLCB = "OpenLCB";
    final public static String RAWCAN = "Raw CAN";
    final public static String TEST = "Test - do not use";
    
    private static String[] options = new String[]{MERGCBUS, OPENLCB, RAWCAN, TEST};
    
    /**
     * Provide the current set of "Option1" 
     * values
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public String[] getSystemOptions() {
        return options;
    }

    /** 
     * Set the list of protocols to start with OpenLCB
     */
    static public void setOpenLCB() {   
        options = new String[]{OPENLCB, MERGCBUS, RAWCAN, TEST};
    }
    
    /** 
     * Set the list of protocols to start with MERG
     */
    static public void setMERG() {
        options = new String[]{MERGCBUS, OPENLCB, RAWCAN, TEST};
    }
    
    public ConfigurationManager(CanSystemConnectionMemo memo){
        adapterMemo=memo;
    }
    
    protected CanSystemConnectionMemo adapterMemo;
    
    abstract public void configureManagers();
     
    /** 
     * Tells which managers this provides by class
     */
    abstract public boolean provides(Class<?> type);
    
    @SuppressWarnings("unchecked")
    abstract public <T> T get(Class<?> T);
    
    abstract public void dispose();
    
    abstract protected ResourceBundle getActionModelResourceBundle();

}

/* @(#)ConfigurationManager.java */
