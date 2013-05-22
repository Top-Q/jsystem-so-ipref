package com.aqua.iperf;

import com.aqua.iperf.Iperf.EnumIperfIpVersion;
import com.aqua.iperf.Iperf.EnumIperfL4Protocol;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;
import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.CliFactory;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

public class IperfClient extends IperfPoint {
	public ClientThread startClientThread;
	private long timeout;
	private String destIp;
	private EnumIperfL4Protocol protocol;
	private EnumIperfTransmitMode mode;
	private int timeOrNumber;
	private int bufferLen;
	private String bandwidth;
	
	public IperfClient() {
		super(false);
	}
	
	public IperfClient(String clientHost , String clientCliProtocol,String clientUser,String clientPassword,EnumOperatinSystem clientOS)throws Exception{
		super(false);
		CliConnectionImpl clientConnection = CliFactory.createCliConnection(clientOS);
		clientConnection.setHost(clientHost);
		clientConnection.setProtocol(clientCliProtocol);
		clientConnection.setUser(clientUser);
		clientConnection.setPassword(clientPassword);
		
		setCliConnection(clientConnection);
	}
	
	/**
	 * Start the iperf client and saves the result into file.
	 * @param destIp -  The ip address of the your iperf server. 
	 * @param protocol - tcp/udp
	 * @param mode -  can be 2 options first CONT that represent -t time in seconds to transmit for (default 10 secs) or BURST that represent -n (number of bytes to transmit)
	 * @param timeOrNumber - time in seconds to transmit the time will be relevant only if the mode will br CONT 
	 * @param bufferLen - length of buffer to read or write
	 * @param bandwidth - for UDP, bandwidth to send at in bits/sec
	 * @throws Exception
	 */
	private synchronized void start(String destIp, EnumIperfL4Protocol protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth) throws Exception {
		start(destIp,protocol,mode,timeOrNumber,bufferLen,bandwidth,2000,false);
	}
	/**
	 * start the iperf file in thread mode and saves the result in the iperf.client object 
	 * @param destIp -  The ip address of the your iperf server. 
	 * @param protocol - tcp/udp
	 * @param mode -  can be 2 things first CONT that represent -t time in seconds to transmit for (default 10 secs) or BURST that represent -n (number of bytes to transmit)
	 * @param timeOrNumber - time in seconds to transmit the time will be relevant only if the mode will br CONT 
	 * @param bufferLen - length of buffer to read or write
	 * @param bandwidth - for UDP, bandwidth to send at in bits/sec
	 * @param timeout - timeout for the cli command
	 * @param isThread - true will run the start function in thread mode false will run it in normal mode
	 * @throws Exception
	 */
	public synchronized void start(String destIp, EnumIperfL4Protocol protocol, EnumIperfTransmitMode mode, int timeOrNumber, int bufferLen, String bandwidth,long timeout, boolean isThread) throws Exception {
		setDestIp(destIp);
		setProtocol(protocol);
		setMode(mode);
		setTimeOrNumber(timeOrNumber);
		setBufferLen(bufferLen);
		setBandwidth(bandwidth);
		setTimeout(timeout);
		
		if (isThread) {    
			ClientThread startClient = new ClientThread(this);
			startClient.start();
			this.startClientThread = startClient;
		} else {
			executeCommand();
		}			
	}
	
	/**
	 * execute command in normal mode 
	 * @throws Exception
	 */
	public void executeCommand() throws Exception {
		executeCommand(false);
	}
	
	/**
	 *  execute command
	 * @param isThread - true will run the command in thread mode false will run it in normal 
	 * @throws Exception
	 */
	public void executeCommand(boolean isThread) throws Exception {
		if (!cliConnection.isConnected()) {
			cliConnection.connect();
		}
		/*
		 * Create an Iperf client command in the format:
		 * iperf -c IP -p port [-u] -t|-n time|number -l length -i seconds -w size -T hops -S TOS -P connections -B host -F file -f format [-b bits/sec]
		 *  + " -B " + getBindHost() 
		 */ 
		String command = " -c " + destIp + " -p " + getPort();
		
		if (protocol == EnumIperfL4Protocol.UDP){	
			command += " " + protocol.value() + " -b " + bandwidth;
		}else{
			//if tcp and also you have window size 
			if(!getWindowSize().equalsIgnoreCase("")){
				command += " -w " + getWindowSize() ;
			}
		}
		
		if (((Iperf)getParent()).getIpVersion() == EnumIperfIpVersion.IPV6){
			command +=  " -V";
		}
		
		command += " " + mode.value() + " " + timeOrNumber + " -l " + bufferLen + " -i " + getInterval() + " -T " + getTtl() + " -S " + getTos() + " -P " + getParallel() + " -f " + getFormat().value();
		
		if (getInputFile() != "") {
			command += " -F " + getInputFile();
		}
		
		if(isRunInBackground()){//ezra
			command += " > " +  getInputFile() + " &";
		}
			
        if (isThread){
          clientThreadCommand(command, 60000);	
        } else {
          command(command, timeout);	
        }
		
		setIsRunning(true);
	}
	
	/**
	 * run client command in thread mode
	 * @param command
	 * @param timeout
	 * @throws Exception
	 */
	void clientThreadCommand(String command, long timeout) throws Exception {
		command(command, false, cliConnection.getUser() + ">", "Start client", timeout);
	}
	
	/**
	 * The time-to-live for outgoing multicast packets.
	 * This is essentially the number of router hops to go through, and is also used for scoping.
	 * @param ttl	time to live, default is 1
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public int getTtl() {
		return ttl;
	}
	private int ttl = 1;
	
	/**
	 * The type-of-service for outgoing packets. 
	 * You may specify the value in hex with a '0x' prefix, in octal with a '0' prefix, or in decimal.
	 * @param top	type of service, default is 0
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
	 * Requires thread support on both the client and server.
	 * @param	parallelConns number of parallel connections, default is 1
	 */
	public void setParallel(int parallelConns) {
		this.parallelConns = parallelConns;
	} 	
	public int getParallel() {
		return parallelConns;
	}
	private int parallelConns = 1;
	
	/**
	 * Use a representative stream to measure bandwidth.
	 * @param inputFile	path to input file, default is none
	 */
	public void setInputFile(String inputFile){
		this.inputFile = inputFile;
	}
	public String getInputFile() {
		return inputFile;
	}
	private String inputFile = "";

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getDestIp() {
		return destIp;
	}

	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}

	public EnumIperfL4Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(EnumIperfL4Protocol protocol) {
		this.protocol = protocol;
	}

	public EnumIperfTransmitMode getMode() {
		return mode;
	}

	public void setMode(EnumIperfTransmitMode mode) {
		this.mode = mode;
	}

	public int getTimeOrNumber() {
		return timeOrNumber;
	}

	public void setTimeOrNumber(int timeOrNumber) {
		this.timeOrNumber = timeOrNumber;
	}

	public int getBufferLen() {
		return bufferLen;
	}

	public void setBufferLen(int bufferLen) {
		this.bufferLen = bufferLen;
	}

	public String getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(String bandwidth) {
		this.bandwidth = bandwidth;
	}

	public int getParallelConns() {
		return parallelConns;
	}

	public void setParallelConns(int parallelConns) {
		this.parallelConns = parallelConns;
	}
}

