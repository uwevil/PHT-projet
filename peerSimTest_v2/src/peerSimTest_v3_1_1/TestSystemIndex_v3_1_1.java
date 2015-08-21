package peerSimTest_v3_1_1;

public class TestSystemIndex_v3_1_1 {

	public static void main(String[] args) throws ErrorException
	{
		PHT systemIndex = new PHT("dcs");
		
		BF key1 = new BF("0001" + "0101" + "1011" + "1000");
		BF key2 = new BF("0001" + "0101" + "1111" + "1000");
		BF key3 = new BF("0001" + "0101" + "1101" + "1000");
		BF key4 = new BF("0001" + "0101" + "1001" + "1000");
		BF key5 = new BF("0100" + "0101" + "1001" + "1100");
		BF key6 = new BF("0000000000000000000000000000001");
		BF key7 = new BF("0000000001000000000000010000001");
		BF key8 = new BF("0001000010010001000001000000000");
		BF key9 = new BF("111111000000000000000");
		BF key10 = new BF("1111111111111111111");
		
		systemIndex.insert(key1);
		systemIndex.insert(key2);
		systemIndex.insert(key3);
		systemIndex.insert(key4);
		systemIndex.insert(key5);
		systemIndex.insert(key6);
		systemIndex.insert(key7);
		systemIndex.insert(key8);
		systemIndex.insert(key9);
		systemIndex.insert(key10);

		System.out.println();
	/*	Hashtable<String, PHT_Node> listNodes = systemIndex.getListNodes();
		
		Enumeration<String> enumeration = listNodes.keys();
		
		while (enumeration.hasMoreElements())
		{
			String s = enumeration.nextElement();
			
			System.out.println(s);
			
			PHT_Node n = listNodes.get(s);
			
			System.out.print(n.getPath());
			
			if (n.getListKeys() != null && n.getListKeys().size() != 0)
			{
				System.out.print(" : " +n.getListKeys().size() + " " + n.getListKeys().toString()+ "\n\n");
			}
			else
			{
				System.out.print(" : noeud\n\n");
			}
		}
	*/	
		System.out.println("-------SEARCH-------");
		System.out.println(" " + key9);
		Object res = systemIndex.ssSearch(key9);
		System.out.println(res);
	}

}
