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
class TransferEncumberedAssetFlow(private val asset: UniqueIdentifier,
                                  private val lockId: UniqueIdentifier,
                                  private val newOwner:Party
) : FlowLogic<Unit>() {

    @Suspendable
    override fun call(): Unit{
        logger.info("START Create Asset Flow")
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary)

        val result = serviceHub.vaultService.queryBy<AssetState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(asset)))
        val lockState= serviceHub.vaultService.queryBy<LockState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(lockId)))

        val outputState = result.states.first().state.data.copy()
        outputState.owner = newOwner
        builder.addInputState(lockState.states.first())
        builder.addInputState(result.states.first())
        builder.addOutputState(state = outputState,
                contract = AssetContract.contractId,
                notary = notary,
                encumbrance = null)
        var lockOutput = lockState.states.first().state.data.copy()
        lockOutput.status = LockStatus.INACTIVE
        builder.addOutputState(lockOutput)

        builder.addCommand(AssetContract.Commands.Transfer(), ourIdentity.owningKey)
        builder.addCommand(LockStateContract.Commands.Deactivate(), ourIdentity.owningKey)
        builder.setTimeWindow(Instant.now(), Duration.ofSeconds(10));

        val signedTransaction = serviceHub.signInitialTransaction(builder)
            val session = initiateFlow(newOwner)
            subFlow(FinalityFlow(signedTransaction, listOf(session)))
    }

    @InitiatedBy(TransferEncumberedAssetFlow::class)
    class TransferEncumberedAssetFlowHandler(val otherSession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            logger.info("START Create Asset Flow Responder")
            subFlow(ReceiveFinalityFlow(otherSession))
        }
    }
}
