// TrainManager.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;

/**
 * Manages trains.
 * @author      Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010
 * @version	$Revision: 1.34 $
 */
public class TrainManager implements java.beans.PropertyChangeListener {
	
	// Train frame attributes
	protected String _sortBy = "";				// Trains frame sort radio button
	protected String _trainAction = TrainsTableFrame.MOVE;	// Trains frame table button action
	protected boolean _buildMessages = true;	// when true, show build messages
	protected boolean _buildReport = false;		// when true, print/preview build reports
	protected boolean _printPreview = false;	// when true, preview train manifest
	protected TrainsTableFrame _trainFrame = null;
	protected Dimension _frameDimension = new Dimension(Control.panelWidth,Control.panelHeight);
	protected Point _framePosition = new Point();
	// Train frame table column widths (12), starts with Time column and ends with Edit
	protected int[] _tableColumnWidths = {40, 38, 72, 100, 120, 100, 100, 100, 100, 100, 66, 60};
	
	// Edit Train frame attributes
	protected TrainEditFrame _trainEditFrame = null;
	protected Dimension _editFrameDimension = null;
	protected Point _editFramePosition = null;
	
	// property changes
	public static final String LISTLENGTH_CHANGED_PROPERTY = "TrainsListLength";
	public static final String PRINTPREVIEW_CHANGED_PROPERTY = "TrainsPrintPreview";
	public static final String TRAIN_ACTION_CHANGED_PROPERTY = "TrainsAction";
	
	public TrainManager() {
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		//CarLoads.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
    }
    
	/** record the single instance **/
	private static TrainManager _instance = null;
	private static int _id = 0;		// train ids
	private static boolean trainsloaded = false;

