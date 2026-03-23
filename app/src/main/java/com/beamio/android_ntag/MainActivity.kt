package com.beamio.android_ntag

import android.Manifest
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.view.View
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.Label
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import java.util.concurrent.CountDownLatch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.beamio.android_ntag.ui.theme.AndroidNTAGTheme
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

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
    val tierDescription: String? = null,
    /** Server: NFT with max on-chain `tiers[i].minUsdc6` on this card (matches `_findBestValidMembership`). */
    val primaryMemberTokenId: String? = null
)
internal data class UIDAssets(
    val ok: Boolean,
    val address: String? = null,
    val aaAddress: String? = null,
    /** NFC 流程下服务端返回的 UID */
    val uid: String? = null,
    /** NFC 流程下服务端返回的 TagID（hex） */
    val tagIdHex: String? = null,
    /** NFC 流程下服务端返回的 counter 十六进制 */
    val counterHex: String? = null,
    /** NFC 流程下服务端返回的 counter 十进制 */
    val counter: Int? = null,
    val cardAddress: String? = null,
    val points: String? = null,
    val points6: String? = null,
    val usdcBalance: String? = null,
    val cardCurrency: String? = null,
    val nfts: List<NftItem>? = null,
    val cards: List<CardItem>? = null,
    val unitPriceUSDC6: String? = null,
    /** AA Factory 配置的 beamioUserCard，maxAmount>0 时 ERC1155 必须用此地址 */
    val beamioUserCard: String? = null,
    val error: String? = null
)
internal data class NftItem(
    val tokenId: String,
    val attribute: String,
    val tier: String,
    val expiry: String,
    val isExpired: Boolean
)

/** getTerminalProfile API 返回的 profile 结构 */
data class TerminalProfile(
    val accountName: String?,
    val first_name: String?,
    val last_name: String?,
    val image: String?,
    val address: String?
)

