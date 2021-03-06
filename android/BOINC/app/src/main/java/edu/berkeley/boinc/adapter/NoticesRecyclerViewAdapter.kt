/*
 * This file is part of BOINC.
 * http://boinc.berkeley.edu
 * Copyright (C) 2020 University of California
 *
 * BOINC is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * BOINC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BOINC.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.berkeley.boinc.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import edu.berkeley.boinc.BOINCActivity
import edu.berkeley.boinc.NoticesFragment
import edu.berkeley.boinc.R
import edu.berkeley.boinc.databinding.NoticesLayoutListItemBinding
import edu.berkeley.boinc.rpc.Notice
import edu.berkeley.boinc.utils.Logging
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NoticesRecyclerViewAdapter(
        private val fragment: NoticesFragment,
        private val notices: List<Notice>
) : RecyclerView.Adapter<NoticesRecyclerViewAdapter.ViewHolder>() {
    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG,
            FormatStyle.SHORT)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NoticesLayoutListItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount() = notices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = notices[position]

        holder.root.setOnClickListener {
            val link = listItem.link

            if (Logging.DEBUG) {
                Log.d(Logging.TAG, "noticeClick: $link")
            }
            if (link.isNotEmpty()) {
                fragment.requireActivity().startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
            }
        }

        val icon = getIcon(position)
        // if available set icon, if not boinc logo
        // if available set icon, if not boinc logo
        if (icon == null) {
            holder.projectIcon.setImageDrawable(ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.ic_boinc))
        } else {
            holder.projectIcon.setImageBitmap(icon)
        }

        holder.projectName.text = listItem.projectName
        holder.title.text = listItem.title
        holder.content.text = HtmlCompat.fromHtml(listItem.description, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(
                listItem.createTime.toLong()), ZoneId.systemDefault())
        holder.time.text = dateTimeFormatter.format(localDateTime)
    }

    private fun getIcon(position: Int): Bitmap? {
        return try {
            BOINCActivity.monitor!!.getProjectIconByName(notices[position].projectName)
        } catch (e: Exception) {
            if (Logging.WARNING) {
                Log.w(Logging.TAG, "TasksListAdapter: Could not load data, clientStatus not initialized.")
            }
            null
        }
    }

    inner class ViewHolder(binding: NoticesLayoutListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val projectIcon = binding.projectIcon
        val projectName = binding.projectName
        val title = binding.noticeTitle
        val content = binding.noticeContent
        val time = binding.noticeTime
    }
}
