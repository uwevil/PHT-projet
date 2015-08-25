package test;

import peerSimTest_v2.BFP2P;
import peerSimTest_v2.Config;
import peerSimTest_v2.ErrorException;
import peerSimTest_v2.Message;
import peerSimTest_v2.SystemNodeP2P;

public class TestSearchExact {

	public static void main(String[] args) throws ErrorException
	{
		String s = "/0/0/0/0/0/0/1/22/0/0/0/8";
		
		BFP2P bf1 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);

		SystemNodeP2P systemIndex = new SystemNodeP2P(0, "/", 1);
		
		systemIndex.add("/0/0/0/0/0/0", 355);
		systemIndex.add("/12/11", 654);
		systemIndex.add("/0", 700);

		systemIndex.add(bf1);
		
		s = "/0/1/22/0/0/0/8";

		bf1 = (new BFP2P()).pathToBF(s, 0, Config.numberOfFragment, Config.sizeOfFragment);
		
		Message o = (Message) systemIndex.searchExact(bf1);
		
		System.out.println(o.toString());
		System.out.println(((BFP2P)o.getData()).toPath(0, 100));
	}

}
