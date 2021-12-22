package com.demo.flows

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import org.junit.AfterClass
import org.junit.BeforeClass


abstract class FlowTestUtils {


    companion object {

        lateinit var mockNetwork: MockNetwork

        //network nodes
        lateinit var node1: StartedMockNode
        lateinit var node2: StartedMockNode
        lateinit var notary: StartedMockNode

        //party
        lateinit var node1Party: Party
        lateinit var node2Party: Party
        lateinit var notaryParty: Party

        //node x500 names
        val node1Name = CordaX500Name("Party A", "Singapore", "SG")
        val node2Name = CordaX500Name("Party B", "Singapore", "SG")
        val notaryName = CordaX500Name("Notary", "Singapore", "GB")

        val cordapp = "com.demo"

        @JvmStatic
        @BeforeClass
        fun setup() {
            mockNetwork = MockNetwork(
                    cordappPackages = listOf(cordapp),
                    defaultParameters = MockNetworkParameters(),
                    notarySpecs = listOf(MockNetworkNotarySpec(notaryName)),
                    networkParameters = testNetworkParameters(
                            minimumPlatformVersion = 4
                    ))
            node1 = mockNetwork.createNode(node1Name)
            node2 = mockNetwork.createNode(node2Name)
            notary = mockNetwork.notaryNodes.first()


            node1Party = node1.info.singleIdentity()
            node2Party = node2.info.singleIdentity()
            notaryParty = notary.info.singleIdentity()

            mockNetwork.startNodes()

        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            mockNetwork.stopNodes()
        }
    }

}