package com.docta.dds.presentation.controller

import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.asCallParameter
import com.docta.drpc.core.network.client.callPost
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class NodeRestControllerImpl(
    override val hostname: String,
    private val client: HttpClient
) : NodeRestController {

    context(ctx: DrpcContext)
    override suspend fun getState(): ResultData<NodeStateDto, Error> = client.callPost(
        url = absoluteUrl + getStatePath
    )

    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + joinPath,
        greeterIpAddress.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationStateDto, Error> = client.callPost(
        url = absoluteUrl + registerNodePath
    )

    context(ctx: DrpcContext)
    override suspend fun replacePredecessor(
        newIpAddress: String
    ): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + replacePredecessorPath,
        newIpAddress.asCallParameter()
    )

}