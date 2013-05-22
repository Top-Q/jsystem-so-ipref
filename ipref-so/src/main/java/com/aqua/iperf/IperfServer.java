package com.aqua.iperf;

import java.io.File;

import systemobject.terminal.Prompt;

import com.aqua.iperf.Iperf.EnumIperfIpVersion;
import com.aqua.iperf.Iperf.EnumIperfL4Protocol;
import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.CliFactory;
import com.aqua.sysobj.conn.CliFactory.EnumOperatinSystem;

/**
 * IperfServer represents Iperf server point.
 * There are three types of parameters:
 * 1. Mutual configuration parameters for client(s) and server are defined in Iperf class and are set using setters.
 * 2. Server configuration parameters are defined using inhere and are set using setters. 
 * 3. Per session (run) parameters that are set in the start command.
 * Note: the distinction between "configuration" and "per session" is somewhat arbitrary and subjective.
 * @author aqua
 */
public class IperfServer extends IperfPoint {
	public ServerThread startServerThread;
	public EnumIperfL4Protocol protocol;
	public int bufferLen;
	public String outFile;
	
	public String getOutFile() {
		return outFile;
	}

	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}

	public IperfServer() {
		super(true);
	}	
	
	public IperfServer(String serverHost, String serverCliProtocol,String serverUser,String serverPassword,EnumOperatinSystem serverOS)throws Exception{
		super(true);
		CliConnectionImpl serverConnection = CliFactory.createCliConnection(serverOS);
		serverConnection.setHost(serverHost);
		serverConnection.setProtocol(serverCliProtocol);
		serverConnection.setUser(serverUser);
		serverConnection.setPassword(serverPassword);
		setCliConnection(serverConnection);
	}
	
	public EnumIperfL4Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(EnumIperfL4Protocol protocol) {
		this.protocol = protocol;
	}

	public int getBufferLen() {
		return bufferLen;
	}

	public void setBufferLen(int bufferLen) {
		this.bufferLen = bufferLen;
	}

	/**
	 *  run start without using thread save result into file.
	 * @param protocol
	 * @param bufferLen
	 * @param outFile
	 * @throws Exception
	 */	
	public void start(EnumIperfL4Protocol protocol, int bufferLen, String outFile) throws Exception {	
		start(protocol, bufferLen,outFile,false);
	}

	/**
	 * run start in thread mode.
	 * @param protocol
	 * @param bufferLen
	 * @param isThread
	 * @throws Exception
	 */
	public void start(EnumIperfL4Protocol protocol, int bufferLen, boolean isThread) throws Exception {	
		start(protocol, bufferLen,null,isThread);
	}
	
	/**
	 *  run start using file or thread . 
	 *  
	 * @param protocol - protocol layer 4 tcp/udp
	 * @param bufferLen  - buffer length 32,64,128....
	 * @param outFile   - name of the out file
	 * @param isThread - run the function in thread mode
	 * @throws Exception
	 */
	public void start(EnumIperfL4Protocol protocol, int bufferLen, String outFile,boolean isThread) throws Exception {	
		setProtocol(protocol);
		setBufferLen(bufferLen);
		setOutFile(outFile);

		if (isThread){
			ServerThread startServerThread = new ServerThread(this);
			startServerThread.start();
			this.startServerThread = startServerThread;
		} else {
			  executeCommand();	
		}
	}
	
   /**
    * Execute command 
    * @throws Exception
    */
	public void executeCommand() throws Exception {
		executeCommand(false);
	}
	
	/**
	 * execute command 
	 * @param isThread - true run command in thread way false run command with iperf result file and ftp.
	 * @throws Exception
	 */
	public void executeCommand(boolean isThread) throws Exception {
		String command = "";
			if (!cliConnection.isConnected()) {
				cliConnection.connect();
			}

			command =" -s -p " + getPort();
			/*
			 * Create an Iperf server command in the format iperf -s -p port [-u] -B host -w size -l size -i seconds -fm > file
			 * " -B " + getBindHost() + 
			 */
			Prompt[] sp = cliConnection.getPrompts();
			cliConnection.setPrompts(null);
			
			if (!getBindHost().equalsIgnoreCase("")){
				command += " -B " + getBindHost();
			}
			
			if(protocol.value().equalsIgnoreCase("")){ //if tcp
				command +=  " " + protocol.value(); 
				if (!getWindowSize().equalsIgnoreCase("")){ //if add window size only in tcp
					command+=" -w " + getWindowSize();
				}
			}else{
				command +=  " " + protocol.value(); 
			}
			
			if (((Iperf)getParent()).getIpVersion() == EnumIperfIpVersion.IPV6){
				command +=  " -V";
			}
			
			if (getParallel()>0){  
				command +=  " -P " + getParallel();
			}
			
			if (!(isRunInBackground())) {//ezra	
				if (isThread){
					command += " -l " + bufferLen + " -i " + getInterval()+ " -f " + getFormat().value();
					serverThreadCommand(command,12000);
				}else{
				   command += " -l " + bufferLen + " -i " + getInterval() + " -f " + getFormat().value() +  " > " + getOutFile();
				   command(command);
				}
			}else{
				command +=" -l " + bufferLen + " -i " + getInterval()+ " -f " + getFormat().value()+  " > " + getOutFile() + " &";
				command(command);	
		
				String result = (String) getTestAgainstObject();
			    String[] lines = result.split("\n");
			    proccessId = new String(lines[lines.length - 2].substring(lines[lines.length - 2].indexOf(' ')));		
			}	
		
			cliConnection.setPrompts(sp);
	}
	
	/**
	 * execute server command.
	 * @param command
	 * @param timeout
	 * @param ExpectedPrompt
	 * @param failToPass
	 * @throws Exception
	 */
    public void serverThreadCommand(String command,long timeout) throws Exception {
    	command(command,false,"-","Start server",timeout);
	}
		
	public File getResultFile(String resultFile, String outPutFile) throws Exception {
		ftp.init();		
		ftp.setAscii(true);
		ftp.copyFileFromRemoteMachineToLocalMachine(new File(outPutFile), new File(resultFile));
		/*
		 * 18/6/06 when working on linux and not closing ftp session,
		 * the system object will fail when ftp reaches max ftp connection 
		 * limit.
		 */
		ftp.close();
		return new File(resultFile);
	}
	
	/**
	 * send ctrl c and stop the server from listen to the port.
	 * @throws Exception
	 */
	public void stop(boolean printResultToReport) throws Exception {
		 CliCommand cmd = new CliCommand();
		
		   if (printResultToReport){
			   cmd.setCommand("\u0003");
			   cliConnection.handleCliCommand("Stop server", cmd);
			   setTestAgainstObject(cliConnection.getTestAgainstObject().toString());		   
			   ((Iperf)getParent()).setTestAgainstObject(cliConnection.getTestAgainstObject().toString());
		   }else{
			   cmd.setCommands(new String[]{new String(new byte[]{3})});
			   cliConnection.command(cmd);
			   cliConnection.command(cmd); //ezra - in case the ctrl c didnt work.
		   }
		   
		   if (isRunInBackground() && proccessId != null){ //ezra
			   cmd.setCommands(new String[]{"kill " + proccessId});
			   cliConnection.command(cmd);
		   }
	}
	/**
	 * @see stop(boolean printResultToReport)
	 * @throws Exception
	 */
	public void stop() throws Exception {
		stop(false);
	}
		
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
	
	private int parallelConns=0; //0 means listen to port until the user stop it.
	
}

