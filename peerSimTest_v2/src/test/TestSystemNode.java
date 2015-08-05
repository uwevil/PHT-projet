package test;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import peerSimTest_v2.*;

@SuppressWarnings("unused")
public class TestSystemNode {

	public static void main(String[] args) throws ErrorException
	{
		String s = "/0/0/0/0/0/0/1/22/0/0/0/8";
		
		BFP2P bf1 = (new PathToBF(s, 8)).convert();

		SystemNodeP2P systemIndex = new SystemNodeP2P(0, "/12/1", 0);
		
		systemIndex.add("/0/0/0/0/0/0", 20);
		systemIndex.add("/10", 23);
		
		Object o = systemIndex.add(bf1);
		
	//	System.out.println(systemIndex.getContainerLocal().toString());
		
	//	System.out.println((Message)o);
	//	System.out.println((new BFToPath((BFP2P)(((Message)o).getData()), 8)).convert());
		
		
		Enumeration<String> enumeration = ((Hashtable<String, HashSet<BFP2P>>) o).keys();
		
		while (enumeration.hasMoreElements())
		{
			String tmp = enumeration.nextElement();
			
			System.out.println(tmp);
			
			Iterator<BFP2P> iterator = ((Hashtable<String, HashSet<BFP2P>>)o).get(tmp).iterator();
			
			System.out.println("   " 
					+ (new BFToPath(iterator.next(), 8)).convert());
		}
		
	}

}
