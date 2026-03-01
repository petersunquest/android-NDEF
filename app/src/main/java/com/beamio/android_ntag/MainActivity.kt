package com.beamio.android_ntag

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.beamio.android_ntag.ui.theme.AndroidNTAGTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** getUIDAssets API 返回结构；多卡时使用 cards，兼容单卡 legacy 字段。cardBackground/cardImage 来自该卡用户拥有的最佳 NFT 的 tier metadata；tierName/tierDescription 来自 tier 或卡级 metadata。 */
internal data class CardItem(
    val cardAddress: String,
    val cardName: String,
    val cardType: String,
    val points: String,
    val points6: String,
    val cardCurrency: String,
    val nfts: List<NftItem>,
    val cardBackground: String? = null,
    val cardImage: String? = null,
    val tierName: String? = null,
    val tierDescription: String? = null
)
internal data class UIDAssets(
    val ok: Boolean,
    val address: String? = null,
    val cardAddress: String? = null,
    val points: String? = null,
    val points6: String? = null,
    val usdcBalance: String? = null,
    val cardCurrency: String? = null,
    val nfts: List<NftItem>? = null,
    val cards: List<CardItem>? = null,
    val error: String? = null
)
internal data class NftItem(
    val tokenId: String,
    val attribute: String,
    val tier: String,
    val expiry: String,
    val isExpired: Boolean
)

internal sealed class ReadStatus {
    object Waiting : ReadStatus()
    object Loading : ReadStatus()
    object Success : ReadStatus()
    object Error : ReadStatus()
}

internal sealed class TopupStatus {
    object Waiting : TopupStatus()
    object Loading : TopupStatus()
    object Success : TopupStatus()
    object Error : TopupStatus()
}

internal sealed class PaymentStatus {
    object Waiting : PaymentStatus()
    object Routing : PaymentStatus()   // Smart Routing 分析中
    object Submitting : PaymentStatus() // 调用 payByNfcUid 中
    object Success : PaymentStatus()
    object Error : PaymentStatus()
}

internal data class RoutingStep(
    val id: String,
    val label: String,
    val detail: String,
    val status: StepStatus
)
internal enum class StepStatus { pending, loading, success, error }

private val ROUTING_STEP_IDS = listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute", "sendTx", "waitTx")
private fun createRoutingSteps() = listOf(
    RoutingStep("detectingUser", "Detecting User", "", StepStatus.pending),
    RoutingStep("membership", "Checking Membership", "", StepStatus.pending),
    RoutingStep("analyzingAssets", "Analyzing Assets", "", StepStatus.pending),
    RoutingStep("optimizingRoute", "Optimizing Route", "", StepStatus.pending),
    RoutingStep("sendTx", "Sending transaction", "", StepStatus.pending),
    RoutingStep("waitTx", "Waiting for transaction", "", StepStatus.pending)
)
private fun updateStep(steps: List<RoutingStep>, id: String, status: StepStatus, detail: String = "") =
    steps.map { if (it.id == id) it.copy(status = status, detail = detail) else it }

class MainActivity : ComponentActivity() {
    private companion object {
        const val HARD_CODED_KEY0 = "894FE8DAD6E206F142D107D11805579A"
        const val HARD_CODED_MASTER_KEY = "3DA90A7D29A5797A16ED53D91A49B803"
        const val SUN_BASE_URL = "https://api.beamio.app/api/sun"
        /** 与 SilentPassUI utils/constants.ts beamioApi 一致 */
        const val BEAMIO_API = "https://beamio.app"
        /** 基础设施卡（与 SilentPassUI BEAMIO_USER_CARD_ASSET_ADDRESS 一致，新创建卡合约地址） */
        const val BEAMIO_USER_CARD_ASSET_ADDRESS = "0xB7644DDb12656F4854dC746464af47D33C206F0E"
        /** 已废弃的旧卡地址，从 endpoint 返回的资产中过滤掉 */
        private const val DEPRECATED_CARD_ADDRESS = "0xEcC5bDFF6716847e45363befD3506B1D539c02D5"
    }

    private var showTopupScreen by mutableStateOf(false)
    private var topupScreenAmount by mutableStateOf("")
    private var topupScreenUid by mutableStateOf("")
    private var topupScreenWallet by mutableStateOf<String?>(null)
    private var topupScreenStatus by mutableStateOf<TopupStatus>(TopupStatus.Waiting)
    private var topupScreenTxHash by mutableStateOf("")
    private var topupScreenError by mutableStateOf("")
    private var topupScreenPreBalance by mutableStateOf<String?>(null)
    private var topupScreenPostBalance by mutableStateOf<String?>(null)
    private var topupScreenCardCurrency by mutableStateOf<String?>(null)
    private var topupScreenAddress by mutableStateOf<String?>(null)
    private var topupScreenCardBackground by mutableStateOf<String?>(null)
    private var topupScreenCardImage by mutableStateOf<String?>(null)
    private var topupScreenTierDescription by mutableStateOf<String?>(null)
    private var topupArmed by mutableStateOf(false)

    private var showReadScreen by mutableStateOf(false)
    private var readScreenUid by mutableStateOf("")
    private var readScreenWallet by mutableStateOf<String?>(null)
    private var readScreenStatus by mutableStateOf<ReadStatus>(ReadStatus.Waiting)
    private var readScreenAssets by mutableStateOf<UIDAssets?>(null)
    private var readScreenError by mutableStateOf("")

    private var showPaymentScreen by mutableStateOf(false)
    private var paymentScreenAmount by mutableStateOf("")
    private var paymentScreenPayee by mutableStateOf("")
    private var paymentScreenUid by mutableStateOf("")
    private var paymentScreenStatus by mutableStateOf<PaymentStatus>(PaymentStatus.Waiting)
    private var paymentScreenTxHash by mutableStateOf("")
    private var paymentScreenError by mutableStateOf("")
    private var paymentScreenRoutingSteps by mutableStateOf<List<RoutingStep>>(emptyList())
    private var paymentScreenSubtotal by mutableStateOf("0")
    private var paymentScreenTip by mutableStateOf("0")
    private var paymentScreenPreBalance by mutableStateOf<String?>(null)
    private var paymentScreenPostBalance by mutableStateOf<String?>(null)
    private var paymentScreenCardCurrency by mutableStateOf<String?>(null)
    private var paymentArmed by mutableStateOf(false)

    private var uidText by mutableStateOf("未读取")
    private var readCheckText by mutableStateOf("未检测初始化状态")
    private var readUrlText by mutableStateOf("")
    private var readParamText by mutableStateOf("")
    private var lastCounter by mutableStateOf(0)
    private var lastCounterInput by mutableStateOf("0")
    private var initText by mutableStateOf("未初始化")
    private var topupAmount by mutableStateOf("")
    private var paymentAmount by mutableStateOf("")
    private var readArmed by mutableStateOf(false)
    private var initArmed by mutableStateOf(false)
    private var initTemplateArmed by mutableStateOf(false)
    private var showOnboardingScreen by mutableStateOf(false)
    private var showAmountInputScreen by mutableStateOf(false)
    private var amountInputMode by mutableStateOf("charge") // "charge" | "topup"
    private var amountInput by mutableStateOf("0")
    private var showTipScreen by mutableStateOf(false)
    private var tipScreenSubtotal by mutableStateOf("0")
    private var selectedTipRate by mutableStateOf(0.0) // 0, 0.15, 0.18, 0.20
    private var showScanMethodScreen by mutableStateOf(false)
    private var scanMethodState by mutableStateOf("nfc") // "nfc" | "qr"
    private var scanWaitingForNfc by mutableStateOf(false) // 按下 Continue 后留在本页等待 UID
    private var nfcFetchingInfo by mutableStateOf(false) // 已获 UID，正在拉取信息，不跳新页
    private var nfcFetchError by mutableStateOf("") // 拉取失败时在 280.dp 框内显示
    private var pendingScanAction by mutableStateOf("read") // "read" | "payment" | "topup"
    private var scanMethodBackTipSubtotal by mutableStateOf("0")
    private var scanMethodBackTipRate by mutableStateOf(0.0)
    private var showInitFlowScreen by mutableStateOf(false)
    private var showInitReconfirm by mutableStateOf(false)
    private var initReconfirmAllowContinue by mutableStateOf(false)
    private var initReconfirmText by mutableStateOf("")
    private var pendingInitTag: Tag? = null
    private var pendingInitRequiresKey0Reset: Boolean = false
    private var pendingInitUseDefaultKey0: Boolean = false
    private var nfcAdapter: NfcAdapter? = null
    private var scanMethodQrUrl by mutableStateOf("")

