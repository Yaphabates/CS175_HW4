package com.example.cs175_hw4


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
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


class MainActivity : AppCompatActivity() {

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

    private val handler = Handler()

    private lateinit var btn: Button
    private lateinit var output_res: TextView
    private lateinit var input_words: EditText


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
        output_res = findViewById(R.id.trans_output)
        input_words = findViewById(R.id.trans_input)

        input_words.setOnClickListener(View.OnClickListener { v ->
            if (v.id == input_words.getId()) {
                // 光标置为可见
                input_words.setCursorVisible(true)
            }
        })
        input_words.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            input_words.setCursorVisible(false)
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
            translate()
        }
    }

    fun translate(){
        var res = ""
        val input :String = input_words.text.toString()
        if (input==""){
            handler.post {
                output_res.text=""
            }
        }
        val url = "https://dict.youdao.com/jsonapi?q=" + input
        request(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (isChinese(input)){
                    res =  "Fail to translate, please try again"
                }
                else{
                    res = "获取信息失败，请重试"
                }
                handler.post {
                    output_res.text = res
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                var ans = gson.fromJson(bodyString,translator::class.java)
                val nums = ans.web_trans?.web_translation?.get(0)?.trans?.size
                var i = 0
                while(i < nums!!.toInt()){
                    res += (i+1).toString() + "."
                    res += ans.web_trans?.web_translation?.get(0)?.trans?.get(i)?.value.toString() + "\n"
                    i++
                }
                handler.post {
                    output_res.text = res
                }
            }
        })

    }
    fun isChinese(msg:String):Boolean{
        return Character.isIdeographic(Character.codePointAt(msg, 0))
    }
}



