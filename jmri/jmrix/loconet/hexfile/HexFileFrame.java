/** 
 * HexFileFrame.java
 *
 * Title:			LocoMon application
 * Description:		Frame to inject LocoNet messages from a hex file
 * @author			Bob Jacobsen  Copyright 2001
 * @version			 
 */

// This is a sample frame that drives a test App.  It controls reading from
// a .hex file, feeding the information to a LocoMonFrame (monitor) and 
// connecting to a LocoGenFrame (for sending a few commands).

package jmri.jmrix.loconet.hexfile;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

import jmri.jmrix.loconet.LnTrafficController;

public class HexFileFrame extends javax.swing.JFrame {

	// member declarations
	javax.swing.JButton openHexFileButton = new javax.swing.JButton();
	javax.swing.JButton filePauseButton = new javax.swing.JButton();
	javax.swing.JButton jButton1 = new javax.swing.JButton();
	javax.swing.JTextField delayField = new javax.swing.JTextField(5);
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

	// to find and remember the log file
	final javax.swing.JFileChooser inputFileChooser = new JFileChooser(" ");

	public HexFileFrame() {
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state
		
		openHexFileButton.setText("Open file");
		openHexFileButton.setVisible(true);
		openHexFileButton.setToolTipText("run from hex file");

		filePauseButton.setText("Pause");
		filePauseButton.setVisible(true);
		filePauseButton.setToolTipText("pauses the trace at the source");

		jButton1.setText("Continue");
		jButton1.setVisible(true);
		jButton1.setToolTipText("continues the trace at the source");

		delayField.setText("200");
		delayField.setVisible(true);
		delayField.setToolTipText("delay (in milliseconds) between commands");

		jLabel1.setText("Delay:");
		jLabel1.setVisible(true);

		setTitle("Hexfile LocoNet simulator");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel pane1 = new JPanel();
			pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
			pane1.add(openHexFileButton);
			pane1.add(new JPanel()); // dummy
		getContentPane().add(pane1);
		
		JPanel pane2 = new JPanel();
			pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
			pane2.add(jLabel1);
			pane2.add(delayField);
		getContentPane().add(pane2);

		JPanel pane3 = new JPanel();
			pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
			pane3.add(filePauseButton);
			pane3.add(jButton1);
		getContentPane().add(pane3);


		openHexFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openHexFileButtonActionPerformed(e);
			}
		});
		filePauseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				filePauseButtonActionPerformed(e);
			}
		});
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButton1ActionPerformed(e);
			}
		});
		delayField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				delayFieldActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// set file chooser to a default
		inputFileChooser.setSelectedFile(new File("lnpacket.hex"));

		// create a new Hex file handler, set its delay
		port = new LnHexFilePort();
		port.setDelay(Integer.valueOf(delayField.getText()).intValue());
		
	}
  
  	private boolean mShown = false;
  	
	public void addNotify() {
		super.addNotify();
		
		if (mShown)
			return;
			
		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}

		mShown = true;
	}

	boolean connected = false;
	
	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
		// disconnect from LnTrafficManager if connected
		if (connected) LnTrafficController.instance().disconnectPort(port);
		connected = false;
	}
	
	public void openHexFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// select the file
		// start at current file, show dialog
		int retVal = inputFileChooser.showOpenDialog(this);

		// handle selection or cancel
		if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
		
		// call load to process the file
		port.load(inputFileChooser.getSelectedFile());

		// connect to the traffic controller
		LnTrafficController.instance().connectPort(port);
		connected = true;
		
		// If a jmri.Programmer instance doesn't exist, create a 
		// loconet.SlotManager to do that
		if (jmri.InstanceManager.programmerInstance() == null) 
			jmri.InstanceManager.setProgrammer(new jmri.progdebugger.ProgDebugger());

		// If a jmri.PowerManager instance doesn't exist, create a 
		// loconet.LnPowerManager to do that
		if (jmri.InstanceManager.powerManagerInstance() == null) 
			jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

		// If a jmri.TurnoutManager instance doesn't exist, create a 
		// loconet.LnTurnoutManager to do that
		if (jmri.InstanceManager.turnoutManagerInstance() == null) 
			jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

		// start operation
		sourceThread = new Thread(port);
		sourceThread.start();
		LnTrafficController.instance().startThreads();
		
		// reach here while file runs.  Need to return so GUI still acts, 
		// but that normally lets the button go back to default.
	}
	
	public void filePauseButtonActionPerformed(java.awt.event.ActionEvent e) {
		sourceThread.suspend();
		// sinkThread.suspend(); // allow sink to catch up
	}
	
	public void jButton1ActionPerformed(java.awt.event.ActionEvent e) {  // resume button
	sourceThread.resume();
		// sinkThread.resume();
	}
	
	public void delayFieldActionPerformed(java.awt.event.ActionEvent e) {
		// if the hex file has been started, change its delay
		if (port!=null) port.setDelay(Integer.valueOf(delayField.getText()).intValue());
	}
	
	
	
	private Thread sourceThread;
	private Thread sinkThread;
	private LnHexFilePort port = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(HexFileFrame.class.getName());
	
}
