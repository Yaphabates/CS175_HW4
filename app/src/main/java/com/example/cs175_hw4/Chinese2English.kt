package com.example.cs175_hw4

import com.google.gson.annotations.SerializedName
class Chinese2English {
    @SerializedName("web_trans") val web_trans : Web_trans?=null

    class Web_trans{
        @SerializedName("web-translation") val web_translation : List<Web_translation>? = null
    }
    class  Web_translation{
        @SerializedName("trans") val trans : List<Trans>?=null
    }
    class Trans{
        @SerializedName("value") val value : String?=null
    }
}