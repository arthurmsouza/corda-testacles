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
package com.github.manosbatsis.corda.testacles.jupiter

import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import com.github.manosbatsis.corda.testacles.model.api.jupiter.JupiterExtensionSupport
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.io.File


class CordformNetworkExtension: JupiterExtensionSupport,
        BeforeAllCallback, AfterAllCallback,
        ParameterResolver  {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformNetworkExtension::class.java)
        private val NAMESPACE = Namespace.create(CordformNetworkExtension::class.java)

        /**
         * Get the default nodes dir (probably created by cordform) for the test in context.
         * This implementation will search in _build/nodes_  and  _../build/nodes_.
         */
        private fun getDefaultNodesDir(): File {
            val projectDir = File(System.getProperty("user.dir"))
            fun resolveIfExists(file: File, path: String) = try {
                val nodesDir = file.resolve(path)
                if (nodesDir.exists() && nodesDir.isDirectory) nodesDir else null
            } catch (e: Exception) {
                logger.warn("Failed resolving nodeDir candidate ", e)
                null
            }

            return resolveIfExists(projectDir, "build/nodes")
                    ?: resolveIfExists(projectDir.parentFile, "build/nodes")
                    ?: error("A nodes directory could not be found in default locations")
        }
    }

    lateinit var cordformNetworkContainer: CordformNetworkContainer

    /** Create and start the Corda network */
    override fun beforeAll(context: ExtensionContext) {
        val testClass = getRequiredTestClass(context)

        // Create and start network container
        cordformNetworkContainer = CordformNetworkContainer(
                nodesDir = findSharedNodesDir(testClass)
                        ?: getDefaultNodesDir(),
                cloneNodesDir = true,
                network = findSharedNetwork(testClass)
                        ?: Network.newNetwork(),
                imageName = findNodesImageName(testClass)
                        ?: CordformNetworkContainer.DEFAULT_CORDA_IMAGE_NAME_4_5)
        cordformNetworkContainer.start()
    }


    /** Stop the Corda network */
    override fun afterAll(context: ExtensionContext) {
        cordformNetworkContainer.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext?,
                                   extensionContext: ExtensionContext?) =
            parameterContext?.parameter?.type == CordformNetworkContainer::class.java

    override fun resolveParameter(
            parameterContext: ParameterContext?,
            extensionContext: ExtensionContext?
    ) = cordformNetworkContainer


    private fun findNodesImageName(testClass: Class<*>): DockerImageName? =
            findNAnnotatedFieldValue(testClass, NodesImageName::class.java,
                    DockerImageName::class.java)

    private fun findSharedNetwork(testClass: Class<*>): Network? =
            findNAnnotatedFieldValue(testClass, NodesNetwork::class.java,
                    Network::class.java)

    private fun findSharedNodesDir(testClass: Class<*>): File? =
            findNAnnotatedFieldValue(testClass, NodesDir::class.java,
                    File::class.java)
}