    /** 从 beamio URL 解析 wallet 参数，如 https://beamio.app/?beamio=xxx&wallet=0x... */
    private fun parseBeamioWalletFromUrl(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            uri.getQueryParameter("wallet")?.takeIf { it.startsWith("0x") && it.length >= 40 }
        } catch (_: Exception) {
            null
        }
    }

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { handleQrScanResult(it) }
    }

    private fun handleQrScanResult(content: String) {
        val wallet = parseBeamioWalletFromUrl(content)
        if (wallet == null) {
            uidText = "无法解析 URL，请扫描 beamio.app 链接"
            return
        }
        showScanMethodScreen = false
        when (pendingScanAction) {
            "read" -> {
                showReadScreen = true
                readScreenUid = ""
                readScreenWallet = wallet
                readScreenStatus = ReadStatus.Loading
                readScreenAssets = null
                readScreenError = ""
                readArmed = false
                fetchWalletAssets(wallet)
            }
            "topup" -> {
                showTopupScreen = true
                topupScreenUid = ""
                topupScreenWallet = wallet
                topupScreenStatus = TopupStatus.Loading
                topupScreenTxHash = ""
                topupScreenError = ""
                topupScreenPreBalance = null
                topupScreenPostBalance = null
                topupScreenCardCurrency = null
                topupScreenAddress = null
                topupScreenCardBackground = null
                topupScreenCardImage = null
                topupScreenTierDescription = null
                topupArmed = false
                executeWalletTopup(wallet, topupScreenAmount)
            }
            else -> { /* payment: QR not supported for now */ }
        }
    }

    private fun appendInitStep(step: String) {
        initText = if (initText.isBlank()) step else "$initText\n$step"
    }

    private fun initFail(step: String, reason: String) {
        appendInitStep("❌ $step 失败: $reason")
        initArmed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedKey = WalletStorageManager.loadPrivateKey(this)
        if (!savedKey.isNullOrEmpty()) {
            try {
                BeamioWeb3Wallet.init(savedKey)
            } catch (_: Exception) {
                showOnboardingScreen = true
            }
        } else {
            showOnboardingScreen = true
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        enableEdgeToEdge()
        // 隐藏底部系统导航栏，用户从底部上滑可临时显示
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            AndroidNTAGTheme {
                when {
                    showOnboardingScreen -> OnboardingScreen(
                        onCreateComplete = { privateKeyHex ->
                            WalletStorageManager.savePrivateKey(this@MainActivity, privateKeyHex)
                            BeamioWeb3Wallet.init(privateKeyHex)
                            showOnboardingScreen = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showTopupScreen -> TopupScreen(
                        amount = topupScreenAmount,
                        uid = topupScreenUid,
                        status = topupScreenStatus,
                        txHash = topupScreenTxHash,
                        error = topupScreenError,
                        postBalance = topupScreenPostBalance,
                        cardCurrency = topupScreenCardCurrency,
                        address = topupScreenAddress,
                        cardBackground = topupScreenCardBackground,
                        cardImage = topupScreenCardImage,
                        tierDescription = topupScreenTierDescription,
                        onBack = { closeTopupScreen() },
                        modifier = Modifier.fillMaxSize()
                    )
                    showScanMethodScreen -> ScanMethodSelectionScreen(
                        scanMethod = scanMethodState,
                        scanWaitingForNfc = scanWaitingForNfc,
                        nfcFetchingInfo = nfcFetchingInfo,
                        nfcFetchError = nfcFetchError,
                        onScanMethodChange = { scanMethodState = it },
                        pendingAction = pendingScanAction,
                        totalAmount = when (pendingScanAction) {
                            "payment" -> paymentScreenAmount
                            "topup" -> topupScreenAmount
                            else -> ""
                        },
                        qrUrl = scanMethodQrUrl,
                        onQrUrlChange = { scanMethodQrUrl = it },
                        onProceed = { proceedFromScanMethod() },
                        onProceedNfcStay = { armForNfcScan() },
                        onProceedWithQr = if (pendingScanAction == "read" || pendingScanAction == "topup") {
                            {
                                val url = scanMethodQrUrl.trim()
                                if (url.isNotEmpty()) {
                                    handleQrScanResult(url)
                                } else {
                                    val options = ScanOptions().apply {
                                        setPrompt("扫描 Beamio 支付码")
                                    }
                                    qrScanLauncher.launch(options)
                                }
                            }
                        } else null,
                        onCancel = {
                            scanWaitingForNfc = false
                            nfcFetchingInfo = false
                            nfcFetchError = ""
                            readArmed = false
                            topupArmed = false
                            paymentArmed = false
                            showScanMethodScreen = false
                            when (pendingScanAction) {
                                "payment" -> {
                                    tipScreenSubtotal = scanMethodBackTipSubtotal
                                    selectedTipRate = scanMethodBackTipRate
                                    showTipScreen = true
                                }
                                "topup" -> {
                                    amountInput = topupScreenAmount
                                    amountInputMode = "topup"
                                    showAmountInputScreen = true
                                }
                                else -> { /* read: just close, back to NdefScreen */ }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showReadScreen -> ReadScreen(
                        uid = readScreenUid,
                        status = readScreenStatus,
                        assets = readScreenAssets,
                        error = readScreenError,
                        onBack = { closeReadScreen() },
                        onTopupClick = {
                            closeReadScreen()
                            amountInput = "0"
                            amountInputMode = "topup"
                            showAmountInputScreen = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showAmountInputScreen -> ChargeAmountScreen(
                        mode = amountInputMode,
                        amount = amountInput,
                        onPadClick = { handleAmountPadClick(it) },
                        onBack = {
                            showAmountInputScreen = false
                            amountInput = "0"
                        },
                        onContinue = {
                            val amt = amountInput.trim()
                            if (amt != "0" && amt != "0." && amt.toDoubleOrNull()?.let { it > 0 } == true) {
                                showAmountInputScreen = false
                                amountInput = "0"
                                when (amountInputMode) {
                                    "topup" -> startTopup(amt)
                                    "charge" -> {
                                        tipScreenSubtotal = amt
                                        selectedTipRate = 0.0
                                        showTipScreen = true
                                    }
                                    else -> startPayment(amt)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showTipScreen -> TipSelectionScreen(
                        subtotal = tipScreenSubtotal,
                        selectedTipRate = selectedTipRate,
                        onTipRateSelect = { selectedTipRate = it },
                        onBack = {
                            showTipScreen = false
                            amountInput = tipScreenSubtotal
                            showAmountInputScreen = true
                        },
                        onConfirmPay = {
                            val subtotal = tipScreenSubtotal.toDoubleOrNull() ?: 0.0
                            val tip = subtotal * selectedTipRate
                            val total = subtotal + tip
                            paymentScreenSubtotal = "%.2f".format(subtotal)
                            paymentScreenTip = "%.2f".format(tip)
                            scanMethodBackTipSubtotal = tipScreenSubtotal
                            scanMethodBackTipRate = selectedTipRate
                            showTipScreen = false
                            tipScreenSubtotal = "0"
                            selectedTipRate = 0.0
                            startPayment("%.2f".format(total))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showPaymentScreen -> PaymentScreen(
                        amount = paymentScreenAmount,
                        payee = paymentScreenPayee,
                        uid = paymentScreenUid,
                        status = paymentScreenStatus,
                        txHash = paymentScreenTxHash,
                        error = paymentScreenError,
                        routingSteps = paymentScreenRoutingSteps,
                        subtotal = paymentScreenSubtotal,
                        tip = paymentScreenTip,
                        postBalance = paymentScreenPostBalance,
                        cardCurrency = paymentScreenCardCurrency,
                        onBack = { closePaymentScreen() },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> NdefScreen(
                        uidText = uidText,
                        readUrlText = readUrlText,
                        readParamText = readParamText,
                        walletAddress = if (BeamioWeb3Wallet.isInitialized()) BeamioWeb3Wallet.getAddress() else null,
                        onCopyWalletClick = {
                            if (BeamioWeb3Wallet.isInitialized()) {
                                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("wallet", BeamioWeb3Wallet.getAddress()))
                            }
                        },
                        onReadClick = { startReadUid() },
                        onTopupClick = {
                            amountInput = "0"
                            amountInputMode = "topup"
                            showAmountInputScreen = true
                        },
                        onPaymentClick = {
                            amountInput = "0"
                            amountInputMode = "charge"
                            showAmountInputScreen = true
                        },
                        onCopyUrlClick = {
                            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cm?.setPrimaryClip(ClipData.newPlainText("url", readUrlText))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun startReadUid() {
        if (nfcAdapter == null) {
            uidText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            uidText = "请先开启 NFC"
            return
        }
        pendingScanAction = "read"
        scanMethodState = "nfc"
        showScanMethodScreen = true
    }

    private fun closeReadScreen() {
        showReadScreen = false
        readArmed = false
    }

    private fun startTopup(amount: String) {
        if (nfcAdapter == null) return
        if (nfcAdapter?.isEnabled != true) return
        topupScreenAmount = amount
        pendingScanAction = "topup"
        scanMethodState = "nfc"
        showScanMethodScreen = true
    }

    private fun closeTopupScreen() {
        showTopupScreen = false
        topupArmed = false
    }

    private fun startPayment(amount: String) {
        if (nfcAdapter == null) return
        if (nfcAdapter?.isEnabled != true) return
        paymentScreenAmount = amount
        paymentScreenPayee = BeamioWeb3Wallet.getAddress()
        pendingScanAction = "payment"
        scanMethodState = "nfc"
        showScanMethodScreen = true
    }

    /** NFC 模式下：按下 Continue 后留在本页，显示 loading，等待贴卡获得 UID */
    private fun armForNfcScan() {
        scanWaitingForNfc = true
        nfcFetchError = ""
        when (pendingScanAction) {
            "read" -> {
                readScreenUid = ""
                readScreenWallet = null
                readScreenStatus = ReadStatus.Loading
                readScreenAssets = null
                readScreenError = ""
                readArmed = true
                initArmed = false
                initTemplateArmed = false
                paymentArmed = false
                topupArmed = false
            }
            "topup" -> {
                topupScreenUid = ""
                topupScreenWallet = null
                topupScreenStatus = TopupStatus.Loading
                topupScreenTxHash = ""
                topupScreenError = ""
                topupScreenPreBalance = null
                topupScreenPostBalance = null
                topupScreenCardCurrency = null
                topupScreenAddress = null
                topupScreenCardBackground = null
                topupScreenCardImage = null
                topupScreenTierDescription = null
                topupArmed = true
                readArmed = false
                initArmed = false
                paymentArmed = false
            }
            "payment" -> {
                paymentScreenUid = ""
                paymentScreenStatus = PaymentStatus.Routing
                paymentScreenTxHash = ""
                paymentScreenError = ""
                paymentScreenRoutingSteps = emptyList()
                paymentScreenPreBalance = null
                paymentScreenPostBalance = null
                paymentScreenCardCurrency = null
                paymentArmed = true
                readArmed = false
                initArmed = false
                topupArmed = false
            }
            else -> { scanWaitingForNfc = false }
        }
    }

    private fun proceedFromScanMethod() {
        showScanMethodScreen = false
        scanWaitingForNfc = false
        when (pendingScanAction) {
            "read" -> {
                showReadScreen = true
                readScreenUid = ""
                readScreenWallet = null
                readScreenStatus = ReadStatus.Waiting
                readScreenAssets = null
                readScreenError = ""
                readArmed = true
                initArmed = false
                initTemplateArmed = false
                paymentArmed = false
                topupArmed = false
                uidText = "请贴卡读取 UID..."
            }
            "topup" -> {
                showTopupScreen = true
                topupScreenUid = ""
                topupScreenWallet = null
                topupScreenStatus = TopupStatus.Waiting
                topupScreenTxHash = ""
                topupScreenError = ""
                topupScreenPreBalance = null
                topupScreenPostBalance = null
                topupScreenCardCurrency = null
                topupScreenAddress = null
                topupScreenCardBackground = null
                topupScreenCardImage = null
                topupScreenTierDescription = null
                topupArmed = true
                readArmed = false
                initArmed = false
                paymentArmed = false
            }
            "payment" -> {
                showPaymentScreen = true
                paymentScreenUid = ""
                paymentScreenStatus = PaymentStatus.Waiting
                paymentScreenTxHash = ""
                paymentScreenError = ""
                paymentScreenRoutingSteps = emptyList()
                paymentScreenPreBalance = null
                paymentScreenPostBalance = null
                paymentScreenCardCurrency = null
                paymentArmed = true
                readArmed = false
                initArmed = false
                topupArmed = false
            }
        }
    }

    private fun closePaymentScreen() {
        showPaymentScreen = false
        paymentArmed = false
    }

    private fun handleAmountPadClick(valStr: String) {
        when (valStr) {
            "back" -> {
                amountInput = if (amountInput.length > 1) amountInput.dropLast(1) else "0"
            }
            "." -> {
                if (!amountInput.contains(".")) amountInput += "."
            }
            else -> {
                amountInput = when {
                    amountInput == "0" -> valStr
                    amountInput.contains(".") && amountInput.substringAfter(".").length >= 2 -> amountInput
                    else -> amountInput + valStr
                }
            }
        }
    }

    private fun executeNfcTopup(uid: String, amount: String) {
        Thread {
            try {
                // 1. Topup 之前先拉取余额，对齐后端返回码（避免 UID/QR 混淆时解析 HTML 报错）
                val preAssets = fetchUidAssetsSync(uid)
                if (preAssets == null || !preAssets.ok) {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = preAssets?.error ?: "查询失败"
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = preAssets?.error ?: "查询失败"
                        }
                    }
                    return@Thread
                }

                val prepare = nfcTopupPrepare(uid, amount)
                if (prepare.error != null) {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = prepare.error!!
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = prepare.error!!
                        }
                    }
                    return@Thread
                }
                // 多卡时使用 topup 所使用的卡的余额与货币，而非首卡/CCSA
                val cardAddr = prepare.cardAddr!!
                val topupCard = preAssets.cards?.firstOrNull { it.cardAddress.equals(cardAddr, ignoreCase = true) }
                val preBalanceStr = topupCard?.points ?: preAssets.points ?: "0"
                val preCurrency = topupCard?.cardCurrency ?: preAssets.cardCurrency ?: "CAD"
                runOnUiThread {
                    topupScreenPreBalance = preBalanceStr
                    topupScreenCardCurrency = preCurrency
                    topupScreenAddress = preAssets.address
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierDescription = topupCard?.tierDescription
                }

                val adminSig = BeamioWeb3Wallet.signExecuteForAdmin(
                    prepare.cardAddr!!,
                    prepare.data!!,
                    prepare.deadline!!,
                    prepare.nonce!!
                )
                val result = nfcTopup(uid, amount, prepare.cardAddr!!, prepare.data!!, prepare.deadline!!, prepare.nonce!!, adminSig)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    if (result.success) {
                        val preNum = preBalanceStr.toDoubleOrNull() ?: 0.0
                        val amtNum = amount.toDoubleOrNull() ?: 0.0
                        topupScreenPostBalance = "%.2f".format(preNum + amtNum)
                        topupScreenStatus = TopupStatus.Success
                        topupScreenTxHash = result.txHash ?: ""
                        topupScreenError = ""
                        if (fromScan) {
                            nfcFetchingInfo = false
                            showScanMethodScreen = false
                            showTopupScreen = true
                        }
                    } else {
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = result.error ?: "Topup 失败"
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = result.error ?: "Topup 失败"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executeNfcTopup", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    topupScreenStatus = TopupStatus.Error
                    topupScreenError = e.message ?: "执行失败"
                    if (fromScan) {
                        nfcFetchingInfo = false
                        nfcFetchError = e.message ?: "执行失败"
                    }
                }
            }
        }.start()
    }

    private data class NfcTopupPrepareResult(
        val cardAddr: String?,
        val data: String?,
        val deadline: Long?,
        val nonce: String?,
        val error: String?
    )

    private fun nfcTopupPrepare(uid: String, amount: String): NfcTopupPrepareResult =
        nfcTopupPrepareInternal(uid = uid, wallet = null, amount = amount)

    private fun nfcTopupPrepareWithWallet(wallet: String, amount: String): NfcTopupPrepareResult =
        nfcTopupPrepareInternal(uid = null, wallet = wallet, amount = amount)

    private fun nfcTopupPrepareInternal(uid: String?, wallet: String?, amount: String): NfcTopupPrepareResult {
        val url = java.net.URL("$BEAMIO_API/api/nfcTopupPrepare")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        val body = org.json.JSONObject().apply {
            if (uid != null) put("uid", uid)
            if (wallet != null) put("wallet", wallet)
            put("amount", amount)
            put("currency", "CAD")
            put("cardAddress", BEAMIO_USER_CARD_ASSET_ADDRESS)
        }.toString()
        conn.outputStream.use { os ->
            os.write(body.toByteArray(Charsets.UTF_8))
        }
        val responseJson = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
        conn.disconnect()
        return try {
            val root = org.json.JSONObject(responseJson)
            val err = root.optString("error").takeIf { it.isNotEmpty() }
            if (err != null) NfcTopupPrepareResult(null, null, null, null, err)
            else NfcTopupPrepareResult(
                cardAddr = root.optString("cardAddr").takeIf { it.isNotEmpty() },
                data = root.optString("data").takeIf { it.isNotEmpty() },
                deadline = root.optLong("deadline", 0L).takeIf { it > 0 },
                nonce = root.optString("nonce").takeIf { it.isNotEmpty() },
                error = null
            )
        } catch (_: Exception) {
            NfcTopupPrepareResult(null, null, null, null, "接口返回异常，请检查网络")
        }
    }

    private fun executeWalletTopup(wallet: String, amount: String) {
        Thread {
            try {
                // 1. Topup 之前先拉取余额，对齐后端 400/404/500 返回码
                val preAssets = fetchWalletAssetsSync(wallet)
                if (preAssets == null || !preAssets.ok) {
                    runOnUiThread {
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = preAssets?.error ?: "查询失败"
                    }
                    return@Thread
                }

                val prepare = nfcTopupPrepareWithWallet(wallet, amount)
                if (prepare.error != null) {
                    runOnUiThread {
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = prepare.error!!
                    }
                    return@Thread
                }
                // 多卡时使用 topup 所使用的卡的余额与货币，而非首卡/CCSA
                val cardAddr = prepare.cardAddr!!
                val topupCard = preAssets.cards?.firstOrNull { it.cardAddress.equals(cardAddr, ignoreCase = true) }
                val preBalanceStr = topupCard?.points ?: preAssets.points ?: "0"
                val preCurrency = topupCard?.cardCurrency ?: preAssets.cardCurrency ?: "CAD"
                runOnUiThread {
                    topupScreenPreBalance = preBalanceStr
                    topupScreenCardCurrency = preCurrency
                    topupScreenAddress = preAssets.address
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierDescription = topupCard?.tierDescription
                }

                val adminSig = BeamioWeb3Wallet.signExecuteForAdmin(
                    prepare.cardAddr!!,
                    prepare.data!!,
                    prepare.deadline!!,
                    prepare.nonce!!
                )
                val result = nfcTopupWithWallet(wallet, prepare.cardAddr!!, prepare.data!!, prepare.deadline!!, prepare.nonce!!, adminSig)
                runOnUiThread {
                    if (result.success) {
                        val preNum = preBalanceStr.toDoubleOrNull() ?: 0.0
                        val amtNum = amount.toDoubleOrNull() ?: 0.0
                        topupScreenPostBalance = "%.2f".format(preNum + amtNum)
                        topupScreenStatus = TopupStatus.Success
                        topupScreenTxHash = result.txHash ?: ""
                        topupScreenError = ""
                    } else {
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = result.error ?: "Topup 失败"
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executeWalletTopup", e)
                runOnUiThread {
                    topupScreenStatus = TopupStatus.Error
                    topupScreenError = e.message ?: "执行失败"
                }
            }
        }.start()
    }

    private fun nfcTopupWithWallet(wallet: String, cardAddr: String, data: String, deadline: Long, nonce: String, adminSignature: String): NfcTopupResult {
        val url = java.net.URL("$BEAMIO_API/api/nfcTopup")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 60000
        val body = org.json.JSONObject().apply {
            put("cardAddr", cardAddr)
            put("data", data)
            put("deadline", deadline)
            put("nonce", nonce)
            put("adminSignature", adminSignature)
            put("wallet", wallet)
        }.toString()
        conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
        conn.disconnect()
        return try {
            val root = org.json.JSONObject(resp)
            NfcTopupResult(
                success = code in 200..299 && root.optBoolean("success", true),
                txHash = root.optString("txHash").takeIf { it.isNotEmpty() },
                error = root.optString("error").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) {
            NfcTopupResult(success = false, txHash = null, error = "接口返回异常，请检查网络")
        }
    }

    private data class NfcTopupResult(val success: Boolean, val txHash: String?, val error: String?)

    private fun nfcTopup(uid: String, amount: String, cardAddr: String, data: String, deadline: Long, nonce: String, adminSignature: String): NfcTopupResult {
        val url = java.net.URL("$BEAMIO_API/api/nfcTopup")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 60000
        val body = org.json.JSONObject().apply {
            put("cardAddr", cardAddr)
            put("data", data)
            put("deadline", deadline)
            put("nonce", nonce)
            put("adminSignature", adminSignature)
            put("uid", uid)
        }.toString()
        conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
        conn.disconnect()
        return try {
            val root = org.json.JSONObject(resp)
            NfcTopupResult(
                success = code in 200..299 && root.optBoolean("success", true),
                txHash = root.optString("txHash").takeIf { it.isNotEmpty() },
                error = root.optString("error").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) {
            NfcTopupResult(success = false, txHash = null, error = "接口返回异常，请检查网络")
        }
    }

    /** CAD 转 USDC6：金额(CAD) / usdcad 汇率 * 1e6。默认 usdcad=1.35 */
    private fun cadToUsdc6(cadAmount: Double, usdcad: Double = 1.35): String {
        if (cadAmount <= 0 || usdcad <= 0) return "0"
        val usdAmount = cadAmount / usdcad
        return (usdAmount * 1_000_000).toLong().toString()
    }

    /** 从 API 获取 oracle usdcad，失败则用 1.35 */
    private fun fetchUsdcadRate(): Double {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getOracle")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = conn.inputStream.use { it.bufferedReader().readText() }
            conn.disconnect()
            val root = org.json.JSONObject(json)
            root.optString("usdcad").toDoubleOrNull()?.takeIf { it > 0 } ?: 1.35
        } catch (_: Exception) {
            1.35
        }
    }

    private fun executePayment(uid: String, amountCad: String, payee: String) {
        Thread {
            try {
                val amountNum = amountCad.toDoubleOrNull() ?: 0.0
                if (amountNum <= 0) {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "金额无效"
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = "金额无效" }
                    }
                    return@Thread
                }
                val usdcad = fetchUsdcadRate()
                val amountUsdc6 = cadToUsdc6(amountNum, usdcad)
                if (amountUsdc6 == "0") {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "金额换算失败"
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = "金额换算失败" }
                    }
                    return@Thread
                }
                var steps = createRoutingSteps()
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Routing
                    paymentScreenRoutingSteps = updateStep(steps, "detectingUser", StepStatus.loading)
                }
                steps = updateStep(steps, "detectingUser", StepStatus.success, "NFC card detected")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "membership", StepStatus.loading) }
                steps = updateStep(steps, "membership", StepStatus.success, "NFC card payment")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                val assets = fetchUidAssetsSync(uid)
                if (assets == null || !assets.ok) {
                    steps = updateStep(steps, "analyzingAssets", StepStatus.error, assets?.error ?: "该卡没有被登记")
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = assets?.error ?: "该卡没有被登记"
                        paymentScreenRoutingSteps = steps
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = assets?.error ?: "该卡没有被登记" }
                    }
                    return@Thread
                }
                runOnUiThread {
                    paymentScreenPreBalance = assets.points
                    paymentScreenCardCurrency = assets.cardCurrency
                }
                steps = updateStep(steps, "analyzingAssets", StepStatus.success, "CCSA + USDC balance")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading) }
                steps = updateStep(steps, "optimizingRoute", StepStatus.success, "Direct: NFC → Merchant")
                // 总余额 = USDC + CCSA 折算为 USDC（CCSA 为 CAD，1 CAD = 1/usdcad USDC）
                val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
                val points6 = assets.points6?.toLongOrNull() ?: 0L
                val ccsaValueUsdc6 = if (usdcad > 0 && points6 > 0) (points6.toDouble() / usdcad).toLong() else 0L
                val totalBalance6 = usdcBalance6.toLong() + ccsaValueUsdc6
                val required6 = amountUsdc6.toLongOrNull() ?: 0L
                if (totalBalance6 < required6) {
                    steps = updateStep(steps, "analyzingAssets", StepStatus.error, "余额不足")
                    val errMsg = "余额不足（需 ${String.format("%.2f", required6 / 1_000_000.0)} USDC，CCSA+USDC 合计约 ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC）"
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = errMsg
                        paymentScreenRoutingSteps = steps
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = errMsg }
                    }
                    return@Thread
                }
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val result = payByNfcUid(uid, amountUsdc6, payee)
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "支付失败"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    paymentScreenRoutingSteps = steps
                    if (result.success) {
                        val pre = paymentScreenPreBalance?.toDoubleOrNull() ?: 0.0
                        val sub = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                        val t = paymentScreenTip.toDoubleOrNull() ?: 0.0
                        paymentScreenPostBalance = "%.2f".format((pre - sub - t).coerceAtLeast(0.0))
                        paymentScreenStatus = PaymentStatus.Success
                        paymentScreenTxHash = result.txHash ?: ""
                        paymentScreenError = ""
                        if (fromScan) {
                            nfcFetchingInfo = false
                            showScanMethodScreen = false
                            showPaymentScreen = true
                        }
                    } else {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = result.error ?: "支付失败"
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = result.error ?: "支付失败" }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executePayment", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    paymentScreenStatus = PaymentStatus.Error
                    paymentScreenError = e.message ?: "执行失败"
                    if (fromScan) { nfcFetchingInfo = false; nfcFetchError = e.message ?: "执行失败" }
                }
            }
        }.start()
    }

    private fun fetchUidAssetsSync(uid: String): UIDAssets? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getUIDAssets")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply { put("uid", uid) }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val respBody = if (code in 200..299) conn.inputStream else conn.errorStream
            val json = respBody?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            parseUidAssetsJson(json)
        } catch (_: Exception) {
            null
        }
    }

    private data class PayByNfcResult(val success: Boolean, val txHash: String?, val error: String?)

    private fun payByNfcUid(uid: String, amountUsdc6: String, payee: String): PayByNfcResult {
        val url = java.net.URL("$BEAMIO_API/api/payByNfcUid")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 30000
        conn.readTimeout = 60000
        val body = org.json.JSONObject().apply {
            put("uid", uid)
            put("amountUsdc6", amountUsdc6)
            put("payee", payee)
        }.toString()
        conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
        conn.disconnect()
        val root = org.json.JSONObject(resp)
        return PayByNfcResult(
            success = code in 200..299 && root.optBoolean("success", true),
            txHash = root.optString("USDC_tx").takeIf { it.isNotEmpty() },
            error = root.optString("error").takeIf { it.isNotEmpty() }
        )
    }

    private fun fetchWalletAssets(wallet: String) {
        Thread {
            try {
                val apiUrl = java.net.URL("$BEAMIO_API/api/getWalletAssets")
                val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                val body = org.json.JSONObject().apply { put("wallet", wallet) }.toString()
                conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val respBody = if (code in 200..299) conn.inputStream else conn.errorStream
                val json = respBody?.use { it.bufferedReader().readText() } ?: "{}"
                conn.disconnect()
                val parsed = parseUidAssetsJson(json)
                val displayError = if (parsed.ok) null else (parsed.error ?: "查询失败")
                runOnUiThread {
                    if (parsed.ok) {
                        readScreenAssets = parsed
                        readScreenStatus = ReadStatus.Success
                        readScreenError = ""
                    } else {
                        readScreenStatus = ReadStatus.Error
                        readScreenError = displayError ?: "查询失败"
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "fetchWalletAssets", e)
                runOnUiThread {
                    readScreenStatus = ReadStatus.Error
                    readScreenError = e.message ?: "网络请求失败"
                }
            }
        }.start()
    }

    private fun fetchWalletAssetsSync(wallet: String): UIDAssets? {
        return try {
            val apiUrl = java.net.URL("$BEAMIO_API/api/getWalletAssets")
            val conn = apiUrl.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply { put("wallet", wallet) }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val respBody = if (code in 200..299) conn.inputStream else conn.errorStream
            val json = respBody?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            parseUidAssetsJson(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchUidAssets(uid: String) {
        Thread {
            try {
                val url = java.net.URL("$BEAMIO_API/api/getUIDAssets")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                val body = org.json.JSONObject().apply { put("uid", uid) }.toString()
                conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val respBody = if (code in 200..299) conn.inputStream else conn.errorStream
                val json = respBody?.use { it.bufferedReader().readText() } ?: "{}"
                conn.disconnect()
                // Debug: 与服务器返回一致，便于排查 cardBackground 未解析/未渲染
                Log.d("getUIDAssets", "response JSON length=${json.length} preview=${json.take(800)}")
                val parsed = parseUidAssetsJson(json)
                Log.d("getUIDAssets", "parsed cards: ${parsed.cards?.joinToString { "${it.cardName}(cardBackground=${it.cardBackground})" } ?: "null"}")
                val displayError = if (parsed.ok) null else (parsed.error ?: "查询失败")
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    if (parsed.ok) {
                        readScreenAssets = parsed
                        readScreenStatus = ReadStatus.Success
                        readScreenError = ""
                        if (fromScan) {
                            nfcFetchingInfo = false
                            showScanMethodScreen = false
                            showReadScreen = true
                        }
                    } else {
                        readScreenStatus = ReadStatus.Error
                        readScreenError = displayError ?: "查询失败"
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = displayError ?: "查询失败"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "fetchUidAssets", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    readScreenStatus = ReadStatus.Error
                    readScreenError = e.message ?: "网络请求失败"
                    if (fromScan) {
                        nfcFetchingInfo = false
                        nfcFetchError = e.message ?: "网络请求失败"
                    }
                }
            }
        }.start()
    }

    private fun parseUidAssetsJson(json: String): UIDAssets {
        return try {
            val root = org.json.JSONObject(json)
            val nftsArr = root.optJSONArray("nfts")
            val nfts = mutableListOf<NftItem>()
            if (nftsArr != null) {
                for (i in 0 until nftsArr.length()) {
                    val o = nftsArr.getJSONObject(i)
                    nfts.add(NftItem(
                        tokenId = o.optString("tokenId", ""),
                        attribute = o.optString("attribute", ""),
                        tier = o.optString("tier", ""),
                        expiry = o.optString("expiry", ""),
                        isExpired = o.optBoolean("isExpired", false)
                    ))
                }
            }
            val cardsArr = root.optJSONArray("cards")
            val rawCards = if (cardsArr != null && cardsArr.length() > 0) {
                (0 until cardsArr.length()).map { idx ->
                    val c = cardsArr.getJSONObject(idx)
                    val cnfts = c.optJSONArray("nfts") ?: org.json.JSONArray()
                    val nftList = (0 until cnfts.length()).map { i ->
                        val o = cnfts.getJSONObject(i)
                        NftItem(
                            tokenId = o.optString("tokenId", ""),
                            attribute = o.optString("attribute", ""),
                            tier = o.optString("tier", ""),
                            expiry = o.optString("expiry", ""),
                            isExpired = o.optBoolean("isExpired", false)
                        )
                    }
                    val cardBgRaw = c.optString("cardBackground", "")
                    if (cardBgRaw.isNotEmpty()) Log.d("getUIDAssets", "card[$idx] ${c.optString("cardName")} cardBackground raw=\"$cardBgRaw\"")
                    CardItem(
                        cardAddress = c.optString("cardAddress", ""),
                        cardName = c.optString("cardName", "Card"),
                        cardType = c.optString("cardType", ""),
                        points = c.optString("points", "0"),
                        points6 = c.optString("points6", "0"),
                        cardCurrency = c.optString("cardCurrency", "CAD"),
                        nfts = nftList,
                        cardBackground = cardBgRaw.takeIf { it.isNotEmpty() },
                        cardImage = c.optString("cardImage").takeIf { it.isNotEmpty() },
                        tierName = c.optString("tierName").takeIf { it.isNotEmpty() },
                        tierDescription = c.optString("tierDescription").takeIf { it.isNotEmpty() }
                    )
                }
            } else {
                null
            }
            // 过滤掉已废弃的旧卡地址
            val cards = rawCards?.filter { !it.cardAddress.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true) }?.takeIf { it.isNotEmpty() }
            if (cards != null) {
                val first = cards.firstOrNull()
                UIDAssets(
                    ok = root.optBoolean("ok", false),
                    address = root.optString("address").takeIf { it.isNotEmpty() },
                    cardAddress = first?.cardAddress,
                    points = first?.points,
                    points6 = first?.points6,
                    usdcBalance = root.optString("usdcBalance").takeIf { it.isNotEmpty() },
                    cardCurrency = first?.cardCurrency,
                    nfts = first?.nfts?.takeIf { it.isNotEmpty() },
                    cards = cards,
                    error = root.optString("error").takeIf { it.isNotEmpty() }
                )
            } else {
                val legacyCardAddr = root.optString("cardAddress").takeIf { it.isNotEmpty() }
                val isDeprecatedLegacy = legacyCardAddr?.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true) == true
                UIDAssets(
                    ok = root.optBoolean("ok", false),
                    address = root.optString("address").takeIf { it.isNotEmpty() },
                    cardAddress = if (isDeprecatedLegacy) null else legacyCardAddr,
                    points = if (isDeprecatedLegacy) null else root.optString("points").takeIf { it.isNotEmpty() },
                    points6 = if (isDeprecatedLegacy) null else root.optString("points6").takeIf { it.isNotEmpty() },
                    usdcBalance = root.optString("usdcBalance").takeIf { it.isNotEmpty() },
                    cardCurrency = if (isDeprecatedLegacy) null else root.optString("cardCurrency").takeIf { it.isNotEmpty() },
                    nfts = if (isDeprecatedLegacy) null else nfts.takeIf { it.isNotEmpty() },
                    cards = null,
                    error = root.optString("error").takeIf { it.isNotEmpty() }
                )
            }
        } catch (_: Exception) {
            UIDAssets(ok = false, error = "解析响应失败")
        }
    }

    private fun startInitCard() {
        if (nfcAdapter == null) {
            initText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "请先开启 NFC"
            return
        }
        initArmed = true
        initTemplateArmed = false
        readArmed = false
        showInitFlowScreen = true
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
        initText = "⏳ [0/11] Init 已启动，请贴卡..."
    }

    private fun startInitTemplateOnly() {
        if (nfcAdapter == null) {
            initText = "设备不支持 NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "请先开启 NFC"
            return
        }
        initArmed = false
        initTemplateArmed = true
        readArmed = false
        showInitFlowScreen = true
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
        initText = "⏳ [T 0/2] 仅写模板模式已启动，请贴卡..."
    }

    private fun continueReInit() {
        val tag = pendingInitTag
        if (tag == null) {
            initText = "❌ 未找到待确认的卡，请重新点击 init"
            showInitReconfirm = false
            initReconfirmAllowContinue = false
            return
        }
        if (!initReconfirmAllowContinue) {
            initText = "❌ 当前卡密钥不匹配，无法继续 init"
            return
        }
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        initText = "⏳ 已确认继续，开始重新 init..."
        handleInitTag(
            tag,
            allowKey2Fallback = !pendingInitRequiresKey0Reset && !pendingInitUseDefaultKey0,
            useDefaultKey0 = pendingInitUseDefaultKey0
        )
    }

    private fun cancelInitFlow() {
        initArmed = false
        initTemplateArmed = false
        readArmed = false
        showInitFlowScreen = false
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        pendingInitTag = null
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = false
    }

    override fun onResume() {
        super.onResume()
        val adapter = nfcAdapter ?: return
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        // 抢占前台 NFC 分发，避免系统默认读取页面接管。
        adapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action ?: return
        if (action != NfcAdapter.ACTION_TAG_DISCOVERED &&
            action != NfcAdapter.ACTION_TECH_DISCOVERED &&
            action != NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            return
        }

        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: run {
            uidText = "读取失败：未获取到 Tag"
            return
        }

        if (!readArmed && !initArmed && !initTemplateArmed && !topupArmed && !paymentArmed) {
            uidText = "已拦截系统读取，请先点击 read/init/topup/payment"
            return
        }

        if (paymentArmed) {
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: return
            scanWaitingForNfc = false
            nfcFetchingInfo = true
            nfcFetchError = ""
            paymentScreenUid = uid
            paymentScreenStatus = PaymentStatus.Routing
            paymentScreenError = ""
            paymentArmed = false
            executePayment(uid, paymentScreenAmount, BeamioWeb3Wallet.getAddress())
            return
        }

        if (topupArmed) {
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: return
            scanWaitingForNfc = false
            nfcFetchingInfo = true
            nfcFetchError = ""
            topupScreenUid = uid
            topupScreenStatus = TopupStatus.Loading
            topupScreenError = ""
            topupArmed = false
            executeNfcTopup(uid, topupScreenAmount)
            return
        }

        if (initTemplateArmed) {
            handleTemplateOnlyTag(tag)
            return
        }

        if (initArmed) {
            handleInitFlowTag(tag)
            return
        }

        if (readArmed) {
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "读取失败"
            scanWaitingForNfc = false
            nfcFetchingInfo = true
            nfcFetchError = ""
            uidText = uid
            readScreenUid = uid
            readScreenStatus = ReadStatus.Loading
            readScreenAssets = null
            readScreenError = ""
            readArmed = false
            fetchUidAssets(uid)
        }
    }

    private data class InitInspectResult(val isInitCard: Boolean, val url: String?)
    private data class InitKeyProbeResult(
        val key0AppMatch: Boolean,
        val key0DefaultMatch: Boolean,
        val masterMatch: Boolean
    )

    private fun handleInitFlowTag(tag: Tag) {
        appendInitStep("⏳ 检测到卡，正在检查是否已初始化...")
        val inspect = inspectCardInitState(tag)
        if (inspect.isInitCard) {
            pendingInitTag = tag
            pendingInitUseDefaultKey0 = false
            pendingInitRequiresKey0Reset = false
            showInitReconfirm = true
            appendInitStep("⏳ 该卡已初始化，正在探测 key0/masterKey 匹配...")
            val probe = probeInitKeyMatch(tag)
            val allowContinue: Boolean
            val modeText: String
            if (probe.key0DefaultMatch) {
                // 出厂空白卡路径：仅凭默认 key0 可继续
                allowContinue = true
                pendingInitUseDefaultKey0 = true
                pendingInitRequiresKey0Reset = false
                modeText = "检测到 key0=0x00（出厂空白卡路径）"
            } else if (probe.key0AppMatch && probe.masterMatch) {
                // 已配置路径：app key0 + app masterKey
                allowContinue = true
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = true
                modeText = "检测到 key0=AppKey0 且 masterKey 匹配（重置路径）"
            } else {
                allowContinue = false
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = false
                modeText = "密钥不匹配，无法继续"
            }
            initReconfirmAllowContinue = allowContinue
            initReconfirmText = buildString {
                append("该卡已经 init。\n")
                append("key0(app)=${if (probe.key0AppMatch) "匹配" else "不匹配"}，")
                append("key0(0x00)=${if (probe.key0DefaultMatch) "匹配" else "不匹配"}，")
                append("masterKey(app)=${if (probe.masterMatch) "匹配" else "不匹配"}\n")
                append("$modeText\n")
                if (allowContinue) {
                    append("是否继续？\n")
                } else {
                    append("该卡无法完成 init（key0/masterKey 不匹配），仅可取消。\n")
                }
                if (!inspect.url.isNullOrBlank()) append(inspect.url)
            }
            appendInitStep(
                if (allowContinue) "⚠️ 已初始化卡：可继续或取消"
                else "❌ 已初始化卡：密钥不匹配，仅可取消"
            )
            return
        }
        // 未初始化/空卡：直接按出厂 key0=0x00 路径执行，避免前置探测误判拦截。
        appendInitStep("✅ 未检测到已初始化数据，按出厂 key0=0x00 路径执行 init")
        pendingInitRequiresKey0Reset = false
        pendingInitUseDefaultKey0 = true
        handleInitTag(tag, allowKey2Fallback = false, useDefaultKey0 = true)
    }

    private fun probeInitKeyMatch(tag: Tag): InitKeyProbeResult {
        val uid = tag.id ?: return InitKeyProbeResult(false, false, false)
        val key0App = hexToBytes(HARD_CODED_KEY0) ?: return InitKeyProbeResult(false, false, false)
        val master = hexToBytes(HARD_CODED_MASTER_KEY) ?: return InitKeyProbeResult(false, false, false)
        val key0Default = ByteArray(16)

        fun auth(keyNo: Int, key: ByteArray): Boolean {
            var iso: IsoDep? = null
            return try {
                iso = IsoDep.get(tag) ?: return false
                if (iso.isConnected) {
                    try { iso.close() } catch (_: Exception) {}
                }
                iso.connect()
                iso.timeout = 3000
                val ev2 = Ntag424Ev2(iso)
                ev2.authenticateEV2First(keyNo, key)
                true
            } catch (_: Exception) {
                false
            } finally {
                try { iso?.close() } catch (_: Exception) {}
            }
        }

        val key0AppMatch = auth(0x00, key0App)
        val key0DefaultMatch = auth(0x00, key0Default)
        val sdmKey = try {
            val domain = "BEAMIO_SDM_V1".toByteArray(Charsets.UTF_8)
            val msg = domain + byteArrayOf(0x00) + uid
            Crypto.aesCmac(master, msg).copyOfRange(0, 16)
        } catch (_: Exception) {
            ByteArray(0)
        }
        val masterMatch = sdmKey.size == 16 && auth(0x02, sdmKey)
        return InitKeyProbeResult(key0AppMatch, key0DefaultMatch, masterMatch)
    }

    private fun inspectCardInitState(tag: Tag): InitInspectResult {
        val ndef = Ndef.get(tag) ?: return InitInspectResult(false, null)
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()
            if (msg == null || msg.records.isEmpty()) {
                return InitInspectResult(false, null)
            }
            val url = msg.records.firstNotNullOfOrNull { it.toUri()?.toString() }
            if (url.isNullOrBlank()) return InitInspectResult(false, null)
            if (!isLikelyInitUrl(url)) return InitInspectResult(false, null)
            InitInspectResult(true, url)
        } catch (_: Exception) {
            InitInspectResult(false, null)
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private data class ReadCheckResult(
        val status: String,
        val url: String?,
        val counter: Int? = null,
        val paramText: String? = null
    )

    private fun checkInitStatusByNdef(tag: Tag): ReadCheckResult {
        val ndef = Ndef.get(tag) ?: return ReadCheckResult("❌ 未按 init 初始化：标签不支持 NDEF", null)
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()

            if (msg == null || msg.records.isEmpty()) {
                return ReadCheckResult("❌ 未按 init 初始化：NDEF 为空", null)
            }

            val url = msg.records.firstNotNullOfOrNull { record ->
                record.toUri()?.toString()
            } ?: return ReadCheckResult("❌ 未按 init 初始化：未找到 URL Record", null)

            if (!isInitUrl(url)) {
                return ReadCheckResult("❌ 未按 init 初始化：URL 不符合规则", null)
            }

            val uri = Uri.parse(url)
            val uid = uri.getQueryParameter("uid").orEmpty()
            val e = uri.getQueryParameter("e").orEmpty()
            val c = uri.getQueryParameter("c").orEmpty()
            val m = uri.getQueryParameter("m").orEmpty()
            val counter = c.toIntOrNull(16)
            val paramText = "uid=$uid\ne=$e\nc=$c (counter=${counter ?: "n/a"})\nm=$m"
            val isAllZero = e.all { it == '0' } && c.all { it == '0' } && m.all { it == '0' }

            if (isAllZero) {
                ReadCheckResult("⚠️ 模板已写入（e/c/m 全0），SUN 动态未启用", url, counter, paramText)
            } else {
                ReadCheckResult("✅ 已完成 init 且检测到 SUN 动态数据", url, counter, paramText)
            }
        } catch (e: Exception) {
            ReadCheckResult("❌ 初始化状态检测失败：${e.message ?: "unknown error"}", null)
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private fun applyLastCounter() {
        val next = lastCounterInput.trim().toIntOrNull()
        if (next == null || next < 0) {
            readCheckText = "❌ lastCounter 必须是非负整数"
            return
        }
        lastCounter = next
        readCheckText = "✅ 已设置 lastCounter=$next"
    }

    private fun copyUrlToClipboard() {
        if (readUrlText.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("init-url", readUrlText))
        readCheckText = "✅ URL 已复制到剪贴板"
    }

    private fun isInitUrl(url: String): Boolean {
        if (!url.startsWith(SUN_BASE_URL)) return false
        val uri = Uri.parse(url)
        val uid = uri.getQueryParameter("uid") ?: return false
        val e = uri.getQueryParameter("e") ?: return false
        val c = uri.getQueryParameter("c") ?: return false
        val m = uri.getQueryParameter("m") ?: return false
        if (uid.isBlank()) return false
        return e.length == 64 &&
            c.length == 6 &&
            m.length == 16 &&
            e.matches(Regex("^[0-9a-fA-F]+$")) &&
            c.matches(Regex("^[0-9a-fA-F]+$")) &&
            m.matches(Regex("^[0-9a-fA-F]+$"))
    }

    private fun isLikelyInitUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        val e = uri.getQueryParameter("e") ?: return false
        val c = uri.getQueryParameter("c") ?: return false
        val m = uri.getQueryParameter("m") ?: return false
        return e.length == 64 &&
            c.length == 6 &&
            m.length == 16 &&
            e.matches(Regex("^[0-9a-fA-F]+$")) &&
            c.matches(Regex("^[0-9a-fA-F]+$")) &&
            m.matches(Regex("^[0-9a-fA-F]+$"))
    }

    private fun statusHexFromError(e: Exception): String {
        val msg = e.message.orEmpty()
        val m = Regex("0x[0-9A-Fa-f]{4}").find(msg)?.value
        return m ?: "unknown"
    }

    private fun handleTemplateOnlyTag(tag: Tag) {
        appendInitStep("⏳ [T 1/2] 检测到卡，开始仅写 URL 模板...")
        try {
            val uidBytes = tag.id ?: run {
                initFail("[T 1/2] 读取 UID", "无法读取卡 UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[T 2/2] NDEF 写入", "标签只读")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[T 2/2] NDEF 写入", "标签不支持 NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            appendInitStep("✅ [T 2/2] 仅写模板完成（未执行 EV2/SDM）")
            readUrlText = initUrl
            readParamText = "template-only: e/c/m 占位，未启用卡侧动态 SDM"
        } catch (e: Exception) {
            initFail("[T X] 模板写入", e.message ?: "unknown error")
        } finally {
            initTemplateArmed = false
            initArmed = false
        }
    }

    private fun handleInitTag(tag: Tag, allowKey2Fallback: Boolean, useDefaultKey0: Boolean) {
        appendInitStep("⏳ [1/11] 已检测到 NFC 标签")
        try {
            if (HARD_CODED_KEY0.length != 32 || HARD_CODED_MASTER_KEY.length != 32) {
                initFail("[2/11] 参数校验", "Key0 或 MasterKey 长度不是 16-byte HEX")
                return
            }
            val appKey0 = hexToBytes(HARD_CODED_KEY0)
            val key0 = if (useDefaultKey0) ByteArray(16) else appKey0
            val masterKey = hexToBytes(HARD_CODED_MASTER_KEY)
            if (key0 == null || appKey0 == null || masterKey == null || key0.size != 16 || appKey0.size != 16 || masterKey.size != 16) {
                initFail("[2/11] 参数校验", "Key0/MasterKey 不是有效的 16-byte HEX")
                return
            }
            appendInitStep("✅ [2/11] 参数校验通过（${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"} / MasterKey）")

            val uidBytes = tag.id ?: run {
                initFail("[3/11] 读取 UID", "无法读取卡 UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            appendInitStep("✅ [3/11] UID=$publicUid")

            val isoDep = IsoDep.get(tag) ?: run {
                initFail("[4/11] EV2 准备", "该卡不支持 IsoDep，无法执行 EV2/SDM")
                return
            }
            isoDep.connect()
            isoDep.timeout = 5000
            appendInitStep("✅ [4/11] IsoDep 已连接")

            val ev2 = Ntag424Ev2(isoDep)
            val sdmKey = ev2.deriveSdmKey(masterKey, uidBytes)
            appendInitStep("✅ [5/11] 通过 masterKey+UID 派生 SDM Key")

            var session: Ev2Session? = null
            var authenticatedWithKey2Fallback = false
            try {
                appendInitStep("⏳ [6/11] 尝试 EV2First(${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"}) ...")
                session = ev2.authenticateEV2First(0x00, key0)
                appendInitStep("✅ [6/11] EV2First(Key0) 认证成功")
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                appendInitStep("⚠️ [6/11] EV2First(Key0) 失败，status=${statusHexFromError(e)}")
                if (msg.contains("0x9140") && allowKey2Fallback) {
                    appendInitStep("⏳ [6/11] 尝试 EV2First(Key2) 回退认证...")
                    try {
                        session = ev2.authenticateEV2First(0x02, sdmKey)
                        appendInitStep("✅ [6/11] EV2First(Key2) 回退认证成功（该卡可能已是已初始化卡）")
                        authenticatedWithKey2Fallback = true
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(Key2)",
                            "回退认证失败，status=${statusHexFromError(e2)}"
                        )
                        return
                    }
                } else if (msg.contains("0x9140") && useDefaultKey0) {
                    appendInitStep("⏳ [6/11] 尝试 EV2First(AppKey0) 认证...")
                    try {
                        session = ev2.authenticateEV2First(0x00, appKey0)
                        appendInitStep("✅ [6/11] EV2First(AppKey0) 认证成功")
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(AppKey0)",
                            "认证失败，status=${statusHexFromError(e2)}；0x00/AppKey0 均不匹配"
                        )
                        return
                    }
                } else if (msg.contains("0x9140")) {
                    initFail("[6/11] EV2First(Key0)", "认证失败 0x9140。当前卡的 Key0 不匹配（0x00/AppKey0）。")
                    return
                } else {
                    throw e
                }
            }
            val sessionSafe = session ?: run {
                initFail("[6/11] EV2First", "未建立有效会话")
                return
            }

            if (!authenticatedWithKey2Fallback) {
                ev2.changeKey(sessionSafe, changingKeyNo = 0x02, newKey = sdmKey)
                appendInitStep("✅ [7/11] ChangeKey(0x02) 写入派生 SDM Key")
            } else {
                appendInitStep("ℹ️ [7/11] Key2 回退模式：跳过 ChangeKey(0x02)")
            }

            val ndefFileNo = ev2.autoDetectNdefFileNo()
            appendInitStep("✅ [8/11] autoDetect NDEF fileNo=0x${String.format("%02X", ndefFileNo)}")

            if (!authenticatedWithKey2Fallback) {
                val patchedSettings = ev2.patchSdmFileSettingsFromLiveNdef(
                    fileNo = ndefFileNo,
                    expectedEncHexLen = 64,
                    expectedCtrHexLen = 6,
                    expectedMacHexLen = 16,
                    readBytes = 300
                )
                ev2.changeFileSettings(sessionSafe, ndefFileNo, patchedSettings)
                appendInitStep("✅ [9/11] 已完成 SDM offsets + ChangeFileSettings")
            } else {
                appendInitStep("ℹ️ [9/11] Key2 回退模式：跳过 ChangeFileSettings（避免权限失败）")
            }
            try {
                isoDep.close()
            } catch (_: Exception) {
                // ignore close error
            }

            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            if (!initUrl.startsWith("https://")) {
                initFail("[10/11] URL 模板", "sunBaseUrl 非 HTTPS")
                return
            }
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[10/11] NDEF 写入", "标签只读")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[10/11] NDEF 写入", "标签不支持 NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            uidText = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "读取失败"
            appendInitStep("✅ [10/11] Init 完成：EV2/SDM 已配置，URL 模板已写入")
            appendInitStep("⏳ [11/11] 开始 init 后检验（读取并校验 URL）")
            val verify = checkInitStatusByNdef(tag)
            if (verify.url.isNullOrBlank() || verify.status.startsWith("❌")) {
                initFail("[11/11] Init 检验", verify.status)
                return
            }
            readUrlText = verify.url
            readParamText = verify.paramText ?: ""
            appendInitStep("✅ [11/11] Init 检验通过：${verify.status}")
            appendInitStep("key0=${HARD_CODED_KEY0.take(8)}... masterKey=${HARD_CODED_MASTER_KEY.take(8)}...")
            appendInitStep("publicUID=$publicUid")
        } catch (e: Exception) {
            initFail("[X] 未捕获异常", e.message ?: "unknown error")
        } finally {
            initArmed = false
        }
    }

    private fun buildSunTemplateUrl(base: String, publicUid: String, lastCounter: Int): String {
        val e = "0".repeat(64)
        val c = String.format("%06X", lastCounter.coerceAtLeast(0))
        val m = "0".repeat(16)
        return if (base.contains("?")) {
            if (base.endsWith("?") || base.endsWith("&")) {
                "${base}uid=$publicUid&e=$e&c=$c&m=$m"
            } else {
                "${base}&uid=$publicUid&e=$e&c=$c&m=$m"
            }
        } else {
            "$base?uid=$publicUid&e=$e&c=$c&m=$m"
        }
    }

    private fun hexToBytes(hex: String): ByteArray? {
        val s = hex.trim()
        if (s.length % 2 != 0) return null
        return try {
            ByteArray(s.length / 2) { i ->
                s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
internal fun TopupScreen(
    amount: String,
    uid: String,
    status: TopupStatus,
    txHash: String,
    error: String,
    postBalance: String?,
    cardCurrency: String?,
    address: String?,
    cardBackground: String?,
    cardImage: String?,
    tierDescription: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        when (status) {
            is TopupStatus.Waiting -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack) { Text("返回") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "请贴卡读取 UID...", fontSize = 16.sp)
                    }
                }
            }
            is TopupStatus.Loading -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = "签名并执行 Topup...", fontSize = 14.sp)
                        if (uid.isNotBlank()) {
                            Text(text = "UID: $uid", fontSize = 12.sp)
                        }
                    }
                }
            }
            is TopupStatus.Success -> TopupSuccessContent(
                modifier = Modifier.fillMaxSize(),
                amount = amount,
                txHash = txHash,
                postBalance = postBalance,
                cardCurrency = cardCurrency,
                address = address,
                cardBackground = cardBackground,
                cardImage = cardImage,
                tierDescription = tierDescription,
                onDone = onBack
            )
            is TopupStatus.Error -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "❌ $error", fontSize = 14.sp)
                        if (uid.isNotBlank()) {
                            Text(text = "UID: $uid", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopupSuccessContent(
    amount: String,
    txHash: String,
    postBalance: String?,
    cardCurrency: String?,
    address: String?,
    cardBackground: String?,
    cardImage: String?,
    tierDescription: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val postBalanceNum = postBalance?.toDoubleOrNull()
    val currency = cardCurrency ?: "CAD"
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = address?.let { if (it.length > 10) "${it.take(6)}...${it.takeLast(4)}" else it } ?: "—"
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash
    val cardBgColor = parseHexColor(cardBackground) ?: Color.Black

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFf5f5f7))
    ) {
        // Header: icon + Top-Up Complete + amount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(48.dp), tint = Color(0xFF34C759))
            }
            Text(
                "Top-Up Complete",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                Text(
                    "$${"%.2f".format(amountNum)}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black
                )
            }
        }

        // Voucher Balance card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = (-40).dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cardImage != null && cardImage.isNotBlank()) {
                                AsyncImage(
                                    model = cardImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        Column {
                            Text(
                                "Card Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF86868b)
                            )
                            Text(
                                if (postBalanceNum != null) "$${"%.2f".format(postBalanceNum)} $currency"
                                else "—",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                                )
                                if (tierDescription != null && tierDescription.isNotBlank()) {
                                    Text(
                                        tierDescription,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF1c1c1e), RoundedCornerShape(20.dp))
                                .clickable { /* Print mock */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Print, null, Modifier.size(24.dp), tint = Color.White)
                                Text("Print", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Receipt details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", fontSize = 15.sp, color = Color(0xFF86868b))
                        Text("$dateString, $timeString", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Account ID", fontSize = 15.sp, color = Color(0xFF86868b))
                        Text(shortAddr, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Security", fontSize = 15.sp, color = Color(0xFF86868b))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, null, Modifier.size(14.dp), tint = Color(0xFF34C759))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NTAG 424 DNA", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF34C759))
                        }
                    }
                    if (txHash.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TX Hash", fontSize = 15.sp, color = Color(0xFF86868b))
                            Text(shortTxHash, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
                        }
                    }
                }
            }
        }

        // Done button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 48.dp)
        ) {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatWithThousands(s: String?): String {
    if (s.isNullOrBlank()) return "0"
    val n = s.toDoubleOrNull() ?: return s
    return java.util.Locale.US.run { "%,.2f".format(n) }
}

/** 解析 metadata 中的 background_color（#RRGGBB 或 RRGGBB）为 Compose Color，解析失败返回 null */
private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val s = hex.trim().removePrefix("#")
    if (s.length != 6 && s.length != 8) {
        Log.d("parseHexColor", "skip: length not 6 or 8 (was ${s.length}) hex=$hex")
        return null
    }
    return try {
        val value = s.toLong(16).toInt()
        val rgb = if (s.length == 8) value else (0xFF shl 24) or (value and 0xFFFFFF)
        Color(rgb)
    } catch (e: Exception) {
        Log.d("parseHexColor", "parse failed: hex=$hex", e)
        null
    }
}

