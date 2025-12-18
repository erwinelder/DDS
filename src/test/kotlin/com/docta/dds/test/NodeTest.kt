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
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NodeTest {

    private fun ApplicationTestBuilder.configureApplication() {
        application {
            configureSerialization()
            configureHTTP()
            configureStatusPages()
            configureDI(mainModule)
        }
    }

    @Test
    fun `nodeState returns empty state for unregistered nodes`() = testApplication {
        configureApplication()
        startApplication()

        listOf(
            "192.168.64.3",
            "192.168.64.4"
        ).forEach { nodeIp ->
            val service = application.get<NodeRestController> { parametersOf(nodeIp) }

            callCatching { service.getState() }.getOrThrow().apply {
                val data = getDataOrNull()
                assertNotNull(actual = data, "Result data should not be null")

                assertNull(actual = data.nodeId, "Node ID should be null for unregistered node")
                assertFalse(actual = data.isLeader, "Unregistered node should not be leader")
                assertNull(actual = data.successorAddress, "Successor should be null for unregistered node")
                assertNull(actual = data.predecessorAddress, "Predecessor should be null for unregistered node")
            }
        }
    }

    @Test
    fun `nodes registration is successful`() = testApplication {
        configureApplication()
        startApplication()

        val nodeIp1 = "192.168.64.3"
        val nodeIp2 = "192.168.64.4"
        val service1 = application.get<NodeRestController> { parametersOf(nodeIp1) }
        val service2 = application.get<NodeRestController> { parametersOf(nodeIp2) }

        callCatching { service1.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertEquals(expected = Error.NodeIsNotRegisteredYet, actual = getErrorOrNull())
        }

        callCatching { service1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull(), "Node 1 should register successfully")
        }

        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data, "Result data should not be null")

            assertNotNull(actual = data.nodeId, "Node ID should not be null for registered node")
            assertTrue(actual = data.isLeader, "First registered node should be leader")
            assertNull(actual = data.successorAddress)
            assertNull(actual = data.predecessorAddress)
        }

        callCatching { service2.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull(), "Node 2 should register successfully")
        }

        callCatching { service1.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data, "Result data should not be null")

            assertNotNull(actual = data.nodeId, "Node ID should not be null for registered node")
            assertTrue(actual = data.isLeader, "First registered node should be leader")
            assertEquals(expected = data.successorAddress, actual = nodeIp2)
            assertEquals(expected = data.predecessorAddress, actual = nodeIp2)
        }

        callCatching { service2.getState() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(data, "Result data should not be null")

            assertNotNull(actual = data.nodeId, "Node ID should not be null for registered node")
            assertFalse(actual = data.isLeader, "Second registered node should not be leader")
            assertEquals(expected = data.successorAddress, actual = nodeIp1)
            assertEquals(expected = data.predecessorAddress, actual = nodeIp1)
        }
    }

}