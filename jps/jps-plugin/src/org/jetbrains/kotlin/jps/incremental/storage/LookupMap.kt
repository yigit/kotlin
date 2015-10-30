/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.jps.incremental.storage

import java.io.File

class LookupMap(storage: File) : BasicMap<LookupHashPair, Set<Int>>(storage, LookupHashPairKeyDescriptor, IntSetExternalizer) {
    override fun dumpKey(key: LookupHashPair): String = key.toString()

    override fun dumpValue(value: Set<Int>): String = value.toString()

    public fun add(name: String, scope: String, fileId: Int) {
        storage.append(LookupHashPair(name, scope)) { out -> out.writeInt(fileId) }
    }

    public operator fun get(lookupHash: LookupHashPair): Set<Int>? = storage[lookupHash]

    public operator fun set(key: LookupHashPair, fileIds: Set<Int>) {
        storage.set(key, fileIds)
    }

    public val keys: Collection<LookupHashPair>
        get() = storage.keys
}
