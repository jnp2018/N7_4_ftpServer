import java.net.*;
import java.io.*;
import java.util.*;
public class client {
     public static void main(String args[]) throws Exception
    {
        Socket socfd=new Socket("localhost",4000);
        ftp t=new ftp (socfd);
        t.displayMenu();
        
    }
}
class ftp
{
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;
    String dir="";
   ftp(Socket soc)
    {
        
    	try
        {
            ClientSoc=soc;
            din=new DataInputStream(ClientSoc.getInputStream());
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            br=new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception ex)
        {
        	
        }        
    }
   
   String chooseFile()throws Exception{
	   String bir,dir="";
	   System.out.println("Enter path: ");
	   bir = br.readLine();
	   while(true){
		 
		   if(dir.equalsIgnoreCase("")) dir=bir;
		   else dir = dir+bir+"\\";
	       File folder = new File(dir);
	       ArrayList<String> files = new ArrayList<String>();
			ArrayList<String> directories = new ArrayList<String>();
			File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        files.add(listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		    	  directories.add(listOfFiles[i].getName());
		      }
		    }
		    
		    System.out.println("Directories:");
		    for(int i=0;i<directories.size();i++){
		    	System.out.println(""+(i+1)+":"+directories.get(i));
		    	
		    }
		    System.out.println("\n\nFiles");
		    for(int i=0;i<files.size();i++){
		    	System.out.println(""+(i+1)+":"+files.get(i));
		    }
		    System.out.println("\n\n\n1.Select file\n2.Enter Subdirectory");
		    int choice;
            choice=Integer.parseInt(br.readLine());
            if(choice==1)  System.out.print("File: ");
            else  System.out.print("Subdirectory: ");
            bir = br.readLine();
            if(choice==1){
            	
            	dir=dir+bir;
            	break;
            }
	   }
	   return dir;
	   
   }
    void SendFile() throws Exception
    {        
        
        String filename, newfilename;
        filename=chooseFile();
            
        File f=new File(filename);
        if(!f.exists())
        {
            System.out.println("File not Exists...");
            dout.writeUTF("File not found");
            return;
        }
        System.out.print("Where to save?--filename with extension: "); 
        newfilename=br.readLine();
        dout.writeUTF(newfilename);
        
        String msgFromServer=din.readUTF();
        if(msgFromServer.compareTo("File Already Exists")==0)
        {
            String Option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            Option=br.readLine();            
            if(Option.equalsIgnoreCase("Y"))    
            {
                dout.writeUTF("Y");
            }
            else
            {
                dout.writeUTF("N");
                return;
            }
        }
        
        System.out.println("Sending File ...");
        FileInputStream fin=new FileInputStream(f);
        int ch;
        do
        {
            ch=fin.read();
            dout.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        fin.close();
        System.out.println(din.readUTF());
        
    }
    
    void ReceiveFile() throws Exception
    {
        String fileName;
        System.out.print("Enter File Name :");
        fileName=br.readLine();
        dout.writeUTF(dir+fileName);
        String msgFromServer=din.readUTF();
        
        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File not found on Server ...");
            return;
        }
        else if(msgFromServer.compareTo("READY")==0)
        {
            System.out.println("Where to save?");
            String path = br.readLine();
            path = path+"\\"+fileName;
        	System.out.println("Receiving File ...");
            File f=new File(path);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option=br.readLine();            
                if(Option=="N")    
                {
                    dout.flush();
                    return;    
                }                
            }
            FileOutputStream fout=new FileOutputStream(f);
            int ch;
            String temp;
            do
            {
                temp=din.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1)
                {
                    fout.write(ch);                    
                }
            }while(ch!=-1);
            fout.close();
            System.out.println(din.readUTF());
                
        }
        
        
    }
    void BrowseDirectory() throws Exception
    {
        String fileName;
        System.out.print("Enter Directory Name:");
        fileName=br.readLine();
        dout.writeUTF(dir+fileName);
        String msgFromServer=din.readUTF();
        
        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File not found on Server ...");
            return;
        }
        else
        {
        	if(dir.equals("")) dir=fileName;
        	else dir=dir+ fileName+"\\";
        	System.out.println(msgFromServer);
        }
    }
    
    void BackDirectory() throws Exception
    {
    	if(dir.endsWith(":\\")) {
    		dir="";
    		dout.writeUTF("Back");
    	}else{
    		int k = dir.lastIndexOf('\\');
    		 System.out.println("ind: "+k);
            char dst[]= new char[1000];
            dir.getChars(0,dir.length(), dst, 0);
            dst[k]='?';
            String str=new String(dst,0,k);
            k = str.lastIndexOf('\\');
            System.out.println("ind: "+k);
            str.getChars(0, k-1, dst, 0);
            dir = new String(dst, 0, k+1);
            System.out.println(dir);
            dout.writeUTF(dir);
            
    	}
            String msgFromServer=din.readUTF();
            
            if(msgFromServer.compareTo("File Not Found")==0)
            {
                System.out.println("File not found on Server ...");
                return;
            }
            else
            {
            	
            	System.out.println(msgFromServer);
            }
    }

    public void displayMenu() throws Exception
    {
        
    	while(true){
    		System.out.println("Enter Username:");
    		String usr=br.readLine();
    		dout.writeUTF(usr);
    		System.out.println("Enter Password:");
    		String pass=br.readLine();
    		dout.writeUTF(pass);
    		String reply = din.readUTF();
    		System.out.println(reply);
    		if(reply.equalsIgnoreCase("login successful")) break;
    	}
    	while(true)
        {    
            System.out.println("pwd: "+dir+"\n[ MENU ]");
            System.out.println("1. Send File");
            System.out.println("2. Receive File");
            System.out.println("3. Browse");
            System.out.println("4. Back");
            System.out.println("5. Exit");
            System.out.print("\nEnter Choice :");
            int choice;
            choice=Integer.parseInt(br.readLine());
            if(choice==1)
            {
                dout.writeUTF("SEND");
                SendFile();
            }
            else if(choice==2)
            {
                dout.writeUTF("RECEIVE");
                ReceiveFile();
            }
            else if(choice==3)
            {
                dout.writeUTF("LIST");
                BrowseDirectory();
            }
            else if(choice==4)
            {
                dout.writeUTF("LIST");
                BackDirectory();
            }
            else
            {
                dout.writeUTF("DISCONNECT");
                System.exit(1);
            }
        }
    }
}
