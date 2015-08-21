package test;

import java.util.ArrayList;

import peerSimTest_v3_1_0_0.BF;
import peerSimTest_v3_1_0_0.ErrorException;

public class TestConvert {

	public static String computeEntry(String path)
	{
		String s_tmp = new String();
		
		if (path.length() > 1)
		{
			char[] tmp = path.toCharArray();
			
			int i = 0;
			for (i = tmp.length - 1; i > 0; i--)
			{
				if (tmp[i] != tmp[i - 1])
					break;
			}
			
			if (i != -1)
				s_tmp = path.substring(0, i + 1);
		}
		else
		{
			return path;
		}
		
		return s_tmp;
	}
	
	public static ArrayList<String> computePath(String entry, String path) throws ErrorException
	{
		if (entry.length() == path.length())
			return null;
		
		ArrayList<String> res = new ArrayList<String>();
		
		int len = path.length() - entry.length();

		if (path.charAt(entry.length()) == '0')
		{
			for (int i = entry.length(); i < entry.length() + len; i++)
			{
				BF bf_tmp = new BF(path);
				bf_tmp.setBit(i, true);

				String tmp = computeEntry(bf_tmp.toString());
				if (i < entry.length() + len - 1)
				{
					res.add(tmp.substring(0, tmp.length() - 1));
				}
				else
				{
					res.add(tmp);
				}
			}
		}
		else // path.charAt(entry.length()) != '0'
		{
			for (int i = entry.length(); i < entry.length() + len; i++)
			{
				BF bf_tmp = new BF(path);
				bf_tmp.setBit(i, false);

				String tmp = computeEntry(bf_tmp.toString());
				if (i < entry.length() + len - 1)
				{
					res.add(tmp.substring(0, tmp.length() - 1));
				}
				else
				{
					res.add(tmp);
				}
			}
		}
		
		return res;
	}

	public static void main(String[] args) throws ErrorException {
		System.out.println(computeEntry("01101011111"));
		System.out.println(computeEntry("01101000000"));
		System.out.println(computeEntry("01101010101"));
		System.out.println(computeEntry("01101010110"));
		System.out.println(computeEntry("0"));
		System.out.println(computeEntry("1"));
		System.out.println(computeEntry("011"));
		System.out.println(computeEntry("100"));

		System.out.println("**************");
		BF key = new BF("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001"
				+ "000000000000000000100000000100000000000000000000000000000000000000100000000000000000000000000000000000000000000001"
				+ "0000000000000000000000000000010000000000000000000000000000000000000010000000000000000000000000000000000000000000000"
				+ "00110000000000000000000000000001000001000000000000000001100000010000000000000000000000000000000000000001000000000000000"
				+ "000001000000000000100000100000000000000000000000000000");
	//	String entry = "0";
	//	String path = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
	//			+ "00000000000000000000000000000000000000000000000000000000000000000000000000000";

		String entry = "010";		
		String path  = "01000000000000000000000000000000000000000000000000000000000000";
		ArrayList<String> arrayList = computePath(entry, path);
		
		System.out.println(entry + "\n" + path + "\n");
		System.out.println(arrayList.size());
		
	//	for (int i = 0; i < arrayList.size(); i++)
	//		System.out.println(arrayList.get(i));

		ArrayList<String> test = new ArrayList<String>();
		for (int i = 0; i < arrayList.size(); i++)
		{
			BF bf_tmp = new BF(arrayList.get(i));
			
			if (key.in(bf_tmp))
				test.add(arrayList.get(i));
		}
		System.out.println(test.size());
//		for (int i = 0; i < test.size(); i++)
//			System.out.println(test.get(i));
	}
	
}
