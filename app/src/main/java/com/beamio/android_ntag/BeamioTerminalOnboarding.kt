package com.beamio.android_ntag

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.Locale

/** iOS `MarketExampleTerminalTheme` (marketExample.html). */
private val MktBg = Color(0xFFF5F7F9)
private val MktPrimary = Color(0xFF0051D1)
private val MktOnSurface = Color(0xFF2C2F31)
private val MktOnSurfaceVariant = Color(0xFF595C5E)
private val MktSurfaceLow = Color(0xFFEEF1F3)
private val MktSurfaceLowest = Color.White
private val MktOutlineVariant = Color(0xFFABADAF)

private val HeroImageUrl =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuC81jazJ6aagCDIf-YFpCeIgCrQ6ESZEbBv5Wlhpz-yY0JbCRJOXX5EILx6F4d2awTUwfnt3HKK36PRL2-GizaBHdbdkdBmcA0J_5PahS-Wrn3tsths3Vew2IgALnwxo2V2EalIIlIAD1IEyJnKUzntUt7dL2FNyxnUOaa4r2ANMFEWFWf0Mc3lg8C16tIZQMn7naGD0XpVDdT_IXlsL_svhLL1VnmWPAnO7Y2c54AnYUCUvDpbujAbOYd_lgCgp5g0Q1Ea9nLjb8I"

private val OnboardBrandBlue = Color(0xFF1562F0)

private fun normalizeTagQuery(raw: String): String = BeamioTagRules.normalizeInput(raw)

/**
 * Align iOS `POSViewModel.assemblePosTerminalBeamioTag`: `{parent}_POS_{nnnn}` within 20 chars
 * (Cluster / `isBeamioAccountNameAvailable` rule). No literal `$` in the tag.
 */
internal fun assemblePosTerminalBeamioTag(parentRaw: String, sequence: Int): String {
    val seq = sequence.coerceIn(0, 9999)
    val tail = "_POS_" + String.format(Locale.US, "%04d", seq)
    var base = normalizeTagQuery(parentRaw)
    base = base.replace(Regex("[^a-zA-Z0-9_.]"), "")
    if (base.isEmpty()) base = "pos"
    val maxPrefix = (20 - tail.length).coerceAtLeast(0)
    if (base.length > maxPrefix) base = base.take(maxPrefix)
    val combined = base + tail
    if (combined.length < 3) return "pos$tail"
    return combined
}

private const val ONBOARD_TAG_AVAILABILITY_RETRIES = 3
private const val ONBOARD_TAG_AVAILABILITY_RETRY_DELAY_MS = 400L

/**
 * Calls registry until a definite `true`/`false` or retries exhausted (`null`).
 * Only `true` means the name is verified usable as a beamioTag before we prefill the field.
 */
private suspend fun isBeamioTagNameVerifiedAvailable(candidate: String): Boolean? {
    repeat(ONBOARD_TAG_AVAILABILITY_RETRIES) { attempt ->
        val avail = withContext(Dispatchers.IO) {
            BeamioOnboardingApi.isBeamioAccountNameAvailableSync(candidate)
        }
        when (avail) {
            true, false -> return avail
            null -> {
                if (attempt < ONBOARD_TAG_AVAILABILITY_RETRIES - 1) {
                    delay(ONBOARD_TAG_AVAILABILITY_RETRY_DELAY_MS)
                }
            }
        }
    }
    return null
}

/**
 * First assembled candidate for which on-chain `isAccountNameAvailable` returns **true** (after retries on RPC `null`).
 * Returns **empty** if no candidate is confirmed available or availability could not be verified (do not prefill unverified names).
 */
internal suspend fun resolveFirstAvailablePosTerminalTag(parentRaw: String): String {
    val parent = normalizeTagQuery(parentRaw)
    if (parent.isEmpty()) return ""
    for (n in 1..9999) {
        val candidate = assemblePosTerminalBeamioTag(parentRaw, n)
        if (!candidate.matches(BeamioTagRules.ALLOWED_REGEX)) continue
        when (isBeamioTagNameVerifiedAvailable(candidate)) {
            true -> return candidate
            false -> continue
            null -> return ""
        }
    }
    return ""
}

