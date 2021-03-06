// BuildReportOptionAction.java
package jmri.jmrit.operations.setup;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to load the print options.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */
public class BuildReportOptionAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 6686776607454912217L;

    public BuildReportOptionAction() {
        this(Bundle.getMessage("TitleBuildReportOptions"));
    }

    public BuildReportOptionAction(String s) {
        super(s);
    }

    BuildReportOptionFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (f == null || !f.isVisible()) {
            f = new BuildReportOptionFrame();
            f.initComponents();
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true);	// this also brings the frame into focus
    }

    static Logger log = LoggerFactory
            .getLogger(BuildReportOptionAction.class.getName());
}

/* @(#)BuildReportOptionAction.java */
