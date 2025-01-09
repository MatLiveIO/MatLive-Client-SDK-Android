package com.matnsolutions.matlive_sdk.services

import com.matnsolutions.matlive_sdk.services.api.CreateRoomRequest
import com.matnsolutions.matlive_sdk.services.api.MatLiveApi
import com.matnsolutions.matlive_sdk.services.api.RetrofitClient
import com.matnsolutions.matlive_sdk.services.api.UpdateMetadataRequest
import okhttp3.ResponseBody



class MatLiveService() {
    private val baseUrl = "https://webapi.dev.ml.matnsolutions.co/"
    private val retrofit = RetrofitClient.getInstance(baseUrl)
    private val api = retrofit.create(MatLiveApi::class.java)

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

    suspend fun createToken(username: String, roomId: String, appKey: String): Result<Map<String, Any>> {
        return try {
            val response = api.createToken(username, roomId, appKey)
            if (response.isSuccessful) {
                response.body()?.let {
                    println("Create token response: $it")
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = getErrorMessage(response.errorBody())
                Result.failure(Exception("Failed to join room: $errorMessage"))
            }} catch (e: Exception) {
            Result.failure(Exception("Failed to join room: ${e.message}"))
        }
    }

    private fun getErrorMessage(errorBody: ResponseBody?): String {
        return try {
            errorBody?.string()?.let { errorString ->
                val jsonObject = org.json.JSONObject(errorString)
                jsonObject.optString("message", "Unknown error")
            } ?: "Unknown error"
        } catch (e: Exception) {
            "Error parsing error message: ${e.message}"
        }
    }
}

