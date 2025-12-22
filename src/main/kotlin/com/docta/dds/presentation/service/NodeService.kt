package com.docta.dds.presentation.service

import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

interface NodeService {

    context(ctx: DrpcContext)
    suspend fun getState(): ResultData<NodeStateDto, Error>

    context(ctx: DrpcContext)
    suspend fun isAlive(): SimpleResult<Error>


    context(ctx: DrpcContext)
    suspend fun join(greeterIpAddress: String): SimpleResult<Error>

    context(ctx: DrpcContext)
    suspend fun registerNode(): ResultData<RegistrationStateDto, Error>

    context(ctx: DrpcContext)
    suspend fun leave(): SimpleResult<Error>

    context(ctx: DrpcContext)
    suspend fun replaceSuccessors(successors: List<String>): SimpleResult<Error>

    context(ctx: DrpcContext)
    suspend fun replacePredecessors(predecessors: List<String>): ResultData<NodeStateDto, Error>


    context(ctx: DrpcContext)
    suspend fun proclaimLeader(leaderId: String, leaderAddress: String): SimpleResult<Error>

    context(ctx: DrpcContext)
    suspend fun initiateLonelinessProtocol(): SimpleResult<Error>

}