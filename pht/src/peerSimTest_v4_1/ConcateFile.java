package peerSimTest_v4_1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConcateFile {

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
			
			for (int i = 0; i < rf.size(); i++)
			{									
				BF bf = new BF(Config.sizeOfBF);
				bf.addAll(rf.getDescription(i));

				config.getTranslate().setRange(Config.requestRange);
				long requestID = config.getTranslate().translate(bf.toString());
				
				String path_tmp = folder + requestID + "_tmp.xml";
				String path = folder + requestID + ".xml";
				
				BufferedReader reader = new BufferedReader(new FileReader(path));
				
				WriteFile wf = new WriteFile(folder + "_all.xml", true);
				
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
			
			System.out.println("End");
		} 
		catch (IOException e )
		{
			e.printStackTrace();
		}	
	}
	
	public static void main (String[] args)
	{
		String s = "17-54-56";
		String date = (new SimpleDateFormat("dd-MM-yyyy")).format(new Date());
		
		ConcateFile(Config.fileRequests, Config.currentDir + date + "/" + s + "_peerSim_v4_1/");
	}

}
