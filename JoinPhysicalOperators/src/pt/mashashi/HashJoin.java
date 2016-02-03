package pt.mashashi;

import java.util.List;

import java.util.ArrayList;

/**
 * http://stackoverflow.com/questions/10901752/what-is-the-significance-of-load-factor-in-hashmap
 * http://trove4j.sourceforge.net/javadocs/
 * https://en.wikipedia.org/wiki/Linear_congruential_generator
 * http://rosettacode.org/wiki/Linear_congruential_generator
 * 
 * Change the a,b on lcd
 * + Impact (low-or-none)
 * 
 * To improve performance supply statistics to initialize the result structure with the approximate number of elements
 * + Impact (significant-low)
 * 
 * Change the implementation of the result list type
 * + Impact (high) - Works better with ArrayList
 * 
 * Give the result structure and the hashmap initial size
 * + Impact (significant) - The price of computing a heuristic was to high initlized at 1000 and hope for the best
 * 
 * 
 * Trove vs java hashmap vs costum
 * + java hashmap: approx. 797 ms
 * + trove: approx. 560 ms
 * + costum: approx. 441 ms
 * 
 * Exchange the PRG to other algorithm
 * + Impact: (expected low)
 * 
 * Do a implementation of the hashmap
 * + Impact: (significant) 
 * 
 * Don't use key values -1 this is used to detect a empty bucket to change this value act upon the constant NOP.
 * 
 * @author Rafael
 *
 */
public class HashJoin implements Join{
	
	public static class MyHashIntInt {
		
		private int m[];
		private int capacityM;
		private int nop;
		
		public MyHashIntInt(int capacity, int nop) {
			this.capacityM = capacity;
			this.nop = nop;
			m = new int[capacity];
			initHash(m);
		}
		
		public void initHash(int[] a){
			for(int i=0;i<a.length;i++){
				a[i] = nop;
			}
		}
		
		public boolean put(int ki, int v){
			int h = hash(ki);
			if(m[h]!=nop){
				return false;
			}
			m[h] = v;
			return true;
		}
		
		public int get(int k){
			return m[hash(k)];
		}
		private int hash(int ki){
			return Math.floorMod(ki, capacityM);
		}
	}
	
	/**
	 * 
	 * @author Rafael
	 * 
	 * Idea: Count the number of times that next was called after each seed have a method to return that value
	 * 
	 */
	public static class LCG{
		
	    // For x(i+1) = (a * x(i) + b) % m.
		// Values for a, b and m taken from wikipedia that in turn were taken on RtlUniform from Native API[9]
		// These values guarantee full-cycle of the generator
	    
		//RtlUniform from Native API[9]
		final static int a = 2147483629;
	    final static int b = 2147483587;
	    final static int m = 2147483647; //2^31 - 1
	    
		
		/*//C++11's minstd_rand
		final static int a = 48271;
	    final static int b = 12345;
	    final static int m = 2147483647; //2^30
	    */
	    
		/*
		//Apple CarbonLib, C++11's minstd_rand0[10]
		final static int a = 16807;
	    final static int b = 12345;
	    final static int m = 2147483647; //2^30
	    */
	    
	    public LCG(){}
	    
	    public int x;
	    
	    public int getState(){
	    	return x;
	    }
	    
	    public void setState(int state){
	    	x = state;
	    }
	    
	    public int next() {
	        // Calculate next value in pseudo random sequence
	    	return x = (a * (x++) + b) % m;
	    }
	    
	}
	
	@Override
	public String getName() {
		return "Hash Join";
	}
	
	
	public final static int NOP = -1;
	
	/**
	 * Assumes that is indiferent which of the lists is used to build the hashmap
	 * 
	 */
	@Override
	public List<Triple> join(List<Tuple> input1, List<Tuple> input2) {
		
		// Configs
		final float TRY_FACTOR = 0.01f;
		final float MEMORY_OVERHEAD = 2f;
		//
		
		final int TRIES_PRG = ((int) (input1.size()*TRY_FACTOR))<10?10:((int) (input1.size()*TRY_FACTOR));
		
		List<Triple> result = new ArrayList<Triple>(1000);
		
		
		MyHashIntInt map = new MyHashIntInt((int) (input1.size()*(1+MEMORY_OVERHEAD)), NOP);
		
		LCG lcg = new LCG();
		
		{ // description - init hash table
			int tries;
			for(int idx=0;idx<input1.size();idx++){ //Better to have a for than a iterator
				{
					lcg.x=input1.get(idx).getID();
					tries = 0;
					while(map.get(lcg.x)!=NOP){ 
						
						lcg.next();
						
						if(++tries==TRIES_PRG){
							throw new RuntimeException("The PRG has unable to find free bucket. Try increasing the initial number of buckets on the hashmap (MEMORY_OVERHEAD) or increasing number of tries (TRIES_PRG). If the list to join has repeated keys count > TRIES_PRG this is the problem.");
						}
						
					}
				}
				map.put(lcg.x, idx);
			}
		}
		
		ArrayList<Tuple> processed = new ArrayList<Tuple>();
		// Sometimes the PRG falls in a bucket that has already been processed giving origin to non valid triples
		// This array list serves as a mean of avoiding this error
		
		{ // perform join
			int input1pointer; 	// This variable has a positive impact
			Tuple e2; 			// This variable has a positive impact
			Tuple e1; 			// This variable has a positive impact
			for (int index2 = 0; index2 < input2.size(); index2++){
				e2 = input2.get(index2);
				lcg.setState(e2.getID());
				while((input1pointer = map.get(lcg.x)) != NOP){
					e1 = input1.get(input1pointer);
					if(e1.getID()==e2.getID() && !processed.contains(e1)){
						processed.add(e1);
						result.add(new Triple(e2.getID(), e1.getValue(), e2.getValue()));
					}
					lcg.next();
				}
				processed.clear();
			}	
		}
		
		return result;
	}
}
