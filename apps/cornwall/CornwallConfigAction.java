// CornwallConfigAction.java

package apps.cornwall;

import apps.*;

/**
 * Swing action to create CornwallConfigFrame
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.1 $
 */
public class CornwallConfigAction extends AbstractConfigAction {

    protected AbstractConfigFile readFile(String name)
        throws org.jdom.JDOMException, java.io.FileNotFoundException {
        CornwallConfigFile file = new CornwallConfigFile();
        if (name!=null) {
            log.debug("using file "+name);
            file.readFile(name);
        } else {
            log.debug("for default file, use "+file.defaultConfigFilename());
            file.readFile(file.defaultConfigFilename());
        }
        return file;
    }
    protected AbstractConfigFrame newFrame(String name){
        return new CornwallConfigFrame(name);
    }

    /**
     * Create an action object, reading configuration with the
     * default filename.
     */
    public CornwallConfigAction(String actionName) {
        super(actionName);
    }

    /**
     * Create an action object, using a specific filename for
     * configuration information.
     */
    public CornwallConfigAction(String actionName, String fileName) {
        super(actionName, fileName);
    }

    /** not finding a file or having a config fail isn't
     *  really an error; record it for later
     */
    protected void configFailed() {
        super.configFailed();
    }

    protected void readFailed(Exception e) {
        super.readFailed(e);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CornwallConfigAction.class.getName());
}

