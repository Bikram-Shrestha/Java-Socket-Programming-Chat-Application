import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
    Created by Bikram Shrestha
    ChatClient class provide the user with GUI to setup
    connection with the server and communicate with the
    server to send messages and receive message from
    other user as well as other relevant information
    regarding the active userList. Thread has been created
    to handle receiving message to the server while message
    is send to the server only when btJoin when userName is
    not registered to the server and btSend is pressed after
    userName has been registered to the server.
 */
public class ChatClient extends Application {

    // Label was created to label different UI components.
    Label labelName = new Label("Name");
    Label labelMessages = new Label("Compose ");
    Label labelReceived = new Label("Messages");
    Label labelTitle = new Label();
    Label labelActiveUser = new Label("Active User");
    Label errorLabel = new Label("");

    /*
     ArrayList for user and chat message was created
     so that it can be used to create observable list.
     */
    ArrayList<String> userList = new ArrayList<>();
    ArrayList<String> chatMessages = new ArrayList<>();

    // List view for user  and message  was declared.
    ListView<String> userListView = new ListView<String>();
    ListView<String> messageListView = new ListView<String>();

    /*
     ObservableList for ListView was created using
     the arrayList of user and chat message.
     */
    ObservableList<String> userItems =
            FXCollections.observableArrayList (userList);

    ObservableList<String> messageItem =
            FXCollections.observableArrayList (chatMessages);


    // Setting text field for user to enter name and message.
    TextField tfName = new TextField();
    TextArea taComposeMessage = new TextArea();

    // Setting button to join, send and exit the chat.
    Button btJoin = new Button("Join");
    Button btSend = new Button("Send");
    Button btDisconnect = new Button("Exit");

    // Declaring dataInput and Output streams.
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    /*
     By default the new user is set to be not jointed or
     joined = false till it meet the criteria, then the
     unique user is set to be joined.
     */
    boolean joined = false;

    //Socket is declared.
    private Socket socket;

    // User name is being used in various methods.
    private String userName;

    private boolean connection = true;


    @Override   // Override the start method.
    public void start(Stage primaryStage)  {

        // Creating BorderPane to arrange all the node.
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        //Setting Title of the application
        Font titleFont = new Font("Times New Roman",20);
        labelTitle.setText("Welcome to Anonymous Chat Application");
        labelTitle.setFont(titleFont);
        Color titleColor = new Color(0.1, 0, 0.5,1);
        labelTitle.setTextFill(titleColor);


        // Setting Prompt for user text field and area.
        tfName.setPromptText("Enter User Name");
        taComposeMessage.setPromptText("Enter your Message");

        // Setting size of the compose text area. So, user can send
        // multiline messages.
        taComposeMessage.setPrefHeight(2*(tfName.getHeight()));
        taComposeMessage.setPrefWidth(250);

        // Creating GridPane for the Center part of BorderPane.
        GridPane centreGridPane = new GridPane();
        centreGridPane.setPadding(new Insets(10));
        centreGridPane.setHgap(20);
        centreGridPane.setVgap(10);

        // Adding item to the centreGridPane
        centreGridPane.add(labelName,0,0);
        centreGridPane.add(tfName,1,0);
        centreGridPane.add(btJoin,2,0);
        centreGridPane.add(labelReceived,0,2);
        centreGridPane.add(errorLabel,1,1,2,1);
        centreGridPane.add(messageListView,1,2,2,1);

        //Setting content to display for the ListVIew
        messageListView.setItems(messageItem);
        userListView.setItems(userItems);

        // user and message list view is made uneditable.
        userListView.setEditable(false);
        messageListView.setEditable(false);

        // Setting size of user ListView.
        userListView.setMaxWidth(180);
        userListView.setMaxHeight(250);


        //Creating and adding item to right of BorderPane
        VBox rightVBox = new VBox();
        rightVBox.setPadding(new Insets(20,0,10,0));
        rightVBox.setSpacing(10);
        rightVBox.getChildren().addAll(labelActiveUser,userListView);
        borderPane.setRight(rightVBox);


        //Creating and adding note to bottomGridPane.
        GridPane bottomGridPane = new GridPane();
        bottomGridPane.add(labelMessages,0,0);
        bottomGridPane.add(taComposeMessage,1,0);
        bottomGridPane.add(btSend,4,0);
        bottomGridPane.add(btDisconnect,7,0);
        bottomGridPane.setHgap(20);
        bottomGridPane.setPadding(new Insets(10,0,10,10));
        btSend.setAlignment(Pos.BASELINE_RIGHT);

        //Adding item to the Top of BorderPane
        borderPane.setTop(labelTitle);
        borderPane.setAlignment(labelTitle,Pos.CENTER);

        //Adding item to the Center of BorderPane
        borderPane.setCenter(centreGridPane);

        //Adding item to the Bottom of BorderPane.
        borderPane.setBottom(bottomGridPane);

        //Creating new scene and placing borderPane.
        Scene scene = new Scene(borderPane,580,400);
        primaryStage.setScene(scene); //Setting scene.
        primaryStage.setTitle("Anonymous Chat"); //Setting title.
        primaryStage.show();    //Display Stage.

        /*
         As socket need to be closed properly for the best
         user experience of the application, it is made
         sure that socket is closed when user close the
         application.
         */
        primaryStage.setOnCloseRequest(t -> closeSocketExit());
        //Send is disable until username is accepted.
        btSend.setDisable(true);

        // Setting listener for the buttons.
        btJoin.setOnAction(event -> joinChat());
        btSend.setOnAction(e -> process());
        btDisconnect.setOnAction(event -> closeSocketExit());

        try {
            // Create a socket to connect to the server
            socket = new Socket("localhost", 8000);

            // Create an input stream to receive data from server.
            dataInputStream =
                    new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            dataOutputStream =
                    new DataOutputStream(socket.getOutputStream());

            // Start a new thread for receiving messages
            new Thread(() -> receiveMessages()).start();
        }
        // Providing feedback to user to notify connection issues.
        catch (IOException ex) {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Unable to establish connection.");
            System.err.println("Connection refused.");
        }
    }