private data class MetadataTier(
    val minUsdc6: Long,
    val name: String?,
    val description: String?,
    val image: String?,
    val backgroundColor: String?
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
    object Refreshing : PaymentStatus() // unused: post-tx balance fetch uses refreshBalance routing step + Submitting
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

private val ROUTING_STEP_IDS = listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute", "sendTx", "waitTx", "refreshBalance")
private fun createRoutingSteps() = listOf(
    RoutingStep("detectingUser", "Detecting User", "", StepStatus.pending),
    RoutingStep("membership", "Checking Membership", "", StepStatus.pending),
    RoutingStep("analyzingAssets", "Analyzing Assets", "", StepStatus.pending),
    RoutingStep("optimizingRoute", "Optimizing Route", "", StepStatus.pending),
    RoutingStep("sendTx", "Sending transaction", "", StepStatus.pending),
    RoutingStep("waitTx", "Waiting for transaction", "", StepStatus.pending),
    RoutingStep("refreshBalance", "Refreshing balance", "", StepStatus.pending)
)
private fun updateStep(steps: List<RoutingStep>, id: String, status: StepStatus, detail: String = "") =
    steps.map { if (it.id == id) it.copy(status = status, detail = detail) else it }

/** Smart Routing 在 280dp 卡内最多显示条数：新步骤从底部增加，超出则顶部丢弃 */
private const val PAYMENT_ROUTING_MONITOR_MAX_VISIBLE_STEPS = 6

internal fun filterPaymentRoutingStepsForDisplay(steps: List<RoutingStep>): List<RoutingStep> =
    steps
        .filter { it.status != StepStatus.pending || it.id in listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute") }
        .filter { it.id in listOf("detectingUser", "membership", "analyzingAssets", "optimizingRoute") || it.status != StepStatus.pending }

/**
 * Charge total in request/card currency (no USDC conversion here).
 * total = requestAmount + (taxPercent/100)*requestAmount - (discountPercent/100)*requestAmount + tipAmount
 *
 * [tipAmount] is the absolute tip in the same currency; tip is computed on **full request** (pre-discount):
 * `requestAmount * tipRate` — use [chargeTipFromRequestAndBps].
 */
private fun chargeTotalInCurrency(
    requestAmount: Double,
    taxPercent: Double,
    tierDiscountPercent: Int?,
    tipAmount: Double
): Double {
    val tax = requestAmount * (taxPercent / 100.0)
    val disc = requestAmount * ((tierDiscountPercent ?: 0).toDouble() / 100.0)
    return requestAmount + tax - disc + tipAmount
}

/** Tip on full request (before tier discount): tip = requestAmount × tipRate (0..1). */
private fun chargeTipFromRequestAndRate(requestAmount: Double, tipRateFraction: Double): Double =
    requestAmount * tipRateFraction.coerceIn(0.0, 1.0)

/** Tip on full request: tip = requestAmount × (tipRateBps / 10000). */
private fun chargeTipFromRequestAndBps(requestAmount: Double, tipRateBps: Int): Double =
    requestAmount * (tipRateBps.coerceIn(0, 10000) / 10000.0)

class MainActivity : ComponentActivity() {
    private companion object {
        const val HARD_CODED_KEY0 = "894FE8DAD6E206F142D107D11805579A"
        const val HARD_CODED_MASTER_KEY = "3DA90A7D29A5797A16ED53D91A49B803"
        const val SUN_BASE_URL = "https://api.beamio.app/api/sun"
        /** 与 SilentPassUI utils/constants.ts beamioApi 一致 */
        const val BEAMIO_API = "https://beamio.app"
        /** USDC on Base */
        const val USDC_BASE = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"
        /** 基础设施卡（与 x402sdk chainAddresses.BEAMIO_USER_CARD_ASSET_ADDRESS 一致） */
        const val BEAMIO_USER_CARD_ASSET_ADDRESS = "0x9cda8477c9f03b8759ac64e21941e578908fd750"
        /** Base RPC，遵循 beamio-base-rpc */
        private const val BASE_RPC_URL = "https://base-rpc.conet.network"
        private const val PREFS_PROFILE_CACHE = "beamio_profile_cache"
        /** 已废弃的旧卡地址，从 endpoint 返回的资产中过滤掉 */
        private const val DEPRECATED_CARD_ADDRESS = "0xEcC5bDFF6716847e45363befD3506B1D539c02D5"
        /** 付款动态 QR 解析失败时的 Logcat 标签（adb logcat -s BeamioQrPayment） */
        private const val LOG_QR_PAYMENT_PARSE = "BeamioQrPayment"
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
    private var topupScreenTierName by mutableStateOf<String?>(null)
    private var topupScreenTierDescription by mutableStateOf<String?>(null)
    private var topupScreenMemberNo by mutableStateOf<String?>(null)
    private var topupArmed by mutableStateOf(false)
    private var topupViaQr by mutableStateOf(false)

    private var showReadScreen by mutableStateOf(false)
    private var readViaQr by mutableStateOf(false)
    private var readScreenUid by mutableStateOf("")
    private var readScreenWallet by mutableStateOf<String?>(null)
    private var readScreenStatus by mutableStateOf<ReadStatus>(ReadStatus.Waiting)
    private var readScreenAssets by mutableStateOf<UIDAssets?>(null)
    private var readScreenRawJson by mutableStateOf<String?>(null)
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
    /** 小费率 bps（如 1800 = 18%），与 nfcTipCurrencyAmount 一并 POST；记账小费走 TX_TIP 行 */
    private var paymentScreenTipRateBps by mutableStateOf(0)
    /** Charge 明细：税率（与终端 metadata / 基础设施 routing 一致） */
    private var paymentChargeBreakdownTaxPercent by mutableStateOf<Double?>(null)
    /** null = 尚未根据客户卡解析；0–100 为会员 tier 折扣率 */
    private var paymentChargeBreakdownTierDiscountPercent by mutableStateOf<Int?>(null)
    private var paymentScreenPreBalance by mutableStateOf<String?>(null)
    private var paymentScreenPostBalance by mutableStateOf<String?>(null)
    private var paymentScreenCardCurrency by mutableStateOf<String?>(null)
    private var paymentScreenCardBackground by mutableStateOf<String?>(null)
    private var paymentScreenCardImage by mutableStateOf<String?>(null)
    private var paymentScreenCardName by mutableStateOf<String?>(null)
    private var paymentScreenTierName by mutableStateOf<String?>(null)
    private var paymentScreenCardType by mutableStateOf<String?>(null)
    private var paymentScreenMemberNo by mutableStateOf<String?>(null)
    /** Charge 选小费页填写的桌号，随成功页 / 打印收据展示 */
    private var paymentScreenTableNumber by mutableStateOf("")
    private var paymentArmed by mutableStateOf(false)
    private var paymentViaQr by mutableStateOf(false)
    private val cardMetadataTierCache = mutableMapOf<String, List<MetadataTier>>()

    private var uidText by mutableStateOf("Not read")
    private var readCheckText by mutableStateOf("Init status not detected")
    private var readUrlText by mutableStateOf("")
    private var readParamText by mutableStateOf("")
    private var lastCounter by mutableStateOf(0)
    private var lastCounterInput by mutableStateOf("0")
    private var initText by mutableStateOf("Not initialized")
    private var topupAmount by mutableStateOf("")
    private var paymentAmount by mutableStateOf("")
    private var readArmed by mutableStateOf(false)
    private var linkAppArmed by mutableStateOf(false)
    /** Link App 成功后展示的深链（含 nftRedeemcode 等 query） */
    private var linkAppDeepLinkUrl by mutableStateOf("")
    /** 409 NFC_LINK_APP_CARD_LOCKED：保留本次 SUN，供 Cancel link lock 调用 /api/nfcLinkAppCancel */
    private var linkAppLastSunForCancel: SunParams? by mutableStateOf(null)
    private var linkAppCancelInProgress by mutableStateOf(false)
    private var initArmed by mutableStateOf(false)
    private var initTemplateArmed by mutableStateOf(false)
    private var showWelcomePage by mutableStateOf(false)
    private var showOnboardingScreen by mutableStateOf(false)
    private var showAmountInputScreen by mutableStateOf(false)
    private var amountInputMode by mutableStateOf("charge") // "charge" | "topup"
    private var amountInput by mutableStateOf("0")
    private var showTipScreen by mutableStateOf(false)
    private var tipScreenSubtotal by mutableStateOf("0")
    private var selectedTipRate by mutableStateOf(0.0) // 0, 0.15, 0.18, 0.20
    private var chargeTableNumber by mutableStateOf("")
    private var showScanMethodScreen by mutableStateOf(false)
    private var scanMethodState by mutableStateOf("nfc") // "nfc" | "qr"
    /** NFC 模式下为 true 时显示中央扫描线动画并已 arm read/topup/payment；进入选方式页或切回 Tap Card 时自动置 true */
    private var scanWaitingForNfc by mutableStateOf(false)
    private var nfcFetchingInfo by mutableStateOf(false) // 已获 UID，正在拉取信息，不跳新页
    private var nfcFetchError by mutableStateOf("") // 拉取失败时在 280.dp 框内显示
    private var pendingScanAction by mutableStateOf("read") // "read" | "payment" | "topup" | "linkApp"
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
    private var qrScanningActive by mutableStateOf(false) // 按下 Scan QR 后，在 280.dp 方框内显示相机
    /** Payment QR 解析失败时递增，用于重建 EmbeddedQrScanner、清除 lastText 去重，便于同一画面内继续扫同一码 */
    private var paymentQrDecodeResetKey by mutableStateOf(0)
    /** Balance read + Scan QR：无效链接或需重试时递增，重建扫码视图 */
    private var readQrDecodeResetKey by mutableStateOf(0)
    /** Charge + Scan QR：已扫到码，正在解析（关相机、中央 280dp 显示 loading） */
    private var paymentQrInterpreting by mutableStateOf(false)
    /** Charge + Scan QR：解析失败时在中央卡片展示（英文） */
    private var paymentQrParseError by mutableStateOf("")
    /** QR Top-up：扫码后留在 Scan 页中央显示 Sign & execute，成功/失败再进入 TopupScreen */
    private var topupQrSigningInProgress by mutableStateOf(false)
    /** NFC Top-up：贴卡后留在选方式页中央 280dp 显示 Sign & execute，成功后再进 TopupScreen 完成页 */
    private var topupNfcExecuteInProgress by mutableStateOf(false)
    /** QR Top-up 在选方式页失败：中央白卡片展示文案，点此区域重试 */
    private var topupQrExecuteError by mutableStateOf("")

    /** AA 账号检测：null=未检测/检测中，true=有 AA，false=无 AA。无 AA 时显示欢迎面板 */
    private var hasAAAccount by mutableStateOf<Boolean?>(null)

    /** 终端首页头部：当前钱包 beamio profile 及上层 admin（merchant）profile */
    private var terminalProfile by mutableStateOf<TerminalProfile?>(null)
    private var terminalAdminProfile by mutableStateOf<TerminalProfile?>(null)

    /** 卡统计：Charge=periodTransferAmount，Top-Up=redeemMintCounterFromClear */
    private var cardChargeAmount by mutableStateOf<Double?>(null)
    private var cardTopUpAmount by mutableStateOf<Double?>(null)

    /** Top-up / Charge 成功后等待链上统计更新；与 baseline 比较，直到变化或放弃 */
    private var pendingVerifyChargeStats by mutableStateOf(false)
    private var pendingVerifyTopUpStats by mutableStateOf(false)
    private var baselineChargeStats: Double? = null
    private var baselineTopUpStats: Double? = null
    /** RPC 拉取失败时在仪表盘对应金额旁显示黄色感叹号 */
    private var cardStatsChargeRpcWarning by mutableStateOf(false)
    private var cardStatsTopUpRpcWarning by mutableStateOf(false)

    /** 基础设施卡 metadata：`tierRoutingDiscounts.taxRatePercent`；折扣为所有 tier 一行拼接（15s home daemon） */
    private var dashboardInfraTaxPercent by mutableStateOf<Double?>(null)
    private var dashboardInfraDiscountSummary by mutableStateOf<String?>(null)

    private val cardStatsVerifyLock = Any()
    @Volatile private var cardStatsVerifyThreadRunning = false

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) qrScanningActive = true
    }

    /** 从 beamio URL 解析 wallet 参数，如 https://beamio.app/?beamio=xxx&wallet=0x... */
    private fun parseBeamioWalletFromUrl(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            uri.getQueryParameter("wallet")?.takeIf { it.startsWith("0x") && it.length >= 40 }
        } catch (_: Exception) {
            null
        }
    }

    /** 从 beamio URL 解析 beamioTab（beamio 参数），如 https://beamio.app?beamio=rrrwe1111&wallet=0x... */
    private fun parseBeamioTabFromUrl(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            uri.getQueryParameter("beamio")?.takeIf { it.isNotBlank() }?.trim()
        } catch (_: Exception) {
            null
        }
    }

    /** 从文本中截取第一个 `{` 到最后一个 `}`，忽略前后缀（如误扫到空白、BOM、前缀文案） */
    private fun extractJsonObjectSubstring(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return raw.substring(start, end + 1)
    }

    /** account / signature / nonce 等：兼容 JSON 里为 string 或 number */
    private fun org.json.JSONObject.optStringCoerced(key: String): String {
        if (!has(key)) return ""
        return try {
            val v = get(key)
            when (v) {
                is String -> v
                is Number -> v.toString()
                else -> if (v == null || v == org.json.JSONObject.NULL) "" else v.toString()
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * 将扫码内容解析为 JSON（支持 BOM、首尾空白、前缀后缀、外层 JSON 字符串、单层 openContainerPayload 包装）。
     */
    private fun parseJsonObjectForQrPayment(content: String): org.json.JSONObject? {
        var t = content.trim()
        if (t.startsWith("\uFEFF")) t = t.substring(1).trim()
        if (t.isEmpty()) return null
        val candidate = extractJsonObjectSubstring(t) ?: t
        return try {
            org.json.JSONObject(candidate)
        } catch (_: Exception) {
            try {
                when (val v = org.json.JSONTokener(candidate).nextValue()) {
                    is String -> {
                        val inner = v.trim().let { if (it.startsWith("\uFEFF")) it.substring(1).trim() else it }
                        val sub = extractJsonObjectSubstring(inner) ?: inner
                        org.json.JSONObject(sub)
                    }
                    is org.json.JSONObject -> v
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * React / SilentPassUI OpenContainerMain 离线签名（containerMainRelayedOpen）：含 currencyType，不含固定 to/items。
     * 封闭 relay（ContainerMain + itemsHash）JSON 含 to + 非空 items，且通常无 currencyType — 不可按 open relay 处理。
     */
    private fun normalizeReactOpenRelayPayload(o: org.json.JSONObject) {
        val vb = o.optStringCoerced("validBefore").trim()
        val dl = o.optStringCoerced("deadline").trim()
        when {
            dl.isEmpty() && vb.isNotEmpty() -> o.put("deadline", vb)
            vb.isEmpty() && dl.isNotEmpty() -> o.put("validBefore", dl)
        }
        if (!o.has("maxAmount")) {
            o.put("maxAmount", "0")
        }
        if (!o.has("currencyType")) {
            o.put("currencyType", 4)
        }
    }

    private data class OpenQrParseResult(
        val payload: org.json.JSONObject?,
        val rejectReason: String?
    )

    /** Logcat：完整原始扫码串（分块），便于对照 JSON / 隐藏字符。仅解析失败时调用。 */
    private fun logPaymentQrParseDebug(raw: String, rejectReason: String) {
        val tag = LOG_QR_PAYMENT_PARSE
        Log.w(tag, "parse FAILED: $rejectReason | length=${raw.length}")
        if (raw.isEmpty()) return
        val maxChunk = 3500
        var start = 0
        var part = 0
        while (start < raw.length && part < 30) {
            val end = min(start + maxChunk, raw.length)
            Log.w(tag, "raw_part_$part=${raw.substring(start, end)}")
            start = end
            part++
        }
        if (start < raw.length) {
            Log.w(tag, "... log truncated: ${raw.length - start} chars not shown (max ${maxChunk * 30} in log)")
        }
    }

    /** 解析 QR 中的 OpenContainerRelayPayload（由 signAAtoEOA_USDC_with_BeamioContainerMainRelayedOpen 生成）
     * Open relay：{ account, currencyType, nonce, deadline, signature, validBefore? }（validBefore 可与 deadline 二选一，由 normalize 补全）
     * 封闭 relay：{ account, to, items, nonce, deadline, signature }（兼容保留）
     */
    private fun parseOpenContainerRelayPayload(content: String): OpenQrParseResult {
        return try {
            var root = parseJsonObjectForQrPayment(content)
                ?: return OpenQrParseResult(null, "not a JSON object (see raw below)")
            root.optJSONObject("openContainerPayload")?.let { root = it }
            val account = root.optStringCoerced("account").trim()
            val sig = root.optStringCoerced("signature").trim()
            if (account.isEmpty()) {
                val keyList = buildString {
                    val ki = root.keys()
                    while (ki.hasNext()) {
                        if (isNotEmpty()) append(',')
                        append(ki.next())
                    }
                }
                return OpenQrParseResult(null, "missing or empty account (keys=$keyList)")
            }
            if (sig.isEmpty()) {
                return OpenQrParseResult(null, "missing or empty signature")
            }

            val to = root.optStringCoerced("to").trim()
            val items = root.optJSONArray("items")
            val hasClosedRelayItems =
                to.isNotEmpty() && items != null && items.length() > 0

            val isOpenRelayQr = when {
                root.has("currencyType") -> true
                hasClosedRelayItems -> false
                else -> root.has("nonce") && (root.has("deadline") || root.has("validBefore"))
            }

            when {
                isOpenRelayQr -> {
                    normalizeReactOpenRelayPayload(root)
                    OpenQrParseResult(root, null)
                }
                hasClosedRelayItems -> OpenQrParseResult(root, null)
                else -> OpenQrParseResult(
                    null,
                    "neither open relay (need currencyType or nonce+deadline/validBefore without to+items) nor closed relay (need to+non-empty items)"
                )
            }
        } catch (e: Exception) {
            OpenQrParseResult(null, "exception: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { handleQrScanResult(it) }
    }

    private fun handleQrScanResult(content: String) {
        when (pendingScanAction) {
            "payment" -> {
                qrScanningActive = false
                paymentQrParseError = ""
                paymentQrInterpreting = true
                vibratePaymentQrAck()
                Thread {
                    val parsed = parseOpenContainerRelayPayload(content)
                    runOnUiThread {
                        paymentQrInterpreting = false
                        if (parsed.payload != null) {
                            executeQrPayment(parsed.payload)
                        } else {
                            logPaymentQrParseDebug(content, parsed.rejectReason ?: "unknown")
                            paymentQrParseError = humanizePaymentQrRejectReason(parsed.rejectReason ?: "unknown")
                            paymentQrDecodeResetKey++
                        }
                    }
                }.start()
            }
            else -> {
                val beamioTab = parseBeamioTabFromUrl(content)
                val wallet = parseBeamioWalletFromUrl(content)
                if (beamioTab == null && wallet == null) {
                    uidText = "Cannot parse URL. Please scan a beamio.app link"
                    if (pendingScanAction == "read" && showScanMethodScreen) {
                        nfcFetchError = "Cannot parse URL. Please scan a beamio.app link."
                        readQrDecodeResetKey++
                        qrScanningActive = false
                    }
                    if (pendingScanAction == "linkApp" && showScanMethodScreen) {
                        nfcFetchError = "Use NFC to scan the customer card."
                        qrScanningActive = false
                    }
                    return
                }
                when (pendingScanAction) {
                    "read" -> {
                        // 与 NFC 读卡一致：留在选方式页，中央显示 Loading；成功再进 Balance Loaded
                        qrScanningActive = false
                        readViaQr = true
                        showReadScreen = false
                        showScanMethodScreen = true
                        readScreenUid = beamioTab ?: ""
                        readScreenWallet = wallet ?: ""
                        readScreenStatus = ReadStatus.Loading
                        readScreenAssets = null
                        readScreenRawJson = null
                        readScreenError = ""
                        readArmed = false
                        nfcFetchError = ""
                        scanWaitingForNfc = false
                        nfcFetchingInfo = true
                        if (beamioTab != null) {
                            fetchUidAssets(beamioTab)
                        } else {
                            fetchWalletAssets(wallet!!)
                        }
                    }
                    "linkApp" -> {
                        qrScanningActive = false
                        uidText = "Use NFC to scan the customer card"
                    }
                    "topup" -> {
                        if (beamioTab == null && wallet == null) {
                            uidText = "Link missing beamio or wallet parameter. Cannot top up"
                            return
                        }
                        qrScanningActive = false
                        topupViaQr = true
                        topupQrSigningInProgress = true
                        topupQrExecuteError = ""
                        topupScreenUid = beamioTab ?: ""
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
                        topupScreenTierName = null
                        topupScreenTierDescription = null
                        topupArmed = false
                        if (beamioTab != null) {
                            executeTopupWithBeamioTag(beamioTab, topupScreenAmount)
                        } else {
                            executeWalletTopup(wallet!!, topupScreenAmount)
                        }
                    }
                    else -> { }
                }
            }
        }
    }

    private fun appendInitStep(step: String) {
        initText = if (initText.isBlank()) step else "$initText\n$step"
    }

    private fun initFail(step: String, reason: String) {
        appendInitStep("❌ $step failed: $reason")
        initArmed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedKey = WalletStorageManager.loadPrivateKey(this)
        if (!savedKey.isNullOrEmpty()) {
            try {
                BeamioWeb3Wallet.init(savedKey)
            } catch (_: Exception) {
                showWelcomePage = true
            }
        } else {
            showWelcomePage = true
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
                LaunchedEffect(showOnboardingScreen) {
                    if (!showOnboardingScreen && BeamioWeb3Wallet.isInitialized() && hasAAAccount == null) {
                        Thread {
                            try {
                                val assets = fetchWalletAssetsSync(BeamioWeb3Wallet.getAddress())
                                runOnUiThread {
                                    hasAAAccount = !assets?.aaAddress.isNullOrEmpty()
                                }
                            } catch (_: Exception) {
                                runOnUiThread { hasAAAccount = true }
                            }
                        }.start()
                    }
                }
                LaunchedEffect(showWelcomePage, showOnboardingScreen, hasAAAccount) {
                    if (!showWelcomePage && !showOnboardingScreen && BeamioWeb3Wallet.isInitialized()) {
                        val wallet = BeamioWeb3Wallet.getAddress() ?: ""
                        if (wallet.isNotEmpty()) {
                            val (cachedTerm, cachedAdmin) = loadProfileCache(wallet)
                            terminalProfile = cachedTerm
                            terminalAdminProfile = cachedAdmin
                        }
                        Thread {
                            val wallet = BeamioWeb3Wallet.getAddress()
                            if (wallet.isNullOrEmpty()) return@Thread
                            val (profile, admin) = fetchTerminalProfileSync(wallet)
                            val (charge, topUp) = fetchCardStatsSync(wallet)
                            runOnUiThread {
                                if (profile != null) terminalProfile = profile
                                if (admin != null) terminalAdminProfile = admin
                                if (profile != null || admin != null) saveProfileCache(wallet, profile, admin)
                                if (charge != null) cardChargeAmount = charge
                                if (topUp != null) cardTopUpAmount = topUp
                            }
                        }.start()
                    }
                }
                LaunchedEffect(showWelcomePage, showOnboardingScreen, showTopupScreen, showReadScreen, showScanMethodScreen, showAmountInputScreen, showTipScreen, showPaymentScreen, showInitFlowScreen) {
                    val onHome = !showWelcomePage && !showOnboardingScreen && !showTopupScreen && !showReadScreen &&
                        !showScanMethodScreen && !showAmountInputScreen && !showTipScreen && !showPaymentScreen && !showInitFlowScreen
                    if (onHome) {
                        Thread { prefetchInfraCardMetadata() }.start()
                    }
                }
                /** Home：每 15s 拉取本终端在基础设施卡上的 `tierRoutingDiscounts`（税率 + tier 折扣摘要） */
                LaunchedEffect(
                    showWelcomePage,
                    showOnboardingScreen,
                    showTopupScreen,
                    showReadScreen,
                    showScanMethodScreen,
                    showAmountInputScreen,
                    showTipScreen,
                    showPaymentScreen,
                    showInitFlowScreen
                ) {
                    val onHome = !showWelcomePage && !showOnboardingScreen && !showTopupScreen && !showReadScreen &&
                        !showScanMethodScreen && !showAmountInputScreen && !showTipScreen && !showPaymentScreen && !showInitFlowScreen
                    if (!onHome || !BeamioWeb3Wallet.isInitialized()) return@LaunchedEffect
                    while (isActive) {
                        val wallet = BeamioWeb3Wallet.getAddress()?.trim().orEmpty()
                        if (wallet.isNotEmpty()) {
                            Thread {
                                val parsed = fetchInfraRoutingForTerminalWalletSync(wallet)
                                runOnUiThread {
                                    if (parsed != null) {
                                        dashboardInfraTaxPercent = parsed.first
                                        dashboardInfraDiscountSummary = parsed.second
                                    }
                                }
                            }.start()
                        }
                        delay(15_000L)
                    }
                }
                LaunchedEffect(topupScreenStatus, topupQrSigningInProgress) {
                    if (!topupQrSigningInProgress) return@LaunchedEffect
                    if (topupScreenStatus is TopupStatus.Success) {
                        topupQrSigningInProgress = false
                        topupQrExecuteError = ""
                        showScanMethodScreen = false
                        showTopupScreen = true
                    }
                }
                val linkAppQrBitmap = androidx.compose.runtime.remember(linkAppDeepLinkUrl) {
                    if (linkAppDeepLinkUrl.isEmpty()) null else encodeLinkAppQrBitmap(linkAppDeepLinkUrl)
                }
                when {
                    showWelcomePage -> WelcomePage(
                        versionName = BuildConfig.VERSION_NAME ?: "1.0",
                        onCreateWalletClick = {
                            showWelcomePage = false
                            showOnboardingScreen = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showOnboardingScreen -> OnboardingScreen(
                        onCreateComplete = { privateKeyHex ->
                            WalletStorageManager.savePrivateKey(this@MainActivity, privateKeyHex)
                            BeamioWeb3Wallet.init(privateKeyHex)
                            hasAAAccount = null
                            showOnboardingScreen = false
                        },
                        onBackToWelcome = {
                            showOnboardingScreen = false
                            showWelcomePage = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showTopupScreen -> TopupScreen(
                        amount = topupScreenAmount,
                        uid = topupScreenUid,
                        status = topupScreenStatus,
                        txHash = topupScreenTxHash,
                        error = topupScreenError,
                        preBalance = topupScreenPreBalance,
                        postBalance = topupScreenPostBalance,
                        cardCurrency = topupScreenCardCurrency,
                        address = topupScreenAddress,
                        memberNo = topupScreenMemberNo,
                        cardBackground = topupScreenCardBackground,
                        cardImage = topupScreenCardImage,
                        tierName = topupScreenTierName,
                        tierDescription = topupScreenTierDescription,
                        settlementViaQr = topupViaQr,
                        onBack = { closeTopupScreen() },
                        modifier = Modifier.fillMaxSize()
                    )
                    showScanMethodScreen -> ScanMethodSelectionScreen(
                        scanMethod = scanMethodState,
                        scanWaitingForNfc = scanWaitingForNfc,
                        nfcFetchingInfo = nfcFetchingInfo,
                        nfcFetchError = nfcFetchError,
                        hideMethodToggle = pendingScanAction == "linkApp",
                        linkAppDeepLinkUrl = linkAppDeepLinkUrl,
                        linkAppQrBitmap = linkAppQrBitmap,
                        onCopyLinkAppUrl = {
                            if (linkAppDeepLinkUrl.isNotEmpty()) {
                                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("beamio-link-app", linkAppDeepLinkUrl))
                            }
                        },
                        linkAppLockedShowCancel = pendingScanAction == "linkApp" && linkAppLastSunForCancel != null && nfcFetchError.isNotEmpty(),
                        linkAppCancelInProgress = linkAppCancelInProgress,
                        onLinkAppCancelLock = { runLinkAppCancelLock() },
                        topupQrSigningInProgress = topupQrSigningInProgress,
                        topupNfcExecuteInProgress = topupNfcExecuteInProgress,
                        topupExecuteUidDisplay = topupScreenUid,
                        topupQrExecuteError = topupQrExecuteError,
                        onRetryTopupQrExecute = { retryTopupQrExecute() },
                        onScanMethodChange = { s ->
                            scanMethodState = s
                            paymentQrParseError = ""
                            paymentQrInterpreting = false
                            if (s == "nfc") {
                                qrScanningActive = false
                                nfcFetchingInfo = false
                                nfcFetchError = ""
                            }
                            if (s == "qr") {
                                scanWaitingForNfc = false
                                nfcFetchingInfo = false
                                nfcFetchError = ""
                            }
                            syncScanMethodEntryAndTabs()
                        },
                        embeddedQrResetKey = when (pendingScanAction) {
                            "payment" -> paymentQrDecodeResetKey
                            "read" -> readQrDecodeResetKey
                            else -> 0
                        },
                        onRetryAfterCentralFetchError = { retryCentralFetchAfterError() },
                        paymentQrInterpreting = paymentQrInterpreting,
                        paymentQrParseError = paymentQrParseError,
                        onRetryPaymentQrScan = {
                            paymentQrParseError = ""
                            paymentQrDecodeResetKey++
                            if (scanMethodState == "qr") startEmbeddedQrScanning()
                        },
                        onRetryPaymentAfterError = { retryChargeAfterScanMethodError() },
                        pendingAction = pendingScanAction,
                        totalAmount = when (pendingScanAction) {
                            "payment" -> paymentScreenAmount
                            "topup" -> topupScreenAmount
                            else -> ""
                        },
                        paymentStatus = if (pendingScanAction == "payment") paymentScreenStatus else null,
                        paymentRoutingSteps = if (pendingScanAction == "payment") paymentScreenRoutingSteps else emptyList(),
                        paymentError = if (pendingScanAction == "payment") paymentScreenError else "",
                        paymentNfcUid = if (pendingScanAction == "payment") paymentScreenUid else "",
                        paymentAmount = if (pendingScanAction == "payment") paymentScreenAmount else "",
                        paymentPayee = if (pendingScanAction == "payment") paymentScreenPayee else "",
                        paymentTxHash = if (pendingScanAction == "payment") paymentScreenTxHash else "",
                        paymentSubtotal = if (pendingScanAction == "payment") paymentScreenSubtotal else null,
                        paymentTip = if (pendingScanAction == "payment") paymentScreenTip else null,
                        paymentPostBalance = if (pendingScanAction == "payment") paymentScreenPostBalance else null,
                        paymentCardCurrency = if (pendingScanAction == "payment") paymentScreenCardCurrency else null,
                        paymentMemberNo = if (pendingScanAction == "payment") paymentScreenMemberNo else null,
                        paymentCardBackground = if (pendingScanAction == "payment") paymentScreenCardBackground else null,
                        paymentCardImage = if (pendingScanAction == "payment") paymentScreenCardImage else null,
                        paymentCardName = if (pendingScanAction == "payment") paymentScreenCardName else null,
                        paymentTierName = if (pendingScanAction == "payment") paymentScreenTierName else null,
                        paymentCardType = if (pendingScanAction == "payment") paymentScreenCardType else null,
                        paymentChargeTaxPercent = if (pendingScanAction == "payment") paymentChargeBreakdownTaxPercent else null,
                        paymentChargeTierDiscountPercent = if (pendingScanAction == "payment") paymentChargeBreakdownTierDiscountPercent else null,
                        paymentChargeTipBps = if (pendingScanAction == "payment") paymentScreenTipRateBps else 0,
                        paymentTableNumber = if (pendingScanAction == "payment") paymentScreenTableNumber else "",
                        onPaymentDone = {
                            showScanMethodScreen = false
                            showTipScreen = false
                            showAmountInputScreen = false
                            qrScanningActive = false
                            scanWaitingForNfc = false
                            nfcFetchingInfo = false
                            nfcFetchError = ""
                            paymentArmed = false
                            chargeTableNumber = ""
                            resetPaymentScreenState()
                        },
                        onProceed = { proceedFromScanMethod() },
                        onProceedNfcStay = { armForNfcScan() },
                        onProceedWithQr = if (pendingScanAction == "read" || pendingScanAction == "topup" || pendingScanAction == "payment") {
                            { startEmbeddedQrScanning() }
                        } else null,
                        qrScanningActive = qrScanningActive,
                        onQrScanResult = { result -> handleQrScanResult(result) },
                        onCancel = {
                            qrScanningActive = false
                            scanWaitingForNfc = false
                            nfcFetchingInfo = false
                            nfcFetchError = ""
                            topupNfcExecuteInProgress = false
                            topupQrSigningInProgress = false
                            topupQrExecuteError = ""
                            readArmed = false
                            linkAppArmed = false
                            topupArmed = false
                            paymentArmed = false
                            showScanMethodScreen = false
                            showTipScreen = false
                            showAmountInputScreen = false
                            when (pendingScanAction) {
                                "payment" -> resetPaymentScreenState()
                                "topup" -> resetTopupScreenState()
                                "linkApp" -> {
                                    linkAppDeepLinkUrl = ""
                                    linkAppLastSunForCancel = null
                                    linkAppCancelInProgress = false
                                }
                                else -> resetReadScreenState()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    showReadScreen -> ReadScreen(
                        uid = readScreenUid,
                        status = readScreenStatus,
                        assets = readScreenAssets,
                        rawResponseJson = readScreenRawJson,
                        error = readScreenError,
                        settlementViaQr = readViaQr,
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
                        onPadClick = { v -> handleAmountPadClick(v) },
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
                                    "topup" -> {
                                        // 与 Home Top-up 一致：金额 → 选方式（NFC / QR）→ 贴卡或扫码，不再从 Balance Loaded 直跳 execute
                                        startTopup(amt)
                                    }
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
                        tableNumber = chargeTableNumber,
                        onTableNumberChange = { chargeTableNumber = it },
                        onTipRateSelect = { r -> selectedTipRate = r },
                        onBack = {
                            showTipScreen = false
                            amountInput = tipScreenSubtotal
                            showAmountInputScreen = true
                        },
                        onConfirmPay = {
                            val subtotal = tipScreenSubtotal.toDoubleOrNull() ?: 0.0
                            // 预览时尚无客户卡 tier：折扣按 0；实扣在 executePayment/executeQr 按真实 tier 重算 tip
                            val tip = chargeTipFromRequestAndRate(subtotal, selectedTipRate)
                            val taxP = dashboardInfraTaxPercent ?: 0.0
                            val total = chargeTotalInCurrency(subtotal, taxP, null, tip)
                            val tipBps = kotlin.math.round(selectedTipRate * 10000.0).toInt().coerceIn(0, 10000)
                            scanMethodBackTipSubtotal = tipScreenSubtotal
                            scanMethodBackTipRate = selectedTipRate
                            showTipScreen = false
                            tipScreenSubtotal = "0"
                            selectedTipRate = 0.0
                            startPayment("%.2f".format(total), "%.2f".format(subtotal), "%.2f".format(tip), tipBps)
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
                        memberNo = paymentScreenMemberNo,
                        cardBackground = paymentScreenCardBackground,
                        cardImage = paymentScreenCardImage,
                        cardName = paymentScreenCardName,
                        tierName = paymentScreenTierName,
                        cardType = paymentScreenCardType,
                        settlementViaQr = paymentViaQr,
                        chargeTaxPercent = paymentChargeBreakdownTaxPercent,
                        chargeTierDiscountPercent = paymentChargeBreakdownTierDiscountPercent,
                        tableNumber = paymentScreenTableNumber.takeIf { it.isNotBlank() },
                        onBack = { closePaymentScreen() },
                        modifier = Modifier.fillMaxSize()
                    )
                    hasAAAccount == false -> NdefScreen(
                        uidText = uidText,
                        readUrlText = readUrlText,
                        readParamText = readParamText,
                        walletAddress = if (BeamioWeb3Wallet.isInitialized()) BeamioWeb3Wallet.getAddress() else null,
                        terminalProfile = terminalProfile,
                        adminProfile = terminalAdminProfile,
                        chargeAmount = cardChargeAmount,
                        topUpAmount = cardTopUpAmount,
                        chargeStatsRpcWarning = cardStatsChargeRpcWarning,
                        topUpStatsRpcWarning = cardStatsTopUpRpcWarning,
                        onCopyWalletClick = {
                            if (BeamioWeb3Wallet.isInitialized()) {
                                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("wallet", BeamioWeb3Wallet.getAddress()))
                            }
                        },
                        onLinkAppClick = { startLinkAppFlow() },
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
                        contentAboveCharge = { WelcomePanelNoAA(modifier = Modifier.fillMaxWidth(), compact = true) },
                        infraRoutingTaxPercent = dashboardInfraTaxPercent,
                        infraRoutingDiscountSummary = dashboardInfraDiscountSummary,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> NdefScreen(
                        uidText = uidText,
                        readUrlText = readUrlText,
                        readParamText = readParamText,
                        walletAddress = if (BeamioWeb3Wallet.isInitialized()) BeamioWeb3Wallet.getAddress() else null,
                        terminalProfile = terminalProfile,
                        adminProfile = terminalAdminProfile,
                        chargeAmount = cardChargeAmount,
                        topUpAmount = cardTopUpAmount,
                        chargeStatsRpcWarning = cardStatsChargeRpcWarning,
                        topUpStatsRpcWarning = cardStatsTopUpRpcWarning,
                        infraRoutingTaxPercent = dashboardInfraTaxPercent,
                        infraRoutingDiscountSummary = dashboardInfraDiscountSummary,
                        onCopyWalletClick = {
                            if (BeamioWeb3Wallet.isInitialized()) {
                                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("wallet", BeamioWeb3Wallet.getAddress()))
                            }
                        },
                        onLinkAppClick = { startLinkAppFlow() },
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
            uidText = "Device does not support NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            uidText = "Please enable NFC first"
            return
        }
        resetReadScreenState()
        pendingScanAction = "read"
        scanMethodState = "nfc"
        showScanMethodScreen = true
        syncScanMethodEntryAndTabs()
    }

    private fun startLinkAppFlow() {
        if (nfcAdapter == null) {
            uidText = "Device does not support NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            uidText = "Please enable NFC first"
            return
        }
        linkAppDeepLinkUrl = ""
        nfcFetchError = ""
        linkAppLastSunForCancel = null
        linkAppCancelInProgress = false
        pendingScanAction = "linkApp"
        scanMethodState = "nfc"
        showScanMethodScreen = true
        syncScanMethodEntryAndTabs()
    }

    private fun closeReadScreen() {
        showReadScreen = false
        readArmed = false
    }

    private fun startTopup(amount: String) {
        if (nfcAdapter == null) return
        if (nfcAdapter?.isEnabled != true) return
        resetTopupScreenState()
        topupScreenAmount = amount
        pendingScanAction = "topup"
        scanMethodState = "nfc"
        showScanMethodScreen = true
        syncScanMethodEntryAndTabs()
    }

    private fun closeTopupScreen() {
        showTopupScreen = false
        topupArmed = false
        topupQrSigningInProgress = false
        topupNfcExecuteInProgress = false
        topupQrExecuteError = ""
    }

    /** Wallet / beamioTag Top-up 错误：QR 且在选方式页时留在 Scan 中央卡片，否则进 TopupScreen */
    private fun applyTopupQrScanScreenError(message: String) {
        topupScreenError = message
        topupScreenStatus = TopupStatus.Error
        if (topupViaQr && showScanMethodScreen) {
            topupQrSigningInProgress = false
            topupQrExecuteError = message
        } else {
            topupQrExecuteError = ""
            if (!showTopupScreen) showTopupScreen = true
        }
    }

    private fun retryTopupQrExecute() {
        if (topupQrExecuteError.isEmpty()) return
        topupQrExecuteError = ""
        topupQrSigningInProgress = true
        topupScreenStatus = TopupStatus.Loading
        topupScreenError = ""
        when {
            topupScreenUid.isNotBlank() -> executeTopupWithBeamioTag(topupScreenUid, topupScreenAmount)
            topupScreenWallet != null -> executeWalletTopup(topupScreenWallet!!, topupScreenAmount)
            else -> {
                topupQrSigningInProgress = false
                applyTopupQrScanScreenError("Missing customer link. Scan QR again.")
            }
        }
    }

    /** 重置支付相关状态，避免 cancel 后再次 Charge 时残留旧数据 */
    private fun resetPaymentScreenState() {
        paymentViaQr = false
        paymentScreenAmount = ""
        paymentScreenPayee = ""
        paymentScreenUid = ""
        paymentScreenStatus = PaymentStatus.Waiting
        paymentScreenTxHash = ""
        paymentScreenError = ""
        paymentScreenRoutingSteps = emptyList()
        paymentScreenSubtotal = "0"
        paymentScreenTip = "0"
        paymentScreenTipRateBps = 0
        paymentChargeBreakdownTaxPercent = null
        paymentChargeBreakdownTierDiscountPercent = null
        paymentScreenPreBalance = null
        paymentScreenPostBalance = null
        paymentScreenCardCurrency = null
        paymentScreenCardBackground = null
        paymentScreenCardImage = null
        paymentScreenCardName = null
        paymentScreenTierName = null
        paymentScreenCardType = null
        paymentScreenMemberNo = null
        paymentScreenTableNumber = ""
        paymentQrInterpreting = false
        paymentQrParseError = ""
    }

    /** Charge 动态 QR：扫到码后短振动，提示进入解析阶段 */
    private fun vibratePaymentQrAck() {
        val v: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (v == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(85, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(85)
            }
        } catch (_: SecurityException) {
        }
    }

    private fun humanizePaymentQrRejectReason(reason: String): String {
        return when {
            reason.startsWith("not a JSON object") -> "Invalid QR: data is not valid JSON."
            reason.startsWith("missing or empty account") -> "Invalid QR: missing customer account."
            reason.startsWith("missing or empty signature") -> "Invalid QR: missing payment signature."
            reason.startsWith("neither open relay") -> "Invalid QR: unrecognized payment format."
            reason.startsWith("exception:") -> "Could not read payment code. Please try again."
            else -> "Could not read payment code."
        }
    }

    /** 重置充值相关状态 */
    private fun resetTopupScreenState() {
        topupQrSigningInProgress = false
        topupNfcExecuteInProgress = false
        topupQrExecuteError = ""
        topupViaQr = false
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
        topupScreenTierName = null
        topupScreenTierDescription = null
        topupScreenMemberNo = null
    }

    /** 重置读取相关状态 */
    private fun resetReadScreenState() {
        readViaQr = false
        readScreenUid = ""
        readScreenWallet = null
        readScreenStatus = ReadStatus.Waiting
        readScreenAssets = null
        readScreenRawJson = null
        readScreenError = ""
    }

    private fun startPayment(amount: String, subtotal: String? = null, tip: String? = null, tipRateBps: Int = 0) {
        if (nfcAdapter == null) return
        if (nfcAdapter?.isEnabled != true) return
        resetPaymentScreenState()
        paymentScreenAmount = amount
        paymentScreenSubtotal = subtotal ?: amount
        paymentScreenTip = tip ?: "0"
        paymentScreenTipRateBps = tipRateBps.coerceIn(0, 10000)
        paymentScreenPayee = BeamioWeb3Wallet.getAddress()
        paymentChargeBreakdownTaxPercent = dashboardInfraTaxPercent
        paymentChargeBreakdownTierDiscountPercent = null
        paymentScreenTableNumber = chargeTableNumber.trim()
        pendingScanAction = "payment"
        scanMethodState = "nfc"
        showScanMethodScreen = true
        syncScanMethodEntryAndTabs()
        // 与 executePayment 一致：终端 metadata 的 tax 可能比 Home 的 dashboard 更早可用，用于底部 Total Amount（含稅）
        Thread {
            try {
                val d = fetchTierRoutingDetailsForTerminalWalletSync(BeamioWeb3Wallet.getAddress()) ?: return@Thread
                val taxFromRouting = d.taxPercent
                runOnUiThread {
                    paymentChargeBreakdownTaxPercent = taxFromRouting
                    val sub = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                    val tipAmt = paymentScreenTip.toDoubleOrNull() ?: 0.0
                    val disc = paymentChargeBreakdownTierDiscountPercent
                    paymentScreenAmount = "%.2f".format(chargeTotalInCurrency(sub, taxFromRouting, disc, tipAmt))
                }
            } catch (_: Exception) {
            }
        }.start()
    }

    /** Charge 在选方式页 Smart Routing 失败后：NFC 回到等待贴卡；QR 留在 Scan QR 并打开相机扫码 */
    private fun retryChargeAfterScanMethodError() {
        if (pendingScanAction != "payment") return
        nfcFetchingInfo = false
        nfcFetchError = ""
        paymentQrInterpreting = false
        paymentQrParseError = ""
        if (scanMethodState == "qr") {
            scanWaitingForNfc = false
            paymentArmed = false
            paymentScreenUid = ""
            paymentScreenStatus = PaymentStatus.Waiting
            paymentScreenTxHash = ""
            paymentScreenError = ""
            paymentScreenRoutingSteps = emptyList()
            paymentScreenPreBalance = null
            paymentScreenPostBalance = null
            paymentScreenCardCurrency = null
            paymentScreenCardBackground = null
            paymentScreenCardImage = null
            paymentScreenCardName = null
            paymentScreenTierName = null
            paymentScreenCardType = null
            paymentScreenMemberNo = null
            paymentChargeBreakdownTierDiscountPercent = null
            scanMethodState = "qr"
            startEmbeddedQrScanning()
        } else {
            qrScanningActive = false
            scanMethodState = "nfc"
            paymentChargeBreakdownTierDiscountPercent = null
            armForNfcScan()
        }
    }

    private fun startEmbeddedQrScanning() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            qrScanningActive = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /** NFC 自动 arm：贴卡拉取中、支付路由中、充值执行中等不重复 arm，避免打断进行中的流程 */
    private fun shouldDeferAutoNfcArm(): Boolean {
        if (nfcFetchingInfo) return true
        if (pendingScanAction == "linkApp" && linkAppDeepLinkUrl.isNotEmpty()) return true
        if (pendingScanAction == "topup" && (topupQrSigningInProgress || topupNfcExecuteInProgress)) return true
        if (topupQrExecuteError.isNotEmpty()) return true
        if (pendingScanAction == "payment") {
            when (paymentScreenStatus) {
                is PaymentStatus.Routing,
                is PaymentStatus.Submitting,
                is PaymentStatus.Refreshing,
                is PaymentStatus.Success,
                is PaymentStatus.Error -> return true
                else -> { }
            }
        }
        return false
    }

    /** QR 自动开相机：解析中/支付路由中等不抢焦点 */
    private fun shouldDeferAutoQrCamera(): Boolean {
        if (pendingScanAction == "topup" && (topupQrSigningInProgress || topupNfcExecuteInProgress)) return true
        if (topupQrExecuteError.isNotEmpty()) return true
        if (pendingScanAction == "payment") {
            if (paymentQrParseError.isNotEmpty()) return true
            if (paymentQrInterpreting) return true
            when (paymentScreenStatus) {
                is PaymentStatus.Routing,
                is PaymentStatus.Submitting,
                is PaymentStatus.Refreshing,
                is PaymentStatus.Success,
                is PaymentStatus.Error -> return true
                else -> { }
            }
        }
        return false
    }

    /** 进入选方式页或切换 Tap Card / Scan QR：无需点中央即可 NFC 动画+监听或打开扫码相机 */
    private fun syncScanMethodEntryAndTabs() {
        if (!showScanMethodScreen) return
        when (scanMethodState) {
            "nfc" -> {
                if (shouldDeferAutoNfcArm()) return
                qrScanningActive = false
                armForNfcScan()
            }
            "qr" -> {
                if (shouldDeferAutoQrCamera()) return
                scanWaitingForNfc = false
                startEmbeddedQrScanning()
            }
        }
    }

    /** 中央区域拉取失败（NFC/QR 读余额等）后点击重试：读余额 + QR 时重新开相机，其余与 NFC arm 一致 */
    private fun retryCentralFetchAfterError() {
        when (pendingScanAction) {
            "read" -> {
                if (scanMethodState == "qr") {
                    nfcFetchError = ""
                    readQrDecodeResetKey++
                    startEmbeddedQrScanning()
                } else {
                    armForNfcScan()
                }
            }
            "linkApp" -> {
                linkAppDeepLinkUrl = ""
                nfcFetchError = ""
                linkAppLastSunForCancel = null
                armForNfcScan()
            }
            else -> armForNfcScan()
        }
    }

    /** NFC 模式下：留在本页，显示扫描动画，等待贴卡获得 UID */
    private fun armForNfcScan() {
        scanWaitingForNfc = true
        nfcFetchError = ""
        when (pendingScanAction) {
            "read" -> {
                readScreenUid = ""
                readScreenWallet = null
                readScreenStatus = ReadStatus.Loading
                readScreenAssets = null
                readScreenRawJson = null
                readScreenError = ""
                readArmed = true
                initArmed = false
                initTemplateArmed = false
                paymentArmed = false
                topupArmed = false
            }
            "topup" -> {
                topupViaQr = false
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
                topupScreenTierName = null
                topupScreenTierDescription = null
                topupArmed = true
                readArmed = false
                initArmed = false
                paymentArmed = false
            }
            "linkApp" -> {
                linkAppDeepLinkUrl = ""
                linkAppLastSunForCancel = null
                linkAppCancelInProgress = false
                linkAppArmed = true
                readArmed = false
                initArmed = false
                initTemplateArmed = false
                paymentArmed = false
                topupArmed = false
            }
            "payment" -> {
                paymentScreenUid = ""
                // Stay in Waiting until a tag is read; Routing triggers Smart Routing UI and hides scanWaitingForNfc.
                paymentScreenStatus = PaymentStatus.Waiting
                paymentScreenTxHash = ""
                paymentScreenError = ""
                paymentScreenRoutingSteps = emptyList()
                paymentScreenPreBalance = null
                paymentScreenPostBalance = null
                paymentScreenCardCurrency = null
                paymentScreenCardBackground = null
                paymentScreenCardImage = null
                paymentScreenCardName = null
                paymentScreenTierName = null
                paymentScreenCardType = null
                paymentArmed = true
                readArmed = false
                initArmed = false
                topupArmed = false
            }
            else -> { scanWaitingForNfc = false }
        }
    }

    private fun proceedFromScanMethod() {
        if (pendingScanAction == "topup") {
            scanWaitingForNfc = false
            topupViaQr = false
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
            topupScreenTierName = null
            topupScreenTierDescription = null
            topupArmed = true
            readArmed = false
            initArmed = false
            paymentArmed = false
            return
        }
        showScanMethodScreen = false
        scanWaitingForNfc = false
        when (pendingScanAction) {
            "read" -> {
                readViaQr = false
                showReadScreen = true
                readScreenUid = ""
                readScreenWallet = null
                readScreenStatus = ReadStatus.Waiting
                readScreenAssets = null
                readScreenRawJson = null
                readScreenError = ""
                readArmed = true
                initArmed = false
                initTemplateArmed = false
                paymentArmed = false
                topupArmed = false
                uidText = "Please tap card to read UID..."
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
        showTipScreen = false
        showScanMethodScreen = false
        chargeTableNumber = ""
        resetPaymentScreenState()
        // Done → 返回 home (NdefScreen)
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

    private fun executeNfcTopup(tag: Tag, uid: String, amount: String, fromScanMethodFlow: Boolean = false) {
        Thread {
            try {
                Log.d("Topup", "[Topup Debug] executeNfcTopup entry: panel ID address=${BeamioWeb3Wallet.getAddress()} | uid=$uid fromScanMethodFlow=$fromScanMethodFlow")
                val sunParams = readSunParamsFromNdef(tag)
                // NFC 格式（14 位 hex uid）必须提供 SUN 参数，不符合 SUN 或无法推导 tagID 的不予受理
                val isNfcUid = uid.length == 14 && uid.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                if (isNfcUid && sunParams == null) {
                    val err = "Card does not support SUN. Cannot top up."
                    runOnUiThread {
                        topupNfcExecuteInProgress = false
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = err
                        if (fromScanMethodFlow) {
                            nfcFetchingInfo = false
                            nfcFetchError = err
                        }
                    }
                    return@Thread
                }
                // 1. Topup 之前先拉取余额，对齐后端返回码（避免 UID/QR 混淆时解析 HTML 报错）
                val preAssets = fetchUidAssetsSync(sunParams?.uid ?: uid, sunParams)
                if (preAssets == null || !preAssets.ok) {
                    val errMsg = preAssets?.error ?: "Query failed"
                    runOnUiThread {
                        topupNfcExecuteInProgress = false
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = errMsg
                        if (fromScanMethodFlow) {
                            nfcFetchingInfo = false
                            nfcFetchError = errMsg
                        }
                    }
                    return@Thread
                }
                val amountCad = amount.toDoubleOrNull() ?: 0.0
                if (!hasMembershipNft(preAssets) && amountCad < 50.0) {
                    val errMsg = "Top-up not allowed without membership card. Please purchase a card first."
                    runOnUiThread {
                        topupNfcExecuteInProgress = false
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = errMsg
                        if (fromScanMethodFlow) {
                            nfcFetchingInfo = false
                            nfcFetchError = errMsg
                        }
                    }
                    return@Thread
                }

                val prepare = nfcTopupPrepare(uid, amount, sunParams)
                if (prepare.error != null) {
                    runOnUiThread {
                        topupNfcExecuteInProgress = false
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = prepare.error!!
                        if (fromScanMethodFlow) {
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
                val memberNo = memberNoFromCardItem(topupCard)
                runOnUiThread {
                    topupScreenPreBalance = preBalanceStr
                    topupScreenCardCurrency = preCurrency
                    topupScreenAddress = preAssets.address
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierName = topupCard?.tierName
                    topupScreenTierDescription = topupCard?.tierDescription
                    topupScreenMemberNo = memberNo.ifEmpty { null }
                }

                // panel ID address = 全局 POS 钱包，topup 签字必须使用此地址（BeamioWeb3Wallet 唯一真相来源）
                val panelIdAddress = BeamioWeb3Wallet.getAddress()
                Log.d("Topup", "[Topup Debug] NFC topup: panel ID address (signer)=$panelIdAddress | cardAddr=${prepare.cardAddr}")

                val adminSig = BeamioWeb3Wallet.signExecuteForAdmin(
                    prepare.cardAddr!!,
                    prepare.data!!,
                    prepare.deadline!!,
                    prepare.nonce!!
                )
                val result = nfcTopup(uid, amount, prepare.cardAddr!!, prepare.data!!, prepare.deadline!!, prepare.nonce!!, adminSig, sunParams)
                val keepOnScanUntilSuccess = fromScanMethodFlow
                runOnUiThread {
                    nfcFetchingInfo = false
                    if (result.success) {
                        topupScreenPostBalance = null
                        topupScreenTxHash = result.txHash ?: ""
                        topupScreenError = ""
                        topupScreenUid = uid
                        topupViaQr = false
                        if (keepOnScanUntilSuccess) {
                            topupNfcExecuteInProgress = true
                        } else {
                            showTopupScreen = true
                        }
                        topupScreenStatus = TopupStatus.Loading
                        Thread {
                            Thread.sleep(3000)
                            val postAssets = fetchUidAssetsSync(sunParams?.uid ?: uid, sunParams)
                            runOnUiThread {
                                if (postAssets != null && postAssets.ok) {
                                    val postCard = postAssets.cards?.firstOrNull { it.cardAddress.equals(cardAddr, ignoreCase = true) }
                                    val newMemberNo = memberNoFromCardItem(postCard)
                                    if (newMemberNo.isNotEmpty()) {
                                        topupScreenMemberNo = newMemberNo
                                    }
                                    topupScreenCardBackground = postCard?.cardBackground ?: topupScreenCardBackground
                                    topupScreenCardImage = postCard?.cardImage ?: topupScreenCardImage
                                    topupScreenTierName = postCard?.tierName ?: topupScreenTierName
                                    topupScreenTierDescription = postCard?.tierDescription ?: topupScreenTierDescription
                                    topupScreenPostBalance = postCard?.points ?: postAssets.points ?: "—"
                                } else {
                                    topupScreenPostBalance = "—"
                                }
                                topupScreenStatus = TopupStatus.Success
                                if (keepOnScanUntilSuccess) {
                                    topupNfcExecuteInProgress = false
                                    showScanMethodScreen = false
                                    showTopupScreen = true
                                }
                                scheduleCardStatsRefetchUntilChanged(expectChargeChange = false, expectTopUpChange = true)
                            }
                        }.start()
                    } else {
                        val errMsg = result.error ?: "Top-up failed"
                        topupNfcExecuteInProgress = false
                        topupScreenStatus = TopupStatus.Error
                        topupScreenError = errMsg
                        if (fromScanMethodFlow) {
                            nfcFetchingInfo = false
                            nfcFetchError = errMsg
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executeNfcTopup", e)
                runOnUiThread {
                    val errMsg = e.message ?: "Execution failed"
                    topupNfcExecuteInProgress = false
                    topupScreenStatus = TopupStatus.Error
                    topupScreenError = errMsg
                    if (fromScanMethodFlow) {
                        nfcFetchingInfo = false
                        nfcFetchError = errMsg
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
        val error: String?,
        val wallet: String? = null
    )

    private fun nfcTopupPrepare(uid: String, amount: String, sunParams: SunParams? = null): NfcTopupPrepareResult =
        nfcTopupPrepareInternal(uid = uid, wallet = null, beamioTag = null, amount = amount, sunParams = sunParams)

    private fun nfcTopupPrepareWithWallet(wallet: String, amount: String): NfcTopupPrepareResult =
        nfcTopupPrepareInternal(uid = null, wallet = wallet, beamioTag = null, amount = amount, sunParams = null)

    private fun nfcTopupPrepareWithBeamioTag(beamioTag: String, amount: String): NfcTopupPrepareResult =
        nfcTopupPrepareInternal(uid = null, wallet = null, beamioTag = beamioTag, amount = amount, sunParams = null)

    private fun nfcTopupPrepareInternal(uid: String?, wallet: String?, beamioTag: String?, amount: String, sunParams: SunParams? = null): NfcTopupPrepareResult {
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
            if (beamioTag != null) put("beamioTag", beamioTag)
            sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
            put("amount", amount)
            put("currency", "CAD")
            put("cardAddress", BEAMIO_USER_CARD_ASSET_ADDRESS)
            // 显式声明 Android 走 admin topup（mintPointsByAdmin）路径，不走 USDC 购买工作流
            put("workflow", "adminTopup")
            put("topupMode", "admin")
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
                error = null,
                wallet = root.optString("wallet").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) {
            NfcTopupPrepareResult(null, null, null, null, "API response error. Please check network", null)
        }
    }

    private fun executeTopupWithBeamioTag(beamioTag: String, amount: String) {
        Thread {
            try {
                Log.d("Topup", "[Topup Debug] executeTopupWithBeamioTag entry: panel ID address=${BeamioWeb3Wallet.getAddress()} | beamioTag=$beamioTag")
                val prepare = nfcTopupPrepareWithBeamioTag(beamioTag, amount)
                if (prepare.error != null) {
                    runOnUiThread { applyTopupQrScanScreenError(prepare.error!!) }
                    return@Thread
                }
                val wallet = prepare.wallet
                if (wallet == null) {
                    runOnUiThread { applyTopupQrScanScreenError("Server did not return wallet. Please retry") }
                    return@Thread
                }
                runOnUiThread { topupScreenWallet = wallet }
                executeWalletTopupInternal(wallet, amount, prepare)
            } catch (e: Exception) {
                Log.e("MainActivity", "executeTopupWithBeamioTag", e)
                runOnUiThread { applyTopupQrScanScreenError(e.message ?: "Execution failed") }
            }
        }.start()
    }

    private fun executeWalletTopup(wallet: String, amount: String) {
        Thread {
            try {
                Log.d("Topup", "[Topup Debug] executeWalletTopup entry: panel ID address=${BeamioWeb3Wallet.getAddress()} | customerWallet=$wallet")
                val prepare = nfcTopupPrepareWithWallet(wallet, amount)
                if (prepare.error != null) {
                    runOnUiThread { applyTopupQrScanScreenError(prepare.error!!) }
                    return@Thread
                }
                executeWalletTopupInternal(wallet, amount, prepare)
            } catch (e: Exception) {
                Log.e("MainActivity", "executeWalletTopup", e)
                runOnUiThread { applyTopupQrScanScreenError(e.message ?: "Execution failed") }
            }
        }.start()
    }

    private fun executeWalletTopupInternal(wallet: String, amount: String, prepare: NfcTopupPrepareResult) {
        try {
                // 1. Topup 之前先拉取余额，对齐后端 400/404/500 返回码
                val preAssets = fetchWalletAssetsSync(wallet)
                if (preAssets == null || !preAssets.ok) {
                    runOnUiThread { applyTopupQrScanScreenError(preAssets?.error ?: "Query failed") }
                    return
                }
                val amountCad = amount.toDoubleOrNull() ?: 0.0
                if (!hasMembershipNft(preAssets) && amountCad < 50.0) {
                    runOnUiThread {
                        applyTopupQrScanScreenError("Top-up not allowed without membership card. Please purchase a card first.")
                    }
                    return
                }
                // 多卡时使用 topup 所使用的卡的余额与货币，而非首卡/CCSA
                val cardAddr = prepare.cardAddr!!
                val topupCard = preAssets.cards?.firstOrNull { it.cardAddress.equals(cardAddr, ignoreCase = true) }
                val preBalanceStr = topupCard?.points ?: preAssets.points ?: "0"
                val preCurrency = topupCard?.cardCurrency ?: preAssets.cardCurrency ?: "CAD"
                val memberNo = memberNoFromCardItem(topupCard)
                runOnUiThread {
                    topupScreenPreBalance = preBalanceStr
                    topupScreenCardCurrency = preCurrency
                    topupScreenAddress = preAssets.address
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierName = topupCard?.tierName
                    topupScreenTierDescription = topupCard?.tierDescription
                    topupScreenMemberNo = memberNo.ifEmpty { null }
                }

                // panel ID address = 全局 POS 钱包，topup 签字必须使用此地址（BeamioWeb3Wallet 唯一真相来源）
                val panelIdAddress = BeamioWeb3Wallet.getAddress()
                Log.d("Topup", "[Topup Debug] Wallet topup: panel ID address (signer)=$panelIdAddress | customerWallet=$wallet | cardAddr=${prepare.cardAddr}")

                val adminSig = BeamioWeb3Wallet.signExecuteForAdmin(
                    prepare.cardAddr!!,
                    prepare.data!!,
                    prepare.deadline!!,
                    prepare.nonce!!
                )
                val result = nfcTopupWithWallet(wallet, prepare.cardAddr!!, prepare.data!!, prepare.deadline!!, prepare.nonce!!, adminSig)
                runOnUiThread {
                    if (result.success) {
                        topupScreenPostBalance = null
                        topupScreenTxHash = result.txHash ?: ""
                        topupScreenError = ""
                        topupQrExecuteError = ""
                        topupScreenStatus = TopupStatus.Loading
                        Thread {
                            Thread.sleep(3000)
                            val postAssets = fetchWalletAssetsSync(wallet)
                            runOnUiThread {
                                if (postAssets != null && postAssets.ok) {
                                    val postCard = postAssets.cards?.firstOrNull { it.cardAddress.equals(cardAddr, ignoreCase = true) }
                                    val newMemberNo = memberNoFromCardItem(postCard)
                                    if (newMemberNo.isNotEmpty()) {
                                        topupScreenMemberNo = newMemberNo
                                    }
                                    topupScreenCardBackground = postCard?.cardBackground ?: topupScreenCardBackground
                                    topupScreenCardImage = postCard?.cardImage ?: topupScreenCardImage
                                    topupScreenTierName = postCard?.tierName ?: topupScreenTierName
                                    topupScreenTierDescription = postCard?.tierDescription ?: topupScreenTierDescription
                                    topupScreenPostBalance = postCard?.points ?: postAssets.points ?: "—"
                                } else {
                                    topupScreenPostBalance = "—"
                                }
                                topupScreenStatus = TopupStatus.Success
                                scheduleCardStatsRefetchUntilChanged(expectChargeChange = false, expectTopUpChange = true)
                            }
                        }.start()
                    } else {
                        applyTopupQrScanScreenError(result.error ?: "Top-up failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("MainActivity", "executeWalletTopupInternal", e)
            runOnUiThread { applyTopupQrScanScreenError(e.message ?: "Execution failed") }
        }
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
            put("workflow", "adminTopup")
            put("topupMode", "admin")
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
            NfcTopupResult(success = false, txHash = null, error = "API response error. Please check network")
        }
    }

    private data class NfcTopupResult(val success: Boolean, val txHash: String?, val error: String?)

    private fun nfcTopup(uid: String, amount: String, cardAddr: String, data: String, deadline: Long, nonce: String, adminSignature: String, sunParams: SunParams? = null): NfcTopupResult {
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
            sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
            put("workflow", "adminTopup")
            put("topupMode", "admin")
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
            NfcTopupResult(success = false, txHash = null, error = "API response error. Please check network")
        }
    }

    /** Oracle 汇率：1 USDC = rate 该货币。与 x402sdk util.ts 一致：usdcad, usdeur, usdjpy 等 */
    private data class OracleRates(
        val usdcad: Double = 1.35,
        val usdeur: Double = 0.92,
        val usdjpy: Double = 150.0,
        val usdcny: Double = 7.2,
        val usdhkd: Double = 7.8,
        val usdsgd: Double = 1.35,
        val usdtwd: Double = 31.0
    )

    /** 从 API 获取完整 oracle，失败则用默认汇率 */
    private fun fetchOracle(): OracleRates {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getOracle")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val json = conn.inputStream.use { it.bufferedReader().readText() }
            conn.disconnect()
            val root = org.json.JSONObject(json)
            fun rate(key: String, default: Double) = root.optString(key).toDoubleOrNull()?.takeIf { it > 0 } ?: default
            OracleRates(
                usdcad = rate("usdcad", 1.35),
                usdeur = rate("usdeur", 0.92),
                usdjpy = rate("usdjpy", 150.0),
                usdcny = rate("usdcny", 7.2),
                usdhkd = rate("usdhkd", 7.8),
                usdsgd = rate("usdsgd", 1.35),
                usdtwd = rate("usdtwd", 31.0)
            )
        } catch (_: Exception) {
            OracleRates()
        }
    }

    /** 按 currency 取 oracle 汇率：1 USDC = rate 该货币。USD/USDC 为 1.0 */
    private fun getRateForCurrency(currency: String, oracle: OracleRates): Double {
        return when (currency.uppercase()) {
            "CAD" -> oracle.usdcad
            "USD", "USDC" -> 1.0
            "EUR" -> oracle.usdeur
            "JPY" -> oracle.usdjpy
            "CNY" -> oracle.usdcny
            "HKD" -> oracle.usdhkd
            "SGD" -> oracle.usdsgd
            "TWD" -> oracle.usdtwd
            else -> oracle.usdcad
        }
    }

    /** 金额(某货币) 转 USDC6：amount / rate * 1e6。用户输入 CAD 时用 getRateForCurrency("CAD", oracle) */
    private fun currencyToUsdc6(amount: Double, currency: String, oracle: OracleRates): String {
        if (amount <= 0) return "0"
        val rate = getRateForCurrency(currency, oracle)
        if (rate <= 0) return "0"
        val usdcAmount = amount / rate
        return (usdcAmount * 1_000_000).toLong().toString()
    }

    /** 与 BeamioERC1155Logic.ISSUED_NFT_START_ID 对齐：membership SBT ids [100, 100000000000) */
    private val ISSUED_NFT_START_ID = 100_000_000_000L

    /** 检查用户是否拥有会员卡 NFT（tokenId > 0 且 < ISSUED_NFT_START_ID） */
    private fun hasMembershipNft(assets: UIDAssets): Boolean {
        val allNfts = (assets.cards?.flatMap { it.nfts } ?: emptyList()) + (assets.nfts ?: emptyList())
        return allNfts.any { nft ->
            val id = nft.tokenId.toLongOrNull() ?: 0L
            id > 0 && id < ISSUED_NFT_START_ID
        }
    }

    /** 从 UIDAssets 计算总余额（CAD） */
    private fun totalBalanceCadFromAssets(assets: UIDAssets, oracle: OracleRates): Double {
        val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
        val cardsValueUsdc6 = assets.cards?.sumOf { card ->
            points6ToUsdc6(card.points6.toLongOrNull() ?: 0L, card.cardCurrency, oracle)
        } ?: run {
            val pts6 = assets.points6?.toLongOrNull() ?: 0L
            val cur = assets.cardCurrency ?: "CAD"
            points6ToUsdc6(pts6, cur, oracle)
        }
        val totalBalance6 = usdcBalance6.toLong() + cardsValueUsdc6
        return (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
    }

    /** 卡内 points6 按 cardCurrency 折算为 USDC6。points6 为 E6 格式 */
    private fun points6ToUsdc6(points6: Long, cardCurrency: String, oracle: OracleRates): Long {
        if (points6 <= 0) return 0L
        val rate = getRateForCurrency(cardCurrency, oracle)
        if (rate <= 0) return 0L
        return (points6.toDouble() / rate).toLong()
    }

    private fun executePayment(tag: Tag, uid: String, payee: String) {
        Thread {
            try {
                val sunParams = readSunParamsFromNdef(tag)
                val effectiveUid = sunParams?.uid ?: uid
                val requestEarly = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                if (requestEarly <= 0) {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Invalid amount"
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = "Invalid amount" }
                    }
                    return@Thread
                }
                val oracle = fetchOracle()
                var steps = createRoutingSteps()
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Routing
                    paymentScreenRoutingSteps = updateStep(steps, "detectingUser", StepStatus.loading)
                }
                steps = updateStep(steps, "detectingUser", StepStatus.success, "NFC card detected")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "membership", StepStatus.loading) }
                steps = updateStep(steps, "membership", StepStatus.success, "NFC card payment")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                val assets = fetchUidAssetsSync(effectiveUid, sunParams)
                if (assets == null || !assets.ok) {
                    steps = updateStep(steps, "analyzingAssets", StepStatus.error, assets?.error ?: "Card not registered")
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = assets?.error ?: "Card not registered"
                        paymentScreenRoutingSteps = steps
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = assets?.error ?: "Card not registered" }
                    }
                    return@Thread
                }
                steps = updateStep(steps, "analyzingAssets", StepStatus.success, "Card + USDC balance")
                val paymentMemberNo = memberNoPrimaryFromSortedCardsItem(assets)
                val paymentCard = assets.cards?.firstOrNull()
                val payeeWallet = BeamioWeb3Wallet.getAddress()
                val routingDetails = fetchTierRoutingDetailsForTerminalWalletSync(payeeWallet)
                val subtotalPay = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                val taxPctResolved = routingDetails?.taxPercent ?: (dashboardInfraTaxPercent ?: 0.0)
                val tierDiscPct = pickTierDiscountPercentFromAssets(assets, routingDetails?.discountByTierKey ?: emptyMap())
                val taxAmtCad = subtotalPay * taxPctResolved / 100.0
                val discAmtCad = subtotalPay * tierDiscPct / 100.0
                val taxFiat6Str = kotlin.math.round(taxAmtCad * 1_000_000.0).toLong().toString()
                val discFiat6Str = kotlin.math.round(discAmtCad * 1_000_000.0).toLong().toString()
                val taxBpsResolved = kotlin.math.round(taxPctResolved * 100.0).toInt().coerceIn(0, 10000)
                val discBpsResolved = (tierDiscPct * 100).coerceIn(0, 10000)
                val tipPay = chargeTipFromRequestAndBps(subtotalPay, paymentScreenTipRateBps)
                val tipStrForNfc = "%.2f".format(tipPay)
                val payCurrency = paymentCard?.cardCurrency ?: assets.cardCurrency ?: "CAD"
                val totalCurrency = chargeTotalInCurrency(subtotalPay, taxPctResolved, tierDiscPct, tipPay)
                val amountUsdc6 = currencyToUsdc6(totalCurrency, payCurrency, oracle)
                if (amountUsdc6 == "0") {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Amount conversion failed"
                        paymentScreenRoutingSteps = steps
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = "Amount conversion failed" }
                    }
                    return@Thread
                }
                runOnUiThread {
                    paymentChargeBreakdownTaxPercent = taxPctResolved
                    paymentChargeBreakdownTierDiscountPercent = tierDiscPct
                    paymentScreenTip = tipStrForNfc
                    paymentScreenAmount = "%.2f".format(totalCurrency)
                    paymentScreenCardCurrency = payCurrency
                    paymentScreenMemberNo = paymentMemberNo.ifEmpty { null }
                    paymentScreenCardBackground = paymentCard?.cardBackground
                    paymentScreenCardImage = paymentCard?.cardImage
                    paymentScreenCardName = paymentCard?.cardName
                    paymentScreenTierName = paymentCard?.tierName
                    paymentScreenCardType = paymentCard?.cardType
                    paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading)
                }
                steps = updateStep(steps, "optimizingRoute", StepStatus.success, "Direct: NFC → Merchant")
                // 总余额 = USDC + 各卡 points 按 cardCurrency 折算为 USDC（依据 oracle）
                val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
                val cardsValueUsdc6 = assets.cards?.sumOf { card ->
                    points6ToUsdc6(card.points6.toLongOrNull() ?: 0L, card.cardCurrency, oracle)
                } ?: run {
                    val pts6 = assets.points6?.toLongOrNull() ?: 0L
                    val cur = assets.cardCurrency ?: "CAD"
                    points6ToUsdc6(pts6, cur, oracle)
                }
                val totalBalance6 = usdcBalance6.toLong() + cardsValueUsdc6
                // 初始余额（CAD）：总 USDC6 转 CAD，供 postBalance = pre - subtotal - tip
                val totalBalanceCad = (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
                runOnUiThread {
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                }
                val required6 = amountUsdc6.toLongOrNull() ?: 0L
                if (totalBalance6 < required6) {
                    steps = updateStep(steps, "analyzingAssets", StepStatus.error, "Insufficient balance")
                    val shortfall6 = (required6 - totalBalance6).coerceAtLeast(0L)
                    val errMsg = "Insufficient balance (need ${String.format("%.2f", required6 / 1_000_000.0)} USDC, total assets ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC, shortfall ${String.format("%.2f", shortfall6 / 1_000_000.0)} USDC)"
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
                val result = payByNfcUidWithContainer(
                    effectiveUid,
                    amountUsdc6,
                    payee,
                    assets,
                    oracle,
                    sunParams,
                    nfcSubtotalCurrencyAmount = paymentScreenSubtotal,
                    nfcTipCurrencyAmount = tipStrForNfc,
                    nfcTipRateBps = paymentScreenTipRateBps,
                    nfcRequestCurrency = payCurrency,
                    nfcTaxAmountFiat6 = taxFiat6Str,
                    nfcTaxRateBps = taxBpsResolved,
                    nfcDiscountAmountFiat6 = discFiat6Str,
                    nfcDiscountRateBps = discBpsResolved
                )
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "Payment failed"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                if (!result.success) {
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenRoutingSteps = steps
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = result.error ?: "Payment failed"
                        if (fromScan) { nfcFetchingInfo = false; nfcFetchError = result.error ?: "Payment failed" }
                    }
                } else {
                    steps = updateStep(steps, "refreshBalance", StepStatus.loading, "Fetching latest balance")
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        paymentScreenRoutingSteps = steps
                        paymentScreenPostBalance = null
                        paymentScreenStatus = PaymentStatus.Submitting
                        paymentScreenTxHash = result.txHash ?: ""
                        paymentScreenError = ""
                        if (fromScan) {
                            paymentViaQr = false
                            nfcFetchingInfo = false
                        }
                    }
                    Thread {
                        Thread.sleep(3000)
                        val postAssets = fetchUidAssetsSync(effectiveUid, sunParams)
                        val stepsAfter = updateStep(
                            steps,
                            "refreshBalance",
                            StepStatus.success,
                            if (postAssets != null && postAssets.ok) "Updated" else "Unavailable"
                        )
                        runOnUiThread {
                            paymentScreenRoutingSteps = stepsAfter
                            if (postAssets != null && postAssets.ok) {
                                paymentScreenPostBalance = "%.2f".format(totalBalanceCadFromAssets(postAssets, oracle))
                            } else {
                                paymentScreenPostBalance = "—"
                            }
                            paymentScreenStatus = PaymentStatus.Success
                            scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                        }
                    }.start()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executePayment", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    paymentScreenStatus = PaymentStatus.Error
                    paymentScreenError = e.message ?: "Execution failed"
                    if (fromScan) { nfcFetchingInfo = false; nfcFetchError = e.message ?: "Execution failed" }
                }
            }
        }.start()
    }

    /** QR 支付：与 SilentPassUI 一致。商户金额 → USDC6，链上查询客户资产，Smart Routing 组装 items，POST /api/AAtoEOA */
    private fun executeQrPayment(payload: org.json.JSONObject) {
        Thread {
            try {
                runOnUiThread {
                    nfcFetchingInfo = true
                    paymentScreenStatus = PaymentStatus.Routing
                    paymentScreenRoutingSteps = createRoutingSteps()
                }
                var steps = createRoutingSteps()
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "detectingUser", StepStatus.loading) }
                val account = payload.optString("account", "")
                val payeeFromQr = payload.optString("to", "")
                if (account.isEmpty()) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Invalid payment code"
                    }
                    return@Thread
                }
                val requestQrEarly = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                if (requestQrEarly <= 0) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Please enter amount first"
                    }
                    return@Thread
                }
                // openRelayed：签名不绑定 to/items，maxAmount=0 表示无限制，金额与收款方由商户填写。不在此处校验 maxAmount，链上会校验。
                steps = updateStep(steps, "detectingUser", StepStatus.success, "Dynamic QR detected")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "membership", StepStatus.loading) }
                val assets = fetchWalletAssetsSync(account)
                if (assets == null || !assets.ok) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = assets?.error ?: "Unable to fetch customer assets"
                    }
                    return@Thread
                }
                val oracle = fetchOracle()
                val payeeWalletTerminal = BeamioWeb3Wallet.getAddress()
                val routingDetailsQr = fetchTierRoutingDetailsForTerminalWalletSync(payeeWalletTerminal)
                val taxPctQr = routingDetailsQr?.taxPercent ?: (dashboardInfraTaxPercent ?: 0.0)
                val tierDiscQr = pickTierDiscountPercentFromAssets(assets, routingDetailsQr?.discountByTierKey ?: emptyMap())
                val requestQr = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                val tipQr = chargeTipFromRequestAndBps(requestQr, paymentScreenTipRateBps)
                val taxAmtCadQr = requestQr * taxPctQr / 100.0
                val discAmtCadQr = requestQr * tierDiscQr / 100.0
                val taxFiat6StrQr = kotlin.math.round(taxAmtCadQr * 1_000_000.0).toLong().toString()
                val discFiat6StrQr = kotlin.math.round(discAmtCadQr * 1_000_000.0).toLong().toString()
                val taxBpsQr = kotlin.math.round(taxPctQr * 100.0).toInt().coerceIn(0, 10000)
                val discBpsQr = (tierDiscQr * 100).coerceIn(0, 10000)
                // 与 NFC executePayment 一致：账单 request 币种取 cards 首张；tier 折扣仍用 pickTierDiscountPercentFromAssets(全卡 NFT)
                val paymentCardForQrCharge = assets.cards?.firstOrNull()
                val payCurrencyQr = paymentCardForQrCharge?.cardCurrency ?: assets.cardCurrency ?: "CAD"
                val totalCurrencyQr = chargeTotalInCurrency(requestQr, taxPctQr, tierDiscQr, tipQr)
                val enteredUsdc6 = currencyToUsdc6(totalCurrencyQr, payCurrencyQr, oracle).toLongOrNull() ?: 0L
                if (enteredUsdc6 <= 0) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Amount conversion failed"
                    }
                    return@Thread
                }
                val hasCardholder = (assets.cards?.any { it.cardType == "ccsa" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) } == true) ||
                    (assets.points6?.toLongOrNull() ?: 0L) > 0
                val effectiveUsdc6 = enteredUsdc6
                steps = updateStep(steps, "membership", StepStatus.success, if (hasCardholder) "Cardholder" else "No membership")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                // QR Charge：与 NFC payByNfcUidWithContainer 对齐——不按用户卡合约地址（如 CCSA 克隆 0x2032…）拆分 container；
                // 可扣款余额仍来自 chargeableCards，但 ERC1155 扣款 asset 固定为 BEAMIO_USER_CARD_ASSET_ADDRESS。
                val unitPriceStr = assets.unitPriceUSDC6
                val unitPriceUSDC6 = unitPriceStr?.toLongOrNull() ?: 0L
                val cards = chargeableCards(assets)
                val ccsaCards = cards.filter { it.cardType == "ccsa" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) }
                val infraCards = cards.filter { it.cardType == "infrastructure" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) }
                val ccsaPoints6 = ccsaCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val infraPoints6 = infraCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
                val ccsaValueUsdc6 = if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) (ccsaPoints6 * unitPriceUSDC6) / 1_000_000 else 0L
                val infraValueUsdc6 = infraCards.sumOf { c -> points6ToUsdc6(c.points6.toLongOrNull() ?: 0L, c.cardCurrency, oracle) }
                val totalBalance6 = ccsaValueUsdc6 + infraValueUsdc6 + usdcBalance6.toLong()
                if (totalBalance6 < effectiveUsdc6) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Insufficient customer balance (need ${String.format("%.2f", effectiveUsdc6 / 1_000_000.0)} USDC, total assets ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC)"
                    }
                    return@Thread
                }
                steps = updateStep(steps, "analyzingAssets", StepStatus.success,
                    if (ccsaValueUsdc6 >= effectiveUsdc6) "\$CCSA: (Sufficient)" else if (ccsaValueUsdc6 > 0) "\$CCSA: (Partial)" else "USDC sufficient")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading) }
                var remaining = effectiveUsdc6
                var ccsaPointsWei = 0L
                var infraPointsWei = 0L
                if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) {
                    val maxPointsFromAmount = (remaining * 1_000_000) / unitPriceUSDC6
                    ccsaPointsWei = minOf(maxPointsFromAmount, ccsaPoints6)
                    val ccsaValue = (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
                    remaining -= ccsaValue
                    // 与 NFC 一致：无 USDC 时若 CCSA 可向上取整覆盖全额，则收紧点数避免残留 USDC kind
                    if (usdcBalance6.toLong() == 0L && remaining > 0 && ccsaPoints6 > ccsaPointsWei) {
                        val ccsaPointsCeil = (effectiveUsdc6 * 1_000_000 + unitPriceUSDC6 - 1) / unitPriceUSDC6
                        if (ccsaPointsCeil <= ccsaPoints6) {
                            ccsaPointsWei = ccsaPointsCeil
                            remaining = effectiveUsdc6 - (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
                        }
                    }
                }
                if (remaining > 0 && infraPoints6 > 0 && infraCards.isNotEmpty()) {
                    val infraCard = infraCards.first()
                    val rate = getRateForCurrency(infraCard.cardCurrency, oracle)
                    if (rate > 0) {
                        val infraValueUsdc6Total = points6ToUsdc6(infraPoints6, infraCard.cardCurrency, oracle)
                        val infraValueUsdc6Needed = minOf(remaining, infraValueUsdc6Total)
                        infraPointsWei = kotlin.math.ceil(infraValueUsdc6Needed * rate).toLong().coerceIn(0L, infraPoints6)
                        remaining = (remaining - points6ToUsdc6(infraPointsWei, infraCard.cardCurrency, oracle)).coerceAtLeast(0L)
                        if (remaining > 0 && usdcBalance6.toLong() == 0L && infraPointsWei < infraPoints6) {
                            val extraPoints = kotlin.math.ceil(remaining * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                            infraPointsWei += extraPoints
                            remaining = 0L
                        }
                    }
                }
                var usdcWei = remaining.coerceAtLeast(0L)
                if (usdcWei > 0 && usdcBalance6.toLong() == 0L && infraPoints6 > 0 && infraCards.isNotEmpty()) {
                    val infraCard = infraCards.first()
                    val rate = getRateForCurrency(infraCard.cardCurrency, oracle)
                    if (rate > 0 && infraPointsWei < infraPoints6) {
                        val extraPoints = kotlin.math.ceil(usdcWei * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                        infraPointsWei += extraPoints
                        usdcWei = 0L
                    }
                }
                val composedItems = mutableListOf<Map<String, Any>>()
                if (usdcWei > 0) {
                    composedItems.add(mapOf("kind" to 0, "asset" to USDC_BASE, "amount" to usdcWei.toString(), "tokenId" to "0", "data" to "0x"))
                }
                // 与 NFC 相同 asset：合并为单条 kind 1，避免两条同 asset 的 container（与贴卡流程一致且利于预检）
                val beamio1155PointsWei = (ccsaPointsWei + infraPointsWei).coerceAtLeast(0L)
                if (beamio1155PointsWei > 0) {
                    composedItems.add(
                        mapOf(
                            "kind" to 1,
                            "asset" to BEAMIO_USER_CARD_ASSET_ADDRESS,
                            "amount" to beamio1155PointsWei.toString(),
                            "tokenId" to "0",
                            "data" to "0x"
                        )
                    )
                }
                if (composedItems.isEmpty()) {
                    composedItems.add(mapOf("kind" to 0, "asset" to USDC_BASE, "amount" to effectiveUsdc6.toString(), "tokenId" to "0", "data" to "0x"))
                }
                val itemsJson = org.json.JSONArray()
                composedItems.forEach { m -> itemsJson.put(org.json.JSONObject(m)) }
                val finalPayload = org.json.JSONObject(payload.toString())
                finalPayload.put("items", itemsJson)
                if (!finalPayload.has("maxAmount")) finalPayload.put("maxAmount", "0")
                val merchantAssets = fetchWalletAssetsSync(BeamioWeb3Wallet.getAddress())
                val toAddr = merchantAssets?.aaAddress?.takeIf { it.isNotEmpty() } ?: payeeFromQr
                if (toAddr.isEmpty()) {
                    runOnUiThread {
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Merchant AA not found. Please ensure terminal is configured."
                    }
                    return@Thread
                }
                // 新格式：若仅有 validBefore 无 deadline，用 validBefore 补全
                if (!finalPayload.has("deadline") && finalPayload.has("validBefore")) {
                    finalPayload.put("deadline", finalPayload.optString("validBefore"))
                }
                finalPayload.put("to", toAddr)
                steps = updateStep(
                    steps,
                    "optimizingRoute",
                    StepStatus.success,
                    when {
                        beamio1155PointsWei > 0 && usdcWei > 0 -> "Hybrid: points + USDC"
                        beamio1155PointsWei > 0 -> "Points only"
                        else -> "USDC only"
                    }
                )
                val qrPaymentMemberNo = memberNoPrimaryFromSortedCardsItem(assets)
                val totalBalanceCad = (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
                // 与 NFC 成功页一致：首张卡作为 Pass 展示来源（税/折扣记账与 NFC 同源）
                val qrPaymentCard = paymentCardForQrCharge
                runOnUiThread {
                    paymentChargeBreakdownTaxPercent = taxPctQr
                    paymentChargeBreakdownTierDiscountPercent = tierDiscQr
                    paymentScreenMemberNo = qrPaymentMemberNo.ifEmpty { null }
                    paymentScreenCardBackground = qrPaymentCard?.cardBackground
                    paymentScreenCardImage = qrPaymentCard?.cardImage
                    paymentScreenCardName = qrPaymentCard?.cardName
                    paymentScreenTierName = qrPaymentCard?.tierName
                    paymentScreenCardType = qrPaymentCard?.cardType
                    paymentScreenPayee = toAddr
                    paymentScreenTip = "%.2f".format(tipQr)
                    paymentScreenAmount = "%.2f".format(totalCurrencyQr)
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                    paymentScreenCardCurrency = payCurrencyQr
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val currencyAmountStr = "%.2f".format(totalCurrencyQr)
                // 与 payByNfcUidWithContainer 一致：小计/小费用屏幕原始字符串，税/折扣 fiat6+bps 与 NFC 相同算法
                val subtotalStrForBill = paymentScreenSubtotal.trim().ifEmpty { "%.2f".format(requestQr) }
                val tipStrForBill = "%.2f".format(tipQr)
                val qrChargeBill = OpenContainerChargeBillFields(
                    subtotal = subtotalStrForBill,
                    tip = tipStrForBill,
                    tipRateBps = paymentScreenTipRateBps,
                    requestCurrency = payCurrencyQr,
                    taxAmountFiat6 = taxFiat6StrQr,
                    taxRateBps = taxBpsQr,
                    discountAmountFiat6 = discFiat6StrQr,
                    discountRateBps = discBpsQr
                )
                val result = postAAtoEOAOpenContainer(
                    finalPayload,
                    listOf(payCurrencyQr),
                    listOf(currencyAmountStr),
                    qrChargeBill
                )
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "Payment failed"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                val customerAccountForPostBalance = account  // 显式捕获客户 AA，避免闭包歧义
                val preBalanceForCheck = totalBalanceCad
                // 扣款来源卡地址：成功页显示该卡余额（非总资产），与用户预期一致
                val deductedCardAddress = if (beamio1155PointsWei > 0) BEAMIO_USER_CARD_ASSET_ADDRESS else null
                if (!result.success) {
                    runOnUiThread {
                        paymentScreenRoutingSteps = steps
                        nfcFetchingInfo = false
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = result.error ?: "Payment failed"
                    }
                } else {
                    steps = updateStep(steps, "refreshBalance", StepStatus.loading, "Fetching latest balance")
                    runOnUiThread {
                        paymentScreenRoutingSteps = steps
                        nfcFetchingInfo = false
                        paymentScreenPostBalance = null
                        paymentScreenStatus = PaymentStatus.Submitting
                        paymentScreenTxHash = result.txHash ?: ""
                        paymentScreenError = ""
                        paymentViaQr = true
                    }
                    Thread {
                        // 等待 5 秒确保链上确认（Base ~2s/block），避免拿到扣款前状态
                        Thread.sleep(5000)
                        var postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true)
                        var postCad = postAssets?.let { a ->
                            if (!a.ok) null else {
                                val card = deductedCardAddress?.let { addr ->
                                    a.cards?.find { it.cardAddress.equals(addr, ignoreCase = true) }
                                }
                                if (card != null) {
                                    val pts6 = card.points6.toLongOrNull() ?: 0L
                                    (pts6 / 1_000_000.0) * getRateForCurrency("CAD", oracle) / getRateForCurrency(card.cardCurrency, oracle)
                                } else {
                                    totalBalanceCadFromAssets(a, oracle)
                                }
                            }
                        }
                        if (postCad != null && postCad >= preBalanceForCheck - 0.01 && totalCurrencyQr > 0.001) {
                            Thread.sleep(3000)
                            postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true)
                            postCad = postAssets?.let { a ->
                                if (!a.ok) null else {
                                    val card = deductedCardAddress?.let { addr ->
                                        a.cards?.find { it.cardAddress.equals(addr, ignoreCase = true) }
                                    }
                                    if (card != null) {
                                        val pts6 = card.points6.toLongOrNull() ?: 0L
                                        (pts6 / 1_000_000.0) * getRateForCurrency("CAD", oracle) / getRateForCurrency(card.cardCurrency, oracle)
                                    } else {
                                        totalBalanceCadFromAssets(a, oracle)
                                    }
                                }
                            }
                        }
                        val finalPostCad = postCad
                        val stepsAfter = updateStep(
                            steps,
                            "refreshBalance",
                            StepStatus.success,
                            if (postAssets != null && postAssets.ok && finalPostCad != null) "Updated" else "Unavailable"
                        )
                        runOnUiThread {
                            paymentScreenRoutingSteps = stepsAfter
                            if (postAssets != null && postAssets.ok && finalPostCad != null) {
                                paymentScreenPostBalance = "%.2f".format(finalPostCad)
                            } else {
                                paymentScreenPostBalance = "—"
                            }
                            paymentScreenStatus = PaymentStatus.Success
                            scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                        }
                    }.start()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "executeQrPayment", e)
                runOnUiThread {
                    nfcFetchingInfo = false
                    paymentScreenStatus = PaymentStatus.Error
                    paymentScreenError = e.message ?: "Execution failed"
                }
            }
        }.start()
    }

    private data class AAtoEOAResult(val success: Boolean, val txHash: String?, val error: String?)

    /** QR OpenContainer POST /api/AAtoEOA：与 payByNfcUidSignContainer 同源字段，供 Master 写 TX_TIP 与税/折扣 meta */
    private data class OpenContainerChargeBillFields(
        val subtotal: String,
        val tip: String,
        val tipRateBps: Int,
        val requestCurrency: String,
        val taxAmountFiat6: String,
        val taxRateBps: Int,
        val discountAmountFiat6: String,
        val discountRateBps: Int
    )

    private fun postAAtoEOAOpenContainer(
        payload: org.json.JSONObject,
        currency: List<String> = emptyList(),
        currencyAmount: List<String> = emptyList(),
        chargeBill: OpenContainerChargeBillFields? = null
    ): AAtoEOAResult {
        return try {
            val body = org.json.JSONObject().apply {
                put("openContainerPayload", payload)
                when {
                    currency.size > 1 && currencyAmount.size > 1 -> {
                        put("currency", org.json.JSONArray(currency))
                        put("currencyAmount", org.json.JSONArray(currencyAmount))
                    }
                    currency.isNotEmpty() && currencyAmount.isNotEmpty() -> {
                        put("currency", currency.first())
                        put("currencyAmount", currencyAmount.first())
                    }
                    else -> {
                        val items = payload.optJSONArray("items")
                        val firstAmount = items?.optJSONObject(0)?.optString("amount") ?: "0"
                        val amt = firstAmount.toLongOrNull() ?: 0L
                        put("currency", "USDC")
                        put("currencyAmount", "%.2f".format(amt / 1_000_000.0))
                    }
                }
                chargeBill?.let { b ->
                    // 与 payByNfcUidSignContainer JSON 规则一致（税/折扣/小费字段可选、bps 范围 0..10000）
                    b.subtotal.trim().takeIf { it.isNotEmpty() }?.let { put("nfcSubtotalCurrencyAmount", it) }
                    put("nfcRequestCurrency", b.requestCurrency.trim().ifEmpty { "CAD" })
                    b.taxAmountFiat6.trim().takeIf { it.isNotEmpty() }?.let { put("nfcTaxAmountFiat6", it) }
                    if (b.taxRateBps in 0..10000) put("nfcTaxRateBps", b.taxRateBps)
                    b.discountAmountFiat6.trim().takeIf { it.isNotEmpty() }?.let { put("nfcDiscountAmountFiat6", it) }
                    if (b.discountRateBps in 0..10000) put("nfcDiscountRateBps", b.discountRateBps)
                    val tipNum = b.tip.toDoubleOrNull() ?: 0.0
                    if (tipNum > 0.0) {
                        put("nfcTipCurrencyAmount", b.tip.trim())
                        if (b.tipRateBps > 0) put("nfcTipRateBps", b.tipRateBps)
                    }
                    put("merchantCardAddress", BEAMIO_USER_CARD_ASSET_ADDRESS)
                }
            }
            val url = java.net.URL("$BEAMIO_API/api/AAtoEOA")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 90000
            conn.outputStream.use { os -> os.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val root = org.json.JSONObject(resp)
            AAtoEOAResult(
                success = code in 200..299 && root.optBoolean("success", true),
                txHash = root.optString("USDC_tx").takeIf { it.isNotEmpty() },
                error = root.optString("error").takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "postAAtoEOAOpenContainer", e)
            AAtoEOAResult(false, null, e.message ?: "Request failed")
        }
    }

    /** SUN params from NDEF URL for getUIDAssets verification. */
    private data class SunParams(val uid: String, val e: String, val c: String, val m: String)

    /** Read uid,e,c,m from NDEF URL if card has valid SUN format. Returns null if NDEF missing or template (e/c/m all 0). */
    private fun readSunParamsFromNdef(tag: Tag): SunParams? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()
            val url = msg?.records?.firstNotNullOfOrNull { it.toUri()?.toString() } ?: return null
            val uri = Uri.parse(url)
            val uid = uri.getQueryParameter("uid")?.trim() ?: return null
            val e = uri.getQueryParameter("e")?.trim() ?: return null
            val c = uri.getQueryParameter("c")?.trim() ?: return null
            val m = uri.getQueryParameter("m")?.trim() ?: return null
            if (e.length != 64 || c.length != 6 || m.length != 16) return null
            if (!e.matches(Regex("^[0-9a-fA-F]+$")) || !c.matches(Regex("^[0-9a-fA-F]+$")) || !m.matches(Regex("^[0-9a-fA-F]+$"))) return null
            if (e.all { it == '0' } && c.all { it == '0' } && m.all { it == '0' }) return null
            SunParams(uid, e, c, m)
        } catch (_: Exception) {
            null
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private fun fetchUidAssetsSync(uid: String, sunParams: SunParams? = null): UIDAssets? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getUIDAssets")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply {
                put("uid", uid)
                sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
            }.toString()
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

    /** 可扣款卡：CCSA + 基础设施 (CashTrees) */
    private fun chargeableCards(assets: UIDAssets): List<CardItem> {
        return assets.cards?.filter {
            it.cardType == "ccsa" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) ||
            it.cardType == "infrastructure" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true)
        } ?: emptyList()
    }

    /** 新流程：Android 自行 Smart Routing 构建 container，服务端仅签名并 relay。CCSA + 基础设施卡均可扣款。NFC 格式(14 位 hex uid)时需传 sunParams(e,c,m) 做 SUN 校验。 */
    private fun payByNfcUidWithContainer(
        uid: String,
        amountUsdc6: String,
        payee: String,
        assets: UIDAssets,
        oracle: OracleRates,
        sunParams: SunParams? = null,
        nfcSubtotalCurrencyAmount: String? = null,
        nfcTipCurrencyAmount: String? = null,
        nfcTipRateBps: Int = 0,
        nfcRequestCurrency: String = "CAD",
        nfcTaxAmountFiat6: String? = null,
        nfcTaxRateBps: Int? = null,
        nfcDiscountAmountFiat6: String? = null,
        nfcDiscountRateBps: Int? = null
    ): PayByNfcResult {
        val amountBig = amountUsdc6.toLongOrNull() ?: 0L
        if (amountBig <= 0) return PayByNfcResult(false, null, "Invalid amountUsdc6")
        val prepare = payByNfcUidPrepare(uid, payee, amountUsdc6, sunParams)
        val ok = prepare["ok"] == true
        val account = prepare["account"] as? String
        val nonce = prepare["nonce"] as? String
        val deadline = prepare["deadline"] as? String
        val payeeAA = prepare["payeeAA"] as? String
        val unitPriceStr = prepare["unitPriceUSDC6"] as? String
        if (!ok || account == null || nonce == null || deadline == null || payeeAA == null || unitPriceStr == null) {
            return PayByNfcResult(false, null, (prepare["error"] as? String) ?: "Prepare failed")
        }
        val unitPriceUSDC6 = unitPriceStr.toLongOrNull() ?: 0L
        if (unitPriceUSDC6 <= 0) return PayByNfcResult(false, null, "Unit price 0")
        val cards = chargeableCards(assets)
        val ccsaCards = cards.filter { it.cardType == "ccsa" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) }
        val infraCards = cards.filter { it.cardType == "infrastructure" || it.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true) }
        val ccsaPoints6 = ccsaCards.sumOf { it.points6.toLongOrNull() ?: 0L }
        val infraPoints6 = infraCards.sumOf { it.points6.toLongOrNull() ?: 0L }
        val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
        val ccsaValueUsdc6 = if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) (ccsaPoints6 * unitPriceUSDC6) / 1_000_000 else 0L
        val infraValueUsdc6 = infraCards.sumOf { c -> points6ToUsdc6(c.points6.toLongOrNull() ?: 0L, c.cardCurrency, oracle) }
        val totalBalance6 = ccsaValueUsdc6 + infraValueUsdc6 + usdcBalance6.toLong()
        if (totalBalance6 < amountBig) {
            val shortfall6 = (amountBig - totalBalance6).coerceAtLeast(0L)
            return PayByNfcResult(false, null, "Insufficient balance (need ${String.format("%.2f", amountBig / 1_000_000.0)} USDC, total assets ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC, shortfall ${String.format("%.2f", shortfall6 / 1_000_000.0)} USDC)")
        }
        var remaining = amountBig
        var ccsaPointsWei = 0L
        var infraPointsWei = 0L
        if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) {
            val maxPointsFromAmount = (remaining * 1_000_000) / unitPriceUSDC6
            ccsaPointsWei = minOf(maxPointsFromAmount, ccsaPoints6)
            val ccsaValue = (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
            remaining -= ccsaValue
            if (usdcBalance6.toLong() == 0L && remaining > 0 && ccsaPoints6 > ccsaPointsWei) {
                val ccsaPointsCeil = (amountBig * 1_000_000 + unitPriceUSDC6 - 1) / unitPriceUSDC6
                if (ccsaPointsCeil <= ccsaPoints6) {
                    ccsaPointsWei = ccsaPointsCeil
                    remaining = amountBig - (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
                }
            }
        }
        if (remaining > 0 && infraPoints6 > 0 && infraCards.isNotEmpty()) {
            val infraCard = infraCards.first()
            val rate = getRateForCurrency(infraCard.cardCurrency, oracle)
            if (rate > 0) {
                val infraValueUsdc6Total = points6ToUsdc6(infraPoints6, infraCard.cardCurrency, oracle)
                val infraValueUsdc6Needed = minOf(remaining, infraValueUsdc6Total)
                // 向上取整，避免 rounding 导致 remaining 残留
                infraPointsWei = kotlin.math.ceil(infraValueUsdc6Needed * rate).toLong().coerceIn(0L, infraPoints6)
                remaining = (remaining - points6ToUsdc6(infraPointsWei, infraCard.cardCurrency, oracle)).coerceAtLeast(0L)
                // 无 USDC 余额时：将 rounding 残留吸收进 infra 点数，避免添加 kind 0 导致 Cluster 预检失败
                if (remaining > 0 && usdcBalance6.toLong() == 0L && infraPointsWei < infraPoints6) {
                    val extraPoints = kotlin.math.ceil(remaining * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                    infraPointsWei += extraPoints
                    remaining = 0L
                }
            }
        }
        var usdcWei = remaining.coerceAtLeast(0L)
        // 兜底：无 USDC 余额时，将任意残留（含 rounding）吸收进 infra，绝不添加 kind 0
        if (usdcWei > 0 && usdcBalance6.toLong() == 0L && infraPoints6 > 0 && infraCards.isNotEmpty()) {
            val infraCard = infraCards.first()
            val rate = getRateForCurrency(infraCard.cardCurrency, oracle)
            if (rate > 0 && infraPointsWei < infraPoints6) {
                val extraPoints = kotlin.math.ceil(usdcWei * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                infraPointsWei += extraPoints
                usdcWei = 0L
            }
        }
        val items = mutableListOf<Map<String, Any>>()
        if (usdcWei > 0) {
            items.add(mapOf(
                "kind" to 0,
                "asset" to USDC_BASE,
                "amount" to usdcWei.toString(),
                "tokenId" to "0",
                "data" to "0x"
            ))
        }
        if (ccsaPointsWei > 0) {
            items.add(mapOf(
                "kind" to 1,
                "asset" to BEAMIO_USER_CARD_ASSET_ADDRESS,
                "amount" to ccsaPointsWei.toString(),
                "tokenId" to "0",
                "data" to "0x"
            ))
        }
        if (infraPointsWei > 0) {
            items.add(mapOf(
                "kind" to 1,
                "asset" to BEAMIO_USER_CARD_ASSET_ADDRESS,
                "amount" to infraPointsWei.toString(),
                "tokenId" to "0",
                "data" to "0x"
            ))
        }
        if (items.isEmpty()) {
            items.add(mapOf(
                "kind" to 0,
                "asset" to USDC_BASE,
                "amount" to amountUsdc6,
                "tokenId" to "0",
                "data" to "0x"
            ))
        }
        val itemsJson = org.json.JSONArray()
        items.forEach { m -> itemsJson.put(org.json.JSONObject(m)) }
        val containerPayload = org.json.JSONObject().apply {
            put("account", account)
            put("to", payeeAA)
            put("items", itemsJson)
            put("nonce", nonce)
            put("deadline", deadline)
        }
        return payByNfcUidSignContainer(
            uid,
            containerPayload,
            amountUsdc6,
            sunParams,
            nfcSubtotalCurrencyAmount,
            nfcTipCurrencyAmount,
            nfcTipRateBps,
            nfcRequestCurrency,
            nfcTaxAmountFiat6,
            nfcTaxRateBps,
            nfcDiscountAmountFiat6,
            nfcDiscountRateBps
        )
    }

    private fun payByNfcUidPrepare(uid: String, payee: String, amountUsdc6: String, sunParams: SunParams? = null): Map<String, Any?> {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/payByNfcUidPrepare")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply {
                put("uid", uid)
                put("payee", payee)
                put("amountUsdc6", amountUsdc6)
                sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
            }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val root = org.json.JSONObject(resp)
            mapOf(
                "ok" to root.optBoolean("ok", false),
                "account" to root.optString("account").takeIf { it.isNotEmpty() },
                "nonce" to root.optString("nonce").takeIf { it.isNotEmpty() },
                "deadline" to root.optString("deadline").takeIf { it.isNotEmpty() },
                "payeeAA" to root.optString("payeeAA").takeIf { it.isNotEmpty() },
                "unitPriceUSDC6" to root.optString("unitPriceUSDC6").takeIf { it.isNotEmpty() },
                "error" to root.optString("error").takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "payByNfcUidPrepare", e)
            mapOf("ok" to false, "error" to (e.message ?: "Prepare failed"))
        }
    }

    private fun payByNfcUidSignContainer(
        uid: String,
        containerPayload: org.json.JSONObject,
        amountUsdc6: String,
        sunParams: SunParams? = null,
        nfcSubtotalCurrencyAmount: String? = null,
        nfcTipCurrencyAmount: String? = null,
        nfcTipRateBps: Int = 0,
        nfcRequestCurrency: String = "CAD",
        nfcTaxAmountFiat6: String? = null,
        nfcTaxRateBps: Int? = null,
        nfcDiscountAmountFiat6: String? = null,
        nfcDiscountRateBps: Int? = null
    ): PayByNfcResult {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/payByNfcUidSignContainer")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 90000
            val body = org.json.JSONObject().apply {
                put("uid", uid)
                put("containerPayload", containerPayload)
                put("amountUsdc6", amountUsdc6)
                sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
                nfcSubtotalCurrencyAmount?.trim()?.takeIf { it.isNotEmpty() }?.let { put("nfcSubtotalCurrencyAmount", it) }
                nfcTipCurrencyAmount?.trim()?.takeIf { it.isNotEmpty() }?.let { tip ->
                    val tipNum = tip.toDoubleOrNull() ?: 0.0
                    if (tipNum > 0) {
                        put("nfcTipCurrencyAmount", tip)
                        if (nfcTipRateBps > 0) put("nfcTipRateBps", nfcTipRateBps)
                    }
                }
                put("nfcRequestCurrency", nfcRequestCurrency.trim().ifEmpty { "CAD" })
                nfcTaxAmountFiat6?.trim()?.takeIf { it.isNotEmpty() }?.let { put("nfcTaxAmountFiat6", it) }
                nfcTaxRateBps?.takeIf { it in 0..10000 }?.let { put("nfcTaxRateBps", it) }
                nfcDiscountAmountFiat6?.trim()?.takeIf { it.isNotEmpty() }?.let { put("nfcDiscountAmountFiat6", it) }
                nfcDiscountRateBps?.takeIf { it in 0..10000 }?.let { put("nfcDiscountRateBps", it) }
            }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val root = org.json.JSONObject(resp)
            PayByNfcResult(
                success = code in 200..299 && root.optBoolean("success", true),
                txHash = root.optString("USDC_tx").takeIf { it.isNotEmpty() },
                error = root.optString("error").takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "payByNfcUidSignContainer", e)
            PayByNfcResult(false, null, e.message ?: "Sign failed")
        }
    }

    private data class NfcLinkAppResult(
        val success: Boolean,
        val deepLinkUrl: String?,
        val error: String?,
        val errorCode: String? = null,
        val redeemOnChain: Boolean = false
    )

    /** API may return HTML error pages (e.g. 502 from proxy); avoid JSONObject crashing on `<!DOCTYPE...`. */
    private fun parseBeamioApiJsonObject(raw: String?, httpCode: Int): org.json.JSONObject? {
        val t = raw?.trim().orEmpty()
        if (t.isEmpty()) {
            Log.w("MainActivity", "Empty API body (HTTP $httpCode)")
            return null
        }
        val first = t.firstOrNull { !it.isWhitespace() }
        if (first != '{' && first != '[') {
            Log.w("MainActivity", "Non-JSON API body (HTTP $httpCode): ${t.take(240)}")
            return null
        }
        return try {
            org.json.JSONObject(t)
        } catch (e: org.json.JSONException) {
            Log.w("MainActivity", "JSON parse error (HTTP $httpCode): ${e.message}; snippet=${t.take(280)}")
            null
        }
    }

    /** POST /api/nfcLinkApp：SUN 已由 Cluster 预检；返回 deepLinkUrl 供展示 QR */
    private fun postNfcLinkApp(sun: SunParams): NfcLinkAppResult {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/nfcLinkApp")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 120000
            val body = org.json.JSONObject().apply {
                put("uid", sun.uid)
                put("e", sun.e)
                put("c", sun.c)
                put("m", sun.m)
                put("cardAddress", BEAMIO_USER_CARD_ASSET_ADDRESS)
            }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() }.orEmpty()
            conn.disconnect()
            val root = parseBeamioApiJsonObject(resp, code)
            if (root == null) {
                val httpOk = code in 200..299
                return NfcLinkAppResult(
                    success = false,
                    deepLinkUrl = null,
                    error = if (httpOk) "Invalid response from server." else "Request failed (HTTP $code).",
                    errorCode = null,
                    redeemOnChain = false
                )
            }
            val okBody = root.optBoolean("success", false)
            val deep = root.optString("deepLinkUrl").takeIf { it.isNotEmpty() }
            val err = root.optString("error").takeIf { it.isNotEmpty() }
            val errCode = root.optString("errorCode").takeIf { it.isNotEmpty() }
            val redeemOn = root.optBoolean("redeemOnChain", false)
            val httpOk = code in 200..299
            val ok = httpOk && okBody && deep != null
            NfcLinkAppResult(
                success = ok,
                deepLinkUrl = deep,
                error = err ?: if (!httpOk || !okBody) "Request failed (HTTP $code)" else null,
                errorCode = errCode,
                redeemOnChain = redeemOn
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "postNfcLinkApp", e)
            NfcLinkAppResult(false, null, e.message ?: "Network error")
        }
    }

    private data class NfcLinkAppCancelResult(val success: Boolean, val error: String?)

    /** POST /api/nfcLinkAppCancel：取消进行中 Link（链上 cancelRedeem + 释放 DB） */
    private fun postNfcLinkAppCancel(sun: SunParams): NfcLinkAppCancelResult {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/nfcLinkAppCancel")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 30000
            conn.readTimeout = 120000
            val body = org.json.JSONObject().apply {
                put("uid", sun.uid)
                put("e", sun.e)
                put("c", sun.c)
                put("m", sun.m)
            }.toString()
            conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() }.orEmpty()
            conn.disconnect()
            val root = parseBeamioApiJsonObject(resp, code)
                ?: return NfcLinkAppCancelResult(false, if (code in 200..299) "Invalid response from server." else "Request failed (HTTP $code).")
            val ok = code in 200..299 && root.optBoolean("success", false)
            val err = root.optString("error").takeIf { it.isNotEmpty() }
            NfcLinkAppCancelResult(ok, err)
        } catch (e: Exception) {
            Log.e("MainActivity", "postNfcLinkAppCancel", e)
            NfcLinkAppCancelResult(false, e.message ?: "Network error")
        }
    }

    private fun runLinkAppCancelLock() {
        val sun = linkAppLastSunForCancel ?: return
        if (linkAppCancelInProgress) return
        linkAppCancelInProgress = true
        Thread {
            val cr = postNfcLinkAppCancel(sun)
            runOnUiThread {
                linkAppCancelInProgress = false
                if (cr.success) {
                    nfcFetchError = ""
                    linkAppLastSunForCancel = null
                    showScanMethodScreen = true
                    syncScanMethodEntryAndTabs()
                } else {
                    nfcFetchError = cr.error ?: "Cancel link lock failed"
                }
            }
        }.start()
    }

    private fun encodeLinkAppQrBitmap(text: String, sizePx: Int = 480): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bmp.setPixel(x, y, if (matrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            Log.e("MainActivity", "encodeLinkAppQrBitmap", e)
            null
        }
    }

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
                val displayError = if (parsed.ok) null else (parsed.error ?: "Query failed")
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    if (parsed.ok) {
                        readScreenAssets = parsed
                        readScreenRawJson = json
                        readScreenStatus = ReadStatus.Success
                        readScreenError = ""
                        if (fromScan) {
                            nfcFetchingInfo = false
                            showScanMethodScreen = false
                            showReadScreen = true
                        }
                    } else {
                        readScreenStatus = ReadStatus.Error
                        readScreenError = displayError ?: "Query failed"
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = displayError ?: "Query failed"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "fetchWalletAssets", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    readScreenStatus = ReadStatus.Error
                    readScreenError = e.message ?: "Network request failed"
                    if (fromScan) {
                        nfcFetchingInfo = false
                        nfcFetchError = e.message ?: "Network request failed"
                    }
                }
            }
        }.start()
    }

    /** @param forPostPayment 若为 true，请求体带 for=postPaymentBalance，便于服务端日志区分扣款后拉取 */
    private fun fetchWalletAssetsSync(wallet: String, forPostPayment: Boolean = false): UIDAssets? {
        return try {
            val apiUrl = java.net.URL("$BEAMIO_API/api/getWalletAssets")
            val conn = apiUrl.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply {
                put("wallet", wallet)
                if (forPostPayment) put("for", "postPaymentBalance")
            }.toString()
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

    /** 从本地加载 profile 缓存，遵循 beamio-ai-onchain-fetch：首屏优先使用本地可信数据 */
    private fun loadProfileCache(wallet: String): Pair<TerminalProfile?, TerminalProfile?> {
        return try {
            val prefs = getSharedPreferences(PREFS_PROFILE_CACHE, Context.MODE_PRIVATE)
            val key = wallet.lowercase()
            val termJson = prefs.getString("term:$key", null)
            val adminJson = prefs.getString("admin:$key", null)
            val term = termJson?.let { parseTerminalProfileFromJson(it) }
            val admin = adminJson?.let { parseTerminalProfileFromJson(it) }
            Pair(term, admin)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    private fun parseTerminalProfileFromJson(json: String): TerminalProfile? {
        return try {
            val o = org.json.JSONObject(json)
            TerminalProfile(
                accountName = o.optString("accountName").takeIf { it.isNotEmpty() },
                first_name = o.optString("first_name").takeIf { it.isNotEmpty() },
                last_name = o.optString("last_name").takeIf { it.isNotEmpty() },
                image = o.optString("image").takeIf { it.isNotEmpty() },
                address = o.optString("address").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) {
            null
        }
    }

    /** 保存可信 profile 到本地，仅成功拉取时调用 */
    private fun saveProfileCache(wallet: String, terminal: TerminalProfile?, admin: TerminalProfile?) {
        try {
            val prefs = getSharedPreferences(PREFS_PROFILE_CACHE, Context.MODE_PRIVATE)
            val key = wallet.lowercase()
            prefs.edit().apply {
                if (terminal != null) putString("term:$key", terminalToJson(terminal))
                if (admin != null) putString("admin:$key", terminalToJson(admin))
                apply()
            }
        } catch (_: Exception) { }
    }

    private fun terminalToJson(p: TerminalProfile): String =
        org.json.JSONObject().apply {
            put("accountName", p.accountName ?: "")
            put("first_name", p.first_name ?: "")
            put("last_name", p.last_name ?: "")
            put("image", p.image ?: "")
            put("address", p.address ?: "")
        }.toString()

    /** 拉取终端 profile 与上层 admin profile。参照 biz.tsx：cardOwner → searchUsername → BeamioCapsule。从 BeamioUserCard 卡合约获取 admin 列表，upperAdmin/owner → search-users → adminProfile。 */
    private fun fetchTerminalProfileSync(wallet: String): Pair<TerminalProfile?, TerminalProfile?> {
        val profile = fetchSearchUsersSync(wallet)
        val upperAdmin = fetchGetCardAdminInfoSync(wallet)
        val adminProfile = if (upperAdmin != null) fetchSearchUsersSync(upperAdmin) else null
        return Pair(profile, adminProfile)
    }

    /** GET /api/getCardAdminInfo 完整 JSON（含 admins / metadatas，与 biz 部署的 tierRouting 同源） */
    private fun fetchGetCardAdminInfoJsonSync(wallet: String): org.json.JSONObject? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getCardAdminInfo?cardAddress=${java.net.URLEncoder.encode(BEAMIO_USER_CARD_ASSET_ADDRESS, "UTF-8")}&wallet=${java.net.URLEncoder.encode(wallet, "UTF-8")}")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val json = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code in 200..299) {
                val root = org.json.JSONObject(json)
                if (root.optBoolean("ok", false)) root else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /** GET /api/getCardAdminInfo?cardAddress=0x...&wallet=0x... - 从 BeamioUserCard 卡合约获取 owner 与 admin 列表，返回该终端的上层 admin（upperAdmin）。cardAddress 默认 BEAMIO_USER_CARD_ASSET_ADDRESS。 */
    private fun fetchGetCardAdminInfoSync(wallet: String): String? {
        val root = fetchGetCardAdminInfoJsonSync(wallet) ?: return null
        return root.optString("upperAdmin").takeIf { it.isNotEmpty() && it != "null" }
            ?: root.optString("owner").takeIf { it.isNotEmpty() && it != "null" }
    }

    /**
     * 解析本终端 EOA 在 `getAdminListWithMetadata` 中对应条目的 metadata，读取 `tierRoutingDiscounts`（与 biz Sign & Deploy 一致）。
     * @return Pair(taxPercent, discountSummary)；仅网络/解析失败时返回 null（不覆盖 UI 旧值）
     */
    private fun fetchInfraRoutingForTerminalWalletSync(wallet: String): Pair<Double, String>? {
        val root = fetchGetCardAdminInfoJsonSync(wallet) ?: return null
        val admins = root.optJSONArray("admins") ?: return null
        val metadatas = root.optJSONArray("metadatas") ?: return null
        val wNorm = wallet.trim().lowercase()
        var idx = -1
        for (i in 0 until admins.length()) {
            if (admins.optString(i, "").trim().lowercase() == wNorm) {
                idx = i
                break
            }
        }
        if (idx < 0) return Pair(0.0, "Not on admin list")
        val metaStr = metadatas.optString(idx, "").trim()
        if (metaStr.isEmpty()) return Pair(0.0, "No routing metadata")
        val parsed = parseTierRoutingDiscountsFromTerminalMetadata(metaStr, BEAMIO_USER_CARD_ASSET_ADDRESS)
        return parsed ?: Pair(0.0, "No valid routing")
    }

    private fun parseTierRoutingDiscountsFromTerminalMetadata(metaJson: String, expectedInfrastructureCard: String): Pair<Double, String>? {
        return try {
            val root = org.json.JSONObject(metaJson)
            val tr = root.optJSONObject("tierRoutingDiscounts") ?: return Pair(0.0, "No tier routing block")
            if (tr.has("schemaVersion") && !tr.isNull("schemaVersion")) {
                val sv = when (val v = tr.get("schemaVersion")) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: return null
                    else -> return null
                }
                if (sv != 1) return null
            }
            val infra = tr.optString("infrastructureCard", "").trim()
            if (infra.isEmpty()) return null
            val infraLc = infra.lowercase()
            val expLc = expectedInfrastructureCard.trim().lowercase()
            if (infraLc != expLc) return null

            var tax = 0.0
            when (val t = tr.opt("taxRatePercent")) {
                is Number -> tax = t.toDouble()
                is String -> tax = t.toDoubleOrNull() ?: 0.0
            }
            tax = (kotlin.math.round(tax.coerceIn(0.0, 100.0) * 100.0) / 100.0)

            val tiers = tr.optJSONArray("tiers") ?: org.json.JSONArray()
            val discParts = ArrayList<Int>(tiers.length())
            for (i in 0 until tiers.length()) {
                val row = tiers.optJSONObject(i) ?: continue
                val d = when (val dp = row.opt("discountPercent")) {
                    is Number -> dp.toInt().coerceIn(0, 100)
                    is String -> dp.toIntOrNull()?.coerceIn(0, 100) ?: continue
                    else -> continue
                }
                discParts.add(d)
            }
            val discLabel = if (discParts.isEmpty()) "—" else discParts.joinToString(" · ") { "$it%" }
            Pair(tax, discLabel)
        } catch (_: Exception) {
            null
        }
    }

    private data class TierRoutingDetails(val taxPercent: Double, val discountByTierKey: Map<String, Int>)

    /** 与 biz `tierRoutingDiscounts.tiers` 一致：chain-tier-{index} / tierId -> discountPercent */
    private fun parseTierRoutingDetailsFromTerminalMetadata(metaJson: String, expectedInfrastructureCard: String): TierRoutingDetails? {
        return try {
            val root = org.json.JSONObject(metaJson)
            val tr = root.optJSONObject("tierRoutingDiscounts") ?: return null
            if (tr.has("schemaVersion") && !tr.isNull("schemaVersion")) {
                val sv = when (val v = tr.get("schemaVersion")) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: return null
                    else -> return null
                }
                if (sv != 1) return null
            }
            val infra = tr.optString("infrastructureCard", "").trim()
            if (infra.isEmpty()) return null
            if (infra.lowercase() != expectedInfrastructureCard.trim().lowercase()) return null

            var tax = 0.0
            when (val t = tr.opt("taxRatePercent")) {
                is Number -> tax = t.toDouble()
                is String -> tax = t.toDoubleOrNull() ?: 0.0
            }
            tax = (kotlin.math.round(tax.coerceIn(0.0, 100.0) * 100.0) / 100.0)

            val map = mutableMapOf<String, Int>()
            val tiers = tr.optJSONArray("tiers") ?: org.json.JSONArray()
            for (i in 0 until tiers.length()) {
                val row = tiers.optJSONObject(i) ?: continue
                val d = when (val dp = row.opt("discountPercent")) {
                    is Number -> dp.toInt().coerceIn(0, 100)
                    is String -> dp.toIntOrNull()?.coerceIn(0, 100) ?: continue
                    else -> continue
                }
                val idx = when (val v = row.opt("chainTierIndex")) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull()
                    else -> null
                }
                val tid = row.optString("tierId", "").trim()
                if (idx != null) map["chain-tier-$idx".lowercase()] = d
                if (tid.isNotEmpty()) map[tid.lowercase()] = d
            }
            TierRoutingDetails(tax, map)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchTierRoutingDetailsForTerminalWalletSync(wallet: String): TierRoutingDetails? {
        val root = fetchGetCardAdminInfoJsonSync(wallet) ?: return null
        val admins = root.optJSONArray("admins") ?: return null
        val metadatas = root.optJSONArray("metadatas") ?: return null
        val wNorm = wallet.trim().lowercase()
        var idx = -1
        for (i in 0 until admins.length()) {
            if (admins.optString(i, "").trim().lowercase() == wNorm) {
                idx = i
                break
            }
        }
        if (idx < 0) return null
        val metaStr = metadatas.optString(idx, "").trim()
        if (metaStr.isEmpty()) return null
        return parseTierRoutingDetailsFromTerminalMetadata(metaStr, BEAMIO_USER_CARD_ASSET_ADDRESS)
    }

    /** 按客户 NFT `tier` 字段匹配 routing 表（取最大折扣率） */
    private fun pickTierDiscountPercentFromAssets(assets: UIDAssets, tierKeyToDiscount: Map<String, Int>): Int {
        if (tierKeyToDiscount.isEmpty()) return 0
        val keys = mutableSetOf<String>()
        assets.cards?.forEach { c ->
            c.nfts.forEach { n ->
                val t = n.tier.trim()
                if (t.isNotEmpty()) {
                    keys.add(t)
                    keys.add(t.lowercase())
                }
            }
        }
        assets.nfts?.forEach { n ->
            val t = n.tier.trim()
            if (t.isNotEmpty()) {
                keys.add(t)
                keys.add(t.lowercase())
            }
        }
        var best = 0
        for (k in keys) {
            tierKeyToDiscount[k]?.let { best = kotlin.math.max(best, it) }
            tierKeyToDiscount[k.lowercase()]?.let { best = kotlin.math.max(best, it) }
        }
        for (k in keys) {
            val idx = k.toIntOrNull()
            if (idx != null) {
                tierKeyToDiscount["chain-tier-$idx".lowercase()]?.let { best = kotlin.math.max(best, it) }
            }
        }
        return best.coerceIn(0, 100)
    }

    /** GET /api/search-users?keyward=... - 解析 results[0] 为 TerminalProfile */
    private fun fetchSearchUsersSync(keyward: String): TerminalProfile? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/search-users?keyward=${java.net.URLEncoder.encode(keyward, "UTF-8")}")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val json = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code in 200..299) {
                val root = org.json.JSONObject(json)
                val results = root.optJSONArray("results")
                val first = results?.optJSONObject(0) ?: return null
                TerminalProfile(
                    accountName = first.optString("username").takeIf { it.isNotEmpty() } ?: first.optString("accountName").takeIf { it.isNotEmpty() },
                    first_name = first.optString("first_name").takeIf { it.isNotEmpty() },
                    last_name = first.optString("last_name").takeIf { it.isNotEmpty() },
                    image = first.optString("image").takeIf { it.isNotEmpty() },
                    address = first.optString("address").takeIf { it.isNotEmpty() }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun statsDashboardAmountChanged(baseline: Double?, current: Double): Boolean {
        if (baseline == null) return true
        return abs(current - baseline) > 1e-5
    }

    /**
     * Top-up / Charge 成功后：每 6 秒链上拉取一次，直到对应统计相对 baseline 已变化（链上已反映）。
     * RPC 失败时不覆盖已有金额，并对正在等待验证的一侧显示黄色感叹号。
     */
    private fun scheduleCardStatsRefetchUntilChanged(expectChargeChange: Boolean, expectTopUpChange: Boolean) {
        runOnUiThread {
            if (expectChargeChange) {
                pendingVerifyChargeStats = true
                cardStatsChargeRpcWarning = false
                baselineChargeStats = cardChargeAmount
            }
            if (expectTopUpChange) {
                pendingVerifyTopUpStats = true
                cardStatsTopUpRpcWarning = false
                baselineTopUpStats = cardTopUpAmount
            }
            startCardStatsVerifyLoopIfNeeded()
        }
    }

    private fun applyCardStatsFetchForVerify(charge: Double?, topUp: Double?) {
        val rpcFail = charge == null && topUp == null
        if (rpcFail) {
            if (pendingVerifyChargeStats) cardStatsChargeRpcWarning = true
            if (pendingVerifyTopUpStats) cardStatsTopUpRpcWarning = true
            return
        }
        if (charge != null) {
            cardChargeAmount = charge
            if (pendingVerifyChargeStats) {
                cardStatsChargeRpcWarning = false
                if (statsDashboardAmountChanged(baselineChargeStats, charge)) {
                    pendingVerifyChargeStats = false
                }
            }
        }
        if (topUp != null) {
            cardTopUpAmount = topUp
            if (pendingVerifyTopUpStats) {
                cardStatsTopUpRpcWarning = false
                if (statsDashboardAmountChanged(baselineTopUpStats, topUp)) {
                    pendingVerifyTopUpStats = false
                }
            }
        }
    }

    private fun startCardStatsVerifyLoopIfNeeded() {
        synchronized(cardStatsVerifyLock) {
            if (cardStatsVerifyThreadRunning) return
            cardStatsVerifyThreadRunning = true
        }
        Thread {
            var stoppedBecauseVerified = false
            try {
                val maxIterations = 60
                var iter = 0
                while (iter < maxIterations) {
                    Thread.sleep(6000)
                    iter++
                    val wallet = BeamioWeb3Wallet.getAddress()?.takeIf { it.isNotEmpty() } ?: break
                    val pair = fetchCardStatsSync(wallet)
                    val latch = CountDownLatch(1)
                    val continueLoop = booleanArrayOf(false)
                    runOnUiThread {
                        try {
                            applyCardStatsFetchForVerify(pair.first, pair.second)
                            continueLoop[0] = pendingVerifyChargeStats || pendingVerifyTopUpStats
                        } finally {
                            latch.countDown()
                        }
                    }
                    latch.await()
                    if (!continueLoop[0]) {
                        stoppedBecauseVerified = true
                        break
                    }
                }
                runOnUiThread {
                    if (!stoppedBecauseVerified && (pendingVerifyChargeStats || pendingVerifyTopUpStats)) {
                        pendingVerifyChargeStats = false
                        pendingVerifyTopUpStats = false
                        cardStatsChargeRpcWarning = false
                        cardStatsTopUpRpcWarning = false
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } finally {
                synchronized(cardStatsVerifyLock) {
                    cardStatsVerifyThreadRunning = false
                }
            }
        }.start()
    }

    /** 直接从链上获取 periodTransferAmount(Charge) 与 periodMint(Top-Up)，不依赖 API。eth_call getAdminStatsFull(admin, PERIOD_DAY, 0, 0)。wallet 须为卡上 admin 才有非零数据。失败时返回 null，遵循 beamio-ai-onchain-fetch。 */
    private fun fetchCardStatsSync(wallet: String): Pair<Double?, Double?> {
        if (wallet.isNullOrEmpty()) {
            Log.w("getCardStats", "wallet empty, skip fetch")
            return Pair(null, null)
        }
        val adminAddr = wallet.removePrefix("0x").lowercase()
        if (adminAddr.length != 40 || !adminAddr.all { it in '0'..'9' || it in 'a'..'f' }) {
            Log.w("getCardStats", "invalid admin address")
            return Pair(null, null)
        }
        return try {
            val data = buildGetAdminStatsFullCalldata(adminAddr)
            val reqBody = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$BEAMIO_USER_CARD_ASSET_ADDRESS","data":"$data"},"latest"],"id":1}"""
            val conn = java.net.URL(BASE_RPC_URL).openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.outputStream.use { it.write(reqBody.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val json = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code in 200..299) {
                val root = org.json.JSONObject(json)
                val result = root.optString("result", "")
                val err = root.optJSONObject("error")
                if (err != null) {
                    Log.w("getCardStats", "RPC error: ${err.optString("message", "")}")
                    return Pair(null, null)
                }
                if (result.isEmpty() || result == "0x") {
                    Log.w("getCardStats", "empty result")
                    return Pair(null, null)
                }
                val (charge, topUp) = decodeGetAdminStatsFullResult(result)
                Log.d("getCardStats", "chain ok charge=$charge topUp=$topUp")
                Pair(charge, topUp)
            } else {
                Log.w("getCardStats", "HTTP $code body=${json.take(200)}")
                Pair(null, null)
            }
        } catch (e: Exception) {
            Log.w("getCardStats", "chain fetch failed", e)
            Pair(null, null)
        }
    }

    /** getAdminStatsFull(address,uint8,uint256,uint256) selector=0x9abc4888, PERIOD_DAY=1 */
    private fun buildGetAdminStatsFullCalldata(adminAddrLower: String): String {
        val addrPadded = adminAddrLower.padStart(64, '0')
        val periodDay = "1".padStart(64, '0')
        val zero = "0".padStart(64, '0')
        return "0x9abc4888" + addrPadded + periodDay + zero + zero
    }

    /** 解析 getAdminStatsFull 返回。ABI：外层 offset 32B，struct 自 byte 32 起；periodTransferAmount=index11(hex 768-831)，periodMint=index8(hex 576-639)。除以 1e6。 */
    private fun decodeGetAdminStatsFullResult(hex: String): Pair<Double, Double> {
        val raw = hex.removePrefix("0x")
        if (raw.length < 832) return 0.0 to 0.0
        val chargeHex = raw.substring(768, 832)   // periodTransferAmount (index 11)
        val topUpHex = raw.substring(576, 640)    // periodMint (index 8)
        val charge = java.math.BigInteger(chargeHex, 16).toDouble() / 1_000_000
        val topUp = java.math.BigInteger(topUpHex, 16).toDouble() / 1_000_000
        return charge to topUp
    }

    private fun fetchUidAssets(uid: String, sunParams: SunParams? = null) {
        Thread {
            try {
                val url = java.net.URL("$BEAMIO_API/api/getUIDAssets")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                val body = org.json.JSONObject().apply {
                    put("uid", uid)
                    sunParams?.let { put("e", it.e); put("c", it.c); put("m", it.m) }
                }.toString()
                conn.outputStream.use { os -> os.write(body.toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val respBody = if (code in 200..299) conn.inputStream else conn.errorStream
                val json = respBody?.use { it.bufferedReader().readText() } ?: "{}"
                conn.disconnect()
                // Debug: 与服务器返回一致，便于排查 cardBackground 未解析/未渲染
                Log.d("getUIDAssets", "response JSON length=${json.length} preview=${json.take(800)}")
                val parsed = parseUidAssetsJson(json)
                Log.d("getUIDAssets", "parsed cards: ${parsed.cards?.joinToString { "${it.cardName}(cardBackground=${it.cardBackground})" } ?: "null"}")
                val displayError = if (parsed.ok) null else (parsed.error ?: "Query failed")
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    if (parsed.ok) {
                        readScreenAssets = parsed
                        readScreenRawJson = json
                        readScreenStatus = ReadStatus.Success
                        readScreenError = ""
                        if (fromScan) {
                            nfcFetchingInfo = false
                            showScanMethodScreen = false
                            showReadScreen = true
                        }
                    } else {
                        readScreenStatus = ReadStatus.Error
                        readScreenError = displayError ?: "Query failed"
                        if (fromScan) {
                            nfcFetchingInfo = false
                            nfcFetchError = displayError ?: "Query failed"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "fetchUidAssets", e)
                runOnUiThread {
                    val fromScan = nfcFetchingInfo
                    readScreenStatus = ReadStatus.Error
                    readScreenError = e.message ?: "Network request failed"
                    if (fromScan) {
                        nfcFetchingInfo = false
                        nfcFetchError = e.message ?: "Network request failed"
                    }
                }
            }
        }.start()
    }

    private fun normalizeHexColorString(raw: String?): String? {
        val s = raw?.trim()?.removePrefix("#") ?: return null
        if (s.length != 6 && s.length != 8) return null
        return if (s.length == 6) "#$s" else "#$s"
    }

    private fun parseMetadataTiers(meta: org.json.JSONObject?): List<MetadataTier> {
        val tiersArr = meta?.optJSONArray("tiers") ?: return emptyList()
        val parsed = mutableListOf<MetadataTier>()
        for (i in 0 until tiersArr.length()) {
            val t = tiersArr.optJSONObject(i) ?: continue
            val minUsdc6 = t.optString("minUsdc6").toLongOrNull() ?: t.optLong("minUsdc6", 0L)
            parsed.add(
                MetadataTier(
                    minUsdc6 = minUsdc6.coerceAtLeast(0L),
                    name = t.optString("name").takeIf { it.isNotBlank() },
                    description = t.optString("description").takeIf { it.isNotBlank() },
                    image = t.optString("image").takeIf { it.isNotBlank() },
                    backgroundColor = normalizeHexColorString(t.optString("backgroundColor"))
                )
            )
        }
        return parsed.sortedBy { it.minUsdc6 }
    }

    /** Pre-fetch BEAMIO_USER_CARD_ASSET_ADDRESS metadata and tiers on Home enter. Cached globally; topup/charge success uses cache without re-fetch. */
    private fun prefetchInfraCardMetadata() {
        fetchCardMetadataTiers(BEAMIO_USER_CARD_ASSET_ADDRESS)
    }

    private fun fetchCardMetadataTiers(cardAddress: String): List<MetadataTier> {
        val key = cardAddress.lowercase()
        cardMetadataTierCache[key]?.let { return it }
        return try {
            val url = java.net.URL("$BEAMIO_API/api/cardMetadata?cardAddress=$cardAddress")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val root = org.json.JSONObject(body)
            val tiers = parseMetadataTiers(root.optJSONObject("metadata"))
            cardMetadataTierCache[key] = tiers
            tiers
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Fetch per-NFT tier metadata image from GET /api/metadata/0x{cardAddress}{tokenId}.json (same source as getUIDAssets). */
    private fun fetchNftTierMetadataImage(cardAddress: String, tokenId: Long): String? {
        if (tokenId <= 0L) return null
        return try {
            val addr = (if (cardAddress.startsWith("0x")) cardAddress else "0x$cardAddress").lowercase()
            val url = java.net.URL("$BEAMIO_API/api/metadata/${addr}$tokenId.json")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code !in 200..299) return null
            val root = org.json.JSONObject(body)
            val img = root.optString("image").takeIf { it.isNotBlank() }
                ?: root.optJSONObject("properties")?.optString("image")?.takeIf { it.isNotBlank() }
            img
        } catch (_: Exception) {
            null
        }
    }

    private fun enrichCardTierFromMetadata(card: CardItem): CardItem {
        val needTier = card.tierName.isNullOrBlank() || card.tierDescription.isNullOrBlank() || card.cardBackground.isNullOrBlank() || card.cardImage.isNullOrBlank()
        if (!needTier) return card
        val isInfraCard = card.cardAddress.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true)
        var cardImage = card.cardImage
        // For infra card: use cached tiers only (pre-fetched on Home enter). No per-NFT fetch after topup/charge.
        if (cardImage.isNullOrBlank() && !isInfraCard) {
            val bestTokenId = card.primaryMemberTokenId?.toLongOrNull()?.takeIf { it > 0 }
                ?: card.nfts
                    .filter { it.tokenId.toLongOrNull()?.let { id -> id > 0 } == true }
                    .maxByOrNull { it.tokenId.toLongOrNull() ?: 0L }
                    ?.tokenId?.toLongOrNull()
            if (bestTokenId != null) {
                cardImage = fetchNftTierMetadataImage(card.cardAddress, bestTokenId)
            }
        }
        val tiers = fetchCardMetadataTiers(card.cardAddress)
        val points6 = card.points6.toLongOrNull() ?: 0L
        val selected = if (tiers.isNotEmpty()) {
            tiers.lastOrNull { it.minUsdc6 <= points6 } ?: tiers.first()
        } else null
        return card.copy(
            tierName = card.tierName ?: selected?.name,
            tierDescription = card.tierDescription ?: selected?.description,
            cardImage = cardImage ?: selected?.image,
            cardBackground = card.cardBackground ?: selected?.backgroundColor
        )
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
                        tierDescription = c.optString("tierDescription").takeIf { it.isNotEmpty() },
                        primaryMemberTokenId = c.optString("primaryMemberTokenId").takeIf { it.isNotEmpty() }
                    )
                }
            } else {
                null
            }
            // 过滤掉已废弃的旧卡地址
            val cardsFromArr = rawCards
                ?.filter { !it.cardAddress.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true) }
                ?.map { enrichCardTierFromMetadata(it) }
                ?.takeIf { it.isNotEmpty() }
            // getWalletAssets 返回 legacy 格式（无 cards 数组）：从 cardAddress/points/points6 构建单卡，并遵循 APP card 渲染 protocol 做 tier 富化
            val cards = cardsFromArr ?: run {
                val legacyAddr = root.optString("cardAddress").takeIf { it.isNotEmpty() }
                if (legacyAddr != null && !legacyAddr.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true)) {
                    listOf(CardItem(
                        cardAddress = legacyAddr,
                        cardName = if (legacyAddr.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true)) "CCSA CARD" else "Card",
                        cardType = if (legacyAddr.equals(BEAMIO_USER_CARD_ASSET_ADDRESS, ignoreCase = true)) "ccsa" else "infrastructure",
                        points = root.optString("points", "0"),
                        points6 = root.optString("points6", "0"),
                        cardCurrency = root.optString("cardCurrency", "CAD"),
                        nfts = emptyList(),
                        cardBackground = null,
                        cardImage = null,
                        tierName = null,
                        tierDescription = null
                    )).map { enrichCardTierFromMetadata(it) }
                } else null
            }
            val unitPriceUSDC6 = root.optString("unitPriceUSDC6").takeIf { it.isNotEmpty() }
            val beamioUserCard = root.optString("beamioUserCard").takeIf { it.isNotEmpty() }
            val uidVal = root.optString("uid").takeIf { it.isNotEmpty() }
            val tagIdHexVal = root.optString("tagIdHex").takeIf { it.isNotEmpty() }
            val counterHexVal = root.optString("counterHex").takeIf { it.isNotEmpty() }
            val counterVal = root.optInt("counter", -1).takeIf { it >= 0 }
            if (cards != null) {
                val first = cards.firstOrNull()
                UIDAssets(
                    ok = root.optBoolean("ok", false),
                    address = root.optString("address").takeIf { it.isNotEmpty() },
                    aaAddress = root.optString("aaAddress").takeIf { it.isNotEmpty() },
                    uid = uidVal,
                    tagIdHex = tagIdHexVal,
                    counterHex = counterHexVal,
                    counter = counterVal,
                    cardAddress = first?.cardAddress,
                    points = first?.points,
                    points6 = first?.points6,
                    usdcBalance = root.optString("usdcBalance").takeIf { it.isNotEmpty() },
                    cardCurrency = first?.cardCurrency,
                    nfts = first?.nfts?.takeIf { it.isNotEmpty() },
                    cards = cards,
                    unitPriceUSDC6 = unitPriceUSDC6,
                    beamioUserCard = beamioUserCard,
                    error = root.optString("error").takeIf { it.isNotEmpty() }
                )
            } else {
                val legacyCardAddr = root.optString("cardAddress").takeIf { it.isNotEmpty() }
                val isDeprecatedLegacy = legacyCardAddr?.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true) == true
                UIDAssets(
                    ok = root.optBoolean("ok", false),
                    address = root.optString("address").takeIf { it.isNotEmpty() },
                    aaAddress = root.optString("aaAddress").takeIf { it.isNotEmpty() },
                    uid = uidVal,
                    tagIdHex = tagIdHexVal,
                    counterHex = counterHexVal,
                    counter = counterVal,
                    cardAddress = if (isDeprecatedLegacy) null else legacyCardAddr,
                    points = if (isDeprecatedLegacy) null else root.optString("points").takeIf { it.isNotEmpty() },
                    points6 = if (isDeprecatedLegacy) null else root.optString("points6").takeIf { it.isNotEmpty() },
                    usdcBalance = root.optString("usdcBalance").takeIf { it.isNotEmpty() },
                    cardCurrency = if (isDeprecatedLegacy) null else root.optString("cardCurrency").takeIf { it.isNotEmpty() },
                    nfts = if (isDeprecatedLegacy) null else nfts.takeIf { it.isNotEmpty() },
                    cards = null,
                    unitPriceUSDC6 = unitPriceUSDC6,
                    beamioUserCard = beamioUserCard,
                    error = root.optString("error").takeIf { it.isNotEmpty() }
                )
            }
        } catch (_: Exception) {
            UIDAssets(ok = false, error = "Failed to parse response")
        }
    }

    private fun startInitCard() {
        if (nfcAdapter == null) {
            initText = "Device does not support NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "Please enable NFC first"
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
        initText = "⏳ [0/11] Init started. Please tap card..."
    }

    private fun startInitTemplateOnly() {
        if (nfcAdapter == null) {
            initText = "Device does not support NFC"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            initText = "Please enable NFC first"
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
        initText = "⏳ [T 0/2] Template-only mode started. Please tap card..."
    }

    private fun continueReInit() {
        val tag = pendingInitTag
        if (tag == null) {
            initText = "❌ No card found to confirm. Please click init again"
            showInitReconfirm = false
            initReconfirmAllowContinue = false
            return
        }
        if (!initReconfirmAllowContinue) {
            initText = "❌ Current card key mismatch. Cannot continue init"
            return
        }
        showInitReconfirm = false
        initReconfirmAllowContinue = false
        initReconfirmText = ""
        initText = "⏳ Confirmed. Starting re-init..."
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

        val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        if (tag == null) {
            uidText = "Read failed: Tag not acquired"
            return
        }

        if (!readArmed && !linkAppArmed && !initArmed && !initTemplateArmed && !topupArmed && !paymentArmed) {
            uidText = "System read intercepted. Please tap read/init/topup/payment first"
            return
        }

        if (linkAppArmed) {
            linkAppArmed = false
            val sunParams = readSunParamsFromNdef(tag)
            if (sunParams == null) {
                scanWaitingForNfc = false
                nfcFetchingInfo = false
                nfcFetchError = "Card does not support SUN. Cannot link app."
                return
            }
            scanWaitingForNfc = false
            nfcFetchingInfo = true
            nfcFetchError = ""
            Thread {
                val r = postNfcLinkApp(sunParams)
                runOnUiThread {
                    nfcFetchingInfo = false
                    if (r.success && !r.deepLinkUrl.isNullOrEmpty()) {
                        linkAppDeepLinkUrl = r.deepLinkUrl!!
                        linkAppLastSunForCancel = null
                    } else {
                        nfcFetchError = r.error ?: "Link App failed"
                        linkAppLastSunForCancel =
                            if (r.errorCode == "NFC_LINK_APP_CARD_LOCKED") sunParams else null
                    }
                }
            }.start()
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
            executePayment(tag, uid, BeamioWeb3Wallet.getAddress())
            return
        }

        if (topupArmed) {
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: return
            scanWaitingForNfc = false
            nfcFetchingInfo = false
            nfcFetchError = ""
            topupNfcExecuteInProgress = true
            topupScreenUid = uid
            topupScreenStatus = TopupStatus.Loading
            topupScreenError = ""
            topupArmed = false
            executeNfcTopup(tag, uid, topupScreenAmount, fromScanMethodFlow = true)
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
            val uid = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "Read failed"
            val sunParams = readSunParamsFromNdef(tag)
            readViaQr = false
            scanWaitingForNfc = false
            nfcFetchingInfo = true
            nfcFetchError = ""
            uidText = uid
            readScreenUid = uid
            readScreenStatus = ReadStatus.Loading
            readScreenAssets = null
            readScreenError = ""
            readArmed = false
            fetchUidAssets(sunParams?.uid ?: uid, sunParams)
        }
    }

    private data class InitInspectResult(val isInitCard: Boolean, val url: String?)
    private data class InitKeyProbeResult(
        val key0AppMatch: Boolean,
        val key0DefaultMatch: Boolean,
        val masterMatch: Boolean
    )

    private fun handleInitFlowTag(tag: Tag) {
        appendInitStep("⏳ Card detected. Checking if initialized...")
        val inspect = inspectCardInitState(tag)
        if (inspect.isInitCard) {
            pendingInitTag = tag
            pendingInitUseDefaultKey0 = false
            pendingInitRequiresKey0Reset = false
            showInitReconfirm = true
            appendInitStep("⏳ Card already initialized. Probing key0/masterKey match...")
            val probe = probeInitKeyMatch(tag)
            val allowContinue: Boolean
            val modeText: String
            if (probe.key0DefaultMatch) {
                // 出厂空白卡路径：仅凭默认 key0 可继续
                allowContinue = true
                pendingInitUseDefaultKey0 = true
                pendingInitRequiresKey0Reset = false
                modeText = "Detected key0=0x00 (factory blank card path)"
            } else if (probe.key0AppMatch && probe.masterMatch) {
                // 已配置路径：app key0 + app masterKey
                allowContinue = true
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = true
                modeText = "Detected key0=AppKey0 and masterKey match (reset path)"
            } else {
                allowContinue = false
                pendingInitUseDefaultKey0 = false
                pendingInitRequiresKey0Reset = false
                modeText = "Key mismatch. Cannot continue"
            }
            initReconfirmAllowContinue = allowContinue
            initReconfirmText = buildString {
                append("Card already init.\n")
                append("key0(app)=${if (probe.key0AppMatch) "match" else "mismatch"}, ")
                append("key0(0x00)=${if (probe.key0DefaultMatch) "match" else "mismatch"}, ")
                append("masterKey(app)=${if (probe.masterMatch) "match" else "mismatch"}\n")
                append("$modeText\n")
                if (allowContinue) {
                    append("Continue?\n")
                } else {
                    append("Card cannot complete init (key0/masterKey mismatch). Cancel only.\n")
                }
                if (!inspect.url.isNullOrBlank()) append(inspect.url)
            }
            appendInitStep(
                if (allowContinue) "⚠️ Initialized card: Continue or Cancel"
                else "❌ Initialized card: Key mismatch. Cancel only"
            )
            return
        }
        // 未初始化/空卡：直接按出厂 key0=0x00 路径执行，避免前置探测误判拦截。
        appendInitStep("✅ No initialized data detected. Executing init with factory key0=0x00 path")
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
        val ndef = Ndef.get(tag) ?: return ReadCheckResult("❌ Not init: Tag does not support NDEF", null)
        return try {
            ndef.connect()
            val msg = ndef.cachedNdefMessage ?: ndef.ndefMessage
            ndef.close()

            if (msg == null || msg.records.isEmpty()) {
                return ReadCheckResult("❌ Not init: NDEF is empty", null)
            }

            val url = msg.records.firstNotNullOfOrNull { record ->
                record.toUri()?.toString()
            } ?: return ReadCheckResult("❌ Not init: URL Record not found", null)

            if (!isInitUrl(url)) {
                return ReadCheckResult("❌ Not init: URL does not match rules", null)
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
                ReadCheckResult("⚠️ Template written (e/c/m all 0). SUN dynamic not enabled", url, counter, paramText)
            } else {
                ReadCheckResult("✅ Init complete and SUN dynamic data detected", url, counter, paramText)
            }
        } catch (e: Exception) {
            ReadCheckResult("❌ Init status check failed: ${e.message ?: "unknown error"}", null)
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    private fun applyLastCounter() {
        val next = lastCounterInput.trim().toIntOrNull()
        if (next == null || next < 0) {
            readCheckText = "❌ lastCounter must be a non-negative integer"
            return
        }
        lastCounter = next
        readCheckText = "✅ lastCounter set to $next"
    }

    private fun copyUrlToClipboard() {
        if (readUrlText.isBlank()) return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("init-url", readUrlText))
        readCheckText = "✅ URL copied to clipboard"
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
        appendInitStep("⏳ [T 1/2] Card detected. Writing URL template only...")
        try {
            val uidBytes = tag.id ?: run {
                initFail("[T 1/2] Read UID", "Cannot read card UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[T 2/2] NDEF write", "Tag is read-only")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[T 2/2] NDEF write", "Tag does not support NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            appendInitStep("✅ [T 2/2] Template write complete (EV2/SDM not executed)")
            readUrlText = initUrl
            readParamText = "template-only: e/c/m placeholder, card-side dynamic SDM not enabled"
        } catch (e: Exception) {
            initFail("[T X] Template write", e.message ?: "unknown error")
        } finally {
            initTemplateArmed = false
            initArmed = false
        }
    }

    private fun handleInitTag(tag: Tag, allowKey2Fallback: Boolean, useDefaultKey0: Boolean) {
        appendInitStep("⏳ [1/11] NFC tag detected")
        try {
            if (HARD_CODED_KEY0.length != 32 || HARD_CODED_MASTER_KEY.length != 32) {
                initFail("[2/11] Param check", "Key0 or MasterKey length is not 16-byte HEX")
                return
            }
            val appKey0 = hexToBytes(HARD_CODED_KEY0)
            val key0 = if (useDefaultKey0) ByteArray(16) else appKey0
            val masterKey = hexToBytes(HARD_CODED_MASTER_KEY)
            if (key0 == null || appKey0 == null || masterKey == null || key0.size != 16 || appKey0.size != 16 || masterKey.size != 16) {
                initFail("[2/11] Param check", "Key0/MasterKey is not valid 16-byte HEX")
                return
            }
            appendInitStep("✅ [2/11] Param check passed (${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"} / MasterKey)")

            val uidBytes = tag.id ?: run {
                initFail("[3/11] Read UID", "Cannot read card UID")
                return
            }
            val publicUid = uidBytes.joinToString("") { b -> "%02X".format(b) }
            appendInitStep("✅ [3/11] UID=$publicUid")

            val isoDep = IsoDep.get(tag) ?: run {
                initFail("[4/11] EV2 prep", "Card does not support IsoDep. Cannot run EV2/SDM")
                return
            }
            isoDep.connect()
            isoDep.timeout = 5000
            appendInitStep("✅ [4/11] IsoDep connected")

            val ev2 = Ntag424Ev2(isoDep)
            val sdmKey = ev2.deriveSdmKey(masterKey, uidBytes)
            appendInitStep("✅ [5/11] SDM Key derived from masterKey+UID")

            var session: Ev2Session? = null
            var authenticatedWithKey2Fallback = false
            try {
                appendInitStep("⏳ [6/11] Trying EV2First(${if (useDefaultKey0) "Key0=0x00" else "Key0=AppKey0"})...")
                session = ev2.authenticateEV2First(0x00, key0)
                appendInitStep("✅ [6/11] EV2First(Key0) auth success")
            } catch (e: Exception) {
                val msg = e.message.orEmpty()
                appendInitStep("⚠️ [6/11] EV2First(Key0) failed, status=${statusHexFromError(e)}")
                if (msg.contains("0x9140") && allowKey2Fallback) {
                    appendInitStep("⏳ [6/11] Trying EV2First(Key2) fallback auth...")
                    try {
                        session = ev2.authenticateEV2First(0x02, sdmKey)
                        appendInitStep("✅ [6/11] EV2First(Key2) fallback auth success (card may already be init)")
                        authenticatedWithKey2Fallback = true
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(Key2)",
                            "Fallback auth failed, status=${statusHexFromError(e2)}"
                        )
                        return
                    }
                } else if (msg.contains("0x9140") && useDefaultKey0) {
                    appendInitStep("⏳ [6/11] Trying EV2First(AppKey0) auth...")
                    try {
                        session = ev2.authenticateEV2First(0x00, appKey0)
                        appendInitStep("✅ [6/11] EV2First(AppKey0) auth success")
                    } catch (e2: Exception) {
                        initFail(
                            "[6/11] EV2First(AppKey0)",
                            "Auth failed, status=${statusHexFromError(e2)}; 0x00/AppKey0 both mismatch"
                        )
                        return
                    }
                } else if (msg.contains("0x9140")) {
                    initFail("[6/11] EV2First(Key0)", "Auth failed 0x9140. Card Key0 mismatch (0x00/AppKey0).")
                    return
                } else {
                    throw e
                }
            }
            val sessionSafe = session ?: run {
                initFail("[6/11] EV2First", "No valid session established")
                return
            }

            if (!authenticatedWithKey2Fallback) {
                ev2.changeKey(sessionSafe, changingKeyNo = 0x02, newKey = sdmKey)
                appendInitStep("✅ [7/11] ChangeKey(0x02) wrote derived SDM Key")
            } else {
                appendInitStep("ℹ️ [7/11] Key2 fallback: skip ChangeKey(0x02)")
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
                appendInitStep("✅ [9/11] SDM offsets + ChangeFileSettings done")
            } else {
                appendInitStep("ℹ️ [9/11] Key2 fallback: skip ChangeFileSettings (avoid permission fail)")
            }
            try {
                isoDep.close()
            } catch (_: Exception) {
                // ignore close error
            }

            val initUrl = buildSunTemplateUrl(base = SUN_BASE_URL, publicUid = publicUid, lastCounter = 0)
            if (!initUrl.startsWith("https://")) {
                initFail("[10/11] URL template", "sunBaseUrl is not HTTPS")
                return
            }
            val msg = NdefMessage(arrayOf(NdefRecord.createUri(initUrl)))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    initFail("[10/11] NDEF write", "Tag is read-only")
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(msg)
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable == null) {
                    initFail("[10/11] NDEF write", "Tag does not support NDEF")
                    return
                }
                formatable.connect()
                formatable.format(msg)
                formatable.close()
            }

            uidText = tag.id?.joinToString("") { b -> "%02X".format(b) } ?: "Read failed"
            appendInitStep("✅ [10/11] Init complete: EV2/SDM configured, URL template written")
            appendInitStep("⏳ [11/11] Post-init verification (read and verify URL)")
            val verify = checkInitStatusByNdef(tag)
            if (verify.url.isNullOrBlank() || verify.status.startsWith("❌")) {
                initFail("[11/11] Init verify", verify.status)
                return
            }
            readUrlText = verify.url
            readParamText = verify.paramText ?: ""
            appendInitStep("✅ [11/11] Init verify passed: ${verify.status}")
            appendInitStep("key0=${HARD_CODED_KEY0.take(8)}... masterKey=${HARD_CODED_MASTER_KEY.take(8)}...")
            appendInitStep("publicUID=$publicUid")
        } catch (e: Exception) {
            initFail("[X] Uncaught exception", e.message ?: "unknown error")
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
    preBalance: String?,
    postBalance: String?,
    cardCurrency: String?,
    address: String?,
    memberNo: String?,
    cardBackground: String?,
    cardImage: String?,
    tierName: String?,
    tierDescription: String?,
    settlementViaQr: Boolean = false,
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
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Tap card to read UID...", fontSize = 16.sp)
                    }
                }
            }
            is TopupStatus.Loading -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Sign & execute", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Completing top-up…", fontSize = 13.sp, color = Color(0xFF64748b))
                        if (uid.isNotBlank()) {
                            Text(text = "UID: $uid", fontSize = 12.sp, color = Color(0xFF86868b))
                        }
                    }
                }
            }
            is TopupStatus.Success -> TopupSuccessContent(
                modifier = Modifier.fillMaxSize(),
                amount = amount,
                txHash = txHash,
                preBalance = preBalance,
                postBalance = postBalance,
                cardCurrency = cardCurrency,
                address = address,
                memberNo = memberNo,
                cardBackground = cardBackground,
                cardImage = cardImage,
                tierName = tierName,
                tierDescription = tierDescription,
                settlementViaQr = settlementViaQr,
                onDone = onBack
            )
            is TopupStatus.Error -> {
                val showErrorDialog = error.isNotBlank()
                if (showErrorDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = onBack,
                        title = { Text("Top-up Not Allowed", fontSize = 18.sp) },
                        text = { Text(error, fontSize = 14.sp) },
                        confirmButton = {
                            TextButton(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("OK") }
                        }
                    )
                }
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
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
    preBalance: String?,
    postBalance: String?,
    cardCurrency: String?,
    address: String?,
    memberNo: String?,
    cardBackground: String?,
    cardImage: String?,
    tierName: String?,
    tierDescription: String?,
    settlementViaQr: Boolean = false,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val postBalanceNum = postBalance?.toDoubleOrNull()
    val currency = cardCurrency ?: "CAD"
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = address?.let { if (it.length > 10) "${it.take(6)}...${it.takeLast(4)}" else it } ?: "—"
    val displayMemberNo = (memberNo?.takeIf { it.isNotBlank() } ?: shortAddr).ifEmpty { "—" }
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash
    val cardBgColor = parseHexColor(cardBackground) ?: Color(0xFF2C5535)

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
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(32.dp), tint = Color(0xFF34C759))
            }
            val isFirstTopUp = memberNo.isNullOrBlank()
            Text(
                if (isFirstTopUp) "Card Minted" else "Top-Up Complete",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
            val (amtPrefix, amtStr, amtSuffix) = formatBalanceWithCurrencyProtocol(amountNum, currency)
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                Row(verticalAlignment = Alignment.Bottom) {
                    if (amtPrefix.isNotEmpty()) Text(amtPrefix, fontSize = 10.sp, color = Color.Black)
                    Text(amtStr, fontSize = 36.sp, fontWeight = FontWeight.Light, color = Color.Black)
                    if (amtSuffix.isNotEmpty()) Text(amtSuffix, fontSize = 10.sp, color = Color.Black)
                }
            }
        }

        // Voucher Balance card（统一会员卡风格）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
        ) {
            val iconBgColor = Color(0xFF3C6A43)
            val accentGreen = Color(0xFF6ED088)
            val labelGrey = Color(0xFFBBBBBB)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 16.dp)
                ) {
                    if (cardImage != null && cardImage.isNotBlank()) {
                        AsyncImage(
                            model = cardImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .size(176.dp, 140.dp)
                                .align(Alignment.TopStart)
                        )
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (cardImage == null || cardImage.isBlank()) {
                                Icon(Icons.Filled.Favorite, null, Modifier.size(44.dp), tint = accentGreen)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .then(if (cardImage != null && cardImage.isNotBlank()) Modifier.padding(start = 176.dp) else Modifier),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    tierName?.takeIf { it.isNotBlank() }?.removeSuffix(" CARD")?.removeSuffix(" Card") ?: "Card",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                val subtitle = tierDescription?.takeIf { it.isNotBlank() }?.takeIf { !it.equals("Card", ignoreCase = true) } ?: ""
                                if (subtitle.isNotBlank()) {
                                    Text(subtitle, fontSize = 12.sp, color = labelGrey)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Member No.", fontSize = 11.sp, color = labelGrey)
                            Text(
                                displayMemberNo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Balance", fontSize = 11.sp, color = labelGrey)
                            if (postBalanceNum != null) {
                                val (prefix, amtStr, suffix) = formatBalanceWithCurrencyProtocol(postBalanceNum, currency)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (prefix.isNotEmpty()) {
                                        Text(prefix, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 2.dp))
                                    }
                                    Text(amtStr, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                                    if (suffix.isNotEmpty()) {
                                        Text(suffix, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 2.dp))
                                    }
                                }
                            } else {
                                Text("—", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                            }
                        }
                    }
                }
            }

            // Receipt details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", fontSize = 13.sp, color = Color(0xFF86868b))
                        Text("$dateString, $timeString", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Member No.", fontSize = 13.sp, color = Color(0xFF86868b))
                        Text(displayMemberNo, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    if (txHash.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TX Hash", fontSize = 13.sp, color = Color(0xFF86868b))
                            Text(shortTxHash, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Settlement", fontSize = 13.sp, color = Color(0xFF86868b))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, null, Modifier.size(12.dp), tint = Color(0xFF34C759))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (settlementViaQr) "App Validator" else "NTAG 424 DNA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF34C759)
                            )
                        }
                    }
                }
            }
        }

        // Print and Done buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { printTopupReceipt(context, amount, postBalance, cardCurrency, address, txHash, dateString, timeString) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.Print, null, Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print Receipt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Done", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun formatWithThousands(s: String?): String {
    if (s.isNullOrBlank()) return "0"
    val n = s.toDoubleOrNull() ?: return s
    return java.util.Locale.US.run { "%,.2f".format(n) }
}

