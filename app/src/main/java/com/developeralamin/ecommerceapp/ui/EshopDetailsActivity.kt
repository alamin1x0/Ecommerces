package com.developeralamin.ecommerceapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.developeralamin.ecommerceapp.Helper.ManagmentCart
import com.developeralamin.ecommerceapp.adapter.PicAdapter
import com.developeralamin.ecommerceapp.adapter.SelectModelAdapter
import com.developeralamin.ecommerceapp.databinding.ActivityEshopDetailsBinding
import com.developeralamin.ecommerceapp.model.ItemsModel
import com.developeralamin.ecommerceapp.utils.Constant

class EshopDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEshopDetailsBinding
    private lateinit var item: ItemsModel
    private var numberOrder = 1
    private lateinit var managmentCart: ManagmentCart


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEshopDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        managmentCart = ManagmentCart(this)

        getBundle()
        initList()

    }

    private fun initList() {
        val modelList = ArrayList<String>()
        for (model in item.model) {
            modelList.add(model)
        }

        binding.modelList.adapter = SelectModelAdapter(modelList)
        binding.modelList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val picList = ArrayList<String>()
        for (imageUrl in item.picUrl) {
            picList.add(imageUrl)
        }

        Glide.with(this)
            .load(picList[0])
            .into(binding.img)

        binding.picList.adapter = PicAdapter(picList) { selectedImageUrl ->
            Glide.with(this).load(selectedImageUrl)
                .into(binding.img)
        }

        binding.picList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun getBundle() {
        item = intent.getParcelableExtra("object")!!

        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.priceTxt.text = "${Constant.TAKA_SYMBOL}${item.price}"
        binding.ratingTxt.text = "${item.rating} Rating"
        binding.addToCart.setOnClickListener {
            item.numberInCart = numberOrder
            managmentCart.insertFood(item)
        }

        binding.backBtn.setOnClickListener { finish() }

        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }
}