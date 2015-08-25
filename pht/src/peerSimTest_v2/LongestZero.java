package peerSimTest_v2;

public class LongestZero {

	private BFP2P bf;
	private int sizeOfFragment;
	private int longestLength;
	private String longestPrefix = new String();
	private String remainPrefix = new String();
	
	/**
	 * Rendre le nombre de zéros à partir la première postion du filtre.,
	 * 
	 * */
	public LongestZero(BFP2P bf, int sizeOfFragment)
	{		
		this.bf = bf;
		this.sizeOfFragment = sizeOfFragment;
		
		int long0 = 0;
		String prefix0 = new String();

		for (int i = 0; i < bf.size()/sizeOfFragment; i++)
		{
			if (bf.getFragment(i, sizeOfFragment).toInt() != 0)
				break;
			
			long0++;
			prefix0 += "/" + bf.getFragment(i, sizeOfFragment).toInt();
		}
		
		longestLength = long0;
		
		if (long0 == 0)
		{
			longestPrefix = null;
		}
		else
		{
			longestPrefix = prefix0;
		}
		
		if (long0 == bf.size())
		{
			remainPrefix = null;
		}
		else
		{
			for (int i = long0; i < bf.size()/sizeOfFragment; i++)
			{
				remainPrefix += "/" + bf.getFragment(i, sizeOfFragment).toInt();
			}
		}
	}
	
	/**
	 * Rendre le nombre de zéros.
	 * 
	 * @param stop 
	 * @return int
	 * @author dcs
	 * */
	public int getLongestLength(int stop)
	{
		if (stop >= bf.size()/sizeOfFragment)
			return this.longestLength;
		
		int long0 = 0;
		for (int i = 0; i < stop; i++)
		{
			if (bf.getFragment(i, sizeOfFragment).toInt() != 0)
				break;
			
			long0++;
		}
		
		return long0;
	}
	
	/**
	 * Rendre le nombre de zéros.
	 * 
	 * @return int
	 * @author dcs
	 * */
	public int getLongestLength()
	{
		return longestLength;
	}
	
	/**
	 * Rendre la chaîne de zéros s'il existe.
	 * 
	 * @return {@link String}
	 * @author dcs
	 * */
	public String getLongestPrefix()
	{
		return this.longestPrefix;
	}
	
	/**
	 * Rendre la chaîne restante après la suppression des zéros au début.
	 * 
	 * @return {@link String}
	 * @author dcs
	 * */
	public String getRemainPrefix()
	{
		return this.remainPrefix;
	}
	
	
	/**
	 * Rendre la chaîne de zéros s'il existe.
	 * 
	 * @param stop
	 * @return {@link String}
	 * @author dcs
	 * */
	public String getLongestPrefix(int stop)
	{
		if (stop >= bf.size()/sizeOfFragment)
			return this.longestPrefix;
		
		int long0 = 0;
		String prefix0 = new String();
		
		for (int i = 0; i < stop; i++)
		{
			if (bf.getFragment(i, sizeOfFragment).toInt() != 0)
				break;
			
			long0++;
			prefix0 += "/" + bf.getFragment(i, sizeOfFragment).toInt();
		}
				
		if (long0 == 0)
		{
			prefix0 = null;
		}
		
		return prefix0;
	}
	
	/**
	 * Rendre la chaîne restante après la suppression des zéros au début.
	 * 
	 * @param start
	 * @return {@link String}
	 * @author dcs
	 * */
	public String getRemainPrefix(int start)
	{
		if (start < 0)
			return null;
		
		String remain = new String();
		
		for (int i = start; i < bf.size()/sizeOfFragment; i++)
		{
			remain += "/" + bf.getFragment(i, sizeOfFragment).toInt();
		}
		
		return remain;
	}
}
