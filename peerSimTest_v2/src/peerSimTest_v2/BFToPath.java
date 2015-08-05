package peerSimTest_v2;

public class BFToPath {

	private BFP2P bf;
	private int sizeOfFragment;
	
	public BFToPath(BFP2P bf, int sizeOfFragment)
	{
		this.bf = bf;
		this.sizeOfFragment = sizeOfFragment;
	}
	
	public String convert()
	{
		String s = new String();
		
		if (bf == null)
			try {
				throw new ErrorException("Convert : BF == null");
			} catch (ErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		int numberOfFragment = bf.size()/sizeOfFragment;
		
		for (int i = 0; i < numberOfFragment; i++)
			s += "/" + (bf.getFragment(i)).toInt();
		
		return s;
	}

	public String split(int start, int stop)
	{
		String s = new String();
		try {
			if (start < 0)
				throw new ErrorException("SPLIT : start == 0");
			
			if (start >= stop)
				throw new ErrorException("SPLIT : start >= stop");
			
			for (int i = start; i <= (stop >= bf.size()/sizeOfFragment ? bf.size()/sizeOfFragment - 1 : stop); i++)
			{
				s += "/" + bf.getFragment(i).toInt();
			}
			return s;
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return s;
	}
}
