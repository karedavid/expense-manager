package com.kdapps.kolteskoveto.ui.archive

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kdapps.kolteskoveto.R
import com.kdapps.kolteskoveto.data.ArchiveNode
import com.kdapps.kolteskoveto.data.ArchiveSum
import com.kdapps.kolteskoveto.databinding.ItemArchiveBinding
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.thread

class ArchiveAdapter(val context: Context, private val archiveViewModel: ArchiveViewModel, private val listener: ArchiveNodeClickListener) : RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder>()  {

    inner class ArchiveViewHolder(val binding: ItemArchiveBinding) : RecyclerView.ViewHolder(binding.root)

    interface ArchiveNodeClickListener {
        fun onItemDeleted(item: ArchiveNode)
        fun onItemUnarchived(item: ArchiveNode)
        fun onItemSend(item: ArchiveNode)
    }

    private var nodes   : List<ArchiveNode> = ArrayList<ArchiveNode>()
    private var sums    : List<ArchiveSum> = ArrayList<ArchiveSum>()

    private var mExpandedPosition = -1

    private val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    init {
        // Setting up number formatter
        moneyFormat.currency = Currency.getInstance("HUF")
        moneyFormat.isGroupingUsed = true
        moneyFormat.maximumFractionDigits = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ArchiveViewHolder (
        ItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val node: ArchiveNode = nodes[holder.adapterPosition]
        val isExpanded = holder.adapterPosition == mExpandedPosition

        // Expand / Collapse management
        holder.binding.llDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.binding.icExpand.rotation = if (isExpanded) 180F else 0F
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener{
            mExpandedPosition = if (isExpanded) -1 else holder.adapterPosition

            if(!isExpanded){
                thread {
                    val description = node.id?.let { it1 -> archiveViewModel.getArchivedNodeDescription(it1) }
                    (context as Activity).runOnUiThread { holder.binding.twDesc.text = description }
                }
            }
            notifyItemChanged(holder.adapterPosition)
        }

        holder.binding.btnRemove.setOnClickListener {
            listener.onItemDeleted(node)
            mExpandedPosition = -1
        }

        holder.binding.btnUnarchive.setOnClickListener {
            listener.onItemUnarchived(node)
            mExpandedPosition = -1
        }

        holder.binding.btnSend.setOnClickListener {
            listener.onItemSend(node)
            mExpandedPosition = -1
        }

        // Views
        holder.binding.twName.text = node.name
        holder.binding.twDateFrom.text = node.date_from.toString()
        holder.binding.twDateTo.text = node.date_to.toString()

        holder.binding.twAmount.text = moneyFormat.format(getSumFromId(node.id))

    }

    override fun getItemCount() = nodes.size

    fun setNodes(nodes : List<ArchiveNode>?){
        if(nodes != null){
            this.nodes = nodes
            notifyDataSetChanged()
        }
    }

    fun setArchiveSum(sum: List<ArchiveSum>?) {
        if(sum != null){
            this.sums = sum
            notifyDataSetChanged()
        }
    }

    private fun getSumFromId(archiveId : Long?) : Int{
        if(archiveId != null){
            for(item in sums){
                if(item.archive_id == archiveId){
                    return item.sum
                }
            }
        }
        return 0
    }
}