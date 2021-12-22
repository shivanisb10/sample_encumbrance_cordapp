package com.demo.flows

import com.demo.states.AssetState
import net.corda.core.contracts.UniqueIdentifier
import org.junit.Test
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.Duration
import java.time.Instant
import java.util.*

class LockFlowTest: FlowTestUtils() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Test(expected = Exception::class)
    fun `Transfer of asset fails before Lock ends`() {
       val linearId = UniqueIdentifier(UUID.randomUUID().toString())
        val assetState = AssetState("Asset1", "House", 400, node1Party, node1Party, linearId)

        //Create and lock the asset state
        val f3 = node1.startFlow(CreateAndLockStateFlow(listOf(assetState), Instant.now() + Duration.ofSeconds(30)))
        mockNetwork.runNetwork()
        val lockstate = f3.get()

        //try to transfer the asset state
        val f4 = node1.startFlow(TransferEncumberedAssetFlow(asset = linearId, newOwner = node2Party, lockId = lockstate.linearId))
        mockNetwork.runNetwork()
        f4.get()
    }

    @Test
    fun `Transfer of the  asset succeeds after end time of lock`(){
        val linearId = UniqueIdentifier(UUID.randomUUID().toString())
        val assetState = AssetState("Asset1", "House", 400, node1Party, node1Party, linearId)

        //lock asset state
        val f3 = node1.startFlow(CreateAndLockStateFlow(listOf(assetState), Instant.now() + Duration.ofSeconds(20)))
        mockNetwork.runNetwork()
        val lockState = f3.get()

        Thread.sleep(40000)
        //try to transfer the asset state
        val f4 = node1.startFlow(TransferEncumberedAssetFlow(asset = linearId, newOwner = node2Party, lockId = lockState.linearId))
        mockNetwork.runNetwork()
        f4.get()
    }

    @Test(expected = Exception::class)
    fun `Transfer of asset fails when encumbered with Time Lock without using the Lock State`() {
        val linearId = UniqueIdentifier(UUID.randomUUID().toString())
        val assetState = AssetState("Asset1", "House", 400, node1Party, node1Party, linearId)

        //Create and lock the asset state
        val f3 = node1.startFlow(CreateAndLockStateFlow(listOf(assetState), Instant.now() + Duration.ofSeconds(30)))
        mockNetwork.runNetwork()
        f3.get()

        //try to transfer the asset state
        val f4 = node1.startFlow(TransferAssetFlow(asset = linearId, newOwner = node2Party))
        mockNetwork.runNetwork()
        f4.get()
    }

}