package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import peerSimTest_v3_1.*;

public class TestSystemIndex_v3_1_all {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ErrorException
	{
		int line = 0;
		int k = 0;
		PHT pht= new PHT("dcs");
		
		Config config_log = new Config();
		
		System.out.println("Lecture wiki");
		
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
					BF key = new BF(Config.sizeOfBF);
					key.addAll(tmp[1]);
					
					line++;
					pht.insert(key);					
				}
				k++;
				System.out.println(line + "/" + k);
		//		if (line == 1600)
		//			break;
			}
			reader.close();
			
			System.out.println("Fini de lecture " + k + " lignes.");
			System.out.println("Nombre de filtres réels : " + line + " filtres.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	
	//	Hashtable<String, PHT_Node> listNodes = pht.getListNodes();
		
	//	String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
	//	Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/" + "_log";
		
	//	WriteFile wf = new WriteFile(Config.peerSimLOG, false);
	//	wf.close();
		/*
		Enumeration<String> enumeration = listNodes.keys();
		int total = 0;
		int nbLeafs = 0;
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			
			wf = new WriteFile(Config.peerSimLOG, true);
			wf.write(s + "\n");
			
			PHT_Node n = listNodes.get(s);
			
			wf.write(n.getPath());
			
			if (n.getListKeys() != null && n.getListKeys().size() != 0)
			{
				nbLeafs++;
				total += n.getListKeys().size();
				wf.write(" : " +n.getListKeys().size() + "\n\n");
			}
			else
			{
				wf.write(" : noeud\n\n");
			}
			wf.close();
		}
		/*
		wf = new WriteFile(Config.peerSimLOG, true);
		wf.write("Nombre total de filtres  : " + total + "\n");
		wf.write("Nombre total de nœuds    : " + listNodes.size() + "\n");
		wf.write("Nombre total de feuilles : " + nbLeafs + "\n");
		wf.close();
		*/
		
		pht.serializeListNodes("/Users/dcs/vrac/test/listNodes");
		
	//	pht.deserializeListNodes("/Users/dcs/vrac/test/listNodes");
		
		try 
		{
			ReadFile rf = new ReadFile("/Users/dcs/vrac/test/wikiDocs<60_500_request");
						
			String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
			Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/";
			
			for (int i = 0; i < rf.size(); i++)
			{			
				BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(i));
				
				config_log.getTranslate().setLength(Config.requestRang);
				int requestID = config_log.getTranslate().translate(bf.toString());
				
				Object res = pht.search(bf);
				
				WriteFile wf = new WriteFile(Config.peerSimLOG + requestID + "_requete", true);
				wf.write(rf.getDescription(i) + "\n");
				wf.write("request : " + bf.toString() + "\n\n");
				wf.close();
				
				wf = new WriteFile(Config.peerSimLOG + requestID + "_resultat", true);
				wf.write(requestID + " : filtres trouvés : " + ((ArrayList<BF>) res).size() + "\n");
				wf.write("  " + res + "\n");
				wf.close();
			}
			
			System.out.println("NOMBRE de requete = " + rf.size());
			
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
