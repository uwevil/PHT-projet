package peerSimTest_v4;

import java.io.Serializable;

/**
 * 
 * SystèmeIndexP2P gère les nœuds
 * <p>
 * Variable locale : 
 * <ul>
 * 	<li> indexName
 * 	<li> serverID
 * 	<li> listeNode
 * </ul>
 * 
*
 **/

public class PHT_IndexNode implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String path;
	private DataStore data;
	private boolean isLeafNode = true;
	
	public PHT_IndexNode(String path) {
		// TODO Auto-generated constructor stub
		this.path = path;
		data = new DataStore();
	}
	
	/**
	 * Retourne le nom du système index.
	 * 
	 * @return {@link String}
	*
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
	
	public DataStore getDataStore()
	{
		return this.data;
	}
	
	public void setDataStore(DataStore data)
	{
		this.data = data;
	}
	
	public int size()
	{
		return this.data.size();
	}

	/**
	 * Insère le filtre dans le système.
	 * 
	*
	 * */
	
	public void insert(BF bf) throws ErrorException
	{				
		data.insert(bf);
	}
	
	
}
