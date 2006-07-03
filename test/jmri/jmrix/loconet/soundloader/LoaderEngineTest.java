// LoaderEngineTest.java
package jmri.jmrix.loconet.soundloader;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.loconet.LocoNetMessage;


/**
 * Tests for the jmri.jmrix.loconet.soundloader.LoaderEngine class.
 *
 * @author			Bob Jacobsen  Copyright 2001, 2002, 2006
 * @version         $Revision: 1.1 $
 */
public class LoaderEngineTest extends TestCase {

    public void testGetEraseMessage() {
        LoaderEngine l = new LoaderEngine();
        LocoNetMessage m = l.getEraseMessage();
        Assert.assertEquals("contents", "D3 02 01 7F 00 50", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetExitMessage() {
        LoaderEngine l = new LoaderEngine();
        LocoNetMessage m = l.getExitMessage();
        Assert.assertEquals("contents", "D3 00 00 00 00 2C", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetStartWavMessage1() {
        LoaderEngine l = new LoaderEngine();
        LocoNetMessage m = l.getStartWavMessage(0x17, 128);
        Assert.assertEquals("contents", "D3 04 17 01 00 3E", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }


    public void testGetStartWavMessage2() {
        LoaderEngine l = new LoaderEngine();
        LocoNetMessage m = l.getStartWavMessage(0x17, 512);
        Assert.assertEquals("contents", "D3 04 17 02 00 3D", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testGetSendWavDataMessage() {
        LoaderEngine l = new LoaderEngine();
        int [] idata = new int[] {  0x17, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x4D, 0x75, 0x74, 0x65, 0x2E, 0x77, 0x61, 0x76,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte [] data = new byte[idata.length];
        for (int i=0; i<data.length; i++) data[i] = (byte)(idata[i]&0xFF);
        
        LocoNetMessage m = l.getSendWavDataMessage(0x17, data);
        Assert.assertEquals("contents", "D3 08 17 28 00 1B 17 80 00 00 00 00 00 00 "+
                                        "4D 75 74 65 2E 77 61 76 00 00 00 00 00 00 "+
                                        "00 00 00 00 00 00 00 00 00 00 00 00 00 00 "+
                                        "00 00 00 00 0F", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
    }

    public void testFullTransfer1() {
        LoaderEngine l = new LoaderEngine();
        int handle = 0x17;
        String name = "Mute.wav";
        byte[] contents = new byte[128];
        for (int i = 0; i<128; i++) contents[i] = (byte)0x80;
        
        LocoNetMessage m;
        
        m = l.initWavTransfer(handle, name, contents);
        Assert.assertEquals("contents", "D3 04 17 01 00 3E", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());

        m = l.nextWavTransfer();
        Assert.assertEquals("contents", "D3 08 17 28 00 1B 17 80 00 00 00 00 00 00 "+
                                        "4D 75 74 65 2E 77 61 76 00 00 00 00 00 00 "+
                                        "00 00 00 00 00 00 00 00 00 00 00 00 00 00 "+
                                        "00 00 00 00 0F", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());
        
        m = l.nextWavTransfer();
        Assert.assertEquals("contents", "D3 08 17 00 01 32 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 80 "+
                                        "80 80 80 80 80 80 FF", m.toString());
        Assert.assertEquals("checksum", true, m.checkParity());

        m = l.nextWavTransfer();
        Assert.assertEquals("end", null, m);
    }
    
    
    // from here down is testing infrastructure

    public LoaderEngineTest(String s) {
    	super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LoaderEngineTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LoaderEngineTest.class);
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoaderEngineTest.class.getName());

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
