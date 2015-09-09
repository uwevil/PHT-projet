package peerSimTest_v4_1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Concate {

	public static void ConcateFile(String fileRequests, String folder)
	{
		Config config = new Config();
		try 
		{
			ReadFile rf = new ReadFile(fileRequests);
		
			System.out.println("Begin");

			File f = new File(folder + "_all.xml");
			if (f.exists())
			{
				System.out.println("_all.xml EXISTED");
				System.out.println("End");
				return;
			}
			
			WriteFile wf = new WriteFile(folder + "_all.xml", true);
			wf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<?xml-stylesheet type='text/xsl' href='style.xsl'?>\n<zcode>\n");
			wf.close();
			
			for (int i = 0; i < rf.size(); i++)
			{									
				BF bf = new BF(512);
				bf.addAll(rf.getDescription(i));

				config.getTranslate().setRange(Config.requestRange);
				long requestID = config.getTranslate().translate(bf.toString());
				
				String path_tmp = folder + requestID + "_tmp.xml";
				String path = folder + requestID + ".xml";
				
				BufferedReader reader = new BufferedReader(new FileReader(path));
				
				wf = new WriteFile(folder + "_all.xml", true);
				
				while (true)
				{
					String s = new String();
					s = reader.readLine();
					if (s == null)
						break;
					
					wf.write(s + "\n");
				}

				reader.close();
				
				reader = new BufferedReader(new FileReader(path_tmp));
								
				while (true)
				{
					String s = new String();
					s = reader.readLine();
					if (s == null)
						break;
					
					wf.write(s + "\n");
				}
				wf.write("</request>\n\n");
				wf.close();
				reader.close();
			}
			
			wf = new WriteFile(folder + "_all.xml", true);
			wf.write("</zcode>");
			wf.close();
			
			System.out.println("End");
		} 
		catch (IOException e )
		{
			e.printStackTrace();
		}	
	}
	
	public static void main (String[] args)
	{
		String s = "0";
		String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
		
		ConcateFile("/Users/dcs/vrac/test/wikiDocs<60_500_request",
				"/Users/dcs/vrac/test/"
				+ date + "/" + s + "_peerSim_v4_1/");
	}

}
