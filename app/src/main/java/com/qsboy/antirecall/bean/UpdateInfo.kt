package com.qsboy.antirecall.bean

data class UpdateInfo(
    val forced: String,
    val ignore: String,
    val md5: String,
    val updateContent: String,
    val updateDate: String,
    val updateUrl: String,
    val versionCode: String,
    val versionName: String
)