	public static synchronized TrainManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainManager creating instance");
			// create and load
			_instance = new TrainManager();
			OperationsXml.instance();				// load setup
			TrainManagerXml.instance();				// load trains
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainManager returns instance "+_instance);
		return _instance;
	}

	public void setTrainsLoaded(){
		trainsloaded = true;
		log.debug("Trains have been loaded!");
	}
 
	/**
	 * 
	 * @return true if build messages are enabled
	 */
    public boolean getBuildMessages(){
    	return _buildMessages;
    }
    
    public void setBuildMessages(boolean messages){
    	_buildMessages = messages;
    }
    
    /**
     * 
     * @return true if build reports are enabled
     */
    public boolean getBuildReport(){
    	return _buildReport;
    }
    
    public void setBuildReport(boolean report){
    	_buildReport = report;
    }
    
    /**
     * 
     * @return true if print preview is enabled
     */
    public boolean getPrintPreview(){
    	return _printPreview;
    }
    
    public void setPrintPreview(boolean preview){
    	boolean old = _printPreview;
    	_printPreview = preview;
    	firePropertyChange(PRINTPREVIEW_CHANGED_PROPERTY, old?"Preview":"Print", preview?"Preview":"Print");
    }
    
    public void setTrainsFrame(TrainsTableFrame frame){
    	_trainFrame = frame;
    }
    
    public Dimension getTrainsFrameSize(){
    	return _frameDimension;
    }
    
    public Point getTrainsFramePosition(){
    	return _framePosition;
    }
    
    public String getTrainsFrameSortBy (){
    	return _sortBy;
    }
   
    public void setTrainsFrameSortBy(String sortBy){
    	_sortBy = sortBy;
    }
    
    public String getTrainsFrameTrainAction (){
    	return _trainAction;
    }
   
    public void setTrainsFrameTrainAction(String action){
    	String old = _trainAction;
    	_trainAction = action;
    	firePropertyChange(TRAIN_ACTION_CHANGED_PROPERTY, old, action);
    }
    
    /**
     * 
     * @return get an array of table column widths for the trains frame
     */
    public int[] getTrainsFrameTableColumnWidths(){
    	return _tableColumnWidths;
    }
    
    public void setTrainsFrameTableColumnWidths(int[] tableColumnWidths){
    	_tableColumnWidths = tableColumnWidths;
    }
    
    public void setTrainEditFrame(TrainEditFrame frame){
    	_trainEditFrame = frame;
    }
    
    public Dimension getTrainEditFrameSize(){
    	return _editFrameDimension;
    }
    
    public Point getTrainEditFramePosition(){
    	return _editFramePosition;
    }
	
	public void dispose() {
    	CarTypes.instance().removePropertyChangeListener(this);
    	CarRoads.instance().removePropertyChangeListener(this);
    	//CarLoads.instance().removePropertyChangeListener(this);
    	EngineTypes.instance().removePropertyChangeListener(this);
        _trainHashTable.clear();
        _instance = null;
        _id = 0;
    }
	
	//	 stores known Train instances by id
    protected Hashtable<String, Train> _trainHashTable = new Hashtable<String, Train>();   

    /**
     * @return requested Train object or null if none exists
     */
     
    public Train getTrainByName(String name) {
		if (!trainsloaded)
			log.error("TrainManager getTrainByName called before trains completely loaded!");
    	Train train;
    	Enumeration<Train> en =_trainHashTable.elements();
    	for (int i = 0; i < _trainHashTable.size(); i++){
    		train = en.nextElement();
    		// windows file names are case independent
    		if (train.getName().toLowerCase().equals(name.toLowerCase()))
    			return train;
      	}
    	log.debug("train "+name+" doesn't exist");
        return null;
    }
    
    public Train getTrainById (String id){
		if (!trainsloaded)
			log.error("TrainManager getTrainById called before trains completely loaded!");
    	return _trainHashTable.get(id);
    }
 
    /**
     * Finds an existing train or creates a new train if needed
     * requires train's name creates a unique id for this train
     * @param name
     * 
     * @return new train or existing train
     */
    public Train newTrain (String name){
    	Train train = getTrainByName(name);
    	if (train == null){
    		_id++;						
    		train = new Train(Integer.toString(_id), name);
    		Integer oldSize = new Integer(_trainHashTable.size());
    		_trainHashTable.put(train.getId(), train);
    		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
    	}
    	return train;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
 	 */
    public void register(Train train) {
    	Integer oldSize = new Integer(_trainHashTable.size());
        _trainHashTable.put(train.getId(), train);
        // find last id created
        int id = Integer.parseInt(train.getId());
        if (id > _id)
        	_id = id;
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     */
    public void deregister(Train train) {
    	if (train == null)
    		return;
        train.dispose();
        Integer oldSize = new Integer(_trainHashTable.size());
    	_trainHashTable.remove(train.getId());
        firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, new Integer(_trainHashTable.size()));
    }
    
    public void replaceType(String oldType, String newType){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			if (train.acceptsTypeName(oldType)){
				train.deleteTypeName(oldType);
				train.addTypeName(newType);
			}
		}
    }
    
    public void replaceLoad(String oldLoadName, String newLoadName){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			String[] loadNames = train.getLoadNames();
			for (int j=0; j<loadNames.length; j++){
				if (loadNames[j].equals(oldLoadName)){
					train.deleteLoadName(oldLoadName);
					if (newLoadName != null)
						train.addLoadName(newLoadName);
				}
			}
		}
    }
    
	public void replaceRoad(String oldRoad, String newRoad){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			String[] roadNames = train.getRoadNames();
			for (int j=0; j<roadNames.length; j++){
				if (roadNames[j].equals(oldRoad)){
					train.deleteRoadName(oldRoad);
					if (newRoad != null)
						train.addRoadName(newRoad);
				}
			}
			if (train.getEngineRoad().equals(oldRoad))
				train.setEngineRoad(newRoad);
			if (train.getCabooseRoad().equals(oldRoad))
				train.setCabooseRoad(newRoad);
		}
	}
	
	/**
	 * 
	 * @return true if there are any trains built
	 */
	public boolean getAnyTrainBuilt(){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			if (train.getBuilt())
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param car
	 * @return Train that can service car from its current
	 * location to the its destination.
	 */
	public Train getTrainForCar(Car car){
		List<String> trains = getTrainsByIdList();
		for (int i=0; i<trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			// does this train service this car?
			if (train.servicesCar(car))
				return train;
		}
		return null;
	}

   /**
     * Sort by train name
     * @return list of train ids ordered by name
     */
    public List<String> getTrainsByNameList() {
    	return getTrainsByList(getList(), GET_TRAIN_NAME);
	}
    
    /**
     * Sort by train departure time
     * @return list of train ids ordered by departure time
     */
    public List<String> getTrainsByTimeList() {
    	return getTrainsByIntList(getTrainsByNameList(), GET_TRAIN_TIME);
	}
    
    /**
     * Sort by train departure name
     * @return list of train ids ordered by departure name
     */
    public List<String> getTrainsByDepartureList() {
    	return getTrainsByList(getTrainsByNameList(), GET_TRAIN_DEPARTES_NAME);
	}
    
    /**
     * Sort by train termination name
     * @return list of train ids ordered by termination name
     */
    public List<String> getTrainsByTerminatesList() {
    	return getTrainsByList(getTrainsByNameList(), GET_TRAIN_TERMINATES_NAME);
	}
    
    /**
     * Sort by train route name
     * @return list of train ids ordered by route name
     */
    public List<String> getTrainsByRouteList() {
    	return getTrainsByList(getTrainsByNameList(), GET_TRAIN_ROUTE_NAME);
	}
       
    /**
	 * Sort by train id
	 * @return list of train ids ordered by id
	 */
    public List<String> getTrainsByIdList() {
    	return getTrainsByIntList(getList(), GET_TRAIN_ID);
    }
    
    private List<String> getTrainsByList(List<String> sortList, int attribute){
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < sortList.size(); i++) {
			boolean trainAdded = false;
			Train train = getTrainById(sortList.get(i));
			String inTrainAttribute = (String)getTrainAttribute(train, attribute);
			for (int j = 0; j < out.size(); j++) {
				train = getTrainById(out.get(j));
				String outTrainAttribute = (String)getTrainAttribute(train, attribute);
				if (inTrainAttribute.compareToIgnoreCase(outTrainAttribute) < 0) {
					out.add(j,sortList.get(i));
					trainAdded = true;
					break;
				}
			}
			if (!trainAdded) {
				out.add(sortList.get(i));
			}
		}
		return out;
    }
    
    private List<String> getTrainsByIntList(List<String> sortList, int attribute) {
    	List<String> out = new ArrayList<String>();
    	for (int i=0; i<sortList.size(); i++){
    		boolean trainAdded = false;
    		Train train = getTrainById (sortList.get(i));
    		int inTrainAttribute = (Integer)getTrainAttribute(train, attribute); 
    		for (int j=0; j<out.size(); j++ ){
    			train = getTrainById (out.get(j));
    			int outTrainAttribute = (Integer)getTrainAttribute(train, attribute);
    			if (inTrainAttribute < outTrainAttribute){
    				out.add(j,sortList.get(i));
    				trainAdded = true;
    				break;
    			}
    		}
    		if (!trainAdded){
    			out.add(sortList.get(i));
    		}
    	}
        return out;
    }
    
    // the various sort options for trains
    private static final int GET_TRAIN_DEPARTES_NAME = 0;
    private static final int GET_TRAIN_NAME = 1;
    private static final int GET_TRAIN_ROUTE_NAME = 2;
    private static final int GET_TRAIN_TERMINATES_NAME = 3;
    private static final int GET_TRAIN_TIME = 4;
    private static final int GET_TRAIN_ID = 5;
      
    private Object getTrainAttribute(Train train, int attribute){
    	switch (attribute){
    	case GET_TRAIN_DEPARTES_NAME: return train.getTrainDepartsName();
    	case GET_TRAIN_NAME: return train.getName();
    	case GET_TRAIN_ROUTE_NAME: return train.getTrainRouteName();
    	case GET_TRAIN_TERMINATES_NAME: return train.getTrainTerminatesName(); 
       	case GET_TRAIN_TIME: return train.getDepartTimeMinutes();
    	case GET_TRAIN_ID: return Integer.parseInt(train.getId());
    	default: return "unknown";
    	}
    }
 
    private List<String> getList() {
		if (!trainsloaded)
			log.error("TrainManager getList called before trains completely loaded!");
        List<String> out = new ArrayList<String>();
        Enumeration<String> en = _trainHashTable.keys();
        String[] arr = new String[_trainHashTable.size()];
        int i=0;
        while (en.hasMoreElements()) {
            arr[i] = en.nextElement();
            i++;
        }
        jmri.util.StringUtil.sort(arr);
        for (i=0; i<arr.length; i++) out.add(arr[i]);
        return out;
    }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
    	box.addItem("");
		List<String> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++){
			Train train = getTrainById(trains.get(i));
			box.addItem(train);
		}
    }
    
    /**
     * Report that the train, car and engine databases are dirty.
     */
    public void setFilesDirty(){
    	log.debug("train files dirty");
    	CarManagerXml.instance().setDirty(true);
    	EngineManagerXml.instance().setDirty(true);
    	TrainManagerXml.instance().setDirty(true);
    }
    
    /**
     * Save all xml files that a train can modify.
     */
    public void save(){
		LocationManagerXml.instance().writeFileIfDirty();		//Need to save "moves" for track location 
		RouteManagerXml.instance().writeFileIfDirty(); 			//Only if user used setX&Y
		CarManagerXml.instance().writeFileIfDirty();			//save train assignments		
		EngineManagerXml.instance().writeFileIfDirty();			//save train assignments
		TrainManagerXml.instance().writeOperationsTrainFile();	//save train changes
    }
  
    /**
     * @return Number of trains
     */
    public int numEntries() { return _trainHashTable.size(); }
    
    public void options (org.jdom.Element values) {
    	if (log.isDebugEnabled()) log.debug("ctor from element "+values);
    	Element e = values.getChild("trainOptions");
    	if (e != null){
    		org.jdom.Attribute a;
    		if ((a = e.getAttribute("sortBy")) != null)
    			_sortBy = a.getValue();
    		if ((a = e.getAttribute("buildMessages")) != null)
    			_buildMessages = a.getValue().equals("true");
    		if ((a = e.getAttribute("buildReport")) != null)
    			_buildReport = a.getValue().equals("true");
    		if ((a = e.getAttribute("printPreview")) != null)
    			_printPreview = a.getValue().equals("true");
       		if ((a = e.getAttribute("trainAction")) != null)
    			_trainAction = a.getValue();
       		// determine panel position
    		int x = 0;
    		int y = 0;
    		int height = Control.panelHeight;
    		int width = Control.panelWidth;
    		try {
    			x = e.getAttribute("x").getIntValue();
    			y = e.getAttribute("y").getIntValue();
    			height = e.getAttribute("height").getIntValue();
    			width = e.getAttribute("width").getIntValue();
    			_frameDimension = new Dimension(width, height);
    			_framePosition = new Point(x,y);
    		} catch ( org.jdom.DataConversionException ee) {
    			log.debug("Did not find train frame attributes");
    		} catch ( NullPointerException ne) {
    			log.debug("Did not find train frame attributes");
    		}
    		if ((a = e.getAttribute("columnWidths")) != null){
             	String[] widths = a.getValue().split(" ");
             	for (int i=0; i<widths.length; i++){
             		try{
             			_tableColumnWidths[i] = Integer.parseInt(widths[i]);
             		} catch (NumberFormatException ee){
             			log.error("Number format exception when reading trains column widths");
             		}
             	}
    		}
    	}
    	// get Train Edit attributes
    	e = values.getChild("trainEditOptions");
    	if (e != null){
    		try {
    			int x = e.getAttribute("x").getIntValue();
    			int y = e.getAttribute("y").getIntValue();
    			int height = e.getAttribute("height").getIntValue();
    			int width = e.getAttribute("width").getIntValue();
    			_editFrameDimension = new Dimension(width, height);
    			_editFramePosition = new Point(x,y);
    		} catch ( org.jdom.DataConversionException ee) {
    			log.debug("Did not find train edit frame attributes");
    		} catch ( NullPointerException ne) {
    			log.debug("Did not find train edit frame attributes");
    		}
    	}
    }

    
    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in operations-trains.dtd.
     * @return Contents in a JDOM Element
     */
    public org.jdom.Element store() {
    	Element values = new Element("options");
        org.jdom.Element e = new org.jdom.Element("trainOptions");
        e.setAttribute("sortBy", getTrainsFrameSortBy());
        e.setAttribute("buildMessages", getBuildMessages()?"true":"false");
        e.setAttribute("buildReport", getBuildReport()?"true":"false");
        e.setAttribute("printPreview", getPrintPreview()?"true":"false");
        e.setAttribute("trainAction", getTrainsFrameTrainAction());
        // get previous Train frame size and position
        Dimension size = getTrainsFrameSize();
        Point posn = getTrainsFramePosition();
        if (_trainFrame != null){
        	size = _trainFrame.getSize();
        	posn = _trainFrame.getLocation();
        	_frameDimension = size;
        	_framePosition = posn;
        }
        e.setAttribute("x", ""+posn.x);
        e.setAttribute("y", ""+posn.y);
        e.setAttribute("height", ""+size.height);
        e.setAttribute("width", ""+size.width);
        // convert column widths to strings
        String columnWidths = "";
        for (int i=0; i<_tableColumnWidths.length; i++){
        	columnWidths += Integer.toString(_tableColumnWidths[i])+" ";
        }
        e.setAttribute("columnWidths", columnWidths);
        values.addContent(e);
        // now save Train Edit frame size and position
        e = new org.jdom.Element("trainEditOptions");
        size = getTrainEditFrameSize();
        posn = getTrainEditFramePosition();
        if (_trainEditFrame != null){
        	size = _trainEditFrame.getSize();
        	posn = _trainEditFrame.getLocation();
        	_editFrameDimension = size;
        	_editFramePosition = posn;
        }
        if (posn != null){
        	e.setAttribute("x", ""+posn.x);
        	e.setAttribute("y", ""+posn.y);
        }
        if (size != null){
        	e.setAttribute("height", ""+size.height);
        	e.setAttribute("width", ""+size.width); 
        }
        values.addContent(e);
        return values;
    }
    
	/**
	 * Check for car type and road name replacements. Also check for engine type
	 * replacement.
	 * 
	 */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	log.debug("TrainManager sees property change: " + e.getPropertyName() + " old: " + e.getOldValue() + " new " + e.getNewValue());
    	if (e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY) ||
    			e.getPropertyName().equals(EngineTypes.ENGINETYPES_NAME_CHANGED_PROPERTY)){
    		replaceType((String)e.getOldValue(), (String)e.getNewValue());
    	}
    	if (e.getPropertyName().equals(CarRoads.CARROADS_NAME_CHANGED_PROPERTY)){
    		replaceRoad((String)e.getOldValue(), (String)e.getNewValue());
    	}
    	// TODO use listener to determine if load name has changed
       	//if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)){
    	//	replaceLoad((String)e.getOldValue(), (String)e.getNewValue());
    	//}
    }
    
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainManager.class.getName());

}

/* @(#)TrainManager.java */
