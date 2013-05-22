package com.aqua.iperf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aqua.iperf.IperfAnalyzer.EnumIperfDataType;
import com.aqua.iperf.IperfCounters.EnumIperfCounterType;

/**
 * This class parses Iperf results file.
 * It extracts the following values:
 * Throughput
 * Jitter
 * Loosses
 * 
 */
public class IperfFile {
    Properties iperfRequestedData = new Properties();
	private String textAgainst;

	final String INTERNAL = "(\\d*\\.\\d*\\s*-\\s*\\d+\\.\\d+\\s+\\w+)";
	final String TRANSFER = "((\\d+\\.*\\d+)\\s+\\w+)";
	final String THROUGHPUT = "((\\d+\\.*\\d+)\\s+\\w+/\\w+)";
	final String JITTER = "((\\d+\\.*\\d+)\\s+\\w+)";
	final String LOSSES = "((\\d+)\\s*/\\s*(\\d+)\\s+\\(((.*)%)\\))";
	final String OUT_OF_ORDER = "(\\d+)\\s+datagrams received out-of-order";
	
	private String summaryLine;
	
	public IperfFile(String textAgainst){
      this.textAgainst = textAgainst;
	}	
	
	public IperfFile(EnumIperfDataType typeOfData, String fname) throws Exception {
		StringBuffer file = fromFile(fname);
 	    getResult(typeOfData, file);
	}
	
	/**
	 * 
	 * @param typeOfData - which iperf data you want to get best,worst
	 * @throws Exception
	 */
	public void buildIperfData(EnumIperfDataType typeOfData)throws Exception{
		getResult(typeOfData, getTextAgainst());
	}
	
	private void getResult(EnumIperfDataType typeOfData, CharSequence file)
			throws Exception {
		String fourLayerProtocol= "UDP";		
		String index = "\\[\\s*\\d+\\]\\s+";
		String IPERF_LINE_FORMAT;
		
		Pattern fourLayerProtocolPattern = Pattern.compile(fourLayerProtocol);
		Matcher fourLayerProtocolMatcher = fourLayerProtocolPattern.matcher(file);			
		boolean udp = fourLayerProtocolMatcher.find();		
		
		if (udp) {
			IPERF_LINE_FORMAT = index+INTERNAL+"\\s+"+TRANSFER+"\\s+"+THROUGHPUT+"\\s+"+JITTER+"\\s+"+LOSSES;
		} else {
			IPERF_LINE_FORMAT = index+INTERNAL+"\\s+"+TRANSFER+"\\s+"+THROUGHPUT;
		}
		
		Pattern pattern = Pattern.compile(IPERF_LINE_FORMAT);
		Pattern out_of_order_pattern = Pattern.compile(OUT_OF_ORDER);		
		Matcher matcher = pattern.matcher(file);
		Matcher out_of_order_matcher = out_of_order_pattern.matcher(file);
			
		String match = null;	
		boolean summaryFlag = false;
		
		double throughput = IperfCounters.initValue(EnumIperfCounterType.THROUGHPUT, typeOfData);
		double jitter = IperfCounters.initValue(EnumIperfCounterType.JITTER, typeOfData);
		double losses = IperfCounters.initValue(EnumIperfCounterType.LOSSES, typeOfData);
		
		int throughputDivider = 0;
		int jitterDivider = 0;
		int lossesDivider = 0;
		
		// Find all matches
		while ( matcher.find() ) {
				
			match = matcher.group();
			
			/**
			 * THIS "IF" expression make sure that the params calculation ignore the first and the last lines : 
			 * the first line is initializing the connection and cause incorrect information  
			 * and the last line is a summary line of all the data .   
			 * The interval of both of the lines begins in "0.0" .
			 *	
			 * The iperfs last line sets in to the String "summaryLine" .			  
			 */
			if(matcher.group(1).substring(0,3).equals("0.0")){
				if (summaryFlag){
					setSummaryLine(matcher.group());					
				}
				continue;
			}
			/** 
			 * Ensure that only the iperfs last line will be set in t0 the String "summaryLine"    
			 */
			if(Integer.parseInt(matcher.group(1).substring(0,1)) >=1 && !summaryFlag ){
				summaryFlag = true;
			}
			/**
			 * There is a bug in the iperf application: sometimes there is -1.$% instead of losses percentages.
			 * The next IF ignores such lines. 
			 */
			if (udp) {
				if(matcher.group(12).contains("$")){
					continue;
				}
			}
			
			switch (typeOfData) {
				case WORST:
					throughput = IperfCounters.returnWorst(EnumIperfCounterType.THROUGHPUT, throughput, Double.parseDouble(matcher.group(5)));
					if (udp) {						
						jitter = IperfCounters.returnWorst(EnumIperfCounterType.JITTER, jitter, Double.parseDouble(matcher.group(7)));
						losses = IperfCounters.returnWorst(EnumIperfCounterType.LOSSES, losses, Double.parseDouble(matcher.group(12)));
					}
					break;
				case BEST:
					throughput = IperfCounters.returnBest(EnumIperfCounterType.THROUGHPUT, throughput, Double.parseDouble(matcher.group(5)));
					if (udp) {
						jitter = IperfCounters.returnBest(EnumIperfCounterType.JITTER, jitter, Double.parseDouble(matcher.group(7)));				
						losses = IperfCounters.returnBest(EnumIperfCounterType.LOSSES, losses, Double.parseDouble(matcher.group(12)));
					}
					break;
				case AVERAGE:					
					throughput = IperfCounters.returnAverage(throughput, Double.parseDouble(matcher.group(5)));
					throughputDivider++;	
					if (udp) {
						jitter = IperfCounters.returnAverage(jitter, Double.parseDouble(matcher.group(7)));
						jitterDivider++;
						losses = IperfCounters.returnAverage(losses, Double.parseDouble(matcher.group(12)));
						lossesDivider++;
					}
				}
		
			if (throughput == 0) {				
				throw new Exception("Iperf File Is Empty From Data");
			}	
			
		}
		
//		This gives us indication that the Iperf test didn't end.
        if (summaryLine == null)
            throw new Exception("Iperf File Indicates that iperf didn't complete successfuly!");
		
		if (typeOfData == EnumIperfDataType.AVERAGE) {
			setVthroughput(throughput/throughputDivider);
		} else {
			setVthroughput(throughput);
		}		
			
		if (udp) {			
			if (typeOfData == EnumIperfDataType.AVERAGE) {
				setVlosses(losses/lossesDivider);
				setVjitter(jitter/jitterDivider);
			} else {
				setVlosses(losses);
				setVjitter(jitter);
			}
		}
							
		while (out_of_order_matcher.find()) {			
			outOfOrder = Double.parseDouble(out_of_order_matcher.group(1));
		}
		
		iperfRequestedData.put(EnumIperfCounterType.THROUGHPUT.toString(), getVthroughput());
		iperfRequestedData.put(EnumIperfCounterType.JITTER.toString(), getVjitter());
		iperfRequestedData.put(EnumIperfCounterType.LOSSES.toString(),  getVlosses());
	}
	
	
	public String returnValue(EnumIperfCounterType conterType){
		return iperfRequestedData.get(conterType.toString()).toString();
	}
	


	
	private StringBuffer fromFile(String filename) throws Exception {
        
    	BufferedReader in = new BufferedReader(new FileReader(filename));
    	
    	StringBuffer file = new StringBuffer();
    	
    	String str;
    	
    	while ((str = in.readLine()) != null) {
            
    		file.append(str+"\r\n");
    		Thread.sleep(20);
        }
    	
        in.close();
        
        return file;
    }

	public double getVjitter() {
		return Vjitter;
	}
	public void setVjitter(double vjitter) {
		Vjitter = vjitter;
	}	
	private double Vjitter = 0.0;

	public double getVlosses() {
		return Vlosses;
	}
	public void setVlosses(double vlosses) {
		Vlosses = vlosses;
	}
	private double Vlosses = 0.0;

	public double getVthroughput() {
		return Vthroughput;
	}
	public void setVthroughput(double vthroughput) {
		Vthroughput = vthroughput;
	}	
	private double Vthroughput = 0.0;

	public double getOutOfOrder() {
		return outOfOrder;
	}
	public void setOutOfOrder(double outOfOrder) {
		this.outOfOrder = outOfOrder;
	}
	private double outOfOrder = 0.0;

	public String getSummaryLine() {
		return summaryLine;
	}

	private void setSummaryLine(String summaryLine) {
		this.summaryLine = summaryLine;
	}

	public Properties getIperfRequestedData() {
		return iperfRequestedData;
	}

	public void setIperfRequestedData(Properties iperfRequestedData) {
		this.iperfRequestedData = iperfRequestedData;
	}

	public String getTextAgainst() {
		return textAgainst;
	}

	public void setTextAgainst(String textAgainst) {
		this.textAgainst = textAgainst;
	}		
}
