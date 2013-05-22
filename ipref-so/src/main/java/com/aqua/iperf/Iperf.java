package com.aqua.iperf;

import java.io.File;

import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemObjectImpl;

import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

/**
 * The Iperf class manages an Iperf session between multiple hosts.
 * The class supports single client and multiple servers (for MCAST).
 * 
 * The main operations of the class are:
 *	Connect - connect to Iperf client and servers and make sure Iperf is installed.
 *            If not installed the method will install it, currently supported only on Windows).
 *  Start   - start Iperf session on servers and client.
 *            After client finishes the method stops the servers and copies the results file to the test machine.  
 * 
 * There are few use cases to this class:
 * Client and server CLI connections are implicitly created by the platform where
 * 		the parameters are taken from the SUT XML (see example FullSut*.xml)
 * Client and server CLI connections are created by the test and given to the system object
 * 		(see example MinSut*.xml)
 * Client and server CLI connections are explicitly created by the platform where the
 * 		parameters are given by the test.  
 * 
 * Work with Iperf 1.7.0.
 * In order to use TOS please use iperf 1.7.0_TOS_Enabled.
 *  
 * @author shmir
 */
public class Iperf extends SystemObjectImpl {
	
	/**
	 * Chose your layer 4 protocol:
	 * TCP - Transmission Control Protocol 
	 * UDP - User Datagram Protocol 
	 */
	public static enum EnumIperfL4Protocol {    
		TCP(""),
		UDP("-u");
		EnumIperfL4Protocol(String value) {
			this.value=value;
		}
		private String value;
		public String value(){return value;}
		public static EnumIperfL4Protocol reverseValue(String value) {
			for (EnumIperfL4Protocol e : EnumIperfL4Protocol.values()) {
				if (e.value() == value) {
					return e;
				}	
			}
			return null;
		}
	}
	
	/**
	 * Transmit duration - time or # bytes 
	 * CONT - time in seconds to transmit for (default 10 secs). 
	 * BURST - number of bytes to transmit.
	 */
	public static enum EnumIperfTransmitMode {    
		CONT("-t"),
		BURST("-n");
		EnumIperfTransmitMode(String value) {
			this.value=value;
		}
		private String value;
		public String value(){return value;}
	}
	
	/**
	 * Specifying the format to print bandwidth numbers in.  
	 * NOTE: here Kilo = 1024, Mega = 1024^2 and Giga = 1024^3 when dealing with bytes.
	 * Commonly in networking, Kilo = 1000, Mega = 1000^2, and Giga = 1000^3 so we use this when dealing with bits.
	 * If this really bothers you, use -f b and do the math.
	 * @author aqua
	 */
	public static enum EnumIperfPrintFormat {    
		BITS("b"),		// bits/sec
		BYETS("B"),		// Bytes/sec
		KBITS("k"),		// Kbits/sec
		KBYTES("K"),	// KBytes/sec
		MBITS("m"),		// Mbits/sec
		MBYTES("M"),	// MBytes/sec
		GBITS("g"),		// Gbits/sec
		GBYTES("G");	// GBytes/sec
		EnumIperfPrintFormat(String value) {
			this.value=value;
		}
		private String value;
		public String value(){return value;}
	}
	
	/**
	 * IP header type IPV4 or IPV6
	 */
	public static enum EnumIperfIpVersion {    
		IPV4("ipV4"),
		IPV6("ipV6");
		EnumIperfIpVersion(String value) {
			this.value=value;
		}
		private String value;
		public String value(){return value;}
	}
	
	public IperfClient   client  = null;
	public IperfServer   server  = null;
	public IperfServer[] servers = null;
    private boolean isClientEnd = false;  //flag indicating that the client has finished sending traffic (session end)
    private EnumIperfIpVersion ipVersion =  EnumIperfIpVersion.IPV4; 

    /**
	 * This constructor is used by the JSystem platform when implicitly instantiating new IPerf system object
	 * from SUT XML.
	 * The init() method will be called implicitly as well.
	 * Note that if the test explicitly instantiates new IPerf object using this constructor it should also
	 * call "init()" explicitly.
	 */
	public Iperf() {
		super();
	}

	/**
	 * This constructor is used by a test when the test already has CLI connection objects and wishes the
	 * IPerf to reuse the objects.
	 * 
	 * @param clientCli		client side CLI connection object	
	 * @param serverCli		server side CLI connection object
	 * @throws Exception
	 */
	public Iperf(CliConnectionImpl clientCli, CliConnectionImpl serverCli) throws Exception {
		this(clientCli, new CliConnectionImpl[]{serverCli});
	}

	/**
	 * This constructor is used by a test when the test already has CLI connection objects and wishes the
	 * IPerf to reuse the objects.
	 * 
	 * @param clientCli		client side CLI connection object	
	 * @param serversCli[]	servers side CLI connection object
	 * @throws Exception
	 */
	public Iperf(CliConnectionImpl clientCli, CliConnectionImpl[] serversCli) throws Exception {

		super();

		client = new IperfClient();
		client.setCliConnection(clientCli);
		client.setParent(this);
		
		servers = new IperfServer[serversCli.length];
		for (int i = 0; i < servers.length; i++) {
			servers[i] = new IperfServer();
			servers[i].setCliConnection(serversCli[i]);
			servers[i].setParent(this);
		}
		
		init();
   	
	}
	
