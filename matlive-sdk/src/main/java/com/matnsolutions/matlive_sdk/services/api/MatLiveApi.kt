package com.matnsolutions.matlive_sdk.services.api

import com.matnsolutions.matlive_sdk.services.CreateRoomRequest
import com.matnsolutions.matlive_sdk.services.UpdateMetadataRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface MatLiveApi {
    @POST("rooms/create-room")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<Map<String, Any>>

    @PUT("rooms/room-metadata")
    suspend fun updateRoomMetadata(@Body request: UpdateMetadataRequest): Response<Map<String, Any>>

    @GET("rooms/token")
    suspend fun createToken(
        @Query("identity") identity: String,
        @Query("room") room: String,
        @Query("appKey") appKey: String,
    ): Response<Map<String, Any>>
}
