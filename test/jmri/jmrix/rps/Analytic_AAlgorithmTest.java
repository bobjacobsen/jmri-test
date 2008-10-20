// Analytic_AAlgorithmTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.Analytic_AAlgorithm class.
 * 
 * 
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision: 1.1 $
 */
public class Analytic_AAlgorithmTest extends AbstractAlgorithmTest {
        

    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Analytic_AAlgorithm(pts, vs);
    }

	// from here down is testing infrastructure

	// Main entry point
	static public void main(String[] args) {
        String[] testCaseName = {Analytic_AAlgorithmTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(Analytic_AAlgorithmTest.class);
        return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
