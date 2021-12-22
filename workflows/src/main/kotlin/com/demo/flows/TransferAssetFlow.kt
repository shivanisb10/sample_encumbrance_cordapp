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
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.TransactionBuilder
import java.time.Duration
import java.time.Instant

@CordaSerializable
@InitiatingFlow
@StartableByRPC
class TransferAssetFlow(private val asset: UniqueIdentifier,
                                  private val newOwner:Party
) : FlowLogic<Unit>() {

    @Suspendable
    override fun call(): Unit{
        logger.info("START Transfer Asset Flow")
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)

        val result = serviceHub.vaultService.queryBy<AssetState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(asset)))

        val outputState = result.states.first().state.data.copy()
        outputState.owner = newOwner

        builder.addInputState(result.states.first())
        builder.addOutputState(outputState)

        builder.addCommand(AssetContract.Commands.Transfer(), ourIdentity.owningKey)
        builder.setTimeWindow(Instant.now(), Duration.ofSeconds(10));

        val signedTransaction = serviceHub.signInitialTransaction(builder)
            val session = initiateFlow(newOwner)
            subFlow(FinalityFlow(signedTransaction, listOf(session)))

    }

    @InitiatedBy(TransferAssetFlow::class)
    class TransferAssetFlowHandler(val otherSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            logger.info("START Transfer Asset Flow Responder")
            subFlow(ReceiveFinalityFlow(otherSession))
        }
    }
}
