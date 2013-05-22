package com.aqua.iperf;

import systemobject.terminal.BufferInputStream;
import systemobject.terminal.Cli;
import systemobject.terminal.Prompt;
import systemobject.terminal.RS232;
import systemobject.terminal.SSH;
import systemobject.terminal.Telnet;

import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.Position;

public class IperfCliConnection extends CliConnectionImpl {

	@Override
	public void init() throws Exception {
		super.init();
		
		setConnectOnInit(false);
	}
	
	@Override
	public Position[] getPositions() {
		return null;
	}

	@Override
	public Prompt[] getPrompts() {
			
		Prompt[] p = new Prompt[3];
		
		p[0] = new Prompt();
		p[0].setPrompt("login:");
		p[0].setStringToSend(user);
			
		p[1] = new Prompt();
		p[1].setPrompt("password:");
		p[1].setStringToSend(password);
				
		p[2] = new Prompt();
		p[2].setPrompt(">");
		p[2].setCommandEnd(true);

		return p;
		
	}
	
	@Override
	public void connect() throws Exception{
		
        if (host == null){
            throw new Exception("Default connection ip/comm is not configured");
        }
        report.report("Init cli, host: " + host );
        if(dummy){
        	return;
        }
       // Terminal t;
        boolean isRs232 = false;
        if (host.toLowerCase().startsWith("com") || protocol.toLowerCase().equals("rs232")){ //syntax for com conneciton found
        	isRs232 = true;
            String[] params = host.split("\\;");
            if (params.length < 5){
                throw new Exception("Unable to extract parameters from host: " + host);
            }
            terminal = new RS232(params[0],
                    Integer.parseInt(params[1]),
                    Integer.parseInt(params[2]),
                    Integer.parseInt(params[3]),
                    Integer.parseInt(params[4]));
        }else if (protocol.toLowerCase().equals("ssh")){
        	terminal =  new SSH(host, user, password);
        }else {
        	terminal = new Telnet(host, port, useTelnetInputStream);
        	
        	((Telnet)terminal).setVtType(null);

        }
        cli = new Cli(terminal);
        cli.setEnterStr("\r\n");
        if(useBuffer){
        	buffer = new BufferInputStream();
        	terminal.addFilter(buffer);
        	buffer.startThread();
        }
        Prompt[] prompts = getPrompts();
        for (int i = 0; i < prompts.length; i++){
        	cli.addPrompt(prompts[i]);
        }
        if(isRs232){
        	cli.command("");
        } else {
            cli.login();
        }
        connected = true;
    }
	
}
