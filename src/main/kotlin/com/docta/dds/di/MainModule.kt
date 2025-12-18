package com.docta.dds.di

import com.docta.dds.domain.model.NodeState
import org.koin.dsl.module

val mainModule = module {

    single {
        NodeState
    }

}