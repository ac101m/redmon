package com.ac101m.redmon.utils

open class RedmonException(message: String, cause: Throwable? = null): Exception(message, cause)
open class UnsupportedProfileVersionException(message: String, cause: Throwable? = null): RedmonException(message, cause)
open class NoActiveProfileException(message: String): RedmonException(message)
open class NoActiveInstructionSetException(message: String): RedmonException(message)
