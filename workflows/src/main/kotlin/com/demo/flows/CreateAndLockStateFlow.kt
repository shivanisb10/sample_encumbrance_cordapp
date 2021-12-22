package com.demo.flows

import co.paralleluniverse.fibers.Suspendable
import com.demo.contracts.AssetContract
import com.demo.contracts.LockStateContract
import com.demo.states.AssetState
import com.demo.states.LockState
import com.demo.states.LockStatus
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import org.intellij.lang.annotations.Flow
import java.security.PublicKey
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

@CordaSerializable
@InitiatingFlow
@StartableByRPC
class CreateAndLockStateFlow(private val assets:List<AssetState>,
                             private val timeToLock:Instant
) : FlowLogic<LockState>() {

    @Suspendable
    override fun call(): LockState {
        logger.info("START Transfer Encumbered Asset Flow")

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)

        var owners :MutableList<FlowSession> = ArrayList()
        assets.forEach {
            if(!(it.owner.name.equals(ourIdentity.name)))
            owners.add(initiateFlow(it.owner))
        }

        var encumbrance = 1
        assets.forEach {
            builder.addOutputState(state = it,
                    contract = AssetContract.contractId,
                    notary = notary,
                    encumbrance = encumbrance )
            encumbrance+=1
        }

        builder.addOutputState(state = LockState(timeToLock, LockStatus.ACTIVE, ourIdentity,
                UniqueIdentifier(UUID.randomUUID().toString())),
                contract = LockStateContract.contractId,
                notary = notary,
                encumbrance = 0)

        builder.addCommand(AssetContract.Commands.Create(), ourIdentity.owningKey)
        builder.addCommand(LockStateContract.Commands.Create(), ourIdentity.owningKey)
        builder.setTimeWindow(Instant.now(), Duration.ofSeconds(10));

        val signedTransaction = serviceHub.signInitialTransaction(builder)
        subFlow(FinalityFlow(signedTransaction, owners))

        return signedTransaction.tx.outputStates[1] as LockState

    }

    @InitiatedBy(CreateAndLockStateFlow::class)
    class LockStateFlowHandler(val otherSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            logger.info("START Transfer Encumbered Asset Flow Responder")
            subFlow(ReceiveFinalityFlow(otherSession))
        }
    }
}