/** Beamio currency 协议：fiat 用前缀符号（CA$、$、€ 等），USDC 用后缀。返回 (prefix, amountStr, suffix) 供 UI 分别渲染，prefix/suffix 用小字号 */
private fun formatBalanceWithCurrencyProtocol(amount: Double, currency: String): Triple<String, String, String> {
    val amtStr = java.lang.String.format(java.util.Locale.US, "%,.2f", amount)
    val ccy = currency.uppercase()
    val prefix = when (ccy) {
        "CAD" -> "CA$"
        "USD" -> "$"
        "EUR" -> "€"
        "JPY" -> "JP¥"
        "CNY" -> "CN¥"
        "HKD" -> "HK$"
        "SGD" -> "SG$"
        "TWD" -> "NT$"
        else -> ""
    }
    val suffix = if (ccy == "USDC") " USDC" else ""
    return Triple(prefix, amtStr, suffix)
}

private fun formatForReceipt(amount: String?, currency: String): String {
    val num = amount?.toDoubleOrNull() ?: 0.0
    val (prefix, amtStr, suffix) = formatBalanceWithCurrencyProtocol(num, currency)
    return if (prefix.isNotEmpty()) "$prefix$amtStr" else "$amtStr$suffix"
}

private fun printTopupReceipt(
    context: Context,
    amount: String,
    postBalance: String?,
    cardCurrency: String?,
    address: String?,
    txHash: String,
    dateString: String,
    timeString: String
) {
    val currency = cardCurrency ?: "CAD"
    val shortAddr = address?.let { if (it.length > 10) "${it.take(6)}...${it.takeLast(4)}" else it } ?: "—"
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash
    val lines = listOf(
        "TOP-UP COMPLETE",
        "",
        "Amount: ${formatForReceipt(amount, currency)}",
        "Card Balance: ${formatForReceipt(postBalance, currency)}",
        "",
        "Date: $dateString, $timeString",
        "Account ID: $shortAddr",
        "TX Hash: $shortTxHash",
        "",
        "Settlement: NTAG 424 DNA"
    )
    printReceiptPdf(context, "Top-Up Receipt", lines)
}

