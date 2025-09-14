package com.storm.tornadoai
sealed class Responder { enum class Intent { GREETING, HELP, DENY, OTHER } }