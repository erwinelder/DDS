# DDS

DDS (\[-\] distributed system) is a sample project demonstrating a simple distributed system architecture using Kotlin and Ktor.
The app uses Peterson algorithm, circle (ring) topology, and dRPC for communication between nodes. It also implements leader election logic.

## Building & Running

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

### Node joins the node pool (the ring)

\* Node N - a new node that joins the node pool

\* Node G - a node greeter node that is already in the node pool and was used by node N as a referral

\* Node S - next neighbor node (a successor)

\* Node P - previous neighbor node (a predecessor)

1. Node N receives a request at "join" endpoint
2. Node N sends a request to node G at "register-node" endpoint
3. Node G looks up its node S and sends a request at "update-neighbor" endpoint, passing node N's info
4. Node S updates its node P reference to node N
5. Node G constructs a response containing reference to node S and itself (node G)
6. Node G updates its node S reference to node N
7. Node G sends the response back to node N
8. Node N saves response data to its state