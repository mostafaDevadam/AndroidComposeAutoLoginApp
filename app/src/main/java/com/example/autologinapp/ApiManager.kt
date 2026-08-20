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
    val status: String,
    val message: String,
    val timestamp: String
)

interface ApiService {
    @GET("/")
    suspend fun getMessage(): MessageResponse
}

object RetrofitInstance {
    private const val BASE_URL = "<host+ip>"

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
    data class Success(val message: String): UiState
    data class Error(val errorMsg: String): UiState


}

class MessageViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        fetchMessage()
    }

    fun fetchMessage(){
        viewModelScope.launch {
             _uiState.value = UiState.Loading
            try {
                val response = RetrofitInstance.api.getMessage()
                _uiState.value = UiState.Success(response.message)
            } catch (e: Exception){
                _uiState.value = UiState.Error(e.localizedMessage ?: "Network error occurred")
            }
        }
    }
}

class MsgVolleyViewModel: ViewModel() {




}