@Composable
internal fun ReadScreen(
    uid: String,
    status: ReadStatus,
    assets: UIDAssets?,
    error: String,
    onBack: () -> Unit,
    onTopupClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val voucherBalance = assets?.points?.toDoubleOrNull() ?: 0.0
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = assets?.address?.let { if (it.length > 10) "${it.take(6)}...${it.takeLast(4)}" else it } ?: "—"

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7))
    ) {
        when (status) {
            is ReadStatus.Waiting -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Button(onClick = onBack) { Text("返回") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "请贴卡读取 UID...", fontSize = 16.sp)
                    }
                }
            }
            is ReadStatus.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator()
                        Text(text = "查询中...", fontSize = 14.sp)
                        if (uid.isNotBlank()) Text(text = "UID: $uid", fontSize = 12.sp)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            is ReadStatus.Success -> {
                val cardList = assets?.cards?.takeIf { it.isNotEmpty() }
                    ?: listOfNotNull(assets?.cardAddress?.let { addr ->
                        CardItem(
                            cardAddress = addr,
                            cardName = "CCSA CARD",
                            cardType = "ccsa",
                            points = assets.points ?: "0",
                            points6 = assets.points6 ?: "0",
                            cardCurrency = assets.cardCurrency ?: "CAD",
                            nfts = assets.nfts ?: emptyList(),
                            cardBackground = null,
                            cardImage = null,
                            tierName = null,
                            tierDescription = null
                        )
                    }).takeIf { it.isNotEmpty() }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White, CircleShape)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(40.dp), tint = Color(0xFF34C759))
                        }
                        Text(
                            "Balance Checked",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        // USDC on Base (like web EOA layer)
                        val usdcBal = assets?.usdcBalance?.toDoubleOrNull() ?: 0.0
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1c1c1e))
                        ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                    .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                Text("USDC on Base", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                                        Text(
                                    "%.2f USDC".format(usdcBal),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        if (cardList != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                "${cardList.size} Passes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            cardList.forEachIndexed { index, card ->
                                val overlap = 24.dp
                                val balanceNum = card.points.toDoubleOrNull() ?: 0.0
                                val memberNo = card.nfts
                                    .filter { it.tokenId.toLongOrNull()?.let { id -> id > 0 } == true }
                                    .maxByOrNull { it.tokenId.toLongOrNull() ?: 0L }
                                    ?.tokenId
                                    ?.let { "M-%s".format(it.padStart(6, '0')) }
                                    ?: ""
                                val isCcsa = card.cardType == "ccsa"
                                val isInfra = card.cardType == "infrastructure"
                                val bgColor = parseHexColor(card.cardBackground) ?: when {
                                    isCcsa -> Color(0xFF6366F1)
                                    isInfra -> Color(0xFF0ea5e9)
                                    else -> Color(0xFF6366F1)
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (index == 0) 0.dp else overlap),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (card.cardImage != null && card.cardImage!!.isNotBlank()) {
                                                AsyncImage(
                                                    model = card.cardImage,
                                                    contentDescription = card.cardName,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                            } else {
                                    Box(
                                        modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                                    Icon(Icons.Filled.Favorite, null, Modifier.size(24.dp), tint = Color.White)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    card.cardName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.95f)
                                                )
                                                if (card.cardType != "infrastructure") {
                                                    Text(
                                                        card.cardType.replaceFirstChar { it.uppercase() },
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.75f)
                                                    )
                                                }
                                                if (card.tierName != null) {
                                                    Text(
                                                        card.tierName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                                if (card.tierDescription != null) {
                                                    Text(
                                                        card.tierDescription,
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                                if (memberNo.isNotEmpty()) {
                                                    Text(
                                                        "NFT $memberNo",
                                                        fontSize = 10.sp,
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "%.2f %s".format(balanceNum, card.cardCurrency),
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Account summary
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Account", fontSize = 15.sp, color = Color(0xFF86868b))
                                    Text(shortAddr, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Security", fontSize = 15.sp, color = Color(0xFF86868b))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Shield, null, Modifier.size(14.dp), tint = Color(0xFF34C759))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("NTAG 424 DNA", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF34C759))
                                    }
                                }
                            }
                        }
                    }

                    // Bottom buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .padding(bottom = 48.dp)
                    ) {
                        Button(
                            onClick = onTopupClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1562f0), contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.Add, null, Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Top-Up Card Now", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                        ) {
                            Text("Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            is ReadStatus.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "❌ $error", fontSize = 14.sp)
                        if (uid.isNotBlank()) Text(text = "UID: $uid", fontSize = 12.sp)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessContent(
    amount: String,
    payee: String,
    txHash: String,
    subtotal: String?,
    tip: String?,
    postBalance: String?,
    cardCurrency: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val subtotalNum = subtotal?.toDoubleOrNull()
    val tipNum = tip?.toDoubleOrNull()
    val postBalanceNum = postBalance?.toDoubleOrNull()
    val currency = cardCurrency ?: "CAD"
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = if (payee.length > 10) "${payee.take(6)}...${payee.takeLast(4)}" else payee
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFf5f5f7))
    ) {
        // Header: icon + Payment Approved + amount
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(48.dp), tint = Color(0xFF34C759))
            }
            Text(
                "Payment Approved",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                Text(
                    "$${"%.2f".format(amountNum)}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.Black
                )
            }
        }

        // Voucher Balance card (UID balance - charge - tip)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = (-40).dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Card Balance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF86868b)
                            )
                            Text(
                                if (postBalanceNum != null) "$${"%.2f".format(postBalanceNum)} $currency"
                                else "—",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF1c1c1e), RoundedCornerShape(20.dp))
                                .clickable { /* Print mock */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Print, null, Modifier.size(24.dp), tint = Color.White)
                                Text("Print", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Smart Routing: Voucher Deduction (charge amount)
            if (subtotalNum != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Smart Routing Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Voucher Deduction", fontSize = 15.sp, color = Color(0xFF86868b))
                            Text("$${"%.2f".format(subtotalNum)} $currency", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                        if (tipNum != null && tipNum > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tip", fontSize = 15.sp, color = Color(0xFF86868b))
                                Text("$${"%.2f".format(tipNum)} $currency", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Receipt details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", fontSize = 15.sp, color = Color(0xFF86868b))
                        Text("$dateString, $timeString", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Account ID", fontSize = 15.sp, color = Color(0xFF86868b))
                        Text(shortAddr, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Security", fontSize = 15.sp, color = Color(0xFF86868b))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, null, Modifier.size(14.dp), tint = Color(0xFF34C759))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NTAG 424 DNA", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF34C759))
                        }
                    }
                    if (txHash.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TX Hash", fontSize = 15.sp, color = Color(0xFF86868b))
                            Text(shortTxHash, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
                        }
                    }
                }
            }

            // Digital Receipt hint
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1562f0).copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color(0xFF1562f0).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp), tint = Color(0xFF1562f0))
                    Column {
                        Text(
                            "Smart Receipt Generated",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            "Transaction secured on CoNET. Users with the Beamio App can view their history asynchronously.",
                            fontSize = 13.sp,
                            color = Color(0xFF86868b),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Done button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 48.dp)
        ) {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Done", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun PaymentScreen(
    amount: String,
    payee: String,
    uid: String,
    status: PaymentStatus,
    txHash: String,
    error: String,
    routingSteps: List<RoutingStep>,
    subtotal: String?,
    tip: String?,
    postBalance: String?,
    cardCurrency: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        when (status) {
            is PaymentStatus.Waiting -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack) { Text("返回") }
                    Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "请贴卡读取 UID...", fontSize = 16.sp)
                }
                }
            }
            is PaymentStatus.Routing, is PaymentStatus.Submitting -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                    Text(
                        text = "Smart Routing Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                    routingSteps
                        .filter { it.status != StepStatus.pending || it.id in listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute") }
                        .filter { it.id in listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute") || it.status != StepStatus.pending }
                        .forEach { step ->
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (step.status) {
                                        StepStatus.loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        StepStatus.success -> Text("✓", fontSize = 20.sp, color = Color(0xFF22c55e))
                                        StepStatus.error -> Text("✗", fontSize = 20.sp, color = Color(0xFFef4444))
                                        StepStatus.pending -> Text("•", fontSize = 14.sp, color = Color(0xFF94a3b8))
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step.label,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (step.detail.isNotBlank()) {
                                        Text(
                                            text = step.detail,
                                            fontSize = 13.sp,
                                            color = Color(0xFF64748b)
                                        )
                                    }
                                }
                            }
                        }
                }
                }
            }
            is PaymentStatus.Success -> PaymentSuccessContent(
                modifier = Modifier.fillMaxSize(),
                amount = amount,
                payee = payee,
                txHash = txHash,
                subtotal = subtotal,
                tip = tip,
                postBalance = postBalance,
                cardCurrency = cardCurrency,
                onDone = onBack
            )
            is PaymentStatus.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Button(onClick = onBack) { Text("返回") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 16.dp)
                    ) {
                    if (routingSteps.isNotEmpty()) {
                        Text(
                            text = "Smart Routing Analysis",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        routingSteps
                            .filter { it.id in listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute") || it.status != StepStatus.pending }
                            .forEach { step ->
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (step.status) {
                                            StepStatus.loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                            StepStatus.success -> Text("✓", fontSize = 20.sp, color = Color(0xFF22c55e))
                                            StepStatus.error -> Text("✗", fontSize = 20.sp, color = Color(0xFFef4444))
                                            StepStatus.pending -> Text("•", fontSize = 14.sp, color = Color(0xFF94a3b8))
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = step.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        if (step.detail.isNotBlank()) {
                                            Text(text = step.detail, fontSize = 13.sp, color = Color(0xFF64748b))
                                        }
                                    }
                                }
                            }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(text = "❌ $error", fontSize = 14.sp)
                    if (uid.isNotBlank()) {
                        Text(text = "UID: $uid", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    }
                }
            }
        }
    }
}

