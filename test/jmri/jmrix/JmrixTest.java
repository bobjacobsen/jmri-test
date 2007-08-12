//JmrixTest.java

package jmri.jmrix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmrix package
 * @author	Bob Jacobsen  Copyright 2003, 2007
 * @version         $Revision: 1.14 $
 */
public class JmrixTest extends TestCase {

	// from here down is testing infrastructure

	public JmrixTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", JmrixTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrix.JmrixTest");

		suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
		suite.addTest(jmri.jmrix.cmri.serial.SerialTest.suite());
		suite.addTest(jmri.jmrix.direct.DirectTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccTest.suite());
		suite.addTest(jmri.jmrix.lenz.XNetTest.suite());
		suite.addTest(jmri.jmrix.loconet.LocoNetTest.suite());
		suite.addTest(jmri.jmrix.nce.NceTest.suite());
		suite.addTest(jmri.jmrix.oaktree.SerialTest.suite());
		suite.addTest(jmri.jmrix.pricom.PricomTest.suite());
		suite.addTest(jmri.jmrix.rps.RpsTest.suite());
		suite.addTest(jmri.jmrix.tmcc.SerialTest.suite());
		suite.addTest(jmri.jmrix.oaktree.SerialTest.suite());
		suite.addTest(jmri.jmrix.qsi.QsiTest.suite());

		return suite;

	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
