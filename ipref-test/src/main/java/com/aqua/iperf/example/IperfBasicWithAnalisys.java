package com.aqua.iperf.example;

import java.io.File;

import jsystem.framework.TestProperties;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.IperfAnalyzer;
import com.aqua.iperf.Iperf.EnumIperfL4Protocol;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;
import com.aqua.iperf.IperfAnalyzer.EnumIperfDataType;
import com.aqua.iperf.IperfCounters.EnumIperfCounterType;

/**
 * This class contains basic tests that verify that Iperf class is up and running (without analysis):
 * 	Start/Stop
 * 	Set per session parameters - TCP/UDP, Continuous/Burst, time in seconds/number of packets, packet length and bandwidth. 
 * The tests assume valid, basic SUT.
 * All tests run with IperfBasic.xml SUT (except if specified otherwise)
 * All tests must be performed with client and server on Windows and Linux.
 * 	Best is client on Windows and server on Linux and then vise versa 
 * 
 * @author shmir
 *
 */
public class IperfBasicWithAnalisys extends IperfBase {
	Iperf multiIperf;
	@Override
	public void setUp() throws Exception {
		super.setUp();
		multiIperf = (Iperf)system.getSystemObject("iperfBasicMcast");
	}

	
	
	@TestProperties (name="Test Iperf protocols - create one Iperf session with TCP traffic and one with UDP traffic")
	public void testIperfAnalyzer() throws Exception {
		multiIperf.init();
		multiIperf.connect();
		setL4(EnumIperfL4Protocol.UDP);
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		multiIperf.setResultFile(file);
		multiIperf.start(multiIperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1.05, 40));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.LOSSES, 1, 0));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.JITTER, 1, 10));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.BEST, EnumIperfCounterType.THROUGHPUT, 1.05, 40));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.BEST, EnumIperfCounterType.LOSSES, 0, 0));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.BEST, EnumIperfCounterType.JITTER, 1, 10));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.WORST, EnumIperfCounterType.THROUGHPUT, 0.95, 40));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.WORST, EnumIperfCounterType.LOSSES, 2, 0));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.WORST, EnumIperfCounterType.JITTER, 1, 10));
		multiIperf.close();
	}
	
	@TestProperties (name="Test Iperf protocols - create one Iperf session with UDP traffic and mulicast address")
	public void testIperfMcastAnalyzer() throws Exception {
		multiIperf.connect();
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		multiIperf.setResultFile(file);
		multiIperf.setMcast("224.0.0.0");
		multiIperf.startUdp("224.0.0.0", getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1.0, 60));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.LOSSES, 1, 0));
		multiIperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.JITTER, 2, 25));
		multiIperf.servers[0].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1.0, 60));
		multiIperf.servers[0].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.LOSSES, 1, 0));
		multiIperf.servers[0].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.JITTER, 2, 25));
		multiIperf.servers[1].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1.0, 60));
		multiIperf.servers[1].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.LOSSES, 1, 0));
		multiIperf.servers[1].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.JITTER, 2, 25));
		multiIperf.setMcast(" ");
		multiIperf.close();
	}

	/**
	 * TCP or UDP
	 * @section Iperf
	 */
	public void setL4(EnumIperfL4Protocol l4) {
		this.l4 = l4;
	}
	public EnumIperfL4Protocol getL4() {
		return l4;
	}
	private EnumIperfL4Protocol l4 = EnumIperfL4Protocol.TCP;

	/**
	 * Continuous (time) or Burst (number) 
	 * @section Iperf
	 */
	public void setMode(EnumIperfTransmitMode mode) {
		this.mode = mode;
	}
	public EnumIperfTransmitMode getMode() {
		return mode;
	}
	private EnumIperfTransmitMode mode = EnumIperfTransmitMode.CONT;

	/**
	 * If mode = Continuous then this variable indicates time in seconds, if = Burst then it indicates number of packets
	 * @section Iperf
	 */
	public void setTimeSlashCount(int timeSlashCount) {
		this.timeSlashCount = timeSlashCount;
	}
	public int getTimeSlashCount() {
		return timeSlashCount;
	}
	private int timeSlashCount = 10;

	/**
	 * Buffer length (packet size)
	 * @section Iperf
	 */
	public void setBufferLen(int bufferLen) {
		this.bufferLen = bufferLen;
	}
	public int getBufferLen() {
		return bufferLen;
	}
	private int bufferLen = 100;
	
	/**
	 * Iperf traffic bandwidth
	 * @param bw
	 */
	public void setBw(String bw) {
		this.bw = bw;
	}	
	public String getBw() {
		return bw;
	}
	private String bw = "1M";

}
