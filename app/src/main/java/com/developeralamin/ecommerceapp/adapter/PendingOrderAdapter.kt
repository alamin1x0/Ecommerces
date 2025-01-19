package com.developeralamin.ecommerceapp.adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.OrderItemListBinding
import com.developeralamin.ecommerceapp.model.PendingOrder
import com.developeralamin.ecommerceapp.ui.OrderTrackingActivity
import com.developeralamin.ecommerceapp.utils.Constant


class PendingOrderAdapter(val list: List<PendingOrder>, val context: Context) :
    RecyclerView.Adapter<PendingOrderAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: OrderItemListBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            OrderItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = list[position]
        holder.binding.productTitle.text = list[position].dateTime.toString()
        holder.binding.productPrice.text = "${Constant.TAKA_SYMBOL}${item.amount}"
        holder.binding.orderNo.text = "#${item.orderNo}"


        when (list[position].status) {
            "Pending" -> {
                holder.binding.productStatus.text = "Pending"
                holder.binding.productStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.royal_blue_dark
                    )
                )
            }

            "Delivered" -> {
                holder.binding.productStatus.text = "Delivered"
                holder.binding.productStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.green
                    )
                )

            }

            "Canceled" -> {
                holder.binding.productStatus.text = "Canceled"
                holder.binding.productStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
            }
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context, OrderTrackingActivity::class.java).apply {
                putExtra("object", item)
            }
            ContextCompat.startActivity(holder.itemView.context, intent, null)

        }
    }


    override fun getItemCount(): Int {
        return list.size
    }
}