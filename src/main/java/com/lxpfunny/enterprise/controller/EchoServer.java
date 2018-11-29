package com.lxpfunny.enterprise.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by lixiaopeng on 2017/8/29.
 */
public class EchoServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)){
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            Scanner scanner = new Scanner(inputStream);
            PrintWriter out = new PrintWriter(outputStream,true);


            boolean done = false;
            while(!done && scanner.hasNext() ){
                String line = scanner.nextLine();
                out.println("Echo:"+line);
                if(line.trim().equals("BYE")){
                    done = true;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
