// StackMonAction.java

package jmri.jmrix.lenz.stackmon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a StackMonFrame object
 *
 * @author	Paul Bender    Copyright (C) 2005
 * @version     $Revision: 1.3 $
 */

public class StackMonAction extends AbstractAction {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo _memo=null;

    public StackMonAction(String s,jmri.jmrix.lenz.XNetSystemConnectionMemo memo) { 
       super(s);
       _memo=memo;
    }
    public StackMonAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) { this("Stack Monitor",memo);}

    public void actionPerformed(ActionEvent e) {

        // create a StackMonFrame
        StackMonFrame f = new StackMonFrame(_memo);
        f.setVisible(true);

    }
}


/* @(#)SlotMonAction.java */
