package com.github.anrimian.musicplayer.ui.utils.views.text_view

import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView

class CustomURLSpan(
    private val url: String,
    private val onLinkClick: (url: String) -> Unit
) : URLSpan(url) {

    override fun onClick(widget: View) {
        onLinkClick(url)
    }

    companion object {

        fun TextView.onLinkClick(
            shouldIntercept: (url: String) -> Boolean,
            onInterceptedClick: (url: String) -> Unit
        ) {
            val text = this.text
            if (text !is Spanned) {
                return
            }

            val spannable = SpannableString.valueOf(text)
            val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)

            urlSpans.forEach { urlSpan ->
                val url = urlSpan.url
                if (shouldIntercept(url)) {
                    val start = spannable.getSpanStart(urlSpan)
                    val end = spannable.getSpanEnd(urlSpan)
                    val flags = spannable.getSpanFlags(urlSpan)

                    spannable.removeSpan(urlSpan)

                    val customSpan = CustomURLSpan(url) { interceptedUrl ->
                        onInterceptedClick(interceptedUrl)
                    }
                    spannable.setSpan(customSpan, start, end, flags)
                }
            }
        }
    }

}

