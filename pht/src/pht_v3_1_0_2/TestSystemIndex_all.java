package pht_v3_1_0_2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import pht_v3_1_0_1.*;

@SuppressWarnings("unused")
public class TestSystemIndex_all {

	public static Config config_log = new Config();
	
	@SuppressWarnings({ "unchecked"})
	public static void main(String[] args) throws ErrorException
	{
		int line = 0;
		int k = 0;
		PHT pht= new PHT("dcs");
		
		String version = Config.version;
		
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
			//		break;
			}
			reader.close();
			
			System.out.println("Fini de lecture " + k + " lignes.");
			System.out.println("Nombre de filtres réels : " + line + " filtres.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		long time = System.currentTimeMillis();
		System.out.println("Désérialisation");
		pht.serializeListNodes("/Users/dcs/vrac/test/listNodes_" + version);
//		pht.deserializeListNodes("/Users/dcs/vrac/test/listNodes_" + version);
		System.out.println("Fin de désérialisation " + (System.currentTimeMillis() - time) + " ms");
		
		
		Hashtable<String, PHT_Node> listNodes = pht.getListNodes();		
	
		String date = (new SimpleDateFormat("dd-MM-yyyy/HH-mm-ss")).format(new Date());
		Config.peerSimLOG = "/Users/dcs/vrac/test/"+ date + "_" + version + "/";
		
		WriteFile wf = new WriteFile(Config.peerSimLOG + "_" + version, false);
		wf.close();
		
		Enumeration<String> enumeration = listNodes.keys();
		int total = 0;
		int nbLeafs = 0;
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			
			wf = new WriteFile(Config.peerSimLOG + "_" + version, true);
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
		
		wf = new WriteFile(Config.peerSimLOG + "_" + version, true);
		wf.write("Nombre total de filtres  : " + total + "\n");
		wf.write("Nombre total de nœuds    : " + listNodes.size() + "\n");
		wf.write("Nombre total de feuilles : " + nbLeafs + "\n");
		wf.close();
				
		int experience = 0;
		try 
		{
			ReadFile rf = new ReadFile("/Users/dcs/vrac/test/wikiDocs<60_500_request");
									
			while (experience < 50)
			{
				Config.peerSimLOG_resultat = Config.peerSimLOG + experience + "_resultat_log";

				int j = 0;
				for (int i = experience*10; i < rf.size() && j < 10; i++)
				{			
					BF bf = new BF(Config.sizeOfBF);
					bf.addAll(rf.getDescription(i));
					
					config_log.getTranslate().setLength(Config.requestRang);
					int requestID = config_log.getTranslate().translate(bf.toString());
					
					long temps = Calendar.getInstance().getTimeInMillis();
					
					Object res = pht.search(bf);
					
					temps = Calendar.getInstance().getTimeInMillis() - temps;
					
					Hashtable<Integer, Object> hashtable = 
							(Hashtable<Integer, Object>) config_log.getListAnswer(requestID);
					ArrayList<String> arrayList = (ArrayList<String>) hashtable.get(requestID);
					
					wf = new WriteFile(Config.peerSimLOG_resultat + "_path_" + requestID, true);
	
					for (int l = 0; l < arrayList.size(); l++)
					{
						wf.write(arrayList.get(l) + "\n");
					}
					wf.close();
					
					if (((ArrayList<BF>) res).size() != 0)
					{	
						wf = new WriteFile(Config.peerSimLOG_resultat + "_" + requestID, true);
						wf.write(rf.getDescription(i) + "\n");
						wf.write("request : " + bf.toString() + "\n\n");
						
						if (temps >= 1000)
						{
							long m = temps;
							long hours = TimeUnit.MILLISECONDS.toHours(m);
							m -= TimeUnit.HOURS.toMillis(hours);
							long minutes = TimeUnit.MILLISECONDS.toMinutes(m);
							m -= TimeUnit.MINUTES.toMillis(minutes);
							long seconds = TimeUnit.MILLISECONDS.toSeconds(m);
							m -= TimeUnit.SECONDS.toMillis(seconds);
							
							wf.write("Temps de recherche      : " + time + "ms == " 
									+ hours + ":" + minutes + ":" + seconds + "." + m
									+ "\n");
						}
						else
						{
							wf.write("Temps de recherche      : " + temps + "ms\n");
						}
						
						wf.write("Nombre de nœuds visités : " + arrayList.size() + "\n");
						wf.write("Filtres trouvés         : " + ((ArrayList<BF>) res).size() + "\n");
						wf.write("  " + res + "\n");
						wf.close();
					}
					else // non trouvé
					{
						wf = new WriteFile(Config.peerSimLOG_resultat + "_null_" + requestID, true);
						wf.write(rf.getDescription(i) + "\n");
						wf.write("request : " + bf.toString() + "\n\n");
						
						if (temps >= 1000)
						{
							long m = temps;
							long hours = TimeUnit.MILLISECONDS.toHours(m);
							m -= TimeUnit.HOURS.toMillis(hours);
							long minutes = TimeUnit.MILLISECONDS.toMinutes(m);
							m -= TimeUnit.MINUTES.toMillis(minutes);
							long seconds = TimeUnit.MILLISECONDS.toSeconds(m);
							m -= TimeUnit.SECONDS.toMillis(seconds);
							
							wf.write("Temps de recherche      : " + time + "ms == " 
									+ hours + ":" + minutes + ":" + seconds + "." + m
									+ "\n");
						}
						else
						{
							wf.write("Temps de recherche      : " + temps + "ms\n");
						}
						
						wf.write("Nombre de nœuds visités : " + arrayList.size() + "\n");
						wf.write("Filtres trouvés         : " + ((ArrayList<BF>) res).size() + "\n");
						wf.close();
					}
					
					wf = new WriteFile(Config.peerSimLOG + "_log_time_" + version, true);
					if (requestID < 10000)
					{
						wf.write(requestID + "    ");
						
						if (arrayList.size() < 100)
						{
							wf.write(arrayList.size() + "    " + temps + "ms\n");
						}
						else if ( arrayList.size() < 1000)
						{
							wf.write(arrayList.size() + "   " + temps + "ms\n");
						}
						else if (arrayList.size() < 10000)
						{
							wf.write(arrayList.size() + "  " + temps + "ms\n");
						}
						else if (arrayList.size() < 100000)
						{
							wf.write(arrayList.size() + " " + temps + "ms\n");
						}
					}
					else if (requestID < 100000)
					{
						wf.write(requestID + "   ");
						
						if (arrayList.size() < 100)
						{
							wf.write(arrayList.size() + "    " + temps + "ms\n");
						}
						else if ( arrayList.size() < 1000)
						{
							wf.write(arrayList.size() + "   " + temps + "ms\n");
						}
						else if (arrayList.size() < 10000)
						{
							wf.write(arrayList.size() + "  " + temps + "ms\n");
						}
						else if (arrayList.size() < 100000)
						{
							wf.write(arrayList.size() + " " + temps + "ms\n");
						}
					}
					else if (requestID < 1000000)
					{
						wf.write(requestID + "  ");
						
						if (arrayList.size() < 100)
						{
							wf.write(arrayList.size() + "    " + temps + "ms\n");
						}
						else if ( arrayList.size() < 1000)
						{
							wf.write(arrayList.size() + "   " + temps + "ms\n");
						}
						else if (arrayList.size() < 10000)
						{
							wf.write(arrayList.size() + "  " + temps + "ms\n");
						}
						else if (arrayList.size() < 100000)
						{
							wf.write(arrayList.size() + " " + temps + "ms\n");
						}
					}
					wf.close();
					
					j++;
				}
				experience++;
			}

			
			System.out.println("NOMBRE de requete = " + rf.size());
			
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
/*
0000000000
0000000000
0000000000
0000000000
0000000000
000000    56

00000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000
00000000000000000000000000000000000000000000000000
0000000000
00000
*/