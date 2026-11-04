package com.github.anrimian.musicplayer.ui.utils.dialogs.menu

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils

class MenuAdapter(
    private val items: List<MenuItem>,
    @LayoutRes private val menuViewRes: Int,
    private val onItemClickListener: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuViewHolder>() {

    constructor(
        menu: Menu,
        @LayoutRes menuViewRes: Int,
        onItemClickListener: (MenuItem) -> Unit
    ) : this(AndroidUtils.getMenuItems(menu), menuViewRes, onItemClickListener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(parent.context),
            parent,
            menuViewRes,
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

}
