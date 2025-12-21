package com.docta.dds.test

import com.docta.dds.config.configureDI
import com.docta.dds.config.configureHTTP
import com.docta.dds.config.configureSerialization
import com.docta.dds.config.configureStatusPages
import com.docta.dds.di.mainModule
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestController
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.drpc.core.network.context.callCatching
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get
import kotlin.test.*

class NodeIntegrationTest {

    private fun ApplicationTestBuilder.configureApplication() {
        application {
            configureSerialization()
            configureHTTP()
            configureStatusPages()
            configureDI(mainModule)
        }
    }

    val nodeIp1 = "192.168.64.3"
    val nodeIp2 = "192.168.64.4"
    val nodeIp3 = "192.168.64.5"


    @Test
    fun `nodeState returns empty state for unregistered nodes`() = testApplication {
        configureApplication()
        startApplication()
        ProcessBuilder("scripts/rerun_remote_docker_containers.sh").start().waitFor()
        delay(500)

        listOf(
            nodeIp1, nodeIp2, nodeIp3
        ).forEach { nodeIp ->
            val service = application.get<NodeRestController> { parametersOf(nodeIp) }

            callCatching { service.getState() }.getOrThrow().apply {
                val data = getDataOrNull()
                assertNotNull(actual = data)

                assertNull(actual = data.nodeId)
                assertNull(actual = data.leaderId)
                assertNull(actual = data.leaderAddress)
                assertFalse(actual = data.isLeader)
                assertNull(actual = data.successorAddress)
                assertNull(actual = data.predecessorAddress)
                assertNull(actual = data.predecessorOfPredecessorAddress)
            }
        }
    }

    @Test
    fun `nodes registration is successful`() = testApplication {
        configureApplication()
        startApplication()
        ProcessBuilder("scripts/rerun_remote_docker_containers.sh").start().waitFor()
        delay(500)

        val service1 = application.get<NodeRestController> { parametersOf(nodeIp1) }
        val service2 = application.get<NodeRestController> { parametersOf(nodeIp2) }
        val service3 = application.get<NodeRestController> { parametersOf(nodeIp3) }

        callCatching { service1.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertEquals(actual = getErrorOrNull(), expected = Error.NodeIsNotRegisteredYet)
        }
        callCatching { service2.join(greeterIpAddress = nodeIp3) }.getOrThrow().apply {
            assertEquals(actual = getErrorOrNull(), expected = Error.NodeIsNotRegisteredYet)
        }
        callCatching { service3.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertEquals(actual = getErrorOrNull(), expected = Error.NodeIsNotRegisteredYet)
        }

        callCatching { service1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp1)
            assertEquals(actual = data.leaderId, expected = data.nodeId)
            assertEquals(actual = data.leaderAddress, expected = nodeIp1)
            assertTrue(actual = data.isLeader)
            assertNull(actual = data.successorAddress)
            assertNull(actual = data.predecessorAddress)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }

        callCatching { service2.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp1)
            assertEquals(actual = data.successorAddress, expected = nodeIp2)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp2)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp2)
            assertEquals(actual = data.successorAddress, expected = nodeIp1)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp1)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }

        callCatching { service3.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp1)
            assertEquals(actual = data.successorAddress, expected = nodeIp2)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp3)
            assertEquals(actual = data.predecessorOfPredecessorAddress, expected = nodeIp2)
        }
        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp2)
            assertEquals(actual = data.successorAddress, expected = nodeIp3)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp1)
            assertEquals(actual = data.predecessorOfPredecessorAddress, expected = nodeIp3)
        }
        callCatching { service3.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp3)
            assertEquals(actual = data.successorAddress, expected = nodeIp1)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp2)
            assertEquals(actual = data.predecessorOfPredecessorAddress, expected = nodeIp1)
        }
    }

    @Test
    fun `new leader with max id is elected after nodes join`() = testApplication {
        configureApplication()
        startApplication()
        ProcessBuilder("scripts/rerun_remote_docker_containers.sh").start().waitFor()
        delay(500)

        val service1 = application.get<NodeRestController> { parametersOf(nodeIp1) }
        val service2 = application.get<NodeRestController> { parametersOf(nodeIp2) }
        val service3 = application.get<NodeRestController> { parametersOf(nodeIp3) }
        var node1State: NodeStateDto
        var node2State: NodeStateDto
        var node3State: NodeStateDto

        callCatching { service1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service2.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service3.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)
            node1State = data
        }
        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)
            node2State = data
        }
        callCatching { service3.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)
            node3State = data
        }

        val nodeIpToAddress = mapOf(
            node1State.nodeId!! to node1State.nodeAddress,
            node2State.nodeId!! to node2State.nodeAddress,
            node3State.nodeId!! to node3State.nodeAddress
        )
        val expectedLeaderId = nodeIpToAddress.keys.maxOrNull()!!
        val expectedLeaderAddress = nodeIpToAddress[expectedLeaderId]!!

        assertEquals(actual = node1State.leaderId, expected = expectedLeaderId)
        assertEquals(actual = node1State.leaderAddress, expected = expectedLeaderAddress)
        assertEquals(actual = node1State.isLeader, expected = node1State.nodeId == expectedLeaderId)

        assertEquals(actual = node2State.leaderId, expected = expectedLeaderId)
        assertEquals(actual = node2State.leaderAddress, expected = expectedLeaderAddress)
        assertEquals(actual = node2State.isLeader, expected = node2State.nodeId == expectedLeaderId)

        assertEquals(actual = node3State.leaderId, expected = expectedLeaderId)
        assertEquals(actual = node3State.leaderAddress, expected = expectedLeaderAddress)
        assertEquals(actual = node3State.isLeader, expected = node3State.nodeId == expectedLeaderId)
    }

    @Test
    fun `ring recovers successfully after node death`() = testApplication {
        configureApplication()
        startApplication()
        ProcessBuilder("scripts/rerun_remote_docker_containers.sh").start().waitFor()
        delay(500)

        val service1 = application.get<NodeRestController> { parametersOf(nodeIp1) }
        val service2 = application.get<NodeRestController> { parametersOf(nodeIp2) }
        val service3 = application.get<NodeRestController> { parametersOf(nodeIp3) }

        callCatching { service1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service2.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service3.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        ProcessBuilder("scripts/stop_remote_docker_container.sh", nodeIp2).start().waitFor()
        delay(6000)

        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp1)
            assertEquals(actual = data.successorAddress, expected = nodeIp3)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp3)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service3.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertNotNull(actual = data.nodeId)
            assertEquals(actual = data.nodeAddress, expected = nodeIp3)
            assertEquals(actual = data.successorAddress, expected = nodeIp1)
            assertEquals(actual = data.predecessorAddress, expected = nodeIp1)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
    }

}