    /*
    As socket need to be closed properly for the best user
    experience of the application, this method is created to
    make sure that the socket is closed and stage is closed
    when this method is called.
     */
    private void closeSocketExit() {
        try {
            //If socket doesn't exit, no need to close.
            if(socket!=null){
                socket.close();
            }
            Platform.exit();    // Close UI.
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    This method receive message for server and read the
    message to be displayed in proper place and relevant
    information is shown to user. It can be related to
    showing in errorLabel whether username has been
    successfully added to server or whether the message
    is to be displayed in user or chat list view.
     */
    public void receiveMessages(){
        try{
            while(connection){
                String message;
                /*If user has not joined the server,
                only addUserName() is allowed to
                perform and other information is
                not shared with user.
                 */

                if(!joined){
                    addUserName();
                }
                /*
                Once userName has been accepted, other
                information like active userList and
                messages is transmitted.
                 */
                else{
                    /*
                    If message start with "[" that is
                    arrayList of user and this is
                    added to user List view.
                     */
                    message = dataInputStream.readUTF();
                    if(message.startsWith("[")){
                        addMessageToUserListView(message);
                    }
                    else{
                    // Display to the message list view.
                        Platform.runLater(() -> {
                            messageItem.add(message);
                        });
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Socket is closed.receive");
            Platform.runLater(() -> {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Unable to establish connection.");
            });
            connection = false;
        }
    }


    /*
    joinChat method allow user to send the userName to
    be approved to the server, as "," is being processed
    in other code to convert arrayList.toString back to
    arrayList, this is not allowed as userName. Else, the
    userName is send to the server and error message is
    handled as so.
     */
    private void joinChat(){
        userName = tfName.getText();
        if(userName.contains(",")){
            Platform.runLater(() -> {
                // Update UI here.
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Cannot contain ','.");
            });
        }
        else{
            try {
                dataOutputStream.writeUTF(userName);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    This method recreate an arrayList from the message and
    add the name to the userListView excluding its own name
    as it is not useful information.
     */
    private void addMessageToUserListView(String s) {
        List<String> list =
                Arrays.asList(
                        s.substring(1, s.length() - 1).split(", ")
        );
        Platform.runLater(() -> {
            // Update UI here.
            userItems.clear();
            for(int i = 0; i < list.size(); i++){
                if(!(list.get(i).equals(userName))){
                    userItems.add(list.get(i));
                }
            }
        });
    }


    /*
    If the server send response to the user and it says accepted,
    then the status of boolean joined is set to be true and this
    is updated in errorLabel to show that the user has joined
    the conversation and the join button is disabled and send
    message button is enabled.
    If it is not accepted, that mean there is userName is in the
    server arrayList so error message is shown letting user
    that the user name exist.
     */
    private void addUserName()  {
        String response;
        try {
            response = dataInputStream.readUTF();
            if (response.startsWith("Accepted")){
                joined = true;
                Platform.runLater(() -> {
                    System.out.println("User Connected as "+ userName);
                    btSend.setDisable(false);
                    btJoin.setDisable(true);
                    tfName.setEditable(false);
                    errorLabel.setTextFill(Color.GREEN);
                    errorLabel.setText("Joined as " + userName);
                });
            }
            else if(response.equals(userName)){
                Platform.runLater(() -> {
                    // Update UI here.
                    tfName.clear();
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("User with same name exist.");
                });
            }
        } catch (IOException e) {
            System.out.println("Socket is closed.add");
            Platform.runLater(() -> {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Unable to establish connection.");
                connection = false;
            });
        }
    }


    /*
    This method send message to server by adding name to the message, so
    that the message can be send to all the user in the chat group.
    Special care has been taken to make sure that the formatting of the
    multiline text is preserved.
     */
    private void process() {
        try {
            // Get the text from the text field
            String string = tfName.getText().trim() + ":\n " +
                     taComposeMessage.getText().trim();

            // Send the text to the server
            dataOutputStream.writeUTF(string);

            // Clear text area.
            taComposeMessage.setText("");
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
}


