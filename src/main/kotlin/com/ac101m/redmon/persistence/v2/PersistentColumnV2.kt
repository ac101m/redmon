package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentColumnV2(
    @param:JsonProperty("signals", required = true)
    var signals: List<PersistentSignalV2>
)
