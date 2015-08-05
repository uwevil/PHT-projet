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
		String s = new String();
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
	
	public String split(int start, int stop)
	{
		if (path.length() == 1 || path == "/")
			try {
				throw new ErrorException("Convert : path == " + path);
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		try {
			if (start < 0)
				throw new ErrorException("SPLIT : start == 0");
			
			if (start >= stop)
				throw new ErrorException("SPLIT : start >= stop");
			
			
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
		
		String[] tmp = path.split("/");
		String s = new String();
		for (int i = start; i <= (stop + 1 >= tmp.length ? tmp.length - 1 : stop + 1); i++)
		{
			if (tmp[i].length() != 0)
			{
				s += "/" + ((new FragmentP2P(sizeOfFragment))
						.intToFragment(sizeOfFragment, Integer.parseInt(tmp[i])))
						.toInt();
			}
		}
		
		return s;
	}

}
