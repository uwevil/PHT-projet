package peerSimTest_v2;

public class PathToBF {

	private String path;
	private int sizeOfFragment;
	
	public PathToBF(String path, int sizeOfFragment)
	{
		this.path = path;
		this.sizeOfFragment = sizeOfFragment;
	}
	
	public BFP2P convert()
	{
		BFP2P bf = null;
		if (path.length() == 1 || path == "/")
			try {
				throw new ErrorException("Convert : path == " + path);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		String[] tmp = path.split("/");
		String s = "";
		for (int i = 0; i < tmp.length; i++)
		{
			if (tmp[i].length() != 0)
			{
				s += ((new FragmentP2P(sizeOfFragment))
						.intToFragment(sizeOfFragment, Integer.parseInt(tmp[i])))
						.toString();
			}
		}
		
		try {
			bf = new BFP2P(s, sizeOfFragment);
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bf;
	}

}
