// app/src/main/java/id/xetor/app/ui/components/MarkdownContent.kt
package id.xetor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xetor.app.ui.theme.GreenPrimary

@Composable
fun MarkdownContent(content: String) {
    val lines = content.lines()
    var i = 0
    var isInBulletList = false

    while (i < lines.size) {
        val line = lines[i].trim()

        when {
            // Title besar (hijau) - # Title
            line.startsWith("# ") && !line.startsWith("##") -> {
                val title = line.removePrefix("# ").trim()
                if (title.isNotEmpty()) {
                    if (isInBulletList) {
                        Spacer(modifier = Modifier.height(8.dp))
                        isInBulletList = false
                    }
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            // Subjudul (bold kecil) - ## Subtitle
            line.startsWith("## ") -> {
                val subtitle = line.removePrefix("## ").trim()
                if (subtitle.isNotEmpty()) {
                    if (isInBulletList) {
                        Spacer(modifier = Modifier.height(8.dp))
                        isInBulletList = false
                    }
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
            }
            // Bullet list - - Item
            line.startsWith("- ") -> {
                val bulletText = line.removePrefix("- ").trim()
                if (bulletText.isNotEmpty()) {
                    if (!isInBulletList) {
                        isInBulletList = true
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 8.dp),
                            lineHeight = 22.sp
                        )
                        // Parse bold text dalam bullet
                        val annotatedText = parseBoldText(bulletText)
                        Text(
                            text = annotatedText,
                            fontSize = 14.sp,
                            color = Color.Black,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // Paragraf biasa
            line.isNotEmpty() -> {
                if (isInBulletList) {
                    Spacer(modifier = Modifier.height(8.dp))
                    isInBulletList = false
                }
                // Parse bold text (**text**)
                val annotatedText = parseBoldText(line)
                Text(
                    text = annotatedText,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }
            // Empty line - tidak perlu spacing untuk paragraf dengan enter
            line.isEmpty() && isInBulletList -> {
                // Don't add spacing inside bullet list
            }
            line.isEmpty() -> {
                // Tidak perlu spacing untuk empty line (paragraf dengan enter akan otomatis terpisah dengan line height)
            }
        }

        i++
    }
}

@Composable
private fun parseBoldText(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0

        while (currentIndex < text.length) {
            val boldStart = text.indexOf("**", currentIndex)
            if (boldStart == -1) {
                // No more bold markers, add remaining text
                append(text.substring(currentIndex))
                break
            } else {
                // Add text before bold
                append(text.substring(currentIndex, boldStart))
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd == -1) {
                    // No closing marker, add as normal text
                    append(text.substring(boldStart))
                    break
                } else {
                    // Add bold text
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(boldStart + 2, boldEnd))
                    }
                    currentIndex = boldEnd + 2
                }
            }
        }
    }
}

