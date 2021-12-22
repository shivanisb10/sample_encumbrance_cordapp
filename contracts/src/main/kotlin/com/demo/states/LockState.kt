package com.demo.states


import com.demo.contracts.LockStateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import java.time.Instant

@BelongsToContract(LockStateContract::class)
@CordaSerializable
data class LockState(
        val endTime:Instant,
        var status: LockStatus,
        val creator : Party,
        override val linearId: UniqueIdentifier): ContractState, LinearState {


    override val participants: List<AbstractParty>
        get() = listOf(creator)

}
@CordaSerializable
enum class LockStatus {
    ACTIVE,
    INACTIVE
}
