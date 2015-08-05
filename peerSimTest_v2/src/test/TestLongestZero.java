package test;

import peerSimTest_v2.BFP2P;
import peerSimTest_v2.BFToPath;
import peerSimTest_v2.LongestZero;
import peerSimTest_v2.PathToBF;

public class TestLongestZero {

	public static void main (String[] args)
	{
		String s = "/0/0/0/0/0/0/1/22/0/0/0/8";
		
		BFP2P bf1 = (new PathToBF(s, 8)).convert();
		String bfToPath = (new BFToPath(bf1, 8)).convert();
		int longestLength = (new LongestZero(bf1, 8)).getLongestLength();
		LongestZero longestZero = new LongestZero(bf1, 8);
		String longestPrefix = longestZero.getLongestPrefix();
		String remainingPrefix = longestZero.getRemainPrefix();
		
		System.out.println(bf1);
		System.out.println(bfToPath);
		System.out.println(longestLength);
		System.out.println(longestPrefix);
		System.out.println(remainingPrefix);

		System.out.println("-------------");
		
		System.out.println((new LongestZero((new PathToBF(remainingPrefix, 8)).convert(), 8)).getLongestLength(4));
		System.out.println((new LongestZero((new PathToBF(remainingPrefix, 8)).convert(), 8)).getLongestPrefix(4));
		System.out.println((new BFToPath((new PathToBF(remainingPrefix, 8)).convert(), 8)).split(1, 100));
		
		System.out.println("-----------------");
		
	}

}
