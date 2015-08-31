package peerSimTest_v4;

import java.io.BufferedReader;
import java.io.File;
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

public class SystemIndexProtocol implements EDProtocol{

	public static int init;
	
	private static final String PAR_TRANSPORT = "transport";
	private String prefix;
	private int tid;
	private int nodeIndex;
	private Transport t;
	private int id;
	
	private Hashtable<String, PHT_Node> list = new Hashtable<String, PHT_Node>();
	private Hashtable<Long, String> listPath = new Hashtable<Long, String>();
	private ArrayDeque<BF> insertFIFO = new ArrayDeque<BF>();
	private ArrayDeque<BF> searchFIFO = new ArrayDeque<BF>();
	
	private Hashtable<Integer, ArrayList<Message>> splitData = new Hashtable<Integer, ArrayList<Message>>();
	
	private int[] listCreateNode = new int[Network.size()];

	private Hashtable<Long, Object> listPUT = new Hashtable<Long, Object>();
			
	private Hashtable<Long ,Message> messageWaitingForResponse = new Hashtable<Long, Message>();
		
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	private boolean insert_OK = true;
	private boolean search_OK = true;
	
	private int numberOfFiltersCreated;
	
	
	private Hashtable<String, PHT_Node_Central> pht_central;
	

	public SystemIndexProtocol(String prefix)
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
		SystemIndexProtocol s = new SystemIndexProtocol(prefix);
		s.tid = this.tid;
		s.prefix = this.prefix;
		s.nodeIndex = this.nodeIndex;
		s.id = this.id;
		s.recu = this.recu;
		return s;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// TODO Auto-generated method stub
		
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
		case "createIndex": //createIndex, Name, sourceID, descID, option
			break;
			
		case "removeIndex": //removeIndex, Name
			break;
			
