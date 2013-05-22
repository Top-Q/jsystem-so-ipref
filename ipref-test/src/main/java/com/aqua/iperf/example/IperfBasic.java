package com.aqua.iperf.example;

import java.io.File;

import jsystem.framework.TestProperties;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.IperfAnalyzer;
import com.aqua.iperf.IperfFile;
import com.aqua.iperf.Iperf.EnumIperfL4Protocol;
import com.aqua.iperf.Iperf.EnumIperfPrintFormat;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;
import com.aqua.iperf.IperfAnalyzer.EnumIperfDataType;
import com.aqua.iperf.IperfCounters.EnumIperfCounterType;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

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
public class IperfBasic extends IperfBase {
	public String bindingAddress ="224.0.55.55";
	public int port1 =6000;
	public int port2 =7000;
	public int illegalPort =10110;
	public String userName ="aqua";
	public String password ="aqua";
	public String protocol ="telnet";
	public EnumOperatinSystem enumOperatinSystem =EnumOperatinSystem.WINDOWS;
	public String iperfExecDir ="iperf.exe";
	
	@TestProperties (name="Test template where the user can play with all parameters")
	public void testTemplate() throws Exception {
		iperf.connect();
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		iperf.setResultFile(file);
		iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		iperf.close();
	}
	
	
	@TestProperties (name="Test TCP traffic with defaults")
	public void testSimpleTcp() throws Exception {
		iperf.setResultFile("blabla1.txt");
		iperf.connect();
		report.report("Test TCP traffic with defaults, CONT = 10 secs, buffer length = 100, bandwith = 1MBytes/s");
		iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		iperf.close();
	}
	
