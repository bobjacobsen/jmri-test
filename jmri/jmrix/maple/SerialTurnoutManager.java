// SerialTurnoutManager.java

package jmri.jmrix.maple;

import jmri.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for serial systems
 * <P>
 * System names are "KTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 * @version	$Revision: 1.2 $
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'K'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = "";
		sName = SerialAddress.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t!=null) {
            return t;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
        if (t!=null) {
            return t;
        }
		
		// check if the addressed output bit is available
		int nAddress = -1;
		nAddress = SerialAddress.getNodeAddressFromSystemName(sName);
		if (nAddress == -1) return (null);
		int bitNum = SerialAddress.getBitFromSystemName(sName);
		if (bitNum == 0) return (null);
		String conflict = "";
		conflict = SerialAddress.isOutputBitFree(nAddress,bitNum);
		if ( ( conflict != "" ) && (!conflict.equals(sName)) ) {
			log.error(sName+" assignment conflict with "+conflict+".");
			notifyTurnoutCreationError(conflict,bitNum);
			return (null);
		}

        // create the turnout
        t = new SerialTurnout(sName,userName);
        
        // does system name correspond to configured hardware
        if ( !SerialAddress.validSystemNameConfig(sName,'T') ) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '"+sName+"' refers to an undefined Serial Node.");
        }
        return t;
    }

    /**
     * Public method to notify user of Turnout creation error.
     */
	public void notifyTurnoutCreationError(String conflict,int bitNum) {
		javax.swing.JOptionPane.showMessageDialog(null,"ERROR - The output bit, "+
			bitNum+", is currently assigned to "+conflict+". Turnout can not be "+
				"created as you specified."," Assignment Conflict",
						javax.swing.JOptionPane.INFORMATION_MESSAGE,null);
	}
	
	/**
	 * Get from the user, the number of addressed bits used to control a turnout. 
	 * Normally this is 1, and the default routine returns 1 automatically.  
	 * Turnout Managers for systems that can handle multiple control bits 
	 * should override this method with one which asks the user to specify the
	 * number of control bits. 
	 * If the user specifies more than one control bit, this method should 
	 * check if the additional bits are available (not assigned to another object).
	 * If the bits are not available, this method should return 0 for number of 
	 * control bits, after informing the user of the problem.
	 * This function is called whenever a new turnout is defined in the Turnout
	 * table.  It can also be used to set up other turnout control options, such
	 * as pulsed control of turnout machines.
	 */
	public int askNumControlBits(String systemName) {

		// ask user how many bits should control the turnout - 1 or 2
		int iNum = selectNumberOfControlBits();
		if (iNum == javax.swing.JOptionPane.CLOSED_OPTION) {
			/* user cancelled without selecting an option */
			iNum = 1;
			log.warn("User cancelled without selecting number of output bits. Defaulting to 1.");
		}
		else {
			iNum = iNum + 1;
		}
		
		if (iNum==2) {
			// check if the second output bit is available
			int nAddress = -1;
			nAddress = SerialAddress.getNodeAddressFromSystemName(systemName);
			if (nAddress == -1) return (0);
			int bitNum = SerialAddress.getBitFromSystemName(systemName);
			if (bitNum == 0) return (0);
			bitNum = bitNum + 1;
			String conflict = "";
			conflict = SerialAddress.isOutputBitFree(nAddress,bitNum);
			if ( conflict != "" ) {
				log.error("Assignment conflict with "+conflict+". Turnout not created.");
				notifySecondBitConflict(conflict,bitNum);
				return (0);
			}
		}
	
		return (iNum);
	}

	/**
	 * Get from the user, the type of output to be used bits to control a turnout. 
	 * Normally this is 0 for 'steady state' control, and the default routine 
	 * returns 0 automatically.  
	 * Turnout Managers for systems that can handle pulsed control as well as  
	 * steady state control should override this method with one which asks 
	 * the user to specify the type of control to be used.  The routine should 
	 * return 0 for 'steady state' control, or n for 'pulsed' control, where n
	 * specifies the duration of the pulse (normally in seconds).  
	 */
	 public int askControlType(String systemName) {
		// ask if user wants 'steady state' output (stall motors, e.g., Tortoises) or 
		//   'pulsed' output (some turnout controllers).
		int iType = selectOutputType();
		if (iType == javax.swing.JOptionPane.CLOSED_OPTION) {
			/* user cancelled without selecting an output type */
			iType = 0;
			log.warn("User cancelled without selecting output type. Defaulting to 'steady state'.");
		}
		// Note: If the user selects 'pulsed', this routine defaults to 1 second.
		return (iType);
	}

    /**
     * Public method to allow user to specify one or two output bits for turnout control
	 *  Note: This method returns 1 or 2 if the user selected, or 0 if the user cancelled
	 *         without selecting.
	 */
	public int selectNumberOfControlBits() {
		int iNum = 0;
		iNum = javax.swing.JOptionPane.showOptionDialog(null,
				"How many output bits should be used to control this turnout?",
					"Turnout Question",javax.swing.JOptionPane.DEFAULT_OPTION,
						javax.swing.JOptionPane.QUESTION_MESSAGE,
						null,new String[] {"Use 1 bit","Use 2 bits"},"Use 1 bit");
		return iNum;
	}

    /**
     * Public method to allow user to specify pulsed or steady state for two output bits 
	 *	for turnout control
	 *  Note: This method returns 1 for steady state or 2 for pulsed if the user selected, 
	 *			or 0 if the user cancelled without selecting.
	 */
	public int selectOutputType() {
		int iType = 0;
		iType = javax.swing.JOptionPane.showOptionDialog(null,
				"Should the output bit(s) be 'steady state' or 'pulsed'?",
					"Output Bits Question",javax.swing.JOptionPane.DEFAULT_OPTION,
						javax.swing.JOptionPane.QUESTION_MESSAGE,
						null,new String[] {"Steady State Output","Pulsed Output"},"Steady State Output");
		return iType;
	}

    /**
     * Public method to notify user when the second bit of a proposed two output bit turnout 
	 *		has a conflict with another assigned bit
     */
	public void notifySecondBitConflict(String conflict,int bitNum) {
		javax.swing.JOptionPane.showMessageDialog(null,"The second output bit, "+bitNum+
			", is currently assigned to "+conflict+". Turnout cannot be created as "+
					"you specified.","Assignment Conflict",
							javax.swing.JOptionPane.INFORMATION_MESSAGE,null);
	}

    static public SerialTurnoutManager instance() {
        if (_instance == null) _instance = new SerialTurnoutManager();
        return _instance;
    }
    static SerialTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */
