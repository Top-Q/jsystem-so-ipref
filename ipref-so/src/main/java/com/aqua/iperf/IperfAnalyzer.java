package com.aqua.iperf;

import java.io.File;

import jsystem.framework.analyzer.AnalyzerParameterImpl;

import com.aqua.iperf.IperfCounters.EnumIperfCounterType;
/**
 * This Analyzer can analyze iperf result from file result and testAgainst text . 
 * This analyzer can give you result about average , worst and best traffic between the time the iperf was activated.
 *
 */
public class IperfAnalyzer extends AnalyzerParameterImpl {
	
	public static enum EnumIperfDataType {    
		WORST(0),
		BEST(1),
		AVERAGE(2);
		EnumIperfDataType(int value) {
			this.value=value;
		}
		private int value;
		public int value(){return value;}
	}

	private double expected = 0.0;
	private double tolerance = 0.0;

	private EnumIperfCounterType  counterType = EnumIperfCounterType.LOSSES;
	private EnumIperfDataType dataType = EnumIperfDataType.AVERAGE;
	public boolean isFile;
	
	
	
	/**
	 * Analyze iperf result by default analyze the result on the iperf file result.
	 * depends how you run the iperf in your test if you get the result of the iperf to a file use this constructor 
	 * @param dataType - average/worst/best.
	 * @param counterType jitter/lossess/throughput.
	 * @param expected  - The result you expect.
	 * @param tolerance - how much you accept to the real result to be different from the result you expected.
	 */
	public IperfAnalyzer(EnumIperfDataType dataType, EnumIperfCounterType counterType, double expected, double tolerance){
		this.dataType = dataType;
		this.counterType = counterType;
		this.expected = expected;
		this.tolerance = tolerance;		
		this.isFile = true;
	}
	
	/**
	 * Analyze iperf result . 
	 * @param dataType - average/worst/best.
	 * @param counterType jitter/lossess/throughput.
	 * @param expected  - The result you expect.
	 * @param tolerance - how much you accept to the real result to be different from the result you expected.
	 * @param isFile - is the analyzer will analyze on a file the holds the results or text.
	 */
	public IperfAnalyzer(EnumIperfDataType dataType, EnumIperfCounterType counterType, double expected, double tolerance,boolean isFile){
		this.dataType = dataType;
		this.counterType = counterType;
		this.expected = expected;
		this.tolerance = tolerance;		
		this.isFile = isFile;
	}

	@Override
	public void analyze() {
		
		try {
			IperfFile iperfFile;
			
			if(isFile){
				File file = (File)testAgainst;
				iperfFile = new IperfFile(dataType, file.getPath());	
			}else{
				iperfFile = new IperfFile(testAgainst.toString());	
				iperfFile.buildIperfData(dataType);
			}
					
			
			IperfCounters toAnalyze = new IperfCounters(iperfFile.getVlosses(), iperfFile.getVjitter(), iperfFile.getOutOfOrder(), iperfFile.getVthroughput());

			toAnalyze.setReport("Throughput:   " + iperfFile.getVthroughput() + " Mbits/sec\r\n"+ 
								"Jitter:       " + iperfFile.getVjitter()     + " ms\r\n"+
								"Losses:       " + iperfFile.getVlosses()     + "%\r\n"+
								"Out Of Order: " + iperfFile.getOutOfOrder());

			switch ( counterType ) {

				case LOSSES:
					message = "Actual losses:  " + toAnalyze.getLosses() + "%\r\n" +
						      "Expected value: " + expected + " datagrams\r\n" + 
						      "Tolerance:      " + tolerance + "%\r\n\r\n" + toAnalyze.toString();
					title = "Actual Losses = (" + toAnalyze.getLosses() + ")%, Expected = (" + expected + ")% with tolerance = " + tolerance + "%";
					status = toAnalyze.analyze(EnumIperfCounterType.LOSSES, expected, tolerance);					
					break;
	
				case THROUGHPUT:
					message = "Actual throughput: " + toAnalyze.getThroughput() + " Mbits/sec\r\n"+
						      "Expected value:    " + expected + " Mbits/sec\r\n" +
					          "Tolerance:         " + tolerance +"%\r\n\r\n" + toAnalyze.toString();
					title = "Actual Throughput = (" + toAnalyze.getThroughput() + ") Mbits/sec, Expected = (" + expected + ") Mbits/sec with tolerance = " + tolerance + "%";
					status = toAnalyze.analyze(EnumIperfCounterType.THROUGHPUT, expected, tolerance);
					break;
	
				case JITTER:
					message = "Actual jitter:  " + toAnalyze.getJitter() + " ms\r\n" +
						      "Expected value: " + expected + " ms\r\n" +
					          "Tolerance:      " + tolerance + "%\r\n\r\n" + toAnalyze.toString();
					title = "Actual Jitter = (" + toAnalyze.getJitter() + ") ms, Expected = (" + expected + ") ms with tolerance = " + tolerance + "%";
					status = toAnalyze.analyze(EnumIperfCounterType.JITTER, expected, tolerance);
					break;
	
				case OUT_OF_ORDER:
					message = "expected value: " + expected
					+ "\r\nactual out-of-order: " + toAnalyze.getOut_of_order() + "\r\n"+
					"tolerance: " + tolerance+"\r\n\r\n"+toAnalyze.toString();
					if ( toAnalyze.analyze(EnumIperfCounterType.OUT_OF_ORDER, expected, tolerance )){
						status = true;
					}
					else {
						status = false;
						title = "Actual out-of-order measurements differs from expected more then accepted tolerance.";
					}
					break;

			}
			
			if (status) {
				title = dataType.toString() + ": PASS: " + title;
			} else {
				title = dataType.toString() + ": FAIL " + title;
			}
			
		} catch (Exception e) {
			setTitle("Cant anlyze iperf file , file is empty or corrupted");
		}
	}
	
	
}
