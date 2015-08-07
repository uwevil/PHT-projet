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
		
		BFP2P bf1 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);

		SystemNodeP2P systemIndex = new SystemNodeP2P(0, "/12", 1);
		
		systemIndex.add("/12/0/0/0/0/0/0", 20);
		systemIndex.add("/12", 23);
		
		Object o = systemIndex.add(bf1);
		
		System.out.println(systemIndex.getLocalContainer().toString());
		
		System.out.println((Message)o);
		System.out.println(((BFP2P)(((Message)o).getData())).toPath(0, Config.numberOfFragment));
		
		
	/*	Enumeration<String> enumeration = ((Hashtable<String, HashSet<BFP2P>>) o).keys();
		
		while (enumeration.hasMoreElements())
		{
			String tmp = enumeration.nextElement();
			
			System.out.println(tmp);
			
			Iterator<BFP2P> iterator = ((Hashtable<String, HashSet<BFP2P>>)o).get(tmp).iterator();
			
			System.out.println("   " 
					+ ((BFP2P)iterator.next()).toPath(0, Config.numberOfFragment));
		}
	*/	
		
		HashSet<BFP2P> localContainer = new HashSet<BFP2P>();
		localContainer.add(bf1);
		
		BFP2P bf2 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);
		
		System.out.println("Contains " + localContainer.contains(bf2));
		System.out.println((localContainer.iterator().next()).equals(bf2));
		System.out.println(bf2.equals(bf1));
		
		int i = 1;
		Object ii = i;
		if (((Object)i).getClass().getName().equals("java.lang.Integer"))
			System.out.println("essssssssssss");
	}

}
