package peerSimTest_v3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadFile {

	private ArrayList<String> docUrl = new ArrayList<String>(); 
	private ArrayList<String> description = new ArrayList<String>();
	private int size = 0;
	
	public ReadFile(String file) throws FileNotFoundException 
	{
		long time = System.currentTimeMillis();
		
		time = System.currentTimeMillis();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String s;
			while ((s = reader.readLine()) != null)
			{
				String[] tmp = s.split(";");
				docUrl.add(tmp[0]);
				description.add(tmp[1]);
			}
			reader.close();

			System.out.println("Temps de lecture le fichier de test: " 
							+ (System.currentTimeMillis() - time)/(1000) + " s");
			this.size = docUrl.size();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void addData(String[][] s)
	{
		if (s.length == 0)
			return;
		
		for (int i = 0; i < s.length; i++)
		{
			if (s[i].length == 0)
				return;
			docUrl.add(s[i][0]);
			description.add(s[i][1]);
			this.size = docUrl.size();
		}
		return;
	}
	
	public void addData(String docUrl, String description)
	{
		this.docUrl.add(docUrl);
		this.description.add(description);
		this.size = this.docUrl.size();
	}
	
	public String getdocUrl(int a)
	{
		return docUrl.get(a);
	}
	
	public String getDescription(int a)
	{
		return description.get(a);
	}
	
	public int size()
	{
		return this.size;
	}

}
