package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.persistence.v1.PersistentSignalV1
import com.fasterxml.jackson.annotation.JsonProperty

class PersistentColumnV2(
    @param:JsonProperty("signals", required = true)
    var signals: List<PersistentSignalV1>
)
