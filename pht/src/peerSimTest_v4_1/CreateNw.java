package peerSimTest_v4_1;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class CreateNw implements Control {
	
	public static final String PAR_PROTOCOL = "protocol";
	
	public static final String VERSION = "version";
	
	public static final String SIZE_OF_BF = "sizeOfBF";
	public static final String SIZE_OF_KEY = "sizeOfKey";
	public static final String GAMMA = "gamma";
	public static final String NUMBER_OF_FILTERS_TEST = "numberOfFiltersTest";
	public static final String TYPE_SIMULATION =  "typeSimulation";
	public static final String CURRENT_DIR = "currentDir";
	public static final String EXP = "exp";
	public static final String FILE_WIKI = "fileWiki";
	public static final String FILE_REQUESTS = "fileRequests";
	
	public static String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
	
	private int pid;
	
	public CreateNw(String prefix) {
		// TODO Auto-generated constructor stub
		pid = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
		
		Config.version = Configuration.getString(prefix + "." + VERSION);
		
		Config.sizeOfBF = Configuration.getInt(prefix + "." + SIZE_OF_BF);
		Config.sizeOfKey = Configuration.getInt(prefix + "." + SIZE_OF_KEY);
		Config.gamma = Configuration.getInt(prefix + "." + GAMMA);
		Config.numberOfFiltersTest = Configuration.getInt(prefix + "." + NUMBER_OF_FILTERS_TEST);
		
		Config.currentDir = Configuration.getString(prefix + "." + CURRENT_DIR);
		Config.fileWiki = Configuration.getString(prefix + "." + FILE_WIKI);
		Config.fileRequests = Configuration.getString(prefix + "." + FILE_REQUESTS);
		
		Config.exp = Configuration.getInt(prefix + "." + EXP);
		
		Config.inputDir = Config.currentDir + "input/";
		Config.outputDir = Config.currentDir + "output/";
		Config.peerSimLOG = Config.currentDir + date + "/" + Config.exp + "_" + Config.version+ "/" + "_log";
		Config.peerSimLOG_resultat = Config.currentDir + date + "/" + Config.exp + "_" + Config.version + "/";
		Config.serializerName  = Config.currentDir + Config.version;
		
		Config.typeSimulation = MessageType.valueOf(Configuration.getString(prefix + "." + TYPE_SIMULATION));
	}
	

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		for (int i = 0; i < Network.size(); i++)
		{
			Node n = (Node) Network.get(i);
			PHT_Protocol s = (PHT_Protocol) n.getProtocol(pid);
			s.setNodeIndex(i);
			s.setID(i);
		}
		
		File f = new File(Config.peerSimLOG);
		if (f.exists())
			f.delete();
		f = new File(Config.peerSimLOG_resultat);
		if (f.exists())
			f.delete();
		
		return false;
	}

}
