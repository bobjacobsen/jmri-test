package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.QuadOutputSignalHead;
import jmri.Turnout;

import jmri.util.NamedBeanHandle;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for QuadOutputSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 1.3 $
 */
public class QuadOutputSignalHeadXml extends TripleTurnoutSignalHeadXml {

    public QuadOutputSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * QuadOutputSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        QuadOutputSignalHead p = (QuadOutputSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        
        element.addContent(addTurnoutElement(p.getGreen(), "green"));
        element.addContent(addTurnoutElement(p.getYellow(), "yellow"));
        element.addContent(addTurnoutElement(p.getRed(), "red"));
        element.addContent(addTurnoutElement(p.getLunar(), "lunar"));

        return element;
    }

    /**
     * Create a QuadOutputSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnoutname");
        if (l.size() == 0) l = element.getChildren("turnout");
        NamedBeanHandle<Turnout> green = loadTurnout(l.get(0));
        NamedBeanHandle<Turnout> yellow = loadTurnout(l.get(1));
        NamedBeanHandle<Turnout> red = loadTurnout(l.get(2));
        NamedBeanHandle<Turnout> lunar = loadTurnout(l.get(3));
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        SignalHead h;
        if (a == null)
            h = new QuadOutputSignalHead(sys, green, yellow, red, lunar);
        else
            h = new QuadOutputSignalHead(sys, a.getValue(), green, yellow, red, lunar);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TripleTurnoutSignalHeadXml.class.getName());
}