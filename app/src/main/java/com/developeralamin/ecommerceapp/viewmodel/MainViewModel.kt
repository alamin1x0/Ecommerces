package com.developeralamin.ecommerceapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.developeralamin.ecommerceapp.model.CategoryModel
import com.developeralamin.ecommerceapp.model.ItemsModel
import com.developeralamin.ecommerceapp.model.PendingOrder
import com.developeralamin.ecommerceapp.model.SliderModel
import com.developeralamin.ecommerceapp.utils.Constant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel() : ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var phone: String? = null
    private var name: String? = null

    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _category = MutableLiveData<List<CategoryModel>>()
    private val _recommended = MutableLiveData<List<ItemsModel>>()
    private val _pendingOrder = MutableLiveData<List<PendingOrder>>()


    val banners: LiveData<List<SliderModel>> = _banner
    val categories: LiveData<List<CategoryModel>> = _category
    val recommended: LiveData<List<ItemsModel>> = _recommended
    val pendingOrder: LiveData<List<PendingOrder>> = _pendingOrder


    fun loadPendingOrder() {
        db.collection(Constant.KEY_COLLECTION_SHOP_ORDER)
            .whereEqualTo(Constant.KEY_COLLECTION_USERID, phone)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                val pendingOrdersList = mutableListOf<PendingOrder>()

                querySnapshot?.documents?.forEach { doc ->
                    val data = doc.toObject(PendingOrder::class.java)
                    if (data != null) {
                        pendingOrdersList.add(data)
                    }
                }
                _pendingOrder.value = pendingOrdersList
            }
    }

    fun loadFiltered(id: String) {
        val Ref = firebaseDatabase.getReference("Items")
        val query: Query = Ref.orderByChild("categoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun loadRecommended() {
        val Ref = firebaseDatabase.getReference("Items")
        val query: Query = Ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }

                _recommended.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun loadCategory() {
        val Ref = firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }

                _category.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }

                _banner.value = lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun initPreferences(context: Context) {
        val preferencesUserName =
            context.getSharedPreferences(Constant.PREFERENCE_NAME, Context.MODE_PRIVATE)
        phone = preferencesUserName.getString(Constant.PHONE_NUMBER, "") ?: ""
        name = preferencesUserName.getString(Constant.FULL_NAME, "") ?: ""
    }
}