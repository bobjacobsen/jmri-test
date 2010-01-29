// DispatcherPro.java

package apps.DispatcherPro;

import apps.Apps;
import jmri.util.JmriJFrame;

import java.awt.event.ActionEvent;

import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The JMRI program for creating control panels.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2 $
 */
public class DispatcherPro extends Apps {

    DispatcherPro(JFrame p) {
        super(p);
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    protected String mainWindowHelpID() {
            return "package.apps.DispatcherPro.DispatcherPro";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("DispatcherProVersionCredit"),
                                new Object[]{jmri.Version.name()});
    }

    protected String line2() {
        return "http://jmri.org/DispatcherPro ";
    }

    protected JPanel statusPanel() {
        JPanel j = new JPanel();
        j.setLayout(new BoxLayout(j, BoxLayout.Y_AXIS));
        j.add(super.statusPanel());

        // Buttons

        Action quit = new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
					Apps.handleQuit();
                }
            };


        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.FlowLayout());
        JButton h1 = new JButton(rb.getString("ButtonHelp"));
        jmri.util.HelpUtil.addHelpToComponent(h1, "html.apps.DispatcherPro.DispatcherPro");
        h1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(h1);
        JButton q1 = new JButton(rb.getString("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        p3.add(q1);
        j.add(p3);

        return j;
    }
    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        log.info(apps.Apps.startupInfo("DispatcherPro"));

        setConfigFilename("DispatcherProConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("DispatcherPro");
        createFrame(new DispatcherPro(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DispatcherPro.class.getName());
}


