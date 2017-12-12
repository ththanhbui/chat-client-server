## A simple Java chat client and server

A single server to support many clients.

To run the server, compile and run the file _ChatServer_ in package **server** with a port number:

_javac -d ./out server/ChatServer.java_,
_cd out; java server.ChatServer 1234_


To connect to the server from the client side, on another terminal, compile and run the file _ChatClient_ in package **client** with _localhost_ and _1234_ as the _hostname_ and _portnumber_:

_javac -d ./out client/ChatClient.java_,
_cd out; java client.ChatClient localhost 1234_


Currently working on popping up a proper GUIClient that has proper interface to type in text, "Send" button, etc.


Enjoy!
