/*delete this : Developer note -> resources
http://www.java2s.com/Tutorials/Java/Java_Network/0070__Java_Network_Non-Blocking_Socket.htm
*/
package market;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.charset.*;
import java.nio.*;

public class Market
{
    private static BufferedReader input = null;
	static String message = null;
    public static final String[] instruments = {"A", "B", "c", "D", "E", "F"};
	public static final int[] stock = {15, 25, 40, 45, 62, 78};
    public static String ID ="";
    public static  final String ipaddr = "127.0.0.1";
    public static final int port = 5001;

	public static void ShowInstruments()
	{
		System.out.println("Available instruments")
		for(int i = 0; i < instruments.length; i++)
		{
			System.out.println("index"+ i + " : [ "+ instruments[i] + " ]");
		}
	}

    public static Boolean processReadySet(Set readySet) throws Exception
    {
        SelectionKey key = null;
        Iterator iterator = null;
	
        iterator = readySet.iterator();

        while (iterator.hasNext())
        {
            key = (SelectionKey) iterator.next();
            iterator.remove();
        }

        if (key.isConnectable())
        {
            Boolean connected = processConnect(key);
            if (!connected)
            {
                return true;
            }
        }

        if (key.isReadable())
        {
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer bb = ByteBuffer.allocate(1024);
            
            sc.read(bb);

            String result = new String(bb.array()).trim();
           
			if(ID.isEmpty())
			{
				ID = result;
				System.out.println("Assigned ID: [ " + ID + " ]");
			}
			else
			{
				System.out.println("Handling request");
				handleRequest(key,result);
			}
        }
        return false;
    }
	
	public static String processOrder(String instrument, String fixmsg)
	{
		int item = 0;

		for(int i = 0 ; i < instruments.length; i++)
		{
			if(instrument.equals(instruments[i]))
		    {
				item = i;
				break;
			}
		}

		String[] msglist = fixmsg.split("\\|");
		String amount = msglist[11].split("=")[1];
		String buyOrSell = msglist[9].split("=")[1];
		int item2 = 0;
		item2 = Integer.parseInt(amount);
		if(buyOrSell.equals("1"))
	    {
		    stock[item] = stock[item] - item2;
			if(stock[item] > 0)
			{
				return "accepted";
			}
			else
			{
				return "rejected";
			}
		}
		else
		{
		   stock[item] = stock[item] + item2;
			
			return "accepted";
		}	
	}
	
	public static void handleRequest( SelectionKey key, String returnedStr)
	{
		try
		{
			String[] a = returnedStr.split("#");
			System.out.println("Message received from Server: [ " + a[1] + " ]");
			String processOrder = processOrder(a[0], a[1]);
			String msg = processOrder + " " + a[1];

			System.out.println("request Handled");

			SocketChannel sc = (SocketChannel) key.channel();
			ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());

			sc.write(bb);

			System.out.println("request sent");
		}
		catch(IOException e)
		{
			 System.out.println("request failed");
		}	
	}
	
    public static Boolean processConnect(SelectionKey key) 
    {
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            while (sc.isConnectionPending())
            {
                sc.finishConnect();
            }
        } catch (IOException e) {
            key.cancel();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception 
	{
		
        InetSocketAddress conn = new InetSocketAddress(ipaddr, port);
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false); //set to non blocking
        sc.connect(conn); //establish connectivity
        sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE); // set allowed operations

        input = new BufferedReader(new InputStreamReader(System.in));
		
        ShowInstruments();

        while (true)
        {
            if (selector.select() > 0)
            {	
                Boolean Ready = processReadySet(selector.selectedKeys());
                if (Ready) {
                    break;
                }
            }
        }
        sc.close();
    }
}