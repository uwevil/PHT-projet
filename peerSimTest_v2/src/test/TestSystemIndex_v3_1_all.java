package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import peerSimTest_v3_1.*;

public class TestSystemIndex_v3_1_all {

	public static void main(String[] args) throws ErrorException
	{
		int line = 0;
		int i = 0;
		PHT pht= new PHT("dcs");
		
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
					
		//			System.out.println(key);
					line++;
					pht.insert(key);
					
		//			if (line == 11375)
		//				System.out.println(key.toString());
				}
				
				System.out.println(line + " " + i++);	
			//	if (line == 16000)
			//		break;
			}
			reader.close();
			
			System.out.println("Fini de lecture " + line + " lignes");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		Hashtable<String, PHT_Node> hashtable = pht.getListNodes();
		System.out.println(hashtable.size());
		/*
		Enumeration<String> enumeration = hashtable.keys();
		
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			System.out.println(s);
		//	if (n.getListKeys() != null)
			//	System.out.println("   " + n.getListKeys().toString());
		}
		
		hashtable = pht.getListNodes();
		
		enumeration = hashtable.keys();
		
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			PHT_Node n = hashtable.get(s);
			if (n.getListKeys() != null && n.getListKeys().size() != 0)
			{
				System.out.println(" " + s);
				System.out.println(n.getListKeys().size());
			}
		}
		*/
	}

}