		case "insertInit" :
			try
			{
				treatInsertInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case "searchInit" :
			try
			{
				treatSearchInit(message, pid);
			}
			catch (ErrorException e3)
			{
				e3.printStackTrace();
			}
			break;
			
		case "PUT": // PUT, indexName, BF, path
			try
			{
				treatPUT(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "PUT_OK" :
			try
			{
				treatPUT_OK(message, pid);
			} 
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "createNode_OK" :
			try
			{
				treatCreateNode_OK(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "insert" :
			try
			{
				treatInsert(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "getLabel": //GET, indexName, path
			treatGetLabel(message, pid);
			break;
			
		case "getLabel_OK":
			try
			{
				treatGetLabel_OK(message, pid);
			}
			catch (ErrorException e2)
			{
				e2.printStackTrace();
			}
			break;
			
		case "lookupPath_OK" :
			try 
			{
				treatLookupPath_OK(message, pid);
			} 
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
		case "lookupPath" :
			treatLookupPath(message, pid);
			break;
			
		case "createNode" :
			try
			{
				treatCreateNode(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
				
		case "getStoredBF" :
			treatGetStoredBF(message, pid);
			break;
			
		case "getStoredBF_OK" :
			treatGetStoredBF_OK(message, pid);
			break;
			
		case "getCollectLeaves" :
			treatGetCollectLeaves(message, pid);
			break;
			
		case "getCollectLeaves_OK" :
			try
			{
				treatGetCollectLeaves_OK(message, pid);
			} catch (ErrorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
			
			
			
		case "simulation" :
			try
			{
				treatSimulation(message, pid);
			}
			catch (ErrorException e1)
			{
				e1.printStackTrace();
			}
			break;
			
		case "createSimulation":
			treatCreateSimulation(message, pid);
			break;
			
		case "createSimulation_OK":
			treatCreateSimulation_OK(message, pid);
			break;
			
			
			
			
			
		case "overview": // nœud 0 balance la requete vers tous les autres nœuds
			try
			{
				treatOverview(message, pid);
			}
			catch (ErrorException e)
			{
				e.printStackTrace();
			}
			break;
			
		case "overview_OK": // tous les nœuds répond au nœud 0
			treatOverview_OK(message, pid);
			break;
			
		default : 
			break;
		}
	}

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
		rep.setType("createSimulation_OK");
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatCreateSimulation_OK(Message message, int pid)
	{
		if (!pht_central.isEmpty())
		{
			Enumeration<String> enumeration = pht_central.keys();
			
			while (enumeration.hasMoreElements())
			{
				String path = enumeration.nextElement();
				
				Message rep = new Message();
				rep.setType("createSimulation");
				rep.setData(pht_central.get(path));
				rep.setPath(path);
				rep.setSource(nodeIndex);
				
				ControlerNw.config_log.getTranslate().setRange(Network.size());
				int serverID = ControlerNw.config_log.getTranslate().translate(path);
				
				rep.setDestinataire(serverID);
				
				t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
				
				pht_central.remove(path);
				
				break;
			}
		}
		else if (nodeIndex == init)
		{
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}	
	}

	private void treatSimulation(Message message, int pid) throws ErrorException
	{
		
		init = nodeIndex;
		
		PHT_Central pht= new PHT_Central("dcs");

		int line = 0;
		int k = 0;
		
		try(BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60")))
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
			//	if (line == 160000)
				//	break;
			}
			reader.close();
			
			System.out.println("Fini de lecture " + k + " lignes.");
			System.out.println("Nombre de filtres réels : " + line + " filtres.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		pht_central = pht.getListNodes();
		
		Enumeration<String> enumeration = pht_central.keys();
		
		while (enumeration.hasMoreElements())
		{
			String path = enumeration.nextElement();
			
			Message rep = new Message();
			rep.setType("createSimulation");
			rep.setData(pht_central.get(path));
			rep.setPath(path);
			rep.setSource(nodeIndex);
			
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(path);
			
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
			
			pht_central.remove(path);
			
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
		rep.setType("lookupPath");
		rep.setBF(bf);
		rep.setKey(key);
		rep.setPath(path);
		rep.setRequestID(requestID);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}

	private void splitLocal(PHT_Node n, long requestID, int pid) throws ErrorException
	{		
		DataStore data = n.getDataStore();
		ArrayList<BF> listKeys = data.getListBFs();
		
		n.setDataStore(null);
		
		if (n.getPath().equals("/"))
		{
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			int serverID0 = ControlerNw.config_log.getTranslate().translate("0");
			int serverID1 = ControlerNw.config_log.getTranslate().translate("1");
			
			BF key_tmp0 = new BF("0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					Message rep = new Message();
					rep.setType("insert");
					rep.setBF(bf_tmp);
					rep.setSource(nodeIndex);
					
					if (this.splitData.containsKey(serverID0))
					{
						this.splitData.get(serverID0).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID0, arrayList);
					}
				}
				else
				{
					Message rep = new Message();
					rep.setType("insert");
					rep.setBF(bf_tmp);
					rep.setSource(nodeIndex);

					if (this.splitData.containsKey(serverID1))
					{
						this.splitData.get(serverID1).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID1, arrayList);
					}
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
				rep.setType("createNode");
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
				rep.setType("createNode");
				rep.setPath("1");
				rep.setOption("1");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID1);
				rep.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID1), rep, pid);
				
				this.listCreateNode[serverID1]++;
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
						
			BF key_tmp0 = new BF(path + "0");
			
			for (int i = 0; i < listKeys.size(); i++)
			{
				BF bf_tmp = listKeys.get(i);
				BF key = bf_tmp.getKey(Config.sizeOfKey);
				
				if (key.equals(key_tmp0))
				{
					Message rep = new Message();
					rep.setType("insert");
					rep.setBF(bf_tmp);
					rep.setSource(nodeIndex);

					if (this.splitData.containsKey(serverID0))
					{
						this.splitData.get(serverID0).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID0, arrayList);
					}
				}
				else
				{
					Message rep = new Message();
					rep.setType("insert");
					rep.setBF(bf_tmp);
					rep.setSource(nodeIndex);

					if (this.splitData.containsKey(serverID1))
					{
						this.splitData.get(serverID1).add(rep);
					}
					else
					{
						ArrayList<Message> arrayList = new ArrayList<Message>();
						arrayList.add(rep);
						this.splitData.put(serverID1, arrayList);
					}
				}
			}
			
			if (serverID0 == nodeIndex)
			{
				PHT_Node n0 = new PHT_Node(path + "0");
				
				if (this.list.containsKey(path_tmp0))
					this.list.remove(path_tmp0);
				
				this.list.put(path_tmp0, n0);
				
				Message rep1 = new Message();
				rep1.setType("createNode");
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
				
				Message rep = new Message();
				rep.setType("createNode");
				rep.setPath(path_tmp0);
				rep.setOption(path + "0");
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID0);
				rep.setRequestID(requestID);

				t.send(Network.get(nodeIndex), Network.get(serverID0), rep, pid);
				
				this.listCreateNode[serverID0]++;
			}
		}
	}

	private int calculRang(String path)
	{
		if (path == "/")
			return -1;
		
		return path.length() - 1;
	}
	
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

	private void treatLookupPath(Message message, int pid)
	{		
	/*	@SuppressWarnings("static-access")
		WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG + "_tmp", true);
		wf.write("requestID = " + message.getRequestID() + "\n");
		wf.close();
		*/
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		Message rep = new Message();
		
		rep.setType("lookupPath_OK");
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

	@SuppressWarnings("static-access")
	private void launchInsert(int pid) throws ErrorException
	{		
		if (nodeIndex == init && insertFIFO.isEmpty())
		{
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
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
			
			BF bf = insertFIFO.poll();
			BF key = bf.getKey(Config.sizeOfKey);
			int requestID = insertFIFO.size();
			
			System.out.println(requestID);
			System.out.println("Nombre de filtres ajoutés : " + ControlerNw.config_log.totalFilterAdded);
			
			this.lookupPath(bf, key, "/", requestID, pid);
		}
	}
	
	private void initInsertFIFO(Message message)
	{
		try(BufferedReader reader = new BufferedReader(new FileReader("/Users/dcs/vrac/test/wikiDocs<60")))
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
					BF bf_tmp = new BF(Config.sizeOfBF);
					bf_tmp.addAll(tmp[1]);

					insertFIFO.add(bf_tmp);
					line++;
				}
				
				System.out.println(line);
				if (line == (int)message.getData())
					break;
			}
			reader.close();
			System.out.println("Fini de lecture " + line + " lignes");
			
			long time = System.currentTimeMillis();
			
			System.out.println("Désérialisation");
	//		Serializer serializer = new Serializer();
	//		serializer.writeObject(this.insertFIFO, "/Users/dcs/vrac/test/fifo_" + ControlerNw.config_log.version);
			
			System.out.println("Fin de désérialisation " + (System.currentTimeMillis() - time) + " ms");			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "static-access", "unchecked" })
	private void treatInsertInit(Message message, int pid) throws ErrorException
	{
		System.out.println("Lecture n°1");
		
		init = nodeIndex;
	
		if (!message.getData().getClass().getName().contains("String"))
		{
			this.initInsertFIFO(message);
		}
		else
		{
			long time = System.currentTimeMillis();

			System.out.println("Désérialisation");
			Serializer serializer = new Serializer();
			this.insertFIFO = (ArrayDeque<BF>) serializer.readObject("/Users/dcs/vrac/test/fifo_" + ControlerNw.config_log.version);
			
			System.out.println("Fin de désérialisation " + (System.currentTimeMillis() - time) + " ms");
		}
		
		numberOfFiltersCreated = insertFIFO.size();
		this.launchInsert(pid);
	}
	
	private void treatInsert(Message message, int pid) throws ErrorException
	{
		if (this.insertFIFO.isEmpty())
		{
			this.insertFIFO.add(message.getBF());
			
			if (insert_OK)
			{
				this.launchInsert(pid);
				return;
			}
		}
		else
		{
			this.insertFIFO.add(message.getBF());
		}
	}
	
	private void insertPath(Message message, int pid)
	{
		BF bf = message.getBF();
		BF key = message.getKey();
		String path = message.getPath();
		long requestID = message.getRequestID();

		Message rep = new Message();
		
		rep.setType("PUT");
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
	 * Traiter le message de type "PUT".
	 * 
	 * @param message
	 *  <li> indexName
	 *  <li> path
	 *  <li> BF
	 *  
	 *  @author dcs
	 * @throws ErrorException 
	 * */
	
	@SuppressWarnings("static-access")
	private void treatPUT(Message message, int pid) throws ErrorException
	{		
		String path = message.getPath();
		
		PHT_Node n = this.list.get(path);
		
		n.insert(message.getBF());
		
		if (n.size() > ControlerNw.config_log.gamma)
		{
			messageWaitingForResponse.put(message.getRequestID(), message);

			n.setLeafNode(false);
			this.splitLocal(n, message.getRequestID(), pid);
		}
		else
		{			
			Message rep = new Message();
			
			rep.setType(message.getType() + "_OK");
			rep.setPath(message.getPath());
			rep.setOption(message.getOption());
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setRequestID(message.getRequestID());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);				
		}
	}
	
	private boolean testOK(int[] tab, int size)
	{
		for (int i = 0; i < size; i++)
		{
			if (tab[i] != 0)
				return false;
		}
		
		return true;
	}
	
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
	
	private void treatCreateNode_OK(Message message, int pid) throws ErrorException
	{		
		this.listCreateNode[message.getSource()]--;
		
		if (this.testOK(this.listCreateNode, Network.size()))
		{
			if (!this.splitData.isEmpty())
			{
				Enumeration<Integer> enumeration = this.splitData.keys();
								
				Message tmp = this.messageWaitingForResponse.get(message.getRequestID());

				while (enumeration.hasMoreElements())
				{
					Integer serverID = enumeration.nextElement();
					ArrayList<Message> arrayList = this.splitData.get(serverID);
										
					for (int i = 0; i < arrayList.size(); i++)
					{
						Message rep = arrayList.get(i);
						rep.setDestinataire(tmp.getSource());
					
						t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);						
					}
				}
				
				Message rep = new Message();
				
				rep.setType(tmp.getType() + "_OK");
				rep.setPath(tmp.getPath());
				rep.setOption(tmp.getOption());
				rep.setSource(nodeIndex);
				rep.setDestinataire(tmp.getSource());
				rep.setRequestID(tmp.getRequestID());
				
				t.send(Network.get(nodeIndex), Network.get(tmp.getSource()), rep, pid);	
				
				this.messageWaitingForResponse.remove(message.getRequestID());			
				
				this.splitData = new Hashtable<Integer, ArrayList<Message>>();
			}
		}
	}
	
	private void treatCreateNode(Message message, int pid) throws ErrorException
	{		
		String path = message.getPath();
		String realPath = (String) message.getOption();
		
		PHT_Node n = new PHT_Node(realPath);
		
		if (this.list.containsKey(path))
			this.list.remove(path);
		
		this.list.put(path, n);
		
		Message rep = new Message();
		rep.setType("createNode_OK");
		rep.setPath(path);
		rep.setOption(realPath);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setRequestID(message.getRequestID());

		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);	
	}
	
