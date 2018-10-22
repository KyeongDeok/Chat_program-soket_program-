import java.net.*;				
import java.io.*;				
import java.util.*;				
import java.text.SimpleDateFormat;

public class ChatServer {				
				
	public static void main(String[] args) {			
		try{		
			ServerSocket server = new ServerSocket(10001);	
			System.out.println("접속을 기다립니다.");	
			HashMap <String,PrintWriter> hm = new HashMap <String,PrintWriter>();	
			while(true){	
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while	
		}catch(Exception e){	
			System.out.println(e);
		}	
	} // main		
}			
			
class ChatThread extends Thread{			
	private Socket sock;		
	private String id;		
	private BufferedReader br;		
	private HashMap hm;
	private PrintWriter pw;
	private boolean initFlag = false;
	private String [] filter = {"씨발","새끼","바보","멍청이","병신"};//필터링할 문자열 저장!!!
	public ChatThread(Socket sock, HashMap hm){		
		this.sock = sock;	
		this.hm = hm;	
		try{	
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));	
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));	
			id = br.readLine();	
			broadcast(id + "님이 접속하였습니다.");	
			System.out.println("접속한 사용자의 아이디는 " + id + "입니다.");	
			synchronized(hm){	
				hm.put(this.id, pw);
			}	
			initFlag = true;	
		}catch(Exception ex){		
			System.out.println(ex);	
		}		
	} // 생성자			
	public void run(){			
		try{		
			String line = null;	
			while((line = br.readLine()) != null){		
				if(line.equals("/quit"))	
					break;
				if(line.indexOf("/to ") == 0){	
					sendmsg(line);
				}
				else if(line.equals("/userlist"))
				{
					senduserlist(line);
				}
				else	
					broadcast(id + " : " + line);
			}		
		}catch(Exception ex){			
			System.out.println(ex);		
		}finally{			
			synchronized(hm){		
				hm.remove(id);	
			}		
			broadcast(id + " 님이 접속 종료하였습니다.");		
			try{		
				if(sock != null)	
					sock.close();
			}catch(Exception ex){}		
		}			
	} // run				
	public void sendmsg(String msg){
	    Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("[hh 시 mm 분 ss 초]");
		String date = sdf.format(dt).toString();				
		int start = msg.indexOf(" ") +1;			
		int end = msg.indexOf(" ", start); // /to kine ejie/			
		if(end != -1){			
			String to = msg.substring(start, end);		
			String msg2 = msg.substring(end+1);		
			Object obj = hm.get(to);		
			if(obj != null){		
				PrintWriter pw = (PrintWriter)obj;	
				pw.println(date+" "+id + " 님이 다음의 귓속말을 보내셨습니다. :" + msg2);	
				pw.flush();	
			} // if	
		}		
	} // sendmsg

	//3)			
	/* 시간을 저장하기 위해 Date 인스턴스와 SimpleDateFormat을 활용하여 시간을 측정하고 
	 * 측정한 시간을 String 에 저장하여 출력한다.
	 */
	//5)
	/* 나에게는 내가 보낸 문장이 나만 안보여야하기 때문에. hm을 탐색해서 거기서 나온 vlaue 값을 이용해 전체에게 메세지를 보내는 방식을 이용한다.
	 * 따라서, hm.value()로 전체 pw 값을 pw1에 저장한후 while(iter.hasNext())로 각각의 pw1값을 현재 서버쓰레드의 pw값과 비교하여.
	 * 만약 현재의 pw와 같으면 출력하지 않고, 같지 않으면 출력한다. 그러기 위해서 boolean flag를 선언 하였다.
	 */
	public void broadcast(String msg){
		boolean flag = false; //현재 서버 쓰레드의 pw와 hm에 저장된 pw 값과 비교 확인하기 위해 선언!
		boolean flag1 = false;
		String restr = msg;
		String alram = "욕하지마.";
	    Date dt = new Date(); //Date 인스턴스 생성
	    //SimpleDateFormat 인스턴스 생성으로 현재 시각을 저장한다.
	    //출력할 시간의 포맷을 [hh시 mm분 ss초]로 설정한다.
		SimpleDateFormat sdf = new SimpleDateFormat("[hh 시 mm 분 ss 초]"); 
		String date = sdf.format(dt).toString(); // 시간을 String 으로 저장한다. 
     //6) 욕설 필터링...
		/* 필터링할 문자배열을 저장합니다.
		 * replaceAll 메소드를 이용하여 욕설이 나올경우 "***"으로 바꾸어 필터링 합니다.
		 * 바꾼 문자열과 원래의 문자열을 비교하여 다를 때, 경고 메세지를 보내줍니다.
		 * 근데 경고메세지가 안보내지네요 ㅠㅜㅠㅜㅠㅜㅠㅜ...
		 */
		for(int i=0;i<filter.length;i++)
		{
			msg = msg.replaceAll(filter[i],"***"); //replaceAll 메소드를 이용하여, 메세지를 필터링!!
		}

		if(!restr.equals(msg)) //만약 다르다면 경고메세지 출력!!
		{
			pw.println("욕하지마");
		}
		
		synchronized(hm){		
			Collection collection = hm.values();	
			Iterator iter = collection.iterator();	
			while(iter.hasNext()){	
				PrintWriter pw1 = (PrintWriter)iter.next();

				if(pw!=pw1)//만약 현재의 서버 쓰레드의 pw값과 hm에서 가져온 pw 값이 다르면 flag를 true로 바꾼다. 
				{
					flag=true;
				}

				if(flag)//true 이면 실행!!
				{					
					pw1.println(date +" "+ msg); //시간을 넣어 출력!
					pw1.flush();
							    
				}

				flag = false;
			}

		}		
	} // broadcast

	//4)
	/* 리스트를 보여주기 위하여 hm에 저장 된 key 값을 가져오면 된다.
	 * 따라서, hm.keySet() 메소드로 전체 key 값을 collection에 저장한후, iterator로 탐색하면서 전체 출력한다.
	 * 전체 사용자 수는 count 변수를 하나 선언하여, iterator가 하나 탐색하고 key값을 출력할때마다 숫자를 1씩 올리면서 센다.
	 * 마지막으로 count 출력하면 끝!
	 */
	public void senduserlist(String msg)
	{
		int count=0;
		Collection <String> col = hm.keySet();
		Iterator <String> iter = col.iterator();
		pw.println("현재 접속한 사람은...");
		String list;
		while(iter.hasNext())
		{
			count++;
			list = iter.next();
			pw.println(list);
		}
		pw.println("현재 접속자 수는 : "+count+"명 입니다.");
		pw.flush();
	}
}

