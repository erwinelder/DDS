package com.docta.dds.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationStateDto(
    val leaderId: String,
    val leaderAddress: String,
    val successors: List<String>,
    val predecessors: List<String>
)
