/*
 * Corda Testacles: Test suite toolkit for Corda developers.
 * Copyright (C) 2020 Manos Batsis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package com.github.manosbatsis.corda.testacles.nodedriver

import com.github.manosbatsis.corda.testacles.nodedriver.TestConfigUtil.myCustomNodeDriverConfig
import mypackage.cordapp.workflow.YoDto
import mypackage.cordapp.workflow.YoFlow1
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.NodeHandle
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.slf4j.LoggerFactory

/** Sample class lifecycle test using the [NodeDriverHelper] directly */
@TestInstance(PER_CLASS)
class NodeDriverHelperClassLfTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NodeDriverHelperClassLfTest::class.java)
    }

    val nodesHelper: NodeDriverHelper by lazy {
        NodeDriverHelper(myCustomNodeDriverConfig())
    }

    /** Start the Corda NodeDriver network */
    @BeforeAll
    fun beforeAll() { nodesHelper.start() }

    /** Stop the Corda network */
    @AfterAll
    fun afterAll() { nodesHelper.stop() }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: NodeHandle = nodesHelper.nodeHandles
                .getNodeByKey("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`() {
        val nodeA = nodesHelper.nodeHandles.getNodeByKey("partya")
        val nodeB = nodesHelper.nodeHandles.getNodeByKey("partyb")
        val yoDto = YoDto(
                recipient = nodeB.nodeInfo.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.rpc.startFlowDynamic(YoFlow1::class.java, yoDto)
                .returnValue.getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
