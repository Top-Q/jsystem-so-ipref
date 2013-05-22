package com.aqua.iperf.example;

import java.io.File;

import jsystem.framework.TestProperties;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.Iperf.EnumIperfPrintFormat;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;

/**
 * This class contains tests that test the configuration of all Iperf parameters except for the per
 * session parameters (without analysis).
 * All tests must be performed on Windows and Linux.
 * Note that there is no test for input streams (yet...).
 * 
 * @author shmir
 *
 */
public class IperfConfiguration extends IperfBase {
	
	@TestProperties (name="Test template where the user can play with all parameters")
	public void testTemplate() throws Exception {
		iperf.connect();
		report.report("Test Iperf" + ", window size = " + getWindowSize() + ", print format = " + getFormat() + ", interval reports = " + getInterval());
		String file = "." + File.separator + "IperfResults-" + getWindowSize() + getFormat() + getInterval();
		iperf.setResultFile(file);
		
		iperf.client.setWindowSize(getWindowSize());
		iperf.server.setWindowSize(getWindowSize());
		iperf.client.setFormat(getFormat());
		iperf.server.setFormat(getFormat());
		iperf.client.setInterval(getInterval());
		iperf.server.setInterval(getInterval());
		
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		
	}
	
	@TestProperties (name="Test Iperf host binding - create one Iperf session that uses default interfaces and one that uses different interfaces")
	public void testIperfBindHost() throws Exception {
		//changeSut("IperfBasic");
		iperf.connect();
		report.report("Test Bind To Host - do not set, make sure client uses server address");
		String file = "." + File.separator + "IperfResults-" + "noBinding";
		iperf.setResultFile(file);
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		

		Iperf iperfConfiguration;
		
		iperfConfiguration  = (Iperf)system.getSystemObject("iperfConfiguration");
		//changeSut("IperfConfiguration");
		iperfConfiguration.connect();
		report.report("Test Bind To Host - bind to different interface");
		file = "." + File.separator + "IperfResults-" + "Binding";
		iperfConfiguration.setResultFile(file);
		iperfConfiguration.startTcp(iperfConfiguration.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperfConfiguration.close();
	}
	
	@TestProperties (name="Test Iperf socket buffer size (window size) - create one Iperf session with TCP and 64K TCP window size and one UDP and max data size of 2K")
	public void testIperfWindowSize() throws Exception {
		iperf.connect();
		report.report("Test window size - set TCP with 64K TCP window size");
		String file = "." + File.separator + "IperfResults-" + "64KTCPWindowSize";
		iperf.setResultFile(file);
		setWindowSize("64K");
		iperf.client.setWindowSize(getWindowSize());
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		

		iperf.connect();
		report.report("Test window size - set UDP with max 2K datagrams");
		file = "." + File.separator + "IperfResults-" + "2KUDPFrame";
		iperf.setResultFile(file);
		setWindowSize("2K");
		iperf.client.setWindowSize(getWindowSize());
		iperf.startUdp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100, "1M");
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf report formats - create Iperf session with different report formats")
	public void testIperfReportFrmat() throws Exception {
		for (EnumIperfPrintFormat e : EnumIperfPrintFormat.values()) {
			iperf.connect();
			report.report("Test report format = " + e);
			setFormat(e);
			String file = "." + File.separator + "IperfResults-" + getFormat();
			iperf.setResultFile(file);
			iperf.server.setFormat(getFormat());
			iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
			iperf.close();
		}
	}
	
	@TestProperties (name="Test Iperf report interval - create one Iperf session zero report interval (no interval reporting, summary) and one with 2 seconds intervals")
	public void testIperfReportInterval() throws Exception {
		iperf.connect();
		report.report("Test interval - no interval reports (at least that what should happen according to the documentation...)");
		String file = "." + File.separator + "IperfResults-" + "NoIntervalReporting";
		iperf.setResultFile(file);
		setInterval(0);
		iperf.server.setInterval(getInterval());
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		

		iperf.connect();
		report.report("Test interval - no interval reports");
		file = "." + File.separator + "IperfResults-" + "2SecsIntervalReporting";
		iperf.setResultFile(file);
		setInterval(2);
		iperf.server.setInterval(getInterval());
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf Type Of Service - create TCP session with 0x10 TOS and UDP session with 0x08 TOS - test always fails...")
	public void testIperfClientTos() throws Exception {
		String file;
		int numOfFails=0;
		
		try{
			
		iperf.connect();
		report.report("Test TOS - TCP session with 0x10 TOS");
		file = "." + File.separator + "IperfResults-" + "NoIntervalReporting";
		iperf.setResultFile(file);
		setTos(0x10); 
		iperf.client.setTos(getTos());
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		
		
		}catch (Exception e) {
			++numOfFails;
		}
		
		
		try {
			iperf.connect();
			report.report("Test TOS - UDP session with 0x08 TOS");
			file = "." + File.separator + "IperfResults-" + "NoIntervalReporting";
			iperf.setResultFile(file);
			setTos(0x08);
			iperf.client.setTos(getTos());
			iperf.startUdp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100, "1M");
			iperf.close();
		} catch (Exception e) {
			++numOfFails;
		}
		
		if (numOfFails==2){
			report.report("Also the tcp and udp traffic failed when we use wrong tos");
		}else{
			report.report("Not all traffic failed when we send wrong tos",false);
		}
		
	}
	
	@TestProperties (name="Test Iperf parallel - create TCP session with 2 connections and UDP session with 4 connections")
	public void testIperfClientParallel() throws Exception {
		iperf.connect();
		report.report("Test parallel - TCP session with 2 parallel connections");
		String file = "." + File.separator + "IperfResults-" + "2TCPConnections";
		iperf.setResultFile(file);
		setParallel(2);
		iperf.client.setParallel(getParallel());
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();		

		iperf.connect();
		report.report("Test parallel - UDP session with 4 parallel connections");
		file = "." + File.separator + "IperfResults-" + "4UDPConnections";
		iperf.setResultFile(file);
		setParallel(4);
		iperf.client.setParallel(getParallel());
		iperf.startUdp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100, "1M");
		iperf.close();		
	}

	/**
	 * Sets the socket buffer sizes to the specified value
	 * @section mutual
	 */
	public void setWindowSize( String windowSize ){
		this.windowSize = windowSize;
	}
	public String getWindowSize() {
		return windowSize;
	}
	private String windowSize = "128K";

	/**
	 * Print format
	 * @section mutual
	 */
	public void setFormat(EnumIperfPrintFormat format) {
		this.format = format;
	}
	public EnumIperfPrintFormat getFormat() {
		return format;
	}
	private EnumIperfPrintFormat format = EnumIperfPrintFormat.MBITS;

	/**
	 * Sets the interval time in seconds between periodic bandwidth, jitter, and loss reports.
	 * @section mutual
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getInterval() {
		return interval;
	}	
	private int interval = 1;
	
	/**
	 * The type-of-service for outgoing packets. 
	 * @section client
	 */
	public void setTos(int tos) {
		this.tos = tos;
	} 
	public int getTos() {
		return tos;
	}
	private int tos = 0x0;

	/**
	 * The number of simultaneous connections to make to the server. 
	 * @section client
	 */
	public void setParallel(int parallelConns) {
		this.parallelConns = parallelConns;
	} 	
	public int getParallel() {
		return parallelConns;
	}
	private int parallelConns = 1;
	
}
