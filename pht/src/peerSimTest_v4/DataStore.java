package peerSimTest_v4;

import java.io.Serializable;
import java.util.ArrayList;

public class DataStore implements Serializable{

	private static final long serialVersionUID = 1L;
	private ArrayList<BF> listKeys;
	
	public DataStore()
	{
		this.listKeys  = new ArrayList<BF>();
	}
	
	public ArrayList<BF> getListKeys()
	{
		return this.listKeys;
	}
	
	public void setListKey(ArrayList<BF> listKeys)
	{
		this.listKeys = listKeys;
	}
	
	public void insert(BF key)
	{
		if (this.listKeys.contains(key))
			return;
		
		this.listKeys.add(key);
	}
	
	public int size()
	{
		return this.listKeys.size();
	}

	public String toString()
	{
		return this.listKeys.toString();
	}

	
}
