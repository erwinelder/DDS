package com.docta.dds.test

import com.docta.dds.config.configureDI
import com.docta.dds.config.configureHTTP
import com.docta.dds.config.configureSerialization
import com.docta.dds.config.configureStatusPages
import com.docta.dds.di.mainModule
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestController
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
            assertEquals(expected = Error.NodeIsNotRegisteredYet, actual = getErrorOrNull())
        }
        callCatching { service2.join(greeterIpAddress = nodeIp3) }.getOrThrow().apply {
            assertEquals(expected = Error.NodeIsNotRegisteredYet, actual = getErrorOrNull())
        }
        callCatching { service3.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertEquals(expected = Error.NodeIsNotRegisteredYet, actual = getErrorOrNull())
        }

        callCatching { service1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
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
            assertTrue(actual = data.isLeader)
            assertEquals(expected = data.successorAddress, actual = nodeIp2)
            assertEquals(expected = data.predecessorAddress, actual = nodeIp2)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertFalse(actual = data.isLeader)
            assertEquals(expected = data.successorAddress, actual = nodeIp1)
            assertEquals(expected = data.predecessorAddress, actual = nodeIp1)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }

        callCatching { service3.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertTrue(actual = data.isLeader)
            assertEquals(expected = nodeIp2, actual = data.successorAddress)
            assertEquals(expected = nodeIp3, actual = data.predecessorAddress)
            assertEquals(expected = nodeIp2, actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertFalse(actual = data.isLeader)
            assertEquals(expected = nodeIp3, actual = data.successorAddress)
            assertEquals(expected = nodeIp1, actual = data.predecessorAddress)
            assertEquals(expected = nodeIp3, actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service3.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertFalse(actual = data.isLeader)
            assertEquals(expected = nodeIp1, actual = data.successorAddress)
            assertEquals(expected = nodeIp2, actual = data.predecessorAddress)
            assertEquals(expected = nodeIp1, actual = data.predecessorOfPredecessorAddress)
        }
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
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertTrue(actual = data.isLeader)
            assertEquals(expected = nodeIp3, actual = data.successorAddress)
            assertEquals(expected = nodeIp3, actual = data.predecessorAddress)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
        callCatching { service3.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data)

            assertNotNull(actual = data.nodeId)
            assertFalse(actual = data.isLeader)
            assertEquals(expected = nodeIp1, actual = data.successorAddress)
            assertEquals(expected = nodeIp1, actual = data.predecessorAddress)
            assertNull(actual = data.predecessorOfPredecessorAddress)
        }
    }

}