package com.demo.states

import com.demo.contracts.AssetContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(AssetContract::class)
@CordaSerializable
data class AssetState(
        val nameOfAsset: String,
        val typeOfAsset: String,
        val valueOfAsset: Int,
        var owner:Party,
        val issuer:Party,
        override val linearId: UniqueIdentifier
): ContractState, LinearState {
    override val participants: List<AbstractParty>
        get() = listOf(owner)
}