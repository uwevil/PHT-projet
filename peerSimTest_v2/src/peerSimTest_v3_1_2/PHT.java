package peerSimTest_v3_1_2;

import java.io.Serializable;
import java.util.ArrayDeque;
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
	 * Recherche l'identifiant du nœud pour l'insertion.
	 * 
	 * @return {@link String}
	 * 	
	 * @author dcs
	 * */
	
	private String lookup_insert(BF key) throws ErrorException
	{
		String path = "/";
		
		while (true)
		{
			PHT_Node n = this.listNodes.get(path);

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
						int i = 1;
						path += key.getFragment(n.getRang() + i++, 1);
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
	 * Insère dans le système.
	 * 
	 * @author dcs
	 * */
	
	public void insert(BF bf) throws ErrorException
	{				
		BF key = bf.getKey(Config.sizeOfKey);
		String path = this.lookup_insert(key);
		
		PHT_Node systemNode = this.listNodes.get(path);
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
	 * */
	
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
	 * Calcule la clé de stockage.
	 * 
	 * @return {@link String}
	 * @author dcs
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
	 * @author dcs
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
	 * Recherche un nœud précis.
	 * 
	 * @return {@link LookUpRep}
	 * @author dcs
	 * */
	
	@SuppressWarnings("unchecked")
	private LookUpRep lookup(String path, int requestID) throws ErrorException
	{
		//*******************LOG**********************								
		Hashtable<Integer, Object> hashtable = (Hashtable<Integer, Object>) 
			TestSystemIndex_all.config_log.getListAnswer(requestID);
								
		if (hashtable == null)
		{
			hashtable = new Hashtable<Integer, Object>();
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(path);
			hashtable.put(requestID, arrayList);
									
			TestSystemIndex_all.config_log.putListAnswer(requestID, hashtable);
		}
		else
		{
			((ArrayList<String>) hashtable.get(requestID)).add(path);
		}
		//*******************************************
				
		PHT_Node n;
		if (path.equals("/"))
		{
			n = this.listNodes.get(path);
			if (n.isLeafNode())
				return new LookUpRep("LeafNode", "/");
			
			return new LookUpRep("InternalNode", "/");
		}
		else
		{
			n = this.listNodes.get(this.skey(path.substring(1, path.length())));
		}
		
		if (n == null)
			return new LookUpRep("ExternalNode", null);
		
		if (n.isLeafNode())
			return new LookUpRep("LeafNode", "/" + n.getPath());
		
		return new LookUpRep("InternalNode", "/" + n.getPath());
	}
	
	/**
	 * Retourne la position de bit '0' à partir de la position 'pos' précise (inclus).
	 * 
	 * @return int
	 * 
	 * @author dcs
	 * */
	
	private int nextZero(BF key, int pos)
	{
		for (int i = pos; i < key.size(); i++)
			if (!key.getBit(i))
				return i;
		
		return -1;
	}
	
	/**
	 * Retourne la position de bit '1' à partir de la position 'pos' précise (inclus).
	 * 
	 * @return int
	 * 
	 * @author dcs
	 * */
	
	private int nextOne(BF key, int pos)
	{
		for (int i = pos; i < key.size(); i++)
			if (key.getBit(i))
				return i;
		
		return -1;
	}
	
	/**
	 * Récupère la liste des filtres à partir d'un nom du nœud.
	 * 
	 * @return {@code ArrayList<BF>}
	 * 
	 * @author dcs
	 * */
	
	public ArrayList<BF> get(String path)
	{
		return this.listNodes.get(path).getListKeys();
	}

	/**
	 * Recherche tous les filtres qui correspondent avec le filtre de la requête.
	 * 
	 * @return {@code ArrayList<BF>}
	 * 
	 * @author dcs
	 * */
	
	public ArrayList<BF> ssSearch(BF bf) throws ErrorException
	{			
		ArrayList<BF> bfs = new ArrayList<BF>();
		String rootName = "/";
		
		//*************************************************
		Config config = new Config();
		config.getTranslate().setLength(Config.requestRang);
		int requestID = config.getTranslate().translate(bf.toString());
		//*************************************************
		
		ArrayList<String> sbTrees = new ArrayList<String>();
		sbTrees.add(rootName);
		
		while (sbTrees.size() != 0)
		{
			ArrayDeque<String> currentStep = new ArrayDeque<String>(sbTrees);
			sbTrees.clear();
			
			while (!currentStep.isEmpty())
			{
				String sbroot = currentStep.poll();
				int nbStep = 1;
				ArrayList<String> newRoots = 
						this.searchMatchedSubtrees(bf, nbStep, sbroot, bfs, requestID);
				if (newRoots != null)
					sbTrees.addAll(newRoots);
			}
		}
		
		return bfs;
	}
	
	/**
	 * Ajoute les filtres qui correspondent avec celui de la requête dans la liste des filtres.
	 * 
	 * @author dcs
	 * */
	
	@SuppressWarnings("unchecked")
	public void retrieve(BF bf, ArrayList<BF> bfs, String nodePath, int requestID) throws ErrorException
	{
		int found = 0;
		String tmp;
		if (nodePath.equals("/"))
		{
			tmp = nodePath;
		}
		else
		{
			tmp = this.skey(nodePath.substring(1, nodePath.length()));
		}
		
		ArrayList<BF> storedBF = this.get(tmp);
		for (int i = 0; i < storedBF.size(); i++)
		{
			BF bf_tmp = storedBF.get(i);
			if (bf.in(bf_tmp))
			{
				found++;
				bfs.add(bf_tmp);
			}
		}
		//*******************LOG**********************								
		ArrayDeque<String> arrayDeque = 
				TestSystemIndex_all.config_log.getRetrieveState(requestID);
												
		if (arrayDeque == null)
		{
			arrayDeque = new ArrayDeque<String>();
			Hashtable<Integer, Object> hashtable = 
				(Hashtable<Integer, Object>) TestSystemIndex_all.config_log.getListAnswer(requestID);
			ArrayList<String> arrayList = (ArrayList<String>) hashtable.get(requestID);
			String s = (found != 0) ? ("      " + arrayList.size() + "  "+found) : arrayList.size() + "";
			arrayDeque.add(s);
			TestSystemIndex_all.config_log.putRetrieveState(requestID, arrayDeque);
		}
		else
		{
			Hashtable<Integer, Object> hashtable = 
				(Hashtable<Integer, Object>) TestSystemIndex_all.config_log.getListAnswer(requestID);
			ArrayList<String> arrayList = (ArrayList<String>) hashtable.get(requestID);	
			String s = (found != 0) ? ("      " + arrayList.size() + "  "+found) : arrayList.size() + "";
			arrayDeque.add(s);
		}
		//*******************************************
				
	}
	
	private ArrayList<String> next1MatchedRoots(BF bf, String ancestor, int nbZero, ArrayList<BF> bfs
			, int requestID) 
			throws ErrorException
	{
		ArrayList<String> newRoots = new ArrayList<String>();
		ArrayDeque<String> candidates = new ArrayDeque<String>();
		candidates.add(ancestor);
		int n = ancestor.length() + nbZero;
		while (!candidates.isEmpty())
		{
			String anc = candidates.poll();
			String prefix = anc + "1";
			LookUpRep rep = this.lookup(prefix, requestID);
			String label = rep.label;
			if (label != null)
			{
				if (label.length() <= n)
				{
					retrieve(bf, bfs, label, requestID);
				}
				else
				{
					if (label.length() == n + 1)
					{
						retrieve(bf, bfs, label, requestID);
					}
					else
					{
						String sb = label.substring(0, n + 1);
						newRoots.add(sb);
					}
					label = label.substring(0, n);
				}
				
				String spx = label;
				while (spx.length() > anc.length())
				{
					spx = spx.substring(0, spx.length() - 1) + "0";
					candidates.add(spx);
					spx = spx.substring(0, spx.length() - 1);
				}
			}
			else
			{
				retrieve(bf, bfs, anc, requestID);
			}			
		}
		
		return newRoots;
	}
	
	private ArrayList<String> searchMatchedSubtrees(BF bf, int nbStep, String sbroot, ArrayList<BF> bfs
			, int requestID) 
			throws ErrorException
	{
		BF key = bf.getKey(Config.sizeOfKey);
		
		int matchedFragSize = sbroot.length() - 1;
		String prefix = sbroot;
		int nextZ = this.nextZero(key, matchedFragSize);
		int nbOnes = 0;
		if (nextZ < 0)
		{
			nbOnes = key.size() - matchedFragSize;
		}
		else
		{
			nbOnes = nextZ - matchedFragSize;
		}
		if (nbOnes > 0)
		{
			prefix = prefix + "1";
			LookUpRep rep = this.lookup(prefix, requestID);
			if (rep.label == null)
			{
				retrieve(bf, bfs, prefix.substring(0, prefix.length() - 1), requestID);
				return null;
			}
			if (rep.label.length() <= (sbroot.length() + nbOnes))
			{
				retrieve(bf, bfs, rep.label, requestID);
				return null;
			}
			else
			{
				prefix = sbroot;
				for (int i = 1; i < nbOnes; i++)
				{
					prefix += "1";
				}
				matchedFragSize = prefix.length() - 1;
			}	
		}
		int n1 = nextOne(key, matchedFragSize);
		int nbZero = 0;
		if (n1 > 0)
		{
			nbZero = n1 - matchedFragSize;
		}
		else
		{
			nbZero = key.size() - matchedFragSize;
		}
		return this.next1MatchedRoots(bf, prefix, nbZero, bfs, requestID);
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
