package com.example.cs175_hw4

fun getAPI_youdao(input:String):String{
    return "https://dict.youdao.com/jsonapi?q=" + input
}

fun containsHanScript(msg:String):Boolean{
    fun isCJK(codepoint: Int): Boolean {
        return Character.isIdeographic(codepoint)
    }

    val length: Int = msg.length
    var offset = 0
    while (offset < length) {
        val codepoint: Int = Character.codePointAt(msg, offset)
        if (isCJK(codepoint))
            return true
        offset += Character.charCount(codepoint)
    }
    return false
}