# Sample CorDapp
This CordApp demonstrate the use encumbrance by Locking an Asset for a given time using encumbrance.

_**Encumbrance:**_
`The encumbrance state, if present, forces additional controls over the encumbered state, since the platform checks
that the encumbrance state is present as an input in the same transaction that consumes the encumbered state, and
the contract code and rules of the encumbrance state will also be verified during the execution of the transaction.`

## States
This Cordapp has two states one **AssetState** that is encumbered and **LockState** that is used as encumbrance state.
The AssetState represents an Asset of a type that can be owned by a Party, that is Locked for a time after it is 
created by using the LockState.
The LockState is used to lock an AssetState for some time using the field endTime.Once the AssetState is locked it cannot 
be consumed until the startTime of transaction is greater than the endTime in LockState with which it is encumbered.

The AssetState is governed by **AssetContract** and Lock State is governed by **LockStateContract**

## Flows
**CreateAndLockStateFlow:**
The CreateAndLockStateFlow creates two states AssetState and LockState and encumbers AssetState with the LockState
     
      `var encumbrance = 1
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
                encumbrance = 0)`
**TransferAssetFlow:**
The TransferAssetFlow changes the owner of the Asset to a new owner. The flow will work only if the asset is not encumbered.

**TransferEncumberedAssetFlow:**
The TransferEncumberedAssetFlow changes the owner of an encumbered asset and Unlocks the encumbered state. The flow will
only work if the endTime in lock state has passed.

        builder.addInputState(lockState.states.first())
        builder.addInputState(result.states.first())
        builder.addOutputState(state = outputState,
                contract = AssetContract.contractId,
                notary = notary,
                encumbrance = null)
        builder.addOutputState(lockOutput)
        
## Tests
The file LockFlowTest contains tests which try certain scenarios with encumbered asset state.
* Transfer encumbered state when using encumbrance state in same txn but lock time has not passed
* Transfer encumbered state without using encumbrance state
* Transfer encumbered state when using encumbrance state in same txn after lock time has passed