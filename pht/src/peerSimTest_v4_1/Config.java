package peerSimTest_v4_1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import peersim.core.Network;

public class Config {
	
	/**
	 * Version.
	 * */
	public static String version = "peerSim_v4";

	/**
	 * Liste des réponses identifiée par requestID.
	 * */
	private Hashtable<Long, Object> listAnswers = new Hashtable<Long, Object>();
	
	private Hashtable<Long, Integer> retrieves = new Hashtable<Long, Integer>();
	
	private Hashtable<Long, Integer> getStatus = new Hashtable<Long, Integer>();
	
	private Hashtable<Long, Integer> nbFiltersRetrieved = new Hashtable<Long, Integer>();
	
	public static ArrayList<Long> listRequestID = new ArrayList<Long>();
	
	/**
	 * Nombre de filtres sur chaque nœud dans l'arbre.
	 * */
	private Hashtable<String, Integer> filterPerNode = new Hashtable<String, Integer>();
	
	/**
	 * Le plus long chemin dans l'abre après l'application la fonction {@link skey}.
	 * */
	private Hashtable<Integer, String> realIndexHeight = new Hashtable<Integer, String>();
	
	/**
	 * Le plus long chemin dans l'arbre.
	 * */
	private Hashtable<Integer, String> indexHeight = new Hashtable<Integer, String>();
	
	/**
	 * Temps depuis le lancement de la requête et le temps pour chaque réponse qui contient un filtre correspondant avec la requête
	 *  identifiée par {@code requestID}.
	 * */
	private Hashtable<Long, Object> timeGlobal = new Hashtable<Long, Object>();

	/**
	 * Nombre de nœuds hébergés sur chaque pair dans le réseau.
	 * */
	private int[] nodePerServer = new int[Network.size()];
	
	public static int indexRanqe = 99999999;
	public static int requestRange = 1000000;
	public static boolean ObserverNw_OK = false;
	private NameToID translate = new NameToID(0);

	private boolean config_OK = true;
	
	/**
	 * Taille d'un filtre.
	 * */
	public static int sizeOfBF = 512;
	
	/**
	 * Taille d'une clé.
	 * */
	public static int sizeOfKey = 256;
	
	/**
	 * Nombre de filtres maximums sur un nœud dans l'arbre.
	 * */
	public static int gamma = 1000;
	
	public static int numberOfFiltersTest = 1600;
	
	/**
	 * Nombre de messages dans le réseau sans compter les messages {@code init}, {@code overview}.
	 * */
	public static long numberOfMessages = 0;
	
	public static int numberOfFiltersCreated = 0;
		
	public static int totalFilterAdded = 0;
	
	private long split = 0;

	public static String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
	
	/**
	 * Emplacement pù on stocke les fichiers de log.
	 * */
	public static String peerSimLOG = "/Users/dcs/vrac/test/"+ date + "_" + version+ "/" + "_log";
	
	/**
	 * Emplacement pù on stocke les fichiers temporaires, de résultats, de log.
	 * */
	public static String peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date  + "_" + version + "/";
	
	public static String serializerName = "/Users/dcs/vrac/test/peerSim_v4";
	
	/**
	 * Configuration.
	 * */
	
	public Config()
	{
		for (int i = 0; i < Network.size(); i++)
		{
			nodePerServer[i] = 0;
		}		

		listAnswers = new Hashtable<Long, Object>();
		timeGlobal = new Hashtable<Long, Object>();
		indexHeight = new Hashtable<Integer, String>();
		retrieves = new Hashtable<Long, Integer>();
				
		date = (new SimpleDateFormat("dd/MM/yyyy/HH-mm-ss")).format(new Date());
		nodePerServer = new int[Network.size()];
		
		for (int i = 0; i < Network.size(); i++)
			nodePerServer[i] = 0;
		split = 0;
		config_OK = true;
	}
	
