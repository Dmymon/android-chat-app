package com.example.a1mymon.myapplication;

        import android.bluetooth.BluetoothSocket;
        import android.content.Context;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.widget.Toast;

        import org.encryptor4j.Encryptor;
        import org.encryptor4j.factory.KeyFactory;

        import java.net.*;
        import java.io.*;
        import java.util.*;

        import javax.crypto.SecretKey;

/**
 * Thread for executing the client operations
 * Created by 1mymon on 07/03/2017.
 */

public class ClientSide extends AsyncTask<String,Void,Void> {

        BluetoothSocket ClientSoc;

        DataInputStream din;
        DataOutputStream dout;
        BufferedReader br;
        Context prvContext;


       public ClientSide(){}

    /**
     * Constructor
     * @param soc active socket for sending client requests
     * @param context user activity context to tunnel back messages and results
     */
       public ClientSide(BluetoothSocket soc, Context context)
        {
            prvContext = context;
            try
            {
                ClientSoc=soc;
                din=new DataInputStream(ClientSoc.getInputStream());
                dout=new DataOutputStream(ClientSoc.getOutputStream());
                br=new BufferedReader(new InputStreamReader(System.in));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }


        }
        void SendFile() throws Exception
        {

            String filename;
            System.out.print("Enter File Name :");
            filename=br.readLine();

            File f=new File(filename);
            if(!f.exists())
            {
                System.out.println("File not Exists...");
                dout.writeUTF("File not found");
                return;
            }

            dout.writeUTF(filename);

            String msgFromServer=din.readUTF();
            if(msgFromServer.compareTo("File Already Exists")==0)
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option=br.readLine();
                if(Option=="Y")
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
            dout.writeUTF(fileName);
            String msgFromServer=din.readUTF();

            if(msgFromServer.compareTo("File Not Found")==0)
            {
                System.out.println("File not found on Server ...");
                return;
            }
            else if(msgFromServer.compareTo("READY")==0)
            {
                System.out.println("Receiving File ...");
                File f=new File(fileName);
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

        /*
        public void displayMenu() throws Exception {
            while (true) {
               System.out.println("[ MENU ]");
                System.out.println("1. Send File");
                System.out.println("2. Receive File");
                System.out.println("3. Exit");
                System.out.print("\nEnter Choice :");

                int choice;
                choice = Integer.parseInt(br.readLine());
                if (choice == 1) {
                    dout.writeUTF("SEND");
                    SendFile();
                } else if (choice == 2) {
                    dout.writeUTF("GET");
                    ReceiveFile();
                } else {
                    dout.writeUTF("DISCONNECT");
                    System.exit(1);
                }
            }
        }
        */


    /**
     * Method for sending FTP message
     * @param message message to send
     * @throws Exception
     */
    public void SendMessage(String message) throws Exception
        {
            try {
                dout.writeUTF("SEND");
                dout.flush();

            do
            {
                dout.writeUTF(message);
                dout.flush();
            }
            while(false);
            }
            catch (Exception e)
            {e.printStackTrace();}

        }

    /**
     * filters requests and act accordingly
     * @param strings holds the requests
     * @return
     */
    @Override
    protected Void doInBackground(String... strings) {

        try {
            if (strings[0] == "KEY")
                SendSecretKey(strings[1]);
            else
                SendMessage(strings[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * responsible for secretkey sharing
     * @param key
     */
    private void SendSecretKey(String key) {

        try {
            dout.writeUTF("KEY");
            dout.flush();

            do
            {
                dout.writeUTF(key);
                dout.flush();
            }
            while(false);
        }
        catch (Exception e)
        {e.printStackTrace();}
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(prvContext,"Message Sent!",Toast.LENGTH_SHORT).show();
        super.onPostExecute(aVoid);
    }
}

