// SerialLightManagerXml.java
package jmri.jmrix.secsi.configurexml;

import jmri.jmrix.secsi.SerialLightManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring SerialLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <P>
 * Based on SerialTurnoutManagerXml.java
 *
 * @author Dave Duchamp Copyright (c) 2004, 2007, 2008
 * @version $Revision$
 */
public class SerialLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.secsi.configurexml.SerialLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // create the master object
        SerialLightManager.instance();
        // load individual lights
        return loadLights(lights);
    }

    static Logger log = LoggerFactory.getLogger(SerialLightManagerXml.class.getName());
}
