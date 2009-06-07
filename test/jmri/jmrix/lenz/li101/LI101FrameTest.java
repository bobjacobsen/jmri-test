package jmri.jmrix.lenz.li101;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LI101FrameTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.li101.LI101Frame class
 * @author			Paul Bender
 * @version         $Revision: 1.2 $
 */
public class LI101FrameTest extends TestCase {

    public void testCtor() {
        LI101Frame f = new LI101Frame();
        Assert.assertNotNull(f);
    }

	// from here down is testing infrastructure

	public LI101FrameTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", LI101FrameTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LI101FrameTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LI101FrameTest.class.getName());

}
