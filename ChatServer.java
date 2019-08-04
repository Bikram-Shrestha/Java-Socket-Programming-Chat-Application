import java.io.*;
import java.net.*;
import java.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


/*
    Created by Bikram Shrestha
    ChatServer class provide a GUI for the user to see
    the various connection that is been established as
    well as current active user list. This is performed
    with the help of multi threading, The server listen
    for connection from client continuously and create
    a new thread to handle the communication with the
    connected socket.
    When the user try to connect to the server for the
    first time, as client side can only send the userName,
    it is checked whether there is user with same name in
     the userList maintained in server side and if the
     user name is unique, an accepted message is send to
     the client and client is then only allow to receive
     message and active userList.
 */
public class ChatServer extends Application {

    // Label was create to label logList and userList.
    Label lbLog = new Label("Log");
    Label lbUserList = new Label("Active User");

    /*
     ArrayList for user and chat message was created
     so that it can be used to create observable list.
     */
    private ArrayList<String> logList = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<>();

    // List view for log  and user  was declared.
    ListView<String> logListView = new ListView<String>();
    ListView<String> userListView = new ListView<String>();

    /*
     ObservableList for ListView was created using
     the arrayList of log and user list.
     */
    ObservableList<String> logItems =
            FXCollections.observableArrayList (logList);
    ObservableList<String> userItems =
            FXCollections.observableArrayList (userList);

    // Mapping of sockets to output streams
    private Hashtable outputStreams = new Hashtable();

    //ArrayList of all open Socket.
    private ArrayList<Socket> socketList = new ArrayList<>();

    // Server socket
    private ServerSocket serverSocket;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        //Setting content to display for the ListVIew
        userListView.setItems(userItems);
        logListView.setItems(logItems);
        logListView.setMinWidth(430);

        // Creating GridPane to arrange all the node.
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));

        //All the nodes are added to the gridPane.
        gridPane.add(lbLog,0,0);
        gridPane.add(logListView,0,1);
        gridPane.add(lbUserList,0,2);
        gridPane.add(userListView,0,3);
        // Create a scene and place it in the stage
        Scene scene = new Scene(gridPane, 450, 400);
        primaryStage.setTitle("Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
        /*
        Special care is taken to make sure that all the connection
        to the client is been closed properly before closing the
        application.
         */
        primaryStage.setOnCloseRequest(t -> closeSocketExit());

        // Start a new thread to listen for connection.
        new Thread(() -> listen()).start();
    }


    /*
    When this method is called, it make sure that all the
    open socket, or connection to the client is terminated
    properly.
     */
    private void closeSocketExit() {
        try {
            for(Socket socket:socketList){
                //If socket doesn't exit, no need to close.
                if(socket!=null){
                    socket.close();
                }
            }
            Platform.exit();    // Close UI.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    This thread create a new serverSocket using the port 8000
    and wait for user to connect. This is done in a loop so
    that this server will be waiting and creating a new
    connection as user join the server.
     */
    private void listen() {
        try {
            // Create a server socket
            serverSocket = new ServerSocket(8000);
            Platform.runLater(() ->
                    logItems.add("MultiThreadServer started at " + new Date()));

            while (true) {// Listen for a new connection request
                Socket socket = serverSocket.accept();

                //Add accepted socket to the socketList.
                socketList.add(socket);

                // Display the client socket information and time connected.
                Platform.runLater(() ->
                        logItems.add("Connection from " + socket + " at " + new Date()));

                // Create output stream
                DataOutputStream dataOutputStream =
                        new DataOutputStream(socket.getOutputStream());

                // Save output stream to hashtable
                outputStreams.put(socket, dataOutputStream);

                // Create a new thread for the connection
                new ServerThread(this, socket);
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }


    // This method dispatch userList to all user in the server.
    private void dispatchUserList() {
        this.sendToAll(userList.toString());
    }


    // Used to get the output streams
    Enumeration getOutputStreams(){
        return outputStreams.elements();
    }


    // Used to send message to all clients
    void sendToAll(String message){
        // Go through hashtable and send message to each output stream
        for (Enumeration e = getOutputStreams(); e.hasMoreElements();){
            DataOutputStream dout = (DataOutputStream)e.nextElement();
            try {
                // Write message
                dout.writeUTF(message);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    // This method send onlineStatus to all the user excluding self.
    void sendOnlineStatus(Socket socket,String message){
        for (Enumeration e = getOutputStreams(); e.hasMoreElements();){
            DataOutputStream dataOutputStream = (DataOutputStream)e.nextElement();
            try {
                //If it is same socket then don't send the message.
                if(!(outputStreams.get(socket) == dataOutputStream)){
                    // Write message
                    dataOutputStream.writeUTF(message);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /*
    Declaring a ServerThread class so that it can be
    used to create a multi-thread server serving
    a each socket in different thread.
     */
    class ServerThread extends Thread {
        private ChatServer server;
        private Socket socket;
        String userName;    // Default null;
        boolean userJoined; // Default false;

        /** Construct a thread */
        public ServerThread(ChatServer server, Socket socket) {
            this.socket = socket;
            this.server = server;
            start();
        }

        /** Run a thread */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream dataInputStream =
                        new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream =
                        new DataOutputStream(socket.getOutputStream());

                // Continuously serve the client
                while (true) {
                    /*
                    When user connect to the server for first time, as it
                    can only send userName, the userName is checked against
                    the userList to make sure only one user with the same
                    name exist, and approve message is send if approved.
                    */
                    if(!userJoined){
                        userName = dataInputStream.readUTF();
                        if(userList.contains(userName)){
                            dataOutputStream.writeUTF(userName);
                            System.out.println(userName + " already exist.");
                        }
                        else{
                            userList.add(userName);
                            dataOutputStream.writeUTF("Accepted");
                            server.dispatchUserList();
                            System.out.println(userName +" joined the chat room");
                            userJoined = true;
                            String userNotification = userName + " joined the chat room.";
                            Platform.runLater(() ->
                                    logItems.add(userName + " joined the chat room."));
                            server.sendOnlineStatus(socket,userNotification);
                            userItems.clear();
                            userItems.addAll(userList);
                        }
                    }
                     /*
                    Once it join it can receive the message from the other
                    user in broadcast mode.
                    */
                    else if(userJoined){
                        // User Message
                        String string = dataInputStream.readUTF();

                        // Send text back to the clients
                        server.sendToAll(string);
                        server.dispatchUserList();

                        // Add chat to the server jta
                        Platform.runLater(() ->logItems.add(string));
                    }
                }
            }


            /*
            When ever Exception is thrown due to closed socket, this is
            handled properly so that further error does not occurs due
            to non existence socket. The user is also removed from the
            userList if it was able to register successfully before changing
            the default value of null to userName. And relevant message is
            broadcast to other user letting them know that the user has
            left the chat due to closed socket.
             */
            catch(IOException ex) {
                System.out.println("Connection Closed for " + userName);
                Platform.runLater(() ->
                        logItems.add("Connection Closed for " + userName));

                if(!userName.equals(null)){
                    userList.remove(userName);
                }
                outputStreams.remove(socket);
                server.dispatchUserList();
                if (!userName.equals(null)){
                    server.sendToAll(userName + " has left the chat room.");
                }
                Platform.runLater(() ->{
                    userItems.clear();
                    userItems.addAll(userList);
                });
            }
        }
    }
}
