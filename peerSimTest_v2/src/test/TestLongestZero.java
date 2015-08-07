package test;

import peerSimTest_v2.BFP2P;
import peerSimTest_v2.Config;
import peerSimTest_v2.LongestZero;

public class TestLongestZero {

	public static void main (String[] args)
	{
		String s = "/0/0/0/0/0/0/1/22/0/0/0/8";
		
		BFP2P bf1 = (new BFP2P()).pathToBF(s, 0, 100, Config.sizeOfFragment);

		String bfToPath = bf1.toPath(0, 100);
		int longestLength = (new LongestZero(bf1, 8)).getLongestLength();
		LongestZero longestZero = new LongestZero(bf1, 8);
		String longestPrefix = longestZero.getLongestPrefix(longestLength);
		String remainingPrefix = longestZero.getRemainPrefix(longestLength);
		
		System.out.println(bf1);
		System.out.println(bfToPath);
		System.out.println(longestLength);
		System.out.println(longestPrefix);
		System.out.println(remainingPrefix);

		System.out.println("-------------");
		
		System.out.println((new LongestZero((new BFP2P())
				.pathToBF(remainingPrefix, 0, 100, Config.sizeOfFragment), 8)).getLongestLength(4));
		System.out.println((new LongestZero((new BFP2P())
				.pathToBF(remainingPrefix, 0, 100, Config.sizeOfFragment), 8)).getLongestPrefix(4));
		System.out.println((new BFP2P()).pathToBF(remainingPrefix, 1, 100, Config.sizeOfFragment).toPath(0, 100));
		
		System.out.println("-----------------");
		 
		
	}

}
