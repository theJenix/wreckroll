package org.gitmad.wreckroll.client;

import java.io.IOException;

import junit.framework.TestCase;

public class RelayClientTest extends TestCase {
	
	RelayClient testclient;
	
	public void setUp(){
		try {
			testclient = new RelayClient("localhost", 6696);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("shouldn't create an error");
		}
	}

	public void testForward() {
		this.testclient.forward();
		fail("Not yet implemented");
	}

	public void testStop() {
		fail("Not yet implemented");
	}

	public void testReverse() {
		fail("Not yet implemented");
	}

	public void testLeft() {
		fail("Not yet implemented");
	}

	public void testRight() {
		fail("Not yet implemented");
	}

	public void testToggleGun() {
		fail("Not yet implemented");
	}

	public void testToggleSmoke() {
		fail("Not yet implemented");
	}

	public void testToggleCanopy() {
		fail("Not yet implemented");
	}

}
