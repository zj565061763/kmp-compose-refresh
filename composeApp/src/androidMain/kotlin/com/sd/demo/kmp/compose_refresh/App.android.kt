package com.sd.demo.kmp.compose_refresh

import android.util.Log

actual fun logMsg(tag: String, block: () -> String) {
  Log.d(tag, block())
}