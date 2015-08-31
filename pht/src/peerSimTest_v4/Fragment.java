package peerSimTest_v4;

import java.io.Serializable;
import java.util.BitSet;

public class Fragment implements Serializable{

	/**
	 * Fragment
	 * 
	 * @author dcs
	 */
	private static final long serialVersionUID = 1L;
	private BitSet bitset;
	private int size;
	
	/**
	 * Crée un fragment vide de taille "nbits"
	 * 
	 * @param nbits
	 * @author dcs
	 * */
	public Fragment(int nbits)
	{
		bitset = new BitSet(nbits);
		this.size = nbits;
	}
	
	/**
	 * Rend le fragment sous forme un BitSet
	 * 
	 * @author dcs
	 * */
	
	public BitSet getBitSet()
	{
		return this.bitset;
	}
	
	/**
	 * Met le bit à la position 'index' la valeur 'value'.
	 * 
	 * @author dcs
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
	
	/**
	 * Teste si le fragment contient un autre. Les deux peuvent avoir deux tailles différentes.
	 * 
	 * @return true si le fragment est contenu dans {@code o}
	 * @author dcs
	 * */
	
	public boolean in(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
		final Fragment other = (Fragment) o;
		
		if (this.size != other.size())
			return false;
		
		for (int i = 0; i < this.size(); i++)
			if (bitset.get(i) && !other.get(i))
				return false;
		return true;
	}
	
	/**
	 * Teste l'égalité entre 2 fragments.
	 * 
	 * @author dcs
	 * */
	
	public boolean equals(Object o)
	{
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		final Fragment other = (Fragment) o;
		
		if (this.size != other.size())
			return false;
		
		if (this.bitset != other.getBitSet() && (this.bitset == null || !this.bitset.equals(other.getBitSet())))
			return false;
		return true;
	}
	
	/** 
	 * Rend la valeur d'un bit à la position 'index'.
	 * 
	 * @return {@link Boolean}
	 * @author dcs
	 * */
	
	public boolean get(int index)
	{
		return bitset.get(index);
	}
	
	/** 
	 * Rend la taille du fragment.
	 * 
	 * @return int
	 * @author dcs
	 * */
	
	public int size()
	{
		return this.size;
	}
	
}
