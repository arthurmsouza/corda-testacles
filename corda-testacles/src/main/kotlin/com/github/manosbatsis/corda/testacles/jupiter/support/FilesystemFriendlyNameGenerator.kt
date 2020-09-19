/*
 * Corda Testacles: Test containers and tools to help cordapps grow.
 * Copyright (C) 2018 Manos Batsis
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
package com.github.manosbatsis.corda.testacles.jupiter.support

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class FilesystemFriendlyNameGenerator {
    companion object{
        val UNKNOWN_NAME = "unknown"
        fun filesystemFriendlyNameOf(context: ExtensionContext): String {
            val contextId = context.uniqueId
            return try {
                if (StringUtils.isBlank(contextId)) UNKNOWN_NAME else URLEncoder.encode(contextId, StandardCharsets.UTF_8.toString())
            } catch (e: UnsupportedEncodingException) {
                UNKNOWN_NAME
            }
        }
    }
}