	/**
	 * This constructor is used by a test when the test doesn't have CLI connection objects and wishes the
	 * IPerf to create the objects.
	 * 
	 * @param clientHost - ip address of the client	 
	 * @param clientCliProtocol - with which protocol you want to connect to the host telnet or ssh 
	 * @param clientUser - username of the client host
	 * @param clientPassword - password of the client host
	 * @param clientOS - operation system name windows,linux of the client
	 * @param serverHost - ip address of the server	 
	 * @param serverCliProtocol - with which protocol you want to connect to the host telnet or ssh 
	 * @param serverUser - username of the server host.
	 * @param serverPassword - password of the server host
	 * @param serverOS - operation system name windows,linux of the server
	 * @throws Exception
	 */
	public Iperf(String clientHost, String clientCliProtocol, String clientUser, String clientPassword, EnumOperatinSystem clientOS, 
			     String serverHost, String serverCliProtocol, String serverUser, String serverPassword, EnumOperatinSystem serverOS) throws Exception {
		super();
		
		client = new IperfClient(clientHost,clientCliProtocol,clientUser,clientPassword,clientOS);
		servers = new IperfServer[1];
        servers[0] = new IperfServer(serverHost,serverCliProtocol,serverUser,serverPassword,serverOS);
	
        client.setParent(this);
        servers[0].setParent(this);
		
		init();
	}
	
	/**
	 * On top of standard system object init(), this init() makes sure that server == servers[0]
	 * so the test developer can use both references alternatively.
	 */
	@Override
	public void init() throws Exception {
		super.init();
		if (server != null) {
			servers = new IperfServer[]{server};
		} else {
			server = servers[0];
		}
	}
	
	/**
	 * Prepare Iperf for execution on client and servers.
	 * This method should be invoked once, before starting Iperf for the first time on each test.
	 * @throws Exception
	 * TODO: when using Threads, there is no need to init FTP so make sure FTP is init
	 */
	public void connect() throws Exception {
		client.connect();
		for (int i = 0; i < servers.length; i++) {
			servers[i].connect();
		}
	}
	
	/**
	 * Start Iperf in UDP mode on all client and servers.
	 * At the end - copy results files from all servers, then stop Iperf on all servers.
	 * Use this method ONLY when working in single thread.
	 * 
	 * @param destIp
	 * @param mode
	 * @param timeOrNumber
	 * @param bufferLen
	 * @param bandwidth
	 * @throws Exception
	 */
	public void startUdp(String destIp, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth) throws Exception {
		start(destIp, EnumIperfL4Protocol.UDP, mode, timeOrNumber, bufferLen, bandwidth);
	}

	/**
	 * Start Iperf in TCP mode on all client and servers.
	 * At the end - copy results files from all servers, then stop Iperf on all servers.
	 * Use this method ONLY when working in single thread.
	 * 
	 * @param destIp
	 * @param mode
	 * @param timeOrNumber
	 * @param bufferLen
	 * @param bandwidth
	 * @throws Exception
	 * TODO TCP does not support bandwidth, remove parameter
	 */
	public void startTcp(String destIp, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen) throws Exception {
		start(destIp, EnumIperfL4Protocol.TCP, mode, timeOrNumber, bufferLen, null);
	}
	
	/**
	 * Start Iperf on all client and servers.
	 * At the end - copy results files from all servers, then stop Iperf on all servers.
	 * Use this method ONLY when working in single thread.
	 * 
	 * @param destIp
	 * @param layer4protocol
	 * @param mode
	 * @param timeOrNumber
	 * @param bufferLen
	 * @param bandwidth
	 * @throws Exception
	 */
	public void start(String destIp, EnumIperfL4Protocol layer4protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth) throws Exception {
		startServers(layer4protocol, bufferLen, false);
		startClient(destIp, layer4protocol, mode, timeOrNumber, bufferLen, bandwidth, 2000, false);
		
		if (isPending()) {
			while (client.isRunning()) {
				Thread.sleep(2000);
			}
			stop();
			getIperfResultFile();
		}
	}
	
	/**
	 * @see startServers(EnumIperfL4Protocol enumIperfL4Protocol, int bufferLen, boolean isThread) throws Exception
	 * 
	 * @param enumIperfL4Protocol
	 * @param bufferLen
	 * @throws Exception
	 */
	public void startServers(EnumIperfL4Protocol enumIperfL4Protocol, int bufferLen) throws Exception {
		startServers(enumIperfL4Protocol, bufferLen, false);
	}
	
	/**
	 * Start Iperf on all servers
	 *  
	 * @param layer4protocol
	 * @param bufferLen
	 * @throws Exception
	 */
	public void startServers(EnumIperfL4Protocol enumIperfL4Protocol, int bufferLen, boolean isThread) throws Exception {
		setOutputFile("iperf_" + System.currentTimeMillis() + ".txt"); //ezra
		setResultFile("ResultIperf_" + System.currentTimeMillis() + ".txt"); //ezra
		for (int i = 0; i < servers.length; i++) {
			servers[i].start(enumIperfL4Protocol, bufferLen, getOutputFile(), isThread);
		}	
	}
	
