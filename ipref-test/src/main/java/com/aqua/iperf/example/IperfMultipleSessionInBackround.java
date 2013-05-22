package com.aqua.iperf.example;

import com.aqua.iperf.Iperf;
import com.aqua.iperf.IperfAnalyzer;
import com.aqua.iperf.Iperf.EnumIperfL4Protocol;
import com.aqua.iperf.Iperf.EnumIperfTransmitMode;
import com.aqua.iperf.IperfAnalyzer.EnumIperfDataType;
import com.aqua.iperf.IperfCounters.EnumIperfCounterType;


/**
 * THIS EXAMPLE WAS TESTED ONLY ON LINUX STATIOS!!!
 * This class contains Muti session traffic tests that verify that Iperf class is up and running (without analysis):
 * 	Define 3 iperf sessions on different ports.
 *  Start all 3 iperf sessions.
 *  All tests must be performed with client and server on Windows and Linux.
 * 	Sleep untill all sessions terminate.
 *  Stop servers.
 *  Get Servers results file.
 *  Analyze results.
 * 	Note: not all sessions atart and end at the same time exactly. therefore its recomended to perform a long test. (time between start of the first client until the second client can get to 5 seconds). 
 * @author genuth
 */
public class IperfMultipleSessionInBackround extends IperfBase {
	
	Iperf iperf_1;
	Iperf iperf_2;
	Iperf iperf_3;
	
	int totalTrafficRate = 102400; 
	int testDuration = 120; //seconds
	
	@Override
	public void setUp() throws Exception{
		iperf_1 = (Iperf)system.getSystemObject("iperf");
		iperf_2 = (Iperf)system.getSystemObject("iperf1");
		iperf_3 = (Iperf)system.getSystemObject("iperf2");
	}
	
	public void testTP_MS_BWA_UL_BV_H003() throws Exception{
		  String osName = System.getProperty("os.name");  
		if(osName.equals("Linux")){
			iperf_1.server.setInterval(5); //default is 1 sec.
			iperf_1.runInBackground(true); //all results on Client and Server will be written into a file.
			iperf_1.setPort(6001);	
			iperf_1.connect();
			
			iperf_2.server.setInterval(5);
			iperf_2.runInBackground(true);
			iperf_2.setPort(6002);
			iperf_2.connect();
			
			iperf_3.server.setInterval(5);
			iperf_3.runInBackground(true);
			iperf_3.setPort(6003);
			iperf_3.connect();
			
			iperf_1.start(iperf_1.server.getHost(), EnumIperfL4Protocol.UDP,EnumIperfTransmitMode.CONT, testDuration, 128, (totalTrafficRate*0.09) + "K");
			iperf_2.start(iperf_2.server.getHost(), EnumIperfL4Protocol.UDP,EnumIperfTransmitMode.CONT, testDuration, 512, (totalTrafficRate*0.3) + "K");
			iperf_3.start(iperf_3.server.getHost(), EnumIperfL4Protocol.UDP,EnumIperfTransmitMode.CONT, testDuration, 1024,(totalTrafficRate*0.61) + "K");
			
			//Test must sleep until all session finish!!!
			sleep((testDuration + 3) * 1000); 
			
			iperf_1.stop(); //Server will be closed and Proccess will get killed.
			iperf_1.getIperfResultFile();
			iperf_2.stop();
			iperf_2.getIperfResultFile();
			iperf_3.stop();
			iperf_3.getIperfResultFile();
			
			iperf_1.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, (totalTrafficRate/1000*0.09), 30));
			iperf_3.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, (totalTrafficRate/1000*0.3), 30));
			iperf_3.analyze(new IperfAnalyzer(EnumIperfDataType.AVERAGE, EnumIperfCounterType.THROUGHPUT, (totalTrafficRate/1000*0.61), 30));
		}else{
			report.report("This test can work only on linux");
		}
	}
	
}
