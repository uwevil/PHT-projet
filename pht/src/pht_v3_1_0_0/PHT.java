package pht_v3_1_0_0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

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
 * @author dcs
 **/

public class PHT implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Hashtable<String, PHT_Node> listNodes;
	
	public PHT(String indexName) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		listNodes = new Hashtable<String, PHT_Node>();
		listNodes.put("/", new PHT_Node("/"));
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public Hashtable<String, PHT_Node> getListNodes()
	{	
		return this.listNodes;
	}
	
	public void serializeListNodes(String fileName)
	{
		Serializer serializer = new Serializer();
		serializer.writeObject(listNodes, fileName);
	}
	
	@SuppressWarnings("unchecked")
	public void deserializeListNodes(String fileName)
	{
		Serializer serializer = new Serializer();
		this.listNodes = (Hashtable<String, PHT_Node>) serializer.readObject(fileName);
	}
	
	/**
	 * Recherche l'identifiant du nœud correspondant.
	 * 
	 * @return {@link String}
	 * 	
	 * @author dcs
	 * */
	
	private String lookup(BF key) throws ErrorException
	{
		String path = "/";
		
		while (true)
		{
			PHT_Node n = this.listNodes.get(path);

			if (n.getPath().equals("/"))
			{
				if (n.isLeafNode())
					return path;
				
				path = key.getFragment(0, Config.sizeOfElement).toString();
			}
			else // !n.getPath().equals("/")
			{				
				BF bf_tmp = new BF(n.getPath());
				if (key.equals(bf_tmp))
				{					
					if (n.isLeafNode())
					{
						return path;
					}
					else // !n.isLeafNode()
					{
						int i = 1;
						path += key.getFragment(n.getRang() + i++, Config.sizeOfElement);
					}
				}
				else // !key.equals(bf_tmp)
				{
					int rang = n.getRang();

					String s = new String();
					for (int i = 0; i <= rang; i++)
					{
						if (key.getBit(i) == bf_tmp.getBit(i))
							s += (key.getBit(i)) ? "1" : "0";
						
						if (key.getBit(i) == !bf_tmp.getBit(i))
						{
							s += (key.getBit(i)) ? "1" : "0";
							break;
						}
					}
					
					path = s;
				}
			}
		}
	}
	
	public void insert(BF key) throws ErrorException
	{				
		String path = this.lookup(key);
		
		PHT_Node systemNode = this.listNodes.get(path);
		systemNode.insert(key);
		
		if (systemNode.size() > Config.gamma)
		{
			systemNode.setLeafNode(false);
			split(systemNode);
		}
	}
	
	private void split(PHT_Node n) throws ErrorException
	{		
		ArrayList<BF> listKeys = n.getListKeys();
		n.setListKey(null);
		
		if (n.getPath().equals("/"))
		{
			PHT_Node new0 = new PHT_Node("0");
			PHT_Node new1 = new PHT_Node("1");
			
			this.listNodes.put("0", new0);
			this.listNodes.put("1", new1);
		}
		else // !n.getPath().equals("/")
		{
			String path = n.getPath();
			
			PHT_Node new0 = new PHT_Node(path + "0");
			PHT_Node new1 = new PHT_Node(path + "1");
			
			String s_tmp = this.computeEntry(path + "0");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new0);
			
			s_tmp = this.computeEntry(path + "1");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new1);
		}
		
		for (int j = 0; j < listKeys.size(); j++)
			this.insert(listKeys.get(j));
		
	}
	
	private String computeEntry(String path)
	{
		String s_tmp = new String();
		
		if (path.length() > 1)
		{
			char[] tmp = path.toCharArray();
			
			int i = 0;
			for (i = tmp.length - 1; i > 0; i--)
			{
				if (tmp[i] != tmp[i - 1])
					break;
			}
			
			if (i != -1)
				s_tmp = path.substring(0, i + 1);
		}
		else
		{
			return path;
		}
		
		return s_tmp;
	}
	
	private ArrayList<String> computePath(String entry, String path) throws ErrorException
	{
		if (entry.length() == path.length())
			return null;
		
		ArrayList<String> res = new ArrayList<String>();
		
		int len = path.length() - entry.length();

		if (path.charAt(entry.length()) == '0')
		{
			for (int i = entry.length(); i < entry.length() + len; i++)
			{
				BF bf_tmp = new BF(entry + new BF(len).toString());
				bf_tmp.setBit(i, true);

				String tmp = computeEntry(bf_tmp.toString());
				if (i < entry.length() + len - 1)
				{
					res.add(tmp.substring(0, tmp.length() - 1));
				}
				else
				{
					res.add(tmp);
				}
			}
		}
		else // path.charAt(entry.length()) != '0'
		{
			BF bf_tmp0 = new BF(len);
			for (int i = 0; i < bf_tmp0.size(); i++)
				bf_tmp0.setBit(i, true);
			
			for (int i = entry.length(); i < entry.length() + len; i++)
			{	
				BF bf_tmp = new BF(entry + bf_tmp0.toString());
				bf_tmp.setBit(i, false);
				
				String tmp = computeEntry(bf_tmp.toString());
				if (i < entry.length() + len - 1)
				{
					res.add(tmp.substring(0, tmp.length() - 1));
				}
				else
				{
					res.add(tmp);
				}
			}
		}
		
		return res;
	}

	public Object search(BF key) throws ErrorException
	{		
		ArrayList<BF> res = new ArrayList<BF>();
		ArrayList<String> listPaths = new ArrayList<String>();
		
		listPaths.add("/");
		int k = 0;
		while (k < listPaths.size())
		{
			String path = listPaths.get(k);
			PHT_Node n = this.listNodes.get(path);
			
			if (path == "/")
			{
				if (n != null && n.isLeafNode())
				{
					ArrayList<BF> listKeys = n.getListKeys();
					if (listKeys != null && listKeys.size() != 0)
					{
						for (int j = 0; j < listKeys.size(); j++)
						{
							BF bf_tmp = listKeys.get(j);
							
							if (key.in(bf_tmp))
								res.add(bf_tmp);
						}
					}
					
					return res;
				}
				else if (n != null) // !n.isLeafNode
				{
					String s_tmp = key.getFragment(0, Config.sizeOfElement).toString();

					if (s_tmp.equals("0"))
					{
						listPaths.add("0");
						listPaths.add("1");
					}
					else // !s_tmp.equals("0")
					{
						listPaths.add("1");
					}
				}
			}
			else // path != "/"
			{	
				if (n != null) 
				{
					if (key.in(new BF(n.getPath())))
					{
						if (n.isLeafNode())
						{
							ArrayList<BF> listKeys = n.getListKeys();
							if (listKeys != null && listKeys.size() != 0)
							{
								for (int j = 0; j < listKeys.size(); j++)
								{
									BF bf_tmp = listKeys.get(j);
									
									if (key.in(bf_tmp))
										res.add(bf_tmp);
								}
							}
						}
					}
					
					ArrayList<String> tmp = this.computePath(path, n.getPath());
					
					if (tmp != null)
					{
						for (int i = 0; i < tmp.size(); i++)
						{
							BF bf_tmp = new BF(tmp.get(i));
							
							if (key.in(bf_tmp))
								listPaths.add(tmp.get(i));
						}
					}
				}
			}
			k++;
		}
		
		//*******************LOG**********************
				Config config_log = new Config();
				config_log.getTranslate().setLength(Config.requestRang);
				int requestID = config_log.getTranslate().translate(key.toString());
				
				@SuppressWarnings("unchecked")
				Hashtable<Integer, Object> hashtable = (Hashtable<Integer, Object>) TestSystemIndex_all.config_log.getListAnswer(requestID);
				
				if (hashtable == null)
				{
					hashtable = new Hashtable<Integer, Object>();
					hashtable.put(requestID, listPaths);
					
					TestSystemIndex_all.config_log.putListAnswer(requestID, hashtable);
				}
				//*******************************************

		return res;
	}
	 
	/**
	 * Rechercher le filtre précise
	 * 
	 * @param bf
	 * @param path
	 * @return
	 * 	<ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link BF}
	 * 	</ul>
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	public Object searchExact(BF key) throws ErrorException
	{		
		return null;
	}
	
	/**
	 * Supprimer le filtre dans le chemin précis.
	 * 
	 * @param bf
	 * @param path
	 * 
	 * @return
	 * 	<li> soit null
	 * 	<li> soit un message vers le serveur hébergé
	 * 		<ul>
				<li> type : remove(supprimer le filtre), removeNode(supprimer un nœud)
				<li> une chaîne de caractères
			</ul>
	 * 
	 * @author dcs
	 * */
	
	public Object remove(BF key)
	{
		return null;
	}
	
	public int size()
	{
		return this.listNodes.size();
	}
	
	public String toString()
	{
		return null;
	}
}
