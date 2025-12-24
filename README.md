# DDS

DDS is a sample project demonstrating a simple distributed system architecture using Kotlin and Ktor.
The app uses Peterson algorithm, circle (ring) topology, and [dRPC](https://github.com/erwinelder/dRPC) for communication between nodes. It also implements leader election logic and a simple chat functionality.

Creation and setup of a simplified version of this project was described in the following [post on Medium](https://medium.com/@erwinelder/how-to-build-a-basic-distributed-system-9b62c0e87ff1).

# Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Logic

### Node State

Each node maintains the following state:

- Its own information (UUID, address)
- Leader information (UUID, address)
- Reference to its next neighbor nodes (successor and grand-successor)
- Reference to its previous neighbor node (predecessor)

This state is initialized when the node joins the node pool.

### Node joins the node pool (the ring)

On joining the node pool, the following sequence of actions occurs:

1. Node sends a join request to a greeter node - an existing node in the pool
   - If greater node is not specified, the node joins the pool as the first node and proclaims itself as the leader; end of the join process
2. Greeter node updates its own neighbor references to include the new node and sends the information about the new node to its neighbors
3. Greater node responds to the new node with necessary registration information (new node's neighbors, leader info, chat state)
4. New node initializes its state based on the received information
5. If the new node id is greater than the current leader id, it starts the leader election process

### Node leaves the node pool (the ring)

When a node leaves the node pool, the following sequence of actions occurs:

1. Node sends a leave notification to its neighbor nodes
2. Neighbor nodes update their neighbor information to bypass the leaving node
3. If the leaving node is the leader, a new leader election process is initiated by the predecessor node
4. Leaving node stops its process and exits

### Ring recovery after node death

When a node detects that its successor is unresponsive, it initiates the ring recovery protocol:

1. Node updates the predecessor information for its grand-successor to point to itself
2. Node updates its own successors information to bypass the unresponsive node
3. If the unresponsive node was the leader, a new leader election process is initiated

### Leader election

The leader election process is initiated in one of the following scenarios:

- A new node with a higher UUID joins the pool
- The current leader node leaves the pool
- The leader was proclaimed dead during ring recovery

The election process follows these steps:

1. Node sends an election message to its successor with its own UUID
2. Each node compares the received UUID with its own:
   - If the received UUID is greater, it updates its leader information and forwards the message to its successor
   - If the received UUID is smaller, it forwards its own UUID to its successor
   - If the received UUID is equal to its own, it proclaims itself as the new leader

### Chat functionality

The chat functionality allows nodes to send and receive messages within the node pool. The chat messages order is maintained by the leader node.
When a node sends a chat message, the following sequence of actions occurs:

1. Node sends the chat message request to its successor
2. Each node forwards the message to its successor until it reaches the leader node
3. Leader node maps the message request to a chat message with a message ID, adds it to the chat history, and broadcasts the message to its successor
4. Each node adds the received chat message to its local chat history and forwards it to its successor until it reaches to the leader node again

## API

The system uses dRPC for communication between nodes. It means that interacting with a node is intended to be done via service interfaces (com.docta.dds.presentation.service package). Even though each service method can be called via HTTP endpoints.
For more information on how dRPC works, please refer to [README file in dRPC repository on GitHub](https://github.com/erwinelder/dRPC).
For quick overview of how to call one of service methods via HTTP, you can refer to the examples after the list of available methods.

### Node service

- '/Node/getState' - Get the current node state
- 'Node/setMessageDelay' - Set artificial delay for each call to other nodes
- 'Node/isAlive' - Health check endpoint, returns SimpleResult.Success if the node is alive
- 'Node/join' - Join the node pool, requires JoinRequest with greeter node (greeter node IP address or empty string for first node)
- 'Node/leave' - Leave the node pool gracefully
- 'Node/kill' - Stop the node process immediately
- 'Node/startElection' - Start the leader election process (for manual intervention)

There are more methods in the Node service, but they are intended for internal use only.

### Chat service

- 'Chat/getChatHistory' - Get the current chat history from the node pool
- 'Chat/sendMessage' - Send a chat message to the node pool

There are more methods in the Chat service, but they are intended for internal use only.

### Example HTTP requests

```
curl -X POST http://192.168.64.3:8080/Node/join \
  -H "Content-Type: application/json" \
  -d '{
        "0": ""
      }'
      
curl -X POST http://192.168.64.4:8080/Node/join \
  -H "Content-Type: application/json" \
  -d '{
        "0": "192.168.64.3"
      }'
      
curl -X POST http://192.168.64.3:8080/Node/getState \
  -H "Content-Type: application/json" \
  -d '{}'
```