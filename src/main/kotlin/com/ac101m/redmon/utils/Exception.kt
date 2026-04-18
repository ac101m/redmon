package com.ac101m.redmon.utils

open class RedmonException(message: String, cause: Throwable? = null): Exception(message, cause)
open class UnsupportedProfileVersionException(message: String, cause: Throwable? = null): RedmonException(message, cause)
