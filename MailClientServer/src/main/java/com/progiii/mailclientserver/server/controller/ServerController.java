package com.progiii.mailclientserver.server.controller;

import com.progiii.mailclientserver.server.model.Server;
import com.progiii.mailclientserver.utils.Action;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController
{
    private Server server;

    @FXML
    private TextArea logTextArea;

    ExecutorService executorService;



    @FXML
    public void initialize()
    {
        if (this.server != null)
            throw new IllegalStateException("Server can only be initialized once");
        this.server = new Server();
        //TODO: load clients from json
        logTextArea.textProperty().bind(server.logProperty());

        executorService = Executors.newFixedThreadPool(9);
        ServerSocket serverSocket;
        try
        {
            serverSocket = new ServerSocket(6969);
        }catch(Exception ex){ex.printStackTrace(); return;}

        new Thread(() -> {
            while(server.isRunning())
            {
                try
                {
                    Socket incomingRequestSocket = serverSocket.accept();
                    ServerTask st = new ServerTask(incomingRequestSocket);
                    executorService.execute(st);
                }catch (Exception ex) {ex.printStackTrace();}
            }
            executorService.shutdown();
            System.out.println("Server shutting down...");
            //TODO find out why it doesn't shut down
        }).start();
    }

    @FXML
    private void onStartServerClicked()
    {
        server.setRunning(true);
    }

    @FXML
    private void onStopServerClicked()
    {
        server.setRunning(false);
    }

    class ServerTask implements Runnable
    {
        Socket incoming;

        public ServerTask(Socket incoming)
        {
            this.incoming = incoming;
        }

        @Override
        public void run()
        {
            try {
                ObjectInputStream inStream = new ObjectInputStream(incoming.getInputStream());
                Action incomingRequest = (Action) inStream.readObject();
                //TODO actually process the request
                server.add(incomingRequest);
            }catch (Exception ex) {ex.printStackTrace();}

        }
    }

}