package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VeyroxCodeTextStyle
import com.example.ui.theme.VeyroxPrimaryBlue
import com.example.ui.theme.VeyroxTheme
import kotlinx.coroutines.delay

private sealed interface MarkdownBlock {
    data class TextBlock(val lines: List<String>) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
}

@Composable
fun MarkdownMessageView(
    markdown: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    val colors = VeyroxTheme.colors

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(
                        language = block.language,
                        code = block.code
                    )
                }

                is MarkdownBlock.TextBlock -> {
                    block.lines.forEach { rawLine ->
                        val line = rawLine.trimEnd()
                        when {
                            line.isBlank() -> {
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            line.startsWith("### ") -> {
                                Text(
                                    text = parseInlineMarkdown(
                                        line.removePrefix("### "),
                                        colors.primaryBlue,
                                        colors.codeBackground
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = textColor
                                )
                            }

                            line.startsWith("## ") -> {
                                Text(
                                    text = parseInlineMarkdown(
                                        line.removePrefix("## "),
                                        colors.primaryBlue,
                                        colors.codeBackground
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = textColor
                                )
                            }

                            line.startsWith("# ") -> {
                                Text(
                                    text = parseInlineMarkdown(
                                        line.removePrefix("# "),
                                        colors.primaryBlue,
                                        colors.codeBackground
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = textColor
                                )
                            }

                            line.startsWith("> ") -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primaryBlue.copy(alpha = 0.08f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(18.dp)
                                            .background(colors.primaryBlue, RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        text = parseInlineMarkdown(
                                            line.removePrefix("> "),
                                            colors.primaryBlue,
                                            colors.codeBackground
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            line.startsWith("- ") || line.startsWith("* ") -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.brightBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = parseInlineMarkdown(
                                            line.drop(2),
                                            colors.primaryBlue,
                                            colors.codeBackground
                                        ),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textColor
                                    )
                                }
                            }

                            else -> {
                                Text(
                                    text = parseInlineMarkdown(
                                        line,
                                        colors.primaryBlue,
                                        colors.codeBackground
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(
    language: String,
    code: String
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1800L)
            copied = false
        }
    }

    val highlighted = remember(code) { highlightSyntax(code) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0A0E17),
        border = BorderStroke(1.dp, Color(0xFF243049))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121826))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7CA9FF)
                )
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = if (copied) "Code copied" else "Copy code",
                        tint = if (copied) Color(0xFF22C55E) else Color(0xFFA0A9B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = highlighted,
                    style = VeyroxCodeTextStyle,
                    color = Color(0xFFE6EDF7)
                )
            }
        }
    }
}

private fun parseMarkdownBlocks(input: String): List<MarkdownBlock> {
    val lines = input.replace("\r\n", "\n").split("\n")
    val result = mutableListOf<MarkdownBlock>()
    val currentText = mutableListOf<String>()
    val currentCode = mutableListOf<String>()
    var inCode = false
    var codeLang = ""

    for (line in lines) {
        val trimmed = line.trimStart()
        if (trimmed.startsWith("```")) {
            if (!inCode) {
                if (currentText.isNotEmpty()) {
                    result.add(MarkdownBlock.TextBlock(currentText.toList()))
                    currentText.clear()
                }
                codeLang = trimmed.removePrefix("```").trim()
                inCode = true
            } else {
                result.add(
                    MarkdownBlock.CodeBlock(
                        language = codeLang,
                        code = currentCode.joinToString("\n")
                    )
                )
                currentCode.clear()
                codeLang = ""
                inCode = false
            }
        } else if (inCode) {
            currentCode.add(line)
        } else {
            currentText.add(line)
        }
    }

    if (inCode && currentCode.isNotEmpty()) {
        result.add(
            MarkdownBlock.CodeBlock(
                language = codeLang,
                code = currentCode.joinToString("\n")
            )
        )
    } else if (currentText.isNotEmpty()) {
        result.add(MarkdownBlock.TextBlock(currentText.toList()))
    }

    return result
}

private fun parseInlineMarkdown(
    text: String,
    accentColor: Color,
    inlineCodeBg: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.5.sp,
                                color = Color(0xFF8EC5FF),
                                background = inlineCodeBg.copy(alpha = 0.65f)
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1 && end > i + 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

private val codeKeywords = setOf(
    "fun", "val", "var", "class", "interface", "object", "return", "if", "else",
    "when", "for", "while", "suspend", "private", "public", "override", "import",
    "package", "data", "sealed", "enum", "true", "false", "null", "const", "let",
    "function", "async", "await", "export", "from", "def", "try", "catch", "throw"
)

private fun highlightSyntax(code: String): AnnotatedString {
    val keywordColor = Color(0xFF5A95FF)
    val stringColor = Color(0xFF7CE38B)
    val commentColor = Color(0xFF6E7A91)
    val numberColor = Color(0xFFF6B756)
    val defaultColor = Color(0xFFE6EDF7)

    return buildAnnotatedString {
        val lines = code.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val trimmed = line.trimStart()
            if (trimmed.startsWith("//") || trimmed.startsWith("#")) {
                withStyle(SpanStyle(color = commentColor, fontStyle = FontStyle.Italic)) {
                    append(line)
                }
            } else {
                var i = 0
                while (i < line.length) {
                    val ch = line[i]
                    when {
                        ch == '"' || ch == '\'' -> {
                            val quote = ch
                            var j = i + 1
                            while (j < line.length && line[j] != quote) {
                                if (line[j] == '\\' && j + 1 < line.length) j++
                                j++
                            }
                            val end = if (j < line.length) j + 1 else line.length
                            withStyle(SpanStyle(color = stringColor)) {
                                append(line.substring(i, end))
                            }
                            i = end
                        }

                        ch.isDigit() -> {
                            var j = i
                            while (j < line.length && (line[j].isDigit() || line[j] == '.')) j++
                            withStyle(SpanStyle(color = numberColor)) {
                                append(line.substring(i, j))
                            }
                            i = j
                        }

                        ch.isLetter() || ch == '_' -> {
                            var j = i
                            while (j < line.length && (line[j].isLetterOrDigit() || line[j] == '_')) j++
                            val word = line.substring(i, j)
                            val color = if (word in codeKeywords) keywordColor else defaultColor
                            val weight = if (word in codeKeywords) FontWeight.SemiBold else FontWeight.Normal
                            withStyle(SpanStyle(color = color, fontWeight = weight, fontFamily = FontFamily.Monospace)) {
                                append(word)
                            }
                            i = j
                        }

                        else -> {
                            withStyle(SpanStyle(color = defaultColor)) {
                                append(ch)
                            }
                            i++
                        }
                    }
                }
            }
            if (lineIdx < lines.lastIndex) append("\n")
        }
    }
}
