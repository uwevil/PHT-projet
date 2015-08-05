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
	private int bitsPerElement;
	private int bitSetSize;
	
	/* 
	 * Créer un filtre vide avec la taille bitSetSize et la taille d'un fragment btsPerElement
	 * */
	
	public BFP2P(int bitSetSize, int bitsPerElement)
	{
		this.bitset = new BitSet(bitSetSize);
		this.bitSetSize = bitSetSize;
		this.bitsPerElement = bitsPerElement;
	}
	
	/* 
	 * Créer un filtre à partir d'une chaîne de caractère : 0 et 1 et avec la taille d'un fragment bitsPerElement
	 * lever Exception quand la chaîne contient les autres caractères.
	 * */
	
	public BFP2P(String chaineBits, int bitsPerElement) throws ErrorException
	{
		char[] chararray = chaineBits.toCharArray();
	
		this.bitsPerElement = bitsPerElement;
		
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
	
	/* 
	 * Test l'égalité entre 2 filtres
	 * */
	
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BFP2P other = (BFP2P) o;
		
		if (this.bitSetSize != other.bitSetSize)
			return false;
		if (this.bitsPerElement != other.bitsPerElement)
			return false;
		if (this.bitset != other.bitset && (this.bitset == null || !this.bitset.equals(other.bitset)))
			return false;
		return true;
	}
	
	/* 
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
	
	/*
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
	
	/* 
	 * Rendre la taille du filtre
	 * */
	
	public int size()
	{
		return bitSetSize;
	}
	
	/* 
	 * Rendre la valeur du filtre sous forme un BitSet
	 * */
	
	public BitSet getBitSet(int start, int stop)
	{
		BitSet res = new BitSet(stop - start);
		
		for (int i = start; i < stop; i++)
			res.set(i, this.getBit(i));
				
		return res;
	}
	
	/* 
	 * Rendre la taille d'un fragment
	 * */
	
	public int getBitsPerElement()
	{
		return this.bitsPerElement;
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
	
	/* 
	 * Test si le filtre contient un autre filtre
	 * */
	
	public boolean in(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BFP2P other = (BFP2P) o;
		
		if (this.bitSetSize != other.bitSetSize)
			return false;
		if (this.bitsPerElement != other.bitsPerElement)
			return false;
		
		for (int i = 0; i < bitSetSize; i++)
			if (this.bitset.get(i) && !other.getBit(i))
				return false;
		return true;
	}
	
	/* 
	 * Rendre le fragment à la position 'index'
	 * */
	
	public FragmentP2P getFragment(int index)
	{
		if (index * this.bitsPerElement >= bitSetSize)
		{
			System.out.println("HEREEEEEE PROB " + index*this.bitsPerElement + " >= " + bitSetSize);
			return null;
		}
			
		FragmentP2P f = new FragmentP2P(bitsPerElement);
		int j = 0;
		for (int i = index*bitsPerElement ; i < (index + 1)*bitsPerElement; i++)
			f.setBit(j++, this.getBit(i));
		
		return  f;
	}

}







