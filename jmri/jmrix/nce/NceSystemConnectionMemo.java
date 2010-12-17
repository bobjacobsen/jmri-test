// LocoNetSystemConnectionMemo.java

package jmri.jmrix.nce;

import jmri.*;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.3.2.4 $
 */
public class NceSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {
    
    public NceSystemConnectionMemo() {
        super("N", "NCE");
        register(); // registers general type
        InstanceManager.store(this, NceSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.nce.swing.NceComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory componentFactory = null;
    
    public void setNceUSB(int result) { getNceTrafficController().setUsbSystem(result); }
    public int getNceUSB() { return getNceTrafficController().getUsbSystem(); }
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public NceTrafficController getNceTrafficController() { return nceTrafficController; }
    private NceTrafficController nceTrafficController;
    public void setNceTrafficController(NceTrafficController tc) { nceTrafficController = tc; }
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled())
                return null;
        if (programmerManager == null)
            programmerManager = new NceProgrammerManager(getNceTrafficController(), new NceProgrammer(getNceTrafficController()));
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    /**
     * Sets the NCE message option.
     */
    public void configureCommandStation(int val) {
    	getNceTrafficController().setCommandOptions(val);
    }

    /** 
     * Currently provides only Programmer this way
     */
    public boolean provides(Class<?> type) {
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        return false; // nothing, by default
    }
    
    /** 
     * Currently provides only Programmer this way
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        return null; // nothing, by default
    }

    private NcePowerManager powerManager;
    private NceTurnoutManager turnoutManager;
    private NceLightManager lightManager;
    private NceSensorManager sensorManager;
    private NceThrottleManager throttleManager;
    private NceClockControl clockManager;
    
    /**
     * Configure the common managers for NCE connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    	powerManager = new jmri.jmrix.nce.NcePowerManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setPowerManager(powerManager);

        turnoutManager = new jmri.jmrix.nce.NceTurnoutManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setTurnoutManager(turnoutManager);  

        lightManager = new jmri.jmrix.nce.NceLightManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setLightManager(lightManager);

        sensorManager = new jmri.jmrix.nce.NceSensorManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setSensorManager(sensorManager);

        throttleManager = new jmri.jmrix.nce.NceThrottleManager(getNceTrafficController(), getSystemPrefix());
        InstanceManager.setThrottleManager(throttleManager);
        
        if (getNceUSB() != NceTrafficController.USB_SYSTEM_NONE) {
            if (getNceUSB() != NceTrafficController.USB_SYSTEM_POWERHOUSE) {
                jmri.InstanceManager.setProgrammerManager(new NceProgrammerManager(getNceTrafficController(),
					new NceProgrammer(getNceTrafficController())));
            }
        } else {
            InstanceManager.setProgrammerManager(
                getProgrammerManager());
        }

        clockManager = new jmri.jmrix.nce.NceClockControl(getNceTrafficController(), getSystemPrefix());
        InstanceManager.addClockControl(clockManager);

    }

    public NcePowerManager getNcePowerManager() { return powerManager; }
    public NceTurnoutManager  getNceTurnoutManager() { return turnoutManager; }
    public NceLightManager  getNceLightManager() { return lightManager; }
    public NceSensorManager  getNceSensorManager() { return sensorManager; }
    public NceThrottleManager  getNceThrottleManager() { return throttleManager; }
    public NceClockControl  getNceClockControl() { return clockManager; }
    
    public void dispose() {
    	nceTrafficController = null;
        InstanceManager.deregister(this, NceSystemConnectionMemo.class);
        if (componentFactory != null) 
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        if (powerManager != null) 
            InstanceManager.deregister(powerManager, jmri.jmrix.nce.NcePowerManager.class);
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.nce.NceTurnoutManager.class);
        if (lightManager != null) 
            InstanceManager.deregister(lightManager, jmri.jmrix.nce.NceLightManager.class);
        if (sensorManager != null) 
            InstanceManager.deregister(sensorManager, jmri.jmrix.nce.NceSensorManager.class);
        if (throttleManager != null) 
            InstanceManager.deregister(throttleManager, jmri.jmrix.nce.NceThrottleManager.class);
        if (clockManager != null) 
            InstanceManager.deregister(clockManager, jmri.jmrix.nce.NceClockControl.class);
        super.dispose();
    }
    
}


/* @(#)NceSystemConnectionMemo.java */
