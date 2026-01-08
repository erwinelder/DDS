package com.docta.dds.domain.model.election

object ElectionContext {

    var status: ElectionStatus = ElectionStatus.Active
        private set

    fun setStatus(status: ElectionStatus) {
        this.status = status
    }


    var seenHigherId: Boolean = false
        private set

    fun setSeenHigherId(value: Boolean) {
        seenHigherId = value
    }


    fun reset() {
        status = ElectionStatus.Active
        seenHigherId = false
    }

}