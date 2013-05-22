package com.aqua.iperf;

/**
 * This class execute server command in thread mode
 * @author guy levi
 *
 */
public class ServerThread extends Thread {
	public IperfServer server;
    /**
     * start server in thread mode
     * @param server - reference  for the iperf server
     * @param protocol - protocol layer 4 tcp/udp
     * @param bufferLen - buffer length 32,64,128....
     */
	public ServerThread(IperfServer server){
		this.server = server;
		this.setPriority(10);
	}
	
	@Override
	public void run(){	
		try{
			server.executeCommand(true);	
		    
			while (!((Iperf)server.getParent()).isClientEnd()){ //waits until the client thread will finish
			  sleep(1000);
			}
			
			server.stop(true); //stop server listening port
			((Iperf)server.getParent()).setClientEnd(false); //init clientEnd back to false
				
		}catch(Exception e){
			System.out.println(e.getMessage());
			
		}
		
		
	}
	
}
