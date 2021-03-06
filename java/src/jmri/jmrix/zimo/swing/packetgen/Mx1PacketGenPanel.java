// MrcPacketGenPanel.java
package jmri.jmrix.zimo.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import jmri.jmrix.zimo.Mx1TrafficController;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of Mrc messages
 *
 * @author	Ken Cameron	Copyright (C) 2010 derived from:
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 25018 $
 */
public class Mx1PacketGenPanel extends jmri.jmrix.zimo.swing.Mx1Panel {

    //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.mrc.packetgen.MrcPacketGenBundle");
    /**
     *
     */
    private static final long serialVersionUID = 7867055984973628076L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);

    private Mx1TrafficController tc = null;

    public Mx1PacketGenPanel() {
        super();
    }

    public void initContext(Object context) throws Exception {
        if (context instanceof Mx1SystemConnectionMemo) {
            try {
                initComponents((Mx1SystemConnectionMemo) context);
            } catch (Exception e) {
                //log.error("BoosterProg initContext failed");
            }
        }
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.zimo.swing.packetgen.Mx1PacketGenPanel";
    }//IN18N

    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("MX1_");//IN18N
        }
        x.append(": ");
        x.append(Bundle.getMessage("Title"));//IN18N
        return x.toString();
    }

    public void initComponents(Mx1SystemConnectionMemo m) throws Exception {
        this.memo = m;
        this.tc = m.getMx1TrafficController();

        // the following code sets the frame's initial state
        jLabel1.setText("Command: ");//IN18N
        jLabel1.setVisible(true);

        sendButton.setText("Send");//IN18N
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");//IN18N

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command"); //IN18N
        packetTextField.setMaximumSize(new Dimension(packetTextField
                .getMaximumSize().width, packetTextField.getPreferredSize().height));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        /*Mx1Message m = new Mx1Message(packetTextField.getText().length());
         for (int i = 0; i < packetTextField.getText().length(); i++)
         m.setElement(i, packetTextField.getText().charAt(i));*/
        tc.sendMx1Message(createPacket(packetTextField.getText()), null);
    }

    Mx1Message createPacket(String s) {
        // gather bytes in result
        byte b[];
        try {
            b = StringUtil.bytesFromHexString(s);
        } catch (NumberFormatException e) {
            return null;
        }
        if (b.length == 0) {
            return null; // no such thing as a zero-length message
        }
        Mx1Message m = new Mx1Message(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, (b[i] & 0xff));
        }
        return m;
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.zimo.swing.Mx1NamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = -5895919905704623321L;

        public Default() {
            super("Open MRC Send Binary Command",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Mx1PacketGenPanel.class.getName(),
                    jmri.InstanceManager.getDefault(Mx1SystemConnectionMemo.class));//IN18N
        }
    }

    static Logger log = LoggerFactory.getLogger(Mx1PacketGenPanel.class.getName());
}
