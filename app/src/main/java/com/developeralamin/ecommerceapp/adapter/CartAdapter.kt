package com.developeralamin.ecommerceapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.developeralamin.ecommerceapp.Helper.ChangeNumberItemsListener
import com.developeralamin.ecommerceapp.Helper.ManagmentCart
import com.developeralamin.ecommerceapp.databinding.ViewholderCartBinding
import com.developeralamin.ecommerceapp.model.ItemsModel
import com.developeralamin.ecommerceapp.utils.Constant

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    val changeNumberItemsListener: ChangeNumberItemsListener,
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {
    class ViewHolder(val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)

    private val managmentCart = ManagmentCart(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ViewholderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItemSelected[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachTime.text = "${Constant.TAKA_SYMBOL}${item.price}"
        holder.binding.totalEachItem.text =
            "${Constant.TAKA_SYMBOL}${Math.round(item.numberInCart * item.price)}"
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .into(holder.binding.pic)

        holder.binding.plusCartBtn.setOnClickListener {
            managmentCart.plusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener.onChanged()
                }

            })
        }

        holder.binding.minusCartBtn.setOnClickListener {
            managmentCart.minusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                override fun onChanged() {
                    notifyDataSetChanged()
                    changeNumberItemsListener.onChanged()
                }

            })
        }


    }

    override fun getItemCount(): Int = listItemSelected.size
}