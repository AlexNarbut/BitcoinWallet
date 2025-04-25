package com.test.commonextens.utils

import android.content.Context
import com.test.common.response.Response
import com.test.commonextens.response.safeRun
import java.io.BufferedReader
import java.io.InputStreamReader

fun readDataFromAssertFile(context: Context, fileName: String): Response<String> = safeRun {
    val inputStream = context.assets.open(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val data = reader.use { it.readText() }
    data
}