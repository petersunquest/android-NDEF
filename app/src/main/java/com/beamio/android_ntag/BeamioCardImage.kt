package com.beamio.android_ntag

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.imageLoader

/**
 * True when the URL is likely SVG (path, data URL, or common query hints).
 * Aligns with iOS `beamioCardImageUrlLooksLikeSvg`.
 */
internal fun beamioCardImageUrlLooksLikeSvg(urlString: String): Boolean {
    val t = urlString.trim()
    if (t.isEmpty()) return false
    val lower = t.lowercase()
    if (lower.startsWith("data:image/svg+xml")) return true
    val u = try {
        Uri.parse(t)
    } catch (_: Exception) {
        return false
    }
    val path = (u.path ?: "").lowercase()
    if (path.endsWith(".svg") || path.contains(".svg?")) return true
    val seg = u.lastPathSegment?.lowercase().orEmpty()
    if (seg.endsWith(".svg")) return true
    for (key in u.queryParameterNames) {
        val n = key.lowercase()
        val v = u.getQueryParameter(key)?.lowercase().orEmpty()
        if (v == "svg" && (n == "format" || n == "type" || n == "fm")) return true
    }
    return false
}

/**
 * SVG or CoNET IPFS `getFragment` (often `Content-Type: image/svg+xml` without `.svg` suffix).
 * iOS uses WebKit; Android uses Coil + [SvgDecoder] for the same URLs.
 */
internal fun beamioCardImageUrlNeedsSvgCapableLoader(urlString: String): Boolean {
    if (beamioCardImageUrlLooksLikeSvg(urlString)) return true
    val t = urlString.trim()
    if (t.isEmpty()) return false
    val u = try {
        Uri.parse(t)
    } catch (_: Exception) {
        return false
    }
    val host = (u.host ?: "").lowercase()
    val path = (u.path ?: "").lowercase()
    return host.contains("ipfs.conet.network") && path.contains("/api/getfragment")
}

/** Single process-wide loader with SVG decode — used only for tier / Pass artwork URLs. */
private object BeamioCardSvgImageLoaderHolder {
    private val lock = Any()
    @Volatile private var loader: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        loader?.let { return it }
        synchronized(lock) {
            loader?.let { return it }
            val app = context.applicationContext
            val created = ImageLoader.Builder(app)
                .components {
                    add(SvgDecoder.Factory())
                }
                .crossfade(true)
                .build()
            loader = created
            return created
        }
    }
}

/**
 * Coil [AsyncImage] cannot decode SVG without [SvgDecoder]; bitmaps use the app default [Context.imageLoader].
 */
@Composable
internal fun BeamioCardRasterOrSvgImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    fallback: @Composable () -> Unit,
) {
    val trimmed = model?.trim().orEmpty()
    if (trimmed.isEmpty()) {
        fallback()
        return
    }
    val context = LocalContext.current
    val needsSvg = remember(trimmed) { beamioCardImageUrlNeedsSvgCapableLoader(trimmed) }
    val imageLoader = if (needsSvg) {
        BeamioCardSvgImageLoaderHolder.get(context)
    } else {
        context.imageLoader
    }
    SubcomposeAsyncImage(
        model = trimmed,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading -> {
                Box(Modifier.fillMaxSize())
            }
            is AsyncImagePainter.State.Error -> {
                fallback()
            }
            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = alignment,
                )
            }
        }
    }
}
