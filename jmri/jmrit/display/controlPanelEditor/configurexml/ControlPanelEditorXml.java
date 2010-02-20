package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.Positionable;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JFrame;

import java.util.List;
import org.jdom.*;

/**
 * Handle configuration for {@link ControlPanelEditor} panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.8 $
 */
public class ControlPanelEditorXml extends AbstractXmlAdapter {

    public ControlPanelEditorXml() {}

    /**
     * Default implementation for storing the contents of a
     * ControlPanelEditor
     * @param o Object to store, of type ControlPanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        ControlPanelEditor p = (ControlPanelEditor)o;
        Element panel = new Element("paneleditor");

        JFrame frame = p.getTargetFrame();
        Dimension size = frame.getSize();
        Point posn = frame.getLocation();

        panel.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.configurexml.ControlPanelEditorXml");
        panel.setAttribute("name", ""+frame.getName());
        panel.setAttribute("x", ""+posn.x);
        panel.setAttribute("y", ""+posn.y);
        panel.setAttribute("height", ""+size.height);
        panel.setAttribute("width", ""+size.width);
        panel.setAttribute("editable", ""+(p.isEditable()?"yes":"no"));
        panel.setAttribute("positionable", ""+(p.allPositionable()?"yes":"no"));
        panel.setAttribute("showcoordinates", ""+(p.showCoordinates()?"yes":"no"));
        panel.setAttribute("showtooltips", ""+(p.showTooltip()?"yes":"no"));
        panel.setAttribute("controlling", ""+(p.allControlling()?"yes":"no"));
        panel.setAttribute("hide", p.isVisible()?"no":"yes");
        panel.setAttribute("panelmenu", frame.getJMenuBar().isVisible()?"yes":"no");
        panel.setAttribute("scrollable", p.getScrollable());

        // include contents
        List <Positionable> contents = p.getContents();
        if (log.isDebugEnabled()) log.debug("N elements: "+contents.size());
        for (int i=0; i<contents.size(); i++) {
            Positionable sub = contents.get(i);
            if (sub!=null && sub.storeItem()) {
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e!=null) panel.addContent(e);
                } catch (Exception e) {
                    log.error("Error storing panel element: "+e);
                    e.printStackTrace();
                }
            }
        }

        return panel;
    }


    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a ControlPanelEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
    	boolean result = true;
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert ControlPanelEditor's attribute");
            result = false;
        }
        // find the name
        String name = "Control Panel";
        if (element.getAttribute("name")!=null)
            name = element.getAttribute("name").getValue();
        // confirm that panel hasn't already been loaded
        if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
        	log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
        	result = false;
        }
        ControlPanelEditor panel = new ControlPanelEditor(name);
		jmri.jmrit.display.PanelMenu.instance().addEditorPanel(panel);
        panel.getTargetFrame().setLocation(x,y);
        panel.getTargetFrame().setSize(width,height);
        
        // load the contents
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via "+adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                // and do it
                adapter.setMajorRelease(getMajorRelease());
                adapter.setMinorRelease(getMinorRelease());
                adapter.setTestRelease(getTestRelease());
                adapter.load(item, panel);
            } catch (Exception e) {
                log.error("Exception while loading "+item.getName()+":"+e);
                result = false;
                e.printStackTrace();
            }
        }

        // Set contents state.
        // This has to be done after the items are
        // loaded, because it will over-write various
        // controls within them.
        Attribute a;
        boolean value = true;
        if ((a = element.getAttribute("editable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllEditable(value);

        value = true;
        if ((a = element.getAttribute("positionable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllPositionable(value);
        
        value = false;
        if ((a = element.getAttribute("showcoordinates"))!=null && a.getValue().equals("yes"))
            value = true;
        panel.setShowCoordinates(value);

        value = true;
        if ((a = element.getAttribute("showtooltips"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllShowTooltip(value);

        value = true;
        if ((a = element.getAttribute("controlling"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllControlling(value);

        value = false;
        if ((a = element.getAttribute("hide"))!=null && a.getValue().equals("yes"))
            value = true;
        panel.setShowHidden(value);

        value = true;
        if ((a = element.getAttribute("panelmenu"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setPanelMenu(value);

        String state = new String("both");
        if ((a = element.getAttribute("scrollable"))!=null)
            state = a.getValue();
        panel.setScroll(state);

        // display the results, with the editor in back
        panel.pack();

        // we don't pack the target frame here, because size was specified
        // TODO: Work out why, when calling this method, panel size is increased
        // vertically (at least on MS Windows)
        panel.getTargetFrame().setVisible(true);    // always show the panel

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

        // reset the size and position, in case the display caused it to change
        panel.getTargetFrame().setLocation(x,y);
        panel.getTargetFrame().setSize(width,height);
        panel.setTitle();
        panel.initView();
        return result;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControlPanelEditorXml.class.getName());

}