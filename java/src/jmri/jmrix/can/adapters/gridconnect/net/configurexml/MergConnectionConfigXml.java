package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.can.adapters.gridconnect.net.MergConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.net.MergNetworkDriverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the
 * NetworkDriverAdapter (and connections).
 * <P>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 19698 $
 */
public class MergConnectionConfigXml extends ConnectionConfigXml {

    public MergConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        adapter = new MergNetworkDriverAdapter();
    }

    protected void getInstance(Object object) {
        adapter = ((MergConnectionConfig) object).getAdapter();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new MergConnectionConfig(adapter));
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(MergConnectionConfigXml.class.getName());

}
