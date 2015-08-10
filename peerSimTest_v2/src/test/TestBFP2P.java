package test;

import java.util.ArrayList;
import java.util.HashSet;

import peerSimTest_v2_1.BFP2P;
import peerSimTest_v2_1.Config;
import peerSimTest_v2_1.ErrorException;

public class TestBFP2P {

	@SuppressWarnings("unused")
	public static void main (String[] args) throws ErrorException
	{
		String path = "/0/0/0/0/1/1/1/1/1/1/0/0/0/0/0/0/0/0/0/0/2/33/4/2/55/8/8/8/8/8/9/9";
		String path2 = "/1/2/3/4/3/2/5/4/6/7/7/7/64/3/2";
		
		BFP2P bf = (new BFP2P()).pathToBF(path, 0, 100, Config.sizeOfFragment);
		BFP2P compressed = bf.compressed(Config.sizeOfFragment);
		
		System.out.println(bf.toPath(0, 100));
		System.out.println(compressed.toPath(0, 100));
		
		ArrayList<BFP2P> arrayList = new ArrayList<BFP2P>();
		HashSet<BFP2P> hashSet = new HashSet<BFP2P>();
		
		arrayList.add(bf);
		hashSet.add(bf);
		
		BFP2P bf2 = (new BFP2P(bf.toString()));
		
		if (arrayList.contains(bf2))
			System.out.println("cinsds");
		
		if (hashSet.contains(bf2))
			System.out.println("ssssssssss");
	}
}
