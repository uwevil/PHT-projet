package peerSimTest_v2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import serveur.Message;
import test.WriteFile;

public class SystemIndexProtocol implements EDProtocol{

	private static final String PAR_TRANSPORT = "transport";
	private String prefix;
	private int tid;
	private int nodeIndex;
	private Transport t;
	private int id;
	
	private Hashtable<Integer, SystemIndexP2P> listSystemIndexP2P = new Hashtable<Integer, SystemIndexP2P>();
	private int[] recu = new int[Network.size()];
	private boolean recu_OK = false;
	
	public SystemIndexProtocol(String prefix) {
		// TODO Auto-generated constructor stub
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
	
	@Override
	public void processEvent(Node node, int pid, Object event) {
		// TODO Auto-generated method stub
		
		t = (Transport) Network.get(nodeIndex).getProtocol(tid);
		Message message = (Message)event;
				
		switch(message.getType())
		{
		case "createIndex": //createIndex, Name, sourceID, descID, option
			treatCreateIndex(message, pid);		
			break;
			
		case "removeIndex": //removeIndex, Name
			treatRemoveIndex(message, pid);
			break;
			
		case "removeIndexSuite":
			treatRemoveIndexSuite(message, pid);
			break;
			
		case "add": // add, Name, path, BFP2P
			treatAdd(message, pid);
			break;
			
		case "remove": //remove, Name, BFP2P
			treatRemove(message, pid);
			break;
			
		case "search": //search, Name, tableau contient BFP2P et liste des paths
			treatSearch(message, pid);
			break;
			
		case "searchExact":
			treatSearchExact(message, pid);
			break;
			
		case "createNode": //createNode, Name, path
			treatCreateNode(message, pid);
			break;
			
		case "removeNode": //removeNode, Name, path
			treatRemoveNode(message, pid);
			break;
		 
		case "search_OK": //			
			treatSearch_OK(message, pid);
			break;
			
		case "searchExact_OK":
			treatSearchExact_OK(message, pid);
			break;
			
		case "overview": // nœud 0 balance la requete vers tous les autres nœuds
			treatOverview(message, pid);
			break;
			
		case "overview_OK": // tous les nœuds répond au nœud 0
			treatOverview_OK(message, pid);
			break;
			
		default : 
			break;
		}
	}


	@SuppressWarnings({ "unchecked" })
	/*
	 * Traiter la réponse pour une recherch
	 * 
	 * Le message contient indexName, filtre de la requête, liste des requêtes trouvés, nombre de réponses en attente
	 * */
	
	private void treatSearch_OK(Message message, int pid)
	{
		BFP2P BFP2P = (BFP2P) ((Object[])message.getData())[0];
		
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());
				
		ArrayList<BFP2P> data1 = (ArrayList<BFP2P>) ((Object[])message.getData())[1];
		
		if (data1 != null && data1.size() > 0)
		{
			if (ControlerNw.search_log.get(key) == null)
			{
				return;
			}
			//*************Compter le nombre de filtres trouvés pour cette requête
			ControlerNw.search_log.get(key).addNumberOfFilters(data1.size());
			//********************************************************************
			
			//*************LOG le résultat******************
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
			
			WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_"+key, true);
			
			for (int i = 0; i < data1.size(); i++)
			{
				wf.write(((BFP2P)(data1.get(i))).toString() + "\n");
			}
			wf.close();
			
			WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + key, true);
			wf1.write(date + "       Source : " + message.getSource() + "\n");
			wf1.write("                              " 
						+ ControlerNw.search_log.get(key).getNumberOfFilters() + " (" + data1.size() +")\n");
			wf1.close();
			//***********************************************
		}
		
		if (treatListAnswer(message)) // si toutes les réponses sont reçues
		{
			//******************Compter le temps total de la recherche*******************
			long time = Calendar.getInstance().getTimeInMillis() - ControlerNw.search_log.get(key).getTime();
			ControlerNw.config_log.getTimeGlobal().put(key, time);
			//***************************************************************************
			
			//******************LOG le résultat*****************************************
			WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_resume_"+key, true);
			
			wf1.write("Nombre de chemins visités : " + ControlerNw.search_log.get(key).getNodeVisited() + " nœuds\n");
			wf1.write("Nombre de chemins matched : " + ControlerNw.search_log.get(key).sizeNodeMatched() + " nœuds\n");
			
			int j = 0;
			for (int i = 0; i < Network.size(); i++)
			{
				if (((int[])((Object[])ControlerNw.search_log.get(key).getListAnswer(key))[0])[i] > 0)
					j++;		
			}
			
			wf1.write("Nombre de pairs visités : " + j + " pairs (ou " + (j + 1) +" pairs si le pair qui"
					+ " reçoit la requête gère la racine du système d'indexation)\n");
			
			if (time >= 1000)
			{
				long i = time;
				long hours = TimeUnit.MILLISECONDS.toHours(i);
				i -= TimeUnit.HOURS.toMillis(hours);
				long minutes = TimeUnit.MILLISECONDS.toMinutes(i);
				i -= TimeUnit.MINUTES.toMillis(minutes);
				long seconds = TimeUnit.MILLISECONDS.toSeconds(i);
				i -= TimeUnit.SECONDS.toMillis(seconds);
				
				wf1.write("\nTemps de recherche : " + time + "ms == " 
						+ hours + ":" + minutes + ":" + seconds + "." + i
						+ "\n");
			}
			else
			{
				wf1.write("\nTemps de recherche : " + time + "ms\n");
			}
			
			wf1.write("Temps de calcul    : " + ControlerNw.search_log.get(key).getTime_calcul() + "ms\n\n");
			
			wf1.write("Trouvé : " + ControlerNw.search_log.get(key).getNumberOfFilters() + " filtres\n\n");
			
			wf1.write("Liste des chemins matched: " + ControlerNw.search_log.get(key).getNodeMatched() + "\n");
			wf1.close();
			
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setIndexName(message.getIndexName());
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
			//*******************************************************************************
			
			//************Supprimer la requête dans la table de requête
			ControlerNw.search_log.remove(key);
			//**********************************************************
		}
		
