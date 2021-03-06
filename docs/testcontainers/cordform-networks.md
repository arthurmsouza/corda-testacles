
# Cordform Containers 

If your project uses the [Cordform](https://docs.corda.net/docs/corda-os/4.5/generating-a-node.html#tasks-using-the-cordform-plug-in) 
Gradle task (usually named `deployNodes`), you may find convenient 
use of it's output as the source for a [Testcontainers](https://www.testcontainers.org/)-based Corda network during testing. 

The `corda-testacles-testcontainers` 
module provides two ways to do just that, 
again based on the _nodes_ directory created by `Cordform`:

- `CordformNetworkExtension`: a convenient JUnit5 extension 
- `CordformNetworkContainer`: a container type that can be used directly 
in conjunction with Testcontainers' `@Testcontainers` and `@Container` annotations. 

Cordform-based containers may not seem that useful, but they can help around a cluttered classpath 
for both cordapp and RPC sides while testing. Future versions will support dynamic network 
configurations without a Cordform's _node_ folder. 

## Gradle Config

Optional: To simplify your Gradle builds, make testing tasks dependent to `deployNodes`:

```groovy
project.afterEvaluate {
    check.dependsOn(":deployNodes")
}
```  

Also, you probably want to increase the Java Heap of node containers by updating 
each node one in `deployNodes` with `custom.jvmArgs`, including a garbage collector 
as shown bellow:

```
node {
    name "..."
    //...
    extraConfig = [
            //...
            'custom.jvmArgs': ["-Xmx2G", "-XX:+UseG1GC"]
    ]
}
``` 



## Cordform Network Extension

An example of using `CordformNetworkExtension` in a simple 
RPC test:

```kotlin
@ExtendWith(CordformNetworkExtension::class)
class CordformNetworkExtensionTest {

    companion object {
        // Optional, defaults to corda/corda-zulu-java1.8-4.5
        @NodesImageName
        @JvmStatic
        val nodesImageName = DEFAULT_CORDA_IMAGE_NAME_4_5
    
        // Optional, defaults to new network
        @NodesNetwork
        @JvmStatic
        val nodesNetwork = Network.newNetwork()
    
        // Optional, defaults to auto-lookup (build/nodes, ../build/nodes)
        @NodesDir
        @JvmStatic
        val nodesDir = File(System.getProperty("user.dir"))
                .resolve("build/nodes")
    }
    
    // The extension implements a ParameterResolver 
    // for CordformNetworkContainer 
    @Test
    fun `Can send a yo`(nodesContainer: CordformNetworkContainer) {
        val rpcOps = nodesContainer.getNode("partya")
            .getRpc(/* optional user or username */)
        // Do something...
    }
}
```

## Cordform Network Container

When `CordformNodesExtension` does not provide the right trade-off between 
abstraction and API access, using `CordformNetworkContainer` directly might 
be a better alternative. Have a look at `CordformContainersSpringBootTest` 
for a sample  involving Spring Boot's `@DynamicPropertySource` annotation.

A simpler example of a `CordformNetworkContainer`-based RPC test is shown bellow:

```kotlin
@Testcontainers
class CordformNetworkContainerRpcTest {

    companion object {
        @Container 
        @JvmStatic
        val nodesContainer = CordformNetworkContainer(
                nodesDir = File(System.getProperty("user.dir"))
                    .parentFile.resolve("build/nodes"),
                // Will clone nodesDir to build/testacles/{random UUID} 
                // and use that instead
                cloneNodesDir = true)
    }

    @Test
    fun `Can send a yo`() {
        val rpcOps = nodesContainer.getNode("partya")
            .getRpc(/* optional user or username */)
        // Do something...

    }
}
```