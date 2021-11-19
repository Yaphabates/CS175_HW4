package com.example.cs175_hw4


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import okhttp3.*
import com.google.gson.GsonBuilder
import okhttp3.EventListener
import okhttp3.Response
import java.io.IOException
import java.util.*




class MainActivity : AppCompatActivity() {

    private val handler = Handler()

    private lateinit var btn: Button
    private lateinit var show_res: TextView
    private lateinit var input_words: EditText

    private var mycache: HashMap<String,String> = HashMap<String,String>()


    val okhttpListener = object : EventListener()
    {
        override fun dnsStart(call: Call, domainName: String) {
            super.dnsStart(call, domainName)
        }
        override fun responseBodyStart(call: Call) {
            super.responseBodyStart(call)
        }
    }
    val client: OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(TimeConsumeInterceptor())
        .eventListener(okhttpListener).build()

    val gson = GsonBuilder().create()

    fun request(url: String, callback: Callback) {
        val request: Request = Request.Builder()
            .url(url)
            .header("User-Agent", "Sjtu-Android-OKHttp")
            .build()
        client.newCall(request).enqueue(callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.trans_btn)
        show_res = findViewById(R.id.trans_output)
        input_words = findViewById(R.id.trans_input)

        input_words.setOnClickListener(View.OnClickListener { v ->
            if (v.id == input_words.getId()) {
                // 光标置为可见
                input_words.setCursorVisible(true)
            }
        })
        input_words.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
//            input_words.isCursorVisible = false
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(
                    input_words.applicationWindowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
            false
        }
        )

        btn.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            get_translate_res()
        }
    }

    fun get_translate_res(){
        var res_msg=""
        val input :String = input_words.text.toString()
        if (input==""){
            handler.post {
                show_res.text=""
            }
        }
        Log.e("input",input)
        //check_cache
        if(input in mycache.keys){
            Log.e("cache", mycache[input].toString())
            handler.post {
                show_res.text=mycache[input].toString()
            }
            return
        }
        //========================

        if(containsHanScript(input)){
            val url = getAPI_youdao(input)
            request(url, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    res_msg =  "error, please try it later......"
                    handler.post {
                        show_res.text=res_msg
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    val bodyString = response.body?.string()
                    val ans = gson.fromJson(bodyString,Chinese2English::class.java)
                    val nums = ans.web_trans?.web_translation?.get(0)?.trans?.size
                    Log.e("num",nums.toString())
                    var i = 0
                    while(i < nums!!){
                        res_msg += ans.web_trans?.web_translation?.get(0)?.trans?.get(i)?.value.toString()
                        res_msg += ";"
                        i++
                    }
                    mycache.put(input,res_msg)
                    handler.post {
                        show_res.text=res_msg
                    }
                }
            })

        }else{
//          English2Chinese()
            val url = getAPI_youdao(input)
            request(url, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    res_msg =  "错误，请稍后再尝试......"
                    handler.post {
                        show_res.text=res_msg
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    val bodyString = response.body?.string()
                    Log.e("Get result",bodyString.toString())
                    val ans = gson.fromJson(bodyString,English2Chinese::class.java)
                    val nums = ans.web_trans?.web_translation?.get(0)?.trans?.size
                    Log.e("num",nums.toString())
                    var i= 0
                    while(i< nums!!){
                        res_msg +=ans.web_trans?.web_translation?.get(0)?.trans?.get(i)?.value.toString()
                        res_msg +="; "
                        i++
                    }
                    mycache.put(input,res_msg)
                    handler.post {
                        show_res.text=res_msg
                    }
                }
            })

        }
    }
}



class TimeConsumeInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.currentTimeMillis()
        val resp =  chain.proceed(chain.request())
        val endTime = System.currentTimeMillis()
        val url = chain.request().url.toString()
        Log.e("TimeConsumeInterceptor","request:$url cost time ${endTime - startTime}")
        return resp
    }
}