private fun printChargeReceipt(
    context: Context,
    amount: String,
    subtotal: String?,
    tip: String?,
    postBalance: String?,
    cardCurrency: String?,
    payee: String,
    txHash: String,
    dateString: String,
    timeString: String,
    tableNumber: String? = null
) {
    val currency = cardCurrency ?: "CAD"
    val shortAddr = if (payee.length > 10) "${payee.take(6)}...${payee.takeLast(4)}" else payee
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash
    val lines = mutableListOf(
        "PAYMENT APPROVED",
        "",
        "Amount: ${formatForReceipt(amount, currency)}",
        "Card Balance: ${formatForReceipt(postBalance, currency)}"
    )
    if (subtotal != null) {
        lines.add("Voucher Deduction: ${formatForReceipt(subtotal, currency)}")
        if (tip != null && tip.toDoubleOrNull()?.let { it > 0 } == true) {
            lines.add("Tip: ${formatForReceipt(tip, currency)}")
        }
    }
    if (!tableNumber.isNullOrBlank()) {
        lines.add("Table number: ${tableNumber.trim()}")
    }
    lines.addAll(listOf("", "Date: $dateString, $timeString", "Account ID: $shortAddr", "TX Hash: $shortTxHash", "", "Settlement: NTAG 424 DNA"))
    printReceiptPdf(context, "Payment Receipt", lines)
}