	@TestProperties (name="Negative test - test with invalid port")
	public void testSimpleTcpFailPermissionDenied() throws Exception {
		int port = iperf.getPort();
		iperf.setPort(illegalPort);
		iperf.setResultFile("blabla1.txt");
		iperf.setPending(false);
		iperf.connect();
		report.report("Test TCP traffic with defaults, BURST = 10 packets, buffer length = 100, bandwith = 1MBytes/s");
		try {
			iperf.startTcp(iperf.server.getHost(), EnumIperfTransmitMode.CONT, 10, 100);
		} catch (Exception e) {
			report.report("Exception thrown as expected");
		}
		iperf.setPort(port);
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf protocols - create one Iperf session with TCP traffic and one with UDP traffic")
	public void testIperfProtocol() throws Exception {
		iperf.connect();
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		iperf.setResultFile(file);
		iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		
		iperf.connect();
		setL4(EnumIperfL4Protocol.UDP);
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		iperf.setResultFile(file);
		iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf modes - create one Iperf session with ${timeSlashCount} seconds and one with ${timeSlashCount} packets")
	public void testIperfMode() throws Exception {
		iperf.connect();
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		iperf.setResultFile(file);
		iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());

		iperf.connect();
		setMode(EnumIperfTransmitMode.BURST);
		setTimeSlashCount(40000000);
		report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
		file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
		iperf.setResultFile(file);
		iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		setMode(EnumIperfTransmitMode.CONT);
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf buffer length - create couple Iperf sessions with buffer length from minimumm to maximum")
	public void testIperfBuffwrLength() throws Exception {
		for (int i = 1024; i <= 2048; i *= 2) {
			iperf.connect();
			setBufferLen(i);
			report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
			String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
			iperf.setResultFile(file);
			iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
		}
		setBufferLen(1470);
		iperf.close();
	}
	
	@TestProperties (name="Test Iperf bandwidth - create couple Iperf sessions with variable bandwidth")
	public void testIperfBandwidth() throws Exception {
		setL4(EnumIperfL4Protocol.UDP);
		int i = 1;
		for (EnumIperfPrintFormat e : EnumIperfPrintFormat.values()) {
			iperf.connect();
			if (!e.value().equalsIgnoreCase("g")) {
				setBw(i + e.value());
				report.report("Test " + getL4() + " traffic, mode = " + getMode() +", time/count = " + getTimeSlashCount() + ", buffer length = " + getBufferLen() + ", bandwidth = " + getBw());
				String file = "." + File.separator + "IperfResults-" + getL4() + getMode() + getTimeSlashCount() + getBufferLen() + getBw();
				iperf.setResultFile(file);
				iperf.start(iperf.server.getHost(), getL4(), getMode(), getTimeSlashCount(), getBufferLen(), getBw());
			}
			i++;
		}
		iperf.close();
	}
	
	@TestProperties (name="Test most basic test with binding host")
	public void testSimpleMcast() throws Exception {
		iperf.setResultFile("mcast.txt");
		iperf.connect();
		report.report("Test MCAST traffic with defaults, CONT = 10 secs, buffer length = 100, bandwith = 1MBytes/s");
		iperf.server.setParallel(1);
		iperf.client.setParallel(1);
		iperf.setMcast(bindingAddress);
		iperf.startUdp(bindingAddress, EnumIperfTransmitMode.CONT, 10, 100, "1M");
		iperf.setMcast("");
		iperf.server.setParallel(0);
		iperf.close();
	}
	
	/**
	 * This test example how to use iperf with binding host
	 * in binding host you must use setParallel (bug of iperf)
	 * @throws Exception
	 */
	@TestProperties (name="Test Iperf with binding host")
	public void testIperfBindingHost() throws Exception{
		iperf.setPending(false);
		iperf.setPort(port2);
		iperf.client.setParallel(1);
		iperf.server.setParallel(1);
		iperf.server.setInterval(1);
		iperf.server.setBindHost(bindingAddress);
		iperf.init();
        iperf.connect();
              
        iperf.startServers(EnumIperfL4Protocol.UDP, 1470,true);
        iperf.startClient(bindingAddress, EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 10, 1470, "1M",25000,true);
        
        iperf.joinIperf();		
		iperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 0.8, 0.2,false), false, false);
		
		iperf.setPort(port1);
		iperf.setPending(true);
		iperf.server.setBindHost("");
		iperf.server.setParallel(0);
		iperf.close();
	}
	
	
	/**
	 * This test example how to use iperf with binding host
	 * in binding host you must use setParallel (bug of iperf)
	 * @throws Exception
	 */
	@TestProperties (name="Test simple iperf with thread")
	public void testSimpleIperfWithThread() throws Exception{
		iperf = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
		iperf.setPort(port2);
		iperf.server.setInterval(1);
		iperf.client.setIperfExec(iperfExecDir);
		iperf.server.setIperfExec(iperfExecDir);
		iperf.init();
              
        iperf.startServers(EnumIperfL4Protocol.UDP, 1470,true);
        iperf.startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 10, 1470, "1M",25000,true);
        
