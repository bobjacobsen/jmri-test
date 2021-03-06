package jmri.jmrix.pi.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;

/**
 * Provides load and store functionality for
 * configuring RaspberryPiSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author  Paul Bender Copyright (c) 2003
 * @version $Revision$
 */
public class RaspberryPiSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public RaspberryPiSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.pi.configurexml.RaspberryPiSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutManagerXml.class.getName());

}
