package pt.mashashi;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Rafael
 * 
 * Based on the algorithm proposed hear
 * http://www.dcs.ed.ac.uk/home/tz/phd/thesis/node20.htm
 * 
 * Heap sort adapted from
 * http://www.code2learn.com/2011/09/heapsort-array-based-implementation-in.html
 * 
 * Using radix sort instead of heap sort would have a considerable increase in the performance
 *
 * There are many global variables and several methods to act upon these variables to avoid passing parameters to methods.
 * 
 * It is faster if we convert the ArrayList to an array.
 * 
 */
public class SortMergeJoin implements Join{
	
	private int p1; // pointer 1
	private int p2; // pointer 2
	private Tuple t1; // tuple 1
	private Tuple t2; // tuple 2
	private Tuple[] r1; // relation 1
	private int r1_length;
	private int r1_last_index;
	private Tuple[] r2; // relation 2
	private int r2_length;
	private int r2_last_index;
	private boolean neor1; // not end of relation 1
	private boolean neor2; // not end of relation 2
	
	private int pl; // pointer local
	private Tuple tl; // tuple local
	
	public String getName() {
		return "Sort Merge Join";
	}
	
	@Override
	public List<Triple> join(List<Tuple> input1, List<Tuple> input2) {
		
		
		List<Triple> result = new ArrayList<Triple>(1000);
		
		r1 =   toArrayList(input1);
		r1_length = r1.length;
		r1_last_index = r1_length-1;
		heap(r1); 
		
		r2 =  toArrayList(input2);
		r2_length = r2.length;
		r2_last_index = r2_length-1;
		heap(r2);
	
		p1 = 0;
		if(neor1=(p1<r1_length))
			t1 = r1[p1];
		
		p2 = 0;
		if(neor2=(p2<r2_length))
			t2 = r2[p2];
		
		end1: while(neor1 && neor2){
			
			if(neor2 && (t2 = r2[p2])!=null && t1.getID()>t2.getID()){
				if(p2<r2_last_index)
					t2 = r2[++p2];
				else
					neor2 = false;
			}else if(neor1 && t1.getID()<t2.getID()){
				if(p1<r1_last_index)
					t1 = r1[++p1];
				else
					neor1 = false;
			}else{
				
				result.add(new Triple(t1.getID(), t1.getValue(), t2.getValue()));
				
				end2: if(neor2) {
					if(p2 < r2_last_index)
						tl = r2[pl = p2 = p2+1];
					else{
						neor2 = false;
						break end2;
					}
					while(t1.getID() == tl.getID()){
						result.add(new Triple(t1.getID(), t1.getValue(), tl.getValue()));
						if(++pl<r2_length)
							tl = r2[pl];
						else
							break;
					}
				}
			
				
				
				if(neor1) {
					if(p1<r1_last_index)
						tl = t1 = r1[pl = p1 = p1+1];
					else{
						neor1 = false;
						continue end1;
					}
					while(tl.getID() == t2.getID()){
						result.add(new Triple(tl.getID(), tl.getValue(), t2.getValue()));
						if(++pl<r1_length)
							tl = r1[pl];
						else
							break;
					}
				}
			
			}
			
		}
		
		return result;
		
	}
	
	
	

	
	
	

	public static Tuple[] toArrayList(List<Tuple> l){
		Tuple[] a = new Tuple[l.size()];
		for(int i=0;i<a.length;i++){
			a[i] = l.get(i);
		}
		return a;
	}
	
	
	
	
	static void heapify(Tuple a[], int n, int i) {
		int max, child;
		child = 2 * i + 1;
		max = i;
		if (child < n)
			if (a[child].getID() > a[max].getID())
				max = child;
		if (child + 1 < n)
			if (a[child + 1].getID() > a[max].getID())
				max = child + 1;
		if (max != i) {
			Tuple temp = a[i];
			a[i] = a[max];
			a[max] = temp;
			heapify(a, n, max);
		}
	}

	static void buildheap(Tuple a[]) {
		for (int i = a.length / 2 - 1; i >= 0; i--)
			heapify(a, a.length, i);
	}

	static void heap(Tuple a[]) {
		buildheap(a);
		for (int i = a.length - 1; i >= 1; i--) {
			Tuple temp = a[0];
			a[0] = a[i];
			a[i] = temp;
			heapify(a, i, 0);
		}
	}



}