		if (ControlerNw.search_log.isEmpty()) // si il n'y a aucune requête en cours
		{
			ControlerNw.config_log.setEnd_OK(true);
		}
	}

	/*
	 * Traiter le message de la recherche exacte
	 * 
	 * Le message contient le filtre de la requête, le filtre trouvé
	 * */
	
	private void treatSearchExact_OK(Message message, int pid)
	{
		
		BFP2P data1 =(BFP2P) ((Object[])message.getData())[1];
		
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate((((Object[])message.getData())[0]).toString());
		
		if (data1 == null)
		{
			//**********Compter le temps total de la recherche exacte*************
			long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(key).getTime();
			ControlerNw.config_log.getTimeGlobal().put(key, time);
			//********************************************************************
			
			/*
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "Exact_null", true);
			wf.write("BFP2P source " + message.getSource() + "\n"
					+ ((BFP2P) ((Object[])message.getData())[0]).toString() + "\n\n"
					+ "\n\n");
			
			wf.write("Filtres crées   : " + ControlerNw.config_log.getTotalFilterCreated() + "\n");
			wf.write("Filtres ajoutés : " + ControlerNw.config_log.getTotalFilterAdded() + "\n\n");
			wf.write("Nœuds crées     : " + ControlerNw.config_log.getNodeCreated() + "\n");
			wf.write("Nœuds visités   : " + ControlerNw.config_log.getNodeVisited() + "\n");
			wf.write("Nœuds matched   : " + ControlerNw.config_log.getNodeMatched().size() + "\n\n");
		
			int k = 0;
			for (int i = 0; i < Network.size(); i++)
			{
				if (ControlerNw.config_log.getPeerCreated(i))
					k++;
			}
			
			wf.write("Nombre de pairs : " + k + " pairs\n");
			
			wf.write("Temps de recherche : " + time + "ms\n\n");
			

			wf.write("Trouvé : " + ControlerNw.search_log.get(key).getNumberOfFilters() + " filtres\n\n");
			
			wf.write("Liste des chemins matched: " + ControlerNw.search_log.get(key).getNodeMatched() + "\n");
			
			wf.close();
			//*****************
			*/
		}
		else // data1 != null
		{	
			//**********Compter le temps total de la recherche exacte*************
			long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(key).getTime();		
			ControlerNw.config_log.getTimeGlobal().put(key, time);
			//********************************************************************
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "Exact_" + key, true);
			wf.write("BFP2P source " + message.getSource() + "\n"
					+ ((BFP2P) ((Object[])message.getData())[0]).toString() + "\n\n"
					+ data1.toString()
					+ "\n\n");
			
			wf.write("Nœuds visités   : " + ControlerNw.config_log.getNodeVisited() + "\n");
			wf.write("Nœuds matched   : " + ControlerNw.config_log.getNodeMatched().size() + "\n\n");
			
			wf.write("Temps de recherche : " + time + "ms\n\n");
			wf.write("Trouvé : " + ControlerNw.search_log.get(key).getNumberOfFilters() + " filtres\n\n");
			wf.write("Liste des chemins matched: " + ControlerNw.search_log.get(key).getNodeMatched() + "\n");
			
			wf.close();
			//*****************
			
			for (int i = 0; i < Network.size(); i++)
			{	
				Message rep = new Message();
				rep.setType("overview");
				rep.setIndexName(message.getIndexName());
				rep.setSource(nodeIndex);
				rep.setDestinataire(i);
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}
		//***************Supprimer la requête dans la liste de requête en cours**
		ControlerNw.search_log.remove(key);
		//*********************************************************
		
		if (ControlerNw.search_log.isEmpty()) // si in n'y a aucune requête en cours, signaler
		{
			ControlerNw.config_log.setEnd_OK(true);
		}
	}

	/*
	 * Traiter le message reçu appellé par treatSearch_OK(recherche globale)
	 * 
	 * retourner true si il n'y a plus de réponses en attente, false sinon
	 * */
	
	private boolean treatListAnswer(Message message)
	{
		BFP2P BFP2P = (BFP2P) ((Object[])message.getData())[0];
		
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());

		// si cette requête a été enregistrée avant le lancement de la recherche
		if (ControlerNw.search_log.get(key).containsKeyListAnswer(key))
		{
			Object[] o = (Object[]) ControlerNw.search_log.get(key).getListAnswer(key);
			int[] received = (int[])o[0];
			int[] total = (int[])o[1];
			
			if (message.getSource() != nodeIndex) // si le serveur répond n'est pas l'enquêteur
				received[message.getSource()] += 1;
			
			// si la liste des réponses en attente du serveur qui a envoyé ce message
			//	est vide
			if (message.getOption1() == null) 
			{
				return testOK(received, total, Network.size());
			}
			else
			{
				String s = (String) message.getOption1();
				String[] tmp = s.split(";");
				
				if (tmp.length < 1)
				{
					return testOK(received, total, Network.size());
				}
				else
				{
					for (int i = 0; i < tmp.length; i++)
					{
						if (tmp[i].length() >= 1)
						{
							int j = Integer.parseInt(tmp[i]);
							
							if (j != nodeIndex)
							{
								total[j] += 1;
							}
						}
					}
					return testOK(received, total, Network.size());
				}
			}
		}
				
		return false;	
	}
	
	/*
	 * Retourner true si 2 tables sont identiques, false sinon
	 * */
	
	private boolean testOK(int[] a, int[] b, int size)
	{
		/*
		//*******LOG*************
		String date = (new SimpleDateFormat("HH-mm-ss-SSS")).format(new Date());
		WriteFile wf = new WriteFile(Config.peerSimLOG + "_testOK", true);
		wf.write(date + "\n");
		wf.write(" received  total \n");
		
		for (int i = 0; i < Network.size(); i++)
		{
			if (a[i] != 0 || b[i] != 0)
				wf.write(i + " " + a[i] + " " + b[i] + "\n");
		}
		wf.write("\n");
		wf.close();
		//************************
		*/
		
		for (int i = 0; i < size; i++)
		{
			if (a[i] != b[i])
				return false;
		}
		return true;
	}
	
	/*
	 * Traiter le message de création du nœud
	 * 
	 * Ce message contient indexName, chaîne de caractères
	 * */
	
	private void treatCreateNode(Message message, int pid)
	{
		String indexName = message.getIndexName();
		String path = message.getPath();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (serverID == nodeIndex) // c'est bien ce serveur qui gère ce path
		{	
			SystemIndexP2P systemIndex;
			if (!this.listSystemIndexP2P.containsKey(indexID)) // contient pas ce systemIndex
			{
				systemIndex = new SystemIndexP2P(indexName, serverID, Config.gamma);
				
				this.listSystemIndexP2P.put(indexID, systemIndex);				
			}
			else // listSystemIndexP2P.containsKey(indexID)
			{
				systemIndex = this.listSystemIndexP2P.get(indexID);			
			}
			
			SystemNodeP2P systemNode = new SystemNodeP2P(serverID, path, 
					(new CalculRangP2P()).getRang(path), Config.gamma);
			
			//*********Calculer le profondeur du système*********
			int rang = systemNode.getRang();
			if (!ControlerNw.config_log.getIndexHeight().containsKey(rang))
			{
				ControlerNw.config_log.getIndexHeight().put(rang, systemNode.getPath());
			}
			//************************************
			
			systemIndex.addSystemNodeP2P(path, systemNode);
			
			ContainerLocalP2P c = (ContainerLocalP2P) message.getData();
			Iterator<BFP2P> iterator = c.iterator();
			
			Message rep = new Message();
			rep.setType("add");
			rep.setIndexName(indexName);
			rep.setPath(path);
			rep.setDestinataire(message.getDestinataire());
			rep.setSource(message.getSource());
			
			while (iterator.hasNext())
			{
				BFP2P BFP2P = (BFP2P)iterator.next();
				rep.setData(BFP2P);
				
				Object o = systemIndex.add(BFP2P, path);
				treatAdd(o, indexName, rep, pid);
			}
			/*
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_createNode", true);
			wf.write("createNode "+ indexID + " node "+ nodeIndex + "\n"
					+ message.toString()
					+ "\n");
			wf.close();
			//*****************
			 */
		}
		else // serverID != nodeIndex
		{
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
			/*
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("createNode forward "+ indexID + " node "+ nodeIndex + "\n"
					+ message.toString()
					+ "\n");
			wf.close();
			//*****************
			 * 
			 */
		}
		
	}

	/*
	 * Traiter le message d'ajout dans le système
	 * 
	 * Ce message contient indexName, chaîne de caractères(path), filtre
	 * */
	
	private void treatAdd(Message message, int pid)
	{
		String indexName = message.getIndexName();
		String path = message.getPath();
		
		if (path.equals("/")) // ajout dans la racine, c-à-d la racine est sur le serveur qui stocke systemIndex
		{
			ControlerNw.config_log.getTranslate().setLength(Network.size());	
			int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			if (serverID == nodeIndex) //c'est bien le serveur racine qui gère ce systemIndex
			{				
				if (!this.listSystemIndexP2P.containsKey(indexID)) // s'il contient pas ce systemIndex
					return;
				
				SystemIndexP2P systemeIndex = (SystemIndexP2P)this.listSystemIndexP2P.get(indexID);
				Object o = systemeIndex.add((BFP2P)message.getData(), path);
				treatAdd(o, indexName, message, pid);
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("add "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
			*/
			}
			else // serverID != nodeIndex
			{				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("add forward "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
			*/
			}
		}
		else // path != "/"
		{ 
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(path);
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			if (serverID == nodeIndex) // si contient ce nœud
			{
				if (!this.listSystemIndexP2P.containsKey(indexID)) // s'il contient pas ce systemIndex, créez le
				{
					SystemIndexP2P systemIndex = new SystemIndexP2P(indexName, serverID, Config.gamma);
					systemIndex.add((BFP2P)message.getData(), path);
					
					this.listSystemIndexP2P.put(indexID, systemIndex);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("add path "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
				*/
				}
				else // this.listSystemIndexP2P.containsKey(indexID)
				{
					SystemIndexP2P systemIndex = (SystemIndexP2P)this.listSystemIndexP2P.get(indexID);
					
					Object o = systemIndex.add((BFP2P)message.getData(), path);
					treatAdd(o, indexName, message, pid);
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("add path "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
				*/
				}
			}
			else // serverID != nodeIndex
			{				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("add path forward "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
			*/
			}
		}
	}
	
	/*
	 * Traiter les cas de la réponse du nœud du système
	 * 
	 * 3 cas: null, chaîne de caractères, et message(split)
	 * */
	
	private void treatAdd(Object o, String indexName, Message message, int pid)
	{
		if (o == null)
			return;
		
		if (o.getClass().getName().equals("java.lang.String")) // adresse d'un nœud
		{
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int tmp_nodeID = ControlerNw.config_log.getTranslate().translate((String)o);
			
			Message rep = new Message();
			rep.setType("add");
			rep.setIndexName(indexName);
			rep.setPath((String) o);
			rep.setData(message.getData());
			rep.setSource(nodeIndex);
			rep.setDestinataire(tmp_nodeID);
			
			t.send(Network.get(nodeIndex), Network.get(tmp_nodeID), rep, pid);
		}
		else // Message split()
		{
			Message o_tmp = (Message)o;
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int tmp_nodeID = ControlerNw.config_log.getTranslate().translate(o_tmp.getPath());
			
			Message rep = new Message();
			rep.setType("createNode");
			rep.setIndexName(indexName);
			rep.setPath(o_tmp.getPath());
			rep.setData(o_tmp.getData());
			rep.setSource(nodeIndex);
			rep.setDestinataire(tmp_nodeID);
			
			t.send(Network.get(nodeIndex), Network.get(tmp_nodeID), rep, pid);
		}
	}
		
	@SuppressWarnings("unchecked")
	
	/*
	 * Traiter le message de la recherche
	 * 
	 * Ce message contient indexName, chaîne de caractères et filtre de la requête
	 * */
	
	private void treatSearch(Message message, int pid)
	{		
		String indexName = message.getIndexName();
		BFP2P BFP2P = (BFP2P) ((Object[])message.getData())[0];
		
		long temps = Calendar.getInstance().getTimeInMillis();

		if (((Object[]) message.getData())[1] == null)
		{
			Message rep = new Message();
			rep.setType("search_KO");
			rep.setIndexName(indexName);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData("list path ou path == null");
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			
			return;
		}
		
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());
			
		if (((Object[]) message.getData())[1].getClass().getName().equals("java.lang.String")) // search la racine
		{	
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			//*************Créer une liste de réponses reçue et en attente pour cette requête******
			if (nodeIndex == message.getSource())
			{				
				if (!ControlerNw.search_log.containsKey(key))
				{
					Config config = new Config();
					config.setTime((Calendar.getInstance()).getTimeInMillis());
										
					if (!config.containsKeyListAnswer(key))
					{
						Object[] o = new Object[2];
						int[] received = new int[Network.size()];
						int[] total = new int[Network.size()];
						
						for (int i = 0; i < Network.size(); i++)
						{
							received[i] = (0);
							total[i] = (0);
						}
						
						received[nodeIndex] = -1;
						total[nodeIndex] = -1;
						
						o[0] = received;
						o[1] = total;
						
						config.putListAnswer(key, o);
					}
					
					String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

					WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + key, true);
					wf1.write(date + "\n");
					wf1.close();
					
					ControlerNw.search_log.put(key, config);	
				}
			}
			//**************************************************************************************
			
			if (serverID == nodeIndex) // ce serveur gère ce systemIndex
			{
				if (this.listSystemIndexP2P.containsKey(indexID)) // il contient ce systemIndex
				{
					
					//*******LOG*******
					String s_tmp = new String();
					for (int i = 0; i < Config.numberOfFragment; i++)
					{
						s_tmp += "/"+ BFP2P.getFragment(i).toInt();
					}
					String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path + "_" + key, false);
					wf1.write(date 
							+ "       Node " + nodeIndex + " receive from " + message.getSource() + "\n"
							+ "        BFP2P " + BFP2P.toString() + "\n"
							+ "        BFP2P_path : " + s_tmp + "\n"
							+ "        Path : " + "/" + "\n\n");
					wf1.close();
					//*****************
				
					SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search racine "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
					Object o = systemIndex.search(BFP2P, "/");
			
					treatSearch(o, indexName, message, pid);		
				}
				else // il contient pas indexID
				{
					
					//*******LOG*******
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path + "_" + key, true);
					wf1.write("Node " + nodeIndex + " reply to " + message.getSource() + "\n"
							+ "\n");
					wf1.close();
					//*****************
					
					Message rep = new Message();
					rep.setIndexName(indexName);
					rep.setType("search_OK");
					
					Object[] o_tmp = new Object[2];
					o_tmp[0] = BFP2P;
					o_tmp[1] =  null;
					
					rep.setData(o_tmp);
					rep.setSource(nodeIndex);
					rep.setDestinataire(message.getSource());
					
					t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search_OK "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
				}
			}
			else // ce n'est pas celui qui gère ce systemIndex : serverID != nodeIndex
			{
				
				//*******LOG*******
				String s_tmp = new String();
				for (int i = 0; i < Config.numberOfFragment; i++)
				{
					s_tmp += "/"+ BFP2P.getFragment(i).toInt();
				}
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path + "_" + key, true);
				wf1.write(date + "       Node " + nodeIndex + " transfer to " + serverID + "\n"
						+ "        BFP2P " + BFP2P.toString() + "\n"
						+ "        BFP2P_path : " + s_tmp + "\n"
						+ "        Path : " + "/" + "\n");
				wf1.close();
				//*****************
				
				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
				
				//***********listAnswers*********
				ControlerNw.config_log.getTranslate().setLength(1000000);
				int key1 = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());
				
				Object[] o = (Object[]) ControlerNw.search_log.get(key1).getListAnswer(key1);
				
				int[] total = (int[])o[1];
				total[serverID] += 1;
				//*******************************
				
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("search racine forward "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
				*/
			}
		}
		else // search les fils : ArrayList<String>
		{
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			
			//*******LOG*******
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
			
			WriteFile wf = new WriteFile(Config.peerSimLOG_path + "_" + key, true);
			wf.write(date +"       Node " + nodeIndex + " receive paths"
				//	+ message.toString() + "\n"
					+ "\n");
			wf.close();
			//*****************
			
			
			if (this.listSystemIndexP2P.containsKey(indexID)) // contient indexID
			{
				SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
				
				ArrayList<String> als = (ArrayList<String>) ((Object[])message.getData())[1];
				Iterator<String> iterator = als.iterator();
				
				while(iterator.hasNext())
				{
					String path_tmp = iterator.next();
					
					//*******LOG*******
					
					ControlerNw.config_log.getTranslate().setLength(1000000);
					int key1 = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path + "_" + key1, true);
					wf1.write("        Path : " + path_tmp + "\n");
					wf1.close();
					//*****************
					
					Object o = systemIndex.search(BFP2P, path_tmp);
					treatSearch(o, indexName, message, pid);
				}
	
				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search path "+ indexID+ " node "+ nodeIndex  + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
				*/
			}
			else // !this.listSystemIndexP2P.containsKey(indexID)
			{
				Message rep = new Message();
				rep.setIndexName(indexName);
				rep.setType("search_OK");
				
				Object[] o_tmp = new Object[2];
				o_tmp[0] = BFP2P;
				o_tmp[1] = null;
				
				rep.setData(o_tmp);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());

				
				//*******LOG*******
				String date1 = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				ControlerNw.config_log.getTranslate().setLength(1000000);
				int key1 = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());

				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path + "_" + key1, true);
				wf1.write(date1 + "       Node " + nodeIndex + " reply to " + message.getSource() + "\n"
						+ "\n");
				wf1.close();
				//*****************
				
				
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
				
				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search_OK path "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
			*/
			}	
		}
		//******************Temps de calcul**********************
		temps = Calendar.getInstance().getTimeInMillis() - temps;
		// cette variable est vidée une fois on a reçu toutes les réponses
		ControlerNw.search_log.get(key).addTime_calcul(temps); 
		// cette variable est permanante
		ControlerNw.config_log.addTimeCalcul(key, temps);
		//*******************************************************
	}
		
	@SuppressWarnings("unchecked")
	
	/*
	 * Traiter la valeur retournée par le système
	 * 
	 * il y a 2 cas, null ou non null
	 * si non null, le message contient une liste de filtres trouvés et une liste des servers à tranférer la requête
	 * */
	
	private void treatSearch(Object o, String indexName, Message message, int pid)
	{
		ControlerNw.config_log.getTranslate().setLength(1000000);
		int key = ControlerNw.config_log.getTranslate().translate(((BFP2P) ((Object[])message.getData())[0]).toString());
		
		if (o == null)
		{
			if (message.getSource() != nodeIndex)
			{
				Object[] o_tmp = new Object[2];
				o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
				o_tmp[1] = null;
				
				Message rep = new Message();
				rep.setType("search_OK");
				rep.setIndexName(indexName);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				rep.setData(o_tmp);
			
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			}
			return;
		}
		
		if (message.getSource() == nodeIndex) // c'est bien l'enquêteur
		{
			if (((ArrayList<BFP2P>) ((Object[])o)[0]).size() == 0)
				return;
			
			//*******LOG*******
			
			ControlerNw.search_log.get(key).addNumberOfFilters( ((ArrayList<BFP2P>) ((Object[])o)[0]).size() ) ;
			
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

			ArrayList<BFP2P> data1 = (ArrayList<BFP2P>) ((Object[])o)[0];
			
			
			if (data1 != null && data1.size() > 0)
			{				
				WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_"+key, true);
				
				for (int i = 0; i < data1.size(); i++)
				{
					wf.write(((BFP2P)(data1.get(i))).toString() + "\n");
				}
				wf.close();
			}
			
			/*
			WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_"+key, true);
			
			
			wf.write("BFP2P source " + message.getSource() + "\n"
					+ ((BFP2P) ((Object[])message.getData())[0]).toString() + "\n\n"
					+ ((ArrayList<BFP2P>) ((Object[])o)[0]).toString()
					+ "\n\n");
			wf.close();
			*/
			
			WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + key, true);
			wf1.write(date + "       Source : " + message.getSource() + "\n");
			wf1.write("                                  " 
					+ ControlerNw.search_log.get(key).getNumberOfFilters()
					+ " (" + ((ArrayList<BFP2P>) ((Object[])o)[0]).size() +")\n");
			wf1.close();
			//*****************
		
		}
		else // message.getSource() != nodeIndex, répond au l'enquêteur
		{
			ArrayList<BFP2P> alBFP2P = ((ArrayList<BFP2P>) ((Object[])o)[0]);
			Hashtable<Integer, ArrayList<String>> hsials = ((Hashtable<Integer, ArrayList<String>>)((Object[])o)[1]);
			Enumeration<Integer> enumInt = hsials.keys();

			String s = new String();
			while (enumInt.hasMoreElements())
			{
				Integer i = enumInt.nextElement();
				s += i;
				for (int j = 1; j < hsials.get(i).size(); j++)
					s += ";" + i;
					
				if (enumInt.hasMoreElements())
					s += ";";
			}		
			
			Object[] o_tmp = new Object[2];
			o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
			o_tmp[1] = alBFP2P;
						
			Message rep = new Message();
			rep.setType("search_OK");
			rep.setIndexName(indexName);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData(o_tmp);
			rep.setOption1(s);
		
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			
			
			//*******LOG*******
			WriteFile wf = new WriteFile(Config.peerSimLOG+"_test_"+key, true);
			wf.write("search treatSearch " + " node "+ nodeIndex + "\n"
					+ rep.toString() + "\n"
					+ "size = " + hsials.size() +"\n"
					+ "\n");
			wf.close();
			//*****************
		
		}	
		
		// transférer la requête
		Hashtable<Integer, ArrayList<String>> hsials = ((Hashtable<Integer, ArrayList<String>>)((Object[])o)[1]);
		
		Enumeration<Integer> enumInt = hsials.keys();

		while (enumInt.hasMoreElements())
		{
			Message rep = new Message();
			rep.setType("search");
			rep.setIndexName(indexName);
			rep.setSource(message.getSource());
			
			Integer i = enumInt.nextElement();
			Object[] o_tmp = new Object[2];
			o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
			ArrayList<String> o_tmp1 = new ArrayList<String>(hsials.get(i));
			o_tmp[1] = o_tmp1;

			rep.setData(o_tmp);
			rep.setDestinataire(i);

			//*******LOG*******
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
			WriteFile wf1 = new WriteFile(Config.peerSimLOG_path+ "_"+ key, true);
			wf1.write(date + "       Node " + nodeIndex + " search path1 to " + i + "\n"
					+ "        Path list : " + ((Object[])rep.getData())[1].toString() + "\n"
					+ "\n");
			wf1.close();
			//*****************
			
			t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
		}	
	}

	/*
	 * Traiter le message de la recherche exacte
	 * 
	 * Ce message contient indexName, filtre de la requête et chaîne de caractères(chemin)
	 * */
	
	private void treatSearchExact(Message message, int pid)
	{		
		String indexName = message.getIndexName();
		BFP2P BFP2P = (BFP2P) ((Object[])message.getData())[0];
		
		if (((Object[]) message.getData())[1] == null)
		{
			Message rep = new Message();
			rep.setType("searchExact_KO");
			rep.setIndexName(indexName);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData("list path ou path == null");
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			return;
		}
		
		if ((String)((Object[]) message.getData())[1] == "/") // search la racine
		{				
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			if (nodeIndex == message.getSource())
			{				
				ControlerNw.config_log.getTranslate().setLength(1000000);
				int key = ControlerNw.config_log.getTranslate().translate(BFP2P.toString());
				
				if (!ControlerNw.search_log.containsKey(key))
				{
					Config config = new Config();
					config.setTime((Calendar.getInstance()).getTimeInMillis());
					
					ControlerNw.search_log.put(key, config);	
				}
			}
			
			if (serverID == nodeIndex) // ce serveur gère ce systemIndex
			{
				if (this.listSystemIndexP2P.containsKey(indexID)) // il contient ce systemIndex
				{
					/*
					//*******LOG*******
					String s_tmp = new String();
					for (int i = 0; i < Config.numberOfFragment; i++)
					{
						s_tmp += "/"+ BFP2P.getFragment(i).toInt();
					}
					String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, false);
					wf1.write(date + "       Node " + nodeIndex + " receive from " + message.getSource() + "\n"
							+ "        BFP2P " + BFP2P.toString() + "\n"
							+ "        BFP2P_path : " + s_tmp + "\n"
							+ "        Path : " + "/" + "\n\n");
					wf1.close();
					//*****************
				*/
					
					SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search racine "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
					
					Object o = systemIndex.searchExact(BFP2P, "/");
					
					treatSearchExact(o, indexName, message, pid);
				}
				else // il contient pas indexID
				{
					/*
					//*******LOG*******
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
					wf1.write("Node " + nodeIndex + " reply to " + message.getSource() + "\n"
							+ "\n");
					wf1.close();
					//*****************
					*/
					
					Message rep = new Message();
					rep.setIndexName(indexName);
					rep.setType("searchExact_OK");
					
					Object[] o_tmp = new Object[2];
					o_tmp[0] = BFP2P;
					o_tmp[1] =  null;
					
					rep.setData(o_tmp);
					rep.setSource(nodeIndex);
					rep.setDestinataire(message.getSource());
					
					t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search_OK "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
				}
			}
			else // ce n'est pas celui qui gère ce systemIndex : serverID != nodeIndex
			{
				/*
				//*******LOG*******
				String s_tmp = new String();
				for (int i = 0; i < Config.numberOfFragment; i++)
				{
					s_tmp += "/"+ BFP2P.getFragment(i).toInt();
				}
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write(date + "       Node " + nodeIndex + " transfer to " + serverID + "\n"
						+ "        BFP2P " + BFP2P.toString() + "\n"
						+ "        BFP2P_path : " + s_tmp + "\n"
						+ "        Path : " + "/" + "\n");
				wf1.close();
				//*****************
				*/
				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
				
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("search racine forward "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
				*/
			}
		}
		else // search les fils : path != "/"
		{
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			/*
			//*******LOG*******
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

			WriteFile wf = new WriteFile(Config.peerSimLOG_path, true);
			wf.write(date +"       Node " + nodeIndex + " receive paths"
				//	+ message.toString() + "\n"
					+ "\n");
			wf.close();
			//*****************
			*/
			
			if (this.listSystemIndexP2P.containsKey(indexID)) // contient indexID
			{
				SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
				
				String path_tmp = (String) ((Object[])message.getData())[1];
									
				//*******LOG*******
				/*	
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write("        Path : " + path_tmp + "\n");
				wf1.close();
				//*****************
				*/
					
				Object o = systemIndex.searchExact(BFP2P, path_tmp);
				treatSearchExact(o, indexName, message, pid);

				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search path "+ indexID+ " node "+ nodeIndex  + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
				*/
			}
			else // !this.listSystemIndexP2P.containsKey(indexID)
			{
				Message rep = new Message();
				rep.setIndexName(indexName);
				rep.setType("searchExact_OK");
				
				Object[] o_tmp = new Object[2];
				o_tmp[0] = BFP2P;
				o_tmp[1] = null;
				
				rep.setData(o_tmp);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				/*
				//*******LOG*******
				String date1 = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write(date1 + "       Node " + nodeIndex + " reply to " + message.getSource() + "\n"
						+ "\n");
				wf1.close();
				//*****************
				*/
				
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search_OK path "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
			*/
			}	
		}	
	}
	
	private void treatSearchExact(Object o, String indexName, Message message, int pid)
	{
		if (o == null)
		{
			if (message.getSource() != nodeIndex)
			{
				Object[] o_tmp = new Object[2];
				o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
				o_tmp[1] = null;
				
				Message rep = new Message();
				rep.setType("searchExact_OK");
				rep.setIndexName(indexName);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				rep.setData(o_tmp);
			
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			}
			else
			{
				ControlerNw.config_log.getTranslate().setLength(1000000);
				int key = ControlerNw.config_log.getTranslate().translate((((Object[])message.getData())[0]).toString());
				
				long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(key).getTime();
				
				ControlerNw.config_log.getTimeGlobal().put(key, time);
				
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "Exact_null", true);
				wf.write("BFP2P source " + message.getSource() + "\n"
						+ ((BFP2P) ((Object[])message.getData())[0]).toString() + "\n\n"
						+ "\n\n");
				
				wf.write("Filtres crées   : " + ControlerNw.config_log.getTotalFilterCreated() + "\n");
				wf.write("Filtres ajoutés : " + ControlerNw.config_log.getTotalFilterAdded() + "\n\n");
				wf.write("Nœuds crées     : " + ControlerNw.config_log.getNodeCreated() + "\n");
				wf.write("Nœuds visités   : " + ControlerNw.search_log.get(key).getNodeVisited() + "\n");
				wf.write("Nœuds matched   : " + ControlerNw.search_log.get(key).getNodeMatched().size() + "\n\n");
			
				int k = 0;
				for (int i = 0; i < Network.size(); i++)
				{						
					if (ControlerNw.config_log.getPeerCreated(i))
						k++;
				}
				
				wf.write("Nombre de pairs : " + k + " pairs\n");
				wf.write("Temps de recherche : " + time + "ms\n\n");
				wf.write("Trouvé : " + ControlerNw.search_log.get(key).getNumberOfFilters() + " filtres\n\n");
				wf.write("Liste des chemins matched: " + ControlerNw.search_log.get(key).getNodeMatched() + "\n");
				
				wf.close();
				*/
				ControlerNw.search_log.remove(key);

				//*****************
			}
			return;
		}
			
		if (o.getClass().getName().equals("java.lang.String"))
		{
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate((String)o);
			
			Object[] o_tmp = new Object[2];
			o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
			o_tmp[1] = o;
			
			Message rep = new Message();
			rep.setType("searchExact");
			rep.setIndexName(indexName);
			rep.setSource(message.getSource());
			rep.setDestinataire(serverID);
			rep.setData(o_tmp);
		
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		}
		else // BFP2P
		{
			if (message.getSource() == nodeIndex)
			{				
				ControlerNw.config_log.getTranslate().setLength(1000000);
				int key = ControlerNw.config_log.getTranslate().translate((((Object[])message.getData())[0]).toString());
				
				long time = (Calendar.getInstance()).getTimeInMillis() - ControlerNw.search_log.get(key).getTime();
				
				ControlerNw.config_log.getTimeGlobal().put(key, time);
				
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "Exact_" + key, true);
				wf.write("BFP2P source " + message.getSource() + "\n"
						+ ((BFP2P) ((Object[])message.getData())[0]).toString() + "\n\n"
						+ ((BFP2P)o).toString()
						+ "\n\n");
				
				wf.write("Nœuds visités   : " + ControlerNw.search_log.get(key).getNodeVisited() + "\n");
				wf.write("Nœuds matched   : " + ControlerNw.search_log.get(key).getNodeMatched().size() + "\n\n");
			
				wf.write("Temps de recherche : " + time + "ms\n\n");
				wf.write("Trouvé : " + ControlerNw.search_log.get(key).getNumberOfFilters() + " filtres\n\n");
				wf.write("Liste des chemins matched: " + ControlerNw.search_log.get(key).getNodeMatched() + "\n");
				
				wf.close();
				
				ControlerNw.search_log.remove(key);

				//*****************
			}
			else // message.getSource() != nodeIndex
			{
				Object[] o_tmp = new Object[2];
				o_tmp[0] = (BFP2P) ((Object[])message.getData())[0];
				o_tmp[1] = o;
				
				Message rep = new Message();
				rep.setType("searchExact_OK");
				rep.setIndexName(indexName);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				rep.setData(o_tmp);
			
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			}
		}	
	}

	/*
	 * Traiter le message de création d'un système index
	 * 
	 * Ce message contient indexName.
	 * */
	
	private void treatCreateIndex(Message message, int pid)
	{
		String indexName = message.getIndexName();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (serverID == nodeIndex)
		{
			if (listSystemIndexP2P.containsKey(indexID)) // contains indexID
			{
				Message rep = new Message();
				rep.setType("createIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("EXISTED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
				/*
				//********test********
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("createIndex "+ indexID + " node "+ nodeIndex + "\n"
						+ rep.toString()
						+ "\n");
				wf.close();
				//********************
				 */
			}
			else // not contains indexID => create
			{
				SystemIndexP2P systemIndex =  new SystemIndexP2P(indexName, serverID, Config.gamma);
				systemIndex.createRoot();
				
				listSystemIndexP2P.put(indexID,systemIndex);
								
				Message rep = new Message();
				rep.setType("createIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("CREATED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
				/*
				//********test********
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("createIndex "+ indexID + " node "+ nodeIndex + "\n"
						+ rep.toString()
						+ "\n");
				wf.close();
				//********************
				*/
			}	
		}
		else // forward cad serverID != nodeIndex
		{ 
			
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
			/*
			//********test********
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("createIndex forward "+ indexID+ " node "+ nodeIndex  + "\n"
					+ message.toString()
					+ "\n");
			wf.close();
			//********************
		*/
		}
	}
	
	/*
	 * Traiter le message de suppression d'un système index
	 * */
	// le nœud qui gère la racine diffuse aux autres pour supprimer le système d'index
	
	private void treatRemoveIndex(Message message, int pid)
	{
		String indexName = message.getIndexName();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (serverID == nodeIndex)
		{	
			Message rep = new Message();
			if (listSystemIndexP2P.containsKey(indexID))
			{
				listSystemIndexP2P.remove(indexID);
				rep.setType("removeIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("REMOVED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}
			else
			{
				rep.setType("removeIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("NOT_EXISTED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}
						
			for (int i = 0; i < Network.size(); i++)
			{
				rep = new Message();
				rep.setType("removeIndexSuite");
				rep.setIndexName(indexName);
				rep.setSource(message.getSource());
				rep.setDestinataire(i);
				
				if (i != nodeIndex)
					t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
			
			/*
			//********test********
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("removeIndex "+ indexID+ " node "+ nodeIndex  + "\n"
					+ rep.toString()
					+ "\n");
			wf.close();
			//********************
			*/
		}
		else // forward
		{
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
			/*
			//********test********
			WriteFile wf = new WriteFile(Config.peerSimLOG, true);
			wf.write("removeIndex forward "+ indexID+ " node "+ nodeIndex  + "\n"
					+ message.toString()
					+ "\n");
			wf.close();
			//********************
		*/
		}	
	}
	
	/*
	 * Traiter le message de suppression d'un système index provenant de la racine
	 * */

	private void treatRemoveIndexSuite(Message message, int pid)
	{
		String indexName = message.getIndexName();
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		Message rep = new Message();
		if (listSystemIndexP2P.containsKey(indexID))
		{
			listSystemIndexP2P.remove(indexID);
			rep.setType("removeIndex_OK");
			rep.setIndexName(indexName);
			rep.setData("REMOVED");
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
						
			t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
		}
		else
		{
			rep.setType("removeIndex_OK");
			rep.setIndexName(indexName);
			rep.setData("NOT_EXISTED");
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			
			t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
		}
	}
	
	/*
	 * Traiter le message de suppression d'un filtre du système index
	 * 
	 * Ce message contient indexName, filtre, chaîne de caractères(chemin)
	 * */
	
	private void treatRemove(Message message, int pid)
	{
		String indexName = message.getIndexName();
		BFP2P BFP2P = (BFP2P) message.getData();
		String path = (String) message.getPath();
		
		if (path == null)
		{
			Message rep = new Message();
			rep.setType("remove_KO");
			rep.setIndexName(indexName);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData("list path ou path == null");
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			return;
		}
		
		if (path == "/") // remove à partir de la racine
		{				
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			if (serverID == nodeIndex) // ce serveur gère ce systemIndex
			{
				if (this.listSystemIndexP2P.containsKey(indexID)) // il contient ce systemIndex
				{
					/*
					//*******LOG*******
					String s_tmp = new String();
					for (int i = 0; i < Config.numberOfFragment; i++)
					{
						s_tmp += "/"+ BFP2P.getFragment(i).toInt();
					}
					String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, false);
					wf1.write(date + "       Node " + nodeIndex + " receive from " + message.getSource() + "\n"
							+ "        BFP2P " + BFP2P.toString() + "\n"
							+ "        BFP2P_path : " + s_tmp + "\n"
							+ "        Path : " + "/" + "\n\n");
					wf1.close();
					//*****************
					*/
					
					SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search racine "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
					
					Object o = systemIndex.remove(BFP2P, "/");
					
					treatRemove(o, indexName, message, pid);
				}
				else // !listSystemIndexP2P.containsKey(indexID)
				{
					/*
					//*******LOG*******
					
					WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
					wf1.write("Node " + nodeIndex + " reply to " + message.getSource() + "\n"
							+ "\n");
					wf1.close();
					//*****************
					*/
					
					Message rep = new Message();
					rep.setIndexName(indexName);
					rep.setType("remove_OK");					
					rep.setData(BFP2P);
					rep.setPath(path);
					rep.setSource(nodeIndex);
					rep.setDestinataire(message.getSource());
					
					t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
					
					/*
					//*******LOG*******
					WriteFile wf = new WriteFile(Config.peerSimLOG, true);
					wf.write("search_OK "+ indexID + " node "+ nodeIndex + "\n"
							+ message.toString()
							+ "\n");
					wf.close();
					//*****************
					*/
				}
			}
			else // serverID != nodeIndex
			{
				/*
				//*******LOG*******
				String s_tmp = new String();
				for (int i = 0; i < Config.numberOfFragment; i++)
				{
					s_tmp += "/"+ BFP2P.getFragment(i).toInt();
				}
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write(date + "       Node " + nodeIndex + " transfer to " + serverID + "\n"
						+ "        BFP2P " + BFP2P.toString() + "\n"
						+ "        BFP2P_path : " + s_tmp + "\n"
						+ "        Path : " + "/" + "\n");
				wf1.close();
				//*****************
				*/
				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
				
				/*
				//*******LOG*******
				WriteFile wf = new WriteFile(Config.peerSimLOG, true);
				wf.write("search racine forward "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf.close();
				//*****************
				*/
			}
		}
		else // path != "/"
		{
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
			
			/*
			//*******LOG*******
			String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

			WriteFile wf = new WriteFile(Config.peerSimLOG_path, true);
			wf.write(date +"       Node " + nodeIndex + " receive paths"
				//	+ message.toString() + "\n"
					+ "\n");
			wf.close();
			//*****************
			*/
			
			if (this.listSystemIndexP2P.containsKey(indexID)) // contient indexID
			{
				SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
									
				//*******LOG*******
				/*	
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write("        Path : " + path_tmp + "\n");
				wf1.close();
				//*****************
				*/
					
				Object o = systemIndex.remove(BFP2P, path);
				
				treatRemove(o, indexName, message, pid);

				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search path "+ indexID+ " node "+ nodeIndex  + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
				*/
			}
			else // !this.listSystemIndexP2P.containsKey(indexID)
			{
				Message rep = new Message();
				rep.setIndexName(indexName);
				rep.setType("remove_OK");
				
				rep.setData(BFP2P);
				rep.setPath(path);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				/*
				//*******LOG*******
				String date1 = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());

				WriteFile wf1 = new WriteFile(Config.peerSimLOG_path, true);
				wf1.write(date1 + "       Node " + nodeIndex + " reply to " + message.getSource() + "\n"
						+ "\n");
				wf1.close();
				//*****************
				*/
				
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
				
				/*
				//*******LOG*******
				WriteFile wf2 = new WriteFile(Config.peerSimLOG, true);
				wf2.write("search_OK path "+ indexID + " node "+ nodeIndex + "\n"
						+ message.toString()
						+ "\n");
				wf2.close();
				//*****************
				*/
			}
		}
	}

	/*
	 * Traiter la réponse du système index
	 * 
	 * il y a 2 cas: null ou message
	 * Pour le message, soit de 'remove' soit de 'removeNode'
	 * */
	
	private void treatRemove(Object o, String indexName, Message message, int pid)
	{
		if (o == null)
			return;
		
		String path = (String) ((Message)o).getData();
		
		ControlerNw.config_log.getTranslate().setLength(Network.size());
		int serverID = ControlerNw.config_log.getTranslate().translate(path);
		
		if ((String) ((Message)o).getOption1() == "remove") // forward
		{
			Message rep = new Message();
			rep.setIndexName(message.getIndexName());
			rep.setData(message.getData());
			rep.setPath(path);
			rep.setType("remove");
			rep.setSource(message.getSource());
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		}
		else // removeNode
		{
			Message rep = new Message();
			rep.setIndexName(indexName);
			rep.setData(message.getData());
			rep.setPath(path);
			rep.setType("removeNode");
			rep.setSource(message.getSource());
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		}	
	}
	
	/*
	 * Traiter le message de suppression d'un nœud
	 * */
	
	private void treatRemoveNode(Message message, int pid)
	{
		String indexName = message.getIndexName();
		BFP2P bf = (BFP2P) message.getData();
		String path = message.getPath();
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		Message rep = new Message();
		
		if (listSystemIndexP2P.containsKey(indexID))
		{
			SystemIndexP2P systemIndex = listSystemIndexP2P.get(indexID);
			
			Object o = systemIndex.removeNode(bf.getFragment((new CalculRangP2P()).getRang(path)), path);
			
			if (o == null)
			{
				rep.setType("removeNode_OK");
				rep.setIndexName(indexName);
				rep.setData("REMOVED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}
			
			ControlerNw.config_log.getTranslate().setLength(Network.size());
			int serverID = ControlerNw.config_log.getTranslate().translate((String)o);
			
			rep.setType("removeNode");
			rep.setIndexName(indexName);
			rep.setData(message.getData());
			rep.setPath((String)o);
			rep.setSource(message.getSource());
			rep.setDestinataire(serverID);
			
			t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
		}
		else // !listSystemIndexP2P.containsKey(indexID)
		{
			rep.setType("removeNode_OK");
			rep.setIndexName(indexName);
			rep.setData("REMOVED_NODE");
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
		}
	}
	
	/*
	 * Capturer l'état du système
	 * */
		
	private void treatOverview(Message message, int pid)
	{	
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(message.getIndexName());
		
		if (this.listSystemIndexP2P.containsKey(indexID))
		{
			// procedure
			SystemIndexP2P systemIndex = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
						
			ControlerNw.config_log.setNodePerServer(nodeIndex, systemIndex.size());		

			Hashtable<String, SystemNodeP2P> htss = systemIndex.getListNode();
			Enumeration<String> htEnumeration = htss.keys();
			
			while (htEnumeration.hasMoreElements())
			{
				String s = htEnumeration.nextElement();
				
				SystemNodeP2P sn = htss.get(s);
				LocalRouteP2P lr = sn.getLocalRoute();
				Enumeration<Integer> lrEnumeration = lr.getKeyAll();
				int j = 0;
				while (lrEnumeration.hasMoreElements())
				{
					Integer i = lrEnumeration.nextElement();
					if (!lr.get(i).getClass().getName().equals("java.lang.String"))
					{
						ContainerLocalP2P cl = (ContainerLocalP2P) lr.get(i);
						j += cl.getNumberOfElements();
					}
				}
				ControlerNw.config_log.getFilterPerNode().put(s, j);
			}
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
				
		//*******************
		WriteFile wf = new WriteFile(Config.peerSimLOG+"_indexHeight", false);
		Enumeration<Integer> enumeration = ControlerNw.config_log.getIndexHeight().keys();
		
		wf.write("Taille du filtre    : " + ControlerNw.config_log.sizeOfBF + "\n");
		wf.write("Nombre de fragments : " + ControlerNw.config_log.numberOfFragment + "\n");
		wf.write("Taille du fragment  : " 
				+ ControlerNw.config_log.sizeOfBF/ControlerNw.config_log.numberOfFragment+ "\n");
		wf.write("Gamma               : " + ControlerNw.config_log.gamma + "\n");
		
		while (enumeration.hasMoreElements())
		{
			Integer i = enumeration.nextElement();
			if (i <= 9)
			{
				wf.write(i + "  " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}
			else
			{
				wf.write(i + " " + ControlerNw.config_log.getIndexHeight().get(i) + "\n");
			}			
		}
		
		wf.close();
		//*******************
		
		int[] tab = new int[10];
		for (int i = 0; i < 10; i++)
			tab[i] = 0;
		
		WriteFile wf1 = new WriteFile(Config.peerSimLOG + "_nodePerServer", true);
		
		int j = 0;
		int k = 0;
		for (int i = 0; i < Network.size(); i++)
		{
			if (ControlerNw.config_log.getNodePerServer(i) == 0)
				continue;
			
			j += ControlerNw.config_log.getNodePerServer(i);

			tab[ControlerNw.config_log.getNodePerServer(i)]++;
			
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
		
		for (int i = 0; i < 10; i++)
		{
			if (tab[i] != 0)
			{
				float tmp = (float) ((float)(tab[i])/(float)k)*100;
				float tmp2 = (float) ((float)(tab[i]*i)/(float)j)*100;
				wf1.write(tab[i] + " pairs contient "+ i + " nœuds = " + tmp + "% de pairs " + tmp2 +"% de nœuds\n");
			}
		}
			
		wf1.close();
		
		wf1 = new WriteFile(Config.peerSimLOG + "_filterPerNode", true);
		
		Enumeration<String> enumeration2 = ControlerNw.config_log.getFilterPerNode().keys();
		
		int i = 0;
		j = 0;
		k = 0;
				
		Hashtable<Integer, ArrayList<String>> hias = new Hashtable<Integer, ArrayList<String>>();
		while (enumeration2.hasMoreElements())
		{
			String s_tmp = enumeration2.nextElement();
			
			WriteFile wf2 = new WriteFile(Config.peerSimLOG + "_node", true);
			wf2.write(s_tmp + "\n");
			wf2.close();
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) == 0)
			{
				i++;
				continue;
			}
			
			j += ControlerNw.config_log.getFilterPerNode().get(s_tmp);
			
			if (i < 10)
			{
				wf1.write(i + "       " + ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp +  "\n");
			}
			else if (i < 100)
			{ 
				wf1.write(i + "      " + ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			else
			{					
				wf1.write(i + "     " + ControlerNw.config_log.getFilterPerNode().get(s_tmp) + "    " + s_tmp + "\n");
			}
			i++;
			k++;
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 1000)
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
		
		int m = hias.get(1000) == null ? 0 : hias.get(1000).size();
		float n = 0;
		
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <1000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		n += (float)((float)m/(float)k)*100;
		m = hias.get(5000) == null ? 0 : hias.get(5000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("  <5000 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(10000) == null ? 0 : hias.get(10000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <10000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(15000) == null ? 0 : hias.get(15000).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write(" <15000 = " + m + "  " + (float)((float)m/(float)k)*100 + "%\n");
		
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
	}
		
}











