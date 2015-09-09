package peerSimTest_v4_1;

import java.io.Serializable;
import java.util.ArrayList;

public class PHT_Node_Central implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String path;
	private boolean isLeafNode = true;
	private ArrayList<BF> listKeys;
	
	public PHT_Node_Central(String path)
	{
		this.path = path;
		listKeys  = new ArrayList<BF>();
	}
	
	/**
	 * Rendre l'identifiant du nœud.
	 * 
	 * @return {@link BF}
	 * @author dcs
	 * */
	
	public String getPath()
	{
		return this.path;
	}
	
	public int getRang() throws ErrorException
	{
		if (this.path == "/")
			return -1;
		
		return this.path.length() - 1;
	}
	
	public void setLeafNode(boolean value)
	{
		this.isLeafNode = value;
	}
	
	public boolean isLeafNode()
	{
		return this.isLeafNode;
	}
	
	public ArrayList<BF> getListKeys()
	{
		return this.listKeys;
	}
	
	public void setListKey(ArrayList<BF> listKeys)
	{
		this.listKeys = listKeys;
	}
	
	@SuppressWarnings("static-access")
	public void insert(BF key)
	{
		if (this.listKeys.contains(key))
			return;
		
		ControlerNw.config_log.totalFilterAdded++;
		this.listKeys.add(key);
	}
	
	public int size()
	{
		return this.listKeys.size();
	}

	public String toString()
	{
		return null;
	}
}







