// LlnmonTest.java

package jmri.jmrix.loconet.locomon;

import apps.tests.Log4JFixture;

import jmri.jmrix.loconet.LocoNetMessage;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.StringUtil;

/**
 * Tests for the jmri.jmrix.loconet.locomon.Llnmon class.
 * @author	    Bob Jacobsen Copyright (C) 2002, 2007
 * @version         $Revision: 1.2 $
 */
public class LlnmonTest extends TestCase {

    public void testTransponding() {
        LocoNetMessage l;
        Llnmon f = new Llnmon();
        
        l = new LocoNetMessage(new int[]{0xD0, 0x01, 0x20, 0x08, 0x20, 0x26});
        assertEquals("out A", "Transponder absent in section 18 zone A decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x20, 0x08, 0x20, 0x04});
        assertEquals(" in A", "Transponder present in section 18 zone A decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x22, 0x08, 0x20, 0x24});
        assertEquals(" in B", "Transponder present in section 18 zone B decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x24, 0x08, 0x20, 0x04});
        assertEquals(" in C", "Transponder present in section 18 zone C decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x26, 0x08, 0x20, 0x04});
        assertEquals(" in D", "Transponder present in section 18 zone D decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x28, 0x08, 0x20, 0x04});
        assertEquals(" in E", "Transponder present in section 18 zone E decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2A, 0x08, 0x20, 0x04});
        assertEquals(" in F", "Transponder present in section 18 zone F decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2C, 0x08, 0x20, 0x04});
        assertEquals(" in G", "Transponder present in section 18 zone G decoder address 1056 (long) \n", f.displayMessage(l));

        l = new LocoNetMessage(new int[]{0xD0, 0x21, 0x2E, 0x08, 0x20, 0x04});
        assertEquals(" in H", "Transponder present in section 18 zone H decoder address 1056 (long) \n", f.displayMessage(l));
    }

    public void testLissy1() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x60,0x01,0x42,0x35,0x05});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 1", "Lissy 1: Loco 8501 moving south", f.displayMessage(l));
    }

    public void testLissy2() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE4,0x08,0x00,0x40,0x01,0x42,0x35,0x25});
        Llnmon f = new Llnmon();
        
        assertEquals("Lissy message 2", "Lissy 1: Loco 8501 moving north", f.displayMessage(l));
    }

    // from here down is testing infrastructure

    public LlnmonTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LlnmonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LlnmonTest.class);
        return suite;
    }

    Log4JFixture log4jfixtureInst = new Log4JFixture(this);

    protected void setUp() {
    	log4jfixtureInst.setUp();
    }

    protected void tearDown() {
    	log4jfixtureInst.tearDown();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LlnmonTest.class.getName());

}
