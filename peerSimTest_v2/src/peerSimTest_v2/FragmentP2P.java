package peerSimTest_v2;

import java.io.Serializable;
import java.util.BitSet;

public class FragmentP2P implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BitSet bitset;
	private int size;
	
	/* 
	 * Créer un fragment vide de taille nbits
	 * */
	
	public FragmentP2P(int nbits)
	{
		bitset = new BitSet(nbits);
		this.size = nbits;
	}
	
	/* 
	 * Rendre le fragment sous forme un BitSet
	 * */
	
	public BitSet getBitSet()
	{
		return this.bitset;
	}
	
	/* 
	 * Mettre le bit à la position 'index' la valeur 'value'
	 **/
	
	public void setBit(int index, boolean value)
	{
		bitset.set(index, value);	
	}
	
	public String toString()
	{
		String s = new String();
		
		for (int i = 0; i < this.size; i++)
		{
			s += (bitset.get(i)) ? "1" : "0";
		}
		
		return s;
	}
	
	/* 
	 * Test si le fragment contient un autre
	 * */
	
	public boolean in(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
		final FragmentP2P other = (FragmentP2P) o;
		
		if (this.size != other.size())
			return false;
		
		for (int i = 0; i < this.size(); i++)
			if (bitset.get(i) && !other.get(i))
				return false;
		return true;
	}
	
	/* 
	 * Test l'égalité entre 2 fragments
	 * */
	
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final FragmentP2P other = (FragmentP2P) o;
		
		if (this.size != other.size())
			return false;
		
		if (this.bitset != other.getBitSet() && (this.bitset == null || !this.bitset.equals(other.getBitSet())))
			return false;
		return true;
	}
	
	/**
	 * Convertir le fragment en entier
	 **/
	
	public int toInt()
	{
		int res = 0;
		for (int i = 0; i < this.size; i++)
		{
			res += this.bitset.get(i) ? (int)Math.pow(2, i) : 0;
		}
		return res;
	}
	
	/* 
	 * Convertir un entier 'a' en fragment de taille 'nbits'
	 * */
	
	public FragmentP2P intToFragment(int a)
	{
		int val = a;
		FragmentP2P f = new FragmentP2P(this.size);
		
		for (int i = 0; i < this.size; i++)
		{
			if (1<<(this.size - i - 1) <= val)
			{
				f.setBit(this.size - i - 1, true);
				val = val - (1<<(this.size - i - 1));
			}
		}
		
		return f;
	}
	
	/* 
	 * Rendre la valeur d'un bit à la position 'index'
	 * */
	
	public boolean get(int index)
	{
		return bitset.get(index);
	}
	
	/* 
	 * Rendre la taille du fragment
	 * */
	
	public int size()
	{
		return this.size;
	}
	
	public String toPath()
	{
		return "/" + this.toInt();
	}
	
	public FragmentP2P pathToFragment(String path)
	{		
		char[] array = path.toCharArray();
		String s = new String();
		for (int i = 1; i < array.length; i++)
		{
			if (array[i] != '/')
				s += array[i];
		}
		
		FragmentP2P f = (new FragmentP2P(this.size)).intToFragment(Integer.parseInt(s));
		return f;
	}
}