private fun printReceiptPdf(context: Context, title: String, lines: List<String>) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val jobName = "${context.getString(R.string.app_name)} - $title"
    val adapter = object : PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            metadata: android.os.Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder("receipt.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out android.print.PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            try {
                val pdfDoc = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(216, 432, 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 12f
                    typeface = Typeface.DEFAULT
                }
                val titlePaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                var y = 40f
                canvas.drawText(title, 20f, y, titlePaint)
                y += 30f
                for (line in lines) {
                    if (cancellationSignal?.isCanceled == true) {
                        pdfDoc.close()
                        callback.onWriteCancelled()
                        return
                    }
                    canvas.drawText(line, 20f, y, paint)
                    y += 18f
                }
                pdfDoc.finishPage(page)
                pdfDoc.writeTo(android.os.ParcelFileDescriptor.AutoCloseOutputStream(destination))
                pdfDoc.close()
                callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            } catch (e: Exception) {
                Log.e("PrintReceipt", "Print failed", e)
                callback.onWriteFailed(e.message)
            }
        }
    }
    printManager.print(jobName, adapter, null)
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

/** Member # from one card: API `primaryMemberTokenId` (max `tiers[i].minUsdc6`) or legacy max tokenId. */
private fun memberNoFromCardItem(card: CardItem?): String {
    if (card == null) return ""
    val primary = card.primaryMemberTokenId?.trim().orEmpty()
    if (primary.isNotEmpty() && (primary.toLongOrNull() ?: 0L) > 0L) {
        return "M-%s".format(primary.padStart(6, '0'))
    }
    val legacy = card.nfts
        .filter { it.tokenId.toLongOrNull()?.let { id -> id > 0 } == true }
        .maxByOrNull { it.tokenId.toLongOrNull() ?: 0L }
        ?.tokenId
    return legacy?.let { "M-%s".format(it.padStart(6, '0')) } ?: ""
}

