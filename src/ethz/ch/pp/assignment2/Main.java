package ethz.ch.pp.assignment2;

import java.util.Arrays;
import java.util.Random;


public class Main {

	public static void main(String[] args) {
 		
		// TODO: adjust appropriately for the required experiments
		taskA();
		
		int[] input1 = generateRandomInput(1000);
		int[] input2 = generateRandomInput(10000);
		int[] input3 = generateRandomInput(100000);
		int[] input4 = generateRandomInput(1000000);
		
		// Sequential version
		// taskB(input1);
		// taskB(input2);
		// taskB(input3);
		// taskB(input4);
		
		// Parallel version
		// taskE(input1, 4);
		// taskE(input2, 4);
		// taskE(input3, 4);
		taskE(input4, 4);

		ArraySplit[] arr = PartitionData(100, 54);
		int total = 0;
		for(ArraySplit p : arr){
			total += p.length;
		}
		
		long threadOverhead = taskC();
		System.out.format("Thread overhead on current system is: %d nano-seconds\n", threadOverhead);		
	}
	
	private final static Random rnd = new Random(42);

	public static int[] generateRandomInput() {
		return generateRandomInput(rnd.nextInt(10000) + 1);
	}
	
	public static int[] generateRandomInput(int length) {	
		int[] values = new int[length];		
		for (int i = 0; i < values.length; i++) {
			values[i] = rnd.nextInt(99999) + 1;				
		}		
		return values;
	}
	
	public static int[] computePrimeFactors(int[] values) {		
		int[] factors = new int[values.length];	
		for (int i = 0; i < values.length; i++) {
			factors[i] = numPrimeFactors(values[i]);
		}		
		return factors;
	}
	
	public static int numPrimeFactors(int number) {
		int primeFactors = 0;
		int n = number;		
		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				primeFactors++;
				n /= i;
			}
		}
		return primeFactors;
	}
	
	public static class ArraySplit {
		public final int startIndex;
		public final int length;
		
		ArraySplit(int startIndex, int length) {
			this.startIndex = startIndex;
			this.length = length;
		}
	}

	// TaskD
	public static ArraySplit[] PartitionData(int length, int numPartitions) {
		//TODO: implement
		ArraySplit[] arr = new ArraySplit[numPartitions];
		int size = length/numPartitions;
		int current = 0;
		for(int i = 0; i < numPartitions; i++){
			ArraySplit split;
			split = new ArraySplit(current, size);
			arr[i] = split;
			current += size;
			length -= size;
		}
		int total = 0;
		for(ArraySplit p : arr){
			total += p.length;
		}
		if(length != 0){
			for(int i = 0; i < numPartitions; i++){
				if(length != 0){
					int oLength = arr[i].length;
					if(i - 1 >= 0)
						arr[i] = new ArraySplit(arr[i - 1].startIndex + arr[i - 1].length, oLength + 1);
					else{
						arr[i] = new ArraySplit(0, oLength + 1);
					}
					length -= 1;
				}
				else{
					int oLength = arr[i].length;
					if(i - 1 >= 0)
						arr[i] = new ArraySplit(arr[i - 1].startIndex + arr[i - 1].length, oLength);
					else{
						arr[i] = new ArraySplit(0, oLength);
					}
				}
			}
		}
		// System.out.println(Arrays.toString(arr));
		return arr;
		// throw new UnsupportedOperationException();
	}
	public static void taskA() {
		//TODO: implement
		Thread t = new Thread(){
			public void run(){
				System.out.println(Thread.currentThread().getName());
				// computePrimeFactors(values)
			}
		};
		t.start();
		try{
			t.join();
		} catch(InterruptedException e){
			System.out.println("ERROR");
		}
	}
	static class myThread implements Runnable{
		public int[] factors;
		public int[] values;

		myThread(int[] values){
			this.values = values;
		}
		public void run(){
			System.out.println(Thread.currentThread().getName());
			this.factors = computePrimeFactors(values);
		}
	}

	public static int[] taskB(final int[] values) {
		//TODO: implement
		int[] result;
		myThread mT = new myThread(values);
		Thread t = new Thread(mT);
		t.start();
		long time = System.nanoTime();
		System.out.println(Thread.currentThread().getName());
		try{
			t.join();
			time = System.nanoTime() - time;
			System.out.println("THE TIME IIIIIIIIIISSSS: " + time);
		} catch(Exception e){
			System.out.println("Caught exception");
		}
		result = mT.factors;
		// System.out.println(Arrays.toString(mT.factors));
		t = null;
		System.gc();
		return result;
		// throw new UnsupportedOperationException();
	}
	
	// Returns overhead of creating thread in nano-seconds
	public static long taskC() {		
		//TODO: implement
		Thread t = new Thread(){
			public void run(){}
		};
		long time = System.nanoTime();
		t.start();
		try{
			t.join();
			time = System.nanoTime() - time;
			return time;
		} catch(Exception e){}
		throw new UnsupportedOperationException();
	}
	
	public static int[] taskE(final int[] values, final int numThreads) {
		//TODO: implement
		long t1 = System.nanoTime();
		taskB(values);
		t1 = System.nanoTime() - t1;
		ArraySplit[] splits = PartitionData(values.length, numThreads);
		myThread[] myThreads = new myThread[numThreads];
		int i = 0;
		for(ArraySplit p : splits){
			int[] newValues = Arrays.copyOfRange(values, p.startIndex, p.startIndex + p.length);
			myThreads[i] = new myThread(newValues);
			i++;
		}
		Thread[] threads = new Thread[myThreads.length];
		i = 0;
		long t2 = System.nanoTime();
		for(myThread m : myThreads){
			Thread t = new Thread(m);
			threads[i] = t;
			t.start();
			i++;
		}

		try{
			for(Thread t : threads){
				t.join();
			}
		} catch(Exception e){}
		t2 = System.nanoTime() - t2;
		int[] result = new int[values.length];
		i = 0;
		for(myThread t : myThreads){
			for(int j = 0; j < t.factors.length; j++){
				result[i] = t.factors[j];
				i++;
			}
		}
		System.out.println("One thread: " + t1 + " | Multiple Threads: " + t2);
		System.out.println((float) t1/t2 + "x times faster with multithreading");
		return result;
		// throw new UnsupportedOperationException();
	}
}