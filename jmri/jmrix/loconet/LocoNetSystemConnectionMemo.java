// LocoNetSystemConnectionMemo.java

package jmri.jmrix.loconet;

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
 * @version             $Revision: 1.19 $
 */
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super("L", "LocoNet");
        this.lt = lt;
        this.sm = sm;
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the LnComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    public LocoNetSystemConnectionMemo() {
        super("L", "LocoNet");
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the LnComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    /**
     * Provides access to the SlotManager for this
     * particular connection.
     */
    public SlotManager getSlotManager() { return sm; }
    private SlotManager sm;
    public void setSlotManager(SlotManager sm){ this.sm = sm;}
    
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public LnTrafficController getLnTrafficController() { return lt; }
    private LnTrafficController lt;
    public void setLnTrafficController(LnTrafficController lt) { this.lt = lt; }
    public LnMessageManager getLnMessageManager() {
        // create when needed
        if (lnm == null) 
            lnm = new LnMessageManager(getLnTrafficController());
        return lnm;
    }
    private LnMessageManager lnm = null;
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null)
            programmerManager = new LnProgrammerManager(getSlotManager());
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    /**
     * Configure the programming manager and "command station" objects
     * @param mCanRead
     * @param mProgPowersOff
     * @param name Command station type name
     */
    public void configureCommandStation(boolean mCanRead, boolean mProgPowersOff, String name) {

        // loconet.SlotManager to do programming (the Programmer instance is registered
        // when the SlotManager is created)
        // set slot manager's read capability
        sm.setCanRead(mCanRead);
        sm.setProgPowersOff(mProgPowersOff);
        sm.setCommandStationType(name);
        
        // store as CommandStation object
        jmri.InstanceManager.setCommandStation(sm);

    }
    
    /** 
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        if (type.equals(jmri.ThrottleManager.class))
            return true;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        if (type.equals(jmri.LightManager.class))
            return true;
        if (type.equals(jmri.ReporterManager.class))
            return true;
        if (type.equals(jmri.ClockControl.class))
            return true;
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(jmri.LightManager.class))
            return (T)getLightManager();
        if (T.equals(jmri.ClockControl.class))
            return (T)getClockControl();
        if (T.equals(jmri.ReporterManager.class))
            return (T)getReporterManager();
        return null; // nothing, by default
    }
    
    protected LocoNetThrottledTransmitter tm;
        
    /**
     * Configure the common managers for LocoNet connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    
        tm = new LocoNetThrottledTransmitter(getLnTrafficController());
        
        InstanceManager.setPowerManager(
            getPowerManager());

        InstanceManager.setTurnoutManager(
            getTurnoutManager());

        InstanceManager.setLightManager(
            getLightManager());

        InstanceManager.setSensorManager(
            getSensorManager());

        InstanceManager.setThrottleManager(
            getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
            getProgrammerManager());

        InstanceManager.setReporterManager(
            getReporterManager());

        InstanceManager.addClockControl(
            getClockControl());

    }
    
    private LnPowerManager powerManager;
    
    public LnPowerManager getPowerManager() { 
        if (getDisabled())
            return null;
        if (powerManager == null)
            powerManager = new jmri.jmrix.loconet.LnPowerManager(this);
        return powerManager;
    }
    
    private LnThrottleManager throttleManager;
    
    public LnThrottleManager getThrottleManager() { 
        if (getDisabled())
            return null;
        if (throttleManager == null)
            throttleManager = new jmri.jmrix.loconet.LnThrottleManager(getSlotManager());
        return throttleManager;
    }
    
    private LnTurnoutManager turnoutManager;
    
    public LnTurnoutManager getTurnoutManager() { 
        if (getDisabled())
            return null;
        if (turnoutManager == null)
            turnoutManager = new jmri.jmrix.loconet.LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix());
        return turnoutManager;
    }
    
    private LnClockControl clockControl;
    
    public LnClockControl getClockControl() { 
        if (getDisabled())
            return null;
        if (clockControl == null)
            clockControl = new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController());
        return clockControl;
    }
    
    private LnReporterManager reporterManager;
    
    public LnReporterManager getReporterManager() { 
        if (getDisabled())
            return null;
        if (reporterManager == null)
            reporterManager = new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix());
        return reporterManager;
    }
    
    private LnSensorManager sensorManager;
    
    public LnSensorManager getSensorManager() { 
        if (getDisabled())
            return null;
        if (sensorManager == null)
            sensorManager = new jmri.jmrix.loconet.LnSensorManager(getLnTrafficController(), getSystemPrefix());
        return sensorManager;
    }
    
    private LnLightManager lightManager;
    
    public LnLightManager getLightManager() { 
        if (getDisabled())
            return null;
        if (lightManager == null)
            lightManager = new jmri.jmrix.loconet.LnLightManager(getLnTrafficController(), getSystemPrefix());
        return lightManager;
    }
    
    
    
    
    public void dispose() {
        lt = null;
        sm = null;
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        if (powerManager != null) 
            InstanceManager.deregister(powerManager, jmri.jmrix.loconet.LnPowerManager.class);
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.loconet.LnTurnoutManager.class);
        if (lightManager != null) 
            InstanceManager.deregister(lightManager, jmri.jmrix.loconet.LnLightManager.class);
        if (sensorManager != null) 
            InstanceManager.deregister(sensorManager, jmri.jmrix.loconet.LnSensorManager.class);
        if (reporterManager != null) 
            InstanceManager.deregister(reporterManager, jmri.jmrix.loconet.LnReporterManager.class);
        if (throttleManager != null) 
            InstanceManager.deregister(throttleManager, jmri.jmrix.loconet.LnThrottleManager.class);
        if (clockControl != null) 
            InstanceManager.deregister(clockControl, jmri.jmrix.loconet.LnClockControl.class);
        super.dispose();
    }
    
}


/* @(#)LocoNetSystemConnectionMemo.java */
