package peerSimTest_v2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

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
		int key = message.getRequestID();

		if (message.getData() != null && ((HashSet<BFP2P>)message.getData()) != null)
		{
			HashSet<BFP2P> data1 = (HashSet<BFP2P>)message.getData();
			
			if (!data1.isEmpty())
			{
				if (ControlerNw.search_log.get(key) == null)
				{
					System.out.println("error(treatSearch_OK) : search_log == null");
					return;
				}

				//*************Compter le nombre de filtres trouvés pour cette requête
				ControlerNw.search_log.get(key).addNumberOfFilters(data1.size());
				//********************************************************************
				
				//*************LOG le résultat******************
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_"+key, true);

				Iterator<BFP2P> iterator = data1.iterator();
				
				while (iterator.hasNext())
				{
					wf.write(((new BFP2P())
							.pathToBF(message.getPath(), 0, Config.numberOfFragment, Config.sizeOfFragment)).toString());
					wf.write(iterator.next().toString() + "\n");
				}
				wf.close();
				
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + key, true);
				wf1.write(date + "       Source : " + message.getSource() + "\n");
				wf1.write("                              " 
							+ ControlerNw.search_log.get(key).getNumberOfFilters() + " (" + data1.size() +")\n");
				wf1.close();
				//***********************************************
			}
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

			/*
			System.out.println("\n\n");
			Enumeration<Integer> test = ControlerNw.search_log.keys();
			while (test.hasMoreElements())
				System.out.println(test.nextElement());
				*/
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
		
		BFP2P data1 = (BFP2P) message.getOption();
		
		int key = message.getRequestID();
		
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

	/**
	 * Traiter le message reçu appellé par treatSearch_OK(recherche globale)
	 * <p>
	 * retourner true si il n'y a plus de réponses en attente, false sinon
	 * */
	
	private boolean treatListAnswer(Message message)
	{		
		int key = message.getRequestID();
		
		// si cette requête a été enregistrée avant le lancement de la recherche
		if (ControlerNw.search_log.get(key).containsKeyListAnswer(key))
		{
			Object[] o = (Object[]) ControlerNw.search_log.get(key).getListAnswer(key);
			int[] received = (int[])o[0];
			int[] total = (int[])o[1];
			
			if (message.getSource() != nodeIndex) // si le serveur répond n'est pas l'enquêteur
				received[message.getSource()] += 1;			
			
			if (message.getOption() == null) 
			{
				return testOK(received, total, Network.size());
			}
			else
			{
				String s = (String) message.getOption();
				String[] path_tmp = s.split(";");
				
				if (path_tmp.length < 1)
				{
					return testOK(received, total, Network.size());
				}
				else
				{
					ControlerNw.config_log.getTranslate().setLength(Network.size());
					for (int i = 0; i < path_tmp.length; i++)
					{
						if (path_tmp[i].length() >= 1)
						{
							int j = ControlerNw.config_log.getTranslate().translate(path_tmp[i]);
							
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
	
	/**
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
	
	/**
	 * Traiter le message de création du nœud
	 * <p>
	 * Ce message contient : 
	 * <ul>
	 * 	<li> indexName
	 * 	<li> path
	 * 	<li> {@code HashSet<BFP2P>}
	 * </ul>
	 * */
	
	@SuppressWarnings("unchecked")
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
				systemIndex = new SystemIndexP2P(indexName, serverID);
				
				this.listSystemIndexP2P.put(indexID, systemIndex);				
			}
			else // listSystemIndexP2P.containsKey(indexID)
			{
				systemIndex = this.listSystemIndexP2P.get(indexID);			
			}
			
			SystemNodeP2P systemNode = new SystemNodeP2P(serverID, path, Config.gamma);
			
			systemIndex.addSystemNodeP2P(path, systemNode);
			
			HashSet<BFP2P> c = (HashSet<BFP2P>) message.getData();
			Iterator<BFP2P> iterator = c.iterator();
			
			while (iterator.hasNext())
			{
				BFP2P BFP2P = (BFP2P)iterator.next();
				Object o = systemIndex.add(BFP2P, path);
				treatAdd(o, indexName, path, pid);
			}
		}
		else // serverID != nodeIndex
		{
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
		}
	}

	/**
	 * Traiter le message d'ajout dans le système.
	 * <p>
	 * Ce message contient : 
	 * <ul>
	 * 	<li> indexName
	 * 	<li> Path
	 * 	<li> BF
	 * </ul>
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
				treatAdd(o, indexName, path, pid);
			}
			else // serverID != nodeIndex
			{				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
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
					SystemIndexP2P systemIndex = new SystemIndexP2P(indexName, serverID);
					systemIndex.add((BFP2P)message.getData(), path);
					
					this.listSystemIndexP2P.put(indexID, systemIndex);
				}
				else // this.listSystemIndexP2P.containsKey(indexID)
				{
					SystemIndexP2P systemIndex = (SystemIndexP2P)this.listSystemIndexP2P.get(indexID);
					
					Object o = systemIndex.add((BFP2P)message.getData(), path);
					treatAdd(o, indexName, path, pid);
				}
			}
			else // serverID != nodeIndex
			{				
				t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
			}
		}
	}
	
	/**
	 * Traiter les cas de la réponse du nœud du système
	 * <p>
	 * 3 cas sont:
	 * <ul>
	 * 	<li> soit {@link null}
	 * 	<li> soit {@link Message}
	 * 	<li> soit {@code Hashtable<String, HashSet<BFP2P>>}.
	 * </ul>
	 * */
	
	private void treatAdd(Object o, String indexName, String path, int pid)
	{
		if (o == null)
			return;
		
		if (o.getClass().getName().contains("Message")) // adresse d'un nœud
		{			
			Message rep = new Message();
			rep.setType("add");
			rep.setIndexName(indexName);
			rep.setPath(((Message)o).getPath());
			rep.setData(((Message)o).getData());
			rep.setSource(nodeIndex);
			rep.setDestinataire(((Message)o).getDestinataire());
			
			t.send(Network.get(nodeIndex), Network.get(((Message)o).getDestinataire()), rep, pid);
		}
		else // split()
		{
			@SuppressWarnings("unchecked")
			Hashtable<String, HashSet<BFP2P>> o_tmp = (Hashtable<String, HashSet<BFP2P>>)o;
			
			Enumeration<String> enumeration = o_tmp.keys();
			
			ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
			int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
						
			while (enumeration.hasMoreElements())
			{
				String path_tmp = enumeration.nextElement();
			
				ControlerNw.config_log.getTranslate().setLength(Network.size());
				int serverID = ControlerNw.config_log.getTranslate().translate(path_tmp);
				
				this.listSystemIndexP2P.get(indexID).addPathNodeID(path, path_tmp, serverID);
				
				Message rep = new Message();
				rep.setType("createNode");
				rep.setIndexName(indexName);
				rep.setPath(path_tmp);
				rep.setSource(nodeIndex);
				rep.setDestinataire(serverID);
				rep.setData(o_tmp.get(path_tmp));
								
				t.send(Network.get(nodeIndex), Network.get(serverID), rep, pid);
			}
		}
	}
			
	/**
	 * Traiter le message de la recherche
	 * <p>
	 * Ce message contient : 
	 * <ul>
	 * 	<li> indexName
	 * 	<li> {@code Hashtable<String, BFP2P>}
	 * </ul>
	 * 	
	 * */
	@SuppressWarnings("unchecked")

	private void treatSearch(Message message, int pid)
	{		
		String indexName = message.getIndexName();
		
		long temps = Calendar.getInstance().getTimeInMillis();

		if ((message.getData()) == null)
		{
			Message rep = new Message();
			rep.setType("search_KO");
			rep.setIndexName(indexName);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setData("list path ou path == null");
			rep.setRequestID(message.getRequestID());
			
			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
						
			return;
		}
				
		if (nodeIndex == message.getSource())
		{
			int requestID = message.getRequestID();
									
			if (!ControlerNw.search_log.containsKey(requestID))
			{
				Config config = new Config();
				config.setTime(Calendar.getInstance().getTimeInMillis());
				
				Object[] o = new Object[2];
				int[] recu = new int[Network.size()];
				int[] total = new int[Network.size()];
				
				for (int i = 0; i < Network.size(); i++)
				{
					recu[i] = 0;
					total[i] = 0;
				}
				recu[nodeIndex] = -1;
				total[nodeIndex] = -1;
				
				o[0] = recu;
				o[1] = total;
				
				config.putListAnswer(requestID, o);
				ControlerNw.search_log.put(requestID, config);
			}
		}
		
		ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
		int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
		
		if (this.listSystemIndexP2P.containsKey(indexID))
		{
			SystemIndexP2P systemIndexP2P = (SystemIndexP2P) this.listSystemIndexP2P.get(indexID);
			
			Hashtable<String, BFP2P> htsbf = (Hashtable<String, BFP2P>)message.getData();
			
			Enumeration<String> enumeration = htsbf.keys();
			
			while(enumeration.hasMoreElements())
			{
				String path_tmp = enumeration.nextElement();
				Object o = systemIndexP2P.search(htsbf.get(path_tmp), path_tmp);
				treatSearch(o, indexName, path_tmp, message, pid);
			}
		}
		
		//******************Temps de calcul**********************
		temps = Calendar.getInstance().getTimeInMillis() - temps;
		// cette variable est vidée une fois on a reçu toutes les réponses
		ControlerNw.search_log.get(message.getRequestID()).addTime_calcul(temps); 
		// cette variable est permanante
		ControlerNw.config_log.addTimeCalcul(message.getRequestID(), temps);
		//*******************************************************
	}
	
	/**
	 * Traiter la valeur retournée par le système
	 * <p>
	 * Il y a 2 cas, null ou non null
	 * si non null, le message contient une liste de filtres trouvés et une liste des servers à tranférer la requête
	 * */
	
	@SuppressWarnings("unchecked")
	private void treatSearch(Object o, String indexName, String path, Message message, int pid)
	{
		ControlerNw.search_log.get(message.getRequestID()).addNodeVisited(1);
		if (o == null)
		{
			if (message.getSource() != nodeIndex)
			{
				Message rep = new Message();
				rep.setType("search_OK");
				rep.setIndexName(indexName);
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
				rep.setData(null);
				rep.setRequestID(message.getRequestID());
			
				t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
			}
			return;
		}
		
		if (message.getSource() == nodeIndex) // c'est bien l'enquêteur
		{
			HashSet<BFP2P> resultat = (HashSet<BFP2P>)((Object[])o)[0];

			//***************LOG*************************
			
			if (!resultat.isEmpty())
			{
				WriteFile wf = new WriteFile(Config.peerSimLOG_resultat + "_" + message.getRequestID(), true);

				Iterator<BFP2P> iterator = resultat.iterator();
				
				while (iterator.hasNext())
				{
					wf.write(((new BFP2P()).pathToBF(path, 0, Config.numberOfFragment, Config.sizeOfFragment))
							.toString());
					wf.write(((BFP2P)iterator.next()).toString() + "\n");
				}
				wf.close();
				
				//*************Compter le nombre de filtres trouvés pour cette requête
				ControlerNw.search_log.get(message.getRequestID()).addNumberOfFilters(resultat.size());
				//********************************************************************
				
				String date = (new SimpleDateFormat("mm-ss-SSS")).format(new Date());
				
				WriteFile wf1 = new WriteFile(Config.peerSimLOG_resultat + "_node_" + message.getRequestID(), true);
				wf1.write(date + "       Source : " + message.getSource() + "\n");
				wf1.write("                              " 
							+ ControlerNw.search_log.get(message.getRequestID()).getNumberOfFilters() 
							+ " (" + resultat.size() +")\n");
				wf1.close();
			}
			//*******************************************
			
			Hashtable<Integer, Hashtable<String, BFP2P>> tmp = 
					(Hashtable<Integer, Hashtable<String, BFP2P>>)((Object[])o)[1];
			Enumeration<Integer> enumeration = tmp.keys();
			
			while (enumeration.hasMoreElements())
			{
				Integer i = enumeration.nextElement();
				
				if (i == nodeIndex)
				{
					ControlerNw.config_log.getTranslate().setLength(Config.indexRand);
					int indexID = ControlerNw.config_log.getTranslate().translate(indexName);
					
					Hashtable<String, BFP2P> h_tmp = (Hashtable<String, BFP2P>)tmp.get(i);
					
					Enumeration<String> s_tmp = h_tmp.keys();

					while (s_tmp.hasMoreElements())
					{
						String tmp2 = s_tmp.nextElement();
						Object o2 = (this.listSystemIndexP2P.get(indexID)).search(h_tmp.get(tmp2), tmp2);		
						
						Message rep = new Message();
						
						rep.setType("search");
						rep.setIndexName(indexName);
						rep.setPath(tmp2);
						rep.setSource(nodeIndex);
						rep.setRequestID(message.getRequestID());

						this.treatSearch(o2, indexName, tmp2, rep, pid);
					}
					continue;
				}
				
				Hashtable<String, BFP2P> h_tmp = (Hashtable<String, BFP2P>)tmp.get(i);
				
				Enumeration<String> s_tmp = h_tmp.keys();
				int j = 0;
				while (s_tmp.hasMoreElements())
				{
					s_tmp.nextElement();
					j++;					
				}
				
				Object[] o_tmp = (Object[]) (ControlerNw.search_log
						.get(message.getRequestID())).getListAnswer(message.getRequestID());
								
				int[] total = (int[]) o_tmp[1];
				total[i] += j;
				
				Message rep = new Message();
				
				rep.setType("search");
				rep.setIndexName(indexName);
				rep.setPath(path);
				rep.setData(h_tmp);
				rep.setSource(message.getSource());
				rep.setDestinataire(i);
				rep.setRequestID(message.getRequestID());
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
		}
		else // message.getSource() != nodeIndex, répond au l'enquêteur
		{
			HashSet<BFP2P> resultat = (HashSet<BFP2P>)((Object[])o)[0];
			Hashtable<Integer, Hashtable<String, BFP2P>> tmp = 
					(Hashtable<Integer, Hashtable<String, BFP2P>>)((Object[])o)[1];
			Enumeration<Integer> enumeration = tmp.keys();

			String s = new String();
			while (enumeration.hasMoreElements())
			{
				Integer i = enumeration.nextElement();
				
				Hashtable<String, BFP2P> h_tmp = (Hashtable<String, BFP2P>)tmp.get(i);
				
				Enumeration<String> s_tmp = h_tmp.keys();
				while (s_tmp.hasMoreElements())
				{
					s += s_tmp.nextElement() + ";";
				}
				
				Message rep = new Message();
				
				rep.setType("search");
				rep.setIndexName(indexName);
				rep.setData(h_tmp);
				rep.setSource(message.getSource());
				rep.setDestinataire(i);
				rep.setRequestID(message.getRequestID());
				
				t.send(Network.get(nodeIndex), Network.get(i), rep, pid);
			}
			
			//************************LOG********************************
			if (!resultat.isEmpty())
				ControlerNw.search_log.get(message.getRequestID()).addNodeMatched(path);
			//************************************************************
			
			Message rep = new Message();
			
			rep.setType("search_OK");
			rep.setIndexName(indexName);
			rep.setPath(path);
			rep.setData(resultat);
			rep.setSource(nodeIndex);
			rep.setDestinataire(message.getSource());
			rep.setRequestID(message.getRequestID());
			rep.setOption(s);

			t.send(Network.get(nodeIndex), Network.get(message.getSource()), rep, pid);
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

	/**
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
			}
			else // not contains indexID => create
			{
				SystemIndexP2P systemIndex =  new SystemIndexP2P(indexName, serverID);
				systemIndex.createRoot();
				
				listSystemIndexP2P.put(indexID,systemIndex);
								
				Message rep = new Message();
				rep.setType("createIndex_OK");
				rep.setIndexName(indexName);
				rep.setData("CREATED");
				rep.setSource(nodeIndex);
				rep.setDestinataire(message.getSource());
								
				t.send(Network.get(nodeIndex), Network.get((int)message.getSource()), rep, pid);
			}	
		}
		else // forward cad serverID != nodeIndex
		{ 
			t.send(Network.get(nodeIndex), Network.get(serverID), message, pid);
		}
	}
	
	/**
	 * Traiter le message de suppression d'un système index
	 * 
	*le nœud qui gère la racine diffuse aux autres pour supprimer le système d'index
	**/
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
			
			Object o = systemIndex.removeNode(bf.getFragment(
					(new CalculRangP2P()).getRang(path), Config.sizeOfFragment), path);
			
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
	
	/**
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
				
				HashSet<BFP2P> containerLocal = sn.getLocalContainer();
				ControlerNw.config_log.getFilterPerNode().put(s, containerLocal.size());
				
				if (!ControlerNw.config_log.getIndexHeight().contains((new CalculRangP2P()).getRang(s)))
					ControlerNw.config_log.getIndexHeight().put((new CalculRangP2P()).getRang(s), s);
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
		wf.write("Gamma               : " + ControlerNw.config_log.gamma + "\n\n");
		
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
			ControlerNw.config_log.getTranslate().setLength(Network.size());
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
			
			if (ControlerNw.config_log.getFilterPerNode().get(s_tmp) < 100)
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

		int m = hias.get(100) == null ? 0 : hias.get(100).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <100 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(200) == null ? 0 : hias.get(200).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <200 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		
		m = hias.get(500) == null ? 0 : hias.get(500).size();
		n += (float)((float)m/(float)k)*100;
		wf1.write("   <500 = " + m + " " + (float)((float)m/(float)k)*100 + "%\n");
		if (m > 0)
			wf1.write("                     " + hias.get(500).toString() + "\n");
		
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
	}
		
}