	/**
	 * Start Iperf on the client.
	 *
	 * @param destIp
	 * @param layer4protocol
	 * @param mode
	 * @param timeOrNumber
	 * @param bufferLen
	 * @param bandwidth
	 * @throws Exception
	 */
    public void startClient(String destIp, EnumIperfL4Protocol enumIperfL4Protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth) throws Exception {
    	startClient(destIp,enumIperfL4Protocol,mode,timeOrNumber,bufferLen,bandwidth,2000);
    }
	
	/**
	 * Start Iperf on the client.
	 * 
	 * @param destIp
	 * @param layer4protocol
	 * @param mode
	 * @param timeOrNumber
	 * @param bufferLen
	 * @param bandwidth
	 * @throws Exception
	 */
    public void startClient(String destIp, EnumIperfL4Protocol enumIperfL4Protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth,long timeout) throws Exception {		
    	client.start(destIp, enumIperfL4Protocol, mode, timeOrNumber, bufferLen, bandwidth,timeout,false);
	}
    
    /**
     * Start Iperf on the client.
     * @param destIp - the destination ip 
     * @param enumIperfL4Protocol - l4 protocol tcp/udp
     * @param mode 
     * @param timeOrNumber 
     * @param bufferLen - The buffer length 32,64,128
     * @param bandwidth - 
     * @param timeout
     * @param isThread - true run the start client in thread false run it normal.
     * @throws Exception
     */
    public void startClient(String destIp, EnumIperfL4Protocol enumIperfL4Protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth, long timeout, boolean isThread) throws Exception {		
    	client.start(destIp, enumIperfL4Protocol, mode, timeOrNumber, bufferLen, bandwidth,timeout,isThread);
	}
	
    /**
     * get the iperf result file add it to the report log
     * and set it to the test aginst object
     * @throws Exception
     */
	public  void  getIperfResultFile() throws Exception{		
		for (int i = 0; i < servers.length; i++) {
			File f = servers[i].getResultFile(getResultFile(), getOutputFile());
			ReporterHelper.copyFileToReporterAndAddLink(report, f, "Iperf Result File = " + f.getPath());
			servers[i].setTestAgainstObject(f);
		}
		setTestAgainstObject(servers[0].getTestAgainstObject());
	}
	
	public boolean isRunning() throws Exception{
		servers[0].command("");
		return client.isRunning();
	}
	
	/**
	 * join to the server and client threads until they die. 
	 * @throws Exception
	 */
	public void joinIperf()throws Exception{	 
		 server.startServerThread.join();
		 client.startClientThread.join();
	}
	
	/**
	 * Stops Iperf session by sending ^C to the terminal.
	 * Normally there is no need to stop the client since it simply finishes to send and exists.
	 * However, the servers must be stopped.
	 * Should the test wish to stop clients before they end, it should call IperfClient.stop() directly.
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		for (int i = 0; i < servers.length; i++) {
			servers[i].stop();
		}
	}

	public boolean isPending() {
		return pending;
	}
	public void setPending(boolean pending) {
		this.pending = pending;
	}
	private boolean pending = true;	 //wait before stop the server listening

	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	public String getResultFile() {
		return resultFile;
	}
	private String resultFile = "iperf.txt"; 

	public void setOutputFile(String outputFile) {		
		this.outputFile = outputFile;		
	}
	public String getOutputFile() {
		return outputFile;
	}
	private String outputFile = "iperf.txt";

	public void setPort(int port) {
		client.setPort(port);
		if (servers != null) {
			for (int i = 0; i < servers.length; i++) {
				servers[i].setPort(port);
			}			
		} else {
			server.setPort(port);
		}
	}
	public int getPort() {
		return client.getPort();
	}

	/**
	 * Note that there is no notion of MCAST in iperf.
	 * For Iperf in UDP server mode, the Bind Host parameter is also used to bind and join to a multicast group.
	 * The addresses in the range 224.0.0.0 to 239.255.255.255 are used for multicast.
	 * This method simply wraps the setBindHost() to highlight the fact that the test uses mcast but the test developer can use bindHost directly
	 * @param mcast
	 */
	public void setMcast(String mcast) {
		for (int i = 0; i < servers.length; i++) {
			servers[i].setBindHost(mcast);
		}
	}
	public void runInBackground(boolean mode) { //ezra
		setPending(false);
		client.setRunInBackground(mode);
		for (int i = 0; i < servers.length; i++) {
			servers[i].setRunInBackground(mode);
		}
	}

	public boolean isClientEnd() {
		return isClientEnd;
	}

	public void setClientEnd(boolean isClientEnd) {
		this.isClientEnd = isClientEnd;
	}

	public EnumIperfIpVersion getIpVersion() {
		return ipVersion;
	}

	public void setIpVersion(EnumIperfIpVersion ipVersion) {
		this.ipVersion = ipVersion;
	}
	
}