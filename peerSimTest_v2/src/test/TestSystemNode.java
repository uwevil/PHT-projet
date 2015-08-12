package test;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import peerSimTest_v3.*;

@SuppressWarnings("unused")
public class TestSystemNode {

	public static void main(String[] args) throws ErrorException
	{
		
		SystemNode node = new SystemNode(new BF("01100"), 1);
		
		BF bf1 = new BF("011001101");
		BF bf2 = new BF("011001010");
		BF bf3 = new BF("011000101");
		node.add(bf1);
		
		Object o = node.add(bf2);
		o = node.add(bf3);
		
		System.out.println(node.getPath().getRang(1));
		System.out.println(bf1.getFragment(5, 1));
		System.out.println(o);
	}

}
