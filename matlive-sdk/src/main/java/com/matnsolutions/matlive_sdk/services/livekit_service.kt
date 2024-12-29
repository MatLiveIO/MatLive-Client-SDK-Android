package com.matnsolutions.matlive_sdk.services

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Data classes for requests and responses
data class CreateRoomRequest(
    val roomName: String
)

data class UpdateMetadataRequest(
    val roomId: String,
    val metadata: String
)

// Retrofit interface for API calls
interface LiveKitApi {
    @POST("livekit/create-room")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<Map<String, Any>>

    @PUT("livekit/room-metadata")
    suspend fun updateRoomMetadata(@Body request: UpdateMetadataRequest): Response<Map<String, Any>>

    @GET("livekit/token")
    suspend fun createToken(
        @Query("identity") identity: String,
        @Query("room") room: String
    ): Response<Map<String, Any>>
}

class LiveKitService(baseUrl: String) {
    private val retrofit = RetrofitClient.getInstance(baseUrl)
    private val api = retrofit.create(LiveKitApi::class.java)

    suspend fun createRoom(roomId: String): Result<Map<String, Any>> {
        return try {
            val response = api.createRoom(CreateRoomRequest(roomId))
            if (response.isSuccessful) {
                response.body()?.let {
                    println("Create room response: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to create room: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create room: ${e.message}"))
        }
    }

    suspend fun updateRoomMetadata(roomId: String, metadata: String): Result<Map<String, Any>> {
        return try {
            val response = api.updateRoomMetadata(UpdateMetadataRequest(roomId, metadata))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to update metadata: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update metadata: ${e.message}"))
        }
    }

    suspend fun createToken(username: String, roomId: String): Result<Map<String, Any>> {
        return try {
            val response = api.createToken(username, roomId)
            if (response.isSuccessful) {
                response.body()?.let {
                    println("Create token response: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to create token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create token: ${e.message}"))
        }
    }
}

// Utility class for creating Retrofit instance
object RetrofitClient {
    fun getInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

//import io.ktor.client.*
//import io.ktor.client.call.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.*
////import io.ktor.client.plugins.logging.*
//import io.ktor.client.request.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import kotlinx.serialization.json.Json
//import com.matnsolutions.matlive_sdk.utils.kPrint
//
//class LiveKitService(private val baseUrl: String) {
//
//    private val client = HttpClient(CIO) {
//        install(ContentNegotiation) {
//            json(Json {
//                ignoreUnknownKeys = true
//                isLenient = true
//            })
//        }
////        install(Logging) {
////            logger = object : Logger {
////                override fun log(message: String) {
////                    kPrint(message)
////                }
////            }
////            level = LogLevel.ALL
////        }
//    }
//
//    suspend fun createRoom(roomId: String): Map<String, Any> {
//        try {
//            val response = client.post("$baseUrl/livekit/create-room") {
//                contentType(ContentType.Application.Json)
//                setBody(mapOf("roomName" to roomId))
//            }
//            val responseBody: Map<String, Any> = response.body()
//            kPrint(responseBody)
//            return responseBody
//        } catch (e: Exception) {
//            throw Exception("Failed to create room: $e")
//        }
//    }
//
//    suspend fun updateRoomMetadata(roomId: String, metadata: String): Map<String, Any> {
//        try {
//            val response = client.put("$baseUrl/livekit/room-metadata") {
//                 contentType(ContentType.Application.Json)
//                setBody(mapOf("roomId" to roomId, "metadata" to metadata))
//            }
//            return response.body()
//        } catch (e: Exception) {
//            throw Exception("Failed to update metadata: $e")
//        }
//    }
//
//    suspend fun createToken(username: String, roomId: String): Map<String, Any> {
//        try {
//            val response = client.get("$baseUrl/livekit/token") {
//                parameter("identity", username)
//                parameter("room", roomId)
//            }
//            val responseBody: Map<String, Any> = response.body()
//            kPrint(responseBody)
//            return responseBody
//        } catch (e: Exception) {
//            throw Exception("Failed to create token: $e")
//        }
//    }
//}