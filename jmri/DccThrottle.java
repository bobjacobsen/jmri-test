// DccThottle.java

package jmri;

/**
 * Description:		<describe the DccThrottle interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */
public interface DccThrottle extends Throttle {

    // extends Throttle to allow asking about DCC items

    public int getDccAddress();
    public void setDccAddress(int address);

    // to handle quantized speed. Note this can change! Valued returned is
    // always positive.
    public float speedIncrement();

    // information on consisting  (how do we set consisting?)

    // register for notification


}


/* @(#)DccThottle.java */