        iperf.joinIperf();		
		iperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 0.8, 0.2,false), false, false);
		
		iperf.setPort(port1);
		iperf.setPending(true);
		iperf.server.setBindHost("");
		iperf.server.setParallel(0);
		iperf.close();
	}
	
	/**
	 *  example of 3 iperf connection at the same time ,server and client work with thread.
	 * @throws Exception
	 */
	@TestProperties (name="multi iperf - Test Iperf with threads")
	public void testMultiIperfInTreadMode() throws Exception{
		Iperf[] iperfArray = new Iperf[3];
		 
		//init iperf and start all servers
		for (int i = 0; i < iperfArray.length; i++) {
			iperfArray[i] = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
			iperfArray[i].setPending(false);
			iperfArray[i].setPort(port1+i);
			iperfArray[i].client.setIperfExec("iperf");
			iperfArray[i].server.setIperfExec("iperf");
			iperfArray[i].server.setInterval(1);
			iperfArray[i].init();
			iperfArray[i].startServers(EnumIperfL4Protocol.UDP, 1470,true);
		}
		//start clients 
		for (int i = 0; i < iperfArray.length; i++) {
			iperfArray[i].startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 10, 1470, "1M", 24000,true);
		}
		//wait for traffic to end.
		for (int i = 0; i < iperfArray.length; i++) {
			iperfArray[i].joinIperf();
		}
		//analyze server results
		for (int i = 0; i < iperfArray.length; i++) {
			iperfArray[i].analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1,10,false),false,false);
		}
	}
	
	/**
	 *  example of 2 iperf connection at the same time in the old way server works with file and client with thread
	 * @throws Exception
	 */
	@TestProperties (name="multi iperf - Test Iperf with multi session clients in thread and server with iperf file")
	public void testMultiIperfWithIperfResultFile() throws Exception{
		//take care to cliconnection parameters
		iperf = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
		iperf.setPending(false);
		iperf.setPort(port1);
		iperf.server.setInterval(5);
		iperf.server.setIperfExec(iperfExecDir);
		iperf.client.setIperfExec(iperfExecDir);
		iperf.init();
        iperf.connect();
        
        Iperf iperf1 = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
		iperf1.setPending(false);
		iperf1.setPort(port2);
		iperf1.server.setInterval(5);
		iperf1.server.setIperfExec(iperfExecDir);
		iperf1.client.setIperfExec(iperfExecDir);
		iperf1.init();
        iperf1.connect();
        
        iperf.startServers(EnumIperfL4Protocol.UDP, 1470);
        iperf1.startServers(EnumIperfL4Protocol.UDP, 1470);

        iperf.startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 20, 1470, "1M",24000,true);
        iperf1.startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 20, 1470, "1M",24000,true);
        iperf.client.startClientThread.join();
        iperf1.client.startClientThread.join();
        
        iperf.stop();
        iperf1.stop();

        iperf.getIperfResultFile();
        iperf1.getIperfResultFile();
       
		iperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1, 30), false, false);
		iperf1.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1, 30), false, false);
	}
	
	public void testStartServersAndClientInSingleThread() throws Exception{
		iperf = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
		iperf.setPending(false);
		iperf.setPort(port1);
		iperf.server.setInterval(5);
		iperf.server.setIperfExec(iperfExecDir);
		iperf.client.setIperfExec(iperfExecDir);
		iperf.init();
        iperf.connect();
        
        iperf.startServers(EnumIperfL4Protocol.UDP, 1470);

        iperf.startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 20, 1470, "1M",24000,true);
        iperf.client.startClientThread.join();
       
        iperf.stop();
        iperf.getIperfResultFile();
   
		iperf.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, 1, 30), false, false);
	}
	
	/**
	 * This test get the best throughput performance
	 * @throws Exception
	 */
	public void testIperfBestPerformance() throws Exception{
        iperf = new Iperf(iperf.client.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS,iperf.server.getHost(), protocol, userName, password, EnumOperatinSystem.WINDOWS);
		iperf.setPending(false);
		iperf.setPort(port1);
		iperf.client.setIperfExec("iperf");
		iperf.server.setIperfExec("iperf");
		iperf.server.setInterval(1);
		iperf.init();
        iperf.connect();
        
        for (int bufLen = 32; bufLen < 2048; bufLen*=2) {
	        report.report( "The best performance is : " + String.valueOf(bestPerformence(iperf,bufLen)));
	        setMinRate(20);
	        setMaxRate(80);
	        setStopAt(0.2);
	        setTolerance(0.2);   
	    }
	}
	
	private double minRate = 20;
	private double maxRate = 70;
	private double stopAt = 0.2;
	private double tolerance = 0.2;
	
     /**
      * This function get iperf object and buffer length and return the best result that 
      * @param iperf
      * @param bufferLen
      * @return
      * @throws Exception
      */
	public double bestPerformence(Iperf iperf,int bufferLen)throws Exception{
		double actualRate;
		double bestThroughput=minRate;
		
		int index =0;
		actualRate = maxRate;
		
		while (true) {
			++index;
			
			if (maxRate - minRate < stopAt) {
				break;
			}
			
			 iperf.startServers(EnumIperfL4Protocol.UDP, bufferLen,true);
		     iperf.startClient(iperf.server.getHost(), EnumIperfL4Protocol.UDP, EnumIperfTransmitMode.CONT, 10, bufferLen, actualRate+"M",40000,true);
		  	     
		     iperf.joinIperf();  
		     
		     String textAginst = iperf.server.getTestAgainstObject().toString();
		     IperfFile iperFile = new IperfFile(textAginst);
		     iperFile.buildIperfData(EnumIperfDataType.AVERAGE);
		     
		     report.startReport("iperf index" +index, "iperf test");
		     report.addProperty("Client", iperf.client.getHost());
		     report.addProperty("Server", iperf.server.getHost());
		     report.addProperty("Expected throughput", String.valueOf(actualRate));
			 report.addProperty("buffer length", String.valueOf(bufferLen));
		 
		     double rateIGet =Double.parseDouble(iperFile.returnValue(EnumIperfCounterType.THROUGHPUT));
		     
		     rateIGet =doubleFormater(rateIGet,2); 
		     report.addProperty("Actual throughput", String.valueOf(rateIGet));
		     
		   if((rateIGet>=(actualRate-tolerance)) && (!Double.isNaN(rateIGet))){
			   bestThroughput = rateIGet;
			   minRate = actualRate;
		       report.addProperty("Result", "Pass"); 
		   }else{
			   maxRate = actualRate;
			   report.addProperty("Result", "Fail");
		   }
		   
		   actualRate =(minRate+maxRate)/2;
	   }
		
	   report.startReport("Summary client: " + iperf.client.getHost() + " server : " + iperf.server.getHost() + " with buffer length of " +bufferLen, "summary");
	   report.addProperty("Best performance", String.valueOf(bestThroughput));
	 
	   return bestThroughput;
	}
	
	/**
	 * get double number and return the number in new format.
	 * example X.XXXXXXX -> X.XX  if we used doubleFormater(x,2);
	 * @param x - The double number with many numbers after the point.
	 * @param numberAfterPoint - How much number you want to see after the point.
	 * @return
	 */
	public double doubleFormater(double x,int numberAfterPoint){
	
		String n = String.valueOf(x);
		int idx = n.indexOf('.');
		if (idx != -1 && idx < n.length()-2)
		{
		x = Double.parseDouble(n.substring(0, idx+numberAfterPoint+1));
		}
		
		return x;
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


	public double getMinRate() {
		return minRate;
	}

	public void setMinRate(double minRate) {
		this.minRate = minRate;
	}

	public double getMaxRate() {
		return maxRate;
	}

	public void setMaxRate(double maxRate) {
		this.maxRate = maxRate;
	}

	public double getStopAt() {
		return stopAt;
	}

	public void setStopAt(double stopAt) {
		this.stopAt = stopAt;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}


	public String getBindingAddress() {
		return bindingAddress;
	}


	public void setBindingAddress(String bindingAddress) {
		this.bindingAddress = bindingAddress;
	}


	public int getPort1() {
		return port1;
	}


	public void setPort1(int port1) {
		this.port1 = port1;
	}


	public int getPort2() {
		return port2;
	}


	public void setPort2(int port2) {
		this.port2 = port2;
	}


	public int getIllegalPort() {
		return illegalPort;
	}


	public void setIllegalPort(int illegalPort) {
		this.illegalPort = illegalPort;
	}


	public String getUsername() {
		return userName;
	}


	public void setUsername(String username) {
		this.userName = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getProtocol() {
		return protocol;
	}


	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}


	public EnumOperatinSystem getEnumOperatinSystem() {
		return enumOperatinSystem;
	}


	public void setEnumOperatinSystem(EnumOperatinSystem enumOperatinSystem) {
		this.enumOperatinSystem = enumOperatinSystem;
	}
	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getIperfExecDir() {
		return iperfExecDir;
	}


	public void setIperfExecDir(String iperfExecDir) {
		this.iperfExecDir = iperfExecDir;
	}

}
