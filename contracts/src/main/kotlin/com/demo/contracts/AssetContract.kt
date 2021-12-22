package com.demo.contracts

import com.demo.states.AssetState
import com.demo.states.LockState
import net.corda.core.contracts.*
import net.corda.core.contracts.Requirements.using
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction

class AssetContract: Contract {

    companion object {
        val contractId = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Create -> verifyCreateCommand(tx)
            is Commands.Transfer -> verifyTransferCommand(tx)
            else -> throw IllegalArgumentException("Command not supported")

        }
    }
    private fun verifyTransferCommand(tx: LedgerTransaction) {
        val inputs = tx.inputsOfType<AssetState>()
        val outputs = tx.outputsOfType<AssetState>()
        val command = tx.commands.requireSingleCommand<Commands>()

        "Only one input state of asset type" using (inputs.size ==1)
        "Only one output state of asset type" using (outputs.size ==1)

        "New owner should not be same as old owner" using (outputs.first().owner != inputs.first().owner)
        "Old owner should be the required signer" using (command.signers.contains(inputs.first().owner.owningKey))

        "New owner should not be same as old owner" using (outputs.first().issuer == inputs.first().issuer
                && outputs.first().nameOfAsset== inputs.first().nameOfAsset
                && outputs.first().typeOfAsset == inputs.first().typeOfAsset
                && outputs.first().valueOfAsset == inputs.first().valueOfAsset)

    }

    private fun verifyCreateCommand(tx: LedgerTransaction) {
        val inputs = tx.inputsOfType<AssetState>()
        val outputs = tx.outputsOfType<AssetState>()

        val command = tx.commands.requireSingleCommand<Commands>()

        "Issuer is the required signer" using (command.signers.contains(outputs.first().issuer.owningKey))

        "Only one input state of asset type" using (inputs.isEmpty())
        "Only one output state of asset type" using (outputs.size ==1)
    }

    interface Commands : CommandData {
        class Create: Commands
        class Transfer: Commands
    }
}