//1)
/* ChatClient.java 에서 InputThread를 만들어 사용하는 이유는 클라이언트가 채팅을 하는 동안 다른 사람들이 보낸 메세티를 받기 위해서 이다. 만약 InputThread가 
 * 만들어 지지 않는다면, 채팅 도중에 메세지가 전송되어 글을 입력 할수 없게 된다. 따라서 메세지를 받는 동시에 글을 보내기 위해서 InputThread를 만든다. 사용 방식은 
 * Client클래스에 InputThead를 새로 만들어 Client 클래스가 하나 실행될때 마다 InputThread가 새로 만들어 지게 한다. InputThread는 Thread는 Thread를 상속하며
 * 오로지 서버로부터 메세지를 받아 모니터에 출력하는 역할을 한다. */
//2)
/*ChatServer.java 내의 broadcast() 와 sendmsg() 에서 hm 사용법은 우선 broadcast()는 채팅 접속자 모두에게 메세지를 보내는 역할을 하는 메소드이다. 따라서 broadcast()
 * 내에서 hm의 역할은 hm에 저장되어 있는 모든 value값을 찾아내서 각각에 모든 채팅접속자에게 메세지를 보내는 역할을 한다. 따라서 메인 서버 클래스에 hashmap을 만들고, 거기에다 서버 쓰레드가
 * 생성될때 마다 사용자의 id와 pw 참조값을 저장하고, broadcast()를 사용 할때, hm.values() 메소드를 활용하여, 저장되어 있는 모든 pw값을 읽고 각각의 클라이언트에게 메세지를 보낸다.
 * sendmsg()의 역할은 우선 특정 한사람에게만 메세지를 보내는 것이므로, 그사람의 pw정보만을 읽어 들여와서 그사람에게만 메세지를 보내면 된다. 따라서 hm에 key값인 귓속말을 보낼 사람의 id를 
 * 사용 하여 해당 사람의 pw값을 찾고 이를 이용하여 해당 사용자에게만 메세지를 보낸다.
 */ 

//7)
/* 서버 쓰레드에서 run 메소드 안에서 br 스트림으로 문자를 읽어 들일때, 조건문으로 if(line.indexOf("+ "))를 사용하여, '+'를 캐치한다.
 * 그 다음 indexOf() 와 substring() 메소드를 사용하여, 그 이후의 단어를 key 값으로, 그다음의 값을 value 값으로 각각 hashmap에 저장한다.
 * 단, key값은 + -> # 로 변환하여 저장한다.
 * int start = line.indexOf(" ");
 * String keyplus = lint.substring(0,start);
 * keyplus = "#"; -> *을 #으로 바꾼다.
 * 그이후 의 값은 int end 변수 선언하고, 위와 같은 방식으로 " "의 인덱스 값을 읽고, substring으로 문자열을 잘라내서 key와 value로 나누어
 * hashmap에 저장한다.
 * 이런식으로 상용구를 등록할때 등록 규칙만 정해져 있다면 해당 규칙에 맞게 indexOf()와 substring()를 이용하여 얼마든지 상용구를 저장할수 있다. 

//8)
 /* 메인서버에 Fileoutputstream 와 Fileinputstream 인스턴스를 생성한다. 또한 String str을 따로 선언하여, 사용자가 입력한 파일 이름을 저장하도록 한다.
  * 서버 쓰레드를 생성할때, Fileoutputstream 와 FileinputStream 의 참조값도 생성자에 전달한다.
  * 다음 클라이언트에서 & 파일이름을 입력하면, run 조건문에서 if 조건문으로 if(line.indexOf("& "))를 검색하여 캐치한다.
  * 다음 line.indexOf()메소드와 line.substring()메소드를 이용하여, 파일 이름을 분리해낸다.
  * 따로 분리된 파일이름을 str에 저장한다.
  * Fileoutputstream 을 생성할때 파일이름 파라미터로 사용할 수 있도록, 사용자가 입력하여 저장한 str를 넣는다.
  * 동시에 Fileoutputstream을 통하여 파일을 전송한다.
  * 메인 서버로 부터 받은 FileInputStream을 통해 FileoutStream으로 부터 받은 파일을 전송 받는다.
  * FileInputStream 의 파일이름은 적절히 만든다.
  */ 