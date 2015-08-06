package test;

import peerSimTest_v2.BFP2P;
import peerSimTest_v2.Config;
import peerSimTest_v2.FragmentP2P;

public class TestConvert {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FragmentP2P f = (new FragmentP2P(Config.sizeOfFragment)).pathToFragment("/11");
		
		System.out.println(f.toInt() + " " + f.toString() + " " + f.toPath());
		BFP2P bf = (new BFP2P()).pathToBF("/01/122", 0, 1, Config.sizeOfFragment);
		System.out.println(bf + " " + bf.toPath(0, 4));
	}

}
