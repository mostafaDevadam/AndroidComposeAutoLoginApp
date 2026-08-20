package com.example.autologinapp

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray

sealed interface UiVState {
    object Loading: UiVState
    data class Success(val message: String, val status: String, val success: Boolean): UiVState
    data class Error(val errorMsg: String): UiVState
}

const val host = "<ip as host>"
const val port = "<port>"
class MsgVolleyViewModel(application: Application): AndroidViewModel(application)  {



   private val _uiVState = MutableStateFlow<UiVState>(UiVState.Loading)
    val uiVState: StateFlow<UiVState> = _uiVState

    // List item state
    private val _uiVListState = MutableStateFlow<UiListState>(UiListState.Loading)
    val uiVListState: StateFlow<UiListState> = _uiVListState

    private val requestQueue = Volley.newRequestQueue(application)

    init{
        fetchMessage()
        //fetchMessagesList()
        fetchMessagesArray()
    }

    fun fetchMessage() {
        _uiVState.value = UiVState.Loading

        val url = "$host:$port/api/test"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val message = response.optString("message", "No message found")
                val status = response.optString("status", "No status found")
                val success = response.optBoolean("success", false)
                _uiVState.value = UiVState.Success(message, status, success)

            },{ error ->
                _uiVState.value = UiVState.Error(error.localizedMessage ?:
                "Failed to connect to local server")

            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun fetchMessagesList() {

        _uiVListState.value = UiListState.Loading

        val url = "$host:$port/api/test/liste"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Log.d("SERVER_RESPONSE", response.toString())


                try {
                    val messageList = mutableListOf<MessageResponse>()

                    //val res = JSONArray(response)

                    //Log.d("RES#", res.toString())

                    val jsonArray = response.getJSONArray("data") // Replace "messages" with your actual JSON key

                    for(i in 0 until jsonArray.length()){
                        val obj = jsonArray.getJSONObject(i)
                        //val obj = res.getJSONObject(i)
                        //val obj = response.getJSONObject(response[i as String] as String)
                        val messageItem = MessageResponse(
                             id = obj.optInt("id", 0),
                             status = obj.optString("status", "no"),
                             success = obj.optBoolean("success", false),
                             message = obj.optString("message", "no"),
                             timestamp = obj.optString("timestamp", "no")
                        )

                        messageList.add(messageItem)
                    }

                    /*val messageItem = MessageResponse(
                        id = response.optInt("id", 0),
                        status = response.optString("status", "no"),
                        success = response.optBoolean("success", false),
                        message = response.optString("message", "no"),
                        timestamp = response.optString("timestamp", "no")
                    )*/

                    _uiVListState.value = UiListState.Success(messageList)
                    //_uiVListState.value = UiListState.Success(listOf(messageItem))

                } catch (e: Exception) {
                    _uiVListState.value = UiListState.Error(e.localizedMessage ?: "Network error")
                    _uiVListState.value = UiListState.Error(e.localizedMessage ?: "Parsing error")
                }


            },{ error ->
                _uiVState.value = UiVState.Error(error.localizedMessage ?:
                "Failed to connect to local server")

            }
        )

        requestQueue.add(jsonObjectRequest)


    }


    fun fetchMessagesArray() {

        _uiVListState.value = UiListState.Loading

        val url = "$host:$port/api/test"

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val messageList = mutableListOf<MessageResponse>()

                    for(i in 0 until response.length()){
                        val obj = response.getJSONObject(i)
                        //val obj = res.getJSONObject(i)
                        //val obj = response.getJSONObject(response[i as String] as String)
                        val messageItem = MessageResponse(
                            id = obj.optInt("id", 0),
                            status = obj.optString("status", "no"),
                            success = obj.optBoolean("success", false),
                            message = obj.optString("message", "no"),
                            timestamp = obj.optString("timestamp", "no")
                        )

                        messageList.add(messageItem)
                    }

                    _uiVListState.value = UiListState.Success(messageList)


                } catch (e: Exception) {
                    _uiVListState.value = UiListState.Error(e.localizedMessage ?: "Network error")
                    _uiVListState.value = UiListState.Error(e.localizedMessage ?: "Parsing error")
                }


            },{ error ->
                _uiVState.value = UiVState.Error(error.localizedMessage ?:
                "Failed to connect to local server")

            }
        )

        requestQueue.add(jsonObjectRequest)


    }



}