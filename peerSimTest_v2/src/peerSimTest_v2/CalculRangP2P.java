package peerSimTest_v2;

public class CalculRangP2P {
	
	public CalculRangP2P() 
	{
		// TODO Auto-generated constructor stub
	}
	
	public int getRang(String path)
	{
		if (path == "/")
			return 0;
		
		String[] tmp = path.split("/");
		return tmp.length - 1;
	}

}
