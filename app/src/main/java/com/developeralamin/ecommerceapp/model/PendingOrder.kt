package com.developeralamin.ecommerceapp.model

import android.os.Parcel
import android.os.Parcelable

data class PendingOrder(
    val amount: Double = 0.0,
    val trxID: String? = null,
    val userId: String = "",
    val orderDate: String = "",
    val orderNo: String? = null,
    val status: String = "",
    val orderType: String = "",
    val dateTime: String = "",
    val source: String = "",
    val invoiceNumber: String? = null,
    val orderId: String? = null,
    val refNo: String? = null,
    val productList: List<Product> = emptyList()
) : Parcelable {
    // Parcelable constructor
    constructor(parcel: Parcel) : this(
        amount = parcel.readDouble(),
        trxID = parcel.readString(),
        userId = parcel.readString()!!,
        orderDate = parcel.readString()!!,
        orderNo = parcel.readString(),
        status = parcel.readString()!!,
        orderType = parcel.readString()!!,
        dateTime = parcel.readString()!!,
        source = parcel.readString()!!,
        invoiceNumber = parcel.readString(),
        orderId = parcel.readString(),
        refNo = parcel.readString(),
        productList = mutableListOf<Product>().apply {
            parcel.readList(this, Product::class.java.classLoader)
        }
    )

    // Parcelable methods
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(amount)
        parcel.writeString(trxID)
        parcel.writeString(userId)
        parcel.writeString(orderDate)
        parcel.writeString(orderNo)
        parcel.writeString(status)
        parcel.writeString(orderType)
        parcel.writeString(dateTime)
        parcel.writeString(source)
        parcel.writeString(invoiceNumber)
        parcel.writeString(orderId)
        parcel.writeString(refNo)
        parcel.writeList(productList)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PendingOrder> {
        override fun createFromParcel(parcel: Parcel): PendingOrder = PendingOrder(parcel)
        override fun newArray(size: Int): Array<PendingOrder?> = arrayOfNulls(size)
    }
}


data class Product(
    val categoryId: String = "",
    val description: String = "",
    val model: List<String> = emptyList(),
    val numberInCart: Int = 0,
    val picUrl: List<String> = emptyList(),
    val price: Double = 0.0,
    val rating: Double = 0.0,
    val showRecommended: Boolean = false,
    val stability: Int = 0,
    val title: String = ""
) : Parcelable {
    // Parcelable constructor
    constructor(parcel: Parcel) : this(
        categoryId = parcel.readString()!!,
        description = parcel.readString()!!,
        model = parcel.createStringArrayList() ?: emptyList(),
        numberInCart = parcel.readInt(),
        picUrl = parcel.createStringArrayList() ?: emptyList(),
        price = parcel.readDouble(),
        rating = parcel.readDouble(),
        showRecommended = parcel.readByte() != 0.toByte(),
        stability = parcel.readInt(),
        title = parcel.readString()!!
    )

    // Parcelable methods
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(categoryId)
        parcel.writeString(description)
        parcel.writeStringList(model)
        parcel.writeInt(numberInCart)
        parcel.writeStringList(picUrl)
        parcel.writeDouble(price)
        parcel.writeDouble(rating)
        parcel.writeByte(if (showRecommended) 1 else 0)
        parcel.writeInt(stability)
        parcel.writeString(title)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product = Product(parcel)
        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
}