/** `cards` are server-sorted by best-tier minUsdc6 descending. */
private fun memberNoPrimaryFromSortedCardsItem(assets: UIDAssets): String {
    for (c in assets.cards.orEmpty()) {
        val m = memberNoFromCardItem(c)
        if (m.isNotEmpty()) return m
    }
    val legacy = assets.nfts
        ?.filter { it.tokenId.toLongOrNull()?.let { id -> id > 0 } == true }
        ?.maxByOrNull { it.tokenId.toLongOrNull() ?: 0L }
        ?.tokenId
    return legacy?.let { "M-%s".format(it.padStart(6, '0')) } ?: ""
}

/** Balance Loaded：单张 Pass 卡展示；compact 时压缩边距与字号以适配一屏 */
@Composable
private fun ReadBalancePassCard(
    card: CardItem,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val balanceNum = card.points.toDoubleOrNull() ?: 0.0
    val memberNo = memberNoFromCardItem(card)
    val bgColor = parseHexColor(card.cardBackground) ?: Color(0xFF2C5535)
    val accentGreen = Color(0xFF6ED088)
    val labelGrey = Color(0xFFBBBBBB)
    val padS = if (compact) 12.dp else 18.dp
    val padT = if (compact) 10.dp else 18.dp
    val padB = if (compact) 10.dp else 14.dp
    val padE = if (compact) 12.dp else 18.dp
    val imgW = if (compact) 100.dp else 152.dp
    val imgH = if (compact) 80.dp else 118.dp
    val iconSz = if (compact) 30.dp else 40.dp
    val titleFs = if (compact) 13.sp else 15.sp
    val subFs = if (compact) 10.sp else 12.sp
    val memFs = if (compact) 12.sp else 15.sp
    val balLblFs = if (compact) 9.sp else 11.sp
    val balSideFs = if (compact) 11.sp else 14.sp
    val balMainFs = if (compact) 17.sp else 22.sp
    val corner = if (compact) 16.dp else 22.dp
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padS, top = padT, end = padE, bottom = padB)
        ) {
            if (card.cardImage != null && card.cardImage!!.isNotBlank()) {
                AsyncImage(
                    model = card.cardImage,
                    contentDescription = card.cardName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .size(imgW, imgH)
                        .align(Alignment.TopStart)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.cardImage == null || card.cardImage!!.isBlank()) {
                        Icon(Icons.Filled.Favorite, null, Modifier.size(iconSz), tint = accentGreen)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (card.cardImage != null && card.cardImage!!.isNotBlank()) {
                                    Modifier.padding(start = imgW + 6.dp)
                                } else {
                                    Modifier
                                }
                            ),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            card.cardName.removeSuffix(" CARD").removeSuffix(" Card"),
                            fontSize = titleFs,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val subtitle = card.tierName ?: card.cardType.takeIf { it.isNotBlank() && it.lowercase() != "infrastructure" }?.replaceFirstChar { it.uppercase() } ?: ""
                        if (subtitle.isNotBlank() && !subtitle.equals("Card", ignoreCase = true)) {
                            Text(subtitle, fontSize = subFs, color = labelGrey)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Member No.", fontSize = balLblFs, color = labelGrey)
                    Text(
                        memberNo.ifEmpty { "—" },
                        fontSize = memFs,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Balance", fontSize = balLblFs, color = labelGrey)
                    val (prefix, amtStr, suffix) = formatBalanceWithCurrencyProtocol(balanceNum, card.cardCurrency)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (prefix.isNotEmpty()) {
                            Text(prefix, fontSize = balSideFs, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 1.dp))
                        }
                        Text(amtStr, fontSize = balMainFs, fontWeight = FontWeight.Bold, color = accentGreen)
                        if (suffix.isNotEmpty()) {
                            Text(suffix, fontSize = balSideFs, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReadScreen(
    uid: String,
    status: ReadStatus,
    assets: UIDAssets?,
    rawResponseJson: String? = null,
    error: String,
    settlementViaQr: Boolean = false,
    onBack: () -> Unit,
    onTopupClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Tap card to read UID...", fontSize = 16.sp)
                    }
                }
            }
            is ReadStatus.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator()
                        Text(text = "Querying...", fontSize = 14.sp)
                        if (uid.isNotBlank()) Text(text = "UID: $uid", fontSize = 12.sp)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            is ReadStatus.Success -> {
                val balanceScroll = rememberScrollState()
                var responseExpanded by remember(rawResponseJson) { mutableStateOf(false) }
                var topupButtonEnabled by remember { mutableStateOf(true) }
                // 每次进入 Balance Loaded（uid / 账户变化）重新计时 1 分钟
                LaunchedEffect(uid, assets?.aaAddress, assets?.address) {
                    topupButtonEnabled = true
                    delay(60_000L)
                    topupButtonEnabled = false
                }
                val cardList = assets?.cards?.takeIf { it.isNotEmpty() }
                    ?: listOfNotNull(assets?.cardAddress?.let { addr ->
                        // 兼容旧接口：若仅返回单卡字段，也按“服务端返回资产”展示，不做 CCSA/基础设施硬编码。
                        CardItem(
                            cardAddress = addr,
                            cardName = "Asset Card",
                            cardType = "",
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
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxHeight < 560.dp
                    val sidePad = if (compact) 12.dp else 16.dp
                    val gapSm = if (compact) 6.dp else 8.dp
                    val headerBox = if (compact) 36.dp else 46.dp
                    val headerIcon = if (compact) 22.dp else 30.dp
                    val passRowH = if (compact) 142.dp else 176.dp
                    val pageCardW = (maxWidth - sidePad * 2 - 20.dp).coerceAtLeast(160.dp)
                    val accInnerPad = if (compact) 10.dp else 14.dp
                    val accSpacing = if (compact) 6.dp else 8.dp
                    val usdcPadH = if (compact) 12.dp else 16.dp
                    val usdcPadV = if (compact) 10.dp else 14.dp
                    val usdcTitleSp = if (compact) 13.sp else 14.sp
                    val usdcAmtSp = if (compact) 16.sp else 17.sp
                    val btnH = if (compact) 44.dp else 48.dp
                    val bottomPad = if (compact) 10.dp else 14.dp
                    Column(Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = sidePad, vertical = if (compact) 4.dp else 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(headerBox)
                                    .background(Color.White, CircleShape)
                                    .padding(if (compact) 5.dp else 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, Modifier.size(headerIcon), tint = Color(0xFF34C759))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Balance Loaded",
                                fontSize = if (compact) 17.sp else 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth()
                                .verticalScroll(balanceScroll)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = sidePad)
                                    .padding(bottom = gapSm)
                            ) {
                                val eoaAddr = assets?.address?.takeIf { it.isNotEmpty() }
                                val aaAddr = assets?.aaAddress?.takeIf { it.isNotEmpty() }
                                val displayUid = assets?.uid?.takeIf { it.isNotEmpty() } ?: uid.takeIf { it.isNotEmpty() }
                                val tagId = assets?.tagIdHex?.takeIf { it.isNotEmpty() }
                                val counterVal = assets?.counter
                                if (eoaAddr != null || aaAddr != null || displayUid != null || tagId != null || counterVal != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = if (compact) 1.dp else 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(accInnerPad),
                                            verticalArrangement = Arrangement.spacedBy(accSpacing)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Account", fontSize = if (compact) 11.sp else 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                counterVal?.let { cnt ->
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(999.dp))
                                                            .background(Color.Black.copy(alpha = 0.06f))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Refresh, null, Modifier.size(11.dp), tint = Color.Gray)
                                                        Text("$cnt", fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.Black)
                                                    }
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                displayUid?.let { uidVal ->
                                                    HexCopyCapsule(
                                                        value = uidVal,
                                                        copyLabel = "uid",
                                                        leadingIcon = {
                                                            Icon(Icons.Filled.Fingerprint, null, Modifier.size(11.dp), tint = Color(0xFF1562f0))
                                                        }
                                                    )
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                if (tagId != null) {
                                                    HexCopyCapsule(
                                                        value = tagId,
                                                        copyLabel = "tagId",
                                                        leadingIcon = {
                                                            Icon(Icons.AutoMirrored.Filled.Label, null, Modifier.size(11.dp), tint = Color(0xFF7C3AED))
                                                        }
                                                    )
                                                } else {
                                                    Row(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(999.dp))
                                                            .background(Color.Black.copy(alpha = 0.06f))
                                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.Label, null, Modifier.size(11.dp), tint = Color(0xFF7C3AED))
                                                        Text("—", fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(gapSm))
                                }
                                val usdcBal = assets?.usdcBalance?.toDoubleOrNull() ?: 0.0
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1c1c1e))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = usdcPadH, vertical = usdcPadV),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("USDC on Base", fontSize = usdcTitleSp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                java.lang.String.format(java.util.Locale.US, "%,.2f", usdcBal),
                                                fontSize = usdcAmtSp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                " USDC",
                                                fontSize = if (compact) 10.sp else 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.padding(bottom = 1.dp)
                                            )
                                        }
                                    }
                                }
                                if (cardList != null) {
                                    Spacer(modifier = Modifier.height(gapSm))
                                    Text(
                                        "${cardList.size} Passes",
                                        fontSize = if (compact) 10.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
                                    if (cardList.size > 1) {
                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(passRowH),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(end = 4.dp)
                                        ) {
                                            itemsIndexed(cardList) { _, card ->
                                                ReadBalancePassCard(
                                                    card = card,
                                                    compact = true,
                                                    modifier = Modifier.width(pageCardW)
                                                )
                                            }
                                        }
                                    } else {
                                        ReadBalancePassCard(
                                            card = cardList.first(),
                                            compact = compact,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                if (rawResponseJson != null && rawResponseJson.isNotBlank()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = gapSm)
                                            .clickable { responseExpanded = !responseExpanded },
                                        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                                    ) {
                                        Column(modifier = Modifier.padding(if (compact) 10.dp else 14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Response Data",
                                                    fontSize = if (compact) 13.sp else 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF86868b)
                                                )
                                                Text(
                                                    if (responseExpanded) "▼" else "▶",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF86868b)
                                                )
                                            }
                                            if (responseExpanded) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = if (compact) 160.dp else 240.dp)
                                                        .verticalScroll(rememberScrollState())
                                                        .background(Color(0xFFf8fafc), RoundedCornerShape(8.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        text = try {
                                                            org.json.JSONObject(rawResponseJson).toString(2)
                                                        } catch (_: Exception) {
                                                            rawResponseJson
                                                        },
                                                        fontSize = 9.sp,
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        color = Color(0xFF64748b)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = sidePad)
                                .padding(top = gapSm, bottom = bottomPad)
                        ) {
                            val actionBtnH = btnH
                            Button(
                                onClick = onTopupClick,
                                enabled = topupButtonEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(actionBtnH),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1562f0),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF1562f0).copy(alpha = 0.35f),
                                    disabledContentColor = Color.White.copy(alpha = 0.55f)
                                )
                            ) {
                                Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Top-Up Card Now", fontSize = if (compact) 13.sp else 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(actionBtnH),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                            ) {
                                Text("Done", fontSize = if (compact) 13.sp else 14.sp, fontWeight = FontWeight.SemiBold)
                            }
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
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
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
    memberNo: String?,
    cardBackground: String? = null,
    cardImage: String? = null,
    cardName: String? = null,
    tierName: String? = null,
    cardType: String? = null,
    settlementViaQr: Boolean = false,
    /** ScanMethod 页顶栏 overlay 占位，避免 Approved 标题被 Back 栏遮住 */
    extraTopInset: Dp = 0.dp,
    /** 与底部 Total 明细一致：税率 % */
    chargeTaxPercent: Double? = null,
    /** 与底部 Total 明细一致：等级折扣 % */
    chargeTierDiscountPercent: Int? = null,
    tableNumber: String? = null,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val subtotalNum = subtotal?.toDoubleOrNull()
    val tipNum = tip?.toDoubleOrNull()
    val postBalanceNum = postBalance?.toDoubleOrNull()
    val currency = cardCurrency ?: "CAD"
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = if (payee.length > 10) "${payee.take(6)}...${payee.takeLast(4)}" else payee
    val displayMemberNo = (memberNo?.takeIf { it.isNotBlank() } ?: shortAddr).ifEmpty { "—" }
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash

    // 全屏高度：顶栏 Approved 固定、底栏按钮固定；中间区域占满剩余高度，仅必要时在中间滚动（避免整页超长滚动条）
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFf5f5f7))
    ) {
        val tightH = maxHeight < 580.dp
        val vCompactH = maxHeight < 460.dp
        val headerTopPad = if (vCompactH) 8.dp else if (tightH) 14.dp else 24.dp
        val headerBottomPad = if (vCompactH) 6.dp else if (tightH) 8.dp else 12.dp
        val amountTextSp = when {
            vCompactH -> 26.sp
            tightH -> 30.sp
            else -> 36.sp
        }
        val cardImgW = when {
            vCompactH -> 112.dp
            tightH -> 144.dp
            else -> 176.dp
        }
        val cardImgH = when {
            vCompactH -> 88.dp
            tightH -> 115.dp
            else -> 140.dp
        }
        val hPad = if (vCompactH) 14.dp else 20.dp
        val sectionGapBelowCards = if (vCompactH) 6.dp else if (tightH) 8.dp else 12.dp
        val routingTopPad = if (vCompactH) 6.dp else 8.dp
        val receiptTopPad = if (vCompactH) 8.dp else 12.dp
        val receiptInnerV = if (vCompactH) 6.dp else 8.dp
        val scrollMid = rememberScrollState()

        Column(Modifier.fillMaxSize()) {
            // Header: Approved + amount（无顶部 icon）；Scan 流程下 extraTopInset 与顶栏 overlay 对齐
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = headerTopPad + extraTopInset, bottom = headerBottomPad),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Approved",
                    fontSize = if (vCompactH) 18.sp else 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                val (amtPrefix, amtStr, amtSuffix) = formatBalanceWithCurrencyProtocol(amountNum, currency)
                Row(
                    modifier = Modifier.padding(top = if (vCompactH) 2.dp else 4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("-", fontSize = if (vCompactH) 14.sp else 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (amtPrefix.isNotEmpty()) Text(amtPrefix, fontSize = if (vCompactH) 9.sp else 10.sp, color = Color.Black)
                        Text(amtStr, fontSize = amountTextSp, fontWeight = FontWeight.Light, color = Color.Black)
                        if (amtSuffix.isNotEmpty()) Text(amtSuffix, fontSize = if (vCompactH) 9.sp else 10.sp, color = Color.Black)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .verticalScroll(scrollMid)
            ) {
                // Voucher Balance card（对齐 Balance Loaded 风格：背景色、图片、布局）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad)
                        .padding(bottom = sectionGapBelowCards)
                ) {
                    val cardBgColor = parseHexColor(cardBackground) ?: Color(0xFF2C5535)
                    val accentGreen = Color(0xFF6ED088)
                    val labelGrey = Color(0xFFBBBBBB)
                    val voucherInnerPad = if (vCompactH) 14.dp else 20.dp
                    val voucherInnerBottom = if (vCompactH) 12.dp else 16.dp
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = voucherInnerPad, top = voucherInnerPad, end = voucherInnerPad, bottom = voucherInnerBottom)
                        ) {
                            if (cardImage != null && cardImage.isNotBlank()) {
                                AsyncImage(
                                    model = cardImage,
                                    contentDescription = "Card",
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(cardImgW, cardImgH)
                                        .align(Alignment.TopStart)
                                )
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (cardImage == null || cardImage.isBlank()) {
                                        Icon(Icons.Filled.Favorite, null, Modifier.size(if (vCompactH) 36.dp else 44.dp), tint = accentGreen)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .then(if (cardImage != null && cardImage.isNotBlank()) Modifier.padding(start = cardImgW) else Modifier),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            (cardName?.takeIf { it.isNotBlank() } ?: "Card").removeSuffix(" CARD").removeSuffix(" Card"),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        val subtitle = tierName ?: cardType?.takeIf { it.isNotBlank() && it.lowercase() != "infrastructure" }?.replaceFirstChar { it.uppercase() } ?: ""
                                        if (subtitle.isNotBlank() && !subtitle.equals("Card", ignoreCase = true)) {
                                            Text(subtitle, fontSize = 12.sp, color = labelGrey)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Member No.", fontSize = 11.sp, color = labelGrey)
                                    Text(
                                        displayMemberNo,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Balance", fontSize = 11.sp, color = labelGrey)
                                    if (postBalanceNum != null) {
                                        val (prefix, amtStr, suffix) = formatBalanceWithCurrencyProtocol(postBalanceNum, currency)
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            if (prefix.isNotEmpty()) {
                                                Text(prefix, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                            Text(amtStr, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                                            if (suffix.isNotEmpty()) {
                                                Text(suffix, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = accentGreen, modifier = Modifier.padding(bottom = 2.dp))
                                            }
                                        }
                                    } else {
                                        Text("—", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentGreen)
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
                        .padding(top = routingTopPad),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(if (vCompactH) 10.dp else 12.dp)) {
                        Text(
                            "Smart Routing Engine",
                            fontSize = if (vCompactH) 13.sp else 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = if (vCompactH) 6.dp else 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Voucher Deduction", fontSize = 15.sp, color = Color(0xFF86868b))
                            val (sPrefix, sAmt, sSuffix) = formatBalanceWithCurrencyProtocol(subtotalNum, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (sPrefix.isNotEmpty()) Text(sPrefix, fontSize = 11.sp, color = Color.Black)
                                Text(sAmt, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                if (sSuffix.isNotEmpty()) Text(sSuffix, fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        val taxP = chargeTaxPercent ?: 0.0
                        val taxAmt = subtotalNum * taxP / 100.0
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tax (${String.format("%.2f", taxP)}%)",
                                fontSize = 15.sp,
                                color = Color(0xFF86868b)
                            )
                            val (txPrefix, txStr, txSuffix) = formatBalanceWithCurrencyProtocol(taxAmt, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (txPrefix.isNotEmpty()) Text(txPrefix, fontSize = 11.sp, color = Color.Black)
                                Text(txStr, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                if (txSuffix.isNotEmpty()) Text(txSuffix, fontSize = 11.sp, color = Color.Black)
                            }
                        }
                        val discP = chargeTierDiscountPercent
                        val discAmt = if (discP != null) subtotalNum * discP / 100.0 else null
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (discP != null && discP > 0) {
                                Text(
                                    "Tier discount ($discP%)",
                                    fontSize = 15.sp,
                                    color = Color(0xFF86868b)
                                )
                                val (dPrefix, dStr, dSuffix) = formatBalanceWithCurrencyProtocol(discAmt ?: 0.0, currency)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("-", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF059669))
                                    if (dPrefix.isNotEmpty()) Text(dPrefix, fontSize = 11.sp, color = Color(0xFF059669))
                                    Text(dStr, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF059669))
                                    if (dSuffix.isNotEmpty()) Text(dSuffix, fontSize = 11.sp, color = Color(0xFF059669))
                                }
                            } else {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tier discount", fontSize = 15.sp, color = Color(0xFF86868b))
                                    Text("Not applied", fontSize = 11.sp, color = Color(0xFF94a3b8))
                                }
                                Text("—", fontSize = 15.sp, color = Color(0xFF94a3b8))
                            }
                        }
                        if (tipNum != null && tipNum > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tip", fontSize = 15.sp, color = Color(0xFF86868b))
                                val (tPrefix, tAmt, tSuffix) = formatBalanceWithCurrencyProtocol(tipNum, currency)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    if (tPrefix.isNotEmpty()) Text(tPrefix, fontSize = 11.sp, color = Color.Black)
                                    Text(tAmt, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                    if (tSuffix.isNotEmpty()) Text(tSuffix, fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Receipt details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = receiptTopPad),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(horizontal = if (vCompactH) 12.dp else 16.dp, vertical = receiptInnerV)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", fontSize = 13.sp, color = Color(0xFF86868b))
                        Text("$dateString, $timeString", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    if (!tableNumber.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Table number", fontSize = 13.sp, color = Color(0xFF86868b))
                            Text(tableNumber.trim(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Member No.", fontSize = 13.sp, color = Color(0xFF86868b))
                        Text(displayMemberNo, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                    if (txHash.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TX Hash", fontSize = 13.sp, color = Color(0xFF86868b))
                            Text(shortTxHash, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1562f0))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Settlement", fontSize = 13.sp, color = Color(0xFF86868b))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, null, Modifier.size(12.dp), tint = Color(0xFF34C759))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (settlementViaQr) "App Validator" else "NTAG 424 DNA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF34C759)
                            )
                        }
                    }
                }
            }
        }
            }

            // Print and Done buttons（贴底 + 导航栏安全区，避免整页滚动）
            val approvedActionButtonModifier = Modifier
                .fillMaxWidth()
                .height(if (vCompactH) 44.dp else 48.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = hPad, vertical = if (vCompactH) 6.dp else 10.dp)
                    .padding(bottom = if (vCompactH) 6.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (vCompactH) 6.dp else 8.dp)
            ) {
            OutlinedButton(
                onClick = {
                    printChargeReceipt(
                        context,
                        amount,
                        subtotal,
                        tip,
                        postBalance,
                        cardCurrency,
                        payee,
                        txHash,
                        dateString,
                        timeString,
                        tableNumber
                    )
                },
                modifier = approvedActionButtonModifier,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.Print, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print Receipt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onDone,
                modifier = approvedActionButtonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Done", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            }
        }
    }
}

@Composable
private fun PaymentRoutingMonitorRow(step: RoutingStep) {
    val mono = FontFamily.Monospace
    val lineColor = Color(0xFF8AE06C)
    val pendingColor = Color(0xFF5c6b5c)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step.status) {
                StepStatus.loading -> CircularProgressIndicator(
                    modifier = Modifier.size(11.dp),
                    strokeWidth = 1.2.dp,
                    color = lineColor
                )
                StepStatus.success -> Text("OK", fontSize = 8.sp, fontFamily = mono, color = lineColor)
                StepStatus.error -> Text("NO", fontSize = 8.sp, fontFamily = mono, color = Color(0xFFFF8A80))
                StepStatus.pending -> Text("--", fontSize = 8.sp, fontFamily = mono, color = pendingColor)
            }
        }
        val line = buildString {
            append(step.label)
            if (step.detail.isNotBlank()) {
                append(" ")
                append(step.detail)
            }
        }
        Text(
            text = line,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontFamily = mono,
            color = when (step.status) {
                StepStatus.pending -> pendingColor
                StepStatus.error -> Color(0xFFFFB4A8)
                else -> lineColor
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Smart Routing 中央区域：整块尺寸即为深色「显示屏」（无白底、无标题），无滚动条；
 * 仅显示最近 [PAYMENT_ROUTING_MONITOR_MAX_VISIBLE_STEPS] 条，新行从底部加入时顶行移出。
 */
@Composable
internal fun PaymentRoutingMonitorDisplayCard(
    steps: List<RoutingStep>,
    modifier: Modifier = Modifier,
    errorLine: String? = null,
    footerLine: String? = null,
    /** English hint below error; shown when [errorLine] is non-blank */
    retryHintEnglish: String? = null,
    onRetryClick: (() -> Unit)? = null
) {
    val visible = filterPaymentRoutingStepsForDisplay(steps).takeLast(PAYMENT_ROUTING_MONITOR_MAX_VISIBLE_STEPS)
    val screenBg = Color(0xFF0f1419)
    val bezelStroke = Color(0xFF3d4553)
    Card(
        modifier = modifier.then(
            if (onRetryClick != null) Modifier.clickable(onClick = onRetryClick) else Modifier
        ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = screenBg),
        border = BorderStroke(2.dp, bezelStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Bottom
            ) {
                visible.forEach { step -> PaymentRoutingMonitorRow(step) }
            }
            if (!errorLine.isNullOrBlank()) {
                Text(
                    text = errorLine,
                    fontSize = 10.sp,
                    color = Color(0xFFFF6B6B),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .basicMarquee()
                )
            }
            if (!retryHintEnglish.isNullOrBlank() && !errorLine.isNullOrBlank()) {
                Text(
                    text = retryHintEnglish,
                    fontSize = 9.sp,
                    color = Color(0xFF7c8a99),
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
            if (!footerLine.isNullOrBlank()) {
                Text(
                    text = footerLine,
                    fontSize = 9.sp,
                    color = Color(0xFF8b939e),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
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
    memberNo: String?,
    cardBackground: String? = null,
    cardImage: String? = null,
    cardName: String? = null,
    tierName: String? = null,
    cardType: String? = null,
    settlementViaQr: Boolean = false,
    chargeTaxPercent: Double? = null,
    chargeTierDiscountPercent: Int? = null,
    tableNumber: String? = null,
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
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Tap card to read UID...", fontSize = 16.sp)
                }
                }
            }
            is PaymentStatus.Routing, is PaymentStatus.Submitting, is PaymentStatus.Refreshing -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        PaymentRoutingMonitorDisplayCard(
                            steps = routingSteps,
                            modifier = Modifier.size(280.dp)
                        )
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
                memberNo = memberNo,
                cardBackground = cardBackground,
                cardImage = cardImage,
                cardName = cardName,
                tierName = tierName,
                cardType = cardType,
                settlementViaQr = settlementViaQr,
                extraTopInset = 0.dp,
                chargeTaxPercent = chargeTaxPercent,
                chargeTierDiscountPercent = chargeTierDiscountPercent,
                tableNumber = tableNumber,
                onDone = onBack
            )
            is PaymentStatus.Error -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Button(onClick = onBack, modifier = Modifier.height(24.dp)) { Text("Back") }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        PaymentRoutingMonitorDisplayCard(
                            steps = routingSteps,
                            modifier = Modifier.size(280.dp),
                            errorLine = error,
                            footerLine = uid.takeIf { it.isNotBlank() }?.let { "UID: $it" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomePanelNoAA(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val padding = if (compact) 0.dp else 24.dp
    val cardPadding = if (compact) 10.dp else 32.dp
    val titleSize = if (compact) 14.sp else 24.sp
    val bodySize = if (compact) 10.sp else 15.sp
    val bodyLineHeight = if (compact) 13.sp else 22.sp
    val bodyBottomPadding = if (compact) 0.dp else 24.dp
    Column(
        modifier = modifier
            .then(if (!compact) Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars) else Modifier)
            .then(
                if (compact) Modifier.padding(vertical = 4.dp)  // align with other cards
                else Modifier.padding(padding)
            )
    ) {
        if (!compact) Spacer(modifier = Modifier.height(48.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(if (compact) 12.dp else 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1562f0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding)
            ) {
                Text(
                    "Welcome to Beamio Web3 POS!",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = if (compact) 6.dp else 12.dp)
                )
                Text(
                    "Your EOA Vault is ready. You can currently send/receive direct USDC payments. Your Smart Terminal (AA) is locked. To unlock zero-gas routing, VIP memberships, and voucher economies, purchase a Fuel Pack or join an Alliance.",
                    fontSize = bodySize,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = bodyLineHeight,
                    modifier = Modifier.padding(bottom = if (compact) 0.dp else bodyBottomPadding),
                    maxLines = if (compact) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 十六进制值胶囊：leadingIcon（或 label 文本）+ 短缩 hex + copy 图标，点击复制完整值，成功后绿色 check 约 2 秒 */
@Composable
private fun HexCopyCapsule(
    value: String,
    copyLabel: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
    val short = if (value.length >= 10) "${value.take(6)}…${value.takeLast(4)}" else value
    val desc = label ?: copyLabel
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.06f))
            .clickable(enabled = value.isNotEmpty()) {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                cm?.setPrimaryClip(ClipData.newPlainText(copyLabel, value))
                copied = true
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        } else if (label != null) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
        Text(
            short,
            fontSize = 11.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = Color.Black
        )
        Icon(
            if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = "Copy $desc",
            modifier = Modifier.size(12.dp),
            tint = if (copied) Color(0xFF34C759) else Color.Black.copy(alpha = 0.6f)
        )
    }
}

/** 姓名字段：空白或字面量 "null" 视为无值（避免 API/JSON 把 null 序列化成字符串） */
private fun sanitizeProfileNamePart(raw: String?): String {
    val t = raw?.trim() ?: ""
    if (t.isEmpty() || t.equals("null", ignoreCase = true)) return ""
    return t
}

/** Beamio 标准胶囊（紧凑版）：左侧头像，右侧 displayName + @accountName。无 first/last 时不显示第一行；无 @tag 时用 fallback 单行 */
@Composable
private fun BeamioCapsuleCompact(
    profile: TerminalProfile,
    fallbackAddress: String?,
    modifier: Modifier = Modifier
) {
    val tag = sanitizeProfileNamePart(profile.accountName).takeIf { it.isNotEmpty() }
    val beamioTag = tag?.let { "@$it" }
    val first = sanitizeProfileNamePart(profile.first_name)
    val lastRaw = profile.last_name?.trim()?.split("\r\n")?.firstOrNull() ?: ""
    val last = if (lastRaw.startsWith("{")) "" else sanitizeProfileNamePart(lastRaw)
    val displayName = "$first $last".trim()
    val hasNameRow = displayName.isNotEmpty()
    val shortFallback = fallbackAddress?.let { if (it.length >= 10) "${it.take(6)}…${it.takeLast(4)}" else it }
    val avatarSeed = tag ?: "Beamio"
    val avatarUrl = "https://api.dicebear.com/8.x/fun-emoji/png?seed=${java.net.URLEncoder.encode(avatarSeed, "UTF-8")}"
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Black.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = profile.image?.takeIf { it.isNotBlank() } ?: avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (hasNameRow) {
                Text(
                    displayName,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (beamioTag != null) {
                Text(
                    beamioTag,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (!hasNameRow) {
                Text(
                    shortFallback ?: "—",

                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun homeDashboardShimmerBrush(progress: Float): Brush {
    val density = LocalDensity.current
    val widthPx = with(density) { 160.dp.toPx() }
    val travel = progress * widthPx * 2.2f - widthPx * 0.5f
    return Brush.linearGradient(
        colorStops = arrayOf(
            0f to Color.White.copy(alpha = 0.06f),
            0.42f to Color.White.copy(alpha = 0.28f),
            0.58f to Color.White.copy(alpha = 0.28f),
            1f to Color.White.copy(alpha = 0.06f)
        ),
        start = Offset(travel, 0f),
        end = Offset(travel + widthPx * 0.55f, 48f)
    )
}

/** Home 仪表盘金额未就绪：流光占位，替代「—」 */
@Composable
private fun HomeDashboardStatLoadingPlaceholder(
    alignEnd: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "homeDashShimmer")
    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val brush = homeDashboardShimmerBrush(progress)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        Text(
            "$",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.42f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            Modifier
                .width(68.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            Modifier
                .width(28.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}

@Composable
private fun HomeDashboardTaxAndTierRoutingRow(
    taxPercent: Double?,
    discountSummary: String?,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val spacingPx = 8.dp.roundToPx()
        val taxMeasurable = subcompose("tax") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFFFC107).copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Percent,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                }
                Text(
                    taxPercent?.let { v ->
                        String.format(java.util.Locale.US, "%.2f%%", v)
                    } ?: "—",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1
                )
            }
        }.first()
        val taxPlaceable = taxMeasurable.measure(
            Constraints(
                minWidth = 0,
                maxWidth = constraints.maxWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
        )
        val tierMaxW = (constraints.maxWidth - taxPlaceable.width - spacingPx).coerceAtLeast(0)
        val tierMeasurable = subcompose("tier") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF1562f0).copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF1562f0)
                    )
                }
                Text(
                    discountSummary ?: "—",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .basicMarquee()
                )
            }
        }.first()
        val tierPlaceable = tierMeasurable.measure(
            Constraints(
                minWidth = 0,
                maxWidth = tierMaxW,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
        )
        val h = maxOf(taxPlaceable.height, tierPlaceable.height)
        layout(constraints.maxWidth, h) {
            taxPlaceable.placeRelative(0, (h - taxPlaceable.height) / 2)
            tierPlaceable.placeRelative(taxPlaceable.width + spacingPx, (h - tierPlaceable.height) / 2)
        }
    }
}

@Composable
fun NdefScreen(
    uidText: String,
    readUrlText: String,
    readParamText: String,
    walletAddress: String?,
    terminalProfile: TerminalProfile? = null,
    adminProfile: TerminalProfile? = null,
    chargeAmount: Double? = null,
    topUpAmount: Double? = null,
    chargeStatsRpcWarning: Boolean = false,
    topUpStatsRpcWarning: Boolean = false,
    /** From on-chain admin metadata `tierRoutingDiscounts.taxRatePercent`; null until first successful 15s sync */
    infraRoutingTaxPercent: Double? = null,
    /** One line: all tier `discountPercent` in JSON order, e.g. `5% · 10% · 18%` */
    infraRoutingDiscountSummary: String? = null,
    onCopyWalletClick: () -> Unit,
    onLinkAppClick: () -> Unit,
    onReadClick: () -> Unit,
    onTopupClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onCopyUrlClick: () -> Unit,
    contentAboveCharge: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var walletCopied by mutableStateOf(false)
    LaunchedEffect(walletCopied) {
        if (walletCopied) {
            delay(2000)
            walletCopied = false
        }
    }
    LaunchedEffect(walletAddress) {
        Log.d("Home", "[Home Debug] charge panel ID address (walletAddress)=${walletAddress ?: "null"}")
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header: B icon + beamio tag (left) | admin BeamioCapsule (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(top = 16.dp),
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
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_adaptive),
                        contentDescription = "App icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val accountForTag = sanitizeProfileNamePart(terminalProfile?.accountName)
                    val titleLine = accountForTag.takeIf { it.isNotEmpty() }?.let { "@$it" }
                        ?: walletAddress?.let { if (it.length >= 10) "${it.take(6)}…${it.takeLast(4)}" else it }
                        ?: "Terminal"
                    Text(titleLine, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }
            }
            // Right: admin BeamioCapsule (avatar + displayName + @accountName)
            if (adminProfile != null) {
                BeamioCapsuleCompact(
                    profile = adminProfile,
                    fallbackAddress = adminProfile.address,
                    modifier = Modifier
                )
            }
        }

        // Content area (weight fills remaining height, compact)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Summary card (Charges | Top-Ups) - compact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                                Box(
                                    modifier = Modifier.size(18.dp).background(Color(0xFF1562f0).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, null, Modifier.size(10.dp), tint = Color(0xFF1562f0))
                                }
                                Text("Charges", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                                Text("Today", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1562f0), modifier = Modifier.background(Color(0xFF1562f0).copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            if (chargeAmount != null) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val s = "%.2f".format(chargeAmount)
                                    val dot = s.indexOf('.')
                                    val numPart = if (dot >= 0) s.substring(0, dot) else s
                                    val decPart = if (dot >= 0) s.substring(dot) else ""
                                    Text("$", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.alignByBaseline())
                                    Text(numPart, fontSize = 44.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.alignByBaseline())
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.alignByBaseline()
                                    ) {
                                        Text(decPart, fontSize = 14.sp, color = Color(0xFF86868b), modifier = Modifier.alignByBaseline())
                                        if (chargeStatsRpcWarning) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = "Charge stats sync failed",
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFFFFC107)
                                            )
                                        }
                                    }
                                }
                            } else {
                                HomeDashboardStatLoadingPlaceholder(alignEnd = false)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(88.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                                Box(
                                    modifier = Modifier.size(18.dp).background(Color(0xFF34C759).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, null, Modifier.size(10.dp), tint = Color(0xFF34C759))
                                }
                                Text("Top-Ups", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                            }
                            if (topUpAmount != null) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val s = "%.2f".format(topUpAmount)
                                    val dot = s.indexOf('.')
                                    val numPart = if (dot >= 0) s.substring(0, dot) else s
                                    val decPart = if (dot >= 0) s.substring(dot) else ""
                                    Text("$", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.alignByBaseline())
                                    Text(numPart, fontSize = 44.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.alignByBaseline())
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.alignByBaseline()
                                    ) {
                                        Text(decPart, fontSize = 14.sp, color = Color(0xFF86868b), modifier = Modifier.alignByBaseline())
                                        if (topUpStatsRpcWarning) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = "Top-up stats sync failed",
                                                modifier = Modifier.size(14.dp),
                                                tint = Color(0xFFFFC107)
                                            )
                                        }
                                    }
                                }
                            } else {
                                HomeDashboardStatLoadingPlaceholder(alignEnd = true)
                            }
                        }
                    }
                    HomeDashboardTaxAndTierRoutingRow(
                        taxPercent = infraRoutingTaxPercent,
                        discountSummary = infraRoutingDiscountSummary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    // Bottom row: ID + SECURED (compact)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (walletAddress != null) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onCopyWalletClick()
                                        walletCopied = true
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val shortAddr = if (walletAddress.length >= 10) {
                                    walletAddress.take(5) + "..." + walletAddress.takeLast(5)
                                } else walletAddress
                                Text("ID: $shortAddr", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                Icon(
                                    if (walletCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (walletCopied) Color(0xFF34C759) else Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Shield, null, Modifier.size(16.dp), tint = Color(0xFF1562f0))
                            Text("SECURED", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }
                }
            }

            if (contentAboveCharge != null) {
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    contentAboveCharge()
                }
            }

            // Link App (same white card + row layout as Charge / Top-Up / Check Balance)
            Card(
                onClick = onLinkAppClick,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Link, null, Modifier.size(20.dp), tint = Color(0xFF7C3AED))
                            }
                            Column {
                                Text("Link App", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                Text("Scan customer card to link", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                            }
                        }
                        Icon(Icons.Filled.ChevronRight, null, Modifier.size(18.dp), tint = Color(0xFFc7c7cc))
                    }
                }
            }

            // Charge
            Card(
                onClick = onPaymentClick,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFF1562f0).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.QrCode2, null, Modifier.size(20.dp), tint = Color(0xFF1562f0))
                        }
                        Column {
                            Text("Charge", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Accept NFC or QR code", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(18.dp), tint = Color(0xFFc7c7cc))
                }
                }
            }
            Card(
                onClick = onTopupClick,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFF34C759).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, null, Modifier.size(20.dp), tint = Color(0xFF34C759))
                        }
                        Column {
                            Text("Top-Up / Mint", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Load balance or new card", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(18.dp), tint = Color(0xFFc7c7cc))
                }
                }
            }
            Card(
                onClick = onReadClick,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFf4f4f5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Search, null, Modifier.size(20.dp), tint = Color.Black)
                        }
                        Column {
                            Text("Check Balance", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Text("Read member profile", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF86868b))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(18.dp), tint = Color(0xFFc7c7cc))
                }
                }
            }

            if (readUrlText.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = readUrlText, modifier = Modifier.weight(1f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    IconButton(onClick = onCopyUrlClick, modifier = Modifier.size(32.dp)) { Text("📋", fontSize = 12.sp) }
                }
            }
            if (readParamText.isNotBlank()) Text(readParamText, fontSize = 10.sp, color = Color(0xFF64748b), modifier = Modifier.padding(top = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmbeddedQrScanner(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as? ComponentActivity
    AndroidView(
        factory = { ctx ->
            var lastText: String? = null
            DecoratedBarcodeView(ctx).apply {
                getStatusView()?.visibility = View.GONE
                barcodeView.setDecoderFactory(DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE)))
                decodeContinuous(object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult) {
                        val text = result.text ?: return
                        if (text == lastText) return
                        lastText = text
                        activity?.runOnUiThread { onResult(text) }
                    }
                    override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {}
                })
            }
        },
        modifier = modifier,
        update = { view -> view.resume() },
        onRelease = { view -> view.pause() }
    )
}

