package com.aqua.iperf;

import com.aqua.iperf.IperfAnalyzer.EnumIperfDataType;

public class IperfCounters {
	
	public static enum EnumIperfCounterType {    
		LOSSES(0),
		JITTER(1),
		THROUGHPUT(3),
		OUT_OF_ORDER(4);
		EnumIperfCounterType(int value) {
			this.value=value;
		}
		private int value;
		public int value(){return value;}
	}
	
	private double 	losses = 0;
	private double 	jitter = 0.0;
	private double 	out_of_order = 0;
	private double 	throughput = 0.0;	
	
	private String report;
		
	public IperfCounters(double losses, double jitter, double out_of_order, double throughput) {
		this.losses = losses;
		this.jitter = jitter;
		this.out_of_order = out_of_order;
		this.throughput = throughput;		
	}
	
	/**
	 * This function checks if one of the iperf counter types is ok(losses,jitter,throughput,out_of_order)
	 * @param actual - the actual value you get in one of the counter types
	 * @param expected - the value you expect to get to one of the counter types(losses,jitter,throughput,out_of_order)
	 * @param tolerance - how much you can ignore from the expected result in order to pass the actual result.
	 * @return true/false
	 */
	private boolean inRange(double actual, double expected, double tolerance) {
		return (actual >= expected * (100 - tolerance)/100);
	}
		
	public boolean analyze(EnumIperfCounterType counter, double expected, double tolerance) {
		
		switch (counter) {
			case LOSSES:
				return inRange(expected, losses, tolerance);
			case THROUGHPUT:
				return inRange(throughput, expected, tolerance);
			case JITTER:
				return inRange(expected, jitter, tolerance);
		}
		
		return false;

	}
	
	@Override
	public String toString(){		
		return report;		
	}
	
	public void setReport( String report ){
		this.report = report;
	}

	public double getJitter() {
		return jitter;
	}

	public void setJitter(double jitter) {
		this.jitter = jitter;
	}

	public double getLosses() {
		return losses;
	}

	public void setLosses(double losses) {
		this.losses = losses;
	}

	public double getOut_of_order() {
		return out_of_order;
	}

	public void setOut_of_order(long out_of_order) {
		this.out_of_order = out_of_order;
	}

	public double getThroughput() {
		return throughput;
	}

	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}

	public static double initValue(EnumIperfCounterType counterType, EnumIperfDataType dataType) {
		switch (dataType) {
			case AVERAGE:
				return 0.0;
			case WORST:
				switch (counterType) {
					case JITTER:
					case LOSSES:
						return 0.0;
					case THROUGHPUT:
						return Double.MAX_VALUE;
				}
				break;
			case BEST:
				switch (counterType) {
				case JITTER:
				case LOSSES:
					return Double.MAX_VALUE;
				case THROUGHPUT:
					return 0.0;
			}
			break;				
		}
		return 0.0;

	}
	
	/** Gets two IperfCponters from the same type and return the worst one  */
	public static double returnWorst(EnumIperfCounterType counterType, double value, double toBeCompare) throws Exception{
		switch (counterType) {		
			case THROUGHPUT:
				return Math.min(value, toBeCompare);				
			case JITTER:
			case LOSSES:
				return Math.max(value, toBeCompare);
			}
		return value;	
	}

	/** Gets two IperfCponters from the same type and return the best one  */
	public static double returnBest(EnumIperfCounterType counterType, double value, double toBeCompare) throws Exception{		
		switch (counterType) {		
			case THROUGHPUT:
				return Math.max(value, toBeCompare);				
			case JITTER:
			case LOSSES:
				return Math.min(value, toBeCompare);
		}
		return value;	
	}
	
	public static double returnAverage(double currentAverage, double addToAverage) throws Exception {
		
		return currentAverage + addToAverage;

	}
	
}