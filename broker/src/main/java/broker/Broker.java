package broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
public class Broker
{
	private static final String localhost = "127.0.0.1";
	private static final int port = 5000;
    private static BufferedReader bufferedReader = null;
	protected SocketChannel client;
	protected ArrayList<String> messages = new ArrayList<>();
	public static String ID ="";
	public static final String[] brokerStockItems = { "Spark Plug", "Air Filter", "Oil Filter", "Fuel Filter", "Engine Oil" };  
	public static final int[] brokerStockQuantity = { 0, 0, 0, 0, 0 };
	
	public static int orderID = 0;
	
    public static void main(String[] args) throws Exception {
		
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(localhost, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT |
                SelectionKey.OP_READ | SelectionKey.
                OP_WRITE);
        bufferedReader = new BufferedReader(new
                InputStreamReader(System.in));
        while (true) {
            if (selector.select() > 0) {
                if (processSet(selector.selectedKeys())) {
                    break;
                }
            }
        }
        socketChannel.close();
    }

public static String setFix(int price, int quantity, int side)
{
		
	ZonedDateTime now= ZonedDateTime.now(ZoneOffset.UTC);

	String fixedBody = "|35=D|49="+ID+"|56=100001|52="+now+"|54="+side+"|38="+quantity+"|44="+price;
	int bodylength = fixedBody.getBytes().length;
	
	String fixedHeader = "8=FIX.4.4|9="+bodylength+"|11="+orderID;
	String fixedTrailer = "|10="+getChecksum(ByteBuffer.wrap(fixedBody.getBytes()), bodylength)+"|";
	String brokerMessage = fixedHeader + fixedBody + fixedTrailer;

	return (brokerMessage);

}

	public static String getChecksum(ByteBuffer a, int b)
	{
		int checksum = 0;
			for (int i = 0; i < b; i++) {
				checksum += a.get(i);
			}
			checksum = checksum%256;
			if(checksum < 10)
			{
				return "00"+checksum;
			}
			else if(checksum < 100)
			{
				return "0"+checksum;
			}
			else
			{
				return checksum % 256+"";
			}
	}
	
	public static Boolean processSet(Set readySet)
            throws Exception {
        SelectionKey key = null;
        Iterator iterator = null;
        iterator = readySet.iterator();
        while (iterator.hasNext()) {
            key = (SelectionKey) iterator.next();
            iterator.remove();
        }
        if (key.isConnectable()) {
            Boolean connected = processConnect(key);
            if (!connected) {
                return true;
            }
        }
        if (key.isReadable()) 
		{
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(byteBuffer);
            String result = new String(byteBuffer.array()).trim();
           if(ID.isEmpty())
			{
				ID = result;
				System.out.println(" ID: " +ID);
				brokerOptions(socketChannel,byteBuffer );	
			}
			else
			{
				 System.out.println(" Server: " + result);
				updatebrokerStock(result);
			}
			brokerOptions(socketChannel,byteBuffer );
			
        }    
        return false;
    }
	
	public static void updatebrokerStock(String input)
	{
		String[] arr = input.split(" ");
		String[] array = arr[1].split("\\|");
		String quantity = array[11].split("=")[1];
		String buyOrSell = array[9].split("=")[1];
		int varb = 0;
	    varb = Integer.parseInt(quantity);
		int var = 0;
		var = Integer.parseInt(array[2].split("=")[1]);
		if(arr[0].equals("accepted"))
		{
			brokerStockItems[var] = brokerStockItems[var] + varb;
		}
	}
	
	public static void brokerOptions(SocketChannel socketChannel, ByteBuffer byteBuffer )
	{
		  try{

			String input = "";
			int quantity = 0;
			int price = 0;
			String item = "";
			int i_input = 0;
			int side = 1;

			//
			while(true)
			{
				System.out.println(" Broker options: 0 = Exit, 1 = Buy, 2 = Sell ");
				input = bufferedReader.readLine();

				if (input.equalsIgnoreCase("0")) {
					System.exit(0);
				} 
				else if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("2"))
				{
					side = Integer.parseInt(input);
					break;
				} else {
					System.out.println(" invalid ");
				}
			

			// print stock
			for (int i = 0; i < brokerStockItems.length; i++) {
				System.out.println(" Item : " + brokerStockItems[i]);
			}

			// get item
			// while (true) {
			// 	System.out.println(" item: 1 - 5");

			// 	input = bufferedReader.readLine();
			// 	while (isDigit(input) == false) {
			// 		System.out.println(" invalid, 1 - 5");
			// 		input = bufferedReader.readLine();
			// 	}
			// }
			i_input = Integer.parseInt(input);
			item =  brokerStockItems[i_input];
			
			// get itemIndex
			int itemIndex = 0;
			for(int i = 0; i< brokerStockItems.length;i++)
			{
				if(item.equals(brokerStockItems[i]))
				{
					itemIndex = i;
					break;
				}
			}
			
			orderID = itemIndex;
			// get quantity requested
			while(true)
			{
				System.out.println(" Stock amount 1 - 5 ");
				input = bufferedReader.readLine();

				if ( isDigit(input) == true ) {
					i_input = Integer.parseInt(input); 
					if (i_input > 0 && i_input < 6) {
						quantity = i_input;
						break;
					} else {
						System.out.println(" invalid, enter 1 - 5 ");
					}
				}
			}
	
			if(side == 2){

				int available = brokerStockQuantity[itemIndex];
				while(true)
				{
					System.out.println(" Stock amount 1 - 5 ");
					input = bufferedReader.readLine();
	
					if ( isDigit(input) == true ) {
						i_input = Integer.parseInt(input); 
						if (i_input > 0 && i_input < 6) {
							quantity = i_input;
							break;
						} else {
							System.out.println(" invalid, enter 1 - 5 ");
						}
					}

					if(quantity < available){
						break;
					}
					System.out.println(" insufficient stock ");
				}
			}	

			// get price
			while(true)
			{
				System.out.println(" price: 1 - 10000 ");
				input = bufferedReader.readLine();

				if ( isDigit(input) == true ) {

					i_input = Integer.parseInt(input); 
					if (i_input > 0 && i_input < 10000) {
						price = i_input;
						break;
					}
				}

				if (price > 0) {
					break;
				} else {
					System.out.println(" invalid ");
				}
				
			}
			
			if(side == 1)
			{
				input = item+"#"+setFix(price, quantity,1);
			}
			else
			{
				input = item+"#"+setFix(price, quantity,2);
			}	
            byteBuffer = ByteBuffer.wrap(input.getBytes());
            socketChannel.write(byteBuffer);
		  }	
		}
		catch(IOException e)
		{
		System.out.println(" error ");
		}		
	}

	public static Boolean processConnect(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			while (socketChannel.isConnectionPending()) {
				socketChannel.finishConnect();
			}
		} catch (IOException e) {
			key.cancel();
			return false;
		}
		return true;
	}

	public static boolean isDigit(String input) {
        try {
            int i = Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}