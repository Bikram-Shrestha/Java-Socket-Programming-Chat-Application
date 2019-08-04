# Chat Application

Chat application called ‘Anonymous Chat Application’ has been created with graphic user interface (GUI) capable of exchanging text chat between two or multiple computers over the network using java socket programming and JavaFX has been created. This application will let the user connect to the chat server or chat room with unique user name and will let user see who is online at the same chat room or server. This application has separated server and client application and as socket programming require server to be present for client to connect, server application called ‘ChatServer’ need to be run first before running ‘ChatClient’. 

## Logic
The logic of the application is explained below in bullet point.

- As server is required to relay the message between the clients, this application has a ‘ChatServer’ component which need to be executed first. 
-	After ‘ChatServer’ is running, client application called ‘ChatClient’ can be executed. 
-	As the client application is the only way the user can send and receive the messages, a user name needs to create before message can be send. 
-	This is performed by sending the user name to the server to check whether the server contain any user with same name, if the user name is accepted, then the user is able to start receiving the message from the server which include the active user list and any message send by user there afterwards, if the user name was unaccepted, the user is asked to enter the user name again till it satisfy the criteria.
-	This is done to prevent two users with same name, which will confuse all the user participating in the chat room. 
-	Once user compose and press send message, the server receives the message and broadcast to all the user to the chat room.
-	Multiline text input has been provided to user by using the text area.
-	A lot of error handling has been performed so that when user exit or close the application, the server receives the socket close notification, closing the user socket and removing it from the current user list so that the user name is available for future connection.
-	As the server side is always listening for the connection request in a loop and once socket is created, a thread is created to communicate with the given client, many clients can create a connection and exchange messages between them.

### Input: - 

The server must be running for the user input to be processed, once the server is running the user can user the text field, buttons and text area to provide input to the system.

-	The user can use text field labelled name to input the user name.
-	The user can then press join to send the user name to the server to be processed.
-	If the name is accepted by server, then the user can use text area labelled compose to compose the message to be send to all the people joining the chat room/server.
-	Once the chat is composed, the user can press send button to send the message to the server for it to be broadcasted to everyone joining in the chat server.
-	The user can press exit to terminate the connection and close the application.
-	Same task can be performed by pressing the cross button in the Windows UI.

### Processing: -

As this application has two components: server and client, these components will process different data differently.
#### Server:

-	The server will receive the connection request from the server and will assign a unique socket for the client to connect and create a thread to communicate with the client.
-	When the client side connect for the first time, it can send the server its user name.
-	Once the user name is received by the server, the server checks the internal user list to check whether there is a user with same user name. If the user exists, it will send username and if does not exist then will add the username to the user list and dispatch message to all the user letting them know that the new user has joined the chat and send client with accepted message and updated user list.
-	Once the user is accepted, it will also receive the list of users as a string, this string is reprocessed to add to the active user list view.
-	The user can then send the message to the server.
-	Once the server receives the message, it broadcast the message to everyone in the chat server. 
-	If the client terminates the application, the server sends a notification to all user letting them know that the user has left the client with updated user list.

### Output: -

#### Server:
The server application will present the user with the information regarding the number of connections made and the user activity.

-	List view of log labelled log show all the activity that is happening in the server.
-	List view labelled active user shows the current active user connected to the server.

#### Client:

The client will present user with the active user list in the chat server as well as messages that been shared by users as well as notification send by server to let users know of new user joining or leaving the chat.

-	List view labelled messages shows all the user’s message as well as notification send by server regarding user leaving and joining the chat server.
-	List view labelled active users shows all the user connected to the server excluding itself.
