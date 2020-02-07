package com.github.kr328.clash.service

import android.content.Context
import androidx.core.content.edit
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.General
import com.github.kr328.clash.core.model.ProxyGroup
import com.github.kr328.clash.service.data.ClashDatabase
import com.github.kr328.clash.service.data.ClashProfileEntity
import com.github.kr328.clash.service.ipc.IStreamCallback
import com.github.kr328.clash.service.ipc.ParcelableContainer

class ClashManager(private val context: Context) : IClashManager.Stub() {
    private val settings = context.getSharedPreferences("service", Context.MODE_PRIVATE)

    override fun queryAllProxies(): Array<ProxyGroup> {
        return Clash.queryProxyGroups().toTypedArray()
    }

    override fun queryGeneral(): General {
        return Clash.queryGeneral()
    }

    override fun setSelectProxy(proxy: String?, selected: String?): Boolean {
        require(proxy != null && selected != null)

        return Clash.setSelectedProxy(proxy, selected)
    }

    override fun putSetting(key: String?, value: String?): Boolean {
        settings.edit {
            putString(key, value)
        }
        return true
    }

    override fun queryAllProfiles(): Array<ClashProfileEntity> {
        return ClashDatabase.getInstance(context).openClashProfileDao().queryProfiles()
    }

    override fun queryBandwidth(): Long {
        val data = Clash.queryBandwidth()

        return data.download + data.upload
    }

    override fun openLogEvent(callback: IStreamCallback?) {
        require(callback != null)

        Clash.openLogEvent().apply {
            onEvent {
                try {
                    callback.send(ParcelableContainer(it))
                } catch (e: Exception) {
                    close()
                }
            }
        }
    }

    override fun startHealthCheck(group: String?, callback: IStreamCallback?) {
        require(group != null && callback != null)

        Clash.startHealthCheck(group).invokeOnCompletion { u ->
            if (u != null)
                callback.completeExceptionally(u.message)
            else
                callback.complete()
        }
    }

    override fun getSetting(key: String?): String {
        return settings.getString(key, "")!!
    }
}