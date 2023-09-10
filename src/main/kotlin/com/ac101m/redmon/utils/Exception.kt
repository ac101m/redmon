package com.ac101m.redmon.utils

open class RedmonException(message: String, cause: Throwable? = null): Exception(message, cause)
class RedmonConfigurationException(message: String, cause: Throwable? = null): RedmonException(message, cause)
class RedmonCommandException(message: String, cause: Throwable? = null): RedmonException(message, cause)
