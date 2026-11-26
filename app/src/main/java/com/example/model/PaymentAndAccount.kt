package com.example.model

enum class PaymentMethod(
    val title: String,
    val subtitleBn: String,
    val badgeLabel: String,
    val requiresMfsDetails: Boolean,
    val accountNumber: String? = null,
    val instructionLabel: String = "",
    val appPackageName: String? = null,
    val ussdCode: String? = null
) {
    BKASH(
        title = "bKash",
        subtitleBn = "বিকাশ Send Money • 01805122136",
        badgeLabel = "bKash",
        requiresMfsDetails = true,
        accountNumber = "01805122136",
        instructionLabel = "bKash Send Money",
        appPackageName = "com.bKash.customerapp",
        ussdCode = "*247#"
    ),
    NAGAD(
        title = "Nagad",
        subtitleBn = "নগদ Send Money • 01537214123",
        badgeLabel = "Nagad",
        requiresMfsDetails = true,
        accountNumber = "01537214123",
        instructionLabel = "Nagad Send Money",
        appPackageName = "com.konasl.nagad",
        ussdCode = "*167#"
    ),
    ROCKET(
        title = "Rocket",
        subtitleBn = "রকেট মোবাইল ব্যাংকিং",
        badgeLabel = "Rocket",
        requiresMfsDetails = true,
        accountNumber = "01537214123",
        instructionLabel = "Rocket Send Money",
        appPackageName = "com.dbbl.nexus.pay",
        ussdCode = "*322#"
    ),
    CASH_ON_DELIVERY(
        title = "Cash on Delivery",
        subtitleBn = "পণ্য হাতে পেয়ে টাকা পরিশোধ করুন",
        badgeLabel = "COD",
        requiresMfsDetails = false
    )
}

data class LocalUserAccount(
    val name: String,
    val phone: String,
    val address: String,
    val passwordHash: String
)

data class OrderRecord(
    val id: String,
    val timestampMillis: Long,
    val itemsSummary: String,
    val totalAmount: Int,
    val paymentMethod: PaymentMethod,
    val paymentReference: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String
)
