package com.docta.dds.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationStateDto(
    val successorIpAddress: String? = null,
    val predecessorIpAddress: String? = null,
    val predecessorOfPredecessorIpAddress: String? = null
) {

    fun replaceNeighborIpAddressesIfNull(newAddress: String): RegistrationStateDto {
        val successorIpAddress = successorIpAddress ?: newAddress
        val predecessorIpAddress = predecessorIpAddress ?: newAddress

        return RegistrationStateDto(
            successorIpAddress = successorIpAddress,
            predecessorIpAddress = predecessorIpAddress,
            predecessorOfPredecessorIpAddress = predecessorOfPredecessorIpAddress
        )
    }

}
