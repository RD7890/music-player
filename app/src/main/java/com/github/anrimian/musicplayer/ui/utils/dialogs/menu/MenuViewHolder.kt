package com.github.anrimian.musicplayer.ui.utils.dialogs.menu

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.context

class MenuViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    @LayoutRes menuViewRes: Int,
    onItemClickListener: (MenuItem) -> Unit
) : RecyclerView.ViewHolder(inflater.inflate(menuViewRes, parent, false)) {

    private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)

    private lateinit var menuItem: MenuItem

    init {
        itemView.setOnClickListener { onItemClickListener(menuItem) }
    }

    fun bind(menuItem: MenuItem) {
        this.menuItem = menuItem

        itemView.isEnabled = menuItem.isEnabled

        tvTitle.text = menuItem.title
        tvTitle.isEnabled = menuItem.isEnabled
        if (menuItem.isChecked) {
            tvTitle.setTextColor(context.attrColor(R.attr.colorAccent))
        } else {
            tvTitle.setTextColor(
                ContextCompat.getColorStateList(context, R.color.color_text_primary)
            )
            CompatUtils.setColorTextPrimaryColor(tvTitle)
        }

        val icon = menuItem.icon
        ivIcon.visibility = if (icon == null) View.GONE else View.VISIBLE
        ivIcon.setColorFilter(tvTitle.currentTextColor)
        ivIcon.setImageDrawable(icon)
        ivIcon.contentDescription = menuItem.title
    }
}
