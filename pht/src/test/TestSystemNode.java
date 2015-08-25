package test;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import peerSimTest_v3.*;

@SuppressWarnings("unused")
public class TestSystemNode {

	public static void main(String[] args) throws ErrorException
	{
		
		SystemIndex systemIndex = new SystemIndex("dcs", 23);
		BF bf1 = new BF(Config.sizeOfBF);
		BF bf2 = new BF(Config.sizeOfBF);
		BF bf3 = new BF(Config.sizeOfBF);
		BF bf4 = new BF(Config.sizeOfBF);

		bf1.addAll("sss,dsq,dsqdz,azdaz");
		bf2.addAll("sss,dsq,dsqdsq,dsfez");
		bf3.addAll("sss,dsq,fds,grtyrt");
		bf4.addAll("ssssssssssss");
		
		BF key1 = bf1.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);
		BF key2 = bf2.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);
		BF key3 = bf3.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);
		BF key4 = bf4.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas);

		systemIndex.add(bf1.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		systemIndex.add(bf2.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		systemIndex.add(bf3.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		systemIndex.add(key4);

		System.out.println(" " + bf1.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		System.out.println(" " + bf2.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		System.out.println(" " + bf3.getKey(Config.sizeOfFragment, Config.numberOfBits, Config.pas));
		System.out.println(" " + key4);
		
		/*
		Iterator<SystemNode> iterator = systemIndex.getListNode().iterator();
		
		while(iterator.hasNext())
		{
			SystemNode systemNode = iterator.next();
			
			ArrayList<BF> arrayList = systemNode.getListKey();
			
			if (arrayList != null && arrayList.size() != 0)
				System.out.println(systemNode.getPath() + "\n" + arrayList.toString());
		}
		*/
		/*
		System.out.println("+" + key2);
		Object o = systemIndex.search(key2);
		
		@SuppressWarnings("unchecked")
		ArrayList<Object> o_tmp = (ArrayList<Object>) o;
		
		for (int i = 0; i< o_tmp.size(); i++)
			System.out.println("-"+o_tmp.get(i));
			*/
		
		System.out.println("+" + key4);
		Object o = systemIndex.searchExact(key4);
		System.out.println("-" + o);
	}

}
