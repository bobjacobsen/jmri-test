/* NceProgrammerManager.java */
package jmri.jmrix.nce;

import jmri.AddressedProgrammer;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;

/**
 * Extend DefaultProgrammerManager to provide ops mode programmers for NCE
 * systems
 *
 * @see jmri.ProgrammerManager
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2013
 * @version	$Revision$
 */
public class NceProgrammerManager extends DefaultProgrammerManager {

    NceTrafficController tc;

    public NceProgrammerManager(Programmer serviceModeProgrammer, NceSystemConnectionMemo memo) {
        super(serviceModeProgrammer, memo);
        this.tc = memo.getNceTrafficController();
    }

    /**
     * Works with command station to provide Ops Mode, so say it works
     *
     * @return true
     */
    public boolean isAddressedModePossible() {
        return true;
    }

    /**
     * Works with PH command station to provide Service Mode and USB connect to
     * PowerCab.
     *
     * @return true if not USB connect to SB3,PowerPro,SB5
     */
    public boolean isGlobalProgrammerAvailable() {
        if (tc != null && (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)) {
            if ((tc.getCmdGroups() & NceTrafficController.CMDS_PROGTRACK) == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public AddressedProgrammer getAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return new NceOpsModeProgrammer(tc, pAddress, pLongAddress);
    }

    public AddressedProgrammer reserveAddressedProgrammer(boolean pLongAddress, int pAddress) {
        return null;
    }
}


/* @(#)NceProgrammerManager.java */
