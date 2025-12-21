// app/src/main/java/id/xetor/app/MidtransPaymentDialog.kt
package id.xetor.app

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class MidtransPaymentDialog(
    private val context: android.content.Context,
    private val redirectUrl: String,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit,
    private val onDismiss: () -> Unit
) {
    private var dialog: Dialog? = null
    
    /**
     * Refresh window attributes untuk memastikan overlay tetap muncul
     * setelah aplikasi kembali dari background
     */
    private fun refreshWindowAttributes() {
        dialog?.window?.apply {
            // Re-apply background transparan
            setBackgroundDrawableResource(android.R.color.transparent)
            
            // Re-apply layout params dengan dim background
            val layoutParams = attributes.apply {
                flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = 0.7f
                format = android.graphics.PixelFormat.TRANSLUCENT
            }
            attributes = layoutParams
        }
    }
    
    fun show() {
        // Get screen dimensions
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Calculate modal size (90% of screen, centered)
        val modalWidth = (screenWidth * 0.9f).toInt()
        val modalHeight = (screenHeight * 0.85f).toInt()
        
        val webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Set background putih untuk menghindari sudut hitam
            setBackgroundColor(Color.WHITE)
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                displayZoomControls = false
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    
                    // Cek jika URL mengandung success indicator
                    url?.let {
                        // Midtrans biasanya redirect ke URL tertentu setelah payment berhasil
                        // Contoh: https://app.sandbox.midtrans.com/snap/v2/vtweb/.../finish
                        if (it.contains("/finish") || it.contains("status_code=200") || it.contains("transaction_status=settlement")) {
                            // Payment berhasil
                            dialog?.dismiss()
                            onSuccess()
                        } else if (it.contains("/error") || it.contains("status_code=400") || it.contains("transaction_status=deny")) {
                            // Payment gagal
                            dialog?.dismiss()
                            onError("Pembayaran gagal")
                        }
                    }
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            
            loadUrl(redirectUrl)
        }
        
        // Create rounded corners drawable untuk background
        val cornerRadiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            context.resources.displayMetrics
        )
        
        val roundedDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = cornerRadiusPx
        }
        
        // Container untuk WebView dengan rounded corners
        val webViewContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Set background putih dengan rounded corners
            background = roundedDrawable
            // Clip children ke rounded corners
            clipToOutline = true
            // Set outline untuk clipping
            outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
                }
            }
            addView(webView)
        }
        
        // Main container - hanya wrapper, tidak perlu background karena sudah di webViewContainer
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                modalWidth,
                modalHeight
            ).apply {
                gravity = Gravity.CENTER
            }
            // Background transparan
            setBackgroundColor(Color.TRANSPARENT)
            addView(webViewContainer)
        }
        
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(mainContainer)
            setCancelable(true)
            setOnCancelListener {
                onDismiss()
            }
            
            window?.apply {
                // Set background transparan sepenuhnya untuk menghilangkan sudut hitam
                setBackgroundDrawableResource(android.R.color.transparent)
                
                // Set layout params untuk modal (centered, dengan margin)
                val layoutParams = WindowManager.LayoutParams().apply {
                    width = modalWidth
                    height = modalHeight
                    gravity = Gravity.CENTER
                    flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    dimAmount = 0.7f // 70% opacity untuk overlay
                    format = android.graphics.PixelFormat.TRANSLUCENT
                }
                setAttributes(layoutParams)
            }
        }
        
        // Tambahkan listener untuk refresh window attributes saat dialog di-resume
        // Ini mengatasi masalah overlay hilang saat aplikasi masuk background
        var lifecycleObserver: androidx.lifecycle.LifecycleEventObserver? = null
        
        if (context is androidx.lifecycle.LifecycleOwner) {
            val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
            
            // Gunakan Activity lifecycle untuk refresh window saat resume
            lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    // Refresh window attributes saat activity resume
                    // Delay sedikit untuk memastikan window sudah siap
                    mainContainer.post {
                        refreshWindowAttributes()
                    }
                }
            }
            
            // Register observer ke activity lifecycle
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        }
        
        // Set dismiss listener untuk cleanup
        val finalObserver = lifecycleObserver
        dialog?.setOnDismissListener {
            if (context is androidx.lifecycle.LifecycleOwner && finalObserver != null) {
                (context as androidx.lifecycle.LifecycleOwner).lifecycle.removeObserver(finalObserver)
            }
            onDismiss()
        }
        
        dialog?.show()
        
        // Refresh window attributes setelah dialog ditampilkan
        // Ini memastikan overlay muncul dengan benar
        mainContainer.post {
            refreshWindowAttributes()
        }
    }
    
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}

