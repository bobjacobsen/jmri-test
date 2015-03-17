// ReportAction.java
package jmri.jmrit.mailreport;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a ReportFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 */
public class ReportAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2713560182288676071L;

    public ReportAction(String s) {
        super(s);
    }

    public ReportAction() {
        this(java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle").getString("Name"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ReportFrame f = new ReportFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception in startup", ex);
        }
        f.setVisible(true);
    }
    static Logger log = LoggerFactory.getLogger(ReportFrame.class.getName());
}

/* @(#)ReportAction.java */
