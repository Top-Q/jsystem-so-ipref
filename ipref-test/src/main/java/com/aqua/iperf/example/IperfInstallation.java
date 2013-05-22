package com.aqua.iperf.example;

import jsystem.framework.TestProperties;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;

/**
 * This class contains tests that verifies the correct installation of Iperf utility (if needed):
 * 	When Iperf already installed
 *  When Iperf installed on client only
 *  When Iperf installed on server only
 *  When Iperf is not installed where it should be (test fail)
 * Each test uses different SUT
 * Each test must be performed once on Windows and once on Linux.
 * 
 * @author shmir
 *
 */
public class IperfInstallation extends IperfBase {
	
	@TestProperties (name="Test when Iperf is installed on both client and server")
	public void testIperfInstallation() throws Exception {

		Iperf iperf1 = (Iperf)system.getSystemObject("iperfInstalled");
	
		//changeSut("IperfInstalled");
		iperf1.connect();
		report.report("Test when Iperf is installed on both client and server");
		iperf1.startTcp(iperf1.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf1.close();

		//changeSut("IperfInstalledOnClientOnly");
		Iperf iperf2 = (Iperf)system.getSystemObject("iperfInstalledOnClientOnly");
		iperf2.connect();
		report.report("Test when Iperf is installed on client only");
		iperf2.startTcp(iperf2.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf2.close();

		//changeSut("IperfInstalledOnServerOnly");
		Iperf iperf3 = (Iperf)system.getSystemObject("iperfInstalledOnServerOnly");
		iperf3.connect();
		report.report("Test when Iperf is installed on server only");
		iperf3.startTcp(iperf3.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf3.close();

		//changeSut("IperfNotInstalled");
		Iperf iperf4 = (Iperf)system.getSystemObject("iperfNotInstalled");
		iperf4.connect();
		report.report("Test when Iperf is not installed on client nor on server");
		iperf4.startTcp(iperf4.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		iperf4.close();

		//changeSut("IperfInstalledInWrongLocation");
		Iperf iperf5 = (Iperf)system.getSystemObject("iperfInstalledInWrongLocation");
		iperf5.connect();
		report.report("Test when Iperf is installed in wrong place");
		try {
			iperf5.startTcp(iperf5.server.getHost(), EnumIperfTransmitMode.CONT, 4, 100);
		} catch (Exception e) {
			report.report("Iperf faild as expected");
		}
	
	}
	
}
