package com.kdapps.kolteskoveto.ui.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.data.SpendNode
import com.kdapps.kolteskoveto.databinding.ItemListBinding
import java.text.NumberFormat
import java.util.*


class ListAdapter(val context: Context, private val listener: SpendNodeClickListener) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    interface SpendNodeClickListener{
        fun onItemDeleted(item: SpendNode)
        fun onItemUpdated(item: SpendNode)
        fun onItemEdited(item: SpendNode)
    }

    inner class ListViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

    private var nodes : List<SpendNode> = ArrayList<SpendNode>()

    private var mExpandedPosition = -1

    private val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    init {
        // Setting up number formatter
        moneyFormat.currency = Currency.getInstance("HUF")
        moneyFormat.isGroupingUsed = true
        moneyFormat.maximumFractionDigits = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListViewHolder(
        ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val node: SpendNode = nodes[holder.adapterPosition]

        // Expand / Collapse management
        val isExpanded = holder.adapterPosition == mExpandedPosition
        holder.binding.llDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.icExpand.rotation = if (isExpanded) 180F else 0F
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener{
                mExpandedPosition = if (isExpanded) -1 else holder.adapterPosition
                notifyItemChanged(holder.adapterPosition)
        }

        if(node.place != null){
            if(node.place?.isEmpty() == true || node.place?.isBlank() == true){
                holder.binding.btnMaps.visibility = View.GONE
                holder.binding.twPlace.visibility = View.GONE
            }else{
                holder.binding.twPlace.visibility = View.VISIBLE
                holder.binding.btnMaps.visibility = View.VISIBLE
                holder.binding.btnMaps.setOnClickListener {
                    val url = "http://maps.google.co.in/maps?q=${node.place}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        }

        if(node.desc == null || node.desc?.isEmpty() == true){
            holder.binding.twDesc.visibility = View.GONE
        }else{
            holder.binding.twDesc.visibility = View.VISIBLE
        }

        if(node.paid){
            holder.binding.btnPaid.visibility = View.GONE
            holder.binding.twPlanned.visibility = View.INVISIBLE
        }else{
            holder.binding.twPlanned.visibility = View.VISIBLE
            holder.binding.btnPaid.setOnClickListener {
                node.paid = true
                listener.onItemUpdated(node)
            }
            holder.binding.btnPaid.visibility = View.VISIBLE
        }

        holder.binding.btnRemove.setOnClickListener {
                listener.onItemDeleted(node)
                mExpandedPosition = -1
        }

        holder.binding.btnEdit.setOnClickListener {
            listener.onItemEdited(node)
            mExpandedPosition = -1
        }

        holder.binding.imageView.setImageResource(getImageResource(node.main_category))
        holder.binding.twName.text = node.name
        holder.binding.twAmount.text = moneyFormat.format(node.amount)
        holder.binding.twDate.text = context.resources.getString(R.string.date_format, node.year, node.month+1, node.day)
        holder.binding.twSecondaryCategory.text = node.secondary_category
        holder.binding.twDesc.text = node.desc
        holder.binding.twPlace.text = node.place

    }

    override fun getItemCount() = nodes.size

    fun setNodes(nodes : List<SpendNode>?){
        if(nodes != null){
            this.nodes = nodes
            notifyDataSetChanged()
        }
    }

    @DrawableRes
    private fun getImageResource(category: Int): Int {
        return when (category) {
            SpendNode.CATEGORY_FOOD             -> R.drawable.ic_food_24
            SpendNode.CATEGORY_HOUSEHOLD        -> R.drawable.ic_shopping_24
            SpendNode.CATEGORY_LIVING           -> R.drawable.ic_home_black_24dp
            SpendNode.CATEGORY_OCCASIONAL       -> R.drawable.ic_occasional_24
            SpendNode.CATEGORY_TRANSPORTATION   -> R.drawable.ic_directions_transit_24
            else                                -> R.drawable.ic_other_24
        }
    }


}