package peerSimTest_v3_1_3;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.BitSet;

public class BF implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BitSet bitset;
	private int bitSetSize;
	
	/** 
	 * Créer un filtre vide.
	 * 
	 * @param bitSetSize taille du filtre.
	 * 
	 * @author dcs
	 * */
	
	public BF(int bitSetSize)
	{
		this.bitset = new BitSet(bitSetSize);
		this.bitSetSize = bitSetSize;
	}
	
	/**
	 * Créer un filtre vide.
	 * 
	 * @author dcs
	 * */
	public BF()
	{
		this.bitSetSize = 0;
		this.bitset = new BitSet(0);
	}
	
	/** 
	 * Créer un filtre.
	 *
	 * @param chaineBits de 0 et 1.
	 * @throws ErrorException
	 * @author dcs
	 * */
	public BF(String chaineBits) throws ErrorException
	{
		char[] chararray = chaineBits.toCharArray();
			
		this.bitset = new BitSet(chararray.length);
		this.bitSetSize = chararray.length;
		for (int i = 0; i< this.bitSetSize; i++)
		{
			if (chararray[i] != '0' && chararray[i] != '1')
			{
				throw new ErrorException("chaineBits contient des caractères spéciales = " + chaineBits);
			}
			if (chararray[i] == '1')
				this.bitset.set(i, true);
		}
	}
	
	/**
	 * Créer un filtre à partir de 2 filtres.
	 * 
	 * @param a
	 * @param b
	 * @author dcs
	 * */
	
	public BF (BF a, BF b)
	{
		this.bitSetSize = a.size()+b.size();
		this.bitset = new BitSet(bitSetSize);
		
		for (int i = 0; i < a.size(); i++)
			this.bitset.set(i, a.getBit(i));
		
		int j = a.size();
		for (int i = 0; i < b.size(); i++)
			this.bitset.set(j+i, b.getBit(i));
	}
	
	/** 
	 * Test l'égalité entre 2 filtres.
	 * 
	 * @author dcs
	 * */
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BF other = (BF) o;

		int min = other.size() >= this.bitSetSize ? this.bitSetSize : other.size();
		
		for (int i = 0; i < min; i++)
			if (other.getBit(i) != this.getBit(i))
				return false;
		return true;
	}
	
	/** 
	 * Ajout une chaîne de description dans le filtre.
	 * 
	 * @param description suite de mots clés sous forme mot0,mot1,mot2,…,motn.
	 * @author dcs
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
	 *  
	 *  @param index position du bit.
	 *  @param v valeur booléenne.
	 *  @author dcs
	 **/
	public void setBit(int index, boolean v)
	{
		bitset.set(index, v);
	}
	
	/**
	 * Rendre la valeur du bit à la position 'index'
	 * 
	 * @param index position du bit.
	 * @return {@link Boolean}
	 * @author dcs
	 * */
	public boolean getBit(int index)
	{
		return bitset.get(index);
	}
	
	/** 
	 * Rendre la taille du filtre
	 * 
	 * @return int
	 * @author dcs
	 * */
	public int size()
	{
		return bitSetSize;
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
	 * 
	 * @param o objet.
	 * @return {@link Boolean}
	 * @author dcs
	 * */
	public boolean in(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final BF other = (BF) o;
		
		int min = other.size() >= this.bitSetSize ? this.bitSetSize : other.size();
		for (int i = 0; i < min; i++)
			if (this.bitset.get(i) && !other.getBit(i))
				return false;
		return true;
	}
	
	/** 
	 * Rendre le fragment de taille connue à la position précise.
	 * 
	 * @param index position du fragment dans le filtre.
	 * @param sizeOfFragment taille du fragment.
	 * @return {@link Fragment}
	 * 
	 * @author dcs
	 * */
	public Fragment getFragment(int index, int sizeOfFragment)
	{
		if (index * sizeOfFragment >= bitSetSize)
		{
			System.out.println("HEREEEEEE PROB " + index*sizeOfFragment + " >= " + bitSetSize);
			return null;
		}
			
		Fragment f = new Fragment(sizeOfFragment);
		int j = 0;
		for (int i = index*sizeOfFragment ; i < (index + 1)*sizeOfFragment; i++)
			f.setBit(j++, this.getBit(i));
		
		return  f;
	}
	
	/**
	 * Retourne un filtre à partir du chemin "/././." dans l'interval [start, stop] avec la taille d'un fragment.
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public BF pathToBF(String path, int start, int stop, int sizeOfFragment)
	{
		try {
			if (start < 0 || start > stop)
				throw new ErrorException("pathToBF : start invalid = " + start);
			
			String[] s = path.split("/");
			
			if (stop > s.length - 2)
				stop = s.length - 2;
			
			String s_tmp = new String();
			for (int i = start + 1; i <= stop + 1; i++)
				s_tmp += ((new Fragment(sizeOfFragment)).intToFragment(Integer.parseInt(s[i]))).toString();
			
			return (new BF(s_tmp));
		} catch (ErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Retourne le rang du filtre entre [0, bitSetSize/sizeOfFragment].
	 * 
	 * @return int
	 * @author dcs
	 * */
	
	public int getRang(int sizeOfFragment)
	{
		if (this.bitSetSize == 0)
			return -1;
		
		return this.bitSetSize/sizeOfFragment - 1;
	}
	
	/**
	 * Retourne une clé sous forme le filtre.
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public BF getKey(int sizeOfKey)
	{
		BF rep = this.getSubFilter(0, sizeOfKey - 1);

		for (int i = sizeOfKey; i < this.bitSetSize; i++)
		{
			if (this.getBit(i))
			{
				rep.setBit(i % sizeOfKey, true);
			}
		}
		return rep;
	}
	
	/**
	 * Retourne un sous filtre de ce filtre dans l'interval [start, stop].
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public BF getSubFilter(int start, int stop)
	{
		BF res = new BF(stop - start + 1);
		
		int j = 0;
		for (int i = start; i <= stop; i++)
			res.setBit(j++, this.getBit(i));
		
		return res;
	}
	
}







