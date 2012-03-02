package jmri.jmrit.signalling;

import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Sensor;
import jmri.Block;
import jmri.Turnout;
import jmri.NamedBean;
import jmri.InstanceManager;
import jmri.JmriException;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.display.layoutEditor.ConnectivityUtil;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockConnectivityTools;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.SensorIcon;


/**
 * Implements an Entry Exit based method of setting turnouts, setting up signal logic and the 
 * allocation of blocks through based upon the layout editor.
 * <p>
 * The route is based upon having a sensor assigned at a known location on the panel 
 * (set at the boundary of two different blocks) through to a sensor at a remote location
 * on the same panel.  Using the layout block routing, a path can then be set between the
 * two sensors so long as one existings and that no section of track is set occupied.
 * If possible an alternative route will be used.
 * <p>
 * Initial implementation only handles the setting up of turnouts on a path.
 
 * @author Kevin Dickerson  Copyright (C) 2011
 * @version			$Revision: 19923 $
 */
public class EntryExitPairs {

	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    static volatile EntryExitPairs _instance = null;
    
    public int routingMethod = LayoutBlockConnectivityTools.METRIC;

    final static int HOPCOUNT = LayoutBlockConnectivityTools.HOPCOUNT;
    final static int METRIC = LayoutBlockConnectivityTools.METRIC;
    
    final static int NXBUTTONSELECTED = 0x08;
    final static int NXBUTTONACTIVE = Sensor.ACTIVE;
    final static int NXBUTTONINACTIVE = Sensor.INACTIVE;
    
    /**
    * Constant value to represent that the entryExit will only set up the
    * turnouts between two different points
    */
    final static int SETUPTURNOUTSONLY = 0x00;
    
    /**
    * ### Not yet fully impliemented ###
    * Constant value to represent that the entryExit will set up the
    * turnouts between two different points and configure the signalmast logic
    * to use the correct blocks.
    */
    final static int SETUPSIGNALMASTLOGIC = 0x01;
    
   /**
    * ### Not yet fully impliemented ###
    * Constant value to represent that the entryExit will do full interlocking
    * it will set the turnouts and "reserve" the blocks.
    */
    final static int FULLINTERLOCK = 0x02;
    
    static int nxButtonTimeout = 10;
    
    static JPanel glassPane = new JPanel();
    
    //Method to get delay between issuing Turnout commands
    int turnoutSetDelay = 0;
    
    private EntryExitPairs(){
        _instance = this;
        InstanceManager.configureManagerInstance().registerUser(this);
        InstanceManager.layoutBlockManagerInstance().addPropertyChangeListener(propertyBlockManagerListener);
        
        glassPane.setOpaque(false);
        glassPane.setLayout(null);
        glassPane.addMouseListener(new MouseAdapter() { 
          public void mousePressed(MouseEvent e) {
            e.consume();
          } 
        }); 
    }
    
    static public EntryExitPairs instance() {
        if (_instance == null){
            EntryExitPairs m = new EntryExitPairs();
            _instance = m;
        }
        return _instance;
    }

    HashMap<PointDetails, Source> nxpair = new HashMap<PointDetails, Source>();
    
    public void addNXSourcePoint(LayoutBlock facing, LayoutBlock protecting, NamedBean loc, LayoutEditor panel){
        PointDetails point = providePoint(facing, protecting, panel);
        point.setRefObject(loc);
    }
    
    public void addNXSourcePoint(NamedBean source){
        PointDetails point = null;
        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        for(int i = 0; i<layout.size(); i++){
            point = providePoint(source, layout.get(i));
        }
        if(point==null){
            log.error("Unable to find a location on any panel for item " + source.getDisplayName());
            return;
        }
    }
    
    public void addNXSourcePoint(NamedBean source, LayoutEditor panel){
        if(source==null){
            log.error("source bean supplied is null");
            return;
        }
        if(panel==null){
            log.error("panel supplied is null");
            return;
        }
        PointDetails point;
        point = providePoint(source, panel);
        if(point==null){
            log.error("Unable to find a location on the panel " + panel.getLayoutName() + " for item " + source.getDisplayName());
            return;
        }
    }
    
    public Object getEndPointLocation(NamedBean source, LayoutEditor panel){
        if(source==null){
            log.error("Source bean past is null");
            return null;
        }
        if(panel==null){
            log.error("panel past is null");
            return null;
        }
        PointDetails sourcePoint = getPointDetails(source, panel);
        if(sourcePoint==null){
            log.error("Point is not located");
            return null;
        }
        return sourcePoint.getRefLocation();
    }
    
    /**
    * This method will generate the point details, given a known source and layout panel.
    * 
    */
    private PointDetails providePoint(NamedBean source, LayoutEditor panel){
        PointDetails sourcePoint = getPointDetails(source, panel);
        if(sourcePoint==null){
            LayoutBlock facing = null;
            LayoutBlock protecting = null;
            if(source instanceof SignalMast){
                facing = InstanceManager.layoutBlockManagerInstance().getFacingBlockByMast((SignalMast)source, panel);
                protecting = InstanceManager.layoutBlockManagerInstance().getProtectedBlockByMast((SignalMast)source, panel);
            } else if (source instanceof Sensor) {
                facing = InstanceManager.layoutBlockManagerInstance().getFacingBlockBySensor((Sensor)source, panel);
                protecting = InstanceManager.layoutBlockManagerInstance().getProtectedBlockBySensor((Sensor)source, panel);
            } else if (source instanceof SignalHead){
                facing = InstanceManager.layoutBlockManagerInstance().getFacingBlock((SignalHead)source, panel);
                protecting = InstanceManager.layoutBlockManagerInstance().getProtectedBlock((SignalHead)source, panel);
            }
            if((facing==null) && (protecting==null)){
                log.error("Unable to find facing and protecting block");
                return null;
            }
            sourcePoint = providePoint(facing, protecting, panel);
            if(sourcePoint!=null)
                sourcePoint.setRefObject(source);
        }
        return sourcePoint;
    }
    
    public List<Object> getSourceList(LayoutEditor panel){
        List<Object> list = new ArrayList<Object>();
         
        for(Entry<PointDetails, Source> e : nxpair.entrySet()){
            Object obj = (e.getKey()).getRefObject();
            LayoutEditor pan = (e.getKey()).getLayoutEditor();
            if(pan==panel){
                if (!list.contains(obj))
                    list.add(obj);
                } // end while
        }
        return list;
    }

    public int getNxPairNumbers(LayoutEditor panel){
        int total=0;
        for(Entry<PointDetails, Source> e : nxpair.entrySet()){
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getLayoutEditor();
            if(pan==panel){
                total = total+nxpair.get(key).getNumberOfDestinations();
            } // end while
        }
        return total;
    }

    ArrayList<Object> destinationList = new ArrayList<Object>();
    
    //Need to sort out the presentation of the name here rather than using the point id
    //This is used for the creation and display of information in the table
    //The presentation of the name might have to be done at the table level.
    public ArrayList<Object> getNxSource(LayoutEditor panel){
        ArrayList<Object> source = new ArrayList<Object>();
        destinationList = new ArrayList<Object>();
        
        for(Entry<PointDetails, Source> e : nxpair.entrySet()){
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getLayoutEditor();
            if(pan==panel){
               ArrayList<PointDetails> dest = nxpair.get(key).getDestinationPoints();
               for(int i = 0; i<dest.size(); i++){
                   destinationList.add(dest.get(i).getRefObject());
                   source.add(key.getRefObject());
               }
            }
        }
        return source;
    }

    public ArrayList<Object> getNxDestination(){
        return destinationList;
    }

    public ArrayList<LayoutEditor> getSourcePanelList(){
        ArrayList<LayoutEditor> list = new ArrayList<LayoutEditor>();
        
        for(Entry<PointDetails, Source> e : nxpair.entrySet()){
            PointDetails key = e.getKey();
            LayoutEditor pan = key.getLayoutEditor();
           if (!list.contains(pan))
                list.add(pan);
        }
        return list;
    }
   
   /**
    * Returns a point if already exists, or creates a new one if not.
    */
    private PointDetails providePoint(LayoutBlock source, LayoutBlock protecting, LayoutEditor panel){
        PointDetails sourcePoint = getPointDetails(source, protecting, panel);
        if(sourcePoint==null){
            sourcePoint = new PointDetails(source, protecting);
            sourcePoint.setPanel(panel);
        }
        return sourcePoint;
    }
    
