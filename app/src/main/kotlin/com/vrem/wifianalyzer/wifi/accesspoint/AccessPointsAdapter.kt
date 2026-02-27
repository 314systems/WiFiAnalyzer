/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.wifi.accesspoint

import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.vrem.wifianalyzer.MainContext
import com.vrem.wifianalyzer.ui.theme.AppTheme
import com.vrem.wifianalyzer.wifi.model.WiFiData
import com.vrem.wifianalyzer.wifi.model.WiFiDetail
import com.vrem.wifianalyzer.wifi.model.WiFiSignal
import com.vrem.wifianalyzer.wifi.scanner.UpdateNotifier

class AccessPointsAdapter(
    private val accessPointsAdapterData: AccessPointsAdapterData = AccessPointsAdapterData(),
    private val accessPointPopup: AccessPointPopup = AccessPointPopup(),
) : BaseExpandableListAdapter(),
    UpdateNotifier {
    lateinit var expandableListView: ExpandableListView

    override fun getGroupView(
        groupPosition: Int,
        expanded: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        val wiFiDetail = getGroup(groupPosition)
        val isGroup = getChildrenCount(groupPosition) > 0
        val view = (convertView as? ComposeView) ?: ComposeView(parent!!.context)
        view.setContent {
            AppTheme {
                AccessPointView(
                    wiFiDetail = wiFiDetail,
                    isGroup = isGroup,
                    isExpanded = expanded,
                )
            }
        }
        view.setOnClickListener { accessPointPopup.show(view, wiFiDetail) }
        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        lastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        val wiFiDetail = getChild(groupPosition, childPosition)
        val view = (convertView as? ComposeView) ?: ComposeView(parent!!.context)
        view.setContent {
            AppTheme {
                AccessPointView(
                    wiFiDetail = wiFiDetail,
                    isChild = true,
                )
            }
        }
        view.setOnClickListener { accessPointPopup.show(view, wiFiDetail) }
        return view
    }

    @Composable
    private fun AccessPointView(
        wiFiDetail: WiFiDetail,
        isChild: Boolean = false,
        isGroup: Boolean = false,
        isExpanded: Boolean = false,
    ) {
        val viewType = MainContext.INSTANCE.settings.accessPointView()
        if (viewType == AccessPointViewType.COMPLETE) {
            AccessPointViewComplete(
                wiFiDetail = wiFiDetail,
                isChild = isChild,
                isGroup = isGroup,
                isExpanded = isExpanded,
            )
        } else {
            val signal = wiFiDetail.wiFiSignal
            val data =
                AccessPointViewData(
                    ssid = wiFiDetail.wiFiIdentifier.title,
                    level = "${signal.level}dBm",
                    channel = signal.channelDisplay(),
                    primaryFrequency = "${signal.primaryFrequency}${WiFiSignal.FREQUENCY_UNITS}",
                    distanceText = signal.distance,
                    isGrouped = isChild,
                    security = wiFiDetail.wiFiSecurity.security.name,
                    showGroupIndicator = isGroup,
                )
            AccessPointViewCompact(data = data)
        }
    }

    override fun update(wiFiData: WiFiData) {
        accessPointsAdapterData.update(wiFiData, expandableListView)
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int = accessPointsAdapterData.parentsCount()

    override fun getChildrenCount(groupPosition: Int): Int = accessPointsAdapterData.childrenCount(groupPosition)

    override fun getGroup(groupPosition: Int): WiFiDetail = accessPointsAdapterData.parent(groupPosition)

    override fun getChild(
        groupPosition: Int,
        childPosition: Int,
    ): WiFiDetail = accessPointsAdapterData.child(groupPosition, childPosition)

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(
        groupPosition: Int,
        childPosition: Int,
    ): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun isChildSelectable(
        groupPosition: Int,
        childPosition: Int,
    ): Boolean = true

    override fun onGroupCollapsed(groupPosition: Int) = accessPointsAdapterData.onGroupCollapsed(groupPosition)

    override fun onGroupExpanded(groupPosition: Int) = accessPointsAdapterData.onGroupExpanded(groupPosition)
}
