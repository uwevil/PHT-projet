package pht_v3_1_0_2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import peersim.core.Network;

public class Config {

	private ArrayList<String> nodeMatched = new ArrayList<String>();
	private Hashtable<Integer, String> indexHeight = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> realIndexHeight = new Hashtable<Integer, String>();
	private Hashtable<Integer, Object> listAnswers = new Hashtable<Integer, Object>();
	private Hashtable<String, Integer> filterPerNode = new Hashtable<String, Integer>();
	private Hashtable<Integer, Long> timeGlobal = new Hashtable<Integer, Long>();
	private Hashtable<Integer, Long> timeCalcul = new Hashtable<Integer, Long>();
	private int[] nodePerServer = new int[Network.size()];
	
	public static String version = "v3_1_0_2";
	
	public static int indexRand = 99999999;
	public static int requestRang = 1000000;
	public static int sizeOfBF = 512;
	public static int sizeOfKey = 256;
	public static int gamma = 1000;
	public static boolean ObserverNw_OK = false;

	private NameToID translate = new NameToID(0);
	private boolean end_OK = false;
	private boolean config_OK = true;
	
	private long time_calcul = 0;
	private long time_route = 0;

	private int nodeVisited = 0;
	private int totalFilterCreated = 0;
	private long time = 0;
	private int numberOfFilter = 0;
	
	private boolean experience_OK = true;
	
	public static String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
	public static String peerSimLOG = "/Users/dcs/vrac/test/"+ date + "_log";
	public static String peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date+ "_resultat_log";
	public static String peerSimLOG_path = "/Users/dcs/vrac/test/" + date + "_path_log";
	
	public Config()
	{
		for (int i = 0; i < Network.size(); i++)
		{
			nodePerServer[i] = 0;
		}
		timeCalcul = new Hashtable<Integer, Long>();
		timeGlobal = new Hashtable<Integer, Long>();
		
		nodeVisited = 0;
		nodeMatched = new ArrayList<String>();
		indexHeight = new Hashtable<Integer, String>();
		realIndexHeight = new Hashtable<Integer, String>();
		listAnswers = new Hashtable<Integer, Object>();
		date = (new SimpleDateFormat("dd/MM/yyyy/HH-mm-ss")).format(new Date());
		time = 0;
		totalFilterCreated = 0;
		nodePerServer = new int[Network.size()];
		
		for (int i = 0; i < Network.size(); i++)
			nodePerServer[i] = 0;
		
		numberOfFilter = 0;
		
		time_calcul = 0;
		time_route = 0;
		
		config_OK = true;
	}
	
	public synchronized void addTotalFilterCreated(int i)
	{
		if (this.getConfig_OK())
			this.totalFilterCreated += i;
	}
	
	public int getTotalFilterCreated()
	{
		return this.totalFilterCreated;
	}
	
	public synchronized void addNumberOfFilters(int i)
	{
		this.numberOfFilter += i;
	}
	
	public int getNumberOfFilters()
	{
		return this.numberOfFilter;
	}
	
	public synchronized void setTime(long time)
	{
		this.time = time;
	}
	
	public long getTime()
	{
		return this.time;
	}
	
	public synchronized void addNodeVisited(int i)
	{
		this.nodeVisited += i;
	}
	
	public int getNodeVisited()
	{
		return this.nodeVisited;
	}
	
	public void putListAnswer(Integer key, Object value)
	{
		this.listAnswers.put(key, value);
	}
	
	public Object getListAnswer(Integer key)
	{
		return this.listAnswers.get(key);
	}
	
	public boolean containsKeyListAnswer(Integer key)
	{
		return this.listAnswers.containsKey(key);
	}
	
	public void removeListAnswer(Integer key)
	{
		this.listAnswers.remove(key);
	}
	
	public void addNodeMatched(String s)
	{
		if (this.nodeMatched.contains(s))
			return;
		
		this.nodeMatched.add(s);
	}
	
	public ArrayList<String> getNodeMatched()
	{
		return this.nodeMatched;
	}
	
	public int sizeNodeMatched()
	{
		return this.nodeMatched.size();
	}
	
	public NameToID getTranslate()
	{
		return this.translate;
	}
	
	public synchronized void setNodePerServer(int index, int i)
	{
		if (this.getConfig_OK())
			this.nodePerServer[index] = i;
	}
	
	public int getNodePerServer(int index)
	{
		return this.nodePerServer[index];
	}
	
	public Hashtable<Integer, String> getIndexHeight()
	{
		return this.indexHeight;
	}
	
	public Hashtable<Integer, String> getRealIndexHeight()
	{
		return this.realIndexHeight;
	}
	
	public synchronized void setExperience_OK(boolean val)
	{
		this.experience_OK = val;
	}
	
	public boolean getExperience_OK()
	{
		return this.experience_OK;
	}
	
	public Hashtable<Integer, Long> getTimeGlobal()
	{
		return this.timeGlobal;
	}
	
	public synchronized void addTimeCalcul(Integer key, long t)
	{
		if (this.timeCalcul.containsKey(key))
		{
			long tmp = this.timeCalcul.get(key);
			this.timeCalcul.remove(key);
			this.timeCalcul.put(key, tmp + t);
		}
		else
		{
			this.timeCalcul.put(key, t);
		}
	}
	
	public long getTimeCalcul(Integer key)
	{
		return (this.timeCalcul.get(key) == null ? 0 : this.timeCalcul.get(key));
	}
	
	public synchronized void setEnd_OK(boolean val)
	{
		this.end_OK = val;
	}
	
	public boolean getEnd_OK()
	{
		return this.end_OK;
	}
	
	public synchronized void setConfig_OK(boolean value)
	{
		this.config_OK = value;
	}
	
	public boolean getConfig_OK()
	{
		return this.config_OK;
	}
	
	public synchronized void addTime_calcul(long t)
	{
		this.time_calcul += t;
	}
	
	public long getTime_calcul()
	{
		return this.time_calcul;
	}
	
	public synchronized void addTime_route(long t)
	{
		this.time_route += t;
	}
	
	public long getTime_route()
	{
		return this.time_route;
	}
	
	public Hashtable<String, Integer> getFilterPerNode()
	{
		return this.filterPerNode;
	}
}
