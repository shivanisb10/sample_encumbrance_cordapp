package com.demo.contracts

import com.demo.states.AssetState
import com.demo.states.LockState
import com.demo.states.LockStatus
import net.corda.core.contracts.*
import net.corda.core.transactions.*

class LockStateContract: Contract  {

    companion object {
        val contractId = this::class.java.enclosingClass.canonicalName
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Create -> verifyCreateCommand(tx)
            is Commands.Deactivate -> verifyDeactivateCommand(tx)
            else -> throw IllegalArgumentException("Command not supported")
        }
    }

    private fun verifyCreateCommand(tx: LedgerTransaction) {
        val lockInputs = tx.inputsOfType<LockState>()
        val lockOutputs = tx.outputsOfType<LockState>()

        val otherOutputs = tx.outputs.filter { it.data !is LockState }

        requireThat {
            "Transaction should have time Window" using (tx.timeWindow?.untilTime !=null)
            "Locking time should be greater than time window" using (tx.timeWindow?.untilTime!! < lockOutputs.first().endTime )

            "No inputs of type Lock are allowed" using (lockInputs.isEmpty())
            "Exactly one output of type Lock is expected" using (lockOutputs.size == 1)

            //states other than Lock state should be encumbered
            otherOutputs.forEach {
                "Output of type AssetState must be encumbered" using (it.encumbrance  != null)
            }

        }
    }

    private fun verifyDeactivateCommand(tx: LedgerTransaction) {
        val assetInputs = tx.inputsOfType<AssetState>()
        val assetOutputs = tx.outputsOfType<AssetState>()

        val otherOutputs = tx.outputs.filter { it.data !is LockState }

        val lockOutputs = tx.outputsOfType<LockState>()
        val lockInputs = tx.inputsOfType<LockState>()

        requireThat {
            "Transaction should have time Window" using (tx.timeWindow?.fromTime !=null)
            "from Time of transaction should be greater than the end time for lock" using (tx.timeWindow?.fromTime!! > lockOutputs.first().endTime )

            //states other than lock should be unencumbered
            otherOutputs.forEach {
                "Output of type AssetState must be unencumbered" using (it.encumbrance  == null)
            }

            //should have exactly one lock input and output
            "Exactly one input of type Lock is expected" using (lockInputs.size == 1)
            "Exactly one output of type Lock is expected" using (lockOutputs.size == 1)

            //status change from ACTIVE to INACTIVE
            "Input  lock status is ACTIVE" using(
                    lockInputs.first().status == LockStatus.ACTIVE)
            "Output lock status is INACTIVE" using(
                    lockOutputs.first().status == LockStatus.INACTIVE)

            "Atleast one input of type AssetState" using (assetInputs.isNotEmpty())
            "Input asset states and output asset states should be same in number" using (
                    assetInputs.size == assetOutputs.size)
        }

    }

    interface Commands : CommandData {
        class Create : Commands
        class Deactivate : Commands
    }
}
