package com.app.market.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.market.resources.Res
import com.app.market.resources.cancel
import com.app.market.resources.confirm
import com.app.market.resources.theme_page_scale
import com.app.market.resources.theme_page_scale_range
import com.app.market.ui.theme.MarketShapeTokens
import org.jetbrains.compose.resources.stringResource

/**
 * M3E 对话框容器：28dp 大圆角、`surfaceContainerHigh` 容器色、内容超长时可滚动。
 */
@Composable
fun MarketDialog(
    show: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(MarketShapeTokens.SheetCorner),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!summary.isNullOrBlank()) {
                    Text(
                        text = summary,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(20.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        content()
                    }
                }
            }
        }
    }
}

/** 标准「确认 / 取消」对话框。 */
@Composable
fun MarketConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String? = null,
    destructive: Boolean = false,
) {
    MarketDialog(
        show = show,
        title = title,
        summary = message,
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            if (dismissText != null) {
                AppTextButton(
                    text = dismissText,
                    onClick = onDismiss,
                )
            }
            AppButton(
                text = confirmText,
                onClick = onConfirm,
                colors = if (destructive) {
                    AppButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                } else {
                    AppButtonDefaults.filled()
                },
            )
        }
    }
}

/** 页面缩放比例设置对话框。 */
@Composable
fun ScaleDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    scaleProvider: () -> Float,
    onScaleChange: (Float) -> Unit,
) {
    var text by remember(show) { mutableStateOf((scaleProvider() * 100).toInt().toString()) }
    MarketDialog(
        show = show,
        title = stringResource(Res.string.theme_page_scale),
        summary = stringResource(Res.string.theme_page_scale_range),
        onDismissRequest = onDismissRequest,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all(Char::isDigit)) text = newValue
            },
            singleLine = true,
            suffix = { Text("%") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppTextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.cancel),
                onClick = onDismissRequest,
            )
            Spacer(Modifier.width(4.dp))
            AppButton(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.confirm),
                onClick = {
                    val value = text.toIntOrNull()?.coerceIn(80, 110)
                        ?: (scaleProvider() * 100).toInt()
                    onScaleChange(value / 100f)
                    onDismissRequest()
                },
            )
        }
    }
}
