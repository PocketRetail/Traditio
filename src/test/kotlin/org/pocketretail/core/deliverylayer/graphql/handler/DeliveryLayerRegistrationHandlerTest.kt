package org.pocketretail.core.deliverylayer.graphql.handler

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.netflix.graphql.dgs.client.GraphQLResponse
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.pocketretail.core.deliverylayer.common.handler.ClientRequestsHandler
import org.pocketretail.core.deliverylayer.database.repo.ClientRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestParameterRepository
import org.pocketretail.core.deliverylayer.database.repo.ClientRequestRepository
import org.pocketretail.core.deliverylayer.event.HealthCheckerEvent
import org.pocketretail.core.deliverylayer.graphql.client.GraphQLWebFluxClient
import org.pocketretail.core.deliverylayer.rest.handler.DeliveryLayerRegistrationHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.StopWatch
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.util.*

@ExtendWith(MockitoExtension::class, SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class DeliveryLayerRegistrationHandlerTest {

    @MockBean
    private lateinit var healthCheckerEvent: HealthCheckerEvent

    companion object {
        @Container
        @ServiceConnection
        val database = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("DeliveryLayer")
            withUsername("postgres")
            withPassword("test")
        }
    }

    @Mock
    private val graphQLClient: MonoGraphQLClient = Mockito.mock(MonoGraphQLClient::class.java)

    @Autowired
    private lateinit var clientRequestRepository: ClientRequestRepository

    @Autowired
    private lateinit var clientRepository: ClientRepository

    @Autowired
    private lateinit var clientRequestParameterRepository: ClientRequestParameterRepository

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // DeliveryLayerRegistrationHandlerTest.kt
    @Test
    @Throws(FileNotFoundException::class)
    fun createClientWithAllTheirRequests() {
        val graphQLWebFluxClient = Mockito.mock(GraphQLWebFluxClient::class.java)
        `when`(graphQLWebFluxClient.createGraphQLClient("etts")).thenReturn(graphQLClient)
        `when`(graphQLClient.reactiveExecuteQuery(anyString())).thenReturn(
            Mono.just(GraphQLResponse(readCreateJson()!!))
        )
        // Ersetzen Sie die Factory in der ClientRequestsHandler-Instanz
        val clientRequestsHandler = ClientRequestsHandler(graphQLWebFluxClient)
        val underTest = DeliveryLayerRegistrationHandler(
            clientRequestsHandler,
            clientRepository,
            clientRequestRepository,
            clientRequestParameterRepository
        )

        val stopWatch = StopWatch()
        stopWatch.start()
        val response = underTest.registerClient("etts").block()!!
        stopWatch.stop()
        println("Time to execute: " + stopWatch.totalTimeSeconds)
        assertTrue(response.msg == "Client with id etts was successfully registered")
        Mockito.verify(graphQLClient, Mockito.times(1)).reactiveExecuteQuery(anyString())
        val actualParameters =
            clientRequestParameterRepository.findAll().collectList().block()!!
        val expectedParameters = getCreateDatabaseJson()
        actualParameters.sortBy { it.clientRequestParameterId }
        assert(actualParameters.size==85)
//        assertEquals(expectedParameters!!.replace("\r\n", "\n"), gson.toJson(actualParameters))
        //TODO : Implement check  for specifig parts of creation as example 2 times should this contains in json or this shouldnt contain in json
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun updateClientWithAllTheirRequests() {
        val graphQLWebFluxClient = Mockito.mock(GraphQLWebFluxClient::class.java)
        `when`(graphQLWebFluxClient.createGraphQLClient("etts")).thenReturn(graphQLClient)
        `when`(graphQLClient.reactiveExecuteQuery(anyString())).thenReturn(
            Mono.just(GraphQLResponse(readUpdateJson()!!))
        )
        // Ersetzen Sie die Factory in der ClientRequestsHandler-Instanz
        val clientRequestsHandler = ClientRequestsHandler(graphQLWebFluxClient)
        val underTest = DeliveryLayerRegistrationHandler(
            clientRequestsHandler,
            clientRepository,
            clientRequestRepository,
            clientRequestParameterRepository
        )
        val stopWatch = StopWatch()
        stopWatch.start()
        val response = underTest.registerClient("etts").block()!!
        stopWatch.stop()
        println("Time to execute: " + stopWatch.totalTimeSeconds)
        assertTrue(response.msg == "Client with id etts was successfully registered")
        Mockito.verify(graphQLClient, Mockito.times(1)).reactiveExecuteQuery(anyString())
        val actualParameters =
            clientRequestParameterRepository.findAll().collectList().block()!!

        val expectedParameters = getUpdateDatabaseJson()

        actualParameters.sortBy { it.clientRequestParameterId }


        assert(actualParameters.size==86)
        //assertEquals(expectedParameters!!.replace("\r\n", "\n"), gson.toJson(actualParameters))
        //TODO : Implement check  for specifig parts of creation as example 2 times should this contains in json or this shouldnt contain in json
    }

    private fun getUpdateDatabaseJson(): String? {
        val inputStream = this.javaClass.getResourceAsStream("/mocks/ETTS_UPDATE_DATABASE.json")
        if (inputStream == null) {
            throw FileNotFoundException("File not found")
        } else {
            try {
                return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getCreateDatabaseJson(): String? {
        val inputStream = this.javaClass.getResourceAsStream("/mocks/ETTS_CREATE_DATABASE.json")
        if (inputStream == null) {
            throw FileNotFoundException("File not found")
        } else {
            try {
                return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    @Throws(FileNotFoundException::class)
    private fun readCreateJson(): String? {
        val inputStream = this.javaClass.getResourceAsStream("/mocks/ETTS_GET_SCHEMAS_CREATE.json")
        if (inputStream == null) {
            throw FileNotFoundException("File not found")
        } else {
            try {
                return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    @Throws(FileNotFoundException::class)
    private fun readUpdateJson(): String? {
        val inputStream = this.javaClass.getResourceAsStream("/mocks/ETTS_GET_SCHEMAS_UPDATE.json")
        if (inputStream == null) {
            throw FileNotFoundException("File not found")
        } else {
            try {
                return inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}