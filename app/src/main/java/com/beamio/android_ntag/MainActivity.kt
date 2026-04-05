package com.beamio.android_ntag

import android.Manifest
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import java.util.Locale
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Contactless
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
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
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import java.util.concurrent.CountDownLatch
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val primaryMemberTokenId: String? = null,
    /** From card `metadata.tiers[].discountPercent` (after [enrichCardTierFromMetadata]); 0–100，两位小数。 */
    val tierDiscountPercent: Double? = null
)
internal data class UIDAssets(
    val ok: Boolean,
    val address: String? = null,
    val aaAddress: String? = null,
    /** 根级 `primaryMemberTokenId`（无 `cards[]`、客户端合成单卡时用，与 iOS `readBalanceCardList` 对齐） */
    val primaryMemberTokenId: String? = null,
    /** 客户 Beamio 用户名（无 @）；来自 API `beamioTag` / `accountName` */
    val beamioTag: String? = null,
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
    val error: String? = null,
    /** Cluster getUIDAssets/getWalletAssets：商户基础设施卡上该会员 DB 最近 top-up（与 insertMemberTopupEvent 同源） */
    val posLastTopupAt: String? = null,
    val posLastTopupUsdcE6: String? = null,
    val posLastTopupPointsE6: String? = null
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
    val backgroundColor: String?,
    /** Card contract `tiers(i)` index or JSON `metadata.tiers[].index` — aligns with `_findBestValidMembership` / `primaryMemberTokenId`. */
    val chainTierIndex: Int? = null,
    /** From card JSON `metadata.tiers[].discountPercent` (biz Sign & Deploy); 0–100，两位小数；优先于解析 [description]。 */
    val discountPercent: Double? = null
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
    tierDiscountPercent: Double?,
    tipAmount: Double
): Double {
    val tax = requestAmount * (taxPercent / 100.0)
    val disc = requestAmount * ((tierDiscountPercent ?: 0.0) / 100.0)
    return requestAmount + tax - disc + tipAmount
}

/** Tip on full request (before tier discount): tip = requestAmount × tipRate (0..1). */
private fun chargeTipFromRequestAndRate(requestAmount: Double, tipRateFraction: Double): Double =
    requestAmount * tipRateFraction.coerceIn(0.0, 1.0)

/** Tip on full request: tip = requestAmount × (tipRateBps / 10000). */
private fun chargeTipFromRequestAndBps(requestAmount: Double, tipRateBps: Int): Double =
    requestAmount * (tipRateBps.coerceIn(0, 10000) / 10000.0)

/** Insufficient-balance full screen (see verra-home home1.html). */
private data class InsufficientBalanceUiModel(
    val totalChargeFormatted: String,
    val currentBalanceFormatted: String,
    val shortfallFormatted: String,
    val voucherDeductionFormatted: String,
    val tierDiscountFormatted: String,
    val tipFormatted: String,
    val taxFormatted: String,
    /** `"nfc"` | `"qr"` — restore scan method on Continue Charge */
    val returnScanMethod: String,
    /** 客户可扣 USDC6 上限；>0 时展示「Charge Available Balance」 */
    val availableBalanceUsdc6: Long = 0L,
)

/** PARTIAL APPROVAL：客户余额不足但已扣完全部可用资产后，展示剩余未付差额（verra-home home1.html）。 */
internal data class PartialApprovalUiModel(
    val payCurrency: String,
    val chargedPayCur: Double,
    val shortfallPayCur: Double,
    val orderTotalPayCur: Double,
    val subtotal: Double,
    val tierDiscountAmount: Double,
    val tierDiscountLabel: String?,
    val taxPercent: Double,
    val taxAmount: Double,
    val tipAmount: Double,
    val memberDisplayName: String,
    val memberNoDisplay: String,
    val txHash: String,
    val settlementViaQr: Boolean,
    val settlementDetail: String,
    val originalSubtotal: Double,
    val originalTipBps: Int,
    val tierDiscPct: Double,
    val returnScanMethod: String,
    /** 与 Balance Details / getUIDAssets `cardBackground` 一致，供标准 Pass Hero 渐变。 */
    val tierCardBackgroundHex: String? = null,
    val cardMetadataImageUrl: String? = null,
    /** Hero 底栏 program 行；缺省则回退 [memberDisplayName]。 */
    val programCardDisplayName: String? = null,
)

