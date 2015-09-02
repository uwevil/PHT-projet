package peerSimTest_v4_1;

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
*
 **/

public class PHT_Central implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Hashtable<String, PHT_Node_Central> listNodes;
	
	public PHT_Central(String indexName) {
		// TODO Auto-generated constructor stub
		this.indexName = indexName;
		listNodes = new Hashtable<String, PHT_Node_Central>();
		listNodes.put("/", new PHT_Node_Central("/"));
	}
	
	/**
	 * Retourne le nom du système index.
	 * 
	 * @return {@link String}
	*
	 * */
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public Hashtable<String, PHT_Node_Central> getListNodes()
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
		this.listNodes = (Hashtable<String, PHT_Node_Central>) serializer.readObject(fileName);
	}
	
	/**
	 * Recherche l'identifiant du nœud pour l'insertion.
	 * 
	 * @return {@link String}
	 * 	
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	private String lookup_insert(BF key) throws  ErrorException
	{
		String path = "/";
		
		while (true)
		{
			PHT_Node_Central n = this.listNodes.get(path);

			if (n.getPath().equals("/"))
			{
				if (n.isLeafNode())
					return path;
				
				path = key.getFragment(0, 1).toString();
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
						path += key.getFragment(n.getRang() + 1, 1);
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
	
	/**
	 * Insère le filtre dans le système.
	 * @throws ErrorException 
	 * @throws  
	 * 
	*
	 * */
	
	public void insert(BF bf) throws ErrorException
	{				
		BF key = bf.getKey(Config.sizeOfKey);
		String path = this.lookup_insert(key);
		
		PHT_Node_Central systemNode = this.listNodes.get(path);
		systemNode.insert(bf);
		
		if (systemNode.size() > Config.gamma)
		{
			systemNode.setLeafNode(false);
			split(systemNode);
		}
	}
	
	/**
	 * Eclater d'un nœud, appelé par la méthode {@code insert}.
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
	
	private void split(PHT_Node_Central n) throws ErrorException
	{		
		ControlerNw.config_log.addSplit(1);
		
		ArrayList<BF> listKeys = n.getListKeys();
		n.setListKey(null);
		
		if (n.getPath().equals("/"))
		{
			PHT_Node_Central new0 = new PHT_Node_Central("0");
			PHT_Node_Central new1 = new PHT_Node_Central("1");
			
			this.listNodes.put("0", new0);
			this.listNodes.put("1", new1);
		}
		else // !n.getPath().equals("/")
		{
			String path = n.getPath();
			
			PHT_Node_Central new0 = new PHT_Node_Central(path + "0");
			PHT_Node_Central new1 = new PHT_Node_Central(path + "1");
			
			String s_tmp = this.skey(path + "0");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new0);
			
			s_tmp = this.skey(path + "1");
			if (this.listNodes.containsKey(s_tmp))
				this.listNodes.remove(s_tmp);
			
			this.listNodes.put(s_tmp, new1);
		}
		
		for (int j = 0; j < listKeys.size(); j++)
			this.insert(listKeys.get(j));
		
	}
	
	/**
	 * Calcule la clé de stockage associée avec le chemin "path".
	 * 
	 * @return {@link String}
	*
	 * */
	
	private String skey(String path) throws ErrorException
	{		
		String rootPath = "/";
		String zeroSeq = "0*";
		String oneSeq = "1*";
		
		if (path.length() <= 0)
			return null;
		
		if (path.equals(rootPath))
			return path;
		
		if (path.matches(zeroSeq))
			return "0";
		
		if (path.matches(oneSeq))
			return "1";
		
		if (path.charAt(path.length() - 1) == '1')
			return this.lpp(path, "01");
		
		return this.lpp(path, "10");
	}
	
	/**
	 * Calcule le plus long préfix de {@code str} qui matche avec la séquence {@code seq}.
	 * 
	 * @return {@link String}
	*
	 * */
	
	private String lpp(String str, String seq) throws ErrorException
	{		
		if (str == null)
			return null;
		
		if (str.length() < seq.length())
		{
			throw new ErrorException("lpp : str.length <= seq.length");
		}
		else
		{
			int occ = str.lastIndexOf(seq);
			
			if (occ != -1)
				return str.substring(0, occ + seq.length());
			
			return null;
		}
	}
	
	/**
	 * Retourne la position de bit '0' à partir de la position 'pos' précise (inclus).
	 * 
	 * @return int
	 * 
	*
	 * */
	
	private int nextZero(BF key, int pos)
	{
		for (int i = pos; i < key.size(); i++)
			if (!key.getBit(i))
				return i;
		
		return -1;
	}
	
	/**
	 * Retourne la position du dernier bit à 1 du filtre de Bloom.
	 * 
	 * @return int
	*
	 * */
	
	private int lastBitSet(BF key)
	{
		int i;
		for (i = key.size() - 1; i >= 0; i--)
		{
			if (key.getBit(i))
				return i;
		}
		
		return -1;
	}

	/**
	 * Recherche tous les filtres qui correspondent avec le filtre de la requête.
	 * 
	 * @return {@code ArrayList<BF>}
	 * 
	*
	 * */
	
	public ArrayList<BF> supersetSearch(BF bf) throws ErrorException
	{					
		ArrayList<BF> bfs = new ArrayList<BF>();
		BF q = bf.getKey(Config.sizeOfKey);
		String sbroot = "/";
		int nextZ = this.nextZero(q, 0);
		int nbOnes = 0;
		if (nextZ < 0)
		{
			nbOnes = q.size();
		}
		else
		{
			nbOnes = nextZ;
		}
		if (nbOnes > 0)
		{
			sbroot += q.toString().substring(0, nbOnes);
		}
		this.exploreSubtree(sbroot, q, bf, bfs);
		
		return bfs;
	}
	
	/**
	 * Ajoute les filtres qui correspondent avec celui de la requête dans la liste des filtres.
	 * 
	 * @throws 
	*
	 * */
	
	public void retrieveSuperset(ArrayList<BF> storedBF, BF bf, ArrayList<BF> bfs, String path) 
			throws ErrorException
	{
		for (int i = 0; i < storedBF.size(); i++)
		{
			BF bf_tmp = storedBF.get(i);
			if (bf.in(bf_tmp))
			{
				bfs.add(bf_tmp);
			}
		}		
	}
	
	/**
	 * Recherche tous les branches dans l'arbre à partir de "sbroot" qui correspondent avec la requête.
	 * 
	 * */
	
	private void exploreSubtree(String sbroot, BF q, BF bf, ArrayList<BF> bfs) throws ErrorException
	{
		String prefix = sbroot + "1";
		PHT_Node_Central n = this.listNodes.get(this.skey(prefix.substring(1, prefix.length())));
		if (n == null)
		{
			String path = prefix.substring(0, prefix.length() - 1);
			String tmp;
			if (path.equals("/"))
			{
				tmp = path;
			}
			else
			{
				tmp = this.skey(path.substring(1, path.length()));
			}
			
			ArrayList<BF> storedBF = this.listNodes.get(tmp).getListKeys();
			
			this.retrieveSuperset(storedBF, bf, bfs, path);
		}
		else
		{
			String label = "/" + n.getPath();
			int toMatchLen = this.lastBitSet(q) + 1;
			if (label.length() > (toMatchLen + 1))
			{
				label = label.substring(0, toMatchLen + 1);
				this.collectLeaves(label, bf, bfs);
				label = label.substring(0, toMatchLen);
			}
			else
			{
				String path = label;
				String tmp;
				if (path.equals("/"))
				{
					tmp = path;
				}
				else
				{
					tmp = this.skey(path.substring(1, path.length()));
				}
				
				ArrayList<BF> storedBF = this.listNodes.get(tmp).getListKeys();
				
				this.retrieveSuperset(storedBF, bf, bfs, path);
			}
			int minLen = sbroot.length();
			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				BF qprefix = q.getSubFilter(0, bs_label.length() - 1);
				bs_label = bs_label.substring(0, bs_label.length() - 1) + "0";
				if (qprefix.in(new BF(bs_label)))
				{
					this.exploreSubtree("/" + bs_label, q, bf, bfs);
				}
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
	}
	
	private void collectLeaves(String sbroot, BF bf, ArrayList<BF> bfs) throws ErrorException
	{
		String prefix = sbroot + "1";
		PHT_Node_Central n = this.listNodes.get(this.skey(prefix.substring(1, prefix.length())));

		if (n == null)
		{
			String path = prefix.substring(0, prefix.length() - 1);
			String tmp;
			if (path.equals("/"))
			{
				tmp = path;
			}
			else
			{
				tmp = this.skey(path.substring(1, path.length()));
			}
			
			ArrayList<BF> storedBF = this.listNodes.get(tmp).getListKeys();
			
			this.retrieveSuperset(storedBF, bf, bfs, path);
		}
		else
		{
			String label = "/" + n.getPath();
			String path = label;
			String tmp;
			if (path.equals("/"))
			{
				tmp = path;
			}
			else
			{
				tmp = this.skey(path.substring(1, path.length()));
			}
			
			ArrayList<BF> storedBF = this.listNodes.get(tmp).getListKeys();
			
			this.retrieveSuperset(storedBF, bf, bfs, path);
			int minLen = sbroot.length();
			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				this.collectLeaves("/" + bs_label, bf, bfs);
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
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
	 * @throws  
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
	*
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