    public void addNXDestination(NamedBean source, NamedBean destination, LayoutEditor panel){
        if (source==null) {
            log.error("no source Object provided");
            return;
        }
        if(destination==null){
            log.error("no destination Object provided");
            return;
        }
        PointDetails sourcePoint = providePoint(source, panel);
        if(sourcePoint==null){
            log.error("source point for " + source.getDisplayName() + " not created addNXDes");
            return;
        }
        
        sourcePoint.setPanel(panel);
        sourcePoint.setRefObject(source);
        PointDetails destPoint = providePoint(destination, panel);
        if(destPoint!=null){
            destPoint.setPanel(panel);
            destPoint.setRefObject(destination);
            if (!nxpair.containsKey(sourcePoint)){
                nxpair.put(sourcePoint, new Source(sourcePoint));
            }
            nxpair.get(sourcePoint).addDestination(destPoint);
        }
        
        firePropertyChange("length", null, null);
    }
    
    public ArrayList<Object> getDestinationList(Object obj, LayoutEditor panel){
        ArrayList<Object> list = new ArrayList<Object>();
        if(nxpair.containsKey(getPointDetails(obj, panel))){
            ArrayList<PointDetails> from = nxpair.get(getPointDetails(obj, panel)).getDestinationPoints();
            for(int i = 0; i<from.size(); i++){
                list.add(from.get(i).getRefObject());
            }
        }
        return list;
    }
    
    public void deleteNxPair(NamedBean source, NamedBean destination, LayoutEditor panel){
        PointDetails sourcePoint = getPointDetails(source, panel);
        if(sourcePoint==null){
            if(log.isDebugEnabled())
                log.debug("source " + source.getDisplayName() + " does not exist so can not delete pair");
            return;
        }
        
        PointDetails destPoint = getPointDetails(destination, panel);
        if(destPoint==null){
            if(log.isDebugEnabled())
                log.debug("destination " + destination.getDisplayName() + " does not exist so can not delete pair");
            return;
        }
        
        if(nxpair.containsKey(sourcePoint)){
            nxpair.get(sourcePoint).removeDestination(destPoint);
            firePropertyChange("length", null, null);
            if(nxpair.get(sourcePoint).getDestinationPoints().size()==0){
                nxpair.remove(sourcePoint);
            }
        }
        else if(log.isDebugEnabled())
            log.debug("source " + source.getDisplayName() + " is not a valid source so can not delete pair");
        
    }
    
