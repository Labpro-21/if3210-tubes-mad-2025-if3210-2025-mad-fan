package itb.ac.id.purrytify.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, name = "All Typography Styles")
@Composable
fun AllTypographyPreview() {
    PurrytifyTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypographyItem("Display Large", MaterialTheme.typography.displayLarge)
            TypographyItem("Display Medium", MaterialTheme.typography.displayMedium)
            TypographyItem("Display Small", MaterialTheme.typography.displaySmall)

            TypographyItem("Headline Large", MaterialTheme.typography.headlineLarge)
            TypographyItem("Headline Medium", MaterialTheme.typography.headlineMedium)
            TypographyItem("Headline Small", MaterialTheme.typography.headlineSmall)

            TypographyItem("Title Large", MaterialTheme.typography.titleLarge)
            TypographyItem("Title Medium", MaterialTheme.typography.titleMedium)
            TypographyItem("Title Small", MaterialTheme.typography.titleSmall)

            TypographyItem("Body Large", MaterialTheme.typography.bodyLarge)
            TypographyItem("Body Medium", MaterialTheme.typography.bodyMedium)
            TypographyItem("Body Small", MaterialTheme.typography.bodySmall)

            TypographyItem("Label Large", MaterialTheme.typography.labelLarge)
            TypographyItem("Label Medium", MaterialTheme.typography.labelMedium)
            TypographyItem("Label Small", MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun TypographyItem(name: String, style: androidx.compose.ui.text.TextStyle) {
    Column {
        Text(text = name, style = MaterialTheme.typography.labelSmall)
        Text(text = "The quick brown fox jumps over the lazy dog", style = style)
    }
}