private fun sanitizeProfilePart(raw: String?): String {
    val t = raw?.trim().orEmpty()
    if (t.isEmpty() || t.equals("null", ignoreCase = true)) return ""
    return t
}

private fun shortAddr(s: String): String {
    val t = s.trim()
    if (t.length < 10) return t
    return "${t.take(6)}…${t.takeLast(4)}"
}

private fun dicebearUrl(seed: String): String {
    val enc = URLEncoder.encode(seed.ifEmpty { "Beamio" }, "UTF-8")
    return "https://api.dicebear.com/8.x/fun-emoji/png?seed=$enc"
}

private suspend fun welcomeFetchSearchResults(programCardAddress: String, keywordLower: String): List<TerminalProfile> {
    if (keywordLower.length < 2) return emptyList()
    return withContext(Dispatchers.IO) {
        BeamioOnboardingApi.searchUsersByCardOwnerOrAdminSync(
            keywordLower,
            listOf(programCardAddress),
        )
    }
}

/**
 * Terminal Setup splash — align iOS POS entry splash: hero, @BeamioTag search, Next Phase.
 */
@Composable
fun WelcomePage(
    versionName: String,
    programCardAddressForSearch: String,
    onNextPhase: (prefillNormalizedHandle: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    var tagQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<TerminalProfile>>(emptyList()) }
    var searchLoading by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var searchRequestId by remember { mutableIntStateOf(0) }
    var selectedLookup by remember { mutableStateOf<TerminalProfile?>(null) }

    val scope = rememberCoroutineScope()
    val keywordForSearch = remember(tagQuery) { normalizeTagQuery(tagQuery).lowercase(Locale.US) }

    LaunchedEffect(tagQuery) {
        val stripped = tagQuery.replace("@", "")
        if (stripped != tagQuery) {
            tagQuery = stripped
            return@LaunchedEffect
        }
        val sel = selectedLookup
        val selUser = sel?.accountName?.trim()?.lowercase(Locale.US).orEmpty()
        if (sel != null && selUser.isNotEmpty() && stripped.lowercase(Locale.US) == selUser) {
            return@LaunchedEffect
        }
        selectedLookup = null
    }

    LaunchedEffect(keywordForSearch) {
        if (keywordForSearch.length < 2) {
            searchResults = emptyList()
            showDropdown = false
            searchLoading = false
            return@LaunchedEffect
        }
        delay(350)
        if (keywordForSearch.length < 2) return@LaunchedEffect
        searchRequestId++
        val id = searchRequestId
        searchLoading = true
        showDropdown = true
        val list = welcomeFetchSearchResults(programCardAddressForSearch, keywordForSearch)
        if (id == searchRequestId) {
            searchLoading = false
            searchResults = list
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MktBg),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            welcomeHeroBlock(versionName = versionName)
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Link Terminal to ",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = MktOnSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Workspace",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = MktPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Enter your business @BeamioTag to authorize this device. This secures your transactions and syncs your inventory.",
                fontSize = 16.sp,
                color = MktOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search an existing @BeamioTag below (workspace-linked results). After you pick a profile, continue with Next Phase or remove the selection to search again.",
                fontSize = 15.sp,
                color = MktOnSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(22.dp))

            if (selectedLookup == null) {
                Text(
                    text = "@BeamioTag",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MktOnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MktSurfaceLow),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = tagQuery,
                        onValueChange = { tagQuery = it.replace("@", "") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MktOnSurface,
                        ),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (tagQuery.isEmpty()) {
                                Text(
                                    "e.g. coffee_house_ny",
                                    color = MktOnSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 17.sp,
                                )
                            }
                            inner()
                        },
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val kw = keywordForSearch
                                if (kw.length < 2) {
                                    searchResults = emptyList()
                                    showDropdown = false
                                    searchLoading = false
                                    return@launch
                                }
                                searchRequestId++
                                val id = searchRequestId
                                searchLoading = true
                                showDropdown = true
                                val list = welcomeFetchSearchResults(programCardAddressForSearch, kw)
                                if (id == searchRequestId) {
                                    searchLoading = false
                                    searchResults = list
                                }
                            }
                        },
                        enabled = keywordForSearch.length >= 2 && !searchLoading,
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
                            .size(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MktPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        if (searchLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.Search, null, tint = Color.White)
                        }
                    }
                }
                if (showDropdown && keywordForSearch.length >= 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MktSurfaceLowest)
                            .border(1.dp, MktOutlineVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    ) {
                        if (searchLoading && searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = MktPrimary)
                            }
                        } else if (searchResults.isEmpty()) {
                            Text(
                                "No matches",
                                modifier = Modifier.padding(16.dp),
                                color = MktOnSurfaceVariant,
                                fontSize = 14.sp,
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 220.dp)
                                    .verticalScroll(rememberScrollState()),
                            ) {
                                searchResults.forEachIndexed { idx, profile ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val t = sanitizeProfilePart(profile.accountName)
                                                tagQuery = t.ifEmpty {
                                                    profile.address?.trim().orEmpty()
                                                }
                                                selectedLookup = profile
                                                showDropdown = false
                                                searchResults = emptyList()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        welcomeSearchResultRow(profile = profile)
                                    }
                                    if (idx < searchResults.lastIndex) {
                                        HorizontalDivider(color = MktOutlineVariant.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                welcomeSelectedProfileBlock(
                    profile = selectedLookup!!,
                    onClear = {
                        selectedLookup = null
                        tagQuery = ""
                        searchResults = emptyList()
                        showDropdown = false
                    },
                    onNextPhase = {
                        onNextPhase(normalizeTagQuery(tagQuery))
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            welcomeInfoStatusCard(selected = selectedLookup)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun welcomeSearchResultRow(profile: TerminalProfile) {
    val title = welcomeRowTitle(profile)
    val subtitle = welcomeRowSubtitle(profile)
    val seed = sanitizeProfilePart(profile.accountName).ifEmpty {
        profile.address ?: "beamio"
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val img = profile.image?.trim().orEmpty()
        if (img.isNotEmpty()) {
            AsyncImage(
                model = img,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            AsyncImage(
                model = dicebearUrl(seed),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MktOnSurface, maxLines = 1)
            subtitle?.let {
                Text(it, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MktOnSurfaceVariant, maxLines = 1)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MktOutlineVariant, modifier = Modifier.size(16.dp))
    }
}

private fun welcomeRowTitle(profile: TerminalProfile): String {
    val raw = sanitizeProfilePart(profile.accountName)
    if (raw.isNotEmpty()) return "@$raw"
    val a = profile.address?.trim().orEmpty()
    return if (a.isNotEmpty()) shortAddr(a) else "—"
}

private fun welcomeRowSubtitle(profile: TerminalProfile): String? {
    val a = profile.address?.trim().orEmpty()
    if (a.isEmpty()) return null
    val short = shortAddr(a)
    if (short == welcomeRowTitle(profile)) return null
    return short
}

@Composable
private fun welcomeSelectedProfileBlock(
    profile: TerminalProfile,
    onClear: () -> Unit,
    onNextPhase: () -> Unit,
) {
    val tagLabel = welcomeHomeStyleBeamioTagLabel(profile)
    val seed = sanitizeProfilePart(profile.accountName).ifEmpty { profile.address ?: "Beamio" }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(999.dp))
                .background(MktSurfaceLowest)
                .border(1.dp, MktOutlineVariant.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AsyncImage(
                model = profile.image?.trim().orEmpty().ifEmpty { dicebearUrl(seed) },
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Text(
                tagLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MktOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onClear) {
            Icon(Icons.Filled.Close, contentDescription = "Remove selection", tint = MktOnSurfaceVariant.copy(alpha = 0.5f))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onNextPhase,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MktPrimary),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Next Phase", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Authorize", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Filled.ChevronRight, null, tint = Color.White)
            }
        }
    }
}

private fun welcomeHomeStyleBeamioTagLabel(profile: TerminalProfile): String {
    val raw = sanitizeProfilePart(profile.accountName)
    if (raw.isNotEmpty()) return "@$raw"
    val a = profile.address?.trim().orEmpty()
    return if (a.isNotEmpty()) shortAddr(a) else "@—"
}

@Composable
private fun welcomeInfoStatusCard(selected: TerminalProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MktSurfaceLow.copy(alpha = 0.5f))
            .border(1.dp, MktOutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MktPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = MktPrimary, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (selected == null) "Ready to bind" else "Profile selected",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MktOnSurface,
            )
            Text(
                if (selected == null) {
                    "Type at least 2 characters, then search. Tap a result to confirm your workspace tag, then use Next Phase below the profile capsule."
                } else {
                    "Tap Next Phase for wallet setup; we suggest an available terminal handle from this tag (parent_POS_####) and verify it can be registered. Use the remove control to search again."
                },
                fontSize = 12.sp,
                color = MktOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun welcomeHeroBlock(versionName: String) {
    val corner = 14.dp
    val heroH = 256.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroH)
            .padding(horizontal = 0.dp)
            .clip(RoundedCornerShape(corner)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(MktPrimary.copy(alpha = 0.35f), Color.Transparent),
                        radius = 400f,
                    ),
                ),
        )
        AsyncImage(
            model = HeroImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.85f,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MktSurfaceLowest.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MktPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("SoftPOS Native", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MktOnSurface)
                    Text("v$versionName Build Stable", fontSize = 12.sp, color = MktOnSurfaceVariant)
                }
            }
        }
    }
}

