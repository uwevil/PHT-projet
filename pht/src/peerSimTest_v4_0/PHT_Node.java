package peerSimTest_v4_0;

import java.io.Serializable;

/**
 * 
 **/

public class PHT_Node implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String path;
	private DataStore data;
	private boolean isLeafNode = true;
	
	
	/**
	 * Initialise avec le chemin {@code path}.
	 * */
	public PHT_Node(String path) {
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
	
	/**
	 * Retourne le rang de ce nœud, -1 si ce nœud est la racine.
	 * */
	public int getRang()
	{
		if (this.path == "/")
			return -1;
		
		return this.path.length() - 1;
	}
	
	/**
	 * Change l'état de ce nœud.
	 * */
	public void setLeafNode(boolean value)
	{
		this.isLeafNode = value;
	}
	
	/**
	 * Retourne {@link DataStore}.
	 * */
	public DataStore getDataStore()
	{
		return this.data;
	}
	
	/**
	 * Remplace l'ancien {@link DataStore} par un nouveau.
	 * */
	@SuppressWarnings("static-access")
	public void setDataStore(DataStore data)
	{
		if (data == null)
			ControlerNw.config_log.totalFilterAdded -= this.data.size();
		
		this.data = data;
	}
	
	/**
	 * Retourne la taille de {@link DataStore} stocké sur ce nœud.
	 * */
	public int size()
	{
		return this.data.size();
	}

	/**
	 * Insère le filtre dans le système.
	 * 
	 * */
	
	public void insert(BF bf) throws ErrorException
	{		
		data.insert(bf);
	}

	/**
	 * Retourne l'état de ce nœud.
	 * */
	public boolean isLeafNode()
	{
		return this.isLeafNode;
	}
	
	public String toString()
	{
		return "Path : " + this.path + "\n" + "IsLeafNode : " +  this.isLeafNode;
	}
	
}
