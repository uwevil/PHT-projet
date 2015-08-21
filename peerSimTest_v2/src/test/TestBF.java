package test;

import java.util.Calendar;

import peerSimTest_v3_1_0_0.BF;
import peerSimTest_v3_1_0_0.Config;
import peerSimTest_v3_1_0_0.ErrorException;

public class TestBF {

	public static void main (String[] args) throws ErrorException
	{
		long time = Calendar.getInstance().getTimeInMillis();
				
		BF key = new BF("0100"+"0000"+"1100"+"0101"+"0111");
		BF key2 = new BF(8);	
		key2.addAll("ssc,csq,cqs,fg,rter");
		
		System.out.println(key.toString());
	//	System.out.println(key.getRang(1));
	//	System.out.println(key.getKey(4, 1, 4));
	//	System.out.println(key.getKey(4, 3, 1));

	//	System.out.println();
		
	//	System.out.println(key2.toString());
	//	System.out.println(key2.getRang(1));
	//	System.out.println(key2.getKey(4, 3, 1));
		
	//	System.out.println();
		
	//	System.out.println(new BF(key.getSubFilter(2*4, 3*4 - 1), key2));
		
		BF key3 = new BF("0");
		System.out.println(key3.toString());
		System.out.println(key.equals(key3));
		System.out.println(key.getSubFilter(0, key.getRang(Config.sizeOfElement)));

		System.out.println(Calendar.getInstance().getTimeInMillis() - time + "ms");
	}
}