	/**
	 * Ajoute d'un objet dans la liste des requêtes identifiées par {@code requestID}.
	 * */
	public void putListAnswer(Long key, Object value)
	{
		this.listAnswers.put(key, value);
	}
	
	/**
	 * Retourne d'un objet dans la liste des requêtes identifiées par {@code requestID}.
	 * */
	public Object getListAnswer(Long key)
	{
		return this.listAnswers.get(key);
	}
	
	/**
	 * Teste l'existence d'un objet dans la liste des requêtes identifiées par {@code requestID}.
	 * */
	public boolean containsKeyListAnswer(Long key)
	{
		return this.listAnswers.containsKey(key);
	}
	
	/**
	 * Retourne le convertisseur d'une chaîne de caractère en un entier.
	 * */
	public NameToID getTranslate()
	{
		return this.translate;
	}
	
	/**
	 * Ajoute le nombre de nœuds hébergés sur un pair {@code index} dans le réseau.
	 * */
	public synchronized void setNodePerServer(int index, int i)
	{
		if (this.getConfig_OK())
			this.nodePerServer[index] = i;
	}
	
	/**
	 * Retourne le nombre de nœuds hébergés sur un pair {@code index} dans le réseau.
	 * */
	public int getNodePerServer(int index)
	{
		return this.nodePerServer[index];
	}
	
	public synchronized void setConfig_OK(boolean value)
	{
		this.config_OK = value;
	}
	
	public boolean getConfig_OK()
	{
		return this.config_OK;
	}

	/**
	 * Retourne une liste de nœuds avec son nombre de filtres stockés.
	 * */
	public Hashtable<String, Integer> getFilterPerNode()
	{
		return this.filterPerNode;
	}
	
	/**
	 * Retourne une liste de requêtes identifiées par {@code requestID} avec son {@link Object} (pour calculer le temps).
	 * */
	public Hashtable<Long, Object> getTimeGlobal()
	{
		return this.timeGlobal;
	} 
	
	/**
	 * Retourne une liste de différentes hauteurs.
	 * */
	public Hashtable<Integer, String> getIndexHeight()
	{
		return this.indexHeight;
	}
	
	/**
	 * Retourne une liste de différentes hauteurs après l'application de méthode {@link skey}.
	 * */
	public Hashtable<Integer, String> getRealIndexHeight()
	{
		return this.realIndexHeight;
	}
	
	public synchronized void addSplit(long i)
	{
		this.split += i;
	}
	
	public long getSplit()
	{
		return this.split;
	}
	
	public synchronized void addRetrieves(long requestID, int i)
	{
		if (!this.retrieves.containsKey(requestID))
		{
			this.retrieves.put(requestID, i);
		}
		else
		{
			int tmp = this.retrieves.get(requestID) + i;
			this.retrieves.remove(requestID);
			this.retrieves.put(requestID, tmp);
		}
	}
	
	public int getRetrieves(long requestID)
	{
		return this.retrieves.get(requestID);
	}
	
	public synchronized void addGetStatus(long requestID, int i)
	{
		if (!this.getStatus.containsKey(requestID))
		{
			this.getStatus.put(requestID, i);
		}
		else
		{
			int tmp = this.getStatus.get(requestID) + i;
			this.getStatus.remove(requestID);
			this.getStatus.put(requestID, tmp);
		}
	}
	
	public int getGetStatus(long requestID)
	{
		return this.getStatus.get(requestID);
	}
	
	public synchronized void addNbFiltersRetrieved(long requestID, int i)
	{
		if (!this.nbFiltersRetrieved.containsKey(requestID))
		{
			this.nbFiltersRetrieved.put(requestID, i);
		}
		else
		{
			int tmp = this.nbFiltersRetrieved.get(requestID) + i;
			this.nbFiltersRetrieved.remove(requestID);
			this.nbFiltersRetrieved.put(requestID, tmp);
		}
	}
	
	public int getNbFiltersRetrieved(long requestID)
	{
		return this.nbFiltersRetrieved.get(requestID);
	}
}