    public boolean isDestinationValid(Object source, Object dest, LayoutEditor panel){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            return nxpair.get(getPointDetails(source, panel)).isDestinationValid(getPointDetails(dest, panel));
        }
        return false;
    }
    
    public boolean isUniDirection(Object source, LayoutEditor panel, Object dest){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            return nxpair.get(getPointDetails(source, panel)).getUniDirection(dest, panel);
        }
        return false;
    }
    
    public void setUniDirection(Object source, LayoutEditor panel, Object dest, boolean set){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            nxpair.get(getPointDetails(source, panel)).setUniDirection(dest, panel, set);
        }
    }
    
    public boolean isEnabled(Object source, LayoutEditor panel, Object dest){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            return nxpair.get(getPointDetails(source, panel)).isEnabled(dest, panel);
        }
        return false;
    }
    
    public void setEnabled(Object source, LayoutEditor panel, Object dest, boolean set){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            nxpair.get(getPointDetails(source, panel)).setEnabled(dest, panel, set);
        }
    }
    
    public void setEntryExitType(Object source, LayoutEditor panel, Object dest, int set){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            nxpair.get(getPointDetails(source, panel)).setEntryExitType(dest, panel, set);
        }
    }
    
    public int getEntryExitType(Object source, LayoutEditor panel, Object dest){
        if(nxpair.containsKey(getPointDetails(source, panel))){
            return nxpair.get(getPointDetails(source, panel)).getEntryExitType(dest, panel);
        }
        return 0x00;
    }
    
    //protecting helps us to determine which direction we are going in.
    //validateOnly flag is used, if all we are doing is simply checking to see if the source/destpoints are valid, when creating the pairs in the user GUI

    public boolean isPathActive(Object sourceObj, Object destObj, LayoutEditor panel){
        PointDetails pd = getPointDetails(sourceObj, panel);
        if(nxpair.containsKey(pd)){
            Source source = nxpair.get(pd);
            return source.isRouteActive(getPointDetails(destObj, panel));
        }
        return false;
    }

    jmri.SignalMastLogicManager smlm = InstanceManager.signalMastLogicManagerInstance();

    class Source {
    
        NamedBean sourceObject = null;
        Object sourceSignal = null;
        jmri.SignalMastLogic sml;
        String ref = "Empty";
        PointDetails pd = null;
        
        //Using Object here rather than sourceSensor, working on the basis that it might
        //one day be possible to have a signal icon selectable on a panel and 
        //generate an propertychange, so hence do not want to tie it down at this stage.
        HashMap<Object, DestinationPoints> destObject = new HashMap<Object, DestinationPoints>();
        HashMap<DestinationPoints, Object> revDestObject = new HashMap<DestinationPoints, Object>();
        
        boolean isEnabled(Object dest,LayoutEditor panel){
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    return en.getKey().isEnabled();
                }
            }
            return true;
        }
        
        void setEnabled(Object dest, LayoutEditor panel, boolean boo){
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    en.getKey().setEnabled(boo);
                }
            }
        }
        
        Source(PointDetails point){
            if(getSensorFromPoint(point)!=null){
                addSourceObject(getSensorFromPoint(point));
            } else {
                addSourceObject(getSignalFromPoint(point));
            }
            point.setSource(this);
            sourceSignal = getSignalFromPoint(point);
            pd = point;
        }
        
        PointDetails getPoint(){
            return pd;
        }
        
        LayoutBlock getStart(){
            return pd.getFacing();
        }
        
        LayoutBlock getSourceProtecting(){
            return pd.getProtecting();
        }
        
        final static int CANCELROUTE = 0;
        final static int CLEARROUTE = 1;
        
        void addDestination(PointDetails dest){
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(dest))
                    return;
            }
            
            DestinationPoints dstPoint = new DestinationPoints(dest);
            Sensor destSensor = getSensorFromPoint(dest);
            dest.setDestination(dstPoint, this);
            if(destSensor!=null){
                if(!revDestObject.containsKey(dstPoint)){
//                    destSensor.addPropertyChangeListener(propertyDestinationListener);
                    destObject.put(destSensor, dstPoint);
                    revDestObject.put(dstPoint, destSensor);
                }
            }
        }
        
        void removeDestination(PointDetails dest){
            DestinationPoints destPoint2Remove = null;
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(dest)){
                    if(revDestObject.get(en.getKey()) instanceof Sensor){
                        en.getKey().getPoint().removeDestination(en.getKey());
                    }
                    if(point.getSensor()!=null){
                        point.removeDestination(en.getKey());
                    }
                    destPoint2Remove = en.getKey();
                    destObject.remove(revDestObject.get(en.getKey()));
                }
            }
            revDestObject.remove(destPoint2Remove);
            if(revDestObject.size()==0){
                getPoint().removeSource(this);
            }
        }

        void addSourceObject(NamedBean source){
            if (sourceObject==source)
                return;
            sourceObject = source;
        }
        
        Object getSourceObject() { return sourceObject; }
        
        ArrayList<PointDetails> getDestinationPoints() {
            ArrayList<PointDetails> rtn = new ArrayList<PointDetails>();
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                rtn.add(en.getKey().getPoint());
            }
            return rtn;
        }
        
        boolean isDestinationValid(PointDetails destPoint){
            boolean exists = false;
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                if((en.getKey()).getPoint().equals(destPoint))
                    exists = true;
            }
            return exists;
        }
        
        boolean getUniDirection(Object dest, LayoutEditor panel){
            //Work on the principle that if the source is uniDirection, then the destination has to be.
            //if(uniDirection) return uniDirection;
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    return en.getKey().getUniDirection();
                }
            }
            
            return true;
        }
        
        void setUniDirection(Object dest, LayoutEditor panel, boolean set){
            
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    en.getKey().setUniDirection(set);
                }
            }
        }
        
        boolean isRouteActive(PointDetails endpoint){
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(endpoint)){
                    return en.getKey().activeEntryExit;
                }
            }
            return false;
        }
        
        void activeBean(DestinationPoints dest, boolean reverseDirection){
            /*DestinationPoints fromSen = destObject.get(dest);
            fromSen.getPoint().nxButtonTimeOut();*/
            dest.activeBean(reverseDirection);
        }

        int getNumberOfDestinations() { return revDestObject.size(); }
        
        void setEntryExitType(Object dest, LayoutEditor panel, int type){
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    en.getKey().setEntryExitType(type);
                }
            }
            if(type!=SETUPTURNOUTSONLY){
                if (sourceSignal instanceof SignalMast){
                    ((SignalMast) sourceSignal).setHeld(true);
                }
            }
        }
        
        int getEntryExitType(Object dest, LayoutEditor panel){
            PointDetails lookingFor = getPointDetails(dest, panel);
            for(Entry<DestinationPoints, Object> en : revDestObject.entrySet()){
                PointDetails point = (en.getKey()).getPoint();
                if(point.equals(lookingFor)){
                    return en.getKey().getEntryExitType();
                }
            }
            
            return 0x00;
        
        }
        
        final static int PROMPTUSER = 0x00;
        final static int AUTOCLEAR = 0x01;
        final static int AUTOCANCEL = 0x02;
        
        class DestinationPoints{
        
            PointDetails point = null;
            Boolean uniDirection = true;
            int entryExitType = SETUPTURNOUTSONLY;//SETUPSIGNALMASTLOGIC;
            boolean enabled = true;
            boolean activeEntryExit = false;
            ArrayList<LayoutBlock> routeDetails = new ArrayList<LayoutBlock>();
            LayoutBlock destination;
            
            boolean isEnabled(){
                return enabled;
            }
            
            void setEnabled(boolean boo){
                //boolean oldEnabled = enabled;
                //Need to do other bits when enabling
                enabled = boo;
            }
        
            DestinationPoints(PointDetails point){
                this.point=point;
                //point.setDestination(this);
            }
            
            PointDetails getPoint(){
                return point;
            }
            
            boolean getUniDirection(){
                return uniDirection;
            }
            
            void setUniDirection(boolean uni){
                uniDirection = uni;
            }
            
            NamedBean getSignal(){
                return getSignalFromPoint(point);
            }
            
            void setRouteTo(boolean set) {
                point.setRouteTo(set);
                if(set){
                    setNXButtonState(point, NXBUTTONACTIVE);
                } else {
                    setNXButtonState(point, NXBUTTONINACTIVE);
                }
            }
            
            void setRouteFrom(boolean set){
                pd.setRouteFrom(set);
                if(set){
                    setNXButtonState(pd, NXBUTTONACTIVE);
                } else {
                    setNXButtonState(pd, NXBUTTONINACTIVE);
                }
            }
            
            boolean isRouteToPointSet() { return point.isRouteToPointSet(); }
            
            LayoutBlock getFacing() { return point.getFacing(); }
            LayoutBlock getProtecting() { return point.getProtecting(); }
            
            int getEntryExitType(){
                return entryExitType;
            }
            
            void setEntryExitType(int type){
                entryExitType = type;
                if(type!=SETUPTURNOUTSONLY)
                    uniDirection = true;
            }
            
            //This should probably look at the changes being made at the block level not at the sensor level
            protected PropertyChangeListener propertyBlockListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    Block blk = (Block) e.getSource();
                    if (e.getPropertyName().equals("state")) {
                        if (log.isDebugEnabled()) log.debug(ref + "  We have a change of state on the block " + blk.getDisplayName());
                        //int now = ((Integer) e.getNewValue()).intValue();
                        int old = ((Integer) e.getOldValue()).intValue();
                        
                        if ((old==Block.OCCUPIED)||(old==Block.UNOCCUPIED)){
                            LayoutBlock lBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(blk);
                            //If the sourceSensor was previously active or inactive then we will 
                            //reset the useExtraColor, but not if it was previously unknown or inconsistent.
                            lBlock.setUseExtraColor(false);
                            blk.removePropertyChangeListener(propertyBlockListener); //was this
                            removeBlockFromRoute(lBlock);
                        } else {
                            if (log.isDebugEnabled()) log.debug("old state was " + old + " did not go through reset");
                        }
                    }
                }
            };
        
            void removeBlockFromRoute(LayoutBlock lBlock){
            
                if (routeDetails!=null){
                    if(routeDetails.contains(lBlock)){
                        routeDetails.remove(lBlock);
                        setRouteFrom(false);
                        setNXButtonState(pd, NXBUTTONINACTIVE);
                    }
                    if (log.isDebugEnabled()){
                        log.debug("Route details contents " + routeDetails);
                        for(int i = 0; i<routeDetails.size(); i++){
                            log.debug("      " + routeDetails.get(i).getDisplayName());
                        }
                    }
                    if((routeDetails.size()==2)&& (routeDetails.contains(destination)) && (routeDetails.contains(getStart()))){
                        //We have reached our destination, therefore we shall clear our inter-lock
                        routeDetails.get(0).getBlock().removePropertyChangeListener(propertyBlockListener);  // was set against block sensor
                        routeDetails.remove(getStart());
                    } if((routeDetails.size()==1) && (routeDetails.contains(destination))){
                        routeDetails.get(0).getBlock().removePropertyChangeListener(propertyBlockListener);  // was set against block sensor
                        routeDetails.remove(destination);
                    }
                }

                if((routeDetails==null)||(routeDetails.size()==0)){
                    //At this point the route has cleared down/the last remaining block are now active.
                    //Therefore we will 
                    routeDetails=null;
                    setRouteTo(false);
                    setRouteFrom(false);
                    //setNXButtonState(point, NXBUTTONINACTIVE);
                    activeEntryExit=false;
                }
            }
            
            //For a clear down we need to add a message, if it is a cancel, manual clear down or I didn't mean it.
            void setRoute(boolean state){
                //int buttonState = NXBUTTONINACTIVE;
                Hashtable<Turnout, Integer> turnoutSettings = new Hashtable<Turnout, Integer>();
                if(routeDetails==null){
                    log.error ("No route to set or clear down");
                    activeEntryExit = false;
                    setRouteTo(false);
                    setRouteFrom(false);
                    if((getSignal() instanceof SignalMast) && (getEntryExitType()!=FULLINTERLOCK)){
                        SignalMast mast = (SignalMast) getSignal();
                        mast.setHeld(false);
                    }
                    destination=null;
                    return;
                }
                if(!state){
                    cancelClearOptionBox();
                    return;
                }

                ConnectivityUtil connection = new ConnectivityUtil(point.getLayoutEditor());
                
                //This for loop was after the if statement
                //Last block in the route is the one that we are protecting at the last sensor/signalmast
                for (int i = 0; i<routeDetails.size(); i++){
                    if (i>0) {
                        ArrayList<LayoutTurnout> turnoutlist;
                        int nxtBlk = i+1;
                        int preBlk = i-1;
                        if (i==routeDetails.size()-1){
                            nxtBlk = i;
                        } else if (i==0){
                            preBlk=i;
                        }
                        turnoutlist=connection.getTurnoutList(routeDetails.get(i).getBlock(), routeDetails.get(preBlk).getBlock(), routeDetails.get(nxtBlk).getBlock());
                        ArrayList<Integer> throwlist=connection.getTurnoutSettingList();
                        for (int x=0; x<turnoutlist.size(); x++){
                            String t = turnoutlist.get(x).getTurnoutName();
                            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(t);
                            turnout.setCommandedState(throwlist.get(x));
                            turnoutSettings.put(turnout, throwlist.get(x));
                            
                            Runnable r = new Runnable() {
                              public void run() {
                                try {
                                    Thread.sleep(250 + turnoutSetDelay);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                              }
                            };
                            Thread thr = new Thread(r);
                            thr.start();
                            try{
                                thr.join();
                            } catch (InterruptedException ex) {
                    //            log.info("interrupted at join " + ex);
                            }
                        }
                        if(getEntryExitType()==FULLINTERLOCK){
                            routeDetails.get(i).setUseExtraColor(true);
                        }
                    }
                    if ((getEntryExitType()!=SETUPTURNOUTSONLY)){
                        if(getStart().getBlock()!=routeDetails.get(i).getBlock()){
                            routeDetails.get(i).getBlock().addPropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
                        }
                    } else {
                        routeDetails.get(i).getBlock().removePropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
                    }
                }

                if (getEntryExitType()!=SETUPTURNOUTSONLY){
                    if((sourceSignal instanceof SignalMast) && (getSignal() instanceof SignalMast)){
                        SignalMast smSource = (SignalMast) sourceSignal;
                        SignalMast smDest = (SignalMast) getSignal();
                        sml = smlm.newSignalMastLogic(smSource);
                        if(!sml.isDestinationValid(smDest)){
                            //if no signalmastlogic existed then created it, but set it not to be stored.
                            sml.setDestinationMast(smDest);
                            sml.setStore(jmri.SignalMastLogic.STORENONE, smDest);
                        }
                        Hashtable<Block, Integer> blks = new Hashtable<Block, Integer>();
                        //Remove the first block
                        routeDetails.remove(0);
                        for(int i = 0; i<routeDetails.size(); i++){
                            if (routeDetails.get(i).getBlock().getState()==Block.UNKNOWN)
                                routeDetails.get(i).getBlock().setState(Block.UNOCCUPIED);
                            blks.put(routeDetails.get(i).getBlock(), Block.UNOCCUPIED);
                        }
                        smSource.setHeld(false);
                        sml.setAutoBlocks(blks, smDest);
                        sml.setAutoTurnouts(turnoutSettings, smDest);
                        sml.initialise(smDest);
                        smSource.addPropertyChangeListener( new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent e) {
                                SignalMast source = (SignalMast)e.getSource();
                                source.removePropertyChangeListener(this);
                                setRouteFrom(true);
                                setRouteTo(true);
                            }
                        });
                        pd.extendedtime=true;
                        point.extendedtime=true;
                    } else {
                        if (sourceSignal instanceof SignalMast){
                            SignalMast mast = (SignalMast) sourceSignal;
                            mast.setHeld(false);
                        } else if (sourceSignal instanceof SignalHead){
                            SignalHead head = (SignalHead) sourceSignal;
                            head.setHeld(false);
                        }
                        setRouteFrom(true);
                        setRouteTo(true);
                    }
                } else {
                    setNXButtonState(pd, NXBUTTONINACTIVE);
                    setNXButtonState(point, NXBUTTONINACTIVE);
                }
            }
            
            private JFrame cancelClearFrame;
            private Thread threadAutoClearFrame = null;
            
            void cancelClearOptionBox(){
                if(cancelClearFrame==null){
                    JButton jButton_Clear = new JButton("Clear Down");
                    JButton jButton_Cancel = new JButton("Cancel");
                    JButton jButton_Exit = new JButton("Exit");
                    JLabel jLabel = new JLabel("What would you like to do with this interlock?");
                    JLabel jIcon = new JLabel(javax.swing.UIManager.getIcon("OptionPane.questionIcon"));
                    cancelClearFrame = new JFrame("Interlock");
                    Container cont = cancelClearFrame.getContentPane();  
                    JPanel qPanel = new JPanel();
                    qPanel.add(jIcon);
                    qPanel.add(jLabel);
                    cont.add(qPanel, BorderLayout.CENTER);  
                    JPanel buttonsPanel = new JPanel();
                    buttonsPanel.add(jButton_Cancel);  
                    buttonsPanel.add(jButton_Clear);  
                    buttonsPanel.add(jButton_Exit);  
                    cont.add(buttonsPanel, BorderLayout.SOUTH);  
                    cancelClearFrame.pack();
                    
                    jButton_Clear.addActionListener( new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    cancelClearFrame.setVisible(false);
                                    threadAutoClearFrame.interrupt();
                                    cancelClearInterlock(CLEARROUTE);
                                }
                            });
                    jButton_Cancel.addActionListener( new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    cancelClearFrame.setVisible(false);
                                    threadAutoClearFrame.interrupt();
                                    cancelClearInterlock(CANCELROUTE);
                                }
                            });
                    jButton_Exit.addActionListener( new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    cancelClearFrame.setVisible(false);
                                    threadAutoClearFrame.interrupt();
                                    cancelClearInterlock(2);
                                }
                            });
                    getPoint().getPanel().setGlassPane(glassPane);
                }
                if(cancelClearFrame.isVisible()){
                    return;
                }
                pd.extendedtime=true;
                point.extendedtime =true;
                
                class MessageTimeOut implements Runnable {
                    MessageTimeOut(){
                    }
                    public void run() {
                        try {
                            //Set a timmer before this window is automatically closed to 30 seconds
                            Thread.sleep(30000);
                            cancelClearFrame.setVisible(false);
                            cancelClearInterlock(2);
                        } catch (InterruptedException ex) {
                            log.debug("Flash timer cancelled");
                        }
                    }
                }
                MessageTimeOut mt = new MessageTimeOut();
                threadAutoClearFrame = new Thread(mt, "NX Button Message Timeout ");
                threadAutoClearFrame.start();
                cancelClearFrame.setAlwaysOnTop(true);
                getPoint().getPanel().getGlassPane().setVisible(true);
                int w = cancelClearFrame.getSize().width;
                int h = cancelClearFrame.getSize().height;
                int x = (int)getPoint().getPanel().getLocation().getX()+((getPoint().getPanel().getSize().width-w)/2);
                int y = (int)getPoint().getPanel().getLocation().getY()+((getPoint().getPanel().getSize().height-h)/2);
                cancelClearFrame.setLocation(x, y);
                cancelClearFrame.setVisible(true);
            }
            
            void cancelClearInterlock(int cancelClear){
                if (cancelClear==2){
                    setNXButtonState(pd, NXBUTTONINACTIVE);
                    setNXButtonState(point, NXBUTTONINACTIVE);
                    getPoint().getPanel().getGlassPane().setVisible(false);
                    return;
                }
                
                if (sourceSignal instanceof SignalMast){
                    SignalMast mast = (SignalMast) sourceSignal;
                    mast.setAspect(mast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DANGER));
                    mast.setHeld(true);
                } else if (sourceSignal instanceof SignalHead){
                    SignalHead head = (SignalHead) sourceSignal;
                    head.setHeld(true);
                } else {
                    log.error("No signal found");
                }
                
                //Get rid of the signal mast logic to the destination mast.
                if((getSignal() instanceof SignalMast) && (sml!=null)){
                    SignalMast mast = (SignalMast) getSignal();
                    if (sml.getStoreState(mast)==jmri.SignalMastLogic.STORENONE)
                        sml.removeDestination(mast);
                }
                sml=null;
                
                for (int i = 0; i<routeDetails.size(); i++){
                    if((getEntryExitType()==FULLINTERLOCK)){
                        routeDetails.get(i).setUseExtraColor(false);
                    }
                    routeDetails.get(i).getBlock().removePropertyChangeListener(propertyBlockListener); // was set against occupancy sensor
                }
                
                if (cancelClear == CLEARROUTE){
                    if (routeDetails.size()==0){
                        if (log.isDebugEnabled()) log.debug(ref + "  all blocks have automatically been cleared down");
                    } else {
                        if (log.isDebugEnabled()) log.debug(ref + "  No blocks were cleared down " + routeDetails.size());
                        try{
                            if (log.isDebugEnabled()) log.debug(ref + "  set first block as active so that we can manually clear this down " + routeDetails.get(0).getBlock().getSensor().getDisplayName());
                            routeDetails.get(0).getBlock().getSensor().setState(Sensor.ACTIVE);
                            getStart().getBlock().getSensor().setState(Sensor.INACTIVE);
                        } catch (java.lang.NullPointerException e){
                            log.error(e);
                        } catch (JmriException e){
                            log.error(e);
                        }
                        if (log.isDebugEnabled()){ 
                            log.debug(ref + "  Going to clear routeDetails down " + routeDetails.size());
                            for(int i = 0; i<routeDetails.size(); i++){
                                log.debug("Block at " + i + " " + routeDetails.get(i).getDisplayName());
                            }
                        }
                        if(routeDetails.size()>1){
                            //We will remove the propertychange listeners on the sensors as we will now manually clear things down.
                            //Should we just be updating the block status and not the sensor
                            for (int i = 1; i <routeDetails.size()-1; i++){
                                if (log.isDebugEnabled()) log.debug(ref + " in loop Set active " + routeDetails.get(i).getDisplayName() + " " + routeDetails.get(i).getBlock().getSystemName());
                                try{
                                    routeDetails.get(i).getOccupancySensor().setState(Sensor.ACTIVE); //was getBlock().getSensor()
                                    routeDetails.get(i).getBlock().goingActive();
                                    
                                    if (log.isDebugEnabled()) log.debug(ref + " in loop Set inactive " + routeDetails.get(i-1).getDisplayName() + " " + routeDetails.get(i-1).getBlock().getSystemName());
                                    routeDetails.get(i-1).getOccupancySensor().setState(Sensor.INACTIVE); //was getBlock().getSensor()
                                    routeDetails.get(i-1).getBlock().goingInactive();
                                } catch (java.lang.NullPointerException e){
                                    log.error(e);
                                } catch (JmriException e){
                                    log.error(e);
                                }
                            }
                            try{
                                if (log.isDebugEnabled()) log.debug(ref + " out of loop Set active " + routeDetails.get(routeDetails.size()-1).getDisplayName() + " " + routeDetails.get(routeDetails.size()-1).getBlock().getSystemName());
                                //Get the last block an set it active.
                                routeDetails.get(routeDetails.size()-1).getOccupancySensor().setState(Sensor.ACTIVE);
                                if (log.isDebugEnabled()) log.debug(ref + " out of loop Set inactive " + routeDetails.get(routeDetails.size()-2).getUserName() + " " + routeDetails.get(routeDetails.size()-2).getBlock().getSystemName());
                                routeDetails.get(routeDetails.size()-2).getOccupancySensor().setState(Sensor.INACTIVE);
                            } catch (java.lang.NullPointerException e){
                                log.error(e);
                            } catch (java.lang.ArrayIndexOutOfBoundsException e){
                                log.error(e);
                            }   catch (JmriException e){
                                log.error(e);
                            }
                        }
                    }
                } else {
                    for(LayoutBlock blk : routeDetails){
                        blk.getBlock().removePropertyChangeListener(propertyBlockListener);
                    }
                    //Need sort out the method to cancel said route.
                }
                activeEntryExit = false;
                setRouteTo(false);
                setRouteFrom(false);
                routeDetails=null;
                pd.cancelNXButtonTimeOut();
                point.cancelNXButtonTimeOut();
                getPoint().getPanel().getGlassPane().setVisible(false);
            
            }
        
            void activeBean(boolean reverseDirection){
                if(activeEntryExit){
                   // log.debug(ref + "  Our route is active so this would go for a clear down but we need to check that the we can clear it down" + activeEndPoint);
                    if(!isEnabled()){
                        log.info("A disabled entry exit has been called will bomb out");
                    }
                    if (activeEntryExit){
                        log.debug(ref + "  We have a valid match on our end point so we can clear down");
                        //setRouteTo(false);
                        //pd.setRouteFrom(false);
                        setRoute(false);
                    } else {
                        log.debug(ref + "  sourceSensor that has gone active doesn't match the active end point so will not clear");
                        JOptionPane.showMessageDialog(null, "A conflicting route has already been set");
                        setNXButtonState(pd, NXBUTTONINACTIVE);
                        setNXButtonState(point, NXBUTTONINACTIVE);
                    }
                } else {
                    if (isRouteToPointSet()){
                        log.debug(ref + "  route to this point is set therefore can not set another to it " /*+ destPoint.getPoint().getID()*/);
                        setNXButtonState(pd, NXBUTTONINACTIVE);
                        setNXButtonState(point, NXBUTTONINACTIVE);
                        return;
                    } else {
                        LayoutBlock startlBlock = getStart();
                        LayoutBlock protectLBlock = getSourceProtecting();
                        LayoutBlock destinationLBlock;

                        if(!reverseDirection){
                            //We have a problem, the destination point is already setup with a route, therefore we would need to 
                            //check some how that a route hasn't been set to it.
                            destinationLBlock = getFacing();
                        } else {
                            protectLBlock = getProtecting();
                            startlBlock = getFacing();
                            destinationLBlock = getStart();
                            try{
                            if(!InstanceManager.layoutBlockManagerInstance().getLayoutBlockConnectivityTools().checkValidDest(startlBlock, protectLBlock, getSourceProtecting(), getStart())){
                                startlBlock = getProtecting();
                                protectLBlock = getFacing();
                                if(!InstanceManager.layoutBlockManagerInstance().getLayoutBlockConnectivityTools().checkValidDest(startlBlock, protectLBlock, getSourceProtecting(), getStart())){
                                    log.error("No route found");
                                    JOptionPane.showMessageDialog(null, "No Valid path found");
                                    setNXButtonState(pd, NXBUTTONINACTIVE);
                                    setNXButtonState(point, NXBUTTONINACTIVE);
                                    return;
                                }
                            }
                            } catch (jmri.JmriException ex){
                                JOptionPane.showMessageDialog(null, ex.getMessage());
                                log.error("Exception " + ex.getMessage());
                                setNXButtonState(pd, NXBUTTONINACTIVE);
                                setNXButtonState(point, NXBUTTONINACTIVE);
                                return;
                            }
                        }
                        destination = destinationLBlock;
                        try{
                            routeDetails = InstanceManager.layoutBlockManagerInstance().getLayoutBlockConnectivityTools().getLayoutBlocks(startlBlock, destinationLBlock, protectLBlock, false, 0x00/*jmri.jmrit.display.layoutEditor.LayoutBlockManager.MASTTOMAST*/);
                        } catch (jmri.JmriException e){
                            JOptionPane.showMessageDialog(null, e.getMessage());
                                //Considered normal if not a valid through path
                                log.error(ref + " " + e.getMessage());
                                setNXButtonState(pd, NXBUTTONINACTIVE);
                                setNXButtonState(point, NXBUTTONINACTIVE);
                                return;
                        }
                        if (log.isDebugEnabled()){
                            for(LayoutBlock blk : routeDetails){
                                log.debug(blk.getDisplayName());
                            }
                        }

                        if(getEntryExitType()==FULLINTERLOCK){
                            activeEntryExit = true;
                        }
                        setRoute(true);
                    }
                }
            }
        }
    }
    
    static void flashSensor(PointDetails pd){
        for(SensorIcon si : pd.getPanel().sensorList){
            if(si.getSensor()==pd.getSensor()){
                si.flashSensor(2, Sensor.ACTIVE, Sensor.INACTIVE);
            }
        }
    }
    
    static void stopFlashSensor(PointDetails pd){
        for(SensorIcon si : pd.getPanel().sensorList){
            if(si.getSensor()==pd.getSensor()){
                si.stopFlash();
            }
        }
    }
    
    synchronized void setNXButtonState(PointDetails nxPoint, int state){
        if(nxPoint.getSensor()==null)
            return;
        if(state==NXBUTTONINACTIVE){
            //If a route is set to or from out point then we need to leave/set the sensor to ACTIVE
            if(nxPoint.isRouteToPointSet()){
                state=NXBUTTONACTIVE;
            } else if(nxPoint.isRouteFromPointSet()){
                state=NXBUTTONACTIVE;
            }
        }
        nxPoint.setNXState(state);
        int sensorState = Sensor.UNKNOWN;
        switch(state){
            case NXBUTTONINACTIVE : sensorState = Sensor.INACTIVE;
                                    break;
            case NXBUTTONACTIVE   : sensorState = Sensor.ACTIVE;
                                    break;
            case NXBUTTONSELECTED : sensorState = Sensor.ACTIVE;
                                    break;
            default               : sensorState = Sensor.UNKNOWN;
                                    break;
        }
        
        //Might need to clear listeners at the stage and then reapply them after.
        if(nxPoint.getSensor().getKnownState()!=sensorState){
            nxPoint.removeSensorList();
            try {
                nxPoint.getSensor().setKnownState(sensorState);
            } catch (jmri.JmriException ex){
                log.error(ex);
            }
            nxPoint.addSensorList();
        }
    }
    
    Sensor getSensorFromPoint(PointDetails point){
        if (point.getRefObject()==null)
            return null;
        if((point.getPanel()!=null) && (!point.getPanel().isEditable()) && (point.getSensor()!=null))
            return point.getSensor();
        
        if (point.getRefObject() instanceof Sensor)
            return (Sensor)point.getRefObject();
        Object objLoc = point.getRefLocation();
        Object objRef = point.getRefObject();
        SignalMast mast=null;
        SignalHead head=null;
        String username = "";
        String systemname = "";
        Sensor sensor = null;
        if(objRef instanceof SignalMast){
            mast = (SignalMast)objRef;
            username = mast.getUserName();
            systemname = mast.getSystemName();
        }
        if(objRef instanceof SignalHead){
            head = (SignalHead)objRef;
            username = head.getUserName();
            systemname = head.getSystemName();
        }
        jmri.SensorManager sm = InstanceManager.sensorManagerInstance();
        if (objLoc instanceof PositionablePoint){
            PositionablePoint p = (PositionablePoint)objLoc;
            if(mast!=null) {
                if((p.getEastBoundSignalMast().equals(username)) || 
                        p.getEastBoundSignalMast().equals(systemname))
                    sensor = sm.getSensor(p.getEastBoundSensor());
                else if((p.getWestBoundSignalMast().equals(username)) || 
                        p.getWestBoundSignalMast().equals(systemname))
                    sensor = sm.getSensor(p.getWestBoundSensor());
            }
            else if(head!=null) {
                if((p.getEastBoundSignal().equals(username)) || 
                        p.getEastBoundSignal().equals(systemname))
                    sensor = sm.getSensor(p.getEastBoundSensor());
                else if((p.getWestBoundSignal().equals(username)) || 
                        p.getWestBoundSignal().equals(systemname))
                    sensor = sm.getSensor(p.getWestBoundSensor());
            }
        } else if (objLoc instanceof LayoutTurnout) {
            LayoutTurnout t = (LayoutTurnout)objLoc;
            if(mast!=null){
                if((t.getSignalAMast().equals(username)) || (t.getSignalAMast().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorA());
                else if((t.getSignalBMast().equals(username)) || (t.getSignalBMast().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorB());
                else if((t.getSignalCMast().equals(username)) || (t.getSignalCMast().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorC());
                else if((t.getSignalDMast().equals(username)) || (t.getSignalDMast().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorD());
            }
            if(head!=null){
                if((t.getSignalA1Name().equals(username)) || (t.getSignalA1Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorA());
                else if((t.getSignalA2Name().equals(username)) || (t.getSignalA2Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorA());
                else if((t.getSignalA3Name().equals(username)) || (t.getSignalA3Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorA());
                else if((t.getSignalB1Name().equals(username)) || (t.getSignalB1Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorB());
                else if((t.getSignalB2Name().equals(username)) || (t.getSignalB2Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorB());
                else if((t.getSignalC1Name().equals(username)) || (t.getSignalC1Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorC());
                else if((t.getSignalC2Name().equals(username)) || (t.getSignalC2Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorC());
                else if((t.getSignalD1Name().equals(username)) || (t.getSignalD1Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorD());
                else if((t.getSignalD2Name().equals(username)) || (t.getSignalD2Name().equals(systemname)))
                    sensor = sm.getSensor(t.getSensorD());
            }
        } else if (objLoc instanceof LevelXing){
            LevelXing x = (LevelXing)objLoc;
            if(mast!=null){
                if((x.getSignalAMastName().equals(username)) || (x.getSignalAMastName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorAName());
                else if((x.getSignalBMastName().equals(username)) || (x.getSignalBMastName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorBName());
                else if((x.getSignalCMastName().equals(username)) || (x.getSignalCMastName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorCName());
                else if((x.getSignalDMastName().equals(username)) || (x.getSignalDMastName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorDName());
            }
            if(head!=null){
                if((x.getSignalAName().equals(username)) || (x.getSignalAName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorAName());
                else if((x.getSignalBName().equals(username)) || (x.getSignalBName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorBName());
                else if((x.getSignalCName().equals(username)) || (x.getSignalCName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorCName());
                else if((x.getSignalDName().equals(username)) || (x.getSignalDName().equals(systemname)))
                    sensor = sm.getSensor(x.getSensorDName());
            }
        }
        point.setSensor(sensor);
        return sensor;
    }
    
    NamedBean getSignalFromPoint(PointDetails point){
        if (point==null){
            log.error("Empty point past in get Signal From Point");
            return null;
        }
        if((point.getPanel()!=null) && (!point.getPanel().isEditable()) && (point.getSignalMast()!=null))
            return point.getSignalMast();
        if((point.getPanel()!=null) && (!point.getPanel().isEditable()) && (point.getSignalHead()!=null))
            return point.getSignalHead();
        jmri.SignalMastManager sm = InstanceManager.signalMastManagerInstance();
        jmri.SignalHeadManager sh = InstanceManager.signalHeadManagerInstance();
        NamedBean signal = null;
        
        if(point.getRefObject()==null) {
            log.error("Signal not found at point");
            return null;
        } else if (point.getRefObject() instanceof SignalMast){
            signal =  point.getRefObject();
            point.setSignalMast(((SignalMast)point.getRefObject()));
            return signal;
        } else if (point.getRefObject() instanceof SignalHead){
            signal =  point.getRefObject();
            point.setSignalHead(((SignalHead)point.getRefObject()));
            return signal;
        }

        
        Sensor sen = (Sensor) point.getRefObject();
        log.debug("looking at Sensor " + sen.getDisplayName());
        String username = sen.getUserName();
        String systemname = sen.getSystemName();
        if(point.getRefLocation() instanceof PositionablePoint){
            PositionablePoint p = (PositionablePoint)point.getRefLocation();
            if((p.getEastBoundSensor().equals(username)) || 
                    p.getEastBoundSensor().equals(systemname)){
                    
                if(!p.getEastBoundSignalMast().equals(""))
                    signal =  sm.getSignalMast(p.getEastBoundSignalMast());
                    
                else if(!p.getEastBoundSignal().equals(""))
                    signal =  sh.getSignalHead(p.getEastBoundSignal());
            }
            else if((p.getWestBoundSensor().equals(username)) || 
                    p.getWestBoundSensor().equals(systemname)){
                    
                if(!p.getWestBoundSignalMast().equals(""))
                    signal =  sm.getSignalMast(p.getWestBoundSignalMast());
                    
                else if(!p.getWestBoundSignal().equals(""))
                    signal =  sh.getSignalHead(p.getWestBoundSignal());
            }
        }
        if(point.getRefLocation() instanceof LayoutTurnout){
            LayoutTurnout t = (LayoutTurnout)point.getRefLocation();
            if(t.getSensorA().equals(username) || t.getSensorA().equals(systemname))
                if(!t.getSignalAMast().equals(""))
                    signal =  sm.getSignalMast(t.getSignalAMast());
                else if(!t.getSignalA1Name().equals(""))
                    signal =  sh.getSignalHead(t.getSignalA1Name());
                    
            else if(t.getSensorB().equals(username) || t.getSensorB().equals(systemname))
                if(!t.getSignalBMast().equals(""))
                    signal =  sm.getSignalMast(t.getSignalBMast());
                else if(!t.getSignalB1Name().equals(""))
                    signal =  sh.getSignalHead(t.getSignalB1Name());
                    
            else if(t.getSensorC().equals(username) || t.getSensorC().equals(systemname))
                if(!t.getSignalCMast().equals(""))
                    signal =  sm.getSignalMast(t.getSignalCMast());
                else if(!t.getSignalC1Name().equals(""))
                    signal =  sh.getSignalHead(t.getSignalC1Name());
                    
            else if(t.getSensorD().equals(username) || t.getSensorD().equals(systemname))
                if(!t.getSignalDMast().equals(""))
                    signal =  sm.getSignalMast(t.getSignalDMast());
                else if(!t.getSignalD1Name().equals(""))
                    signal =  sh.getSignalHead(t.getSignalD1Name());
        }
        
        else if(point.getRefLocation() instanceof LevelXing){
            LevelXing x = (LevelXing)point.getRefLocation();
            if(x.getSensorAName().equals(username) || x.getSensorAName().equals(systemname))
                if(!x.getSignalAMastName().equals(""))
                    signal =  sm.getSignalMast(x.getSignalAMastName());
                else if(!x.getSignalAName().equals(""))
                    signal =  sh.getSignalHead(x.getSignalAName());
                    
            else if(x.getSensorBName().equals(username) || x.getSensorBName().equals(systemname))
                if(!x.getSignalBMastName().equals(""))
                    signal =  sm.getSignalMast(x.getSignalBMastName());
                else if(!x.getSignalBName().equals(""))
                    signal =  sh.getSignalHead(x.getSignalBName());
                    
            else if(x.getSensorCName().equals(username) || x.getSensorCName().equals(systemname))
                if(!x.getSignalCMastName().equals(""))
                    signal =  sm.getSignalMast(x.getSignalCMastName());
                else if(!x.getSignalCName().equals(""))
                    signal =  sh.getSignalHead(x.getSignalCName());
                    
            else if(x.getSensorDName().equals(username) || x.getSensorDName().equals(systemname))
                if(!x.getSignalDMastName().equals(""))
                    signal =  sm.getSignalMast(x.getSignalDMastName());
                else if(!x.getSignalDName().equals(""))
                    signal =  sh.getSignalHead(x.getSignalDName());
        }
        if(signal instanceof SignalMast)
            point.setSignalMast(((SignalMast)signal));
        else if (signal instanceof SignalHead)
            point.setSignalHead(((SignalHead)signal));
        return signal;
    }
    
    PointDetails getPointDetails(Object obj, LayoutEditor panel){
        for (int i = 0; i<pointDetails.size(); i++){
            if ((pointDetails.get(i).getRefObject()==obj) && (pointDetails.get(i).getPanel()==panel)) {
                return pointDetails.get(i);
                
            }
        }
        return null;
    }
    
    /*
    * Returns either an existing point, or creates a new one as required.
    */
    PointDetails getPointDetails(LayoutBlock source, LayoutBlock destination, LayoutEditor panel){
        PointDetails newPoint = new PointDetails(source, destination);
        newPoint.setPanel(panel);
        for (int i = 0; i<pointDetails.size(); i++){
            if (pointDetails.get(i).equals(newPoint)){
                return pointDetails.get(i);
                
            }
        }
        //Not found so will add
        pointDetails.add(newPoint);
        return newPoint;
    }
    
    //No point in have multiple copies of what is the same thing.
    static ArrayList<PointDetails> pointDetails = new ArrayList<PointDetails>();
    
    public String getPointAsString(NamedBean obj, LayoutEditor panel){
        if (obj==null)
            return "null";
        PointDetails valid = getPointDetails(obj, panel);  //was just plain getPoint
        if(valid!=null){
            return valid.getDisplayName();
        }
        return "empty";
    }
    
    public void removePropertyChangeListener(PropertyChangeListener list, NamedBean obj, LayoutEditor panel){
        if (obj==null)
            return;
        PointDetails valid = getPointDetails(obj, panel);
        if(valid!=null){
            valid.removePropertyChangeListener(list);
        }
    }
    
    class PointDetails{
        //May want to look at putting a listener on the refLoc to listen to updates to blocks, signals and sensors attached to it
        LayoutEditor panel = null;
        LayoutBlock facing;
        LayoutBlock protecting;
        private NamedBean refObj;
        private Object refLoc;
        private Sensor sensor;
        private SignalMast signalmast;
        private SignalHead signalhead;
        
        Source sourceRoute;
        Hashtable<Source.DestinationPoints, Source> destinations = new Hashtable<Source.DestinationPoints, Source>(5);
        
        PointDetails(LayoutBlock facing, LayoutBlock protecting){
            this.facing=facing;
            this.protecting = protecting;
        }
        
        LayoutBlock getFacing(){ return facing; }
        LayoutBlock getProtecting(){ return protecting; }
        
        //This might be better off a ref to the source pointdetail.
        boolean routeToSet = false;
        void setRouteTo(boolean boo){
            routeToSet = boo;
        }
        
        boolean routeFromSet = false;
        void setRouteFrom(boolean boo){
            routeFromSet = boo;
        }
        
        void setPanel(LayoutEditor panel){
            this.panel = panel;
        }
        
        void setSensor(Sensor sen){
            if(sensor==sen)
                return;
            if(sensor!=null)
                sensor.removePropertyChangeListener(nxButtonListener);
            sensor = sen;
            sensor.addPropertyChangeListener(nxButtonListener);
        }
        
        void addSensorList(){
            sensor.addPropertyChangeListener(nxButtonListener);
        }
        
        void removeSensorList(){
            sensor.removePropertyChangeListener(nxButtonListener);
        }
        
        Sensor getSensor() { return sensor; }
        
        protected PropertyChangeListener nxButtonListener = new PropertyChangeListener() {
        //First off if we were inactive, and now active
            public void propertyChange(PropertyChangeEvent e) {
                if(!e.getPropertyName().equals("KnownState"))
                    return;
                int now = ((Integer) e.getNewValue()).intValue();
                int old = ((Integer) e.getOldValue()).intValue();
                
                if((old==Sensor.UNKNOWN) || (old==Sensor.INCONSISTENT)){
                    setButtonState(NXBUTTONINACTIVE);
                    return;
                }
                
                Source.DestinationPoints destPoint = null;
                
                for(Entry<Source.DestinationPoints, Source> dp: destinations.entrySet()){
                    destPoint = dp.getKey();
                    if(dp.getValue().getPoint().getNXState()==NXBUTTONSELECTED){
                        setButtonState(NXBUTTONSELECTED);
                        destPoint.activeBean(false);
                        return;
                    }
                }
                
                if(sourceRoute!=null){
                    if(now==Sensor.ACTIVE && getNXState()==NXBUTTONINACTIVE){
                        setButtonState(NXBUTTONSELECTED);
                        for(Entry<Source.DestinationPoints, Object> en : sourceRoute.revDestObject.entrySet()){
                            //Sensor sen = getSensorFromPoint(en.getKey().getPoint());
                            //Set a time out on the source sensor, so that if its state hasn't been changed, then we will clear it out.
                            if(!en.getKey().getUniDirection()){
                                if(en.getKey().getPoint().getNXState()==NXBUTTONSELECTED){
                                    sourceRoute.activeBean(en.getKey(), true);
                                }
                            }
                        }
                    } else if (now==Sensor.INACTIVE && getNXState()==NXBUTTONSELECTED){
                        //sensor inactive, nxbutton state was selected, going to set back to inactive - ie user cancelled button
                        setButtonState(NXBUTTONINACTIVE);
                    } else if (now==Sensor.INACTIVE && getNXState()==NXBUTTONACTIVE){
                        //Sensor gone inactive, while nxbutton was selected - potential start of user either clear route or setting another
                        setButtonState(NXBUTTONSELECTED);
                    }
                } else if (destPoint!=null){
                    //Button set as a destination but has no source, it has had a change in state
                    if(now==Sensor.ACTIVE){
                        //State now is Active will set flashing
                        setButtonState(NXBUTTONSELECTED);
                    } else if(getNXState()==NXBUTTONACTIVE){
                        //Sensor gone inactive while it was previosly active
                        setButtonState(NXBUTTONSELECTED);
                    } else if(getNXState()==NXBUTTONSELECTED){
                        //Sensor gone inactive while it was previously selected therefore will cancel
                        setButtonState(NXBUTTONINACTIVE);
                    }
                }
            }
        };
        
        void setSignalMast(SignalMast mast) {
            signalmast = mast;
        }
        
        void setSource(Source src){
            if(sourceRoute==src)
                return;
            sourceRoute=src;
        }
        
        void setDestination(Source.DestinationPoints srcdp, Source src){
            if(!destinations.containsKey(srcdp))
                destinations.put(srcdp, src);
        }
        
        void removeDestination(Source.DestinationPoints srcdp){
            destinations.remove(srcdp);
            if(sourceRoute==null && destinations.size()==0){
                stopFlashSensor(this);
                sensor.removePropertyChangeListener(nxButtonListener);
            }
        }
        
        void removeSource(Source src){
            sourceRoute=null;
            if(destinations.size()==0) {
                stopFlashSensor(this);
                sensor.removePropertyChangeListener(nxButtonListener);
            }
        }
        
        private int nxButtonState = NXBUTTONINACTIVE;
        
        void setButtonState(int state){
            setNXButtonState(this, state);
        }
        
        void setNXState(int state){
            if(state==nxButtonState)
                return;
            if(state==NXBUTTONSELECTED) {
                nxButtonTimeOut();
                flashSensor(this);
            } else {
                cancelNXButtonTimeOut();
                stopFlashSensor(this);
            }
            nxButtonState=state;
        }
        
        int getNXState(){
            return nxButtonState;
        }
        
        SignalMast getSignalMast() { return signalmast; }
        
        void setSignalHead(SignalHead head){
            signalhead = head;
        }
        
        SignalHead getSignalHead() { return signalhead; }
        
        LayoutEditor getPanel() { return panel; }
        
        void setRefObject(NamedBean refObs){
            refObj = refObs;
            if (panel!=null && refObj!=null){
                if (refObj instanceof SignalMast){
                    String mast = ((SignalMast)refObj).getUserName();
                    refLoc = panel.findPositionablePointByEastBoundSignalMast(mast);
                    if(refLoc==null)
                        refLoc = panel.findPositionablePointByWestBoundSignalMast(mast);
                    if(refLoc==null)
                        refLoc = panel.findLayoutTurnoutBySignalMast(mast);
                    if(refLoc==null)
                        refLoc = panel.findLevelXingBySignalMast(mast);
                    if(refLoc==null){
                        mast = ((SignalMast)refObj).getSystemName();
                        if(refLoc==null)
                            refLoc = panel.findPositionablePointByWestBoundSignalMast(mast);
                        if(refLoc==null)
                            refLoc = panel.findLayoutTurnoutBySignalMast(mast);
                        if(refLoc==null)
                            refLoc = panel.findLevelXingBySignalMast(mast);
                    }
                } else if (refObj instanceof Sensor) {
                    String sourceSensor = ((Sensor)refObj).getSystemName();
                    refLoc = panel.findPositionablePointByEastBoundSensor(sourceSensor);
                    if(refLoc==null)
                        refLoc = panel.findPositionablePointByWestBoundSensor(sourceSensor);
                    if(refLoc==null)
                        refLoc = panel.findLayoutTurnoutBySensor(sourceSensor);
                    if(refLoc==null)
                        refLoc = panel.findLevelXingBySensor(sourceSensor);
                    if(refLoc==null){
                        sourceSensor = ((Sensor)refObj).getUserName();
                        refLoc = panel.findPositionablePointByEastBoundSensor(sourceSensor);
                        if(refLoc==null)
                            refLoc = panel.findPositionablePointByWestBoundSensor(sourceSensor);
                        if(refLoc==null)
                            refLoc = panel.findLayoutTurnoutBySensor(sourceSensor);
                        if(refLoc==null)
                            refLoc = panel.findLevelXingBySensor(sourceSensor);
                    }
                    setSensor((Sensor)refObj);
                } else if (refObj instanceof SignalHead){
                    String signal = ((SignalHead)refObj).getDisplayName();
                    refLoc = panel.findPositionablePointByEastBoundSignal(signal);
                    if(refLoc==null)
                        refLoc = panel.findPositionablePointByWestBoundSignal(signal);
                }
            }
            if (refLoc!=null){
                if(refLoc instanceof PositionablePoint){
                    //((PositionablePoint)refLoc).addPropertyChangeListener(this);
                } else if (refLoc instanceof LayoutTurnout){
                    //((LayoutTurnout)refLoc).addPropertyChangeListener(this);
                } else if (refLoc instanceof LevelXing){
                    //((LevelXing)refLoc).addPropertyChangeListener(this);
                }
            }
            //With this set ref we can probably add a listener to it, so that we can detect when a change to the point details takes place
        }
        
        NamedBean getRefObject(){ return refObj; }
        
        Object getRefLocation() { return refLoc; }
        
        LayoutEditor getLayoutEditor() { return panel; }
        
        boolean isRouteToPointSet() { return routeToSet; }
        boolean isRouteFromPointSet() { return routeFromSet; }
        
        String getDisplayName(){
            if(refObj instanceof SignalMast){
                return ((SignalMast)refObj).getDisplayName();
            } else if (refObj instanceof Sensor) {
                return ((Sensor)refObj).getDisplayName();
            } else if (refObj instanceof SignalHead){
                return ((SignalHead)refObj).getDisplayName();
            }
            return "no display name";
        }
        
        Thread nxButtonTimeOutThr;
        
        void nxButtonTimeOut(){
            if((nxButtonTimeOutThr!=null) && (nxButtonTimeOutThr.isAlive())){
                return;
            }
            extendedtime = true;
            class ButtonTimeOut implements Runnable {
                ButtonTimeOut(PointDetails pd){
                    this.pd=pd;
                }
                PointDetails pd;
                public void run() {
                    try {
                        //Stage one default timer for the button if no other button has been pressed
                        Thread.sleep(nxButtonTimeout*1000);
                        //Stage two if an extended time out has been requested
                        if(extendedtime){
                            Thread.sleep(60000);  //timeout after a minute waiting for the sml to set.
                        }
                    } catch (InterruptedException ex) {
                        log.debug("Flash timer cancelled");
                    }
                    instance().setNXButtonState(pd, NXBUTTONINACTIVE);
                }
            }
            ButtonTimeOut t = new ButtonTimeOut(this);
            nxButtonTimeOutThr = new Thread(t, "NX Button Timeout " + getSensor().getDisplayName());
            
            nxButtonTimeOutThr.start();
        }
        
        void cancelNXButtonTimeOut(){
            if(nxButtonTimeOutThr!=null)
                nxButtonTimeOutThr.interrupt();
        
        }
        
        boolean extendedtime = false;
        
        @Override
        public boolean equals(Object obj){
            if(obj ==this)
                return true;
            if(obj ==null)
                return false;
                
                if(!(getClass()==obj.getClass()))
                    return false;
                else{
                    PointDetails tmp = (PointDetails)obj;
                    if(tmp.getFacing()!=this.facing)
                        return false;
                    if(tmp.getProtecting()!=this.protecting)
                        return false;
                    if(tmp.getLayoutEditor()!=this.panel)
                        return false;
                }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.panel != null ? this.panel.hashCode() : 0);
            hash = 37 * hash + (this.facing != null ? this.facing.hashCode() : 0);
            hash = 37 * hash + (this.protecting != null ? this.protecting.hashCode() : 0);
            return hash;
        }
        
        java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
        public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
    }
    
    
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	
    boolean runWhenStablised = false;
    LayoutEditor toUseWhenStable;
    int interlockTypeToUseWhenStable;
    
    /**
    * Discover all possible valid source and destination signalmasts past pairs 
    * on all layout editor panels.
    */
    public void automaticallyDiscoverEntryExitPairs(LayoutEditor editor, int interlockType) throws JmriException{
        //This is almost a duplicate of that in the DefaultSignalMastLogicManager
        runWhenStablised=false;
        jmri.jmrit.display.layoutEditor.LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if(!lbm.isAdvancedRoutingEnabled()){
            throw new JmriException("advanced routing not enabled");
        }
        if(!lbm.routingStablised()){
            runWhenStablised=true;
            toUseWhenStable=editor;
            interlockTypeToUseWhenStable = interlockType;
            log.debug("Layout block routing has not yet stabilsed, discovery will happen once it has");
            return;
        }
        Hashtable<NamedBean, ArrayList<NamedBean>> validPaths = lbm.getLayoutBlockConnectivityTools().discoverValidBeanPairs(editor, Sensor.class);
        Enumeration<NamedBean> en = validPaths.keys();
        EntryExitPairs eep = EntryExitPairs.instance();
        while (en.hasMoreElements()) {
            NamedBean key = en.nextElement();
            ArrayList<NamedBean> validDestMast = validPaths.get(key);
            if(validDestMast.size()>0){
                eep.addNXSourcePoint(key, editor);
                for(int i = 0; i<validDestMast.size(); i++){
                    if(!eep.isDestinationValid(key, validDestMast.get(i), editor)){
                        eep.addNXDestination(key, validDestMast.get(i), editor);
                        eep.setEntryExitType(key, editor, validDestMast.get(i), interlockType);
                    }
                }
            }
        }
        
        firePropertyChange("autoGenerateComplete", null, null);
    }
    
    protected PropertyChangeListener propertyBlockManagerListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent e) {
            if(e.getPropertyName().equals("topology")){
                //boolean newValue = new Boolean.parseBoolean(String.valueOf(e.getNewValue()));
                boolean newValue = (Boolean) e.getNewValue();
                if(newValue){
                    if(runWhenStablised){
                        try {
                           automaticallyDiscoverEntryExitPairs(toUseWhenStable, interlockTypeToUseWhenStable);
                        } catch (JmriException je){
                            //Considered normal if routing not enabled
                        }
                    }
                }
            }
        }
    };
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EntryExitPairs.class.getName());
}