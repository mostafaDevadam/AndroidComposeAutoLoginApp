package com.example.autologinapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


data class MessageResponse(
    val id: Int,
    val success: Boolean,
    val status: String,
    val message: String,
    val timestamp: String
)

interface ApiService {
    @GET("/api/test/")
    suspend fun getMessage(): MessageResponse

    @GET("/api/test/list")
    suspend fun getMessageList(): List<MessageResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "$host:$port/"

   /* private val gson = GsonBuilder()
        .setLenient()
        .create()
    */

    private val gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    val api: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

sealed interface UiState {
    object Loading: UiState
    data class Success(val message: String, val status: String, val success: Boolean): UiState
    data class Error(val errorMsg: String): UiState
}

sealed interface UiListState {
    object Loading: UiListState
    data class Success(val messages: List<MessageResponse>): UiListState
    data class Error(val errorMsg: String): UiListState
}

class MessageViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _uiListState = MutableStateFlow<UiListState>(UiListState.Loading)
    val uiListState: StateFlow<UiListState> = _uiListState

    init {
        fetchMessage()
        fetchMessageList()
    }

    fun fetchMessage(){
        viewModelScope.launch {
             _uiState.value = UiState.Loading
            try {
                val response = RetrofitInstance.api.getMessage()
                _uiState.value = UiState.Success(response.message, response.status, response.success)




            } catch (e: Exception){
                _uiState.value = UiState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    fun fetchMessageList(){
        viewModelScope.launch {
            _uiListState.value = UiListState.Loading
            try {
                val response = RetrofitInstance.api.getMessageList()
                _uiListState.value = UiListState.Success(response)


            } catch (e: Exception){
                _uiListState.value = UiListState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }

    }
}