@Composable
internal fun ScanMethodSelectionScreen(
    scanMethod: String,
    scanWaitingForNfc: Boolean = false,
    nfcFetchingInfo: Boolean = false,
    nfcFetchError: String = "",
    qrScanningActive: Boolean = false,
    /** payment / read：解析失败或需重试时递增以重建 EmbeddedQrScanner */
    embeddedQrResetKey: Int = 0,
    paymentQrInterpreting: Boolean = false,
    paymentQrParseError: String = "",
    onRetryPaymentQrScan: () -> Unit = {},
    /** Charge Smart Routing 失败：点击中央显示器后回到等待贴卡重试 */
    onRetryPaymentAfterError: () -> Unit = {},
    topupQrSigningInProgress: Boolean = false,
    /** NFC 贴卡后：与 QR 共用中央白卡 Sign & execute，不单独进 TopupScreen */
    topupNfcExecuteInProgress: Boolean = false,
    /** 中央执行态可选展示 UID / beamioTag（与 TopupScreen Loading 一致） */
    topupExecuteUidDisplay: String = "",
    topupQrExecuteError: String = "",
    onRetryTopupQrExecute: () -> Unit = {},
    onScanMethodChange: (String) -> Unit,
    pendingAction: String,
    totalAmount: String,
    paymentStatus: PaymentStatus? = null,
    paymentRoutingSteps: List<RoutingStep> = emptyList(),
    paymentError: String = "",
    /** NFC Charge：失败态底部可选展示客户卡 UID */
    paymentNfcUid: String = "",
    paymentAmount: String = "",
    paymentPayee: String = "",
    paymentTxHash: String = "",
    paymentSubtotal: String? = null,
    paymentTip: String? = null,
    paymentPostBalance: String? = null,
    paymentCardCurrency: String? = null,
    paymentMemberNo: String? = null,
    paymentCardBackground: String? = null,
    paymentCardImage: String? = null,
    paymentCardName: String? = null,
    paymentTierName: String? = null,
    paymentCardType: String? = null,
    paymentChargeTaxPercent: Double? = null,
    paymentChargeTierDiscountPercent: Int? = null,
    paymentChargeTipBps: Int = 0,
    paymentTableNumber: String = "",
    onPaymentDone: () -> Unit = {},
    onProceed: () -> Unit,
    onProceedNfcStay: (() -> Unit)? = null,
    /** 中央拉取失败（如读余额）后点击重试；null 时用 onProceedNfcStay */
    onRetryAfterCentralFetchError: (() -> Unit)? = null,
    onProceedWithQr: (() -> Unit)? = null,
    onQrScanResult: (String) -> Unit = {},
    onCancel: () -> Unit,
    /** Link App：仅 NFC，隐藏 Tap Card / Scan QR */
    hideMethodToggle: Boolean = false,
    linkAppDeepLinkUrl: String = "",
    linkAppQrBitmap: Bitmap? = null,
    onCopyLinkAppUrl: () -> Unit = {},
    /** 卡已锁定（409）：展示 Cancel link lock */
    linkAppLockedShowCancel: Boolean = false,
    linkAppCancelInProgress: Boolean = false,
    onLinkAppCancelLock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val showPaymentRouting = pendingAction == "payment" && (
        nfcFetchingInfo ||
        paymentStatus is PaymentStatus.Routing ||
        paymentStatus is PaymentStatus.Submitting ||
        paymentStatus is PaymentStatus.Refreshing
    )
    val showPaymentSuccess = pendingAction == "payment" && paymentStatus is PaymentStatus.Success
    val showPaymentError = pendingAction == "payment" && paymentStatus is PaymentStatus.Error
    /** 贴卡动画/扫码进行中仍可切换 Tap Card ↔ Scan QR */
    val showMethodToggle = !showPaymentSuccess && !showPaymentRouting && !showPaymentError &&
        !topupQrSigningInProgress && !topupNfcExecuteInProgress && topupQrExecuteError.isEmpty() &&
        !(pendingAction == "payment" && paymentQrParseError.isNotEmpty()) &&
        !(pendingAction == "payment" && paymentQrInterpreting)
    val effectiveShowMethodToggle = showMethodToggle && !hideMethodToggle

    /** Charge：底部 Total = request + tax%*request - tier%*request + tip（与链上记账一致）；勿仅用 totalAmount 字符串以免漏税 */
    val bottomTotalDisplay = if (pendingAction == "payment") {
        val req = paymentSubtotal?.toDoubleOrNull() ?: 0.0
        if (req > 0.0) {
            val tip = paymentTip?.toDoubleOrNull() ?: 0.0
            val taxP = paymentChargeTaxPercent ?: 0.0
            chargeTotalInCurrency(req, taxP, paymentChargeTierDiscountPercent, tip)
        } else {
            totalAmount.toDoubleOrNull() ?: 0.0
        }
    } else {
        totalAmount.toDoubleOrNull() ?: 0.0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7))
    ) {
        // 280.dp area: centered by full screen height (independent of top/bottom controls)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val bcMaxWidth = maxWidth
            val bcMaxHeight = maxHeight
            val scanSize = minOf(280.dp, bcMaxHeight - 64.dp).coerceIn(160.dp, 280.dp)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // 扫描区始终渲染，由内部状态分支决定显示内容
                if (true) {
                    // Charge 支付结果（NFC 贴卡 或 QR 扫码）：中间窗口显示
                    if (showPaymentSuccess) {
                    // Approved：PaymentSuccessContent 内顶栏固定、底栏固定；中间 weight(1f)+verticalScroll，勿在外层再包 verticalScroll
                    PaymentSuccessContent(
                        amount = paymentAmount,
                        payee = paymentPayee,
                        txHash = paymentTxHash,
                        subtotal = paymentSubtotal,
                        tip = paymentTip,
                        postBalance = paymentPostBalance,
                        cardCurrency = paymentCardCurrency,
                        memberNo = paymentMemberNo,
                        cardBackground = paymentCardBackground,
                        cardImage = paymentCardImage,
                        cardName = paymentCardName,
                        tierName = paymentTierName,
                        cardType = paymentCardType,
                        settlementViaQr = scanMethod == "qr",
                        extraTopInset = BACK_BUTTON_TOP_BAR_HEIGHT,
                        chargeTaxPercent = paymentChargeTaxPercent,
                        chargeTierDiscountPercent = paymentChargeTierDiscountPercent,
                        tableNumber = paymentTableNumber.trim().takeIf { it.isNotEmpty() },
                        onDone = onPaymentDone,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showPaymentError) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        PaymentRoutingMonitorDisplayCard(
                            steps = paymentRoutingSteps,
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center),
                            errorLine = paymentError,
                            footerLine = paymentNfcUid.takeIf { it.isNotBlank() }?.let { "UID: $it" },
                            retryHintEnglish = "Tap center to retry",
                            onRetryClick = onRetryPaymentAfterError
                        )
                    }
                } else if (pendingAction == "payment" && paymentQrParseError.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable { onRetryPaymentQrScan() }
                    ) {
                        Card(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    paymentQrParseError,
                                    fontSize = 14.sp,
                                    color = Color(0xFFef4444),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Text(
                                    "Tap the center area to scan again",
                                    fontSize = 15.sp,
                                    color = Color(0xFF86868b),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                } else if (pendingAction == "payment" && paymentQrInterpreting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .align(Alignment.Center),
                                        color = Color(0xFF1562f0),
                                        strokeWidth = 2.dp
                                    )
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomCenter),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Processing…",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            "Reading payment code.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF86868b),
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (pendingAction == "topup" && topupQrExecuteError.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable { onRetryTopupQrExecute() }
                    ) {
                        Card(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    topupQrExecuteError,
                                    fontSize = 14.sp,
                                    color = Color(0xFFef4444),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Text(
                                    "Tap the center area to retry",
                                    fontSize = 15.sp,
                                    color = Color(0xFF86868b),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                } else if (pendingAction == "linkApp" && linkAppDeepLinkUrl.isNotEmpty()) {
                    var linkUrlCopied by remember(linkAppDeepLinkUrl) { mutableStateOf(false) }
                    LaunchedEffect(linkUrlCopied) {
                        if (!linkUrlCopied) return@LaunchedEffect
                        delay(2000)
                        linkUrlCopied = false
                    }
                    val linkPillShape = RoundedCornerShape(999.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier.size(scanSize),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "Link ready",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Text(
                                        "Customer opens Beamio app with this link",
                                        fontSize = 11.sp,
                                        color = Color(0xFF86868b),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                                    )
                                    linkAppQrBitmap?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Link app QR",
                                            modifier = Modifier
                                                .size(198.dp)
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .width(scanSize)
                                    .clip(linkPillShape)
                                    .background(Color.White, linkPillShape)
                                    .border(1.dp, Color.Black.copy(alpha = 0.12f), linkPillShape)
                                    .clickable {
                                        onCopyLinkAppUrl()
                                        linkUrlCopied = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Link URL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF86868b)
                                    )
                                    Text(
                                        text = linkAppDeepLinkUrl,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1562f0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Crossfade(
                                    targetState = linkUrlCopied,
                                    label = "linkUrlCopyIcon"
                                ) { copied ->
                                    if (copied) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Copied",
                                            tint = Color(0xFF22c55e),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(22.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy link",
                                            tint = Color(0xFF1562f0),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (pendingAction == "topup" && (topupQrSigningInProgress || topupNfcExecuteInProgress)) {
                    // QR / NFC Top-up：留在选方式页；中央白卡显示 Sign & execute + loading
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .padding(bottom = 20.dp)
                                                .size(96.dp),
                                            color = Color(0xFF1562f0),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Sign & execute",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        if (topupExecuteUidDisplay.isNotBlank()) {
                                            Text(
                                                topupExecuteUidDisplay,
                                                fontSize = 11.sp,
                                                color = Color(0xFF86868b),
                                                modifier = Modifier.padding(top = 4.dp),
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )
                                        }
                                        Text(
                                            "Completing top-up…",
                                            fontSize = 12.sp,
                                            color = Color(0xFF86868b),
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (showPaymentRouting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        PaymentRoutingMonitorDisplayCard(
                            steps = paymentRoutingSteps,
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center)
                        )
                    }
                } else if (scanWaitingForNfc) {
                    // 等待 UID：固定显示等待态（仅图标 + 扫描线），不显示 tap/type 文案
                    val scanLineY by rememberInfiniteTransition(label = "nfcScanLine").animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "nfcScanLineY"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Nfc,
                                        null,
                                        Modifier
                                            .size(96.dp)
                                            .align(Alignment.Center),
                                        tint = Color(0xFF86868b).copy(alpha = 0.1f)
                                    )
                                    Text(
                                        "Hold the customer's NTAG 424 DNA card near the NFC sensor.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF86868b),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                            ) {
                                val y = scanLineY * (size.height - 2f)
                                drawLine(
                                    color = Color(0xFF1562F0),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 0.2.dp.toPx()
                                )
                            }
                        }
                    }
                } else if (nfcFetchingInfo) {
                    // 已获 UID，拉取信息中：无蓝线，仅 loading（与 scanWaitingForNfc 同结构保持中央区域原位）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(scanSize)
                                .align(Alignment.Center)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .align(Alignment.Center),
                                        color = Color(0xFF1562f0),
                                        strokeWidth = 2.dp
                                    )
                                    Column(
                                        modifier = Modifier.align(Alignment.BottomCenter),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Loading...",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        Text(
                                            "Fetching card info.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF86868b),
                                            modifier = Modifier.padding(top = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (nfcFetchError.isNotEmpty()) {
                    // 拉取失败：在 scanSize 框内显示错误；Link App 已锁定时提供 Cancel link lock
                    if (linkAppLockedShowCancel) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(scanSize)
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        nfcFetchError,
                                        fontSize = 14.sp,
                                        color = Color(0xFFef4444),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (linkAppCancelInProgress) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = Color(0xFF1562f0)
                                        )
                                    } else {
                                        Button(
                                            onClick = onLinkAppCancelLock,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cancel link lock")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tap outside this card to scan again",
                                        fontSize = 12.sp,
                                        color = Color(0xFF86868b),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .clickable { (onRetryAfterCentralFetchError ?: onProceedNfcStay)?.invoke() }
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clickable { (onRetryAfterCentralFetchError ?: onProceedNfcStay)?.invoke() }
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(scanSize)
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        nfcFetchError,
                                        fontSize = 14.sp,
                                        color = Color(0xFFef4444),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                    Text(
                                        "Tap to retry",
                                        fontSize = 15.sp,
                                        color = Color(0xFF86868b),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (scanMethod == "qr") {
                        // Same structure as Tap Card: Box with Card only, centered independently
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(scanSize)
                                    .align(Alignment.Center)
                                    .then(
                                        if (qrScanningActive) Modifier
                                        else Modifier.clickable {
                                            onProceedWithQr?.invoke()
                                        }
                                    ),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (qrScanningActive) {
                                        key(embeddedQrResetKey) {
                                            EmbeddedQrScanner(
                                                onResult = onQrScanResult,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.QrCode2,
                                                null,
                                                Modifier.size(96.dp),
                                                tint = Color.Black.copy(alpha = 0.1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // NFC 已改为进入即 arm；此分支仅作极端回退（非 waiting 且非上述状态）
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Card(
                                modifier = Modifier
                                    .size(scanSize)
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(2.dp, Color.Black.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Nfc,
                                        null,
                                        Modifier.size(96.dp),
                                        tint = Color(0xFF86868b).copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        // Top bar overlay: Back button (left) + NFC/QR toggle (center, when visible)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    if (effectiveShowMethodToggle) 120.dp else BACK_BUTTON_TOP_BAR_HEIGHT
                )
                .background(Color(0xFFf5f5f7))
                .align(Alignment.TopCenter)
        ) {
            BackButtonIcon(
                onClick = {
                    if (showPaymentSuccess) onPaymentDone()
                    else onCancel()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = BACK_BUTTON_TOP_PADDING, start = BACK_BUTTON_START_PADDING)
            )
            if (effectiveShowMethodToggle) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TOP_BAR_CAPSULE_TITLE_PADDING, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { onScanMethodChange("nfc") },
                                modifier = Modifier.height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scanMethod == "nfc") Color(0xFF1562f0) else Color.Transparent,
                                    contentColor = if (scanMethod == "nfc") Color.White else Color.Black.copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.Nfc, null, Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tap Card", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { onScanMethodChange("qr") },
                                modifier = Modifier.height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (scanMethod == "qr") Color(0xFF1562f0) else Color.Transparent,
                                    contentColor = if (scanMethod == "qr") Color.White else Color.Black.copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.QrCode2, null, Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan QR", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Bottom overlay: Total amount（仅在有内容时绘制；Approved 时不可留空 Column+background，否则会挡住 PaymentSuccessContent 底栏）
        val showBottomTotalOverlay = pendingAction != "read" && pendingAction != "linkApp" && !showPaymentSuccess &&
            (pendingAction != "payment" && totalAmount.isNotBlank() ||
                pendingAction == "payment" && (bottomTotalDisplay > 0.0 || totalAmount.isNotBlank()))
        if (showBottomTotalOverlay) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFf5f5f7))
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (pendingAction == "payment") "Total Amount" else "Top-Up Amount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (pendingAction == "topup") Color(0xFF1562f0) else Color(0xFF86868b)
                    )
                    Text(
                        "$${"%.2f".format(if (pendingAction == "payment" && bottomTotalDisplay > 0.0) bottomTotalDisplay else (totalAmount.toDoubleOrNull() ?: 0.0))}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (pendingAction == "topup") Color(0xFF1562f0) else Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (pendingAction == "payment") {
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TipSelectionScreen(
    subtotal: String,
    selectedTipRate: Double,
    tableNumber: String,
    onTableNumberChange: (String) -> Unit,
    onTipRateSelect: (Double) -> Unit,
    onBack: () -> Unit,
    onConfirmPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numAmount = subtotal.toDoubleOrNull() ?: 0.0
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7)),
        verticalArrangement = Arrangement.Top
    ) {
        // Header: Back (aligned with scan page) | Add Tip (aligned with Charge Amount title)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            BackButtonIcon(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = BACK_BUTTON_TOP_PADDING, start = BACK_BUTTON_START_PADDING)
            )
            Text(
                "Add Tip",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = TOP_BAR_CAPSULE_TITLE_PADDING)
            )
        }

        // Scrollable middle: Subtotal + Tip options (height-adaptive)
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
                    .padding(top = 8.dp, bottom = 24.dp),
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

            OutlinedTextField(
                value = tableNumber,
                onValueChange = { s -> onTableNumberChange(s.filter { it.isDigit() }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                label = { Text("Table number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Tip options: 2x2 grid - 15%, 18%, 20%, No Tip (compact padding for small screens)
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
                                        .padding(vertical = 24.dp),
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
                                        if (rate == 0.0) "+$0.00" else "+$${"%.2f".format(chargeTipFromRequestAndRate(numAmount, rate))}",
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
        }

        Button(
            onClick = onConfirmPay,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp)
                .heightIn(min = 56.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Confirm & Pay", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Filled.ChevronRight, null, Modifier.size(16.dp), tint = Color.White)
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
        // Header: Back (aligned with scan page) | title (aligned with scan page capsule)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            BackButtonIcon(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = BACK_BUTTON_TOP_PADDING, start = BACK_BUTTON_START_PADDING)
            )
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = TOP_BAR_CAPSULE_TITLE_PADDING)
            )
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
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(24.dp), tint = Color.Black)
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canContinue) continueBtnColor else Color(0xFFe5e5ea),
                    disabledContainerColor = Color(0xFFe5e5ea),
                    contentColor = if (canContinue) Color.White else Color(0xFF86868b),
                    disabledContentColor = Color(0xFF86868b)
                )
            ) {
                Text("Continue", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
        Text(text = "Init mode")
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
                    Button(onClick = onContinue, modifier = Modifier.height(48.dp)) {
                        Text("Continue", fontSize = 14.sp)
                    }
                }
                Button(onClick = onCancel, modifier = Modifier.height(48.dp)) {
                    Text("cancel", fontSize = 14.sp)
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
                Button(onClick = onCancel, modifier = Modifier.height(24.dp)) {
                    Text("cancel", fontSize = 14.sp)
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
            onLinkAppClick = {},
            onReadClick = {},
            onTopupClick = {},
            onPaymentClick = {},
            onCopyUrlClick = {}
        )
    }
}