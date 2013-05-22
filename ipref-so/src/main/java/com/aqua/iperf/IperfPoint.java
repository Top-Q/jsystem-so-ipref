package com.aqua.iperf;

import java.io.File;

import jsystem.framework.system.SystemObjectImpl;
import jsystem.utils.StringUtils;

import com.aqua.filetransfer.ftp.FTPFileTransfer;
import com.aqua.iperf.Iperf.EnumIperfPrintFormat;
import com.aqua.stations.StationDefaultImpl;
import com.aqua.stations.StationsFactory;
import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnection;
import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.WindowsDefaultCliConnection;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

/**
 * the class IperfPoint serves for instantiation of one of iperf side - server or client;
 * obtains the possibility to execute server or client iperf command
 * on the remote host using telnet connection.
 * The IperfPoint requires a pair - another IperfPoint that will execute client command for
 * server point. The same Iperf point may be server and client simultaneously running on
 * different ports. No sence in independent using of this class.
 * The class Iperf manages iperf session and uses 2 instances of IperfPoint class.
 * This class should be used in test developing.
 * The different options might be configured with the set methods.
 * @author shmir
 *
 */
public class IperfPoint extends SystemObjectImpl {
	private boolean isRunning;	
	private boolean isRunInBackground = false; //ezra
	protected String proccessId = null; //ezra - in order to kill the Server process at the end.
	
	protected FTPFileTransfer ftp = null;
	private boolean ftpUser = false;	
	
	private final String IPERF_EXEC_WINDOWS = "/com/aqua/iperf/windows/iperf.exe";
	private final String IPERF_EXEC_LINUX   = "/com/aqua/iperf/linux/iperf.exe";

	public IperfPoint(boolean ftpUser) {
		super();
		this.ftpUser = ftpUser;
	}	
	
	public void copyIperfExec() throws Exception {
		if (iperfExec == "") {
			EnumOperatinSystem os = (cliConnection instanceof WindowsDefaultCliConnection) ? EnumOperatinSystem.WINDOWS : EnumOperatinSystem.LINUX;
			String localIperfFile = (os == EnumOperatinSystem.WINDOWS) ? IPERF_EXEC_WINDOWS : IPERF_EXEC_LINUX;
			StationDefaultImpl station = StationsFactory.createStation(cliConnection.getHost(), os, cliConnection.getProtocol(), cliConnection.getUser(), cliConnection.getPassword(), cliConnection.getPrompts());
			String remoteIperfFile = station.getTempDirectory() + File.separator + "iperf.exe";
			station.copyFileFromLocalMachineToRemoteMachine(getClass().getResourceAsStream(localIperfFile), new File(remoteIperfFile));
			setIperfExec(remoteIperfFile);
		}
	}
	
	public void connect() throws Exception {
		if (ftpUser) {		
			ftp = new FTPFileTransfer((CliConnection) this.cliConnection.clone());			
		}
		copyIperfExec();
	}
	
	/**
	 *   
	 * @see command(String command,long timeout)
	 * @throws Exception
	 */
	public void command(String command) throws Exception {
		command(command,2000);
	}
	
	public void command(String command,long timeout)throws Exception{
		command(command,true,cliConnection.getUser() + ">"," ",timeout);
	}
	
	
	/**
	 * Execute string command to iperf client and server use in normal mode (not in background mode ,thread)
	 * @param command - string command
	 * @param timeout - timeout for the command
	 * @throws Exception
	 */	
	public void command(String command,boolean isFailToPass ,String expectedPrompt,String addTitle,long timeout) throws Exception {
		CliCommand cmd = new CliCommand();
		try {
			report.setFailToPass(isFailToPass);
			cmd.setTimeout(timeout);
			cmd.setCommands(new String[]{"\"" + getIperfExec() + "\" " + command});
			if (cliConnection instanceof WindowsDefaultCliConnection) {
				cmd.setPromptString(expectedPrompt);
			}
			cmd.setSuppressEcho(true);
			cliConnection.handleCliCommand(addTitle + " \"" + getIperfExec() + "\"" + command, cmd);
			
			if (addTitle.equalsIgnoreCase(" ")){
				if (!(isRunInBackground)){ //ezra
					/*
					 * If CLI command returns normally (prompt found) we assume Iperf failure.
					 * This means Iperf sessions should be set to send data for at least 2 seconds.
					 */ 
						throw new Exception("Iperf utility failed : " + cmd.getResult());
					}
			}
		} catch (Exception e) {
			// We expect Iperf to fail on timeout
			if (e.getMessage().startsWith("timeout:")) {
				setTestAgainstObject(cmd.getResult());
			} else {
				throw new Exception("Iperf utility failed : " + e.getMessage() + cmd.getResult());
			}
		} finally {
			report.setFailToPass(false);	
		}
	}
	
