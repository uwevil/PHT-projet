package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

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
		
		
		Hashtable<String, PHT_Node> hashtable = pht.getListNodes();
		
		String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
		Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/" + "_log";
		
		WriteFile wf = new WriteFile(Config.peerSimLOG, false);
		wf.write("Nombre total de nœuds : " + hashtable.size() + "\n");
		wf.close();
		
		Enumeration<String> enumeration = hashtable.keys();
		int total = 0;
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			
			wf = new WriteFile(Config.peerSimLOG, true);
			wf.write(s + "\n");
			
			PHT_Node n = hashtable.get(s);
			
			wf.write(n.getPath());
			
			if (n.getListKeys() != null && n.getListKeys().size() != 0)
			{
				total += n.getListKeys().size();
				wf.write(" : " +n.getListKeys().size() + "\n\n");
			}
			else
			{
				wf.write("\n\n");
			}
			wf.close();
		}
		
		wf = new WriteFile(Config.peerSimLOG, true);
		wf.write("Nombre de filtres totaux : " + total + "\n");
		wf.close();
		/*
		int experience = 0;
		try 
		{
			ReadFile rf = new ReadFile("/Users/dcs/vrac/test/wikiDocs<60_500_request");
			
			int j = 0;
			/*
			String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
			Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "/" + experience + "_log";
			Config.peerSimLOG_resultat = "/Users/dcs/vrac/test/" + date + "/" + experience + "_resultat_log";
			Config.peerSimLOG_path = "/Users/dcs/vrac/test/" + date + "/" + experience + "_path_log";
			*/

	//		for (int i = experience*10; i < rf.size() && j < 10; i++)
	//		{			
		/*		BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(1));
				
				config_log.getTranslate().setLength(Config.requestRang);
				int requestID = config_log.getTranslate().translate(bf.toString());
				
		//		Object res = pht.search(bf);
				
				System.out.println("request : " + bf.toString());
			//	System.out.println(requestID + " : filtres trouvés : " + ((ArrayList<BF>) res).size());
			//	System.out.println("  " + res);
				j++;
	//		}
			experience++;
			
			System.out.println("NOMBRE de requete = " + rf.size());
			
		} 
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
