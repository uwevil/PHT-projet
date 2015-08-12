package test;

import java.util.Calendar;

import peerSimTest_v3.BF;
import peerSimTest_v3.ErrorException;

public class TestBF {

	public static void main (String[] args) throws ErrorException
	{
		long time = Calendar.getInstance().getTimeInMillis();
				
		BF key = new BF("0100"+"0000"+"1100"+"0101"+"0111");
		BF key2 = new BF(20);	
		key2.addAll("ssc,csq,cqs,fg,rter");
		
		System.out.println(key.toString());
		System.out.println(key.getRang(1));
		System.out.println(key.getKey(4, 1, 4));
		System.out.println(key.getKey(4, 3, 1));

		System.out.println();
		
		System.out.println(key2.toString());
		System.out.println(key2.getRang(1));
		System.out.println(key2.getKey(4, 3, 1));
		
		System.out.println(Calendar.getInstance().getTimeInMillis() - time + "ms");
	}
}
