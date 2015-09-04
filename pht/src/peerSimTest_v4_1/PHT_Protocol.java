package peerSimTest_v4_1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class PHT_Protocol implements EDProtocol{

	public static int init;
	
	private static final String PAR_TRANSPORT = "transport";
	private String prefix;
	private int tid;
	private int nodeIndex;
	private Transport t;
	private int id;
	
	private Hashtable<String, PHT_Node> list = new Hashtable<String, PHT_Node>();
	private Hashtable<Long, String> listPath = new Hashtable<Long, String>();
	private ArrayDeque<String> insertFIFO = new ArrayDeque<String>();
	private ArrayDeque<BF> searchFIFO = new ArrayDeque<BF>();
	private ArrayDeque<String> keywordsFIFO = new ArrayDeque<String>();

	private Hashtable<Long, BF> listRequests = new Hashtable<Long, BF>();
	
	private Hashtable<String, ArrayList<BF>> splitData = new Hashtable<String, ArrayList<BF>>();
			
	private int[] listCreateNode = new int[Network.size()];
	private int[] listPUTData = new int[Network.size()];
	private String pathTest;
	private boolean prob = false;
	
	private Hashtable<Long, Object> listPUT = new Hashtable<Long, Object>();
			
	private ArrayDeque<Message> messageWaitingForResponse = new ArrayDeque<Message>();
		
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	private boolean insert_OK = true;
	private boolean search_OK = true;	
	
	private Hashtable<String, PHT_Node_Central> pht_listNodes;
	

	public PHT_Protocol(String prefix)
	{
		this.prefix = prefix;
		tid = Configuration.getPid(prefix+ "." + PAR_TRANSPORT);
		
		for (int i = 0; i < Network.size(); i++)
			recu[i] = 0;
		
	}
	
	public void setID(int id)
	{
		this.id = id;
	}
	
	public void setNodeIndex(int nodeIndex)
	{
		this.nodeIndex = nodeIndex;
	}
	
	public Object clone()
	{
		PHT_Protocol s = new PHT_Protocol(prefix);
		s.tid = this.tid;
		s.prefix = this.prefix;
		s.nodeIndex = this.nodeIndex;
		s.id = this.id;
		s.recu = this.recu;
		return s;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void processEvent(Node node, int pid, Object event)
	{
		t = (Transport) Network.get(nodeIndex).getProtocol(tid);
		Message message = (Message)event;
/*
		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG, true);
		wf.write(message + "\n");
		wf.close();
*/
		ControlerNw.config_log.numberOfMessages++;
		
		switch(message.getType())
		{
		case CREATE_INDEX: 
			break;
			
		case REMOVE_INDEX: 
			break;
			
		case INSERT_INIT :
			try
			{
				treatInsertInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case SEARCH_INIT :
			try
			{
				treatSearchInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case PUT:
			try
			{
				treatPUT(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case PUT_OK :
			try
			{
				treatPUT_OK(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case CREATE_NODE_OK :
			try
			{
				treatCreateNode_OK(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case PUT_DATA:
			try
			{
				treatPutData(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case PUT_DATA_OK:
			try
			{
				treatPutData_OK(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case INSERT :
			try
			{
				treatInsert(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case GET_STATUS: 
			treatGetStatus(message, pid);
			break;
			
		case GET_STATUS_OK:
			try
			{
				treatGetStatus_OK(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case LOOKUP_PATH_OK :
			try 
			{
				treatLookupPath_OK(message, pid);
			} 
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
		case LOOKUP_PATH :
			treatLookupPath(message, pid);
			break;
			
		case CREATE_NODE :
			try
			{
				treatCreateNode(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
				
		case GET_STORED_BF :
			treatGetStoredBF(message, pid);
			break;
			
		case GET_STORED_BF_OK :
			treatGetStoredBF_OK(message, pid);
			break;
			
		case GET_COLLECT_LEAVES :
			treatGetCollectLeaves(message, pid);
			break;
			
		case GET_COLLECT_LEAVES_OK :
			try
			{
				treatGetCollectLeaves_OK(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
			
			
		case SIMULATION :
			try
			{
				treatSimulation(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
		case CREATE_SIMULATION :
			treatCreateSimulation(message, pid);
			break;
			
		case CREATE_SIMULATION_OK :
			treatCreateSimulation_OK(message, pid);
			break;
			
			
			
			
			
		case OVERVIEW :
			try
			{
				treatOverview(message, pid);
			}
			catch (ErrorException e)
			{
				e.printStackTrace();
			}
			break;
			
		case OVERVIEW_OK : 
			treatOverview_OK(message, pid);
			break;
			
		default : 
			break;
		}
	}

	/**
	 * Traite le message de simulation de l'initiateur.
	 * */
	
	private void treatCreateSimulation(Message message, int pid)
	{
		String path = message.getPath();
		PHT_Node_Central n = (PHT_Node_Central) message.getData();
		PHT_Node n_tmp = new PHT_Node(n.getPath());
		
		DataStore data = new DataStore();
		data.setListBFs(n.getListKeys());
		
		n_tmp.setDataStore(data);
		n_tmp.setLeafNode(n.isLeafNode());
		
		this.list.put(path, n_tmp);
		
		Message rep = new Message();
		rep.setType(MessageType.CREATE_SIMULATION_OK);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	/**
	 * L'inititateur reçoit l'aquittement après l'envoi de message de simulation.
	 * */
	
	private void treatCreateSimulation_OK(Message message, int pid)
	{
		if (!pht_listNodes.isEmpty())
		{
			Enumeration<String> enumeration = pht_listNodes.keys();
			
			while (enumeration.hasMoreElements())
			{
				String path = enumeration.nextElement();
				
				Message rep = new Message();
				rep.setType(MessageType.CREATE_SIMULATION);
				rep.setData(pht_listNodes.get(path));
				rep.setPath(path);
				rep.setSource(nodeIndex);
				
				ControlerNw.config_log.getTranslate().setRange(Network.size());
				int serverID = ControlerNw.config_log.getTranslate().translate(path);
				
				rep.setDestinataire(serverID);
				
				t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
				
				pht_listNodes.remove(path);
				
				break;
			}
		}
		else if (nodeIndex == init)
		{
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType(MessageType.OVERVIEW);
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}	
	}

	/**
	 * Traite le message de simulation au lancement de la simulation.
	 * */
	
	private void treatSimulation(Message message, int pid) throws ErrorException
	{
		init = nodeIndex;
		
		PHT_Central pht= new PHT_Central("dcs");

		int line = 0;
		int k = 0;
		
		try(BufferedReader reader = new BufferedReader(new FileReader(Config.fileWiki)))
		{
			while (true)
			{
				String s = new String();
				s = reader.readLine();
				if (s == null)
					break;
				String[] tmp = s.split(";");
				
				if (tmp.length >= 2 && tmp[1].length() > 2 )
				{
					BF bf = new BF(Config.sizeOfBF);
					bf.addAll(tmp[1]);
					
					pht.insert(bf);
					line++;
				}
				k++;
				System.out.println(line + "/" + k);
				if (line == Config.numberOfFiltersTest)
					break;
			}
			reader.close();
			
			System.out.println("Fini de lecture " + k + " lignes.");
			System.out.println("Nombre de filtres réels : " + line + " filtres.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
						
		pht_listNodes = pht.getListNodes();
		
		Enumeration<String> enumeration = pht_listNodes.keys();
		
		while (enumeration.hasMoreElements())
		{
			String path = enumeration.nextElement();
			
			Message rep = new Message();
			rep.setType(MessageType.CREATE_SIMULATION);
			rep.setData(pht_listNodes.get(path));
			rep.setPath(path);
			rep.setSource(nodeIndex);
			
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(path);
			
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
			
			pht_listNodes.remove(path);
			
			break;
		}
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
	 * La méthode qui lance une requête de recherche d'un chemin vers le serveur qui le gère.
	 * */
	
	private void lookupPath(BF bf, BF key, String path, long requestID, int pid) throws ErrorException
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		if (this.listPath.containsKey(requestID))
		{
			this.listPath.remove(requestID);
			this.listPath.put(requestID, path);
		}
		else
		{
			this.listPath.put(requestID, path);
		}
		
		Message rep = new Message();
		rep.setType(MessageType.LOOKUP_PATH);
		rep.setBF(bf);
		rep.setKey(key);
		rep.setPath(path);
		rep.setRequestID(requestID);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}

	/**
	 * Appellé lors qu'un nœud contient plus de {@link Config.gamma} filtres.
	 * */
	
	private void splitLocal(PHT_Node n, long requestID, int pid) throws ErrorException
	{		
		ControlerNw.config_log.addSplit(1);
		
		System.out.println(ControlerNw.config_log.getSplit() + " splits : " + n.getPath());
		
		DataStore data = n.getDataStore();
		ArrayList<BF> listKeys = data.getListBFs();
		
		n.setDataStore(null);
		
		this.splitData.clear();
		
		if (n.getPath().equals("/"))
		{
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate("0");
			int serverID1 = ControlerNw.config_log.getTranslate().translate("1");
			
			this.splitData.put("0", new ArrayList<BF>());
			this.splitData.put("1", new ArrayList<BF>());

			BF key_tmp0 = new BF("0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					this.splitData.get("0").add(bf_tmp);
				}
				else
				{
					this.splitData.get("1").add(bf_tmp);
				}
			}
			
			if (serverID0 == nodeIndex)
			{
				PHT_Node n0 = new PHT_Node("0");
				this.list.put("0", n0);
			}
			else
			{
				Message rep = new Message();
				rep.setType(MessageType.CREATE_NODE);
				rep.setPath("0");
				rep.setOption("0");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID0);
				rep.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
				
				this.listCreateNode[serverID0]++;
			}
			
			if (serverID1 == nodeIndex)
			{
				PHT_Node n1 = new PHT_Node("1");
				this.list.put("1", n1);
			}
			else // serverID1 == nodeIndex
			{
				Message rep = new Message();
				rep.setType(MessageType.CREATE_NODE);
				rep.setPath("1");
				rep.setOption("1");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID1);
				rep.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID1), rep, pid);

				this.listCreateNode[serverID1]++;
			}
			
			if (serverID0 == serverID1 && serverID0 == nodeIndex)
			{
				this.resolveProblem(requestID, pid);
			}
			else
			{
				this.prob = true;
			}
		}
		else // !n.getPath().equals("/")
		{
			String path = n.getPath();
			
			String path_tmp0 = this.skey(path + "0");
			String path_tmp1 = this.skey(path + "1");
			
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate(path_tmp0);
			int serverID1 = ControlerNw.config_log.getTranslate().translate(path_tmp1);
						
			this.splitData.put(path + "0", new ArrayList<BF>());
			this.splitData.put(path + "1", new ArrayList<BF>());
			
			BF key_tmp0 = new BF(path + "0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					this.splitData.get(path + "0").add(bf_tmp);
				}
				else
				{
					this.splitData.get(path + "1").add(bf_tmp);
				}
			}
			
			if (serverID0 == nodeIndex)
			{
				PHT_Node n0 = new PHT_Node(path + "0");
				
				if (this.list.containsKey(path_tmp0))
					this.list.remove(path_tmp0);
				
				this.list.put(path_tmp0, n0);
			}
			else
			{
				Message rep = new Message();
				rep.setType(MessageType.CREATE_NODE);
				rep.setPath(path_tmp0);
				rep.setOption(path + "0");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID0);
				rep.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
				
				this.listCreateNode[serverID0]++;
			}
			
			if (serverID1 != nodeIndex)
			{
				Message rep1 = new Message();
				rep1.setType(MessageType.CREATE_NODE);
				rep1.setPath(path_tmp1);
				rep1.setOption(path + "1");
				rep1.setSource(nodeIndex);
				rep1.setDestinataire(serverID1);
				rep1.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID1), rep1, pid);
	
				this.listCreateNode[serverID1]++;
			}
			else // serverID1 == nodeIndex
			{
				PHT_Node n1 = new PHT_Node(path + "1");
				
				if (this.list.containsKey(path_tmp1))
					this.list.remove(path_tmp1);
				
				this.list.put(path_tmp1, n1);
			}
			
			if (serverID0 == serverID1 && serverID0 == nodeIndex)
			{
				this.resolveProblem(requestID, pid);
			}
		}
	}

	private void resolveProblem(long requestID, int pid) throws ErrorException
	{
		PHT_Node splitOK = null;
		
		Enumeration<String> enumeration = this.splitData.keys();
		
		while (enumeration.hasMoreElements())
		{
			String path_tmp = enumeration.nextElement();
			String path = this.skey(path_tmp);
			
			PHT_Node n = this.list.get(path);
			
			ArrayList<BF> arrayList = this.splitData.get(path_tmp);
			
			for (int i = 0; i < arrayList.size(); i++)
			{
				n.insert(arrayList.get(i));
			}
			
			if (n.size() > Config.gamma)
				splitOK = n;
		}
		
		if (splitOK != null)
		{
			splitOK.setLeafNode(false);
			this.splitLocal(splitOK, requestID, pid);
		}
		else
		{
			Message tmp = this.messageWaitingForResponse.pollLast();
			
			Message rep = new Message();
			
			rep.setType(MessageType.valueOf(tmp.getType() + "_OK"));
			rep.setRequestID(tmp.getRequestID());
			rep.setSource(nodeIndex);
			rep.setDestinataire(tmp.getSource());
			
			t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);
		}
	}

	/**
	 * Retourne le rang d'un chemin.
	 * */
	
	private int calculRang(String path)
	{
		if (path == "/")
			return -1;
		
		return path.length() - 1;
	}
	
	/**
	 * Traite le message réponse de {@link lookupPath}.
	 * */
	
	private void treatLookupPath_OK(Message message, int pid) throws ErrorException
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		String path = (String) message.getOption();
		boolean isLeafNode = message.getIsLeafNode();
		long requestID = message.getRequestID();
		
		if (path.equals("/"))
		{
			if (isLeafNode)
			{
				insertPath(message, pid);
				return;
			}
			
			path = key.getFragment(0, 1).toString();
		}
		else // !n.getPath().equals("/")
		{				
			BF bf_tmp = new BF(path);
			int range = this.calculRang(path);
			
			if (key.equals(bf_tmp))
			{					
				if (isLeafNode)
				{
					insertPath(message, pid);
					return;
				}
				else // !n.isLeafNode()
				{
					path += key.getFragment(range + 1, 1);
				}
			}
			else // !key.equals(bf_tmp)
			{
				String s = new String();
				for (int i = 0; i <= range; i++)
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
		
		this.lookupPath(bf, key, path, requestID, pid);
	}

	/**
	 * Traite de message {@link lookupPath}.
	 * */
	
	private void treatLookupPath(Message message, int pid)
	{		
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		Message rep = new Message();
		
		rep.setType(MessageType.LOOKUP_PATH_OK);
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
		rep.setPath(path);
		rep.setIsLeafNode(n.isLeafNode());
		rep.setRequestID(message.getRequestID());
		rep.setOption(n.getPath());
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);		
	}

	/**
	 * L'initiateur lance la requête d'insertion.
	 * */
	
	@SuppressWarnings({ "static-access" })
	private void launchInsert(int pid) throws ErrorException
	{		
		if (nodeIndex == init && insertFIFO.isEmpty())
		{
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType(MessageType.OVERVIEW);
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}
		
		if (insert_OK)
		{			
			insert_OK = false;
			if (insertFIFO.isEmpty())
				return;
			
			String desc = insertFIFO.poll();
			BF bf = new BF(Config.sizeOfBF);
			bf.addAll(desc);
			
			BF key = bf.getKey(Config.sizeOfKey);
			int requestID = insertFIFO.size();
			
			System.out.println(requestID);
			System.out.println("Nombre de filtres ajoutés : " + ControlerNw.config_log.totalFilterAdded);
			
			this.lookupPath(bf, key, "/", requestID, pid);
		}
	}
	
	/**
	 * Initialise la liste des filtres pour l'insertion.
	 * */
	
	@SuppressWarnings("static-access")
	private void initInsertFIFO()
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(Config.fileWiki)))
		{
			int line = 0;
			while (true)
			{
				String s = new String();
				s = reader.readLine();
				
				if (s == null)
					break;
				
				String[] tmp = s.split(";");
				
				if (tmp.length >= 2 && tmp[1].length() > 2 )
				{
					insertFIFO.add(tmp[1]);
					line++;
				}
				System.out.println(line);
				if (line == ControlerNw.config_log.numberOfFiltersTest)
					break;
			}
			ControlerNw.config_log.numberOfFiltersCreated = line;
						
			reader.close();		
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Traite le message d'initialisation de l'insertion.
	 * */
	
	private void treatInsertInit(Message message, int pid) throws ErrorException
	{		
		init = nodeIndex;
			
		this.initInsertFIFO();
		
		this.launchInsert(pid);
	}
	
	/**
	 * Traite le message d'insertion.
	 * */
	
	private void treatInsert(Message message, int pid) throws ErrorException
	{
		if (this.insertFIFO.isEmpty())
		{
			this.insertFIFO.add((String) message.getData());
			
			if (insert_OK)
			{
				this.launchInsert(pid);
				return;
			}
		}
		else
		{
			this.insertFIFO.add((String) message.getData());
		}
	}
	
	/**
	 * Envoie le message {@code PUT} vers un serveur correspondant.
	 * */
	
	private void insertPath(Message message, int pid)
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		String path = message.getPath();
		long requestID = message.getRequestID();

		Message rep = new Message();
		
		rep.setType(MessageType.PUT);
		rep.setBF(bf);
		rep.setKey(key);
		rep.setPath(path);
		rep.setOption(message.getOption());
		rep.setRequestID(requestID);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		
		int[] tmp = new int[Network.size()];
		tmp[message.getSource()]++;
		
		this.listPUT.put(requestID, tmp);
	}
	
	/**
	 * Traite le message {@code PUT}
	 * */
	
	@SuppressWarnings("static-access")
	private void treatPUT(Message message, int pid) throws ErrorException
	{		
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		n.insert(message.getBF());
		
		if (n.size() > ControlerNw.config_log.gamma)
		{
			messageWaitingForResponse.add(message);

			n.setLeafNode(false);
			this.splitLocal(n, message.getRequestID(), pid);
		}
		else
		{			
			Message rep = new Message();
						
			rep.setType(MessageType.valueOf(message.getType() + "_OK"));
			rep.setPath(message.getPath());
			rep.setOption(message.getOption());
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setRequestID(message.getRequestID());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);				
		}
	}
	
	/**
	 * Teste si toutes les entrées d'une table de taille {@code size} sont égales à {@code 0}, sinon {@code false}.
	 * */
	
	private boolean testOK(int[] tab, int size)
	{
		for (int i = 0; i < size; i++)
		{
			if (tab[i] != 0)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Traite le message réponse de {@code PUT}.
	 * */
	
	private void treatPUT_OK(Message message, int pid) throws ErrorException
	{
		int[] tmp = (int[]) this.listPUT.get(message.getRequestID());
		
		if (tmp == null)
		{
			return;
		}
		tmp[message.getSource()]--;

		if (this.testOK(tmp, Network.size()))
		{
			insert_OK = true;
			this.launchInsert(pid);
		}		
	}
	
	/**
	 * Traite le message réponse de {@code createNode}.
	 * */
	
	private void treatCreateNode_OK(Message message, int pid) throws ErrorException
	{	
		if (message.getPath().equals("/"))
			return;
		
		this.listCreateNode[message.getSource()]--;
		
		if (this.testOK(this.listCreateNode, Network.size()))
		{
			if (!this.splitData.isEmpty())
			{
				Enumeration<String> enumeration = this.splitData.keys();

				while (enumeration.hasMoreElements())
				{
					String path_tmp = enumeration.nextElement();
					String path = this.skey(path_tmp);
					
					ControlerNw.config_log.getTranslate().setRange(Network.size());
					Integer serverID = ControlerNw.config_log.getTranslate().translate(path);
					
					ArrayList<BF> arrayList = this.splitData.get(path_tmp);

					if (serverID == nodeIndex)
					{
						PHT_Node n = this.list.get(path);
						
						for (int i = 0; i < arrayList.size(); i++)
						{
							n.insert(arrayList.get(i));
						}
						
						if (n.size() > Config.gamma)
							this.pathTest = path_tmp;						
					}
					else
					{						
						Message rep = new Message();
						rep.setType(MessageType.PUT_DATA);
						rep.setPath(path);
						rep.setData(arrayList);
						rep.setSource(nodeIndex);
						rep.setDestinataire(serverID);
						rep.setRequestID(message.getRequestID());
					
						t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
			
						this.listPUTData[serverID]++;
					}
				}
				
				this.splitData = new Hashtable<String, ArrayList<BF>>();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void treatPutData(Message message, int pid) throws ErrorException
	{		
		ArrayList<BF> arrayList = (ArrayList<BF>) message.getData();
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		for (int i = 0; i < arrayList.size(); i++)
		{
			n.insert(arrayList.get(i));
		}
		
		if (n.size() > Config.gamma)
		{
			this.messageWaitingForResponse.add(message);

			n.setLeafNode(false);
			this.splitLocal(n, message.getRequestID(), pid);
			return;
		}
		else
		{
			Message rep = new Message();
			
			rep.setType(MessageType.PUT_DATA_OK);
			rep.setPath(message.getPath());
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setRequestID(message.getRequestID());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		}
	}
	
	private void treatPutData_OK(Message message, int pid) throws ErrorException
	{
		if (this.prob)
		{
			this.listPUTData[message.getSource()]--;
	
			if (this.testOK(this.listPUTData, Network.size()))
			{
				if (this.pathTest != null && !this.pathTest.isEmpty())
				{
					PHT_Node n = this.list.get(this.skey(this.pathTest));
					
					n.setLeafNode(false);
					this.splitLocal(n, message.getRequestID(), pid);

					this.pathTest = new String();
				}
				else
				{
					Message tmp = this.messageWaitingForResponse.pollLast();
					
					Message rep = new Message();
					
					rep.setType(MessageType.valueOf(tmp.getType() + "_OK"));
					rep.setRequestID(tmp.getRequestID());
					rep.setSource(nodeIndex);
					rep.setDestinataire(tmp.getSource());
					
					t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);
					
					this.messageWaitingForResponse.remove(message.getRequestID());
				}
				
				this.prob = false;
			}
		}
		else
		{
			if (this.pathTest != null && !this.pathTest.isEmpty())
			{
				PHT_Node n = this.list.get(this.skey(this.pathTest));
				
				n.setLeafNode(false);
				this.splitLocal(n, message.getRequestID(), pid);

				this.pathTest = new String();
			}
			else
			{
				Message tmp = this.messageWaitingForResponse.pollLast();
				
				Message rep = new Message();
				
				rep.setType(MessageType.valueOf(tmp.getType() + "_OK"));
				rep.setRequestID(tmp.getRequestID());
				rep.setSource(nodeIndex);
				rep.setDestinataire(tmp.getSource());
				
				t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);
				
				this.messageWaitingForResponse.remove(message.getRequestID());
			}
		}
		
	}
	
	/**
	 * Traite le message {@code createNode}.
	 * */
	
	private void treatCreateNode(Message message, int pid) throws ErrorException
	{		
		String path = message.getPath();
		String realPath = (String) message.getOption();
		
		PHT_Node n = new PHT_Node(realPath);
		
		if (this.list.containsKey(path))
			this.list.remove(path);
		
		this.list.put(path, n);
		
		Message rep = new Message();
		rep.setType(MessageType.CREATE_NODE_OK);
		rep.setPath(path);
		rep.setOption(realPath);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setRequestID(message.getRequestID());

		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);	
	}
	
	/**
	 * Traite le message d'initialisation de la recherche.
	 * */
	
	private void treatSearchInit(Message message, int pid) throws ErrorException
	{
		try 
		{
			ReadFile rf = new ReadFile(Config.fileRequests);
		
			for (int i = 0; i < rf.size(); i++)
			{									
				BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(i));
			
				this.searchFIFO.add(bf);
				keywordsFIFO.add(rf.getDescription(i));
			}
			
			System.out.println("NOMBRE de requete = " + rf.size());
			
			this.launchSearch(pid);
			
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * Lance une recherche.
	 * */
	
	@SuppressWarnings("static-access")
	private void launchSearch(int pid) throws ErrorException
	{		
		if (search_OK)
		{
			search_OK = false;
			int experience = 0;
			int j = 0;
			while (!searchFIFO.isEmpty())
			{
				BF bf = searchFIFO.poll();
				String keywords = keywordsFIFO.poll();
				
				ControlerNw.config_log.getTranslate().setRange(Config.requestRange);
				long requestID = ControlerNw.config_log.getTranslate().translate(bf.toString());
				
				this.listRequests.put(requestID, bf);
				
				ControlerNw.config_log.listRequestID.add(requestID);
				ControlerNw.config_log.addNbFiltersRetrieved(requestID, 0);
				
				long time = Calendar.getInstance().getTimeInMillis();
				ArrayList<Long> array = new ArrayList<Long>();
				
				WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG_resultat + requestID + ".xml", true);
				wf.write("<request id=\"" + requestID + "\", nbkeywords=\"" + (experience + 5)
						+ "\", time=\"" + time +"\">\n");
				wf.write(this.baslise("keywords", keywords) + "\n");
				wf.write(this.baslise("requestBF", bf.toString()) + "\n");
				wf.write(this.baslise("requestKey", bf.getKey(Config.sizeOfKey).toString()) + "\n");
				wf.close();
				
				array.add(time);
				ControlerNw.config_log.getTimeGlobal().put(requestID, array);
								
				this.supersetSearch(bf, experience, requestID, pid);
				j++;
				
				if (j == 10)
				{
					j = 0;
					experience++;
				}
			}
			
			/*
			for (int i = 0; i < 10; i++)
			{
				BF bf = new BF(Config.sizeOfBF);
				
				for (j = 0; j < (i + 1)*10; j++)
				{
					bf.setBit(j, true);
				}
				
				System.out.println(bf);
				
				ControlerNw.config_log.getTranslate().setRange(Config.requestRange);
				long requestID = ControlerNw.config_log.getTranslate().translate(bf.toString());
				
				long time = Calendar.getInstance().getTimeInMillis();
				ArrayList<Long> array = new ArrayList<Long>();
				
				array.add(time);
				ControlerNw.config_log.getTimeGlobal().put(requestID, array);
								
				this.supersetSearch(bf, experience, requestID, pid);
			}
			*/
		}
	}
		
	private void supersetSearch(BF bf, int experience, long requestID, int pid) throws ErrorException
	{
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
		this.exploreSubtree(sbroot, experience, requestID, pid);
	}
	
	private void exploreSubtree(String sbroot, int exprerience, long requestID, int pid) throws ErrorException
	{
		String prefix = sbroot + "1";
		this.getStatus(this.skey(prefix.substring(1, prefix.length())), sbroot, exprerience, requestID, pid);
	}
	
	private void getStatus(String path, String sbroot, int exprerience, long requestID, int pid)
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_STATUS);
		rep.setRequestID(requestID);
		rep.setPath(path);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		rep.setOption(sbroot);
		rep.setOption2(exprerience);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}
	
	private void treatGetStatus(Message message, int pid)
	{
		PHT_Node n = this.list.get(message.getPath());
		
		PHT_Node n_tmp = null;
		if (n != null)
		{
			n_tmp = new PHT_Node(n.getPath());
			n_tmp.setLeafNode(n.isLeafNode());
			n_tmp.setDataStore(null);
		}
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_STATUS_OK);
		rep.setData(n_tmp);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setOption(message.getOption());
		rep.setRequestID(message.getRequestID());
		rep.setOption2(message.getOption2());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatGetStatus_OK(Message message, int pid) throws ErrorException
	{
		ControlerNw.config_log.addGetStatus(message.getRequestID(), 1);
		
		BF bf = this.listRequests.get(message.getRequestID());
		BF q = bf.getKey(Config.sizeOfKey);
		int exprerience = (int) message.getOption2();
		long requestID = message.getRequestID();
		String sbroot = (String) message.getOption();
		
		String p = sbroot + "1";
				
		PHT_Node n = (PHT_Node) message.getData();
		
		if (n == null)
		{
			String path = p.substring(0, p.length() - 1);
			String tmp;
			if (path.equals("/"))
			{
				tmp = path;
			}
			else
			{
				tmp = this.skey(path.substring(1, path.length()));
			}
			
			this.getStoredBF(tmp, exprerience, message.getRequestID(), pid);
		}
		else
		{
			String label = "/" + n.getPath();
			int toMatchLen = this.lastBitSet(q) + 1;
			if (label.length() > (toMatchLen + 1))
			{
				label = label.substring(0, toMatchLen + 1);
				this.collectLeaves(label, exprerience, requestID, pid);
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
				
				this.getStoredBF(tmp, exprerience, requestID, pid);				
			}
			
			int minLen = sbroot.length();

			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				BF qprefix = q.getSubFilter(0, bs_label.length() - 1);
				bs_label = bs_label.substring(0, bs_label.length() - 1) + "0";
				if (qprefix.in(new BF(bs_label)))
				{
					this.exploreSubtree("/" + bs_label, exprerience, requestID, pid);
				}
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
	}
	
	private void collectLeaves(String sbroot, int exprerience, long requestID, int pid) throws ErrorException
	{
		String prefix = sbroot + "1";
		this.getCollectLeaves(this.skey(prefix.substring(1, prefix.length())), sbroot, exprerience, requestID, pid);
	}
	
	private void getCollectLeaves(String path, String sbroot, int exprerience, long requestID, int pid)
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_COLLECT_LEAVES);
		rep.setRequestID(requestID);
		rep.setPath(path);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		rep.setOption(sbroot);
		rep.setOption2(exprerience);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}
	
	private void treatGetCollectLeaves(Message message, int pid)
	{
		PHT_Node n = this.list.get(message.getPath());
		
		PHT_Node n_tmp = null;
		if (n != null)
		{
			n_tmp = new PHT_Node(n.getPath());
			n_tmp.setLeafNode(n.isLeafNode());
			n_tmp.setDataStore(null);
		}
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_COLLECT_LEAVES_OK);
		rep.setData(n_tmp);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setOption(message.getOption());
		rep.setRequestID(message.getRequestID());
		rep.setOption2(message.getOption2());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatGetCollectLeaves_OK(Message message, int pid) throws ErrorException
	{
		String sbroot = (String) message.getOption();
		
		int experience = (int) message.getOption2();
		long requestID = message.getRequestID();
		
		String p = sbroot + "1";
		
		PHT_Node n = (PHT_Node) message.getData();
		
		if (n == null)
		{
			String path = p.substring(0, p.length() - 1);
			String tmp;
			if (path.equals("/"))
			{
				tmp = path;
			}
			else
			{
				tmp = this.skey(path.substring(1, path.length()));
			}
			
			this.getStoredBF(tmp, experience, requestID, pid);			
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
			
			this.getStoredBF(tmp, experience, requestID, pid);			
			
			int minLen = sbroot.length();
			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				this.collectLeaves("/" + bs_label, experience, requestID, pid);
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
	}
	
	private void getStoredBF(String path, int exprerience, long requestID, int pid)
	{
//		long time = Calendar.getInstance().getTimeInMillis();
//		((ArrayList<Long>) ControlerNw.config_log.getTimeGlobal().get(requestID)).add(time);
		
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_STORED_BF);
		rep.setRequestID(requestID);
		rep.setPath(path);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		rep.setOption2(exprerience);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}
	
	private void treatGetStoredBF(Message message, int pid)
	{
		PHT_Node n = this.list.get(message.getPath());
		
		Message rep = new Message();
		
		rep.setType(MessageType.GET_STORED_BF_OK);
		rep.setPath(message.getPath());
		rep.setData(n);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setRequestID(message.getRequestID());
		rep.setOption2(message.getOption2());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatGetStoredBF_OK(Message message, int pid)
	{
		PHT_Node n = (PHT_Node) message.getData();
		
		ControlerNw.config_log.addRetrieves(message.getRequestID(), 1);
		
		if (n == null)
		{
		//	ArrayList<Long> arrayList = ((ArrayList<Long>) ControlerNw.config_log.getTimeGlobal().get(message.getRequestID()));
		//	arrayList.remove(arrayList.size() - 1);
			return;
		}
		BF bf = this.listRequests.get(message.getRequestID());
		
		ArrayList<BF> storedBF = n.getDataStore().getListBFs();
		
		this.retrieveSuperset(storedBF, bf, (int) message.getOption2(), message.getRequestID(), n.getPath());
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	private void retrieveSuperset(ArrayList<BF> storedBF, BF bf, int experience, long requestID, String path)
	{
		//*******************LOG**********************	
		Hashtable<Long, Object> hashtable = (Hashtable<Long, Object>) 
				ControlerNw.config_log.getListAnswer(requestID);
									
		if (hashtable == null)
		{
			hashtable = new Hashtable<Long, Object>();
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(path);
			hashtable.put(requestID, arrayList);
										
			ControlerNw.config_log.putListAnswer(requestID, hashtable);
		}
		else
		{
			((ArrayList<String>) hashtable.get(requestID)).add(path);
		}		
		//*******************LOG**********************	
		
		ControlerNw.config_log.addNbFiltersRetrieved(requestID, storedBF.size());
		
		ControlerNw.config_log.ObserverNw_tmp_OK = true;
		
		for (int i = 0; i < storedBF.size(); i++)
		{
			BF bf_tmp = storedBF.get(i);
			if (bf.in(bf_tmp))
			{
				ArrayList<Long> arrayList = ((ArrayList<Long>) ControlerNw.config_log.getTimeGlobal().get(requestID));
				long time = Calendar.getInstance().getTimeInMillis();
				arrayList.add(time);
				
				time = time - arrayList.get(0);
				
				WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG_resultat + requestID + ".xml", true);
				String s = this.baslise("path", path) + "\n";
				s += this.baslise("time", time+"") + "\n";
				s += this.baslise("retrieveAt", ControlerNw.config_log.getRetrieves(requestID)+"") + "\n";
				s += this.baslise("filter", bf_tmp.toString()) + "\n";
				s += this.baslise("keyFilter", bf_tmp.getKey(Config.sizeOfKey).toString()) + "\n";
				wf.write(this.baslise("resultat", s) +"\n");
				wf.close();				
			}
		}
	}

	private void treatOverview(Message message, int pid) throws ErrorException
	{	
		ControlerNw.config_log.setNodePerServer(nodeIndex, list.size());		

		Enumeration<String> enumeration = this.list.keys();
				
		while (enumeration.hasMoreElements())
		{
			String s_tmp = enumeration.nextElement();
			PHT_Node n = this.list.get(s_tmp); 
			if (n.isLeafNode())
			{
				ControlerNw.config_log.getFilterPerNode().put(n.getPath(), n.size());
			}
									
			if (!ControlerNw.config_log.getIndexHeight().contains(n.getRang()))
				ControlerNw.config_log.getIndexHeight().put(n.getRang(), n.getPath());
			
			if (!ControlerNw.config_log.getRealIndexHeight().contains(s_tmp.length() - 1))
				ControlerNw.config_log.getRealIndexHeight().put(s_tmp.length() - 1, s_tmp);
		}
			
		
		
		Message rep = new Message();
		rep.setType(MessageType.OVERVIEW_OK);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		
	}
	
	@SuppressWarnings("static-access")
	private void treatOverview_OK(Message message, int pid)
	{
		if (recu_OK)
			return;
		
		if (recu[message.getSource()] != 0 && recu_OK)
		{
			System.out.println(message.getSource() + " != 0");
		}
		
		recu[message.getSource()] = 1;
		for (int i = 0; i < Network.size(); i++)
		{
			if (recu[i] == 0)
				return;
		}
		
		ControlerNw.config_log.setConfig_OK(false);
				
		WriteFile wf1 = new WriteFile(Config.peerSimLOG + "_nodePerServer.csv", true);
		
		wf1.write("nodeIndex;nombre de noeuds\n");
		
		for (int i = 0; i < Network.size(); i++)
		{
			if (ControlerNw.config_log.getNodePerServer(i) == 0)
				continue;
			
			wf1.write(i + ";" + ControlerNw.config_log.getNodePerServer(i) + "\n");
		}
		
		wf1.close();

		wf1 = new WriteFile(Config.peerSimLOG + "_filterPerNode.csv", true);
		
		wf1.write("index;nombre de filtres;noeud\n");
		
		Enumeration<String> enumeration4 = ControlerNw.config_log.getFilterPerNode().keys();
		
		int i = 0;
		
		WriteFile wf2 = new WriteFile(Config.peerSimLOG + "_node.csv", true);
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate("/");
		
		wf2.write("index;noeud;nodeIndex\n");
		wf2.write("0;/;" + serverID + "\n");
		wf2.close();
		wf1.write(i++ + ";"+ 0 + ";" + "/" +  "\n");
		
		while (enumeration4.hasMoreElements())
		{
			String s_tmp = enumeration4.nextElement();
			
			wf2 = new WriteFile(Config.peerSimLOG + "_node.csv", true);
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			wf2.write(i + ";" + s_tmp + ";" + ControlerNw.config_log.getTranslate().translate(s_tmp) + "\n");
			wf2.close();

			wf1.write(i + ";" 
					+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + ";" + s_tmp +  "\n");
			
			i++;
		}
		
		wf1.close();
		
		recu_OK = true;
		
		//*******************
		WriteFile wf = new WriteFile(Config.peerSimLOG+"_indexHeight.csv", false);
		Enumeration<Integer> enumeration = ControlerNw.config_log.getIndexHeight().keys();
		wf.write("NumberOfMessages;NumberOfSplits;NumberOfFiltersCreated;NumberOfFiltersAdded;SizeOfFilter;SizeOfKey"
				+ "Gamma;noeudSansSkey;longueurSansSkey;noeudAvecSkey;longueurAvecSkey\n");
		wf.write((ControlerNw.config_log.numberOfMessages - Network.size()*2) + ";");
		wf.write(ControlerNw.config_log.getSplit() + ";");
		wf.write(ControlerNw.config_log.numberOfFiltersCreated + ";");
		wf.write(ControlerNw.config_log.totalFilterAdded + ";"); //j + "\n\n");
		wf.write(ControlerNw.config_log.sizeOfBF + ";");
		wf.write(ControlerNw.config_log.sizeOfKey + ";");
		wf.write(ControlerNw.config_log.gamma + ";");

		int max = 0;
		while (enumeration.hasMoreElements())
		{
			i = enumeration.nextElement();
			if (i > max)
				max = i;		
		}
		
		wf.write(ControlerNw.config_log.getIndexHeight().get(max) + ";" + max + ";");

		
		enumeration = ControlerNw.config_log.getRealIndexHeight().keys();
		
		max = 0;
		while (enumeration.hasMoreElements())
		{
			i = enumeration.nextElement();
			if (i > max)
				max = i;
		}
		
		wf.write(ControlerNw.config_log.getRealIndexHeight().get(max) + ";" + max + "\n");
		wf.close();		
		//*******************
		
		Config.ObserverNw_OK = true;
	}
	
	private String baslise(String balise, String s)
	{
		return "<" + balise + ">" 
					+ s 
				+ "</" + balise + ">"; 
	}
	
}











