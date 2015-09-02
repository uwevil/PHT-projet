package peerSimTest_v4_1;

public class Message {
	
	private String indexName;
	private MessageType type;
	private String path;
	private Object data = null;
	private BF key;
	private BF bf;
	private int src;
	private int dest;
	private long requestID;
	private boolean isLeafNode;
	private Object option = null;
	private Object option2 = null;
	
	public Message()
	{
	}
	
	public String getIndexName()
	{
		return this.indexName;
	}
	
	public void setIndexName(String indexName)
	{
		this.indexName = indexName;
	}
	
	public BF getBF()
	{
		return this.bf;
	}
	
	public void setBF(BF bf)
	{
		this.bf = bf;
	}
	
	public BF getKey()
	{
		return this.key;
	}
	
	public void setKey(BF key)
	{
		this.key = key;
	}
	
	public MessageType getType()
	{
		return this.type;
	}
	
	public void setType(MessageType type)
	{
		this.type = type;
	}
	
	public Object getData()
	{
		return this.data;
	}
	
	public void setData(Object data)
	{
		this.data = data;
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public int getSource()
	{
		return this.src;
	}
	
	public void setSource(int src)
	{
		this.src = src;
	}
	
	public int getDestinataire()
	{
		return this.dest;
	}
	
	public void setDestinataire(int dest)
	{
		this.dest = dest;
	}
	
	public long getRequestID()
	{
		return this.requestID;
	}
	
	public void setRequestID(long requestID)
	{
		this.requestID = requestID;
	}
	
	public Object getOption()
	{
		return this.option;
	}
	
	public void setOption(Object option)
	{
		this.option = option;
	}
	
	public Object getOption2()
	{
		return this.option2;
	}
	
	public void setOption2(Object option2)
	{
		this.option2 = option2;
	}
	
	public void setIsLeafNode(boolean value)
	{
		this.isLeafNode = value;
	}
	
	public boolean getIsLeafNode()
	{
		return this.isLeafNode;
	}
	
	public String toString()
	{
		return "Message \n  "
				+ "Type         : " + this.getType()			+ "\n  "
				+ "Index        : " + this.indexName 			+ "\n  "
				+ "BF           : " + this.bf 					+ "\n  "
				+ "Key          : " + this.key      			+ "\n  "
				+ "Path         : " + this.path      			+ "\n  "
				+ "Data         : " + this.data 				+ "\n  "
				+ "IsLeafNode   : " + this.isLeafNode 			+ "\n  "
				+ "Source       : " + this.getSource() 			+ "\n  "
				+ "Destinataire : " + this.getDestinataire() 	+ "\n  "
				+ "RequestID    : " + this.requestID 			+ "\n  "
				+ "Option       : " + this.option 				+ "\n  "
				+ "Option2      : " + this.option2 				+ "\n";
	}
	
}