@Composable
fun NdefScreen(
    uidText: String,
    readUrlText: String,
    readParamText: String,
    walletAddress: String?,
    onCopyWalletClick: () -> Unit,
    onReadClick: () -> Unit,
    onTopupClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onCopyUrlClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var walletCopied by mutableStateOf(false)
    LaunchedEffect(walletCopied) {
        if (walletCopied) {
            delay(2000)
            walletCopied = false
        }
    }
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        // Header: Store icon + title (nativeApp style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .padding(top = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Color.Black, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Store,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
                Column {
                    Text("Beamio Terminal", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text("Active Terminal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                }
            }
        }

        // Content area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(dateString, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF86868b), modifier = Modifier.padding(bottom = 12.dp))

            // Summary card (Charges | Top-Ups) - nativeApp style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(Color(0xFF1562f0).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, null, Modifier.size(12.dp), tint = Color(0xFF1562f0))
                                }
                                Text("Charges", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                            }
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$845", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(".00", fontSize = 18.sp, color = Color(0xFF86868b), modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Text("CAD", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(64.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(Color(0xFF34C759).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, null, Modifier.size(12.dp), tint = Color(0xFF34C759))
                                }
                                Text("Top-Ups", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                            }
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$400", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(".00", fontSize = 18.sp, color = Color(0xFF86868b), modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Text("CAD", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    // Wallet address pill - bottom right
                    if (walletAddress != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        onCopyWalletClick()
                                        walletCopied = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val shortAddr = if (walletAddress.length >= 10) {
                                    walletAddress.take(5) + "..." + walletAddress.takeLast(5)
                                } else walletAddress
                                Text(shortAddr, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Icon(
                                    if (walletCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (walletCopied) Color(0xFF34C759) else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons (nativeApp style): Charge, Top-Up, Check Balance
            Card(
                onClick = onPaymentClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color(0xFF1562f0).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowDownward, null, Modifier.size(24.dp), tint = Color(0xFF1562f0))
                        }
                        Column {
                            Text("Charge", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Accept NFC or Scan QR", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = Color(0xFFc7c7cc))
                }
            }
            Card(
                onClick = onTopupClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color(0xFF34C759).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowUpward, null, Modifier.size(24.dp), tint = Color(0xFF34C759))
                        }
                        Column {
                            Text("Top-Up", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Load customer balance", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = Color(0xFFc7c7cc))
                }
            }
            Card(
                onClick = onReadClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color(0xFFf4f4f5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Search, null, Modifier.size(24.dp), tint = Color.Black)
                        }
                        Column {
                            Text("Check Balance", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Read card via NFC", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = Color(0xFFc7c7cc))
                }
            }

            if (readUrlText.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = readUrlText, modifier = Modifier.weight(1f), fontSize = 11.sp)
                    IconButton(onClick = onCopyUrlClick) { Text("📋") }
                }
            }
            if (readParamText.isNotBlank()) Text(readParamText, fontSize = 11.sp, color = Color(0xFF64748b), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ScanMethodSelectionScreen(
    scanMethod: String,
    scanWaitingForNfc: Boolean = false,
    nfcFetchingInfo: Boolean = false,
    nfcFetchError: String = "",
    onScanMethodChange: (String) -> Unit,
    pendingAction: String,
    totalAmount: String,
    qrUrl: String = "",
    onQrUrlChange: (String) -> Unit = {},
    onProceed: () -> Unit,
    onProceedNfcStay: (() -> Unit)? = null,
    onProceedWithQr: (() -> Unit)? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color.Black),
        verticalArrangement = Arrangement.Top
    ) {
        // Top Toggle: NFC vs QR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { if (!scanWaitingForNfc && !nfcFetchingInfo) onScanMethodChange("nfc") },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMethod == "nfc") Color.White else Color.Transparent,
                            contentColor = if (scanMethod == "nfc") Color.Black else Color.White.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Nfc, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tap Card", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { if (!scanWaitingForNfc && !nfcFetchingInfo) onScanMethodChange("qr") },
                        modifier = Modifier.height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scanMethod == "qr") Color.White else Color.Transparent,
                            contentColor = if (scanMethod == "qr") Color.Black else Color.White.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.QrCode2, null, Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Scanning area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (scanMethod == "nfc") {
                if (scanWaitingForNfc) {
                    // 等待 UID：蓝线上下往复移动
                    val lineOffsetY = remember { Animatable(0f) }
                    LaunchedEffect(scanWaitingForNfc) {
                        if (scanWaitingForNfc) {
                            while (isActive) {
                                lineOffsetY.animateTo(1f, animationSpec = tween<Float>(2000))
                                lineOffsetY.animateTo(0f, animationSpec = tween<Float>(2000))
                            }
                        } else {
                            lineOffsetY.snapTo(0f)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFF1562f0),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        ) {
                            val y = lineOffsetY.value * (size.height - 2)
                            drawLine(
                                color = Color(0xFF1562f0),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 0.3.dp.toPx()
                            )
                        }
                    }
                    Text(
                        "Verifying...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Hold the customer's NTAG 424 DNA card near the NFC sensor.",
                        fontSize = 15.sp,
                        color = Color(0xFF86868b),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (nfcFetchingInfo) {
                    // 已获 UID，拉取信息中：无蓝线，仅 loading
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFF1562f0),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    Text(
                        "Loading...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Fetching card info.",
                        fontSize = 15.sp,
                        color = Color(0xFF86868b),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (nfcFetchError.isNotEmpty()) {
                    // 拉取失败：在 280.dp 框内显示绿色错误
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    nfcFetchError,
                                    fontSize = 14.sp,
                                    color = Color(0xFF22c55e),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Text(
                        "Error",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Tap Continue to retry.",
                        fontSize = 15.sp,
                        color = Color(0xFF86868b),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Nfc, null, Modifier.size(40.dp), tint = Color.White)
                    }
                    Text(
                        "Ready to Scan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                    Text(
                        "Hold the customer's NTAG 424 DNA card near the NFC sensor.",
                        fontSize = 15.sp,
                        color = Color(0xFF86868b),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.QrCode2,
                        null,
                        Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.2f)
                    )
                }
                Text(
                    "Scan Dynamic QR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 32.dp)
                )
                Text(
                    "Position the customer's Beamio App payment code in the frame.",
                    fontSize = 15.sp,
                    color = Color(0xFF86868b),
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = qrUrl,
                    onValueChange = onQrUrlChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    placeholder = { Text("或粘贴 beamio.app 链接", color = Color(0xFF86868b), fontSize = 14.sp) }
                )
            }
        }

        // Bottom: Total amount + Proceed + Cancel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            if (pendingAction != "read" && totalAmount.isNotBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (pendingAction == "payment") "Total Amount" else "Top-Up Amount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF86868b)
                    )
                    Text(
                        "$${"%.2f".format(totalAmount.toDoubleOrNull() ?: 0.0)}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
            Button(
                onClick = {
                    if (scanMethod == "qr" && onProceedWithQr != null) {
                        onProceedWithQr()
                    } else if (scanMethod == "nfc" && onProceedNfcStay != null) {
                        onProceedNfcStay()
                    } else {
                        onProceed()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
                enabled = !scanWaitingForNfc && !nfcFetchingInfo
            ) {
                Text(
                    when {
                        scanWaitingForNfc -> "Waiting for NFC..."
                        nfcFetchingInfo -> "Loading..."
                        scanMethod == "qr" -> "Scan QR / Paste URL"
                        else -> "Continue"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
            }
        }
    }
}

@Composable
fun TipSelectionScreen(
    subtotal: String,
    selectedTipRate: Double,
    onTipRateSelect: (Double) -> Unit,
    onBack: () -> Unit,
    onConfirmPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numAmount = subtotal.toDoubleOrNull() ?: 0.0
    val tipValue = numAmount * selectedTipRate
    val totalAmount = numAmount + tipValue
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7)),
        verticalArrangement = Arrangement.Top
    ) {
        // Header: Back | Add Tip | spacer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(top = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onBack() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, null, Modifier.size(24.dp), tint = Color(0xFF1562f0))
                Text("Back", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
            }
            Text("Add Tip", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Box(modifier = Modifier.width(64.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Subtotal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Favorite, null, Modifier.size(14.dp), tint = Color(0xFF1562f0))
                    Text("Present to Customer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF86868b))
                }
                Text("Subtotal", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b), modifier = Modifier.padding(top = 16.dp))
                Text("$${"%.2f".format(numAmount)}", fontSize = 48.sp, fontWeight = FontWeight.Light, color = Color.Black)
            }

            // Tip options: 2x2 grid - 15%, 18%, 20%, No Tip
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(
                    listOf(0.15 to "15%", 0.18 to "18%"),
                    listOf(0.20 to "20%", 0.0 to "No Tip")
                ).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { (rate, label) ->
                            val isSelected = selectedTipRate == rate
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTipRateSelect(rate) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF1562f0).copy(alpha = 0.1f) else Color.White
                                ),
                                border = if (isSelected) BorderStroke(2.dp, Color(0xFF1562f0)) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        label,
                                        fontSize = if (rate == 0.0) 22.sp else 26.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF1562f0) else Color.Black
                                    )
                                    Text(
                                        if (rate == 0.0) "+$0.00" else "+$${"%.2f".format(numAmount * rate)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF1562f0).copy(alpha = 0.8f) else Color(0xFF86868b),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Total + Confirm & Pay
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 48.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Total to Pay", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        Text("$${"%.2f".format(totalAmount)}", fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Button(
                        onClick = onConfirmPay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Confirm & Pay", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ChargeAmountScreen(
    mode: String, // "charge" | "topup"
    amount: String,
    onPadClick: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val canContinue = amount != "0" && amount != "0."
    val title = if (mode == "topup") "Top-Up Amount" else "Charge Amount"
    val continueBtnColor = if (mode == "topup") Color(0xFF1562f0) else Color.Black
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7)),
        verticalArrangement = Arrangement.Top
    ) {
        // Header: Back | Charge Amount | spacer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(top = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable { onBack() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, null, Modifier.size(24.dp), tint = Color(0xFF1562f0))
                Text("Back", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
            }
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Box(modifier = Modifier.width(64.dp))
        }

        // Big amount display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("$", fontSize = 36.sp, fontWeight = FontWeight.Light, color = Color(0xFF86868b))
                Text(amount, fontSize = 72.sp, fontWeight = FontWeight.Light, color = Color.Black)
            }
        }

        // Keypad + Continue
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(".", "0", "back")
                    ).forEach { rowKeys ->
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            rowKeys.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable {
                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                                            onPadClick(key)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "back") {
                                        Icon(Icons.Filled.ArrowBack, null, Modifier.size(24.dp), tint = Color.Black)
                                    } else {
                                        Text(key, fontSize = 28.sp, fontWeight = FontWeight.Normal, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canContinue) continueBtnColor else Color(0xFFe5e5ea),
                    disabledContainerColor = Color(0xFFe5e5ea),
                    contentColor = if (canContinue) Color.White else Color(0xFF86868b),
                    disabledContentColor = Color(0xFF86868b)
                )
            ) {
                Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun InitFlowScreen(
    initText: String,
    showReconfirm: Boolean,
    allowContinue: Boolean,
    reconfirmText: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Init 模式")
        Text(
            text = initText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        if (showReconfirm) {
            Text(
                text = reconfirmText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (allowContinue) {
                    Button(onClick = onContinue) {
                        Text("继续")
                    }
                }
                Button(onClick = onCancel) {
                    Text("cancel")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onCancel) {
                    Text("cancel")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidNTAGTheme {
        NdefScreen(
            uidText = "04A1B2C3D4",
            readUrlText = "https://api.beamio.app/api/sun?e=...&c=...&m=...",
            readParamText = "e=...\nc=000001 (counter=1)\nm=...",
            walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
            onCopyWalletClick = {},
            onReadClick = {},
            onTopupClick = {},
            onPaymentClick = {},
            onCopyUrlClick = {}
        )
    }
}