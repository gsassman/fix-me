package router;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.io.BufferedReader;

public class Server extends Thread {

	private List<Handler> clientList;
	private ArrayList<String> messages = new ArrayList<String>();
	public Handler socketHandlerAsync = null;
	public ServerSocketChannel server = null;
	SocketChannel sc;
    /**********************************************/
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String RESET_CO = "\u001B[0m";
    /*********************************************/
    public int port;
    public String componentType;
	String ID = "";
	public BufferedReader input = null;
	protected int brokerID = 100000;//limit  499 999
	protected int marketID = 500000;//limit is 999 999
	Selector selector;
	
    public Server(int recievedPort, String cType)
    {
        this.port = recievedPort;
        this.componentType = cType;
		clientList = new ArrayList<Handler>();
    }
	
	private String setConnectionID(String componentType)
	{
		if(componentType.equalsIgnoreCase("broker"))
		{
			this.brokerID++;
			if(brokerID > 500000)
		    {
				System.out.println(PURPLE + componentType+RED+"YOU SOMEHOW EXHAUSTED THE UNIQUE 6 DIGIT POSSIPLE IDS"+RESET_CO);
				System.out.println(PURPLE + componentType+RED+" DISCONNECTING FROM SERVER"+RESET_CO);
				System.exit(0);
			}
			return brokerID+"";
		}
		else
		{
			this.marketID++;
			if(marketID > 1000000)
		    {
				System.out.println(PURPLE + componentType+RED+"YOU SOMEHOW EXHAUSTED THE UNIQUE 6 DIGIT POSSIPLE IDS"+RESET_CO);
				System.out.println(PURPLE + componentType+RED+" DISCONNECTING FROM SERVER"+RESET_CO);
				System.exit(0);
			}
			return marketID+"";
		}
	
	}
	
    protected  void runServer()
    {
        try
        {
			server = ServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", port));
            System.out.println(YELLOW+"[SERVER]" + CYAN + "[LISTENING ON PORT " + YELLOW + port + CYAN + " ]" + RESET_CO);
		   String ID = setConnectionID(componentType);
		   this.ID = ID;
		   while(true)
		   {
			   sc = server.accept();
			   socketHandlerAsync = new Handler(sc, clientList.size() ,messages, port, ID,  componentType);
			   System.out.println(PURPLE + componentType+ CYAN + "[ CONNECTION ACCEPTED ] "+YELLOW+"ID : "+GREEN+ID+"\n"+RESET_CO);
			   clientList.add(socketHandlerAsync);
			   socketHandlerAsync.start();
		   }		   
        }
        catch (IOException e)
        {
            System.out.println(RED+"Disconnected from the server");
        }
    }
	
	public void sendMessage(String str) 
	{
		socketHandlerAsync.sendMessage(str);
	}

	public String getID()
	{
		return ID;
	}

	public String getMessages() {
		return socketHandlerAsync.getMessages();
	}

    @Override
    public void run()
    {
        runServer();
    }

}