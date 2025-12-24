package com.docta.dds.test

import com.docta.dds.config.configureDI
import com.docta.dds.config.configureHTTP
import com.docta.dds.config.configureSerialization
import com.docta.dds.config.configureStatusPages
import com.docta.dds.di.chatModule
import com.docta.dds.di.mainModule
import com.docta.dds.di.nodeModule
import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.presentation.controller.ChatRestController
import com.docta.dds.presentation.controller.NodeRestController
import com.docta.drpc.core.network.context.callCatching
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChatIntegrationTest {

    private fun ApplicationTestBuilder.configureApplication() {
        application {
            configureSerialization()
            configureHTTP()
            configureStatusPages()
            configureDI(mainModule, nodeModule, chatModule)
        }
    }

    val nodeIp1 = "192.168.64.3"
    val nodeIp2 = "192.168.64.4"
    val nodeIp3 = "192.168.64.5"


    @Test
    fun `sent message successfully broadcasts to each node`() = testApplication {
        configureApplication()
        startApplication()
        ProcessBuilder("scripts/restart_remote_docker_containers.sh").start().waitFor()
        delay(500)

        val nodeService1 = application.get<NodeRestController> { parametersOf(nodeIp1) }
        val nodeService2 = application.get<NodeRestController> { parametersOf(nodeIp2) }
        val nodeService3 = application.get<NodeRestController> { parametersOf(nodeIp3) }
        val chatService1 = application.get<ChatRestController> { parametersOf(nodeIp1) }
        val chatService2 = application.get<ChatRestController> { parametersOf(nodeIp2) }
        val chatService3 = application.get<ChatRestController> { parametersOf(nodeIp3) }

        callCatching { nodeService1.join(greeterIpAddress = "") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }
        callCatching { nodeService2.join(greeterIpAddress = nodeIp1) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        callCatching { chatService1.sendMessage(text = "Hello from 1st node!") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        var expectedChatMessages = listOf(
            ChatMessage(messageId = 1, message = "Hello from 1st node!", senderAddress = nodeIp1)
        )
        callCatching { chatService1.getChatHistory() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertEquals(actual = data.seq, expected = 1)
            assertEquals(actual = data.messageHistory, expected = expectedChatMessages)
        }
        callCatching { chatService2.getChatHistory() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertEquals(actual = data.seq, expected = 1)
            assertEquals(actual = data.messageHistory, expected = expectedChatMessages)
        }

        callCatching { nodeService3.join(greeterIpAddress = nodeIp2) }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        callCatching { chatService2.sendMessage(text = "Hello from 2st node!") }.getOrThrow().apply {
            assertNull(actual = getErrorOrNull())
        }

        expectedChatMessages = listOf(
            ChatMessage(messageId = 1, message = "Hello from 1st node!", senderAddress = nodeIp1),
            ChatMessage(messageId = 2, message = "Hello from 2st node!", senderAddress = nodeIp2)
        )
        callCatching { chatService1.getChatHistory() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertEquals(actual = data.seq, expected = 2)
            assertEquals(actual = data.messageHistory, expected = expectedChatMessages)
        }
        callCatching { chatService2.getChatHistory() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertEquals(actual = data.seq, expected = 2)
            assertEquals(actual = data.messageHistory, expected = expectedChatMessages)
        }
        callCatching { chatService3.getChatHistory() }.getOrThrow().apply {
            val data = getDataOrNull()
            assertNotNull(actual = data)

            assertEquals(actual = data.seq, expected = 2)
            assertEquals(actual = data.messageHistory, expected = expectedChatMessages)
        }
    }

}