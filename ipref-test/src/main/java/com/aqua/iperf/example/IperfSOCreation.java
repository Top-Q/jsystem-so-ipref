package com.aqua.iperf.example;

import java.io.File;

import jsystem.framework.TestProperties;
import jsystem.framework.sut.ChangeSutTest;
import junit.framework.SystemTestCase;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;
import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

/**
 * This class contains tests that verify the different ways to create Iperf system object:
 * 	From SUT
 *  From SUT with CLI SO
 *  No SUT - directly from code
 * Each test uses different SUT
 * These tests can run either on Windows or Linux (no need to run on both)
 * 
 * @author shmir
 *
 */
public class IperfSOCreation extends SystemTestCase {
	protected Iperf iperf = null;	
	private String iperfDir="iperf.exe";
	private String bindingHost = "224.0.0.0";
	private CliConnectionImpl cliConnection1;
	private CliConnectionImpl cliConnection2;
	private CliConnectionImpl cliConnection3;
	
	@TestProperties (name="Test building iperf from sut")
	public void testIperfSOCreation() throws Exception {
		iperf = (Iperf)system.getSystemObject("iperf");
		iperfDir = iperf.server.getIperfExec();
		
		String file = "." + File.separator + "IperfResults-IperfLoadedFromSUT";
		iperf.setResultFile(file);
		iperf.init();
		iperf.connect();
		report.report("Test when Iperf is loaded from SUT");
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf.close();
		
		
		cliConnection1 = (CliConnectionImpl)system.getSystemObject("cliConnection1");		
		cliConnection2 = (CliConnectionImpl)system.getSystemObject("cliConnection2");
		
		Iperf iperf = new Iperf(cliConnection1, cliConnection2);
		
		file = "." + File.separator + "IperfResults-CLIFromSUT";
		iperf.server.setIperfExec(iperfDir);
		iperf.client.setIperfExec(iperfDir);
		iperf.setResultFile(file);
		iperf.init();
		iperf.connect();
		report.report("Test when CLI is from SUT and Iperf is created by test");
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf.close();

		Iperf iperfFromTest = new Iperf("localhost", "telnet", "aqua", "aqua", EnumOperatinSystem.WINDOWS,
						  "localhost", "telnet", "aqua", "aqua", EnumOperatinSystem.WINDOWS);
		
		
		file = "." + File.separator + "IperfResults-CreateManually";
		iperfFromTest.server.setIperfExec(iperfDir);
		iperfFromTest.client.setIperfExec(iperfDir);
		iperfFromTest.setResultFile(file);
		iperfFromTest.init();
		iperfFromTest.connect();
		report.report("Test when manually creating Iperf with all params");
		iperfFromTest.startTcp(iperfFromTest.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperfFromTest.close();
	}
	
	@TestProperties (name="Test build iperf from cliconnection objects")
	public void testIperfSOCreationMcast() throws Exception {
		cliConnection1 = (CliConnectionImpl)system.getSystemObject("cliConnection1");		
		cliConnection2 = (CliConnectionImpl)system.getSystemObject("cliConnection2");
		cliConnection3 = (CliConnectionImpl)system.getSystemObject("cliConnection3");
		iperf = new Iperf(cliConnection1, new CliConnectionImpl[]{cliConnection2, cliConnection3});
		String file = "." + File.separator + "IperfResults-CLIFromSUT";
		iperf.setResultFile(file);
		iperf.server.setIperfExec(iperfDir);
		iperf.client.setIperfExec(iperfDir);
		iperf.init();
		iperf.connect();
		report.report("Test when CLI is from SUT and Iperf is created by test");
		iperf.setMcast(bindingHost);
		iperf.startUdp(bindingHost, EnumIperfTransmitMode.CONT, 4, 100, "1M");
		iperf.close();
	}

	protected void changeSut(String sut) throws Exception {
		ChangeSutTest cst = new ChangeSutTest();
		cst.setSut(sut+".xml");
		cst.changeSut();
	}

	public String getBindingHost() {
		return bindingHost;
	}

	public void setBindingHost(String bindingHost) {
		this.bindingHost = bindingHost;
	}

	public String getIperfDir() {
		return iperfDir;
	}

	public void setIperfDir(String iperfDir) {
		this.iperfDir = iperfDir;
	}
	
}