	/**
	 * execute enter and check for prompt
	 * if we get prompt then we know that the client has finish it's job and now we can stop the server. 
	 * @return
	 * @throws Exception
	 */		
	synchronized boolean isRunning() throws Exception {
		if (!isRunning) {
            return false;
        }
        CliCommand cmd =  new CliCommand();
        cmd.setAddEnter(false);
        cmd.setCommands(new String[]{""});
        cmd.setTimeout(1000);       
        cliConnection.command(cmd);
       
        if (StringUtils.isEmpty(cmd.getResult())){
            isRunning = false;
        }else{
            isRunning = cmd.isFailed();
        }
        return isRunning;
	}
	
	synchronized void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning; 
	}
	
	public void setCliConnection(CliConnectionImpl cliConnection) {
		this.cliConnection = cliConnection;
	}
	public CliConnectionImpl getCliConnection() {
		return cliConnection;
	}
	public CliConnectionImpl cliConnection;

	public String getHost() {	
		return cliConnection.getHost();
	}

	public void setIperfExec(String iperfExec) {
		this.iperfExec = iperfExec;
	}
	public String getIperfExec() {
		return iperfExec;
	}
	String iperfExec = "";
	
	/**
	 * The server port for the server to listen on and the client to connect to.
	 * This should be the same in both client and server.
	 * @param port	port to listen on, default is 5001, the same as ttcp.
	 */
	public void setPort(int port){
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	private int port = 5001;
	
	/**
	 * Bind to host, one of this machine's addresses.
	 * For the client this sets the outbound interface.
	 * For a server this sets the incoming interface.
	 * This is only useful on multihomed hosts, which have multiple network interfaces.  
	 * For Iperf in UDP server mode, this is also used to bind and join to a multicast group. Use addresses in the range 224.0.0.0 to 239.255.255.255 for multicast.
	 * @param bindHost	host IP address to bind to, default is host IP address
	 */	
	public void setBindHost(String bindHost){
		this.bindHost = bindHost;
	}
	public String getBindHost() {
		return bindHost;
	}
	private String bindHost = "";

	/**
	 * Sets the socket buffer sizes to the specified value.
	 * For TCP, this sets the TCP window size.
	 * For UDP it is just the buffer which datagrams are received in, and so limits the largest receivable datagram size.
	 * @param windowSize	window size in the format x[KM], default is 128K
	 */
	public void setWindowSize( String windowSize ) {
		this.windowSize = windowSize;
	}
	public String getWindowSize() {
		return windowSize;
	}
	private String windowSize = "128K";

	/**
	 * Print format (see EnumIperfPrintFormat for description).
	 * Note that since the current version of the SO does not save the client results file this parameter
	 * is relevant for server only.
	 * @param format	format to use, default is Mbits/sec
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
	 * If non-zero, a report is made every interval seconds of the bandwidth since the last report.
	 * If zero, no periodic reports are printed.
	 * Note that since the current version of the SO does not save the client results file this parameter
	 * is relevant for server only.
	 * @param interval	interval in seconds, default is 1 second.
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getInterval() {
		return interval;
	}	
	private int interval = 1;

	public boolean isRunInBackground() { //ezra
		return isRunInBackground;
	}

	public void setRunInBackground(boolean isRunInBackground) { //ezra
		this.isRunInBackground = isRunInBackground;
	}

	public boolean isFtpUser() {
		return ftpUser;
	}

	public void setFtpUser(boolean ftpUser) {
		this.ftpUser = ftpUser;
	}

}

