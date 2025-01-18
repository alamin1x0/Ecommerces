package com.developeralamin.ecommerceapp.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.developeralamin.ecommerceapp.adapter.ListItemsAdapter
import com.developeralamin.ecommerceapp.databinding.ActivityListItemsBinding
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.InternetConnection
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.viewmodel.MainViewModel

class ListItemsActivity : BaseActivity() {

    private lateinit var binding: ActivityListItemsBinding
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        if (InternetConnection.isOnline(this)) {
            getBundle()
            initList()
        } else {
            ToastHelper.showErrorToast(this, Constant.NO_INTERNET)
        }

    }

    private fun initList() {
        binding.apply {
            progressBarList.visibility = View.VISIBLE
            viewModel.recommended.observe(this@ListItemsActivity, Observer {
                viewList.layoutManager = GridLayoutManager(this@ListItemsActivity, 2)
                viewList.adapter = ListItemsAdapter(it)
                progressBarList.visibility = View.GONE
            })

            viewModel.loadFiltered(id)
        }
    }

    private fun getBundle() {
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        binding.categoryTxt.text = title
        binding.backBtn.setOnClickListener { finish() }
    }


}