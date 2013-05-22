package com.aqua.iperf;

/**
 * This class execute client command in thread mode
 * @author guy levi
 *
 */
public class ClientThread extends Thread {
	public IperfClient client; 
	
	/**
	 * @param client - reference for the iperf client
	 * @param destIp -  The ip address of the your iperf server. 
	 * @param protocol - tcp/udp
	 * @param mode -  can be 2 things first CONT that represent -t time in seconds to transmit for (default 10 secs) or BURST that represent -n (number of bytes to transmit)
	 * @param timeOrNumber - time in seconds to transmit the time will be relevant only if the mode will br CONT 
	 * @param bufferLen - length of buffer to read or write
	 * @param bandwidth - for UDP, bandwidth to send at in bits/sec
	 * @param timeout - timeout for the cli command
	 */
	public ClientThread(IperfClient client){
		this.client =client;
		this.setPriority(5);
	}
	
	@Override
	public void run() {
		try {
			client.executeCommand(true);
	  }catch(Exception e){
		  System.out.println(e.getMessage());
	  }
	  
	  ((Iperf)client.getParent()).setClientEnd(true); //the client end it actions .
	  
	}
}
