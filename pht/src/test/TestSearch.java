package test;

import java.util.Enumeration;
import java.util.Hashtable;

import peerSimTest_v2.BFP2P;
import peerSimTest_v2.Config;
import peerSimTest_v2.ErrorException;
import peerSimTest_v2.SystemNodeP2P;

public class TestSearch {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ErrorException
	{
		String s = "/0/0/0/0/0/0/1/22/0/0/0/8";
		
		BFP2P bf1 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);

		SystemNodeP2P systemIndex = new SystemNodeP2P(0, "/12", 1);
		
		systemIndex.add("/12/0/0/0/0/0/0", 355);
		systemIndex.add("/12/0", 654);
		systemIndex.add("/12/1", 700);
		systemIndex.add(bf1);
		
		s = "/0/1/22/0/0/0/8";
		
		bf1 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);
		
		Object[] o = (Object[]) systemIndex.search(bf1);
		
		System.out.println(((Object[])o)[0].toString());
		
		Hashtable<Integer, Hashtable<String, BFP2P>> o1 = (Hashtable<Integer, Hashtable<String, BFP2P>>) ((Object[])o)[1];
		
		Enumeration<Integer> enumeration = o1.keys();
		
		while (enumeration.hasMoreElements())
		{
			Integer o_tmp = enumeration.nextElement();
			System.out.println(o_tmp);
			
			Enumeration<String> o11 = ((Hashtable<String, BFP2P>) o1.get(o_tmp)).keys();
			while(o11.hasMoreElements())
			{
				String s_tmp = o11.nextElement();
				System.out.println(s_tmp + " : " + ((Hashtable<String, BFP2P>) o1.get(o_tmp)).get(s_tmp).toPath(0, 100));
			}
		}
	}

}
