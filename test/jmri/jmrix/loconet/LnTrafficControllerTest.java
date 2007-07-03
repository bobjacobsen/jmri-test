
package jmri.jmrix.loconet;

import junit.framework.*;


import apps.tests.*;

/**
 * Generated by JBuilder
 * <p>Title: LnTrafficControllerTest </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Bob Jacobsen
 * @version $Id: LnTrafficControllerTest.java,v 1.2 2007-07-03 07:03:28 jacobsen Exp $
 */
public class LnTrafficControllerTest extends TestCase {

  public LnTrafficControllerTest(String s) {
    super(s);
  }

    public void testNull() {
        // just to make JUnit feel better
    }
    
	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LnTrafficControllerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LnTrafficControllerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