	private void treatSearchInit(Message message, int pid) throws ErrorException
	{
		try 
		{
			ReadFile rf = new ReadFile("/Users/dcs/vrac/test/wikiDocs<60_500_request");
		
			for (int i = 0; i < rf.size(); i++)
			{									
				BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(i));
			
				this.searchFIFO.add(bf);
			}
			
			System.out.println("NOMBRE de requete = " + rf.size());
			
			this.launchSearch(pid);
			
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}	
		
	}
	
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
				ControlerNw.config_log.getTranslate().setRange(Config.requestRange);
				long requestID = ControlerNw.config_log.getTranslate().translate(bf.toString());
				
				long time = Calendar.getInstance().getTimeInMillis();
				ArrayList<Long> array = new ArrayList<Long>();
				
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
		this.exploreSubtree(sbroot, q, bf, experience, requestID, pid);
	}

	private void exploreSubtree(String sbroot, BF q, BF bf, int exprerience, long requestID, int pid) throws ErrorException
	{
		String prefix = sbroot + "1";
		this.getLabel(this.skey(prefix.substring(1, prefix.length())), sbroot, bf, q, exprerience, requestID, pid);
	}
	
	private void getLabel(String path, String sbroot, BF bf, BF key, int exprerience, long requestID, int pid)
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType("getLabel");
		rep.setRequestID(requestID);
		rep.setBF(bf);
		rep.setKey(key);
		rep.setPath(path);
		rep.setSource(nodeIndex);
		rep.setDestinataire(serverID);
		rep.setOption(sbroot);
		rep.setOption2(exprerience);
		
		t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
	}
	
	private void treatGetLabel(Message message, int pid)
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
		
		rep.setType("getLabel_OK");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
		rep.setData(n_tmp);
		rep.setSource(nodeIndex);
		rep.setDestinataire(message.getSource());
		rep.setOption(message.getOption());
		rep.setRequestID(message.getRequestID());
		rep.setOption2(message.getOption2());
		
		t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
	}
	
	private void treatGetLabel_OK(Message message, int pid) throws ErrorException
	{
		BF bf = message.getBF();
		BF q = message.getKey();
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
			
			this.getStoredBF(tmp, bf, q, exprerience, message.getRequestID(), pid);
		}
		else
		{
			String label = "/" + n.getPath();
			int toMatchLen = this.lastBitSet(q) + 1;
			if (label.length() > (toMatchLen + 1))
			{
				label = label.substring(0, toMatchLen + 1);
				this.collectLeaves(label, bf, q, exprerience, requestID, pid);
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
				
				this.getStoredBF(tmp, bf, q, exprerience, requestID, pid);				
			}
			
			int minLen = sbroot.length();
			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				BF qprefix = q.getSubFilter(0, bs_label.length() - 1);
				bs_label = bs_label.substring(0, bs_label.length() - 1) + "0";
				if (qprefix.in(new BF(bs_label)))
				{
					this.exploreSubtree("/" + bs_label, q, bf, exprerience, requestID, pid);
				}
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
	}
	
	private void collectLeaves(String sbroot, BF bf, BF key, int exprerience, long requestID, int pid) throws ErrorException
	{
		String prefix = sbroot + "1";
		this.getCollectLeaves(this.skey(prefix.substring(1, prefix.length())), sbroot, bf, key, exprerience, requestID, pid);
	}
	
	private void getCollectLeaves(String path, String sbroot, BF bf, BF key, int exprerience, long requestID, int pid)
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType("getCollectLeaves");
		rep.setRequestID(requestID);
		rep.setBF(bf);
		rep.setKey(key);
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
		
		rep.setType("getCollectLeaves_OK");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
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
		BF bf = message.getBF();
		BF key = message.getKey();
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
			
			this.getStoredBF(tmp, bf, key, experience, requestID, pid);			
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
			
			this.getStoredBF(tmp, bf, key, experience, requestID, pid);			
			
			int minLen = sbroot.length();
			String bs_label = label.substring(1, label.length());
			while (bs_label.length() >= minLen)
			{
				this.collectLeaves("/" + bs_label, bf, key, experience, requestID, pid);
				bs_label = bs_label.substring(0, bs_label.length() - 1);
			}
		}
	}


	private void getStoredBF(String path, BF bf, BF key, int exprerience, long requestID, int pid)
	{
		ControlerNw.config_log.getTranslate().setRange(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		Message rep = new Message();
		
		rep.setType("getStoredBF");
		rep.setBF(bf);
		rep.setKey(key);
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
		
		rep.setType("getStoredBF_OK");
		rep.setBF(message.getBF());
		rep.setKey(message.getKey());
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
		
		if (n == null)
			return;
		
		BF bf = message.getBF();
		
		ArrayList<BF> storedBF = n.getDataStore().getListBFs();
		
		this.retrieveSuperset(storedBF, bf, (int) message.getOption2(), message.getRequestID(), n.getPath());
	}
	
	@SuppressWarnings({ "unchecked", "static-access" })
	private void retrieveSuperset(ArrayList<BF> storedBF, BF bf, int exprerience, long requestID, String path)
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

		String s = ControlerNw.config_log.peerSimLOG_resultat + exprerience + "_resultat_log_" + requestID;
		
		for (int i = 0; i < storedBF.size(); i++)
		{
			BF bf_tmp = storedBF.get(i);
			if (bf.in(bf_tmp))
			{
				File fs = new File(s);
				if (!fs.exists())
				{
					
					long time = Calendar.getInstance().getTimeInMillis();
					((ArrayList<Long>) ControlerNw.config_log.getTimeGlobal().get(requestID)).add(time);
					
					WriteFile wf1 = new WriteFile(s, true);
					wf1.write("Requete : " + bf + "\n");
					wf1.write("Key     : " + bf.getKey(Config.sizeOfKey) + "\n\n");
					wf1.write(path + " :\n\n");
					wf1.write(bf_tmp + "\n");
					wf1.close();
					
					ArrayList<Long> arrayList = ((ArrayList<Long>) ControlerNw.config_log.getTimeGlobal().get(requestID));
					time = arrayList.get(arrayList.size() - 1) - arrayList.get(arrayList.size() -2);
					
					wf1 = new WriteFile(ControlerNw.config_log.peerSimLOG_resultat + exprerience + "_time_" + requestID, true);
					wf1.write( ((ArrayList<String>) hashtable.get(requestID)).size() + " retrieve(s) (+" + time + "ms)\n");
					wf1.close();
					
				}	
				else
				{
					WriteFile wf1 = new WriteFile(s, true);
					wf1.write(bf_tmp + "\n");
					wf1.close();
					
					WriteFile wf = new WriteFile(ControlerNw.config_log.peerSimLOG_resultat + exprerience + "_path_" + requestID, true);
					wf.write(path + "\n");
					wf.close();
				}
			}
		}
	}

	/**
	 * Capturer l'état du système.
	 * 
	 * @author dcs
	 * @throws ErrorException 
	 * */
		
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
		rep.setType("overview_OK");
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
		
		Hashtable<Integer, Integer> tab = new Hashtable<Integer, Integer>();
		
		WriteFile wf1 = new WriteFile(Config.peerSimLOG + "_nodePerServer", true);
		
		int j = 0;
		int k = 0;
		for (int i = 0; i < Network.size(); i++)
		{
			if (ControlerNw.config_log.getNodePerServer(i) == 0)
				continue;
			
			j += ControlerNw.config_log.getNodePerServer(i);

			if (tab.containsKey(ControlerNw.config_log.getNodePerServer(i)))
			{
				int p = tab.get(ControlerNw.config_log.getNodePerServer(i)) + 1;
				tab.remove(ControlerNw.config_log.getNodePerServer(i));
				tab.put(ControlerNw.config_log.getNodePerServer(i), p);
			}
			else
			{
				tab.put(ControlerNw.config_log.getNodePerServer(i), 1);
			}
			
			if (i < 10)
			{
				wf1.write(i + "       " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			else if (i < 100)
			{ 
				wf1.write(i + "      " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			else
			{					
				wf1.write(i + "     " + ControlerNw.config_log.getNodePerServer(i) + "\n");
			}
			k++;
		}
		
		wf1.write("Total : " + k + " pairs " + j+ " nœuds\n");
		
		Enumeration<Integer> enumeration2 = tab.keys();
		
		while (enumeration2.hasMoreElements())
		{
			int q = enumeration2.nextElement();
			
			float tmp = (float) ((float)(tab.get(q))/(float)k)*100;
			float tmp2 = (float) ((float)(tab.get(q)*q)/(float)j)*100;
			wf1.write(tab.get(q) + " pairs contient "+ q + " nœuds = " + tmp + "% de pairs " + tmp2 +"% de nœuds\n");
		}
			
		wf1.close();
		
		wf1 = new WriteFile(Config.peerSimLOG + "_filterPerNode", true);
		
		Enumeration<String> enumeration4 = ControlerNw.config_log.getFilterPerNode().keys();
		
		int i = 0;
		j = 0;
		k = 0;
				
		Hashtable<Integer, ArrayList<String>> hias = new Hashtable<Integer, ArrayList<String>>();
		while (enumeration4.hasMoreElements())
		{
			String s_tmp = enumeration4.nextElement();
			
			WriteFile wf2 = new WriteFile(Config.peerSimLOG + "_node", true);
			ControlerNw.config_log.getTranslate().setRange(Network.size());
			if (s_tmp != "/")
			{
				wf2.write(s_tmp + "        " + ControlerNw.config_log.getTranslate().translate(s_tmp) + "\n");
			}
			else
			{
				wf2.write(s_tmp + "\n");
			}
			wf2.close();
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) == 0)
			{
				i++;
				continue;
			}
			
			j += ControlerNw.config_log.getFilterPerNode().get(s_tmp);
			
			if (i < 10)
			{
				wf1.write(i + "       " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp +  "\n");
			}
			else if (i < 100)
			{ 
				wf1.write(i + "      " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			else
			{					
				wf1.write(i + "     " 
						+ ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			i++;
			k++;
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 50)
			{
				if (hias.containsKey(50))
				{
					hias.get(50).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(50, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 100)
			{
				if (hias.containsKey(100))
				{
					hias.get(100).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(100, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 200)
			{
				if (hias.containsKey(200))
				{
					hias.get(200).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(200, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 500)
			{
				if (hias.containsKey(500))
				{
					hias.get(500).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(500, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 1000)
			{
				if (hias.containsKey(1000))
				{
					hias.get(1000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(1000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 5000)
			{
				System.out.println("<5000 = " + ControlerNw.config_log.getFilterPerNode().get(s_tmp));
				if (hias.containsKey(5000))
				{
					hias.get(5000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(5000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 10000)
			{
				if (hias.containsKey(10000))
				{
					hias.get(10000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(10000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 15000)
			{
				if (hias.containsKey(15000))
				{
					hias.get(15000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(15000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 20000)
			{
				if (hias.containsKey(20000))
				{
					hias.get(20000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(20000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 30000)
			{
				if (hias.containsKey(30000))
				{
					hias.get(30000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(30000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 40000)
			{
				if (hias.containsKey(40000))
				{
					hias.get(40000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(40000, als);
				}
			}
			else if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) >= 40000)
			{
				if (hias.containsKey(50000))
				{
					hias.get(50000).add(s_tmp);
				}
				else
				{
					ArrayList<String> als = new ArrayList<String>();
					als.add(s_tmp);
					hias.put(50000, als);
				}
			}
		}
		
		wf1.write("Total : " + k + "/" + i+ "\n");
		wf1.write("Moyen : " + j + "/" + k + " = " + (j/k) + "\n\n");
		
		float n = 0;

		int m = hias.get(50) == null ? 0 : hias.get(50).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("    <50 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(100) == null ? 0 : hias.get(100).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <100 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(200) == null ? 0 : hias.get(200).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <200 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(500) == null ? 0 : hias.get(500).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <500 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(1000) == null ? 0 : hias.get(1000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <1000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(5000) == null ? 0 : hias.get(5000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <5000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(5000).toString() + "\n");
		
		m = hias.get(10000) == null ? 0 : hias.get(10000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <10000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(10000).toString() + "\n");
		
		m = hias.get(15000) == null ? 0 : hias.get(15000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <15000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(15000).toString() + "\n");
		
		m = hias.get(20000) == null ? 0 : hias.get(20000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <20000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(20000).toString() + "\n");
		
		m = hias.get(30000) == null ? 0 : hias.get(30000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <30000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(30000).toString() + "\n");
		
		m = hias.get(40000) == null ? 0 : hias.get(40000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <40000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(40000).toString() + "\n");
		
		m = hias.get(50000) == null ? 0 : hias.get(50000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(">=40000 = " + m + "   " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(50000).toString() + "\n");
		
		wf1.write("            = " + n + "%\n");
		
		wf1.close();
		
		recu_OK = true;
		
		//*******************
		WriteFile wf = new WriteFile(Config.peerSimLOG+"_indexHeight", false);
		Enumeration<Integer> enumeration = ControlerNw.config_log.getIndexHeight().keys();
		wf.write("Nombre de messages        : " + (ControlerNw.config_log.numberOfMessages - Network.size()*2 - 1*2 - 1) + "\n\n");
		wf.write("Nombre de filtres crées   : " + numberOfFiltersCreated + "\n");
		wf.write("Nombre de filtres ajoutés : " + ControlerNw.config_log.totalFilterAdded + "\n\n"); //j + "\n\n");
		wf.write("Taille du filtre          : " + ControlerNw.config_log.sizeOfBF + "\n");
		wf.write("Gamma                     : " + ControlerNw.config_log.gamma + "\n\n");
		
		while (enumeration.hasMoreElements())
		{
			i = enumeration.nextElement();
			if (i <= 9)
			{
				wf.write(i + "  " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}
			else
			{
				wf.write(i + " " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}			
		}
		wf.write("\n");
		
		enumeration = ControlerNw.config_log.getRealIndexHeight().keys();
		
		while (enumeration.hasMoreElements())
		{
			i = enumeration.nextElement();
			if (i <= 9)
			{
				wf.write(i + "  " + ControlerNw.config_log.getRealIndexHeight().get(i) + "\n");
			}
			else
			{
				wf.write(i + " " + ControlerNw.config_log.getRealIndexHeight().get(i) + "\n");
			}	
		}
		
		wf.close();		
		//*******************
		
		Config.ObserverNw_OK = true;
	}
		
	
}