class MainActivity : ComponentActivity() {
    private companion object {
        const val HARD_CODED_KEY0 = "894FE8DAD6E206F142D107D11805579A"
        const val HARD_CODED_MASTER_KEY = "3DA90A7D29A5797A16ED53D91A49B803"
        const val SUN_BASE_URL = "https://api.beamio.app/api/sun"
        /** 与 SilentPassUI utils/constants.ts beamioApi 一致 */
        const val BEAMIO_API = "https://beamio.app"
        /** USDC on Base */
        const val USDC_BASE = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"
        /**
         * 默认基础设施卡地址（`/api/myPosAddress` 无登记或请求失败时回退）。
         * 运行时以终端钱包拉取 `/api/myPosAddress` 得到的登记卡为准。
         */
        const val DEFAULT_BEAMIO_USER_CARD_ASSET_ADDRESS = "0xA756F2E27a332d6Be2d399dA543E3Ce4C8455F14"
        /** Base RPC，遵循 beamio-base-rpc */
        private const val BASE_RPC_URL = "https://base-rpc.conet.network"
        /** `BeamioUserCard` / `BeamioUserCardBase`：`Tier[] public tiers` → `tiers(uint256)`（与 BeamioUserCardArtifact.json 一致） */
        private const val SEL_TIERS_UINT256 = "0x039af9eb"
        /** 链上 tiers 无 length getter，依次探测直至 revert */
        private const val CHAIN_TIERS_MAX_PROBE = 48
        private const val PREFS_PROFILE_CACHE = "beamio_profile_cache"
        /** POS 为链上 owner 直属 admin 时，QR/NFC Charge 需附 chargeOwnerChildBurn；与链上 adminParent(eoa)==owner(card) 一致 */
        private const val PREF_KEY_CHARGE_OWNER_CHILD_ELIGIBLE = "charge_owner_child_burn_eligible"
        private const val PREF_KEY_CHARGE_OWNER_CHILD_CARD = "charge_owner_child_burn_card"
        private const val PREF_KEY_CHARGE_OWNER_CHILD_WALLET = "charge_owner_child_burn_wallet"
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
    /** getUIDAssets/getWalletAssets `beamioTag`，与 Balance Hero 主名一致 */
    private var topupScreenBeamioTag by mutableStateOf<String?>(null)
    private var topupScreenCardBackground by mutableStateOf<String?>(null)
    private var topupScreenCardImage by mutableStateOf<String?>(null)
    private var topupScreenTierName by mutableStateOf<String?>(null)
    private var topupScreenTierDescription by mutableStateOf<String?>(null)
    private var topupScreenMemberNo by mutableStateOf<String?>(null)
    /** Top-up 成功页 Pass 与 Balance Loaded 对齐：保留整卡快照（含 nfts / primaryMemberTokenId） */
    private var topupScreenPassCardSnapshot by mutableStateOf<CardItem?>(null)
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
    private var paymentChargeBreakdownTierDiscountPercent by mutableStateOf<Double?>(null)
    private var paymentScreenPreBalance by mutableStateOf<String?>(null)
    private var paymentScreenPostBalance by mutableStateOf<String?>(null)
    private var paymentScreenCardCurrency by mutableStateOf<String?>(null)
    private var paymentScreenCardBackground by mutableStateOf<String?>(null)
    private var paymentScreenCardImage by mutableStateOf<String?>(null)
    private var paymentScreenCardName by mutableStateOf<String?>(null)
    private var paymentScreenTierName by mutableStateOf<String?>(null)
    private var paymentScreenCardType by mutableStateOf<String?>(null)
    private var paymentScreenMemberNo by mutableStateOf<String?>(null)
    /** Charge 成功页 Pass 与 Balance Loaded 对齐 */
    private var paymentScreenPassCardSnapshot by mutableStateOf<CardItem?>(null)
    /** 客户 beamioTag / EOA，供收据 Hero 主名与 Balance 一致 */
    private var paymentScreenPayerBeamioTag by mutableStateOf<String?>(null)
    private var paymentScreenPayerAddress by mutableStateOf<String?>(null)
    /** Charge 选小费页填写的桌号，随成功页 / 打印收据展示 */
    private var paymentScreenTableNumber by mutableStateOf("")
    /** 余额不足专用页（home1.html 风格）；非 null 时优先全屏展示 */
    private var insufficientBalanceUi by mutableStateOf<InsufficientBalanceUiModel?>(null)
    /**
     * 与 iOS `chargeInsufficientRetry*` 一致：不足额页点「Charge available balance」时直执行 partial max，不再次贴卡/扫码。
     * 在 [openInsufficientBalanceScreen] 前写入；取消/继续收款/Top-up/[resetPaymentScreenState] 时清空。
     */
    private var insufficientPartialRetryContext: InsufficientPartialRetryContext? = null
    /** 扣完全部余额后仍有短款的 PARTIAL APPROVAL 页；与 [PaymentStatus.Success] 同时有效 */
    private var paymentPartialApprovalUi by mutableStateOf<PartialApprovalUiModel?>(null)
    /**
     * 用户在「余额不足」页选择按当前可用余额扣款时置 true；下一次 NFC/QR execute 在链上按 min(应收, 客户总资产) 扣款，随后 PARTIAL APPROVAL。
     * 一次性消费，成功进入 partial 分支或重新打开 insufficient 时清除。
     */
    private var chargeCapToCustomerAvailableBalance by mutableStateOf(false)
    /** 当前账单下最近一次 Charge 动态 QR 解析得到的 relay JSON（含离线 signature）。 */
    private var lastQrPaymentOpenRelayPayload: org.json.JSONObject? = null
    private var paymentArmed by mutableStateOf(false)
    private var paymentViaQr by mutableStateOf(false)
    private val cardMetadataTierCache = mutableMapOf<String, List<MetadataTier>>()
    /** True when [fetchCardMetadataTiers] filled tiers from GET `/api/cardMetadata` `metadata.tiers` (not chain probe fallback). */
    private val cardMetadataTierFromApiCache = mutableMapOf<String, Boolean>()

    /**
     * 基础设施 BeamioUserCard 合约地址：首启后 GET `/api/myPosAddress?wallet=<终端EOA>` 解析；
     * 与 DB 登记一致；失败或未登记时用 [DEFAULT_BEAMIO_USER_CARD_ASSET_ADDRESS]。
     */
    @Volatile
    private var beamioUserCardAssetAddressRuntime: String = DEFAULT_BEAMIO_USER_CARD_ASSET_ADDRESS

    @Volatile
    private var myPosInfraCardFetchInFlight: Boolean = false

    private fun infraCardAddress(): String = beamioUserCardAssetAddressRuntime

    /**
     * POS 与 Cluster：请求体带 `merchantInfraCard`（终端登记基础设施卡地址）。
     * `merchantInfraOnly=true` 时服务端**只返回**该行 CashTrees/infrastructure（用于特殊场景）；客户「Check Balance / Balance Loaded」须为 **false**，
     * 否则界面只剩基础设施模板一行、无 CCSA/会员 Pass（见 x402sdk `cardsScope: merchantInfraOnly`）。
     */
    private fun org.json.JSONObject.putMerchantInfraParams(merchantInfraOnly: Boolean) {
        put("merchantInfraCard", infraCardAddress())
        if (merchantInfraOnly) put("merchantInfraOnly", true)
    }

    /** Cluster DB：已登记终端返回 `cardAddress` / `myPosAddress`。 */
    private fun fetchMyPosInfraCardAddressSync(wallet: String): String? {
        val trimmed = wallet.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("0x", ignoreCase = true)) return null
        return try {
            val enc = java.net.URLEncoder.encode(trimmed, "UTF-8")
            val url = java.net.URL("$BEAMIO_API/api/myPosAddress?wallet=$enc")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() }
                .orEmpty()
            conn.disconnect()
            if (code !in 200..299) {
                Log.d("MyPosAddress", "HTTP $code body=${body.take(200)}")
                return null
            }
            val root = parseBeamioApiJsonObject(body, code) ?: return null
            if (!root.optBoolean("ok", false)) return null
            val addr = root.optString("cardAddress").takeIf { it.isNotEmpty() }
                ?: root.optString("myPosAddress").takeIf { it.isNotEmpty() }
            addr
        } catch (e: Exception) {
            Log.w("MyPosAddress", "fetch failed", e)
            null
        }
    }

    /**
     * 在 topup / Charge / getCardAdminInfo / getUIDAssets 等依赖「终端登记基础设施卡」的请求前调用。
     * [ensureMyPosInfraCardAddressLoaded] 为异步，若用户在首屏立即操作，runtime 可能仍为 [DEFAULT_BEAMIO_USER_CARD_ASSET_ADDRESS]，
     * 导致 merchantInfraCard、cardAddress 与 DB 不一致，查错合约 → cards 为空或 admin 元数据不匹配。
     */
    private fun refreshMerchantInfraCardFromDbSync(): String {
        if (!BeamioWeb3Wallet.isInitialized()) return infraCardAddress()
        val w = BeamioWeb3Wallet.getAddress()?.trim().orEmpty()
        if (w.isEmpty()) return infraCardAddress()
        val resolved = fetchMyPosInfraCardAddressSync(w)
        if (!resolved.isNullOrBlank() && looksLikeEthereumAddress(resolved)) {
            beamioUserCardAssetAddressRuntime = resolved
            Log.d("MyPosAddress", "[infra] sync before API use: $resolved")
        }
        return infraCardAddress()
    }

    private fun ensureMyPosInfraCardAddressLoaded() {
        if (!BeamioWeb3Wallet.isInitialized()) return
        val w = BeamioWeb3Wallet.getAddress()?.trim().orEmpty()
        if (w.isEmpty()) return
        if (myPosInfraCardFetchInFlight) return
        myPosInfraCardFetchInFlight = true
        Thread {
            try {
                val resolved = fetchMyPosInfraCardAddressSync(w)
                runOnUiThread {
                    if (!resolved.isNullOrBlank() && looksLikeEthereumAddress(resolved)) {
                        beamioUserCardAssetAddressRuntime = resolved
                        cardMetadataTierCache.clear()
                        cardMetadataTierFromApiCache.clear()
                        val onHome = !showWelcomePage && !showOnboardingScreen && !showTopupScreen && !showReadScreen &&
                            !showScanMethodScreen && !showAmountInputScreen && !showTipScreen && !showPaymentScreen && !showInitFlowScreen &&
                            insufficientBalanceUi == null
                        if (onHome) {
                            Thread { prefetchInfraCardMetadata() }.start()
                        }
                    }
                }
            } finally {
                myPosInfraCardFetchInFlight = false
            }
        }.start()
    }

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
                            fetchUidAssets(beamioTab, sunParams = null, merchantInfraOnly = false)
                        } else {
                            fetchWalletAssets(wallet!!, merchantInfraOnly = false)
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
                        topupScreenBeamioTag = null
                        topupScreenCardBackground = null
                        topupScreenCardImage = null
                        topupScreenTierName = null
                        topupScreenTierDescription = null
                        topupScreenPassCardSnapshot = null
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
        ensureMyPosInfraCardAddressLoaded()
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
                                val assets = fetchWalletAssetsSync(BeamioWeb3Wallet.getAddress(), merchantInfraOnly = false)
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
                            refreshMerchantInfraCardFromDbSync()
                            val (profile, admin) = fetchTerminalProfileSync(wallet)
                            val (charge, topUp) = fetchCardStatsSync(wallet)
                            runOnUiThread {
                                if (profile != null) terminalProfile = profile
                                terminalAdminProfile = admin
                                saveProfileCache(wallet, profile, admin)
                                if (charge != null) cardChargeAmount = charge
                                if (topUp != null) cardTopUpAmount = topUp
                            }
                        }.start()
                    }
                }
                LaunchedEffect(showWelcomePage, showOnboardingScreen, showTopupScreen, showReadScreen, showScanMethodScreen, showAmountInputScreen, showTipScreen, showPaymentScreen, showInitFlowScreen, insufficientBalanceUi) {
                    val onHome = !showWelcomePage && !showOnboardingScreen && !showTopupScreen && !showReadScreen &&
                        !showScanMethodScreen && !showAmountInputScreen && !showTipScreen && !showPaymentScreen && !showInitFlowScreen &&
                        insufficientBalanceUi == null
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
                    showInitFlowScreen,
                    insufficientBalanceUi
                ) {
                    val onHome = !showWelcomePage && !showOnboardingScreen && !showTopupScreen && !showReadScreen &&
                        !showScanMethodScreen && !showAmountInputScreen && !showTipScreen && !showPaymentScreen && !showInitFlowScreen &&
                        insufficientBalanceUi == null
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
                    insufficientBalanceUi != null -> InsufficientBalanceScreen(
                        model = insufficientBalanceUi!!,
                        onTopUp = { onInsufficientBalanceTopUpFromScreen() },
                        onChargeAvailableBalance = { onInsufficientBalanceChargeAvailableFromScreen() },
                        onContinueCharge = { onInsufficientBalanceContinueChargeFromScreen() },
                        onCancel = { onInsufficientBalanceCancelFromScreen() },
                        modifier = Modifier.fillMaxSize()
                    )
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
                            ensureMyPosInfraCardAddressLoaded()
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
                        customerBeamioTag = topupScreenBeamioTag,
                        memberNo = topupScreenMemberNo,
                        cardBackground = topupScreenCardBackground,
                        cardImage = topupScreenCardImage,
                        tierName = topupScreenTierName,
                        tierDescription = topupScreenTierDescription,
                        passCardSnapshot = topupScreenPassCardSnapshot,
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
                        paymentPassCard = if (pendingScanAction == "payment") paymentScreenPassCardSnapshot else null,
                        paymentCustomerBeamioTag = if (pendingScanAction == "payment") paymentScreenPayerBeamioTag else null,
                        paymentCustomerWalletAddress = if (pendingScanAction == "payment") paymentScreenPayerAddress else null,
                        paymentChargeTaxPercent = if (pendingScanAction == "payment") paymentChargeBreakdownTaxPercent else null,
                        paymentChargeTierDiscountPercent = if (pendingScanAction == "payment") paymentChargeBreakdownTierDiscountPercent else null,
                        paymentChargeTipBps = if (pendingScanAction == "payment") paymentScreenTipRateBps else 0,
                        paymentTableNumber = if (pendingScanAction == "payment") paymentScreenTableNumber else "",
                        paymentPartialApproval = if (pendingScanAction == "payment") paymentPartialApprovalUi else null,
                        onPartialApprovalContinue = { onPartialApprovalContinue() },
                        onPartialApprovalCancel = { onPartialApprovalCancel() },
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
                        posMerchantInfraCard = infraCardAddress(),
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
                        passCardSnapshot = paymentScreenPassCardSnapshot,
                        settlementViaQr = paymentViaQr,
                        customerBeamioTag = paymentScreenPayerBeamioTag,
                        customerWalletAddress = paymentScreenPayerAddress,
                        chargeTaxPercent = paymentChargeBreakdownTaxPercent,
                        chargeTierDiscountPercent = paymentChargeBreakdownTierDiscountPercent,
                        tableNumber = paymentScreenTableNumber.takeIf { it.isNotBlank() },
                        partialApproval = paymentPartialApprovalUi,
                        onPartialApprovalContinue = { onPartialApprovalContinue() },
                        onPartialApprovalCancel = { onPartialApprovalCancel() },
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
        paymentScreenPassCardSnapshot = null
        paymentScreenPayerBeamioTag = null
        paymentScreenPayerAddress = null
        paymentScreenTableNumber = ""
        paymentQrInterpreting = false
        paymentQrParseError = ""
        insufficientBalanceUi = null
        insufficientPartialRetryContext = null
        paymentPartialApprovalUi = null
        chargeCapToCustomerAvailableBalance = false
        lastQrPaymentOpenRelayPayload = null
    }

    /** PARTIAL APPROVAL：按未付比例缩小 subtotal 后继续收款（保持小费 bps 与税率由后续 execute 重算）。 */
    private fun onPartialApprovalContinue() {
        val snap = paymentPartialApprovalUi ?: return
        paymentPartialApprovalUi = null
        val order = snap.orderTotalPayCur.coerceAtLeast(1e-9)
        val ratio = (snap.shortfallPayCur / order).coerceIn(0.0, 1.0)
        val newSub = (snap.originalSubtotal * ratio).coerceAtLeast(0.01)
        paymentScreenSubtotal = "%.2f".format(newSub)
        paymentScreenTipRateBps = snap.originalTipBps
        paymentScreenStatus = PaymentStatus.Waiting
        paymentScreenTxHash = ""
        paymentScreenError = ""
        paymentScreenRoutingSteps = emptyList()
        paymentScreenPostBalance = null
        paymentScreenPreBalance = null
        chargeCapToCustomerAvailableBalance = false
        paymentArmed = false
        nfcFetchingInfo = false
        nfcFetchError = ""
        paymentQrParseError = ""
        paymentQrInterpreting = false
        lastQrPaymentOpenRelayPayload = null
        scanMethodState = snap.returnScanMethod
        showScanMethodScreen = true
        showPaymentScreen = false
        pendingScanAction = "payment"
        syncScanMethodEntryAndTabs()
    }

    private fun onPartialApprovalCancel() {
        paymentPartialApprovalUi = null
        chargeTableNumber = ""
        resetPaymentScreenState()
        showScanMethodScreen = false
        showPaymentScreen = false
        paymentArmed = false
        nfcFetchingInfo = false
        nfcFetchError = ""
    }

    private fun formatFiatDisplayLineForInsufficient(amount: Double, currency: String): String {
        val (prefix, amtStr, suffix) = formatBalanceWithCurrencyProtocol(amount, currency)
        return buildString {
            if (prefix.isNotEmpty()) {
                append(prefix.trimEnd())
                append(" ")
            }
            append(amtStr)
            append(suffix)
        }
    }

    private fun buildInsufficientBalanceUiFromPaymentContext(
        totalCurrency: Double,
        payCurrency: String,
        totalBalance6: Long,
        required6: Long,
        oracle: OracleRates,
        subtotal: Double,
        taxPct: Double,
        tierDiscPct: Double,
        tip: Double,
        returnScanMethod: String
    ): InsufficientBalanceUiModel {
        val rate = getRateForCurrency(payCurrency, oracle)
        val balancePayCur = (totalBalance6 / 1_000_000.0) * rate
        val needPayCur = (required6 / 1_000_000.0) * rate
        val shortfallPayCur = (needPayCur - balancePayCur).coerceAtLeast(0.0)
        val taxAmt = subtotal * taxPct / 100.0
        val discAmt = subtotal * tierDiscPct / 100.0 // tierDiscPct：metadata 两位小数 %
        fun line(amt: Double) = formatFiatDisplayLineForInsufficient(amt, payCurrency)
        return InsufficientBalanceUiModel(
            totalChargeFormatted = line(totalCurrency),
            currentBalanceFormatted = line(balancePayCur),
            shortfallFormatted = "-${line(shortfallPayCur)}",
            voucherDeductionFormatted = line(subtotal),
            tierDiscountFormatted = line(discAmt),
            tipFormatted = line(tip),
            taxFormatted = line(taxAmt),
            returnScanMethod = if (returnScanMethod == "qr") "qr" else "nfc",
            availableBalanceUsdc6 = totalBalance6.coerceAtLeast(0L),
        )
    }

    private fun parseInsufficientBalanceUsdcFromError(message: String): Triple<Double, Double, Double>? {
        val re = Regex(
            """need\s+([\d.]+)\s+USDC,\s+total assets\s+([\d.]+)\s+USDC,\s+shortfall\s+([\d.]+)\s+USDC""",
            RegexOption.IGNORE_CASE
        )
        val m = re.find(message) ?: return null
        val need = m.groupValues[1].toDoubleOrNull() ?: return null
        val total = m.groupValues[2].toDoubleOrNull() ?: return null
        val short = m.groupValues[3].toDoubleOrNull() ?: return null
        return Triple(need, total, short)
    }

    private fun openInsufficientBalanceScreen(model: InsufficientBalanceUiModel) {
        chargeCapToCustomerAvailableBalance = false
        insufficientBalanceUi = model
        showScanMethodScreen = false
        showPaymentScreen = false
        nfcFetchingInfo = false
        nfcFetchError = ""
        paymentQrInterpreting = false
        paymentQrParseError = ""
        paymentScreenStatus = PaymentStatus.Waiting
        paymentScreenError = ""
        paymentScreenRoutingSteps = emptyList()
    }

    private fun onInsufficientBalanceTopUpFromScreen() {
        insufficientBalanceUi = null
        insufficientPartialRetryContext = null
        chargeCapToCustomerAvailableBalance = false
        resetPaymentScreenState()
        amountInput = "0"
        amountInputMode = "topup"
        showAmountInputScreen = true
    }

    private fun onInsufficientBalanceContinueChargeFromScreen() {
        chargeCapToCustomerAvailableBalance = false
        val method = insufficientBalanceUi?.returnScanMethod ?: "nfc"
        insufficientBalanceUi = null
        insufficientPartialRetryContext = null
        paymentScreenStatus = PaymentStatus.Waiting
        paymentScreenError = ""
        paymentScreenRoutingSteps = emptyList()
        paymentQrParseError = ""
        paymentQrInterpreting = false
        nfcFetchError = ""
        scanMethodState = method
        showScanMethodScreen = true
        pendingScanAction = "payment"
        syncScanMethodEntryAndTabs()
    }

    private fun onInsufficientBalanceCancelFromScreen() {
        insufficientBalanceUi = null
        insufficientPartialRetryContext = null
        chargeCapToCustomerAvailableBalance = false
        chargeTableNumber = ""
        resetPaymentScreenState()
    }

    /** 按客户当前可用余额扣款（不足全额应收）；成功后走 PARTIAL APPROVAL。对齐 iOS `chargeAvailableBalanceAfterInsufficientFunds`（无需再次贴卡/扫码）。 */
    private fun onInsufficientBalanceChargeAvailableFromScreen() {
        val m = insufficientBalanceUi ?: return
        if (m.availableBalanceUsdc6 <= 0L) return
        val snap = insufficientPartialRetryContext
        val nfcOk = snap?.nfcUid?.trim()?.isNotEmpty() == true
        val qrOk = snap?.qrAccount?.trim()?.isNotEmpty() == true && snap.qrPayloadJson?.trim()?.isNotEmpty() == true
        if (snap == null || (!nfcOk && !qrOk)) {
            // 与 iOS 一致：无重试上下文时不误关页（理论上每次 openInsufficient 都会写入 context）
            return
        }
        insufficientBalanceUi = null
        insufficientPartialRetryContext = null
        chargeCapToCustomerAvailableBalance = false
        showScanMethodScreen = false
        scanWaitingForNfc = false
        showPaymentScreen = true
        paymentArmed = false
        paymentScreenStatus = PaymentStatus.Routing
        paymentScreenRoutingSteps = createRoutingSteps()
        paymentScreenError = ""
        paymentQrParseError = ""
        paymentQrInterpreting = false
        nfcFetchError = ""
        paymentScreenTxHash = ""
        pendingScanAction = "payment"
        executeImmediatePartialMaxCharge(snap)
    }

    /** iOS `executePartialMaxNfcCharge` / `executePartialMaxQrCharge`：刷新资产、`min(available,freshTotal)`、scaled bill、submit。 */
    private fun executeImmediatePartialMaxCharge(snap: InsufficientPartialRetryContext) {
        when {
            snap.nfcUid?.trim()?.isNotEmpty() == true -> executeImmediatePartialNfcFromInsufficient(snap)
            snap.qrAccount?.trim()?.isNotEmpty() == true && snap.qrPayloadJson?.trim()?.isNotEmpty() == true ->
                executeImmediatePartialQrFromInsufficient(snap)
            else -> runOnUiThread {
                paymentScreenStatus = PaymentStatus.Error
                paymentScreenError = "Cannot retry payment"
            }
        }
    }

    private fun executeImmediatePartialNfcFromInsufficient(snap: InsufficientPartialRetryContext) {
        val uid = snap.nfcUid!!.trim()
        val sunParams = snap.nfcSun
        Thread {
            try {
                refreshMerchantInfraCardFromDbSync()
                var steps = createRoutingSteps()
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Routing
                    paymentScreenRoutingSteps = updateStep(steps, "detectingUser", StepStatus.loading)
                }
                steps = updateStep(steps, "detectingUser", StepStatus.success, "NFC")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "membership", StepStatus.loading) }
                steps = updateStep(steps, "membership", StepStatus.success, "Card")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                val assets = fetchUidAssetsSync(uid, sunParams, merchantInfraOnly = false)
                if (assets == null || !assets.ok) {
                    steps = updateStep(steps, "analyzingAssets", StepStatus.error, assets?.error ?: "Card not registered")
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = assets?.error ?: "Card not registered"
                        paymentScreenRoutingSteps = steps
                    }
                    return@Thread
                }
                steps = updateStep(steps, "analyzingAssets", StepStatus.success, "Card + USDC balance")
                val paymentMemberNo = memberNoPrimaryFromSortedCardsItem(assets)
                val paymentCard = assets.cards?.firstOrNull()
                val payeeWallet = BeamioWeb3Wallet.getAddress()?.trim().orEmpty()
                if (payeeWallet.isEmpty()) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Wallet not initialized"
                    }
                    return@Thread
                }
                val payCurrency = paymentCard?.cardCurrency ?: assets.cardCurrency ?: snap.payCurrency
                val oracle = fetchOracle()
                val subtotalPay = snap.subtotal
                val taxPctResolved = snap.taxPct
                val tierDiscPct = snap.tierDiscPct
                val tipPay = snap.tip
                val taxAmtCad = subtotalPay * taxPctResolved / 100.0
                val discAmtCad = subtotalPay * tierDiscPct / 100.0
                val totalBalance6 = chargePreflightTotalBalanceUsdc6(assets, oracle)
                val amountPartial = minOf(snap.availableUsdc6.coerceAtLeast(0L), totalBalance6)
                if (amountPartial <= 0L) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "No available balance"
                        paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.error, "No balance")
                    }
                    return@Thread
                }
                val chargedFiat = usdc6ToCurrencyAmount(amountPartial, payCurrency, oracle)
                if (chargedFiat <= 0) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Amount conversion failed"
                    }
                    return@Thread
                }
                val scaled = snap.scaledBillForChargedPayCur(chargedFiat)
                val totalCurrency = snap.totalCurrency
                val totalBalanceCad = (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
                val taxBpsResolved = scaled.taxBps
                val discBpsResolved = scaled.discBps
                runOnUiThread {
                    paymentChargeBreakdownTaxPercent = taxPctResolved
                    paymentChargeBreakdownTierDiscountPercent = tierDiscPct
                    paymentScreenTip = scaled.tipStr ?: "0.00"
                    paymentScreenAmount = "%.2f".format(chargedFiat)
                    paymentScreenCardCurrency = payCurrency
                    paymentScreenMemberNo = paymentMemberNo.ifEmpty { null }
                    paymentScreenCardBackground = paymentCard?.cardBackground
                    paymentScreenCardImage = paymentCard?.cardImage
                    paymentScreenCardName = paymentCard?.cardName
                    paymentScreenTierName = paymentCard?.tierName
                    paymentScreenCardType = paymentCard?.cardType
                    paymentScreenPassCardSnapshot = paymentCard
                    paymentScreenPayerBeamioTag = assets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    paymentScreenPayerAddress = assets.address?.trim()
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                    paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading)
                }
                steps = updateStep(steps, "optimizingRoute", StepStatus.success, "Direct: NFC → Merchant")
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val result = payByNfcUidWithContainer(
                    uid,
                    amountPartial.toString(),
                    payeeWallet,
                    assets,
                    oracle,
                    sunParams,
                    chargeTotalInPayCurrency = chargedFiat,
                    nfcSubtotalCurrencyAmount = scaled.subtotalStr,
                    nfcTipCurrencyAmount = scaled.tipStr,
                    nfcTipRateBps = paymentScreenTipRateBps,
                    nfcRequestCurrency = payCurrency,
                    nfcTaxAmountFiat6 = scaled.taxFiat6,
                    nfcTaxRateBps = taxBpsResolved,
                    nfcDiscountAmountFiat6 = scaled.discFiat6,
                    nfcDiscountRateBps = discBpsResolved,
                )
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "Payment failed"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                if (!result.success) {
                    val err = result.error ?: "Payment failed"
                    runOnUiThread {
                        paymentScreenRoutingSteps = steps
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = err
                    }
                    return@Thread
                }
                val payPassSnapshotAddr = paymentCard?.cardAddress
                steps = updateStep(steps, "refreshBalance", StepStatus.loading, "Fetching latest balance")
                runOnUiThread {
                    paymentScreenRoutingSteps = steps
                    paymentScreenPostBalance = null
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenTxHash = result.txHash ?: ""
                    paymentScreenError = ""
                    paymentViaQr = false
                }
                Thread {
                    Thread.sleep(3000)
                    val postAssets = fetchUidAssetsSync(uid, sunParams, merchantInfraOnly = false)
                    val stepsAfter = updateStep(
                        steps,
                        "refreshBalance",
                        StepStatus.success,
                        if (postAssets != null && postAssets.ok) "Updated" else "Unavailable",
                    )
                    runOnUiThread {
                        paymentScreenRoutingSteps = stepsAfter
                        var refreshedPass: CardItem? = null
                        if (postAssets != null && postAssets.ok) {
                            paymentScreenPostBalance = "%.2f".format(totalBalanceCadFromAssets(postAssets, oracle))
                            refreshedPass = postAssets.cards?.firstOrNull { payPassSnapshotAddr != null && it.cardAddress.equals(payPassSnapshotAddr, ignoreCase = true) }
                                ?: postAssets.cards?.firstOrNull()
                            if (refreshedPass != null) {
                                paymentScreenPassCardSnapshot = refreshedPass
                                paymentScreenCardBackground = refreshedPass.cardBackground
                                paymentScreenCardImage = refreshedPass.cardImage
                                paymentScreenTierName = refreshedPass.tierName
                                paymentScreenCardName = refreshedPass.cardName
                            }
                        } else {
                            paymentScreenPostBalance = "—"
                        }
                        val hero = refreshedPass ?: paymentCard
                        val shortfallNfc = (totalCurrency - chargedFiat).coerceAtLeast(0.0)
                        val memberTitleNfc = passHeroMemberDisplayNameLine(
                            assets.beamioTag,
                            assets.address,
                            hero,
                            hero?.cardName,
                        )
                        val mn = (paymentMemberNo.ifEmpty { memberNoPrimaryFromSortedCardsItem(assets) }).ifBlank { "—" }
                        val term = BeamioWeb3Wallet.getAddress()?.trim()?.takeIf { it.length >= 6 }?.let { "POS-${it.takeLast(4).uppercase(Locale.US)}" } ?: "—"
                        paymentPartialApprovalUi = PartialApprovalUiModel(
                            payCurrency = payCurrency,
                            chargedPayCur = chargedFiat,
                            shortfallPayCur = shortfallNfc,
                            orderTotalPayCur = totalCurrency,
                            subtotal = subtotalPay,
                            tierDiscountAmount = discAmtCad,
                            tierDiscountLabel = hero?.tierName?.takeIf { it.isNotBlank() },
                            taxPercent = taxPctResolved,
                            taxAmount = taxAmtCad,
                            tipAmount = tipPay,
                            memberDisplayName = memberTitleNfc,
                            memberNoDisplay = mn,
                            txHash = result.txHash ?: "",
                            settlementViaQr = false,
                            settlementDetail = term,
                            originalSubtotal = subtotalPay,
                            originalTipBps = paymentScreenTipRateBps,
                            tierDiscPct = tierDiscPct,
                            returnScanMethod = if (snap.returnScanMethod == "qr") "qr" else "nfc",
                            tierCardBackgroundHex = hero?.cardBackground,
                            cardMetadataImageUrl = hero?.cardImage,
                            programCardDisplayName = hero?.let { balanceDetailsCardNameLine(it) }?.takeIf { it != "—" },
                        )
                        paymentScreenStatus = PaymentStatus.Success
                        scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                        scheduleElasticHomeDataPullAfterTxSuccess()
                    }
                }.start()
            } catch (e: Exception) {
                Log.e("MainActivity", "executeImmediatePartialNfcFromInsufficient", e)
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Error
                    paymentScreenError = e.message ?: "Execution failed"
                }
            }
        }.start()
    }

    private fun executeImmediatePartialQrFromInsufficient(snap: InsufficientPartialRetryContext) {
        Thread {
            try {
                refreshMerchantInfraCardFromDbSync()
                val payload = org.json.JSONObject(snap.qrPayloadJson!!)
                val account = snap.qrAccount!!.trim()
                var steps = createRoutingSteps()
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Routing
                    paymentScreenRoutingSteps = updateStep(steps, "detectingUser", StepStatus.loading)
                }
                steps = updateStep(steps, "detectingUser", StepStatus.success, "Dynamic QR")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "membership", StepStatus.loading) }
                val assets = fetchWalletAssetsSync(account, merchantInfraOnly = false)
                if (assets == null || !assets.ok) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = assets?.error ?: "Unable to fetch customer assets"
                    }
                    return@Thread
                }
                val oracle = fetchOracle()
                val taxPctQr = snap.taxPct
                val tierDiscQr = snap.tierDiscPct
                val requestQr = snap.subtotal
                val tipQr = snap.tip
                val taxAmtCadQr = requestQr * taxPctQr / 100.0
                val discAmtCadQr = requestQr * tierDiscQr / 100.0
                val paymentCardForQrCharge = assets.cards?.firstOrNull()
                val payCurrencyQr = paymentCardForQrCharge?.cardCurrency ?: assets.cardCurrency ?: snap.payCurrency
                val totalCurrencyQr = snap.totalCurrency
                steps = updateStep(steps, "membership", StepStatus.success, "Cardholder")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                val unitPriceStr = assets.unitPriceUSDC6
                val unitPriceUSDC6 = unitPriceStr?.toLongOrNull() ?: 0L
                val cards = chargeableCards(assets)
                val ccsaCards = cards.filter { it.cardType == "ccsa" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
                val infraCards = cards.filter { it.cardType == "infrastructure" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
                val ccsaPoints6 = ccsaCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val infraPoints6 = infraCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
                val totalBalance6 = chargePreflightTotalBalanceUsdc6(assets, oracle)
                val rateQrPartial = getRateForCurrency(payCurrencyQr, oracle)
                val balancePayCurBeforeQr = (totalBalance6 / 1_000_000.0) * rateQrPartial
                val amountPartial = minOf(snap.availableUsdc6.coerceAtLeast(0L), totalBalance6)
                if (amountPartial <= 0L) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "No available balance"
                    }
                    return@Thread
                }
                val chargedFiat = usdc6ToCurrencyAmount(amountPartial, payCurrencyQr, oracle)
                if (chargedFiat <= 0) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Amount conversion failed"
                    }
                    return@Thread
                }
                val scaled = snap.scaledBillForChargedPayCur(chargedFiat)
                val taxFiat6Bill = scaled.taxFiat6
                val discFiat6Bill = scaled.discFiat6
                val taxBpsQr = scaled.taxBps
                val discBpsQr = scaled.discBps
                steps = updateStep(steps, "analyzingAssets", StepStatus.success, "USDC sufficient")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading) }
                val qrSplit = computeChargeContainerSplit(
                    amountPartial,
                    chargedFiat,
                    payCurrencyQr,
                    oracle,
                    unitPriceUSDC6,
                    ccsaPoints6,
                    infraPoints6,
                    infraCards.firstOrNull()?.cardCurrency,
                    usdcBalance6.toLong(),
                )
                var ccsaPointsWei = qrSplit.ccsaPointsWei
                var infraPointsWei = qrSplit.infraPointsWei
                var usdcWei = qrSplit.usdcWei
                val composedItems = mutableListOf<Map<String, Any>>()
                if (usdcWei > 0) {
                    composedItems.add(mapOf("kind" to 0, "asset" to USDC_BASE, "amount" to usdcWei.toString(), "tokenId" to "0", "data" to "0x"))
                }
                val beamio1155PointsWei = (ccsaPointsWei + infraPointsWei).coerceAtLeast(0L)
                if (beamio1155PointsWei > 0) {
                    composedItems.add(
                        mapOf(
                            "kind" to 1,
                            "asset" to infraCardAddress(),
                            "amount" to beamio1155PointsWei.toString(),
                            "tokenId" to "0",
                            "data" to "0x",
                        ),
                    )
                }
                if (composedItems.isEmpty()) {
                    composedItems.add(mapOf("kind" to 0, "asset" to USDC_BASE, "amount" to amountPartial.toString(), "tokenId" to "0", "data" to "0x"))
                }
                val itemsJson = org.json.JSONArray()
                composedItems.forEach { m -> itemsJson.put(org.json.JSONObject(m)) }
                val finalPayload = org.json.JSONObject(payload.toString())
                finalPayload.put("items", itemsJson)
                if (!finalPayload.has("maxAmount")) finalPayload.put("maxAmount", "0")
                val merchantAssets = fetchWalletAssetsSync(BeamioWeb3Wallet.getAddress(), merchantInfraOnly = false)
                val toAddr = merchantAssets?.aaAddress?.takeIf { it.isNotEmpty() } ?: payload.optString("to")
                if (toAddr.isEmpty()) {
                    runOnUiThread {
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = "Merchant AA not found. Please ensure terminal is configured."
                    }
                    return@Thread
                }
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
                    },
                )
                val qrPaymentMemberNo = memberNoPrimaryFromSortedCardsItem(assets)
                val qrPaymentCard = paymentCardForQrCharge
                val totalBalanceCad = (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
                runOnUiThread {
                    paymentChargeBreakdownTaxPercent = taxPctQr
                    paymentChargeBreakdownTierDiscountPercent = tierDiscQr
                    paymentScreenMemberNo = qrPaymentMemberNo.ifEmpty { null }
                    paymentScreenCardBackground = qrPaymentCard?.cardBackground
                    paymentScreenCardImage = qrPaymentCard?.cardImage
                    paymentScreenCardName = qrPaymentCard?.cardName
                    paymentScreenTierName = qrPaymentCard?.tierName
                    paymentScreenCardType = qrPaymentCard?.cardType
                    paymentScreenPassCardSnapshot = qrPaymentCard
                    paymentScreenPayerBeamioTag = assets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    paymentScreenPayerAddress = assets.address?.trim()
                    paymentScreenPayee = toAddr
                    paymentScreenTip = scaled.tipStr ?: "0.00"
                    paymentScreenAmount = "%.2f".format(chargedFiat)
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                    paymentScreenCardCurrency = payCurrencyQr
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val currencyAmountStr = "%.2f".format(chargedFiat)
                val subtotalStrForBill = scaled.subtotalStr
                val tipStrForBill = scaled.tipStr ?: "0.00"
                val qrChargeBill = OpenContainerChargeBillFields(
                    subtotal = subtotalStrForBill,
                    tip = tipStrForBill,
                    tipRateBps = paymentScreenTipRateBps,
                    requestCurrency = payCurrencyQr,
                    taxAmountFiat6 = taxFiat6Bill,
                    taxRateBps = taxBpsQr,
                    discountAmountFiat6 = discFiat6Bill,
                    discountRateBps = discBpsQr,
                )
                val result = postAAtoEOAOpenContainer(
                    finalPayload,
                    listOf(payCurrencyQr),
                    listOf(currencyAmountStr),
                    qrChargeBill,
                )
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "Payment failed"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                val customerAccountForPostBalance = account
                val preBalanceForCheck = totalBalanceCad
                val deductedCardAddress = if (beamio1155PointsWei > 0) infraCardAddress() else null
                if (!result.success) {
                    val errPost = result.error ?: "Payment failed"
                    runOnUiThread {
                        paymentScreenRoutingSteps = steps
                        paymentScreenStatus = PaymentStatus.Error
                        paymentScreenError = errPost
                    }
                    return@Thread
                }
                steps = updateStep(steps, "refreshBalance", StepStatus.loading, "Fetching latest balance")
                runOnUiThread {
                    paymentScreenRoutingSteps = steps
                    paymentScreenPostBalance = null
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenTxHash = result.txHash ?: ""
                    paymentScreenError = ""
                    paymentViaQr = true
                }
                Thread {
                    Thread.sleep(5000)
                    var postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true, merchantInfraOnly = false)
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
                        postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true, merchantInfraOnly = false)
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
                        if (postAssets != null && postAssets.ok && finalPostCad != null) "Updated" else "Unavailable",
                    )
                    runOnUiThread {
                        paymentScreenRoutingSteps = stepsAfter
                        var refreshedPass: CardItem? = null
                        if (postAssets != null && postAssets.ok && finalPostCad != null) {
                            paymentScreenPostBalance = "%.2f".format(finalPostCad)
                            refreshedPass = postAssets.cards?.firstOrNull { deductedCardAddress != null && it.cardAddress.equals(deductedCardAddress, ignoreCase = true) }
                                ?: postAssets.cards?.firstOrNull()
                            if (refreshedPass != null) {
                                paymentScreenPassCardSnapshot = refreshedPass
                                paymentScreenCardBackground = refreshedPass.cardBackground
                                paymentScreenCardImage = refreshedPass.cardImage
                                paymentScreenTierName = refreshedPass.tierName
                                paymentScreenCardName = refreshedPass.cardName
                            }
                        } else {
                            paymentScreenPostBalance = "—"
                        }
                        val hero = refreshedPass ?: qrPaymentCard
                        val shortfallQr = (totalCurrencyQr - chargedFiat).coerceAtLeast(0.0)
                        val memberTitleQr = passHeroMemberDisplayNameLine(
                            assets.beamioTag,
                            assets.address,
                            hero,
                            hero?.cardName,
                        )
                        val termQr = BeamioWeb3Wallet.getAddress()?.trim()?.takeIf { it.length >= 6 }
                            ?.let { "POS-${it.takeLast(4).uppercase(Locale.US)}" } ?: "—"
                        paymentPartialApprovalUi = PartialApprovalUiModel(
                            payCurrency = payCurrencyQr,
                            chargedPayCur = chargedFiat,
                            shortfallPayCur = shortfallQr,
                            orderTotalPayCur = totalCurrencyQr,
                            subtotal = requestQr,
                            tierDiscountAmount = discAmtCadQr,
                            tierDiscountLabel = hero?.tierName?.takeIf { it.isNotBlank() },
                            taxPercent = taxPctQr,
                            taxAmount = taxAmtCadQr,
                            tipAmount = tipQr,
                            memberDisplayName = memberTitleQr,
                            memberNoDisplay = qrPaymentMemberNo.ifBlank { "—" },
                            txHash = result.txHash ?: "",
                            settlementViaQr = true,
                            settlementDetail = termQr,
                            originalSubtotal = requestQr,
                            originalTipBps = paymentScreenTipRateBps,
                            tierDiscPct = tierDiscQr,
                            returnScanMethod = "qr",
                            tierCardBackgroundHex = hero?.cardBackground,
                            cardMetadataImageUrl = hero?.cardImage,
                            programCardDisplayName = hero?.let { balanceDetailsCardNameLine(it) }?.takeIf { it != "—" },
                        )
                        paymentScreenStatus = PaymentStatus.Success
                        lastQrPaymentOpenRelayPayload = null
                        scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                        scheduleElasticHomeDataPullAfterTxSuccess()
                    }
                }.start()
            } catch (e: Exception) {
                Log.e("MainActivity", "executeImmediatePartialQrFromInsufficient", e)
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Error
                    paymentScreenError = e.message ?: "Execution failed"
                }
            }
        }.start()
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
        topupScreenBeamioTag = null
        topupScreenCardBackground = null
        topupScreenCardImage = null
        topupScreenTierName = null
        topupScreenTierDescription = null
        topupScreenMemberNo = null
        topupScreenPassCardSnapshot = null
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
            paymentScreenPassCardSnapshot = null
            paymentScreenPayerBeamioTag = null
            paymentScreenPayerAddress = null
            paymentChargeBreakdownTierDiscountPercent = null
            scanMethodState = "qr"
            startEmbeddedQrScanning()
        } else {
            qrScanningActive = false
            scanMethodState = "nfc"
            paymentChargeBreakdownTierDiscountPercent = null
            paymentScreenPassCardSnapshot = null
            paymentScreenPayerBeamioTag = null
            paymentScreenPayerAddress = null
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
                topupScreenBeamioTag = null
                topupScreenCardBackground = null
                topupScreenCardImage = null
                topupScreenTierName = null
                topupScreenTierDescription = null
                topupScreenPassCardSnapshot = null
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
                paymentScreenPassCardSnapshot = null
                paymentScreenPayerBeamioTag = null
                paymentScreenPayerAddress = null
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
            topupScreenBeamioTag = null
            topupScreenCardBackground = null
            topupScreenCardImage = null
            topupScreenTierName = null
            topupScreenTierDescription = null
            topupScreenPassCardSnapshot = null
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
                paymentScreenCardBackground = null
                paymentScreenCardImage = null
                paymentScreenCardName = null
                paymentScreenTierName = null
                paymentScreenCardType = null
                paymentScreenMemberNo = null
                paymentScreenPassCardSnapshot = null
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
                refreshMerchantInfraCardFromDbSync()
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
                val preAssets = fetchUidAssetsSync(sunParams?.uid ?: uid, sunParams, merchantInfraOnly = false)
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

                // Admin mint 路径由服务端 nfcTopupPrepare 支持首发/普通充值（MemberCard: 不再在 prepare 层阻断无会员卡用户）；
                // 不得以 getUIDAssets 的 cards/nfts 为空做本地拦截（客户尚无点数/卡行未展开时易误判）。

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
                    topupScreenBeamioTag = preAssets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierName = topupCard?.tierName
                    topupScreenTierDescription = topupCard?.tierDescription
                    topupScreenMemberNo = memberNo.ifEmpty { null }
                    topupScreenPassCardSnapshot = topupCard
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
                            val postAssets = fetchUidAssetsSync(sunParams?.uid ?: uid, sunParams, merchantInfraOnly = false)
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
                                    if (postCard != null) topupScreenPassCardSnapshot = postCard
                                    topupScreenPostBalance = postCard?.points ?: postAssets.points ?: "—"
                                    topupScreenBeamioTag = postAssets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                                        ?: topupScreenBeamioTag
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
                                scheduleElasticHomeDataPullAfterTxSuccess()
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
            put("cardAddress", infraCardAddress())
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
                refreshMerchantInfraCardFromDbSync()
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
                refreshMerchantInfraCardFromDbSync()
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
                refreshMerchantInfraCardFromDbSync()
                // 1. Pull balance first; if EOA has no AA yet, ensure AA then retry once (QR / beamioTag topup).
                var preAssets = fetchWalletAssetsSync(wallet, merchantInfraOnly = false)
                if (preAssets == null || !preAssets.ok) {
                    Log.d(
                        "Topup",
                        "[Topup Debug] getWalletAssets first try failed wallet=$wallet err=${preAssets?.error} — ensureAAForEOA"
                    )
                    if (ensureAaForEoaSync(wallet)) {
                        preAssets = fetchWalletAssetsSync(wallet, merchantInfraOnly = false)
                    }
                }
                if (preAssets == null || !preAssets.ok) {
                    runOnUiThread { applyTopupQrScanScreenError(preAssets?.error ?: "Query failed") }
                    return
                }

                // 同上：不与服务端 admin topup 规则重复做「无会员卡」本地拦截。

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
                    topupScreenBeamioTag = preAssets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    topupScreenCardBackground = topupCard?.cardBackground
                    topupScreenCardImage = topupCard?.cardImage
                    topupScreenTierName = topupCard?.tierName
                    topupScreenTierDescription = topupCard?.tierDescription
                    topupScreenMemberNo = memberNo.ifEmpty { null }
                    topupScreenPassCardSnapshot = topupCard
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
                            val postAssets = fetchWalletAssetsSync(wallet, merchantInfraOnly = false)
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
                                    if (postCard != null) topupScreenPassCardSnapshot = postCard
                                    topupScreenPostBalance = postCard?.points ?: postAssets.points ?: "—"
                                    topupScreenBeamioTag = postAssets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                                        ?: topupScreenBeamioTag
                                } else {
                                    topupScreenPostBalance = "—"
                                }
                                topupScreenStatus = TopupStatus.Success
                                scheduleCardStatsRefetchUntilChanged(expectChargeChange = false, expectTopUpChange = true)
                                scheduleElasticHomeDataPullAfterTxSuccess()
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

    /** [currencyToUsdc6] 的逆：USDC6 → 账单货币金额（与 iOS `usdc6ToCurrencyAmount` 一致） */
    private fun usdc6ToCurrencyAmount(usdc6: Long, currency: String, oracle: OracleRates): Double {
        if (usdc6 <= 0L) return 0.0
        val rate = getRateForCurrency(currency, oracle)
        if (rate <= 0) return 0.0
        return (usdc6 / 1_000_000.0) * rate
    }

    /**
     * Charge 余额预检（贴卡 / 动态 QR 一致）：USDC + 各卡 points 按各自 cardCurrency 折 USDC6。
     * Smart Routing 仍用 [chargeableCards] + CCSA unit price 组装 items；此值仅用于是否不足额与 partial 封顶。
     */
    private fun chargePreflightTotalBalanceUsdc6(assets: UIDAssets, oracle: OracleRates): Long {
        val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
        val cardsValueUsdc6 = assets.cards?.sumOf { card ->
            points6ToUsdc6(card.points6.toLongOrNull() ?: 0L, card.cardCurrency, oracle)
        } ?: run {
            val pts6 = assets.points6?.toLongOrNull() ?: 0L
            val cur = assets.cardCurrency ?: "CAD"
            points6ToUsdc6(pts6, cur, oracle)
        }
        return usdcBalance6.toLong() + cardsValueUsdc6
    }

    /** 从 UIDAssets 计算总余额（CAD） */
    private fun totalBalanceCadFromAssets(assets: UIDAssets, oracle: OracleRates): Double {
        val totalBalance6 = chargePreflightTotalBalanceUsdc6(assets, oracle)
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
                refreshMerchantInfraCardFromDbSync()
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
                val assets = fetchUidAssetsSync(effectiveUid, sunParams, merchantInfraOnly = false)
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
                val tierDiscPct = pickChargeTierDiscountPercentForPaymentCard(
                    paymentCard,
                    assets,
                    routingDetails?.discountByTierKey ?: emptyMap()
                )
                val taxAmtCad = subtotalPay * taxPctResolved / 100.0
                val discAmtCad = subtotalPay * tierDiscPct / 100.0
                val taxFiat6Str = kotlin.math.round(taxAmtCad * 1_000_000.0).toLong().toString()
                val discFiat6Str = kotlin.math.round(discAmtCad * 1_000_000.0).toLong().toString()
                val taxBpsResolved = kotlin.math.round(taxPctResolved * 100.0).toInt().coerceIn(0, 10000)
                val discBpsResolved = kotlin.math.round(tierDiscPct * 100.0).toInt().coerceIn(0, 10000)
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
                    paymentScreenPassCardSnapshot = paymentCard
                    paymentScreenPayerBeamioTag = assets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    paymentScreenPayerAddress = assets.address?.trim()
                    paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading)
                }
                steps = updateStep(steps, "optimizingRoute", StepStatus.success, "Direct: NFC → Merchant")
                val totalBalance6 = chargePreflightTotalBalanceUsdc6(assets, oracle)
                // 初始余额（CAD）：总 USDC6 转 CAD，供 postBalance = pre - subtotal - tip
                val totalBalanceCad = (totalBalance6 / 1_000_000.0) * getRateForCurrency("CAD", oracle)
                runOnUiThread {
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                }
                val required6 = amountUsdc6.toLongOrNull() ?: 0L
                if (totalBalance6 >= required6) {
                    chargeCapToCustomerAvailableBalance = false
                }
                val ratePayForPartial = getRateForCurrency(payCurrency, oracle)
                val balancePayCurBeforeNfc = (totalBalance6 / 1_000_000.0) * ratePayForPartial
                var partialChargeNfc = false
                var effectiveAmountUsdc6Str = amountUsdc6
                var chargeTotalInPayForSplit = totalCurrency
                var nfcSubtotalStrEff = paymentScreenSubtotal.trim().ifEmpty { "%.2f".format(subtotalPay) }
                var nfcTipStrEff = tipStrForNfc
                var nfcTaxFiat6Eff = taxFiat6Str
                var nfcDiscFiat6Eff = discFiat6Str
                if (totalBalance6 < required6) {
                    val allowCapToAvailable = chargeCapToCustomerAvailableBalance && totalBalance6 > 0L
                    if (!allowCapToAvailable) {
                        steps = updateStep(steps, "analyzingAssets", StepStatus.error, "Insufficient balance")
                        val shortfall6 = (required6 - totalBalance6).coerceAtLeast(0L)
                        val errMsg = "Insufficient balance (need ${String.format("%.2f", required6 / 1_000_000.0)} USDC, total assets ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC, shortfall ${String.format("%.2f", shortfall6 / 1_000_000.0)} USDC)"
                        runOnUiThread {
                            val fromScan = nfcFetchingInfo
                            val model = buildInsufficientBalanceUiFromPaymentContext(
                                totalCurrency = totalCurrency,
                                payCurrency = payCurrency,
                                totalBalance6 = totalBalance6,
                                required6 = required6,
                                oracle = oracle,
                                subtotal = subtotalPay,
                                taxPct = taxPctResolved,
                                tierDiscPct = tierDiscPct,
                                tip = tipPay,
                                returnScanMethod = scanMethodState
                            )
                            insufficientPartialRetryContext = InsufficientPartialRetryContext(
                                totalCurrency = totalCurrency,
                                payCurrency = payCurrency,
                                availableUsdc6 = totalBalance6,
                                requiredUsdc6 = required6,
                                subtotal = subtotalPay,
                                taxPct = taxPctResolved,
                                tierDiscPct = tierDiscPct,
                                tip = tipPay,
                                returnScanMethod = if (scanMethodState == "qr") "qr" else "nfc",
                                nfcUid = effectiveUid,
                                nfcSun = sunParams,
                            )
                            openInsufficientBalanceScreen(model)
                            if (fromScan) {
                                nfcFetchingInfo = false
                                nfcFetchError = ""
                            }
                            Log.w("Charge", "Insufficient balance (NFC pre-tx): $errMsg")
                        }
                        return@Thread
                    }
                    chargeCapToCustomerAvailableBalance = false
                    partialChargeNfc = true
                    val ratioNfc = totalBalance6.toDouble() / required6.toDouble().coerceAtLeast(1.0)
                    effectiveAmountUsdc6Str = totalBalance6.toString()
                    chargeTotalInPayForSplit = totalCurrency * ratioNfc
                    nfcSubtotalStrEff = "%.2f".format(subtotalPay * ratioNfc)
                    nfcTipStrEff = "%.2f".format(tipPay * ratioNfc)
                    nfcTaxFiat6Eff = kotlin.math.round(taxAmtCad * ratioNfc * 1_000_000.0).toLong().toString()
                    nfcDiscFiat6Eff = kotlin.math.round(discAmtCad * ratioNfc * 1_000_000.0).toLong().toString()
                    runOnUiThread {
                        paymentScreenAmount = "%.2f".format(balancePayCurBeforeNfc)
                        paymentScreenTip = nfcTipStrEff
                    }
                    Log.w("Charge", "PARTIAL NFC charge: using full balance totalBalance6=$totalBalance6 required6=$required6 ratio=$ratioNfc")
                }
                runOnUiThread {
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val result = payByNfcUidWithContainer(
                    effectiveUid,
                    effectiveAmountUsdc6Str,
                    payee,
                    assets,
                    oracle,
                    sunParams,
                    chargeTotalInPayCurrency = chargeTotalInPayForSplit,
                    nfcSubtotalCurrencyAmount = nfcSubtotalStrEff,
                    nfcTipCurrencyAmount = nfcTipStrEff,
                    nfcTipRateBps = paymentScreenTipRateBps,
                    nfcRequestCurrency = payCurrency,
                    nfcTaxAmountFiat6 = nfcTaxFiat6Eff,
                    nfcTaxRateBps = taxBpsResolved,
                    nfcDiscountAmountFiat6 = nfcDiscFiat6Eff,
                    nfcDiscountRateBps = discBpsResolved
                )
                steps = updateStep(steps, "sendTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Sent" else (result.error ?: "Payment failed"))
                steps = updateStep(steps, "waitTx", if (result.success) StepStatus.success else StepStatus.error, if (result.success) "Transaction complete" else (result.error ?: ""))
                if (!result.success) {
                    val err = result.error ?: "Payment failed"
                    runOnUiThread {
                        val fromScan = nfcFetchingInfo
                        val parsed = parseInsufficientBalanceUsdcFromError(err)
                        if (parsed != null && err.startsWith("Insufficient balance", ignoreCase = true)) {
                            val (needU, totalU, shortU) = parsed
                            val rate = getRateForCurrency(payCurrency, oracle)
                            val taxAmtLine = subtotalPay * taxPctResolved / 100.0
                            val discAmtLine = subtotalPay * tierDiscPct / 100.0
                            val avail6FromErr = kotlin.math.round(totalU * 1_000_000.0).toLong().coerceAtLeast(0L)
                            val need6FromErr = kotlin.math.round(needU * 1_000_000.0).toLong().coerceAtLeast(0L)
                            val model = InsufficientBalanceUiModel(
                                totalChargeFormatted = formatFiatDisplayLineForInsufficient(totalCurrency, payCurrency),
                                currentBalanceFormatted = formatFiatDisplayLineForInsufficient(totalU * rate, payCurrency),
                                shortfallFormatted = "-${formatFiatDisplayLineForInsufficient(shortU * rate, payCurrency)}",
                                voucherDeductionFormatted = formatFiatDisplayLineForInsufficient(subtotalPay, payCurrency),
                                tierDiscountFormatted = formatFiatDisplayLineForInsufficient(discAmtLine, payCurrency),
                                tipFormatted = formatFiatDisplayLineForInsufficient(tipPay, payCurrency),
                                taxFormatted = formatFiatDisplayLineForInsufficient(taxAmtLine, payCurrency),
                                returnScanMethod = if (scanMethodState == "qr") "qr" else "nfc",
                                availableBalanceUsdc6 = avail6FromErr,
                            )
                            insufficientPartialRetryContext = InsufficientPartialRetryContext(
                                totalCurrency = totalCurrency,
                                payCurrency = payCurrency,
                                availableUsdc6 = avail6FromErr,
                                requiredUsdc6 = need6FromErr,
                                subtotal = subtotalPay,
                                taxPct = taxPctResolved,
                                tierDiscPct = tierDiscPct,
                                tip = tipPay,
                                returnScanMethod = if (scanMethodState == "qr") "qr" else "nfc",
                                nfcUid = effectiveUid,
                                nfcSun = sunParams,
                            )
                            openInsufficientBalanceScreen(model)
                            if (fromScan) {
                                nfcFetchingInfo = false
                                nfcFetchError = ""
                            }
                        } else {
                            paymentScreenRoutingSteps = steps
                            paymentScreenStatus = PaymentStatus.Error
                            paymentScreenError = err
                            if (fromScan) {
                                nfcFetchingInfo = false
                                nfcFetchError = err
                            }
                        }
                    }
                } else {
                    val payPassSnapshotAddr = paymentCard?.cardAddress
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
                        val postAssets = fetchUidAssetsSync(effectiveUid, sunParams, merchantInfraOnly = false)
                        val stepsAfter = updateStep(
                            steps,
                            "refreshBalance",
                            StepStatus.success,
                            if (postAssets != null && postAssets.ok) "Updated" else "Unavailable"
                        )
                        runOnUiThread {
                            paymentScreenRoutingSteps = stepsAfter
                            var refreshedPass: CardItem? = null
                            if (postAssets != null && postAssets.ok) {
                                paymentScreenPostBalance = "%.2f".format(totalBalanceCadFromAssets(postAssets, oracle))
                                refreshedPass = postAssets.cards?.firstOrNull { payPassSnapshotAddr != null && it.cardAddress.equals(payPassSnapshotAddr, ignoreCase = true) }
                                    ?: postAssets.cards?.firstOrNull()
                                if (refreshedPass != null) {
                                    paymentScreenPassCardSnapshot = refreshedPass
                                    paymentScreenCardBackground = refreshedPass.cardBackground
                                    paymentScreenCardImage = refreshedPass.cardImage
                                    paymentScreenTierName = refreshedPass.tierName
                                    paymentScreenCardName = refreshedPass.cardName
                                }
                            } else {
                                paymentScreenPostBalance = "—"
                            }
                            if (partialChargeNfc) {
                                val hero = refreshedPass ?: paymentCard
                                val shortfallNfc = (totalCurrency - balancePayCurBeforeNfc).coerceAtLeast(0.0)
                                val memberTitleNfc = passHeroMemberDisplayNameLine(
                                    assets.beamioTag,
                                    assets.address,
                                    hero,
                                    hero?.cardName,
                                )
                                val mn = (paymentMemberNo.ifEmpty { memberNoPrimaryFromSortedCardsItem(assets) }).ifBlank { "—" }
                                val term = BeamioWeb3Wallet.getAddress()?.trim()?.takeIf { it.length >= 6 }?.let { "POS-${it.takeLast(4).uppercase(Locale.US)}" } ?: "—"
                                paymentPartialApprovalUi = PartialApprovalUiModel(
                                    payCurrency = payCurrency,
                                    chargedPayCur = balancePayCurBeforeNfc,
                                    shortfallPayCur = shortfallNfc,
                                    orderTotalPayCur = totalCurrency,
                                    subtotal = subtotalPay,
                                    tierDiscountAmount = discAmtCad,
                                    tierDiscountLabel = hero?.tierName?.takeIf { it.isNotBlank() },
                                    taxPercent = taxPctResolved,
                                    taxAmount = taxAmtCad,
                                    tipAmount = tipPay,
                                    memberDisplayName = memberTitleNfc,
                                    memberNoDisplay = mn,
                                    txHash = result.txHash ?: "",
                                    settlementViaQr = false,
                                    settlementDetail = term,
                                    originalSubtotal = subtotalPay,
                                    originalTipBps = paymentScreenTipRateBps,
                                    tierDiscPct = tierDiscPct,
                                    returnScanMethod = if (scanMethodState == "qr") "qr" else "nfc",
                                    tierCardBackgroundHex = hero?.cardBackground,
                                    cardMetadataImageUrl = hero?.cardImage,
                                    programCardDisplayName = hero?.let { balanceDetailsCardNameLine(it) }?.takeIf { it != "—" },
                                )
                            } else {
                                paymentPartialApprovalUi = null
                            }
                            paymentScreenStatus = PaymentStatus.Success
                            scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                            scheduleElasticHomeDataPullAfterTxSuccess()
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
                refreshMerchantInfraCardFromDbSync()
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
                lastQrPaymentOpenRelayPayload = try {
                    org.json.JSONObject(payload.toString())
                } catch (_: Exception) {
                    null
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
                val assets = fetchWalletAssetsSync(account, merchantInfraOnly = false)
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
                // 与 NFC executePayment 一致：账单/折扣以客户主卡（服务端 cards[0]）为准
                val paymentCardForQrCharge = assets.cards?.firstOrNull()
                val tierDiscQr = pickChargeTierDiscountPercentForPaymentCard(
                    paymentCardForQrCharge,
                    assets,
                    routingDetailsQr?.discountByTierKey ?: emptyMap()
                )
                val requestQr = paymentScreenSubtotal.toDoubleOrNull() ?: 0.0
                val tipQr = chargeTipFromRequestAndBps(requestQr, paymentScreenTipRateBps)
                val taxAmtCadQr = requestQr * taxPctQr / 100.0
                val discAmtCadQr = requestQr * tierDiscQr / 100.0
                val taxFiat6StrQr = kotlin.math.round(taxAmtCadQr * 1_000_000.0).toLong().toString()
                val discFiat6StrQr = kotlin.math.round(discAmtCadQr * 1_000_000.0).toLong().toString()
                val taxBpsQr = kotlin.math.round(taxPctQr * 100.0).toInt().coerceIn(0, 10000)
                val discBpsQr = kotlin.math.round(tierDiscQr * 100.0).toInt().coerceIn(0, 10000)
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
                val hasCardholder = (assets.cards?.any { it.cardType == "ccsa" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) } == true) ||
                    (assets.points6?.toLongOrNull() ?: 0L) > 0
                steps = updateStep(steps, "membership", StepStatus.success, if (hasCardholder) "Cardholder" else "No membership")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "analyzingAssets", StepStatus.loading) }
                // QR Charge：与 NFC payByNfcUidWithContainer 对齐——不按用户卡合约地址（如 CCSA 克隆 0x2032…）拆分 container；
                // 可扣款余额仍来自 chargeableCards，但 ERC1155 扣款 asset 固定为当前基础设施卡（infraCardAddress）。
                val unitPriceStr = assets.unitPriceUSDC6
                val unitPriceUSDC6 = unitPriceStr?.toLongOrNull() ?: 0L
                val cards = chargeableCards(assets)
                val ccsaCards = cards.filter { it.cardType == "ccsa" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
                val infraCards = cards.filter { it.cardType == "infrastructure" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
                val ccsaPoints6 = ccsaCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val infraPoints6 = infraCards.sumOf { it.points6.toLongOrNull() ?: 0L }
                val usdcBalance6 = (assets.usdcBalance?.toDoubleOrNull() ?: 0.0) * 1_000_000
                val ccsaValueUsdc6 = if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) (ccsaPoints6 * unitPriceUSDC6) / 1_000_000 else 0L
                val infraValueUsdc6 = infraCards.sumOf { c -> points6ToUsdc6(c.points6.toLongOrNull() ?: 0L, c.cardCurrency, oracle) }
                val totalBalance6 = chargePreflightTotalBalanceUsdc6(assets, oracle)
                val rateQrPartial = getRateForCurrency(payCurrencyQr, oracle)
                val balancePayCurBeforeQr = (totalBalance6 / 1_000_000.0) * rateQrPartial
                var partialChargeQr = false
                var effectiveUsdc6Final = enteredUsdc6
                var chargeTotalForSplitQr = totalCurrencyQr
                var tipQrEff = tipQr
                var requestQrEff = requestQr
                var taxAmtQrEff = taxAmtCadQr
                var discAmtQrEff = discAmtCadQr
                var taxFiat6Bill = taxFiat6StrQr
                var discFiat6Bill = discFiat6StrQr
                if (totalBalance6 >= enteredUsdc6) {
                    chargeCapToCustomerAvailableBalance = false
                }
                if (totalBalance6 < enteredUsdc6) {
                    val allowCapQr = chargeCapToCustomerAvailableBalance && totalBalance6 > 0L
                    if (!allowCapQr) {
                        val shortfall6Qr = (enteredUsdc6 - totalBalance6).coerceAtLeast(0L)
                        val errQr = "Insufficient customer balance (need ${String.format("%.2f", enteredUsdc6 / 1_000_000.0)} USDC, total assets ${String.format("%.2f", totalBalance6 / 1_000_000.0)} USDC)"
                        runOnUiThread {
                            val model = buildInsufficientBalanceUiFromPaymentContext(
                                totalCurrency = totalCurrencyQr,
                                payCurrency = payCurrencyQr,
                                totalBalance6 = totalBalance6,
                                required6 = enteredUsdc6,
                                oracle = oracle,
                                subtotal = requestQr,
                                taxPct = taxPctQr,
                                tierDiscPct = tierDiscQr,
                                tip = tipQr,
                                /** QR execute 路径固定回到扫码，与 NFC 分支显式区分方式一致 */
                                returnScanMethod = "qr"
                            )
                            insufficientPartialRetryContext = InsufficientPartialRetryContext(
                                totalCurrency = totalCurrencyQr,
                                payCurrency = payCurrencyQr,
                                availableUsdc6 = totalBalance6,
                                requiredUsdc6 = enteredUsdc6,
                                subtotal = requestQr,
                                taxPct = taxPctQr,
                                tierDiscPct = tierDiscQr,
                                tip = tipQr,
                                returnScanMethod = "qr",
                                qrAccount = account,
                                qrPayloadJson = try {
                                    payload.toString()
                                } catch (_: Exception) {
                                    null
                                },
                            )
                            openInsufficientBalanceScreen(model)
                            nfcFetchingInfo = false
                            qrScanningActive = false
                            Log.w("Charge", "Insufficient balance (QR pre-tx): $errQr shortfall6=$shortfall6Qr")
                        }
                        return@Thread
                    }
                    chargeCapToCustomerAvailableBalance = false
                    partialChargeQr = true
                    val ratioQr = totalBalance6.toDouble() / enteredUsdc6.toDouble().coerceAtLeast(1.0)
                    effectiveUsdc6Final = totalBalance6
                    chargeTotalForSplitQr = totalCurrencyQr * ratioQr
                    requestQrEff = requestQr * ratioQr
                    tipQrEff = tipQr * ratioQr
                    taxAmtQrEff = taxAmtCadQr * ratioQr
                    discAmtQrEff = discAmtCadQr * ratioQr
                    taxFiat6Bill = kotlin.math.round(taxAmtQrEff * 1_000_000.0).toLong().toString()
                    discFiat6Bill = kotlin.math.round(discAmtQrEff * 1_000_000.0).toLong().toString()
                    Log.w("Charge", "PARTIAL QR charge: totalBalance6=$totalBalance6 enteredUsdc6=$enteredUsdc6 ratio=$ratioQr")
                }
                val effectiveUsdc6 = effectiveUsdc6Final
                steps = updateStep(steps, "analyzingAssets", StepStatus.success,
                    if (ccsaValueUsdc6 >= effectiveUsdc6) "\$CCSA: (Sufficient)" else if (ccsaValueUsdc6 > 0) "\$CCSA: (Partial)" else "USDC sufficient")
                runOnUiThread { paymentScreenRoutingSteps = updateStep(steps, "optimizingRoute", StepStatus.loading) }
                val qrSplit = computeChargeContainerSplit(
                    effectiveUsdc6,
                    chargeTotalForSplitQr,
                    payCurrencyQr,
                    oracle,
                    unitPriceUSDC6,
                    ccsaPoints6,
                    infraPoints6,
                    infraCards.firstOrNull()?.cardCurrency,
                    usdcBalance6.toLong(),
                )
                var ccsaPointsWei = qrSplit.ccsaPointsWei
                var infraPointsWei = qrSplit.infraPointsWei
                var usdcWei = qrSplit.usdcWei
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
                            "asset" to infraCardAddress(),
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
                val merchantAssets = fetchWalletAssetsSync(BeamioWeb3Wallet.getAddress(), merchantInfraOnly = false)
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
                    paymentScreenPassCardSnapshot = qrPaymentCard
                    paymentScreenPayerBeamioTag = assets.beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
                    paymentScreenPayerAddress = assets.address?.trim()
                    paymentScreenPayee = toAddr
                    paymentScreenTip = "%.2f".format(tipQrEff)
                    paymentScreenAmount = if (partialChargeQr) "%.2f".format(balancePayCurBeforeQr) else "%.2f".format(totalCurrencyQr)
                    paymentScreenPreBalance = "%.2f".format(totalBalanceCad)
                    paymentScreenCardCurrency = payCurrencyQr
                    paymentScreenStatus = PaymentStatus.Submitting
                    paymentScreenRoutingSteps = updateStep(steps, "sendTx", StepStatus.loading)
                }
                val currencyAmountStr = "%.2f".format(chargeTotalForSplitQr)
                // 与 payByNfcUidWithContainer 一致：小计/小费用屏幕原始字符串，税/折扣 fiat6+bps 与 NFC 相同算法
                val subtotalStrForBill = "%.2f".format(requestQrEff)
                val tipStrForBill = "%.2f".format(tipQrEff)
                val qrChargeBill = OpenContainerChargeBillFields(
                    subtotal = subtotalStrForBill,
                    tip = tipStrForBill,
                    tipRateBps = paymentScreenTipRateBps,
                    requestCurrency = payCurrencyQr,
                    taxAmountFiat6 = taxFiat6Bill,
                    taxRateBps = taxBpsQr,
                    discountAmountFiat6 = discFiat6Bill,
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
                val deductedCardAddress = if (beamio1155PointsWei > 0) infraCardAddress() else null
                if (!result.success) {
                    val errPost = result.error ?: "Payment failed"
                    runOnUiThread {
                        val parsedQr = parseInsufficientBalanceUsdcFromError(errPost)
                        if (parsedQr != null && errPost.startsWith("Insufficient balance", ignoreCase = true)) {
                            val (needU, totalU, shortU) = parsedQr
                            val rateE = getRateForCurrency(payCurrencyQr, oracle)
                            val taxAmtLineQr = requestQr * taxPctQr / 100.0
                            val discAmtLineQr = requestQr * tierDiscQr / 100.0
                            val avail6Post = kotlin.math.round(totalU * 1_000_000.0).toLong().coerceAtLeast(0L)
                            val need6Post = kotlin.math.round(needU * 1_000_000.0).toLong().coerceAtLeast(0L)
                            val modelPost = InsufficientBalanceUiModel(
                                totalChargeFormatted = formatFiatDisplayLineForInsufficient(totalCurrencyQr, payCurrencyQr),
                                currentBalanceFormatted = formatFiatDisplayLineForInsufficient(totalU * rateE, payCurrencyQr),
                                shortfallFormatted = "-${formatFiatDisplayLineForInsufficient(shortU * rateE, payCurrencyQr)}",
                                voucherDeductionFormatted = formatFiatDisplayLineForInsufficient(requestQr, payCurrencyQr),
                                tierDiscountFormatted = formatFiatDisplayLineForInsufficient(discAmtLineQr, payCurrencyQr),
                                tipFormatted = formatFiatDisplayLineForInsufficient(tipQr, payCurrencyQr),
                                taxFormatted = formatFiatDisplayLineForInsufficient(taxAmtLineQr, payCurrencyQr),
                                returnScanMethod = "qr",
                                availableBalanceUsdc6 = avail6Post,
                            )
                            insufficientPartialRetryContext = InsufficientPartialRetryContext(
                                totalCurrency = totalCurrencyQr,
                                payCurrency = payCurrencyQr,
                                availableUsdc6 = avail6Post,
                                requiredUsdc6 = need6Post,
                                subtotal = requestQr,
                                taxPct = taxPctQr,
                                tierDiscPct = tierDiscQr,
                                tip = tipQr,
                                returnScanMethod = "qr",
                                qrAccount = account,
                                qrPayloadJson = try {
                                    finalPayload.toString()
                                } catch (_: Exception) {
                                    null
                                },
                            )
                            openInsufficientBalanceScreen(modelPost)
                            qrScanningActive = false
                            Log.w("Charge", "Insufficient balance (QR post-tx): $errPost")
                        } else {
                            paymentScreenRoutingSteps = steps
                            nfcFetchingInfo = false
                            paymentScreenStatus = PaymentStatus.Error
                            paymentScreenError = errPost
                        }
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
                        var postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true, merchantInfraOnly = false)
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
                            postAssets = fetchWalletAssetsSync(customerAccountForPostBalance, forPostPayment = true, merchantInfraOnly = false)
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
                            var refreshedPass: CardItem? = null
                            if (postAssets != null && postAssets.ok && finalPostCad != null) {
                                paymentScreenPostBalance = "%.2f".format(finalPostCad)
                                refreshedPass = postAssets.cards?.firstOrNull { deductedCardAddress != null && it.cardAddress.equals(deductedCardAddress, ignoreCase = true) }
                                    ?: postAssets.cards?.firstOrNull()
                                if (refreshedPass != null) {
                                    paymentScreenPassCardSnapshot = refreshedPass
                                    paymentScreenCardBackground = refreshedPass.cardBackground
                                    paymentScreenCardImage = refreshedPass.cardImage
                                    paymentScreenTierName = refreshedPass.tierName
                                    paymentScreenCardName = refreshedPass.cardName
                                }
                            } else {
                                paymentScreenPostBalance = "—"
                            }
                            if (partialChargeQr) {
                                val hero = refreshedPass ?: qrPaymentCard
                                val shortfallQr = (totalCurrencyQr - balancePayCurBeforeQr).coerceAtLeast(0.0)
                                val memberTitleQr = passHeroMemberDisplayNameLine(
                                    assets.beamioTag,
                                    assets.address,
                                    hero,
                                    hero?.cardName,
                                )
                                val termQr = BeamioWeb3Wallet.getAddress()?.trim()?.takeIf { it.length >= 6 }
                                    ?.let { "POS-${it.takeLast(4).uppercase(Locale.US)}" } ?: "—"
                                paymentPartialApprovalUi = PartialApprovalUiModel(
                                    payCurrency = payCurrencyQr,
                                    chargedPayCur = balancePayCurBeforeQr,
                                    shortfallPayCur = shortfallQr,
                                    orderTotalPayCur = totalCurrencyQr,
                                    subtotal = requestQr,
                                    tierDiscountAmount = discAmtCadQr,
                                    tierDiscountLabel = hero?.tierName?.takeIf { it.isNotBlank() },
                                    taxPercent = taxPctQr,
                                    taxAmount = taxAmtCadQr,
                                    tipAmount = tipQr,
                                    memberDisplayName = memberTitleQr,
                                    memberNoDisplay = qrPaymentMemberNo.ifBlank { "—" },
                                    txHash = result.txHash ?: "",
                                    settlementViaQr = true,
                                    settlementDetail = termQr,
                                    originalSubtotal = requestQr,
                                    originalTipBps = paymentScreenTipRateBps,
                                    tierDiscPct = tierDiscQr,
                                    returnScanMethod = "qr",
                                    tierCardBackgroundHex = hero?.cardBackground,
                                    cardMetadataImageUrl = hero?.cardImage,
                                    programCardDisplayName = hero?.let { balanceDetailsCardNameLine(it) }?.takeIf { it != "—" },
                                )
                            } else {
                                paymentPartialApprovalUi = null
                            }
                            paymentScreenStatus = PaymentStatus.Success
                            lastQrPaymentOpenRelayPayload = null
                            scheduleCardStatsRefetchUntilChanged(expectChargeChange = true, expectTopUpChange = false)
                            scheduleElasticHomeDataPullAfterTxSuccess()
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
                // 与 NFC Charge 一致：每笔 Open relay 带商户卡地址，供 Indexer 推导 topAdmin/subordinate（owner 直属下级时 topAdmin=owner EOA）
                put("merchantCardAddress", infraCardAddress())
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
                }
            }
            try {
                attachChargeOwnerChildBurnToRequestBody(body, payload)
            } catch (_: Exception) { /* optional: owner-child burn only when chain + infra items match */ }
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

    private data class ScaledNfcBillPartial(
        val subtotalStr: String,
        val tipStr: String?,
        val taxFiat6: String,
        val taxBps: Int,
        val discFiat6: String,
        val discBps: Int,
    )

    /**
     * 余额不足全屏页点「Charge available balance」：保存 NFC uid/SUN 或 QR payload，供立即 partial execute（对齐 iOS）。
     */
    private data class InsufficientPartialRetryContext(
        val totalCurrency: Double,
        val payCurrency: String,
        val availableUsdc6: Long,
        val requiredUsdc6: Long,
        val subtotal: Double,
        val taxPct: Double,
        val tierDiscPct: Double,
        val tip: Double,
        val returnScanMethod: String,
        val nfcUid: String? = null,
        val nfcSun: SunParams? = null,
        val qrAccount: String? = null,
        val qrPayloadJson: String? = null,
    ) {
        fun scaledBillForChargedPayCur(chargedFiat: Double): ScaledNfcBillPartial {
            val total = totalCurrency.coerceAtLeast(1e-9)
            val ratio = (chargedFiat / total).coerceIn(0.0, 1.0)
            val sReq = subtotal * ratio
            val sTip = tip * ratio
            val taxFiat6 = kotlin.math.round(sReq * taxPct / 100.0 * 1_000_000.0).toLong()
            val taxBps = kotlin.math.round(taxPct * 100.0).toInt().coerceIn(0, 10000)
            val discFiat6 = kotlin.math.round(sReq * tierDiscPct / 100.0 * 1_000_000.0).toLong()
            val discBps = kotlin.math.round(tierDiscPct * 100.0).toInt().coerceIn(0, 10000)
            val tipStr = if (sTip > 0) "%.2f".format(sTip) else null
            return ScaledNfcBillPartial(
                subtotalStr = "%.2f".format(sReq),
                tipStr = tipStr,
                taxFiat6 = taxFiat6.toString(),
                taxBps = taxBps,
                discFiat6 = discFiat6.toString(),
                discBps = discBps,
            )
        }
    }

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

    private fun fetchUidAssetsSync(uid: String, sunParams: SunParams? = null, merchantInfraOnly: Boolean = false): UIDAssets? {
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
                putMerchantInfraParams(merchantInfraOnly)
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
            it.cardType == "ccsa" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) ||
            it.cardType == "infrastructure" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true)
        } ?: emptyList()
    }

    private data class ChargeContainerSplit(
        val ccsaPointsWei: Long,
        val infraPointsWei: Long,
        val usdcWei: Long,
    )

    /**
     * Charge 路由：先 CCSA（按 unitPriceUSDC6），再基础设施点，最后 USDC。
     * 当账单币种与基础设施卡 [infraCardCurrency] 一致时，基础设施点按「找零」直接用本币 fiat6（与 charge 合计同一套 round）
     * 计算，避免总金额先换 USDC 再 ceil 换回点数产生的误差。
     */
    private fun computeChargeContainerSplit(
        amountBig: Long,
        chargeTotalInPayCurrency: Double,
        payCurrency: String,
        oracle: OracleRates,
        unitPriceUSDC6: Long,
        ccsaPoints6: Long,
        infraPoints6: Long,
        infraCardCurrency: String?,
        usdcBalance6: Long,
    ): ChargeContainerSplit {
        if (amountBig <= 0L) return ChargeContainerSplit(0L, 0L, 0L)
        var remaining = amountBig
        var ccsaPointsWei = 0L
        if (ccsaPoints6 > 0 && unitPriceUSDC6 > 0) {
            val maxPointsFromAmount = (remaining * 1_000_000) / unitPriceUSDC6
            ccsaPointsWei = minOf(maxPointsFromAmount, ccsaPoints6)
            val ccsaValue = (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
            remaining -= ccsaValue
            if (usdcBalance6 == 0L && remaining > 0 && ccsaPoints6 > ccsaPointsWei) {
                val ccsaPointsCeil = (amountBig * 1_000_000 + unitPriceUSDC6 - 1) / unitPriceUSDC6
                if (ccsaPointsCeil <= ccsaPoints6) {
                    ccsaPointsWei = ccsaPointsCeil
                    remaining = amountBig - (ccsaPointsWei * unitPriceUSDC6) / 1_000_000
                }
            }
        }
        val remainingAfterCcsa = remaining
        val ccsaConsumedUsdc6 = amountBig - remainingAfterCcsa
        var infraPointsWei = 0L
        val payCur = payCurrency.trim().uppercase()
        val infraCur = infraCardCurrency?.trim()?.uppercase()
        var infraFromFiat = false
        if (remainingAfterCcsa > 0 && infraPoints6 > 0 && infraCur != null) {
            if (infraCur == payCur && chargeTotalInPayCurrency > 0) {
                val R = getRateForCurrency(payCur, oracle)
                if (R > 0) {
                    val totalFiat6 = kotlin.math.round(chargeTotalInPayCurrency * 1_000_000.0).toLong()
                    val remainingFiat6 =
                        (totalFiat6 - kotlin.math.round(ccsaConsumedUsdc6 * R).toLong()).coerceAtLeast(0L)
                    infraPointsWei = minOf(infraPoints6, remainingFiat6)
                    val infraUsdc6Equiv = points6ToUsdc6(infraPointsWei, payCur, oracle)
                    remaining = (remainingAfterCcsa - infraUsdc6Equiv).coerceAtLeast(0L)
                    infraFromFiat = true
                }
            }
            if (!infraFromFiat) {
                val rate = getRateForCurrency(infraCur, oracle)
                if (rate > 0) {
                    val infraValueUsdc6Total = points6ToUsdc6(infraPoints6, infraCur, oracle)
                    val infraValueUsdc6Needed = minOf(remainingAfterCcsa, infraValueUsdc6Total)
                    infraPointsWei = kotlin.math.ceil(infraValueUsdc6Needed * rate).toLong().coerceIn(0L, infraPoints6)
                    remaining = (remainingAfterCcsa - points6ToUsdc6(infraPointsWei, infraCur, oracle)).coerceAtLeast(0L)
                    if (remaining > 0 && usdcBalance6 == 0L && infraPointsWei < infraPoints6) {
                        val extraPoints = kotlin.math.ceil(remaining * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                        infraPointsWei += extraPoints
                        remaining = 0L
                    }
                }
            }
        }
        var usdcWei = remaining.coerceAtLeast(0L)
        if (usdcWei > 0 && usdcBalance6 == 0L && infraPoints6 > 0 && infraPointsWei < infraPoints6) {
            val rate = getRateForCurrency(infraCardCurrency?.trim()?.uppercase() ?: payCur, oracle)
            if (rate > 0) {
                val extraPoints = kotlin.math.ceil(usdcWei * rate).toLong().coerceIn(0L, infraPoints6 - infraPointsWei)
                infraPointsWei += extraPoints
                usdcWei = 0L
            }
        }
        return ChargeContainerSplit(ccsaPointsWei, infraPointsWei, usdcWei)
    }

    /** 新流程：Android 自行 Smart Routing 构建 container，服务端仅签名并 relay。CCSA + 基础设施卡均可扣款。NFC 格式(14 位 hex uid)时需传 sunParams(e,c,m) 做 SUN 校验。 */
    private fun payByNfcUidWithContainer(
        uid: String,
        amountUsdc6: String,
        payee: String,
        assets: UIDAssets,
        oracle: OracleRates,
        sunParams: SunParams? = null,
        chargeTotalInPayCurrency: Double,
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
        val ccsaCards = cards.filter { it.cardType == "ccsa" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
        val infraCards = cards.filter { it.cardType == "infrastructure" || it.cardAddress.equals(infraCardAddress(), ignoreCase = true) }
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
        val split = computeChargeContainerSplit(
            amountBig,
            chargeTotalInPayCurrency,
            nfcRequestCurrency,
            oracle,
            unitPriceUSDC6,
            ccsaPoints6,
            infraPoints6,
            infraCards.firstOrNull()?.cardCurrency,
            usdcBalance6.toLong(),
        )
        var ccsaPointsWei = split.ccsaPointsWei
        var infraPointsWei = split.infraPointsWei
        var usdcWei = split.usdcWei
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
                "asset" to infraCardAddress(),
                "amount" to ccsaPointsWei.toString(),
                "tokenId" to "0",
                "data" to "0x"
            ))
        }
        if (infraPointsWei > 0) {
            items.add(mapOf(
                "kind" to 1,
                "asset" to infraCardAddress(),
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
            }
            try {
                attachChargeOwnerChildBurnToRequestBody(body, containerPayload)
            } catch (_: Exception) { /* skip burn attachment */ }
            val bodyStr = body.toString()
            conn.outputStream.use { os -> os.write(bodyStr.toByteArray(Charsets.UTF_8)) }
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
                put("cardAddress", infraCardAddress())
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
                    linkAppDeepLinkUrl = ""
                    linkAppLastSunForCancel = null
                    linkAppCancelInProgress = false
                    linkAppArmed = false
                    scanWaitingForNfc = false
                    nfcFetchingInfo = false
                    qrScanningActive = false
                    showScanMethodScreen = false
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

    private fun fetchWalletAssets(wallet: String, merchantInfraOnly: Boolean = true) {
        Thread {
            try {
                refreshMerchantInfraCardFromDbSync()
                val apiUrl = java.net.URL("$BEAMIO_API/api/getWalletAssets")
                val conn = apiUrl.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                val body = org.json.JSONObject().apply {
                    put("wallet", wallet)
                    putMerchantInfraParams(merchantInfraOnly)
                }.toString()
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

    /**
     * GET /api/ensureAAForEOA — Master queues createAccountFor when EOA has no deployed AA.
     * Used after getWalletAssets fails (e.g. 404) so QR topup can proceed once AA exists.
     */
    private fun ensureAaForEoaSync(eoa: String): Boolean {
        val trimmed = eoa.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("0x", ignoreCase = true)) return false
        return try {
            val url = java.net.URL(
                "$BEAMIO_API/api/ensureAAForEOA?eoa=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
            )
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 30000
            conn.readTimeout = 120000
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.use { it.bufferedReader().readText() }
                .orEmpty()
            conn.disconnect()
            val root = parseBeamioApiJsonObject(resp, code)
            if (root == null || code !in 200..299) {
                Log.w("Topup", "ensureAaForEoaSync failed HTTP=$code")
                return false
            }
            val aa = root.optString("aa")
            val ok = aa.isNotEmpty()
            if (ok) Log.d("Topup", "ensureAaForEoaSync OK aa=$aa")
            else Log.w("Topup", "ensureAaForEoaSync: no aa in JSON body")
            ok
        } catch (e: Exception) {
            Log.e("MainActivity", "ensureAaForEoaSync", e)
            false
        }
    }

    /** @param forPostPayment 若为 true，请求体带 for=postPaymentBalance，便于服务端日志区分扣款后拉取 */
    private fun fetchWalletAssetsSync(wallet: String, forPostPayment: Boolean = false, merchantInfraOnly: Boolean = false): UIDAssets? {
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
                putMerchantInfraParams(merchantInfraOnly)
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
                else remove("admin:$key")
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

    /**
     * 拉取终端 profile 与上层 merchant 胶囊（owner 或上级 admin）。
     * 与 iOS [POSViewModel.refreshHomeProfiles] / Cluster `getCardAdminInfo` 语义对齐：`upperAdmin` 若与 `owner` 相同（直属 owner 的 admin）仍解析该 owner；
     * `upperAdmin` 为空时回退 `owner`（与 iOS `upperAdmin ?? owner` 一致）。首页右侧胶囊**仅当** `search-users` 有结果时展示，无结果则隐藏。
     */
    private fun fetchTerminalProfileSync(wallet: String): Pair<TerminalProfile?, TerminalProfile?> {
        refreshMerchantInfraCardFromDbSync()
        val profile = fetchSearchUsersSync(wallet)
        val root = fetchGetCardAdminInfoJsonSync(wallet)
        val ownerAddr = root?.optString("owner")?.takeIf { it.isNotEmpty() && !it.equals("null", true) }?.trim()
        val upperRaw = root?.optString("upperAdmin")?.takeIf { it.isNotEmpty() && !it.equals("null", true) }?.trim()
        val adminLookupAddr = upperRaw?.takeIf { it.isNotEmpty() } ?: ownerAddr?.takeIf { it.isNotEmpty() }
        val adminProfile = adminLookupAddr?.let { fetchSearchUsersSync(it) }
        syncChargeOwnerChildBurnEligibilityCache(wallet)
        return Pair(profile, adminProfile)
    }

    /**
     * 链上同步：基础设施卡上 adminParent(终端EOA) == owner(card) 时记 true（与服务器 burn 预检一致）。
     * 在拉取上层 admin / 全局 profile 后调用，供 QR Charge 必附离线焚烧签字。
     */
    private fun syncChargeOwnerChildBurnEligibilityCache(wallet: String) {
        try {
            if (!BeamioWeb3Wallet.isInitialized()) return
            val w = wallet.trim()
            if (!looksLikeEthereumAddress(w)) return
            val walletNorm = if (w.startsWith("0x", true)) w else "0x$w"
            val card = infraCardAddress().trim()
            if (!looksLikeEthereumAddress(card)) return
            val ownerHex = jsonRpcEthCall(card, "0x8da5cb5b") ?: run {
                clearChargeOwnerChildBurnCache()
                return
            }
            val ownerAddr = decodeAbiAddressWord(ownerHex) ?: run {
                clearChargeOwnerChildBurnCache()
                return
            }
            val apData = buildAdminParentCalldata(walletNorm)
            if (apData.isEmpty()) return
            val parentHex = jsonRpcEthCall(card, apData) ?: run {
                clearChargeOwnerChildBurnCache()
                return
            }
            val parentAddr = decodeAbiAddressWord(parentHex) ?: run {
                clearChargeOwnerChildBurnCache()
                return
            }
            val zero = "0x0000000000000000000000000000000000000000"
            val parentIsZero = parentAddr.equals(zero, ignoreCase = true)
            val parentIsOwner = ownerAddr.equals(parentAddr, ignoreCase = true)
            val eligible = when {
                parentIsOwner -> true
                parentIsZero -> {
                    val isAd = buildIsAdminCalldata(walletNorm)
                    if (isAd.isEmpty()) false
                    else decodeAbiBoolWord(jsonRpcEthCall(card, isAd) ?: "")
                }
                else -> false
            }
            getSharedPreferences(PREFS_PROFILE_CACHE, Context.MODE_PRIVATE).edit().apply {
                putBoolean(PREF_KEY_CHARGE_OWNER_CHILD_ELIGIBLE, eligible)
                putString(PREF_KEY_CHARGE_OWNER_CHILD_CARD, card.lowercase())
                putString(PREF_KEY_CHARGE_OWNER_CHILD_WALLET, walletNorm.lowercase())
                apply()
            }
            Log.d("ChargeOwnerChildBurn", "cache synced eligible=$eligible card=${card.take(10)}…")
        } catch (e: Exception) {
            Log.w("ChargeOwnerChildBurn", "sync cache failed", e)
        }
    }

    private fun clearChargeOwnerChildBurnCache() {
        try {
            getSharedPreferences(PREFS_PROFILE_CACHE, Context.MODE_PRIVATE).edit()
                .remove(PREF_KEY_CHARGE_OWNER_CHILD_ELIGIBLE)
                .remove(PREF_KEY_CHARGE_OWNER_CHILD_CARD)
                .remove(PREF_KEY_CHARGE_OWNER_CHILD_WALLET)
                .apply()
        } catch (_: Exception) { }
    }

    /** 与当前钱包、当前基础设施卡地址匹配的缓存为 owner 直属时返回 true */
    private fun isCachedChargeOwnerChildBurnEligible(): Boolean {
        if (!BeamioWeb3Wallet.isInitialized()) return false
        val wallet = BeamioWeb3Wallet.getAddress().trim()
        val prefs = getSharedPreferences(PREFS_PROFILE_CACHE, Context.MODE_PRIVATE)
        val w = prefs.getString(PREF_KEY_CHARGE_OWNER_CHILD_WALLET, null)?.trim()?.lowercase() ?: return false
        val c = prefs.getString(PREF_KEY_CHARGE_OWNER_CHILD_CARD, null)?.trim()?.lowercase() ?: return false
        if (w != wallet.lowercase()) return false
        if (c != infraCardAddress().trim().lowercase()) return false
        return prefs.getBoolean(PREF_KEY_CHARGE_OWNER_CHILD_ELIGIBLE, false)
    }

    /**
     * QR / NFC Charge JSON：附 chargeOwnerChildBurn。缓存标明 owner 直属时强制再拉一次链上缓存并构建签字，减少漏附焚烧。
     */
    private fun attachChargeOwnerChildBurnToRequestBody(body: org.json.JSONObject, containerPayload: org.json.JSONObject) {
        try {
            val posEoa = BeamioWeb3Wallet.getAddress()
            var burn = buildChargeOwnerChildBurnJsonIfNeeded(containerPayload, posEoa)
            if (burn == null && isCachedChargeOwnerChildBurnEligible()) {
                syncChargeOwnerChildBurnEligibilityCache(posEoa)
                burn = buildChargeOwnerChildBurnJsonIfNeeded(containerPayload, posEoa)
                if (burn == null) {
                    Log.w(
                        "ChargeOwnerChildBurn",
                        "Cached owner-direct but burn payload not built (check items=single infra ERC1155)"
                    )
                }
            }
            burn?.let { body.put("chargeOwnerChildBurn", it) }
        } catch (_: Exception) { /* optional attachment */ }
    }

    /** GET /api/getCardAdminInfo 完整 JSON（含 admins / metadatas，与 biz 部署的 tierRouting 同源） */
    private fun fetchGetCardAdminInfoJsonSync(wallet: String): org.json.JSONObject? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/getCardAdminInfo?cardAddress=${java.net.URLEncoder.encode(infraCardAddress(), "UTF-8")}&wallet=${java.net.URLEncoder.encode(wallet, "UTF-8")}")
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

    /**
     * 解析本终端 EOA 在 `getAdminListWithMetadata` 中对应条目的 metadata，读取 `tierRoutingDiscounts`（与 biz Sign & Deploy 一致）。
     * 与卡片 URI / DB 里的 `tiers[]` 不同：`tierRoutingDiscounts` 必须写在**链上 admin metadata**（或继承自上级的同字段）。
     * @return Pair(taxPercent, discountSummary)；仅网络失败时返回 null（不覆盖 UI 旧值）
     */
    private fun fetchInfraRoutingForTerminalWalletSync(wallet: String): Pair<Double, String>? {
        refreshMerchantInfraCardFromDbSync()
        val root = fetchGetCardAdminInfoJsonSync(wallet) ?: return null
        val admins = root.optJSONArray("admins") ?: return null
        val metadatas = root.optJSONArray("metadatas") ?: return null
        val parents = root.optJSONArray("parents")
        val wNorm = wallet.trim().lowercase()
        var idx = -1
        for (i in 0 until admins.length()) {
            if (admins.optString(i, "").trim().lowercase() == wNorm) {
                idx = i
                break
            }
        }
        if (idx < 0) return Pair(0.0, "Not on admin list")

        fun adminIndexForAddress(addr: String): Int {
            val a = addr.trim().lowercase()
            if (a.isEmpty() || a == "0x0000000000000000000000000000000000000000") return -1
            for (i in 0 until admins.length()) {
                if (admins.optString(i, "").trim().lowercase() == a) return i
            }
            return -1
        }

        fun parseAtRow(rowIdx: Int): Pair<Double, String>? {
            if (rowIdx < 0 || rowIdx >= metadatas.length()) return null
            val metaStr = metadatas.optString(rowIdx, "").trim()
            if (metaStr.isEmpty()) return null
            return parseTierRoutingDiscountsFromTerminalMetadata(metaStr, infraCardAddress())
        }

        // 1) 本终端一行
        parseAtRow(idx)?.let { return it }

        // 2) 沿 parents 向上查找（上级已 Deploy routing、子级 metadata 仍为空时）
        var walk = idx
        repeat(8) {
            val pRaw = parents?.optString(walk, "").orEmpty().trim()
            val pIdx = adminIndexForAddress(pRaw)
            if (pIdx < 0) return@repeat
            parseAtRow(pIdx)?.let { return it }
            walk = pIdx
        }

        // 3) DB /api/cardMetadata：优先 tierRoutingDiscounts；否则用 metadata.tiers[].description 中的 N% 组装折扣（税率记 0）
        fetchTierRoutingFromCardMetadataApi()?.let { return it }

        val hadAnyMeta = metadatas.optString(idx, "").trim().isNotEmpty()
        return Pair(0.0, if (hadAnyMeta) "No tier routing block" else "No routing metadata")
    }

    /** 从会员档文案中解析首个百分比，如 `7.5% discount` → 7.5（两位小数）。 */
    private fun parseDiscountPercentFromMembershipTierDescription(text: String): Double? {
        if (text.isBlank()) return null
        val m = Regex("""(\d+(?:\.\d+)?)\s*%""").find(text) ?: return null
        val v = m.groupValues[1].toDoubleOrNull() ?: return null
        return roundDiscountPercent2(v).takeIf { it > 0 }
    }

    /**
     * 使用 `/api/cardMetadata` 返回的 `metadata.tiers`（非 `tierRoutingDiscounts`）按链上 `index` 排序组装 Home 折扣摘要。
     * 仅展示用；税率由调用方置 0（与 Sign & Deploy 的 tax 无关）。
     */
    private fun parseMembershipTierDiscountSummaryFromMetadata(meta: org.json.JSONObject): String? {
        val tiersArr = meta.optJSONArray("tiers") ?: return null
        if (tiersArr.length() == 0) return null
        data class IdxPct(val chainIndex: Int, val pct: Double)
        val rows = mutableListOf<IdxPct>()
        for (i in 0 until tiersArr.length()) {
            val row = tiersArr.optJSONObject(i) ?: continue
            val chainIndex = when (val v = row.opt("index")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: i
                else -> i
            }
            val desc = row.optString("description", "").trim()
            val pct = parseDiscountPercentFromMembershipTierDescription(desc) ?: continue
            rows.add(IdxPct(chainIndex, pct))
        }
        if (rows.isEmpty()) return null
        return rows.sortedBy { it.chainIndex }.joinToString(" · ") { "${formatTierDiscountPercentForUi(it.pct)}%" }
    }

    /**
     * GET /api/cardMetadata（基础设施卡地址）：先 `tierRoutingDiscounts`；失败则 `metadata.tiers` + description 折扣；仍无则 null。
     */
    private fun fetchTierRoutingFromCardMetadataApi(): Pair<Double, String>? {
        return try {
            val enc = java.net.URLEncoder.encode(infraCardAddress(), "UTF-8")
            val url = java.net.URL("$BEAMIO_API/api/cardMetadata?cardAddress=$enc")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() }.orEmpty()
            conn.disconnect()
            if (code !in 200..299) return null
            val resp = org.json.JSONObject(body)
            val meta = resp.optJSONObject("metadata") ?: return null
            parseTierRoutingDiscountsFromTerminalMetadata(meta.toString(), infraCardAddress())
                ?: parseMembershipTierDiscountSummaryFromMetadata(meta)?.let { summary -> Pair(0.0, summary) }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTierRoutingDiscountsFromTerminalMetadata(metaJson: String, expectedInfrastructureCard: String): Pair<Double, String>? {
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
            val discParts = ArrayList<String>(tiers.length())
            for (i in 0 until tiers.length()) {
                val row = tiers.optJSONObject(i) ?: continue
                val d = parseDiscountPercentJsonValue(row.opt("discountPercent")) ?: continue
                discParts.add(formatTierDiscountPercentForUi(d))
            }
            val discLabel = if (discParts.isEmpty()) "—" else discParts.joinToString(" · ") { "$it%" }
            Pair(tax, discLabel)
        } catch (_: Exception) {
            null
        }
    }

    private data class TierRoutingDetails(val taxPercent: Double, val discountByTierKey: Map<String, Double>)

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

            val map = mutableMapOf<String, Double>()
            val tiers = tr.optJSONArray("tiers") ?: org.json.JSONArray()
            for (i in 0 until tiers.length()) {
                val row = tiers.optJSONObject(i) ?: continue
                val d = parseDiscountPercentJsonValue(row.opt("discountPercent")) ?: continue
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
        val metaStr = resolveAdminMetadataJsonForTerminalWithFallback(wallet) ?: return null
        return parseTierRoutingDetailsFromTerminalMetadata(metaStr, infraCardAddress())
            ?: fetchTierRoutingDetailsFromCardMetadataApi()
    }

    /** 终端自身 → 沿 parents 向上 → 与 [fetchInfraRoutingForTerminalWalletSync] 同源 */
    private fun resolveAdminMetadataJsonForTerminalWithFallback(wallet: String): String? {
        val root = fetchGetCardAdminInfoJsonSync(wallet) ?: return null
        val admins = root.optJSONArray("admins") ?: return null
        val metadatas = root.optJSONArray("metadatas") ?: return null
        val parents = root.optJSONArray("parents")
        val wNorm = wallet.trim().lowercase()
        var idx = -1
        for (i in 0 until admins.length()) {
            if (admins.optString(i, "").trim().lowercase() == wNorm) {
                idx = i
                break
            }
        }
        if (idx < 0) return null

        fun adminIndexForAddress(addr: String): Int {
            val a = addr.trim().lowercase()
            if (a.isEmpty() || a == "0x0000000000000000000000000000000000000000") return -1
            for (i in 0 until admins.length()) {
                if (admins.optString(i, "").trim().lowercase() == a) return i
            }
            return -1
        }

        fun rowHasTierRouting(rowIdx: Int): Boolean {
            if (rowIdx < 0 || rowIdx >= metadatas.length()) return false
            val s = metadatas.optString(rowIdx, "").trim()
            if (s.isEmpty()) return false
            return parseTierRoutingDiscountsFromTerminalMetadata(s, infraCardAddress()) != null
        }

        var walk = idx
        repeat(8) {
            if (rowHasTierRouting(walk)) return metadatas.optString(walk, "").trim()
            val pRaw = parents?.optString(walk, "").orEmpty().trim()
            val pIdx = adminIndexForAddress(pRaw)
            if (pIdx < 0) return@repeat
            walk = pIdx
        }
        return metadatas.optString(idx, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun fetchTierRoutingDetailsFromCardMetadataApi(): TierRoutingDetails? {
        return try {
            val enc = java.net.URLEncoder.encode(infraCardAddress(), "UTF-8")
            val url = java.net.URL("$BEAMIO_API/api/cardMetadata?cardAddress=$enc")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() }.orEmpty()
            conn.disconnect()
            if (code !in 200..299) return null
            val resp = org.json.JSONObject(body)
            val meta = resp.optJSONObject("metadata") ?: return null
            parseTierRoutingDetailsFromTerminalMetadata(meta.toString(), infraCardAddress())
        } catch (_: Exception) {
            null
        }
    }

    /** `metadata.tiers[].discountPercent`（biz 与 cardMetadata API）；两位小数，缺省或无法解析时 null */
    private fun parseMetadataTierDiscountPercentField(t: org.json.JSONObject): Double? {
        if (!t.has("discountPercent") || t.isNull("discountPercent")) return null
        return parseDiscountPercentJsonValue(t.opt("discountPercent"))?.takeIf { it > 0 }
    }

    /** 单条 metadata tier 的 Charge 折扣：优先 [MetadataTier.discountPercent]，否则解析 description 内 `N%` */
    private fun discountPercentFromMetadataTier(row: MetadataTier): Double {
        row.discountPercent?.let { return roundDiscountPercent2(it.coerceIn(0.0, 100.0)) }
        return parseDiscountPercentFromMembershipTierDescription(row.description ?: "") ?: 0.0
    }

    /**
     * Charge 档位折扣：客户主卡（`cards[0]`）的 `metadata.tiers` 中与主会员 NFT 对应的行（与 biz 表格同源）；
     * 若无 API tiers 或未匹配到行，回退终端 `tierRoutingDiscounts`（[pickTierDiscountPercentFromAssets]）。
     */
    private fun pickChargeTierDiscountPercentForPaymentCard(
        paymentCard: CardItem?,
        assets: UIDAssets,
        tierKeyToDiscountFallback: Map<String, Double>
    ): Double {
        val addr = paymentCard?.cardAddress?.trim().orEmpty()
        if (addr.isNotEmpty() && paymentCard != null) {
            val tiers = fetchCardMetadataTiers(addr)
            val fromApi = cardMetadataTierFromApiCache[addr.lowercase()] == true
            val row = selectMetadataTierForPrimaryMembership(paymentCard, tiers)
            if (fromApi && row != null) {
                return roundDiscountPercent2(discountPercentFromMetadataTier(row).coerceIn(0.0, 100.0))
            }
        }
        return pickTierDiscountPercentFromAssets(assets, tierKeyToDiscountFallback)
    }

    /** 按客户 NFT `tier` 字段匹配 routing 表（取最大折扣率，两位小数） */
    private fun pickTierDiscountPercentFromAssets(assets: UIDAssets, tierKeyToDiscount: Map<String, Double>): Double {
        if (tierKeyToDiscount.isEmpty()) return 0.0
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
        var best = 0.0
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
        return roundDiscountPercent2(best.coerceIn(0.0, 100.0))
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

    /**
     * Charge / Top-up 成功后 +6s：与首页首次进入、iOS [POSViewModel.refreshHomeProfiles] 同类拉取（profile、钱包资产、链上 KPI、路由摘要）。
     * 与 [scheduleCardStatsRefetchUntilChanged] 互补：后者轮询 KPI 直至变化；本方法额外刷新 getCardAdminInfo / search-users 等。
     */
    private fun scheduleElasticHomeDataPullAfterTxSuccess() {
        Thread {
            try {
                Thread.sleep(6000)
            } catch (_: InterruptedException) {
                return@Thread
            }
            val wallet = BeamioWeb3Wallet.getAddress()?.trim().orEmpty()
            if (wallet.isEmpty()) return@Thread
            try {
                refreshMerchantInfraCardFromDbSync()
                val (profile, admin) = fetchTerminalProfileSync(wallet)
                val assets = fetchWalletAssetsSync(wallet, forPostPayment = false, merchantInfraOnly = false)
                val (charge, topUp) = fetchCardStatsSync(wallet)
                val routing = fetchInfraRoutingForTerminalWalletSync(wallet)
                runOnUiThread {
                    if (profile != null) terminalProfile = profile
                    terminalAdminProfile = admin
                    saveProfileCache(wallet, profile, admin)
                    if (assets != null && assets.ok) {
                        hasAAAccount = !assets.aaAddress.isNullOrEmpty()
                    } else {
                        hasAAAccount = true
                    }
                    if (charge != null) cardChargeAmount = charge
                    if (topUp != null) cardTopUpAmount = topUp
                    if (routing != null) {
                        dashboardInfraTaxPercent = routing.first
                        dashboardInfraDiscountSummary = routing.second
                    }
                }
            } catch (_: Exception) {
                // best-effort
            }
        }.start()
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
            val reqBody = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"${infraCardAddress()}","data":"$data"},"latest"],"id":1}"""
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

    /** eth_call：成功返回 `result` 十六进制（含 0x）；错误 / revert / 空结果返回 null */
    private fun jsonRpcEthCall(toAddress: String, dataHex: String): String? {
        val to = run {
            val t = toAddress.trim()
            if (t.startsWith("0x", ignoreCase = true)) t.lowercase() else "0x${t.lowercase()}"
        }
        if (!looksLikeEthereumAddress(to)) return null
        val data = if (dataHex.startsWith("0x", ignoreCase = true)) dataHex.lowercase() else "0x$dataHex".lowercase()
        return try {
            val reqBody = """{"jsonrpc":"2.0","method":"eth_call","params":[{"to":"$to","data":"$data"},"latest"],"id":1}"""
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
            if (code !in 200..299) return null
            val root = org.json.JSONObject(json)
            if (root.optJSONObject("error") != null) return null
            val result = root.optString("result", "")
            if (result.isEmpty() || result == "0x") null else result
        } catch (_: Exception) {
            null
        }
    }

    /** 解码 eth_call 返回的单 address（32 字节右对齐） */
    private fun decodeAbiAddressWord(hex: String): String? {
        val raw = hex.trim().removePrefix("0x")
        if (raw.length < 64) return null
        return "0x" + raw.substring(raw.length - 40)
    }

    /** adminParent(address) selector + 参数 */
    private fun buildAdminParentCalldata(adminEoa: String): String {
        val trimmed = adminEoa.trim().removePrefix("0x").lowercase()
        if (trimmed.length != 40 || !trimmed.all { it in '0'..'9' || it in 'a'..'f' }) return ""
        return "0x49dd0900" + trimmed.padStart(64, '0')
    }

    /** isAdmin(address) — selector 0x24d7806c */
    private fun buildIsAdminCalldata(adminEoa: String): String {
        val trimmed = adminEoa.trim().removePrefix("0x").lowercase()
        if (trimmed.length != 40 || !trimmed.all { it in '0'..'9' || it in 'a'..'f' }) return ""
        return "0x24d7806c" + trimmed.padStart(64, '0')
    }

    /** eth_call 返回单个 bool（32 字节 word） */
    private fun decodeAbiBoolWord(hex: String): Boolean {
        val raw = hex.trim().removePrefix("0x")
        if (raw.length < 64) return false
        val w = raw.substring(raw.length - 64)
        return w.any { it != '0' }
    }

    /** POST /api/burnPointsByAdminPrepare — 返回 cardAddr、data、deadline、nonce */
    private fun postBurnPointsByAdminPrepare(cardAddress: String, targetAa: String, amountDigits: String): org.json.JSONObject? {
        return try {
            val url = java.net.URL("$BEAMIO_API/api/burnPointsByAdminPrepare")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            val body = org.json.JSONObject().apply {
                put("cardAddress", cardAddress)
                put("target", targetAa)
                put("amount", amountDigits)
            }.toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val resp = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            val root = org.json.JSONObject(resp)
            if (code !in 200..299 || root.has("error") && root.optString("error").isNotEmpty()) {
                Log.w("ChargeOwnerChildBurn", "burnPointsByAdminPrepare failed code=$code resp=${resp.take(300)}")
                return null
            }
            root
        } catch (e: Exception) {
            Log.w("ChargeOwnerChildBurn", "burnPointsByAdminPrepare", e)
            null
        }
    }

    /**
     * owner 直属下级 POS Charge：链上 adminParent(payee)==owner 且 container 仅含基础设施卡 ERC1155 点数时，
     * 请求 burn prepare 并由终端钱包签 ExecuteForAdmin，供 Master relay 后焚烧入账 points。
     */
    private fun buildChargeOwnerChildBurnJsonIfNeeded(containerPayload: org.json.JSONObject, payeeEoa: String): org.json.JSONObject? {
        if (!BeamioWeb3Wallet.isInitialized()) return null
        val payeeTrim = payeeEoa.trim()
        if (!looksLikeEthereumAddress(payeeTrim)) return null
        val payeeNorm = if (payeeTrim.startsWith("0x", true)) payeeTrim else "0x$payeeTrim"
        val toAa = containerPayload.optString("to").trim()
        if (!looksLikeEthereumAddress(toAa)) return null
        val items = containerPayload.optJSONArray("items") ?: return null
        val byAsset = mutableMapOf<String, Long>()
        for (i in 0 until items.length()) {
            val it = items.optJSONObject(i) ?: continue
            if (it.optInt("kind") != 1) continue
            val asset = it.optString("asset").trim()
            if (asset.isEmpty()) continue
            val amt = it.optString("amount").toLongOrNull() ?: 0L
            if (amt <= 0L) continue
            val k = asset.lowercase()
            byAsset[k] = (byAsset[k] ?: 0L) + amt
        }
        if (byAsset.size != 1) return null
        val infra = infraCardAddress().trim()
        val onlyKey = byAsset.keys.single()
        if (!onlyKey.equals(infra.lowercase(), ignoreCase = true)) return null
        val pointsSum = byAsset.values.single()
        if (pointsSum <= 0L) return null
        val ownerHex = jsonRpcEthCall(infra, "0x8da5cb5b") ?: return null
        val ownerAddr = decodeAbiAddressWord(ownerHex) ?: return null
        val apData = buildAdminParentCalldata(payeeNorm)
        if (apData.isEmpty()) return null
        val parentHex = jsonRpcEthCall(infra, apData) ?: return null
        val parentAddr = decodeAbiAddressWord(parentHex) ?: return null
        val zero = "0x0000000000000000000000000000000000000000"
        val parentIsZero = parentAddr.equals(zero, ignoreCase = true)
        val parentIsOwner = ownerAddr.equals(parentAddr, ignoreCase = true)
        if (!parentIsOwner) {
            if (!parentIsZero) return null
            val isAd = buildIsAdminCalldata(payeeNorm)
            if (isAd.isEmpty()) return null
            val isHex = jsonRpcEthCall(infra, isAd) ?: return null
            if (!decodeAbiBoolWord(isHex)) return null
        }
        val prep = postBurnPointsByAdminPrepare(infra, toAa, pointsSum.toString()) ?: return null
        val cardAddrPrep = prep.optString("cardAddr", "").ifEmpty { infra }
        val data = prep.optString("data", "")
        val nonce = prep.optString("nonce", "")
        val deadline = when (val d = prep.opt("deadline")) {
            is Number -> d.toLong()
            else -> prep.optString("deadline").toLongOrNull() ?: 0L
        }
        if (data.isEmpty() || nonce.isEmpty() || deadline <= 0L) return null
        val adminSig = BeamioWeb3Wallet.signExecuteForAdmin(cardAddrPrep, data, deadline, nonce)
        return org.json.JSONObject().apply {
            put("cardAddr", cardAddrPrep)
            put("data", data)
            put("deadline", deadline)
            put("nonce", nonce)
            put("adminSignature", adminSig)
        }
    }

    /** `tiers(uint256 index)` calldata — 返回 (minUsdc6, attr, tierExpirySeconds) 各 32 字节 */
    private fun buildTiersGetterCalldata(index: Long): String {
        val idx = index.coerceAtLeast(0L)
        val idxHex = java.lang.Long.toHexString(idx).lowercase().padStart(64, '0')
        return SEL_TIERS_UINT256 + idxHex
    }

    private fun decodeTiersPublicGetterResult(hex: String): Triple<Long, Long, Long>? {
        val raw = hex.removePrefix("0x")
        if (raw.length < 192) return null
        fun wordToLong(w: String): Long {
            val bi = java.math.BigInteger(w, 16)
            val maxBi = java.math.BigInteger.valueOf(Long.MAX_VALUE)
            return if (bi > maxBi) Long.MAX_VALUE else bi.toLong()
        }
        val w0 = raw.substring(0, 64)
        val w1 = raw.substring(64, 128)
        val w2 = raw.substring(128, 192)
        return Triple(wordToLong(w0), wordToLong(w1), wordToLong(w2))
    }

    /**
     * 对任意 BeamioUserCard 地址链上读取 `tiers(i)`（无独立 tiersLength view，索引递增直至 revert）。
     * 转成 [MetadataTier] 供 [enrichCardTierFromMetadata] 与 API metadata 相同的消费路径；不含 JSON 里的 name/image 时仅用占位英文说明。
     */
    private fun fetchChainTiersAsMetadataTiersSync(cardAddress: String): List<MetadataTier> {
        val to = cardAddress.trim()
        if (!looksLikeEthereumAddress(to)) return emptyList()
        val out = mutableListOf<MetadataTier>()
        for (i in 0 until CHAIN_TIERS_MAX_PROBE) {
            val hex = jsonRpcEthCall(to, buildTiersGetterCalldata(i.toLong())) ?: break
            val triple = decodeTiersPublicGetterResult(hex) ?: break
            val (minUsdc6, attr, expSec) = triple
            val desc = buildString {
                append("attr=").append(attr)
                append(", expirySeconds=")
                append(if (expSec == 0L) "global" else expSec.toString())
            }
            out.add(
                MetadataTier(
                    minUsdc6 = minUsdc6.coerceAtLeast(0L),
                    name = "Tier $i",
                    description = desc,
                    image = null,
                    backgroundColor = null,
                    chainTierIndex = i
                )
            )
        }
        if (out.isNotEmpty()) {
            Log.d("ChainTiers", "card=${to.lowercase()} chainTierCount=${out.size}")
        }
        return out.sortedBy { it.minUsdc6 }
    }

    private fun fetchUidAssets(uid: String, sunParams: SunParams? = null, merchantInfraOnly: Boolean = false) {
        Thread {
            try {
                refreshMerchantInfraCardFromDbSync()
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
                    putMerchantInfraParams(merchantInfraOnly)
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
            val chainIdx = when (val v = t.opt("index")) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull()
                else -> null
            }
            val discPct = parseMetadataTierDiscountPercentField(t)
            parsed.add(
                MetadataTier(
                    minUsdc6 = minUsdc6.coerceAtLeast(0L),
                    name = t.optString("name").takeIf { it.isNotBlank() },
                    description = t.optString("description").takeIf { it.isNotBlank() },
                    image = t.optString("image").takeIf { it.isNotBlank() },
                    backgroundColor = normalizeHexColorString(t.optString("backgroundColor")),
                    chainTierIndex = chainIdx,
                    discountPercent = discPct
                )
            )
        }
        return parsed.sortedBy { it.minUsdc6 }
    }

    /** Chain tier indices to match `MetadataTier.chainTierIndex` (API nft.tier / attribute). */
    private fun chainTierIndexCandidatesFromNft(nft: NftItem): List<Int> {
        val ordered = LinkedHashSet<Int>()
        val tierRaw = nft.tier.trim()
        tierRaw.toIntOrNull()?.let { ordered.add(it) }
        Regex("""(?i)chain-tier-(\d+)""").find(tierRaw)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { ordered.add(it) }
        nft.attribute.trim().toIntOrNull()?.let { ordered.add(it) }
        return ordered.toList()
    }

    /**
     * Pick metadata tier row for the **primary** membership NFT (`primaryMemberTokenId` or legacy max tokenId).
     * Do not use points balance for this — tier display must match on-chain NFT tier, not voucher balance vs minUsdc6.
     */
    private fun selectMetadataTierForPrimaryMembership(card: CardItem, tiers: List<MetadataTier>): MetadataTier? {
        if (tiers.isEmpty()) return null
        val primaryTid = card.primaryMemberTokenId?.toLongOrNull()?.takeIf { it > 0L }
            ?: card.nfts
                .asSequence()
                .filter { it.tokenId.toLongOrNull()?.let { id -> id > 0L } == true }
                .maxByOrNull { it.tokenId.toLongOrNull() ?: 0L }
                ?.tokenId
                ?.toLongOrNull()
        val primaryNft = primaryTid?.let { tid -> card.nfts.find { it.tokenId.toLongOrNull() == tid } }
            ?: return null
        for (idx in chainTierIndexCandidatesFromNft(primaryNft)) {
            tiers.firstOrNull { it.chainTierIndex == idx }?.let { return it }
        }
        val tierLabel = primaryNft.tier.trim()
        if (tierLabel.isNotEmpty() && tierLabel.toIntOrNull() == null &&
            !Regex("""(?i)chain-tier-\d+""").containsMatchIn(tierLabel)
        ) {
            tiers.firstOrNull { it.name?.equals(tierLabel, ignoreCase = true) == true }?.let { return it }
        }
        return null
    }

    /** Pre-fetch 基础设施卡 metadata 与 tiers（Home 进入时）。缓存全局；topup/charge 成功路径复用缓存。 */
    private fun prefetchInfraCardMetadata() {
        fetchCardMetadataTiers(infraCardAddress())
    }

    private fun fetchCardMetadataTiers(cardAddress: String): List<MetadataTier> {
        val key = cardAddress.lowercase()
        cardMetadataTierCache[key]?.let { return it }
        var apiTiers: List<MetadataTier> = emptyList()
        var fromApiTiers = false
        try {
            val url = java.net.URL("$BEAMIO_API/api/cardMetadata?cardAddress=$cardAddress")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.bufferedReader().readText() } ?: "{}"
            conn.disconnect()
            if (code in 200..299) {
                val root = org.json.JSONObject(body)
                val meta = root.optJSONObject("metadata")
                val tiersLen = meta?.optJSONArray("tiers")?.length() ?: 0
                if (tiersLen > 0) {
                    apiTiers = parseMetadataTiers(meta)
                    fromApiTiers = apiTiers.isNotEmpty()
                }
            }
        } catch (_: Exception) {
        }
        val resolved = apiTiers.takeIf { it.isNotEmpty() }
            ?: fetchChainTiersAsMetadataTiersSync(cardAddress)
        cardMetadataTierFromApiCache[key] = fromApiTiers
        cardMetadataTierCache[key] = resolved
        return resolved
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

    /** 主会员档在 `metadata.tiers` 命中行的背景与图（与 iOS `primaryTierMetadataVisuals` 一致）。 */
    private fun primaryTierMetadataVisuals(card: CardItem, tiers: List<MetadataTier>): Pair<String?, String?> {
        if (tiers.isEmpty()) return Pair(null, null)
        val row = selectMetadataTierForPrimaryMembership(card, tiers) ?: return Pair(null, null)
        val bg = row.backgroundColor?.trim()?.takeIf { it.isNotEmpty() }
        val img = row.image?.trim()?.takeIf { it.isNotEmpty() }
        return Pair(bg, img)
    }

    /**
     * 与 iOS `mergePrimaryTierStyleFromCardMetadata` 一致：GET `/api/cardMetadata` 的 `metadata.tiers` 主档行
     * 覆盖 getUIDAssets 可能仍带的 **陈旧 NFT** `cardBackground` / `cardImage`（档名已升级时仍对齐卡面）。
     * 链上 probe 回填的 tiers 不视为 API metadata，此时 [cardMetadataTierFromApiCache] 为 false，本函数不覆盖。
     */
    private fun mergePrimaryTierStyleFromCardMetadata(card: CardItem): CardItem {
        val key = card.cardAddress.trim().lowercase(Locale.US)
        if (key.isEmpty() || cardMetadataTierFromApiCache[key] != true) return card
        val tiers = fetchCardMetadataTiers(card.cardAddress)
        val (bg, img) = primaryTierMetadataVisuals(card, tiers)
        var bgOut = card.cardBackground
        var imgOut = card.cardImage
        if (!bg.isNullOrBlank()) bgOut = bg
        if (!img.isNullOrBlank()) imgOut = img
        if (bgOut == card.cardBackground && imgOut == card.cardImage) return card
        return card.copy(cardBackground = bgOut, cardImage = imgOut)
    }

    private fun enrichCardTierFromMetadata(card: CardItem): CardItem {
        val tiers = fetchCardMetadataTiers(card.cardAddress)
        val fromPrimary = selectMetadataTierForPrimaryMembership(card, tiers)
        val points6 = card.points6.toLongOrNull() ?: 0L
        val fromPoints = if (tiers.isNotEmpty()) tiers.lastOrNull { it.minUsdc6 <= points6 } else null
        val selected = fromPrimary ?: fromPoints

        val isInfraCard = card.cardAddress.equals(infraCardAddress(), ignoreCase = true)
        var cardImage = card.cardImage
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

        val tierRowForDiscount = fromPrimary ?: selected
        val discPct = tierRowForDiscount?.let { discountPercentFromMetadataTier(it) }?.takeIf { it > 0.0 }

        val tierNeedsFill = card.tierName.isNullOrBlank() || card.tierDescription.isNullOrBlank()
        val styleNeedsFill = card.cardBackground.isNullOrBlank() || cardImage.isNullOrBlank()
        val mergedDisc = discPct ?: card.tierDiscountPercent?.takeIf { it > 0.0 }
        if (fromPrimary == null && !tierNeedsFill && !styleNeedsFill) {
            val discOnly = if (mergedDisc != null) card.copy(tierDiscountPercent = mergedDisc) else card
            return mergePrimaryTierStyleFromCardMetadata(discOnly)
        }

        // getUIDAssets / getWalletAssets 卡行的 tierName、tierDescription 与链上主档一致；勿用 cardMetadata 覆盖已有文案
        // （否则 metadata 里整段营销 name 会与 API 的「档名 + 描述」拼错，例如单独一行 "Silver 5% discount"）。
        val tierNameOut = if (!card.tierName.isNullOrBlank()) card.tierName else fromPrimary?.name ?: selected?.name
        val tierDescOut = if (!card.tierDescription.isNullOrBlank()) card.tierDescription else fromPrimary?.description ?: selected?.description

        return mergePrimaryTierStyleFromCardMetadata(
            card.copy(
                tierName = tierNameOut,
                tierDescription = tierDescOut,
                cardImage = cardImage ?: fromPrimary?.image ?: selected?.image,
                cardBackground = card.cardBackground ?: fromPrimary?.backgroundColor ?: selected?.backgroundColor,
                tierDiscountPercent = mergedDisc
            )
        )
    }

    /** API 可能返回 `points` / `points6` 为 JSON 数字；`optString` 在部分类型下不可靠，与 iOS `JSONSerialization` 数值解码对齐。 */
    private fun org.json.JSONObject.optBeamioAmountString(key: String, default: String = "0"): String {
        if (!has(key) || isNull(key)) return default
        return try {
            when (val v = get(key)) {
                is String -> v.trim().ifEmpty { default }
                is Number -> v.toString()
                else -> default
            }
        } catch (_: Exception) {
            default
        }
    }

    private fun org.json.JSONObject.optBeamioAmountStringIfPresent(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optBeamioAmountString(key, "0")
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
                        points = c.optBeamioAmountString("points", "0"),
                        points6 = c.optBeamioAmountString("points6", "0"),
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
                        cardName = if (legacyAddr.equals(infraCardAddress(), ignoreCase = true)) "CCSA CARD" else "Card",
                        cardType = if (legacyAddr.equals(infraCardAddress(), ignoreCase = true)) "ccsa" else "infrastructure",
                        points = root.optBeamioAmountString("points", "0"),
                        points6 = root.optBeamioAmountString("points6", "0"),
                        cardCurrency = root.optString("cardCurrency", "CAD"),
                        /** Legacy 响应无 `cards[]` 时 NFT 在根级 `nfts`，必须并入单卡否则 Member NO / 主档逻辑为空 */
                        nfts = nfts,
                        cardBackground = null,
                        cardImage = null,
                        tierName = null,
                        tierDescription = null,
                        primaryMemberTokenId = root.optString("primaryMemberTokenId").takeIf { it.isNotEmpty() }
                    )).map { enrichCardTierFromMetadata(it) }
                } else null
            }
            val unitPriceUSDC6 = root.optString("unitPriceUSDC6").takeIf { it.isNotEmpty() }
            val beamioUserCard = root.optString("beamioUserCard").takeIf { it.isNotEmpty() }
            val posLastTopupAt = root.optString("posLastTopupAt").takeIf { it.isNotEmpty() }
            val posLastTopupUsdcE6 = root.optBeamioAmountStringIfPresent("posLastTopupUsdcE6")
            val posLastTopupPointsE6 = root.optBeamioAmountStringIfPresent("posLastTopupPointsE6")
            val beamioTagVal = root.optString("beamioTag").takeIf { it.isNotEmpty() }
                ?: root.optString("accountName").takeIf { it.isNotEmpty() }
                ?: root.optString("username").takeIf { it.isNotEmpty() }
            val beamioTagNormalized = beamioTagVal?.trim()?.removePrefix("@")?.trim()?.takeIf { it.isNotEmpty() }
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
                    primaryMemberTokenId = root.optString("primaryMemberTokenId").takeIf { it.isNotEmpty() },
                    beamioTag = beamioTagNormalized,
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
                    error = root.optString("error").takeIf { it.isNotEmpty() },
                    posLastTopupAt = posLastTopupAt,
                    posLastTopupUsdcE6 = posLastTopupUsdcE6,
                    posLastTopupPointsE6 = posLastTopupPointsE6
                )
            } else {
                val legacyCardAddr = root.optString("cardAddress").takeIf { it.isNotEmpty() }
                val isDeprecatedLegacy = legacyCardAddr?.equals(DEPRECATED_CARD_ADDRESS, ignoreCase = true) == true
                UIDAssets(
                    ok = root.optBoolean("ok", false),
                    address = root.optString("address").takeIf { it.isNotEmpty() },
                    aaAddress = root.optString("aaAddress").takeIf { it.isNotEmpty() },
                    primaryMemberTokenId = root.optString("primaryMemberTokenId").takeIf { it.isNotEmpty() },
                    beamioTag = beamioTagNormalized,
                    uid = uidVal,
                    tagIdHex = tagIdHexVal,
                    counterHex = counterHexVal,
                    counter = counterVal,
                    cardAddress = if (isDeprecatedLegacy) null else legacyCardAddr,
                    points = if (isDeprecatedLegacy) null else root.optBeamioAmountStringIfPresent("points"),
                    points6 = if (isDeprecatedLegacy) null else root.optBeamioAmountStringIfPresent("points6"),
                    usdcBalance = root.optString("usdcBalance").takeIf { it.isNotEmpty() },
                    cardCurrency = if (isDeprecatedLegacy) null else root.optString("cardCurrency").takeIf { it.isNotEmpty() },
                    nfts = if (isDeprecatedLegacy) null else nfts.takeIf { it.isNotEmpty() },
                    cards = null,
                    unitPriceUSDC6 = unitPriceUSDC6,
                    beamioUserCard = beamioUserCard,
                    error = root.optString("error").takeIf { it.isNotEmpty() },
                    posLastTopupAt = posLastTopupAt,
                    posLastTopupUsdcE6 = posLastTopupUsdcE6,
                    posLastTopupPointsE6 = posLastTopupPointsE6
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
            fetchUidAssets(sunParams?.uid ?: uid, sunParams, merchantInfraOnly = false)
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
    /** 与 Balance Loaded 一致：Hero 主名优先 API beamioTag */
    customerBeamioTag: String? = null,
    memberNo: String?,
    cardBackground: String?,
    cardImage: String?,
    tierName: String?,
    tierDescription: String?,
    passCardSnapshot: CardItem? = null,
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
                customerBeamioTag = customerBeamioTag,
                memberNo = memberNo,
                cardBackground = cardBackground,
                cardImage = cardImage,
                tierName = tierName,
                tierDescription = tierDescription,
                passCard = passCardSnapshot,
                settlementViaQr = settlementViaQr,
                onBack = onBack,
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
    customerBeamioTag: String? = null,
    memberNo: String?,
    cardBackground: String?,
    cardImage: String?,
    tierName: String?,
    tierDescription: String?,
    passCard: CardItem? = null,
    settlementViaQr: Boolean = false,
    onBack: () -> Unit,
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
    val heroMemberTitle = passHeroMemberDisplayNameLine(
        customerBeamioTag,
        address,
        passCard,
        passCard?.cardName,
    )
    val heroMemberNo = memberNoFromCardItem(passCard).ifEmpty { displayMemberNo }
    val (heroBalPre, heroBalAmt, heroBalSuf) = if (postBalanceNum != null) {
        formatBalanceWithCurrencyProtocol(postBalanceNum, currency)
    } else {
        Triple("", "—", "")
    }
    val heroProgram = passCard?.let { balanceDetailsCardNameLine(it) } ?: "—"
    val isFirstTopUp = memberNo.isNullOrBlank()
    val topBarTitle = if (isFirstTopUp) "Card Minted" else "Top-Up Complete"

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BalanceDetailsSurface)
        ) {
            BalanceDetailsTopBar(onBack = onBack, title = topBarTitle)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
        // Success icon + amount (title only in BalanceDetailsTopBar)
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

        // Pass Hero（与 Balance Details 同一标准组件）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
        ) {
            StandardMemberPassHeroCard(
                memberDisplayName = heroMemberTitle,
                memberNo = heroMemberNo,
                tierDisplayName = passCard?.tierName?.trim()?.takeIf { it.isNotEmpty() }
                    ?: tierName?.trim()?.takeIf { it.isNotEmpty() },
                tierDiscountPercent = passCard?.tierDiscountPercent,
                programCardDisplayName = heroProgram,
                tierCardBackgroundHex = passCard?.cardBackground ?: cardBackground,
                cardMetadataImageUrl = passCard?.cardImage?.takeIf { it.isNotBlank() } ?: cardImage?.takeIf { it.isNotBlank() },
                balancePrefix = heroBalPre,
                balanceAmount = heroBalAmt,
                balanceSuffix = heroBalSuf,
                modifier = Modifier.fillMaxWidth()
            )

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
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

        // Print receipt
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
        }
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

/** metadata / `tierRoutingDiscounts` 百分比 0–100，统一四舍五入到两位小数（与 biz `discountPercent` 对齐）。 */
private fun roundDiscountPercent2(raw: Double): Double =
    kotlin.math.round(raw.coerceIn(0.0, 100.0) * 100.0) / 100.0

private fun formatTierDiscountPercentForUi(percent: Double): String =
    String.format(Locale.US, "%.2f", roundDiscountPercent2(percent))

/** JSON `discountPercent` 字段：Number / 可解析 String → 两位小数百分比。 */
private fun parseDiscountPercentJsonValue(value: Any?): Double? {
    return try {
        val raw = when (value) {
            is Number -> value.toDouble()
            is String -> value.trim().replace(",", ".").toDoubleOrNull() ?: return null
            else -> return null
        }
        if (raw.isNaN() || raw < 0) return null
        roundDiscountPercent2(raw.coerceAtMost(100.0))
    } catch (_: Exception) {
        null
    }
}

/**
 * WCAG 2.1 相对亮度 L（sRGB），范围约 0–1。
 * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
 */
private fun colorRelativeLuminance(c: Color): Double {
    fun lin(x: Float): Double {
        val xs = x.toDouble()
        return if (xs <= 0.03928f) xs / 12.92 else java.lang.Math.pow((xs + 0.055) / 1.055, 2.4)
    }
    val r = lin(c.red)
    val g = lin(c.green)
    val b = lin(c.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * WCAG 2.1 对比度： (L_light + 0.05) / (L_dark + 0.05)，L 为 [colorRelativeLuminance]。
 * https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio
 */
private fun wcagContrastRatio(luminanceA: Double, luminanceB: Double): Double {
    val light = maxOf(luminanceA, luminanceB)
    val dark = minOf(luminanceA, luminanceB)
    return (light + 0.05) / (dark + 0.05)
}

private const val WCAG_LUMINANCE_WHITE = 1.0
private const val WCAG_LUMINANCE_BLACK = 0.0

/**
 * 在纯白字与纯黑字之间，按 WCAG 对比度选较优者。
 *
 * @return `true` 表示应采用深色前景（本卡用近黑 #0F172A），`false` 表示浅色前景（白）。
 */
private fun useDarkForegroundPerWcag(backgroundLuminance: Double): Boolean {
    val lb = backgroundLuminance.coerceIn(0.0, 1.0)
    val contrastWithWhiteText = wcagContrastRatio(WCAG_LUMINANCE_WHITE, lb)
    val contrastWithBlackText = wcagContrastRatio(lb, WCAG_LUMINANCE_BLACK)
    return contrastWithBlackText > contrastWithWhiteText
}

/** 与 [StandardMemberPassHeroCard] 一致：`linearGradient`(0,0)→(w,h) 上参数 t∈[0,1] 处的 sRGB 插值。 */
private fun gradientColorAlongCardDiagonal(gradientStart: Color, gradientEnd: Color, t: Float): Color {
    val s = t.coerceIn(0f, 1f)
    return Color(
        gradientStart.red + (gradientEnd.red - gradientStart.red) * s,
        gradientStart.green + (gradientEnd.green - gradientStart.green) * s,
        gradientStart.blue + (gradientEnd.blue - gradientStart.blue) * s,
        gradientStart.alpha + (gradientEnd.alpha - gradientStart.alpha) * s,
    )
}

/**
 * 归一化坐标：左上 (0,0)、右下 (1,1)，u=横、v=纵；对角 blend 参数 t = (u·(w/h)² + v) / ((w/h)² + 1)。
 * [aspectWidthOverHeight] 与 Hero `aspectRatio(1.6f)` 一致。
 */
private fun cardDiagonalGradientT(u: Float, v: Float, aspectWidthOverHeight: Float = 1.6f): Float {
    val a2 = aspectWidthOverHeight * aspectWidthOverHeight
    val den = a2 + 1f
    return (u.coerceIn(0f, 1f) * a2 + v.coerceIn(0f, 1f)) / den
}

/**
 * 优先照顾**右侧小字**（tier、discount 等）：在该区域沿对角渐变多点采样背景，对白字/黑字分别求 WCAG 对比度
 * 的**最小值**，选最小值更大的一侧（min-max，保证最弱采样点）。
 */
private fun useDarkForegroundWcagPreferRightSmallTextZone(
    gradientStart: Color,
    gradientEnd: Color,
    aspectWidthOverHeight: Float = 1.6f,
): Boolean {
    val uvSamples = arrayOf(
        Pair(0.93f, 0.07f),
        Pair(0.94f, 0.14f),
        Pair(0.91f, 0.23f),
        Pair(0.89f, 0.32f),
        Pair(0.86f, 0.41f),
    )
    var minCrWhite = Double.MAX_VALUE
    var minCrBlack = Double.MAX_VALUE
    for ((u, v) in uvSamples) {
        val t = cardDiagonalGradientT(u, v, aspectWidthOverHeight)
        val bg = gradientColorAlongCardDiagonal(gradientStart, gradientEnd, t)
        val lb = colorRelativeLuminance(bg)
        minCrWhite = minOf(minCrWhite, wcagContrastRatio(WCAG_LUMINANCE_WHITE, lb))
        minCrBlack = minOf(minCrBlack, wcagContrastRatio(lb, WCAG_LUMINANCE_BLACK))
    }
    if (!minCrWhite.isFinite() || !minCrBlack.isFinite()) {
        return useDarkForegroundPerWcag(colorRelativeLuminance(gradientStart))
    }
    return minCrBlack > minCrWhite
}

private fun rgbToHsv(r: Float, g: Float, b_: Float): Triple<Float, Float, Float> {
    val max = maxOf(r, g, b_)
    val min = minOf(r, g, b_)
    val delta = max - min
    if (delta < 1e-5f) return Triple(0f, 0f, max)
    val hDeg = when {
        abs(max - r) < 1e-5f -> {
            var hh = 60f * ((g - b_) / delta)
            if (hh < 0f) hh += 360f
            hh
        }
        abs(max - g) < 1e-5f -> 60f * ((b_ - r) / delta + 2f)
        else -> 60f * ((r - g) / delta + 4f)
    }
    val h = ((hDeg / 360f) % 1f + 1f) % 1f
    val s = delta / max
    return Triple(h, s, max)
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val hh = (h * 6f % 6f + 6f) % 6f
    val i = floor(hh.toDouble()).toInt().coerceIn(0, 5)
    val f = hh - i
    val p = v * (1f - s)
    val q = v * (1f - f * s)
    val t = v * (1f - (1f - f) * s)
    val (rp, gp, bp) = when (i) {
        0 -> Triple(v, t, p)
        1 -> Triple(q, v, p)
        2 -> Triple(p, v, t)
        3 -> Triple(p, q, v)
        4 -> Triple(t, p, v)
        else -> Triple(v, p, q)
    }
    return Color(rp.coerceIn(0f, 1f), gp.coerceIn(0f, 1f), bp.coerceIn(0f, 1f))
}

/** 与 [start] 同色相：与 [useDarkForegroundPerWcag] 一致——偏深底则终点偏浅，偏浅底则终点偏深。 */
private fun sameFamilyGradientEnd(start: Color): Color {
    val lum = colorRelativeLuminance(start)
    val deepBackground = !useDarkForegroundPerWcag(lum)
    val (ho, s0, v0) = rgbToHsv(start.red, start.green, start.blue)
    return if (deepBackground) {
        val v1 = (v0 + 0.38f).coerceIn(0.52f, 0.97f)
        val s1 = (s0 * 0.9f).coerceIn(0.1f, 1f)
        hsvToColor(ho, s1, v1)
    } else {
        val v1 = (v0 - 0.32f).coerceIn(0.1f, 0.5f)
        val s1 = (s0 * 1.06f).coerceIn(0.12f, 1f)
        hsvToColor(ho, s1, v1)
    }
}

private data class PassHeroPalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val decorativeCircle: Color,
    val avatarBorder: Color,
    val avatarBackdrop: Color,
    val walletIconTint: Color,
)

/** 由 `cardBackground` 解析渐变；前景白/黑由右侧小字区的 WCAG min 对比度约束（相对纯白/纯黑）。 */
private fun passHeroPalette(tierCardBackgroundHex: String?): PassHeroPalette {
    val start = parseHexColor(tierCardBackgroundHex) ?: Color(0xFF1562F0)
    val end = sameFamilyGradientEnd(start)
    val darkForeground = useDarkForegroundWcagPreferRightSmallTextZone(start, end)
    val primary = if (darkForeground) Color(0xFF0F172A) else Color.White
    val secondary = if (darkForeground) Color(0xFF0F172A).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f)
    val tertiary = if (darkForeground) Color(0xFF0F172A).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.78f)
    val deco = if (darkForeground) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.05f)
    val avBorder = if (darkForeground) Color.Black.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.22f)
    val avBack = if (darkForeground) Color.Black.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.08f)
    val wallet = if (darkForeground) Color.Black.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.2f)
    return PassHeroPalette(start, end, primary, secondary, tertiary, deco, avBorder, avBack, wallet)
}

/** `0x` + 40 hex：用于区分链上地址与非地址 uid 等展示场景 */
private fun looksLikeEthereumAddress(value: String): Boolean {
    val t = value.trim()
    if (!t.startsWith("0x", ignoreCase = true) || t.length != 42) return false
    return t.drop(2).all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
}

/**
 * 当 API 未提供 `tierName` 时的回退：用主档 NFT 的链上 `tier` 拼「Tier N」（与 getUIDAssets 卡行占位一致）。
 * 有 `tierName` 时应优先用 [passTierSubtitleForPassCard]（卡级 metadata.tiers 展示名）。
 */
private fun chainTierLabelFromPrimaryNft(card: CardItem): String? {
    val primaryTid = card.primaryMemberTokenId?.trim().orEmpty()
    val primaryNft = when {
        primaryTid.isNotEmpty() ->
            card.nfts.firstOrNull { it.tokenId == primaryTid }
                ?: card.nfts.firstOrNull { it.tokenId.equals(primaryTid, ignoreCase = true) }
        else -> null
    } ?: card.nfts.firstOrNull { (it.tokenId.toLongOrNull() ?: 0L) > 0L }
        ?: return null
    val tierRaw = primaryNft.tier.trim()
    if (tierRaw.isEmpty()) return null
    if (tierRaw.all { it.isDigit() }) return "Tier $tierRaw"
    Regex("""(?i)chain-tier-(\d+)""").find(tierRaw)?.groupValues?.getOrNull(1)?.let { return "Tier $it" }
    return null
}

/** Pass 副标题：优先 API `tierName`（卡级 metadata.tiers 名称）+ `tierDescription`；缺失时再回退链上「Tier N」。 */
private fun passTierSubtitleForPassCard(card: CardItem): String {
    val displayName = card.tierName?.takeIf { it.isNotBlank() }
    val chainTier = chainTierLabelFromPrimaryNft(card)
    val desc = card.tierDescription?.takeIf { it.isNotBlank() && !it.equals("Card", ignoreCase = true) }
    return when {
        displayName != null && desc != null && !desc.equals(displayName, ignoreCase = true) && !desc.startsWith(displayName, ignoreCase = true) ->
            "$displayName · $desc"
        displayName != null -> displayName
        chainTier != null && desc != null && !desc.equals(chainTier, ignoreCase = true) && !desc.startsWith(chainTier, ignoreCase = true) ->
            "$chainTier · $desc"
        chainTier != null -> chainTier
        else ->
            desc
                ?: card.cardType.takeIf { it.isNotBlank() && it.lowercase() != "infrastructure" }?.replaceFirstChar { it.uppercase() }
                ?: ""
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

/** `cardName` 展示行（去 CARD 后缀）；与 Balance Details hero 一致。 */
private fun balanceDetailsCardNameLine(card: CardItem): String {
    val raw = card.cardName.trim()
    if (raw.isEmpty()) return "—"
    return raw.removeSuffix(" CARD").removeSuffix(" Card").trim().ifEmpty { "—" }
}

/**
 * Pass Hero 左上主名：与 [ReadScreen] Balance Loaded 一致 —
 * `beamioTag`（无 @）→ 短钱包地址 → 非泛化的 cardName → `"Member"`。
 */
private fun passHeroMemberDisplayNameLine(
    beamioTag: String?,
    walletAddress: String?,
    passCard: CardItem?,
    cardNameFallback: String?,
): String {
    val tag = beamioTag?.trim()?.removePrefix("@")?.takeIf { it.isNotBlank() }
    if (tag != null) return tag
    val a = walletAddress?.trim()
    if (!a.isNullOrEmpty() && a.startsWith("0x") && a.length >= 10) {
        return if (a.length > 10) "${a.take(6)}…${a.takeLast(4)}" else a
    }
    val nm = (passCard?.cardName ?: cardNameFallback)
        ?.removeSuffix(" CARD")?.removeSuffix(" Card")?.trim()
        ?.takeIf { it.isNotBlank() &&
            !it.equals("Infrastructure card", ignoreCase = true) &&
            !it.equals("Asset Card", ignoreCase = true) }
    return nm ?: "Member"
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
    val balSideFs = if (compact) 11.sp else 14.sp
    val balMainFs = if (compact) 17.sp else 22.sp
    val corner = if (compact) 16.dp else 22.dp
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        val gapHeaderToFooter = if (compact) 12.dp else 16.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padS, top = padT, end = padE, bottom = padB)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                BeamioCardRasterOrSvgImage(
                    model = card.cardImage,
                    contentDescription = card.cardName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.size(imgW, imgH),
                    fallback = {
                        Icon(Icons.Filled.Favorite, null, Modifier.size(iconSz), tint = accentGreen)
                    },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        card.cardName.removeSuffix(" CARD").removeSuffix(" Card"),
                        fontSize = titleFs,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val subtitle = passTierSubtitleForPassCard(card)
                    if (subtitle.isNotBlank() && !subtitle.equals("Card", ignoreCase = true)) {
                        Text(
                            subtitle,
                            fontSize = subFs,
                            color = labelGrey,
                            maxLines = if (compact) 3 else 4,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(gapHeaderToFooter))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        memberNo.ifEmpty { "—" },
                        fontSize = memFs,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        balanceDetailsCardNameLine(card),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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

/** POS Balance Detail：最近 top-up 金额仅用 `posLastTopupPointsE6`，币种与当前卡 `cardCurrency` 一致（Beamio 前缀/后缀协议）。无点数时可显示时间或 —。 */
private fun balanceDetailsLastTopUpFallbackLine(assets: UIDAssets?): String {
    val a = assets ?: return "—"
    val iso = a.posLastTopupAt?.trim().orEmpty()
    if (iso.isNotEmpty()) {
        val t = iso.replace("T", " ").trim()
        return t.take(minOf(16, t.length)).trim()
    }
    return "—"
}

@Composable
private fun BalanceDetailsLastTopUpAmountRow(
    assets: UIDAssets?,
    cardCurrency: String,
) {
    val a = assets
    val p6 = a?.posLastTopupPointsE6?.trim()?.toLongOrNull() ?: 0L
    if (p6 > 0L) {
        val v = p6 / 1_000_000.0
        val (pre, mid, suf) = formatBalanceWithCurrencyProtocol(v, cardCurrency)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
            if (pre.isNotEmpty()) {
                Text(
                    pre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BalanceDetailsOnSurface,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Spacer(Modifier.width(2.dp))
            }
            Text(
                mid,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = BalanceDetailsOnSurface,
                fontFamily = FontFamily.Monospace
            )
            if (suf.isNotEmpty()) {
                Spacer(Modifier.width(2.dp))
                Text(
                    suf.trimStart(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BalanceDetailsOutline,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    } else {
        Text(
            balanceDetailsLastTopUpFallbackLine(a),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BalanceDetailsOnSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}

/** balanceNew.html：Material / surface tokens */
private val BalanceDetailsSurface = Color(0xFFF9F9FE)
private val BalanceDetailsPrimary = Color(0xFF003692)
private val BalanceDetailsSecondary = Color(0xFF0052D2)
private val BalanceDetailsOutline = Color(0xFF737685)
private val BalanceDetailsOnSurface = Color(0xFF1A1C1F)
private val BalanceDetailsSurfaceContainerLow = Color(0xFFF3F3F8)
private val BalanceDetailsSurfaceContainerLowest = Color(0xFFFFFFFF)

@Composable
private fun BalanceDetailsTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Balance Details",
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BACK_BUTTON_TOP_BAR_HEIGHT)
            .background(Color.White.copy(alpha = 0.92f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButtonIcon(
            onClick = onBack,
            modifier = Modifier.padding(start = BACK_BUTTON_START_PADDING)
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BalanceDetailsPrimary,
            letterSpacing = (-0.2).sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )
        if (trailing != null) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = trailing
            )
        } else {
            Spacer(Modifier.width(52.dp))
        }
    }
}

/**
 * POS 标准 Pass Hero：Balance Loaded、Top-Up Complete、Charge Approved / Partial Approval 共用
 * （圆角 24dp、1.6 宽高比、`cardBackground` 起色的同色系渐变终点、依底色选白/黑文案）。
 */
@Composable
private fun StandardMemberPassHeroCard(
    memberDisplayName: String,
    memberNo: String,
    tierDisplayName: String?,
    tierDiscountPercent: Double?,
    programCardDisplayName: String,
    /** Tier metadata `background` / API `cardBackground`：渐变起点与前景深浅的唯一依据。 */
    tierCardBackgroundHex: String?,
    /** Tier / NFT metadata image (GET metadata JSON `image`); bottom-right decoration replaces wallet icon when set. */
    cardMetadataImageUrl: String?,
    balancePrefix: String,
    balanceAmount: String,
    balanceSuffix: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val heroCornerRadius = 24.dp
    val tone = remember(tierCardBackgroundHex) { passHeroPalette(tierCardBackgroundHex) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .shadow(12.dp, shape, spotColor = Color(0x33000000))
            .clip(shape)
            .drawBehind {
                val r = CornerRadius(heroCornerRadius.toPx(), heroCornerRadius.toPx())
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(tone.gradientStart, tone.gradientEnd),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = r
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 120.dp, y = (-72).dp)
                .background(tone.decorativeCircle, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 12.dp)
                .size(48.dp)
                .border(1.dp, tone.avatarBorder, CircleShape)
                .clip(CircleShape)
                .background(tone.avatarBackdrop)
        ) {
            BeamioCardRasterOrSvgImage(
                model = cardMetadataImageUrl,
                contentDescription = programCardDisplayName,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
                fallback = {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        tint = tone.walletIconTint
                    )
                },
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        memberDisplayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tone.primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        memberNo.ifEmpty { "—" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = tone.primaryText,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val tierLine = tierDisplayName?.trim()?.takeIf { it.isNotEmpty() }
                    if (tierLine != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tierLine,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = tone.secondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            style = TextStyle(
                                lineHeight = 13.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                    val disc = tierDiscountPercent
                    if (disc != null && disc > 0.0) {
                        Text(
                            "${formatTierDiscountPercentForUi(disc)}% discount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = tone.tertiaryText,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            style = TextStyle(
                                lineHeight = 12.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
            Column {
                Text(
                    programCardDisplayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tone.secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                    if (balancePrefix.isNotEmpty()) {
                        Text(
                            balancePrefix.trimEnd(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tone.primaryText,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                    }
                    Text(
                        balanceAmount,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = tone.primaryText,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.6).sp
                    )
                    if (balanceSuffix.isNotEmpty()) {
                        Spacer(Modifier.width(2.dp))
                        Text(
                            balanceSuffix,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = tone.primaryText,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hero「CURRENT BALANCE」用：优先 `points6`（链上 6 位小数），再 `points`，
 * 避免仅填 `points6`、`points` 为 0 时大屏永远显示 0。
 */
private fun balanceAmountForBalanceDetailsHero(primaryPass: CardItem?, assets: UIDAssets?): Double {
    val card = primaryPass
    if (card != null) {
        val p6 = card.points6.toLongOrNull()
        if (p6 != null && p6 > 0L) return p6 / 1_000_000.0
        val p = card.points.toDoubleOrNull()
        if (p != null && p != 0.0) return p
    }
    val a6 = assets?.points6?.toLongOrNull()
    if (a6 != null && a6 > 0L) return a6 / 1_000_000.0
    val ap = assets?.points?.toDoubleOrNull()
    if (ap != null && ap != 0.0) return ap
    return 0.0
}

@Composable
internal fun ReadScreen(
    uid: String,
    status: ReadStatus,
    assets: UIDAssets?,
    rawResponseJson: String? = null,
    error: String,
    settlementViaQr: Boolean = false,
    /** Terminal-registered merchant / infrastructure card; hero & pass UI only use rows matching this address. */
    posMerchantInfraCard: String,
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
                LaunchedEffect(uid, assets?.beamioTag, assets?.uid, assets?.tagIdHex, assets?.counter) {
                    topupButtonEnabled = true
                    delay(60_000L)
                    topupButtonEnabled = false
                }
                val infraNorm = posMerchantInfraCard.trim()
                val cardList: List<CardItem>? = run {
                    val fromApi = assets?.cards?.orEmpty()?.filter { it.cardAddress.equals(infraNorm, ignoreCase = true) }
                    if (!fromApi.isNullOrEmpty()) return@run fromApi
                    val leg = assets?.cardAddress?.trim()
                    if (!leg.isNullOrEmpty() && leg.equals(infraNorm, ignoreCase = true)) {
                        val slice = assets?.cards?.orEmpty()
                            ?.firstOrNull { it.cardAddress.equals(leg, ignoreCase = true) }
                            ?: assets?.cards?.orEmpty()?.singleOrNull()
                        listOf(
                            CardItem(
                                cardAddress = leg,
                                cardName = slice?.cardName?.takeIf { it.isNotBlank() } ?: "Asset Card",
                                cardType = slice?.cardType?.takeIf { it.isNotBlank() } ?: "",
                                points = assets.points ?: slice?.points ?: "0",
                                points6 = assets.points6 ?: slice?.points6 ?: "0",
                                cardCurrency = assets.cardCurrency ?: slice?.cardCurrency ?: "CAD",
                                nfts = slice?.nfts?.takeIf { it.isNotEmpty() } ?: assets.nfts ?: emptyList(),
                                cardBackground = slice?.cardBackground,
                                cardImage = slice?.cardImage,
                                tierName = slice?.tierName,
                                tierDescription = slice?.tierDescription,
                                primaryMemberTokenId = slice?.primaryMemberTokenId?.trim()?.takeIf { it.isNotEmpty() }
                                    ?: assets.primaryMemberTokenId?.trim()?.takeIf { it.isNotEmpty() },
                                tierDiscountPercent = slice?.tierDiscountPercent
                            )
                        )
                    } else null
                }?.distinctBy { it.cardAddress.trim().lowercase(Locale.US) }
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxHeight < 560.dp
                    val sidePad = if (compact) 16.dp else 20.dp
                    val gapSm = if (compact) 8.dp else 10.dp
                    val btnH = if (compact) 44.dp else 48.dp
                    val bottomPad = if (compact) 12.dp else 16.dp
                    val primaryPass = cardList?.firstOrNull()
                    val balanceDetailsHeroCardBackgroundHex = primaryPass?.cardBackground?.trim()?.takeIf { it.isNotEmpty() }
                        ?: assets?.cards?.orEmpty()
                            ?.firstOrNull { it.cardAddress.equals(infraNorm, ignoreCase = true) }
                            ?.cardBackground?.trim()?.takeIf { it.isNotEmpty() }
                        ?: assets?.cards?.orEmpty()?.singleOrNull()?.cardBackground?.trim()?.takeIf { it.isNotEmpty() }
                    val memberDisplayName = passHeroMemberDisplayNameLine(
                        assets?.beamioTag,
                        assets?.address,
                        primaryPass,
                        primaryPass?.cardName,
                    )
                    val memberNoStr = when {
                        primaryPass != null -> memberNoFromCardItem(primaryPass)
                        assets != null -> {
                            val p = assets.primaryMemberTokenId?.trim()?.takeIf { it.isNotEmpty() }
                            if (p != null && (p.toLongOrNull()?.let { id -> id > 0 } == true)) {
                                "M-%s".format(p.padStart(6, '0'))
                            } else ""
                        }
                        else -> ""
                    }
                    val balanceNum = balanceAmountForBalanceDetailsHero(primaryPass, assets)
                    val balCurrency = primaryPass?.cardCurrency ?: assets?.cardCurrency ?: "CAD"
                    val (balPrefix, balAmt, balSuffix) = formatBalanceWithCurrencyProtocol(balanceNum, balCurrency)
                    val usdcBal = assets?.usdcBalance?.toDoubleOrNull() ?: 0.0
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BalanceDetailsSurface)
                    ) {
                        BalanceDetailsTopBar(onBack = onBack)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(balanceScroll)
                                .padding(horizontal = sidePad)
                                .padding(top = 16.dp, bottom = gapSm)
                        ) {
                            StandardMemberPassHeroCard(
                                memberDisplayName = memberDisplayName,
                                memberNo = memberNoStr,
                                tierDisplayName = primaryPass?.tierName?.trim()?.takeIf { it.isNotEmpty() },
                                tierDiscountPercent = primaryPass?.tierDiscountPercent,
                                programCardDisplayName = primaryPass?.let { balanceDetailsCardNameLine(it) } ?: "—",
                                tierCardBackgroundHex = balanceDetailsHeroCardBackgroundHex,
                                cardMetadataImageUrl = primaryPass?.cardImage,
                                balancePrefix = balPrefix,
                                balanceAmount = balAmt,
                                balanceSuffix = balSuffix,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(gapSm + 4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = BalanceDetailsSurfaceContainerLow),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "LAST TOP-UP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = BalanceDetailsOutline,
                                                letterSpacing = 1.sp
                                            )
                                            BalanceDetailsLastTopUpAmountRow(
                                                assets = assets,
                                                cardCurrency = balCurrency
                                            )
                                        }
                                        Column(
                                            Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "USDC on Base",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = BalanceDetailsOutline,
                                                letterSpacing = 1.sp,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    String.format(Locale.US, "%,.2f", usdcBal),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BalanceDetailsOnSurface,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    " USDC",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = BalanceDetailsOutline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (rawResponseJson != null && rawResponseJson.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = gapSm)
                                        .clickable { responseExpanded = !responseExpanded },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = BalanceDetailsSurfaceContainerLowest),
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
                                                color = BalanceDetailsOutline
                                            )
                                            Text(
                                                if (responseExpanded) "▼" else "▶",
                                                fontSize = 11.sp,
                                                color = BalanceDetailsOutline
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
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF64748b)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(horizontal = sidePad)
                                .padding(top = gapSm, bottom = bottomPad)
                        ) {
                            Button(
                                onClick = onTopupClick,
                                enabled = topupButtonEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(btnH),
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

/** Base mainnet transaction page on BaseScan. */
private fun baseScanTransactionUri(txHash: String): Uri? {
    val t = txHash.trim()
    if (t.isEmpty()) return null
    val path = when {
        t.startsWith("0x", ignoreCase = true) -> t
        t.length == 64 && t.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' } ->
            "0x${t.lowercase(Locale.US)}"
        else -> t
    }
    return Uri.parse("https://basescan.org/tx/$path")
}

private val receiptScrollBottomSpacerDp = 128.dp

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
    /** 客户 beamioTag（无 @），与 Balance Loaded 一致 */
    customerBeamioTag: String? = null,
    /** 客户 EOA，收据 Hero 在无 tag 时显示短地址 */
    customerWalletAddress: String? = null,
    cardBackground: String? = null,
    cardImage: String? = null,
    cardName: String? = null,
    tierName: String? = null,
    cardType: String? = null,
    passCard: CardItem? = null,
    settlementViaQr: Boolean = false,
    chargeTaxPercent: Double? = null,
    chargeTierDiscountPercent: Double? = null,
    tableNumber: String? = null,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val amountNum = amount.toDoubleOrNull() ?: 0.0
    val subtotalNum = subtotal?.toDoubleOrNull()
    val tipNum = tip?.toDoubleOrNull() ?: 0.0
    val postBalanceNum = postBalance?.toDoubleOrNull()
    val currency = cardCurrency ?: "CAD"
    val dateString = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date())
    val timeString = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date())
    val shortAddr = if (payee.length > 10) "${payee.take(6)}...${payee.takeLast(4)}" else payee
    val displayMemberNo = (memberNo?.takeIf { it.isNotBlank() } ?: shortAddr).ifEmpty { "—" }
    val shortTxHash = if (txHash.length > 12) "${txHash.take(7)}...${txHash.takeLast(5)}" else txHash

    val surface = BalanceDetailsSurface
    val primaryContainer = Color(0xFF004BC3)
    val primary = Color(0xFF003692)
    val onSurface = Color(0xFF1A1C1F)
    val onSurfaceVariant = Color(0xFF434654)
    val outline = Color(0xFF737685)
    val outlineVariant = Color(0xFFC3C6D6)
    val surfaceContainerLow = Color(0xFFF3F3F8)
    val memberTitle = passHeroMemberDisplayNameLine(
        customerBeamioTag,
        customerWalletAddress,
        passCard,
        cardName,
    )

    fun shareReceipt() {
        val body = buildString {
            append("Transaction Receipt\n")
            append("Date: $dateString, $timeString\n")
            append("Member: $memberTitle\n")
            append("Member No: $displayMemberNo\n")
            if (subtotalNum != null) {
                val (p, a, s) = formatBalanceWithCurrencyProtocol(subtotalNum, currency)
                append("Voucher deduction: $p$a$s\n")
            }
            val (ap, astr, asuf) = formatBalanceWithCurrencyProtocol(amountNum, currency)
            append("Total charged: $ap$astr$asuf\n")
            if (txHash.isNotBlank()) append("TX: $txHash\n")
            append("Payee: $payee\n")
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(send, "Share receipt"))
    }

    val scrollMid = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7))
    ) {
        Column(Modifier.fillMaxSize().background(BalanceDetailsSurface)) {
            BalanceDetailsTopBar(
                onBack = onDone,
                title = "Transaction Receipt",
                trailing = {
                    IconButton(
                        onClick = { shareReceipt() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = BalanceDetailsOutline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
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
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Print,
                            contentDescription = "Print",
                            tint = BalanceDetailsOutline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val vCompactH = maxHeight < 460.dp
                val hPad = if (vCompactH) 16.dp else 24.dp
                val overlapTop = if (vCompactH) 40.dp else 48.dp
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollMid)
                        .padding(horizontal = hPad)
                        .padding(top = 4.dp, bottom = 8.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                // Overlapping status + standard Pass Hero（与 Balance Details 一致）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = overlapTop)
                ) {
                    val payHeroDisc: Double? = passCard?.tierDiscountPercent ?: chargeTierDiscountPercent
                    val payProg = passCard?.let { balanceDetailsCardNameLine(it) }
                        ?: cardName?.takeIf { it.isNotBlank() }
                            ?.removeSuffix(" CARD")?.removeSuffix(" Card")?.trim()?.takeIf { it.isNotEmpty() }
                        ?: "—"
                    val (payBp, payBamt, payBs) = if (postBalanceNum != null) {
                        formatBalanceWithCurrencyProtocol(postBalanceNum, currency)
                    } else {
                        Triple("", "—", "")
                    }
                    StandardMemberPassHeroCard(
                        memberDisplayName = memberTitle,
                        memberNo = displayMemberNo,
                        tierDisplayName = passCard?.tierName?.trim()?.takeIf { it.isNotEmpty() }
                            ?: tierName?.trim()?.takeIf { it.isNotEmpty() },
                        tierDiscountPercent = payHeroDisc?.takeIf { it > 0.0 },
                        programCardDisplayName = payProg,
                        tierCardBackgroundHex = passCard?.cardBackground ?: cardBackground,
                        cardMetadataImageUrl = passCard?.cardImage?.takeIf { it.isNotBlank() }
                            ?: cardImage?.takeIf { it.isNotBlank() },
                        balancePrefix = payBp,
                        balanceAmount = payBamt,
                        balanceSuffix = payBs,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-overlapTop))
                            .zIndex(2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(4.dp, surface, CircleShape)
                                .clip(CircleShape)
                                .background(primaryContainer)
                                .shadow(12.dp, CircleShape, spotColor = primaryContainer.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = "Approved",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = primaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Smart Routing Engine
                if (subtotalNum != null) {
                    val taxP = chargeTaxPercent ?: 0.0
                    val taxAmt = subtotalNum * taxP / 100.0
                    val discP = chargeTierDiscountPercent
                    val discAmt = if (discP != null && discP > 0.0) subtotalNum * discP / 100.0 else 0.0
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceContainerLow)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val (sumP, sumS, sumX) = formatBalanceWithCurrencyProtocol(amountNum, currency)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                if (sumP.isNotEmpty()) {
                                    Text(
                                        sumP,
                                        fontSize = if (vCompactH) 20.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryContainer,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    sumS,
                                    fontSize = if (vCompactH) 30.sp else 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryContainer,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (sumX.isNotEmpty()) {
                                Text(
                                    sumX,
                                    fontSize = if (vCompactH) 20.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryContainer,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = outlineVariant.copy(alpha = 0.35f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Memory,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "SMART ROUTING ENGINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceVariant,
                                letterSpacing = 0.2.sp
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Voucher Deduction", fontSize = 14.sp, color = onSurfaceVariant)
                            val (vp, vs, vx) = formatBalanceWithCurrencyProtocol(subtotalNum, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (vp.isNotEmpty()) Text(vp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(vs, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                                if (vx.isNotEmpty()) Text(vx, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tax (${String.format(Locale.US, "%.2f", taxP)}%)",
                                fontSize = 14.sp,
                                color = onSurfaceVariant
                            )
                            val (txp, txs, txx) = formatBalanceWithCurrencyProtocol(taxAmt, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (txp.isNotEmpty()) Text(txp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = outline, fontFamily = FontFamily.Monospace)
                                Text(txs, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = outline, fontFamily = FontFamily.Monospace)
                                if (txx.isNotEmpty()) Text(txx, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = outline, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Tier discount", fontSize = 14.sp, color = onSurfaceVariant)
                                if (discP != null && discP > 0.0) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = primaryContainer.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "${formatTierDiscountPercentForUi(discP)}%",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            val (dp, ds, dx) = formatBalanceWithCurrencyProtocol(discAmt, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("- ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = primary, fontFamily = FontFamily.Monospace)
                                if (dp.isNotEmpty()) Text(dp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primary, fontFamily = FontFamily.Monospace)
                                Text(ds, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primary, fontFamily = FontFamily.Monospace)
                                if (dx.isNotEmpty()) Text(dx, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primary, fontFamily = FontFamily.Monospace)
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 2.dp),
                            thickness = 1.dp,
                            color = outlineVariant.copy(alpha = 0.35f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tip", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = onSurfaceVariant)
                            val (tp, ts, tt) = formatBalanceWithCurrencyProtocol(tipNum, currency)
                            Row(verticalAlignment = Alignment.Bottom) {
                                if (tp.isNotEmpty()) Text(tp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(ts, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                                if (tt.isNotEmpty()) Text(tt, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = onSurface, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Metadata
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DATE & TIME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = outline,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "$dateString, $timeString",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (!tableNumber.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TABLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = outline,
                                letterSpacing = 1.sp
                            )
                            Text(
                                tableNumber.trim(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            displayMemberNo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurface,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (txHash.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TX HASH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = outline,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                modifier = Modifier.clickable {
                                    val uri = baseScanTransactionUri(txHash) ?: return@clickable
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (_: Exception) {
                                    }
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = primary.copy(alpha = 0.06f)
                            ) {
                                Text(
                                    shortTxHash,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SETTLEMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = outline,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                if (settlementViaQr) "App Validator" else "NTAG 424 DNA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurface
                            )
                            Icon(
                                Icons.Filled.Verified,
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(receiptScrollBottomSpacerDp))
                }
            }
        }
    }
}

@Composable
private fun PartialApprovalContent(
    model: PartialApprovalUiModel,
    postBalance: String?,
    onBack: () -> Unit,
    onContinueRemaining: () -> Unit,
    onCancelRemaining: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val primaryBlue = Color(0xFF1D4ED8)
    val onSurface = Color(0xFF1A1C1F)
    val onSurfaceVariant = Color(0xFF434654)
    val outline = Color(0xFF737685)
    val emerald = Color(0xFF059669)
    val emeraldBadgeBg = Color(0xFFD1FAE5)
    val emeraldBadgeText = Color(0xFF065F46)
    val errorCol = Color(0xFFBA1A1A)
    val overlapTopPartial = 40.dp
    val datePart = java.text.SimpleDateFormat("MMM d, yyyy", Locale.US).format(java.util.Date()).uppercase(Locale.US)
    val timePart = java.text.SimpleDateFormat("HH:mm:ss", Locale.US).format(java.util.Date())
    val dateTimeLine = "$datePart · $timePart"
    val shortTx = if (model.txHash.length > 12) "${model.txHash.take(6)}...${model.txHash.takeLast(4)}" else model.txHash
    fun sharePartial() {
        fun fiatLine(amt: Double): String {
            val (p, a, s) = formatBalanceWithCurrencyProtocol(amt, model.payCurrency)
            return buildString {
                if (p.isNotEmpty()) {
                    append(p.trimEnd())
                    append(" ")
                }
                append(a)
                append(s)
            }
        }
        val body = buildString {
            append("Partial charge approved\n")
            append("Charged: ${fiatLine(model.chargedPayCur)}\n")
            append("Remaining: ${fiatLine(model.shortfallPayCur)}\n")
            append("Order total: ${fiatLine(model.orderTotalPayCur)}\n")
            if (model.txHash.isNotBlank()) append("TX: ${model.txHash}\n")
        }
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
        }, null))
    }
    val scroll = rememberScrollState()
    val balNum = postBalance?.takeIf { it.isNotBlank() && it != "—" }?.toDoubleOrNull() ?: 0.0
    val (chPref, chAmt, chSuf) = formatBalanceWithCurrencyProtocol(model.chargedPayCur, model.payCurrency)
    val (sfPref, sfAmt, sfSuf) = formatBalanceWithCurrencyProtocol(model.shortfallPayCur, model.payCurrency)
    val (totPref, totAmt, totSuf) = formatBalanceWithCurrencyProtocol(model.orderTotalPayCur, model.payCurrency)
    val (vPre, vAmt, vSuf) = formatBalanceWithCurrencyProtocol(model.subtotal, model.payCurrency)
    val (dPre, dAmt, dSuf) = formatBalanceWithCurrencyProtocol(model.tierDiscountAmount, model.payCurrency)
    val (taxPre, taxAmt, taxSuf) = formatBalanceWithCurrencyProtocol(model.taxAmount, model.payCurrency)
    val (tipPre, tipAm, tipSuf) = formatBalanceWithCurrencyProtocol(model.tipAmount, model.payCurrency)
    val tierLine = buildString {
        append("Tier Discount")
        model.tierDiscountLabel?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
    }
    val taxLabel = "Tax (${String.format(Locale.US, "%.2f", model.taxPercent)}%)"

    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(Color(0xFFf5f5f7))
    ) {
        Column(Modifier.fillMaxSize().background(BalanceDetailsSurface)) {
            val hPad = 24.dp
            BalanceDetailsTopBar(
                onBack = onBack,
                title = "Partial Approval",
                trailing = {
                    IconButton(onClick = { sharePartial() }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = BalanceDetailsOutline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(horizontal = hPad)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = overlapTopPartial)
                    ) {
                        val (pBp, pBa, pBs) = formatBalanceWithCurrencyProtocol(balNum, model.payCurrency)
                        StandardMemberPassHeroCard(
                            memberDisplayName = model.memberDisplayName,
                            memberNo = model.memberNoDisplay,
                            tierDisplayName = model.tierDiscountLabel?.trim()?.takeIf { it.isNotEmpty() },
                            tierDiscountPercent = model.tierDiscPct.takeIf { it > 0.0 },
                            programCardDisplayName = model.programCardDisplayName ?: model.memberDisplayName,
                            tierCardBackgroundHex = model.tierCardBackgroundHex,
                            cardMetadataImageUrl = model.cardMetadataImageUrl,
                            balancePrefix = pBp,
                            balanceAmount = pBa,
                            balanceSuffix = pBs,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-overlapTopPartial))
                            .zIndex(2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = emerald, modifier = Modifier.size(32.dp))
                        }
                        Surface(
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(999.dp),
                            color = emeraldBadgeBg
                        ) {
                            Text(
                                "PARTIAL APPROVAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldBadgeText,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Successfully Charged", fontSize = 12.sp, color = onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Text(chPref, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = FontFamily.Monospace)
                                Text(chAmt, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = FontFamily.Monospace)
                                Text(chSuf, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryBlue, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color(0xFFE2E2E7))
                        )
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Remaining Shortfall", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = errorCol)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.padding(top = 4.dp)) {
                                Text(sfPref, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = errorCol, fontFamily = FontFamily.Monospace)
                                Text(sfAmt, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = errorCol, fontFamily = FontFamily.Monospace)
                                Text(sfSuf, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = errorCol, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F3F8))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Info, null, tint = onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Text(
                                "The member's balance was exhausted. Please collect the remaining amount via an alternative payment method.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = onSurfaceVariant
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "SMART ROUTING ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceVariant,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Voucher Deduction", fontSize = 14.sp, color = onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("-", fontSize = 13.sp, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(vPre, fontSize = 12.sp, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(vAmt, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(vSuf, fontSize = 12.sp, color = emerald, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tierLine, fontSize = 14.sp, color = onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("-", fontSize = 13.sp, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(dPre, fontSize = 12.sp, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(dAmt, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = emerald, fontFamily = FontFamily.Monospace)
                                Text(dSuf, fontSize = 12.sp, color = emerald, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(taxLabel, fontSize = 14.sp, color = onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("+", fontSize = 13.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(taxPre, fontSize = 12.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(taxAmt, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(taxSuf, fontSize = 12.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Service Tip", fontSize = 14.sp, color = onSurfaceVariant)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("+", fontSize = 13.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(tipPre, fontSize = 12.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(tipAm, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(tipSuf, fontSize = 12.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E2E7).copy(0.5f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Order Value", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onSurface)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(totPref, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(totAmt, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onSurface, fontFamily = FontFamily.Monospace)
                                Text(totSuf, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurface, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DATE & TIME", fontSize = 9.sp, color = outline, letterSpacing = 1.sp)
                            Text(dateTimeLine, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = onSurface, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TX HASH", fontSize = 9.sp, color = outline, letterSpacing = 1.sp)
                            Text(shortTx, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = onSurface, modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "SETTLEMENT (${if (model.settlementViaQr) "App Validator" else "NTAG 424 DNA"})",
                                fontSize = 9.sp,
                                color = outline,
                                letterSpacing = 1.sp
                            )
                            Text(model.settlementDetail, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = onSurface, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(model.memberNoDisplay, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = onSurface)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.92f))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = hPad, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF004BC3), Color(0xFF1562F0))))
                        .clickable { onContinueRemaining() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Continue Remaining Charge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                TextButton(
                    onClick = onCancelRemaining,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Remaining", color = outline, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            // ~0.5rem inset so bottom-anchored process lines sit slightly above the bezel edge
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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
    passCardSnapshot: CardItem? = null,
    settlementViaQr: Boolean = false,
    customerBeamioTag: String? = null,
    customerWalletAddress: String? = null,
    chargeTaxPercent: Double? = null,
    chargeTierDiscountPercent: Double? = null,
    tableNumber: String? = null,
    partialApproval: PartialApprovalUiModel? = null,
    onPartialApprovalContinue: () -> Unit = {},
    onPartialApprovalCancel: () -> Unit = {},
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
            is PaymentStatus.Success -> if (partialApproval != null) {
                PartialApprovalContent(
                    model = partialApproval,
                    postBalance = postBalance,
                    onBack = onBack,
                    onContinueRemaining = onPartialApprovalContinue,
                    onCancelRemaining = onPartialApprovalCancel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PaymentSuccessContent(
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
                    passCard = passCardSnapshot,
                    settlementViaQr = settlementViaQr,
                    customerBeamioTag = customerBeamioTag,
                    customerWalletAddress = customerWalletAddress,
                    chargeTaxPercent = chargeTaxPercent,
                    chargeTierDiscountPercent = chargeTierDiscountPercent,
                    tableNumber = tableNumber,
                    onDone = onBack
                )
            }
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
        val avatarModifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
        val primaryAvatarModel = profile.image?.takeIf { it.isNotBlank() } ?: avatarUrl
        BeamioCardRasterOrSvgImage(
            model = primaryAvatarModel,
            contentDescription = null,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
            fallback = {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = avatarModifier,
                    contentScale = ContentScale.Crop
                )
            },
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
    paymentPassCard: CardItem? = null,
    /** Charge 成功页与 Balance 对齐：客户 beamioTag / EOA */
    paymentCustomerBeamioTag: String? = null,
    paymentCustomerWalletAddress: String? = null,
    paymentChargeTaxPercent: Double? = null,
    paymentChargeTierDiscountPercent: Double? = null,
    paymentChargeTipBps: Int = 0,
    paymentTableNumber: String = "",
    paymentPartialApproval: PartialApprovalUiModel? = null,
    onPartialApprovalContinue: () -> Unit = {},
    onPartialApprovalCancel: () -> Unit = {},
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

    /** 中央为 loading / routing / execute 时隐藏顶栏返回，避免流程中误退 */
    val scanMethodTopBarLoading = showPaymentRouting ||
        (pendingAction == "payment" && paymentQrInterpreting) ||
        (pendingAction == "topup" && (topupQrSigningInProgress || topupNfcExecuteInProgress)) ||
        nfcFetchingInfo ||
        (linkAppLockedShowCancel && linkAppCancelInProgress)

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
    val haptic = LocalHapticFeedback.current

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
                    if (paymentPartialApproval != null) {
                        PartialApprovalContent(
                            model = paymentPartialApproval,
                            postBalance = paymentPostBalance,
                            onBack = onPaymentDone,
                            onContinueRemaining = onPartialApprovalContinue,
                            onCancelRemaining = onPartialApprovalCancel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
                        passCard = paymentPassCard,
                        settlementViaQr = scanMethod == "qr",
                        customerBeamioTag = paymentCustomerBeamioTag,
                        customerWalletAddress = paymentCustomerWalletAddress,
                        chargeTaxPercent = paymentChargeTaxPercent,
                        chargeTierDiscountPercent = paymentChargeTierDiscountPercent,
                        tableNumber = paymentTableNumber.trim().takeIf { it.isNotEmpty() },
                        onDone = onPaymentDone,
                        modifier = Modifier.fillMaxSize()
                    )
                    }
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
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onLinkAppCancelLock()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF1562f0),
                                                contentColor = Color.White,
                                                disabledContainerColor = Color(0xFF1562f0).copy(alpha = 0.35f),
                                                disabledContentColor = Color.White.copy(alpha = 0.55f)
                                            )
                                        ) {
                                            Text(
                                                "Cancel link lock",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
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

        // Top bar overlay: Back button (left) + NFC/QR toggle (center, when visible)；支付成功页自带顶栏，不再铺灰底
        if (!showPaymentSuccess && !scanMethodTopBarLoading) {
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
                    onClick = onCancel,
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

/** Transaction status — insufficient balance (layout aligned with verra-home home1.html). */
@Composable
private fun InsufficientBalanceScreen(
    model: InsufficientBalanceUiModel,
    onTopUp: () -> Unit,
    onChargeAvailableBalance: () -> Unit,
    onContinueCharge: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val bg = Color(0xFFF9F9FE)
    val onSurface = Color(0xFF1A1C1F)
    val onSurfaceVariant = Color(0xFF434654)
    val errorC = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)
    val onErrorContainer = Color(0xFF93000A)
    val primaryBlue = Color(0xFF003692)
    val surfaceLow = Color(0xFFF3F3F8)
    val surfaceHigh = Color(0xFFE7E8ED)
    val secondaryBlue = Color(0xFF266AF8)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BACK_BUTTON_TOP_BAR_HEIGHT)
            ) {
                BackButtonIcon(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCancel()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = BACK_BUTTON_START_PADDING)
                )
                Text(
                    "Transaction Status",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 48.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = onErrorContainer
                    )
                }
                Text(
                    "Insufficient Balance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    "Your wallet requires additional funds to complete this request.",
                    fontSize = 14.sp,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Charge Amount", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = onSurfaceVariant)
                            Text(
                                model.totalChargeFormatted,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Wallet Balance", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = onSurfaceVariant)
                            Text(
                                model.currentBalanceFormatted,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurface,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        HorizontalDivider(color = Color.Black.copy(alpha = 0.06f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(errorContainer.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SHORTFALL AMOUNT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = errorC,
                                letterSpacing = 1.sp
                            )
                            Text(
                                model.shortfallFormatted,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = errorC,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceHigh.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(18.dp))
                            Text(
                                "SMART ROUTING ENGINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryBlue,
                                letterSpacing = 2.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voucher Deduction", fontSize = 14.sp, color = onSurfaceVariant)
                            Text(model.voucherDeductionFormatted, fontSize = 14.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tier discount", fontSize = 14.sp, color = onSurfaceVariant)
                            Text(model.tierDiscountFormatted, fontSize = 14.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tip", fontSize = 14.sp, color = onSurfaceVariant)
                            Text(model.tipFormatted, fontSize = 14.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax", fontSize = 14.sp, color = onSurfaceVariant)
                            Text(model.taxFormatted, fontSize = 14.sp, color = onSurface, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                if (model.availableBalanceUsdc6 > 0L) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onChargeAvailableBalance()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Charge Available Balance", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTopUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Top-Up", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onContinueCharge()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = onSurface)
                ) {
                    Text("Continue Charge", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCancel()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "CANCEL TRANSACTION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = errorC,
                        letterSpacing = 2.sp
                    )
                }
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