package peerSimTest_v2;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.BitSet;

public class BFP2P implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BitSet bitset;
	private int bitSetSize;
	
	/** 
	 * Créer un filtre vide avec la taille bitSetSize et la taille d'un fragment btsPerElement
	 * */
	
	public BFP2P(int bitSetSize)
	{
		this.bitset = new BitSet(bitSetSize);
		this.bitSetSize = bitSetSize;
	}
	
	public BFP2P()
	{}
	
	/** 
	 * Créer un filtre à partir d'une chaîne de caractère : 0 et 1 et avec la taille d'un fragment bitsPerElement
	 * lever Exception quand la chaîne contient les autres caractères.
	 * */
	
	public BFP2P(String chaineBits) throws ErrorException
	{
		char[] chararray = chaineBits.toCharArray();
			
		this.bitset = new BitSet(chararray.length);
		this.bitSetSize = chararray.length;
		for (int i = 0; i< this.bitSetSize; i++)
		{
			if (chararray[i] != '0' && chararray[i] != '1')
			{
				throw new ErrorException("chaineBits contient des caractères spéciales");
			}
			if (chararray[i] == '1')
				this.bitset.set(i, true);
		}
	}
	
	/** 
	 * Test l'égalité entre 2 filtres
	 * */
	
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BFP2P other = (BFP2P) o;

		if (this.bitSetSize != other.size())
			return false;
		if (this.bitset != other.bitset && (this.bitset == null || !this.bitset.equals(other.bitset)))
			return false;
		return true;
	}
	
	/** 
	 * Ajout une chaîne de description sous forme: mot1,mot2,mot3... dans le filtre
	 * */
	
	public void addAll(String description)
	{		
		String[] s = description.split(",");
		
		MessageDigest md256, md512, md;
		try
		{
			md = MessageDigest.getInstance("SHA-1");
			md256 = MessageDigest.getInstance("SHA-256");
			md512 = MessageDigest.getInstance("SHA-512");

			for (int i = 0; i < s.length; i++)
			{
				if (!s[i].equals(""))
				{
					byte[] tmp = md.digest(s[i].getBytes("UTF-8"));
					double n = 0;

					for (int j = 0; j < tmp.length; j++)
					{
						n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
					}
					bitset.set((int) (n % this.bitSetSize), true);
					
					tmp = md256.digest(s[i].getBytes("UTF-8"));
					n = 0;
					for (int j = 0; j < tmp.length; j++)
					{
						n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
					}
					bitset.set((int) (n % this.bitSetSize), true);
					
					tmp = md512.digest(s[i].getBytes("UTF-8"));
					n = 0;
					for (int j = 0; j < tmp.length; j++)
					{
						n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
					}
					bitset.set((int) (n % this.bitSetSize), true);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/* 
	 * Ajout un mot dans le filtre
	 * */
	/*
	public void add(String a)
	{
		if (a.equals(""))
			return;
		MessageDigest md256, md512, md;
		try
		{
			md = MessageDigest.getInstance("SHA-1");
			md256 = MessageDigest.getInstance("SHA-256");
			md512 = MessageDigest.getInstance("SHA-512");

				byte[] tmp = md.digest(a.getBytes("UTF-8"));
				double n = 0;

				for (int j = 0; j < tmp.length; j++)
				{
					n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
				}
				bitset.set((int) (n % this.bitSetSize), true);
				
				tmp = md256.digest(a.getBytes("UTF-8"));
				n = 0;
				for (int j = 0; j < tmp.length; j++)
				{
					n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
				}
				bitset.set((int) (n % this.bitSetSize), true);
				
				tmp = md512.digest(a.getBytes("UTF-8"));
				n = 0;
				for (int j = 0; j < tmp.length; j++)
				{
					n = ((double)(tmp[j] & 0x000000FF)*Math.pow(2, j*8)) % this.bitSetSize + n;
				}
				bitset.set((int) (n % this.bitSetSize), true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	*/
	
	/**
	 *  Mettre le bit à la position 'index' une valeur 'v'
	 **/
	
	public void setBit(int index, boolean v)
	{
		bitset.set(index, v);
	}
	
	/* 
	 * Rendre la valeur d'un bit à la position 'index'
	 * */
	
	public boolean getBit(int index)
	{
		return bitset.get(index);
	}
	
	/** 
	 * Rendre la taille du filtre
	 * */
	
	public int size()
	{
		return bitSetSize;
	}
	
	/** 
	 * Rendre la valeur du filtre sous forme un BitSet
	 * */
	
	public BitSet getBitSet(int start, int stop)
	{
		BitSet res = new BitSet(stop - start);
		
		for (int i = start; i < stop; i++)
			res.set(i, this.getBit(i));
				
		return res;
	}
	
	public String toString()
	{
		String s = new String();
		
		for (int i = 0; i < bitSetSize; i++)
		{
			s += (bitset.get(i)) ? "1" : "0";
		}
		
		return s;
	}
	
	/** 
	 * Test si le filtre contient un autre filtre
	 * */
	
	public boolean in(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BFP2P other = (BFP2P) o;
		
		if (this.bitSetSize != other.size())
			return false;
		
		for (int i = 0; i < bitSetSize; i++)
			if (this.bitset.get(i) && !other.getBit(i))
				return false;
		return true;
	}
	
	/** 
	 * Rendre le fragment à la position 'index'
	 * */
	
	public FragmentP2P getFragment(int index, int sizeOfFragment)
	{
		if (index * sizeOfFragment >= bitSetSize)
		{
			System.out.println("HEREEEEEE PROB " + index*sizeOfFragment + " >= " + bitSetSize);
			return null;
		}
			
		FragmentP2P f = new FragmentP2P(sizeOfFragment);
		int j = 0;
		for (int i = index*sizeOfFragment ; i < (index + 1)*sizeOfFragment; i++)
			f.setBit(j++, this.getBit(i));
		
		return  f;
	}
	
	/**
	 * [start, stop]
	 * 
	 * */
	
	public String toPath(int start, int stop)
	{
		try {
			String s = new String();
			
			if (start < 0)
				throw new ErrorException("toPath : start < 0 = " + start);
			
			int max = stop;
			
			if (stop >= this.bitSetSize/Config.sizeOfFragment)
				max = this.bitSetSize/Config.sizeOfFragment - 1;
			
			for (int i = start; i <= max; i++)
				s += "/" + this.getFragment(i, Config.sizeOfFragment).toInt();
			
			return s;
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public BFP2P pathToBF(String path, int start, int stop, int sizeOfFragment)
	{
		try {
			if (start < 0 || start > stop)
				throw new ErrorException("pathToBF : start invalid = " + start);
			
			String[] s = path.split("/");
			
			if (stop > s.length - 2)
				stop = s.length - 2;
			
			String s_tmp = new String();
			for (int i = start + 1; i <= stop + 1; i++)
				s_tmp += ((new FragmentP2P(sizeOfFragment)).intToFragment(Integer.parseInt(s[i]))).toString();
			
			return (new BFP2P(s_tmp));
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}







