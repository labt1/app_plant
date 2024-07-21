package com.example.catdogclassification

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity2 : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var welcomeText :TextView
    lateinit var messageEditText:EditText
    lateinit var sendButton:ImageButton
    lateinit var messageList:MutableList<Message>
    lateinit var messageAdapter:MessageAdapter
    val client = OkHttpClient()
    //val resultado = intent.getStringExtra("RESULTADO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        messageList = ArrayList()
        recyclerView = findViewById(R.id.recycler_view)
        welcomeText = findViewById(R.id.welcome_text)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_bt)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val layoutManger = LinearLayoutManager(this)
        layoutManger.stackFromEnd = true
        recyclerView.layoutManager = layoutManger

        val textViewResultado = findViewById<TextView>(R.id.textViewResultado)
        val resultado = intent.getStringExtra("RESULTADO")
        textViewResultado.text = resultado ?: "No se recibió ningún resultado"

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim{ it <= ' '}
            addToChat(question,Message.SENT_BY_ME)
            messageEditText.setText("")
            //callAPI(question)
            addResponse(getResponseSafely(question))
            welcomeText.visibility = View.GONE
        }
    }

    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread{
            messageList.add(Message(message,sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }

    }

    fun addResponse(response:String?){
        messageList.removeAt(messageList.size -1)
        addToChat(response!!,Message.SENT_BY_BOT)
    }

    fun getResponse(prompt: String): String? {

        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", "gpt-3.5-turbo")

            val messageArr = JSONArray()
            val obj = JSONObject()
            obj.put("role", "user")
            obj.put("content", prompt)
            messageArr.put(obj)

            jsonBody.put("messages", messageArr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer ____api_key____")
            .post(body)
            .build()

        Log.d("myTag", "This is my message entry")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error: ${response.code}")
                println("Error Body: ${response.body?.string()}")
                return null
            } else {
                val responseBody = response.body?.string()
                return JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            }
        }
    }

    fun getResponseSafely(prompt: String): String {
        return try {
            val response = getResponse(prompt)
            response ?: "Error: No response from GPT."
        } catch (e: Exception) {
            Log.d("myTag", "Error: ${e.message}")
            "Exception: ${e.message}"
        }
    }
}