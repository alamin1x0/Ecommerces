package com.developeralamin.ecommerceapp.payment

import android.app.ProgressDialog
import androidx.lifecycle.MutableLiveData

object Constants {

    //test
/*    var bkashSandboxUsername = "sandboxTokenizedUser02"
    var bkashSandboxPassword = "sandboxTokenizedUser02@12345"
    var bkashSandboxAppKey = "4f6o0cjiki2rfm34kfdadl1eqq"
    var bkashSandboxAppSecret = "2is7hdktrekvrbljjh44ll3d9l1dtjo4pasmjvs5vl5qr3fug4b"
    var mode = "0011"
    var payerReference = "number"
    var callbackURL = "https://mtsbdtelecom.xyz/callback"
    var merchantAssociationInfo = ""
    var amount = "10"
    var currency = "BDT"
    var intents = "sale"
    var merchantInvoiceNumber = "Inv0124"*/

    //Live
    var bkashSandboxUsername = "01300587256"
    var bkashSandboxPassword = "5]D3f-fA[D{"
    var bkashSandboxAppKey = "llYZSzu6ZJb71QN3dPBGVpbOtc"
    var bkashSandboxAppSecret = "EY6Lt0XRZiJiArJ2PwlpmMK14OqrjBxB08WjQIXfY49JmuK2yHd8"
    var mode = "0011"
    var payerReference = "number"
    var callbackURL = "https://mtsbdtelecom.xyz/callback"
    var merchantAssociationInfo = ""
    var amount = "10"
    var currency = "BDT"
    var intents = "sale"
    var merchantInvoiceNumber = "MT01234"

    var sessionIdToken = ""
    var paymentIDBkash = ""
    var searchTextInput = ""
    var pd: ProgressDialog?= null
    val liveData = MutableLiveData<Boolean>()
}
