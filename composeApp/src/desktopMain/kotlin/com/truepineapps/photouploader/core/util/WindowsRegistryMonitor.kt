/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.util

import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.WinError
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinReg

/**
 * A generic helper to monitor Windows registry keys for changes.
 */
fun startRegistryMonitor(path: String, monitorName: String, onUpdate: () -> Unit): Thread = Thread {
    val hKeyRef = WinReg.HKEYByReference()
    val result = Advapi32.INSTANCE.RegOpenKeyEx(
        WinReg.HKEY_CURRENT_USER,
        path,
        0,
        WinNT.KEY_READ or WinNT.KEY_NOTIFY,
        hKeyRef
    )

    if (result == WinError.ERROR_SUCCESS) {
        val hKey = hKeyRef.value
        try {
            while (!Thread.currentThread().isInterrupted) {
                val notifyResult = Advapi32.INSTANCE.RegNotifyChangeKeyValue(
                    hKey,
                    false,
                    WinNT.REG_NOTIFY_CHANGE_LAST_SET,
                    null,
                    false
                )

                if (notifyResult == WinError.ERROR_SUCCESS) {
                    onUpdate()
                } else {
                    break
                }
            }
        } finally {
            Advapi32.INSTANCE.RegCloseKey(hKey)
        }
    }
}.apply {
    isDaemon = true
    name = monitorName
    start()
}