private enum class OnboardTagStatus { Idle, Checking, Valid, Invalid }

/**
 * Wallet setup — align iOS `OnboardingView`: handle check, password rules, Continue → addUser.
 */
@Composable
fun OnboardingScreen(
    /** Normalized parent @BeamioTag from Terminal Setup; used to build `{parent}_POS_{nnnn}` and probe availability. */
    parentBeamioTagFromWelcome: String,
    onCreateComplete: (privateKeyHex: String, registeredBeamioAccountName: String) -> Unit,
    onBackToWelcome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var beamioTag by remember(parentBeamioTagFromWelcome) { mutableStateOf("") }
    var suggestionResolving by remember(parentBeamioTagFromWelcome) {
        mutableStateOf(parentBeamioTagFromWelcome.isNotBlank())
    }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var tagStatus by remember { mutableStateOf(OnboardTagStatus.Idle) }
    var tagError by remember { mutableStateOf("") }
    var lastCheckedTag by remember { mutableStateOf("") }
    var submitError by remember { mutableStateOf("") }
    var suggestionResolveError by remember(parentBeamioTagFromWelcome) { mutableStateOf("") }

    LaunchedEffect(parentBeamioTagFromWelcome) {
        val stem = parentBeamioTagFromWelcome.trim()
        suggestionResolveError = ""
        lastCheckedTag = ""
        if (stem.isEmpty()) {
            beamioTag = ""
            suggestionResolving = false
            tagStatus = OnboardTagStatus.Idle
            return@LaunchedEffect
        }
        suggestionResolving = true
        tagStatus = OnboardTagStatus.Idle
        tagError = ""
        val resolved = resolveFirstAvailablePosTerminalTag(stem)
        suggestionResolving = false
        if (resolved.isEmpty()) {
            beamioTag = ""
            tagStatus = OnboardTagStatus.Invalid
            suggestionResolveError =
                "Could not verify an available terminal handle. Check your connection and try again, or enter a handle manually."
            return@LaunchedEffect
        }
        val confirm = withContext(Dispatchers.IO) {
            BeamioOnboardingApi.isBeamioAccountNameAvailableSync(resolved)
        }
        when (confirm) {
            true -> {
                beamioTag = resolved
                lastCheckedTag = resolved.trim().lowercase(Locale.US)
                tagStatus = OnboardTagStatus.Valid
                tagError = ""
                suggestionResolveError = ""
            }
            false -> {
                beamioTag = ""
                tagStatus = OnboardTagStatus.Invalid
                suggestionResolveError = "@$resolved is already taken. Try again or choose a handle manually."
            }
            null -> {
                beamioTag = ""
                tagStatus = OnboardTagStatus.Invalid
                suggestionResolveError =
                    "Could not verify handle on-chain. Check your connection and try again, or enter a handle manually."
            }
        }
    }

    fun localValidateTag(v: String): Pair<Boolean, String> {
        val t = normalizeTagQuery(v)
        if (t.isEmpty()) return false to "Please enter a business handle"
        if (!t.matches(BeamioTagRules.ALLOWED_REGEX)) {
            return false to BeamioTagRules.RULE_HINT
        }
        return true to t
    }

    val passwordRules = remember(password) {
        val len8 = password.length >= 8
        val mixed = password.contains(Regex("[a-z]")) && password.contains(Regex("[A-Z]"))
        val numbers = password.contains(Regex("[0-9]"))
        Triple(len8, mixed, numbers)
    }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val confirmMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

    LaunchedEffect(beamioTag) {
        val canon = normalizeTagQuery(beamioTag)
        val canonLc = canon.lowercase(Locale.US)
        if (canon.length > 2 &&
            canonLc == lastCheckedTag.lowercase(Locale.US) &&
            tagStatus == OnboardTagStatus.Valid &&
            tagError.isEmpty() &&
            suggestionResolveError.isEmpty()
        ) {
            return@LaunchedEffect
        }
        tagError = ""
        if (canonLc.length <= 2) {
            if (canonLc.isEmpty()) {
                if (suggestionResolveError.isEmpty()) {
                    tagStatus = OnboardTagStatus.Idle
                }
                return@LaunchedEffect
            }
            tagStatus = OnboardTagStatus.Idle
            return@LaunchedEffect
        }
        tagStatus = OnboardTagStatus.Idle
        delay(3000)
        val afterWait = normalizeTagQuery(beamioTag)
        if (afterWait.lowercase(Locale.US) != canonLc) return@LaunchedEffect
        val loc = localValidateTag(afterWait)
        if (!loc.first) return@LaunchedEffect
        tagStatus = OnboardTagStatus.Checking
        val available = withContext(Dispatchers.IO) {
            BeamioOnboardingApi.isBeamioAccountNameAvailableSync(afterWait)
        }
        if (normalizeTagQuery(beamioTag).lowercase(Locale.US) != canonLc) return@LaunchedEffect
        when (available) {
            false -> {
                tagStatus = OnboardTagStatus.Invalid
                tagError = "@$afterWait is already taken"
            }
            true -> {
                tagStatus = OnboardTagStatus.Valid
                tagError = ""
                lastCheckedTag = afterWait.lowercase(Locale.US)
            }
            null -> {
                tagStatus = OnboardTagStatus.Invalid
                tagError = "Network error. Try again."
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Step 1 of 2",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = OnboardBrandBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(OnboardBrandBlue.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Text("Business identity", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = MktOnSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Create your business identity", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Choose your Beamio handle and set the password that protects your business workspace.",
                fontSize = 16.sp,
                color = MktOnSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(28.dp))

            Text("Business handle", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, color = Color.Black.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(10.dp))
            if (suggestionResolving) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = OnboardBrandBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Resolving suggested terminal handle from your workspace…",
                        fontSize = 12.sp,
                        color = MktOnSurfaceVariant,
                    )
                }
            }
            OutlinedTextField(
                value = beamioTag,
                onValueChange = { nv ->
                    beamioTag = nv.replace("@", "")
                    suggestionResolveError = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !suggestionResolving,
                placeholder = { Text("@yourbusiness") },
                singleLine = true,
                isError = tagStatus == OnboardTagStatus.Invalid || suggestionResolveError.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE5E5EA),
                    unfocusedContainerColor = Color(0xFFE5E5EA),
                ),
            )
            when {
                suggestionResolveError.isNotEmpty() -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = Color(0xFFFF9500), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(suggestionResolveError, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9500))
                    }
                }
                tagStatus == OnboardTagStatus.Invalid && tagError.isNotEmpty() -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = Color(0xFFFF9500), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tagError, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9500))
                    }
                }
                tagStatus == OnboardTagStatus.Valid && normalizeTagQuery(beamioTag).isNotEmpty() -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = OnboardBrandBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("@${normalizeTagQuery(beamioTag)} is available", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OnboardBrandBlue)
                    }
                }
                tagStatus == OnboardTagStatus.Checking -> {
                    Row(modifier = Modifier.padding(start = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking availability…", fontSize = 12.sp, color = MktOnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Account password", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, color = Color.Black.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE5E5EA),
                        unfocusedContainerColor = Color(0xFFE5E5EA),
                    ),
                )
                IconButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show", fontSize = 12.sp, color = MktOnSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Confirm password", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, color = Color.Black.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Confirm password") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmMismatch,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE5E5EA),
                    unfocusedContainerColor = Color(0xFFE5E5EA),
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))
            onboardRuleRow(ok = passwordRules.first, text = "At least 8 characters")
            onboardRuleRow(ok = passwordRules.second, text = "Upper and lower case letters")
            onboardRuleRow(ok = passwordRules.third, text = "At least one number")

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Filled.CheckCircle, null, tint = MktOnSurfaceVariant.copy(alpha = 0.45f), modifier = Modifier.size(22.dp))
                Column {
                    Text("Protected by local encryption", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                    Text(
                        "Your business credentials stay encrypted on this device and under your control.",
                        fontSize = 13.sp,
                        color = MktOnSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                Toast.makeText(
                    ctx,
                    "Use Beamio Business on the web to restore an existing workspace.",
                    Toast.LENGTH_LONG,
                ).show()
                onBackToWelcome()
            }) {
                Row {
                    Text("Already have a wallet? ", color = MktOnSurfaceVariant)
                    Text("Restore", color = OnboardBrandBlue)
                }
            }

            val canSubmit = !suggestionResolving &&
                tagStatus == OnboardTagStatus.Valid &&
                passwordRules.first && passwordRules.second && passwordRules.third &&
                passwordsMatch && !isSubmitting && tagStatus != OnboardTagStatus.Checking

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    submitError = ""
                    scope.launch {
                        val norm = normalizeTagQuery(beamioTag)
                        val loc = localValidateTag(norm)
                        if (!loc.first) {
                            tagStatus = OnboardTagStatus.Invalid
                            tagError = loc.second
                            return@launch
                        }
                        isSubmitting = true
                        val available = withContext(Dispatchers.IO) {
                            BeamioOnboardingApi.isBeamioAccountNameAvailableSync(norm)
                        }
                        when (available) {
                            false -> {
                                tagStatus = OnboardTagStatus.Invalid
                                tagError = "@$norm is already taken"
                                isSubmitting = false
                                return@launch
                            }
                            null -> {
                                tagStatus = OnboardTagStatus.Invalid
                                tagError = "Network error. Try again."
                                isSubmitting = false
                                return@launch
                            }
                            true -> { }
                        }
                        if (!passwordRules.first || !passwordRules.second || !passwordRules.third || !passwordsMatch) {
                            isSubmitting = false
                            return@launch
                        }
                        try {
                            val pk = BeamioWeb3Wallet.generatePrivateKeyHex()
                            val addrLower = BeamioWeb3Wallet.addressHexLowerFromPrivateKeyHex(pk)
                            val walletCs = BeamioOnboardingApi.toChecksumAddress0x(addrLower)
                            val sig = withContext(Dispatchers.Default) {
                                BeamioWeb3Wallet.signEthereumPersonalMessageUtf8(pk, walletCs)
                            }
                            val err = withContext(Dispatchers.IO) {
                                BeamioOnboardingApi.addUserSync(norm, walletCs, sig)
                            }
                            isSubmitting = false
                            if (err != null) {
                                submitError = err
                            } else {
                                onCreateComplete(pk, norm)
                            }
                        } catch (e: Exception) {
                            isSubmitting = false
                            submitError = e.message ?: "Failed"
                        }
                    }
                },
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSubmit && !isSubmitting) OnboardBrandBlue else Color(0xFFD1D1D6),
                    disabledContainerColor = Color(0xFFD1D1D6),
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (canSubmit && !isSubmitting) Color.White else MktOnSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ChevronRight, null, tint = if (canSubmit && !isSubmitting) Color.White else MktOnSurfaceVariant)
                }
            }
            if (submitError.isNotEmpty()) {
                Text(submitError, color = Color(0xFFE11D48), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBackToWelcome) {
                Text("Back", fontWeight = FontWeight.SemiBold)
            }
        }

        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = OnboardBrandBlue)
                    Text("Creating your business workspace…", fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Text(
                        "We're preparing your business identity and getting your Beamio workspace ready.",
                        fontSize = 15.sp,
                        color = MktOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Text("This usually takes a few seconds.", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MktOnSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun onboardRuleRow(ok: Boolean, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            null,
            tint = if (ok) OnboardBrandBlue else MktOnSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(16.dp),
        )
        Text(text, fontSize = 12.sp, color = MktOnSurfaceVariant)
    }
}
