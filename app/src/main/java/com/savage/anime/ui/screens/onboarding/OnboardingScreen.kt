package com.savage.anime.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savage.anime.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int,
    val emoji: String
)

private val pages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_page1_title,
        descriptionRes = R.string.onboarding_page1_desc,
        emoji = "\uD83C\uDFAC"
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page2_title,
        descriptionRes = R.string.onboarding_page2_desc,
        emoji = "\uD83D\uDD00"
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page3_title,
        descriptionRes = R.string.onboarding_page3_desc,
        emoji = "\u25B6\uFE0F"
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = p.emoji,
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(p.titleRes),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(p.descriptionRes),
                    color = Color(0xFF999999),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                            else Color(0xFF444444)
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(
                    if (pagerState.currentPage < pages.size - 1) R.string.onboarding_next
                    else R.string.onboarding_start
                ),
                color = Color.White,
                fontSize = 16.sp
            )
        }

        if (pagerState.currentPage < pages.size - 1) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onComplete) {
                Text(stringResource(R.string.onboarding_skip), color = Color(0xFF666666), fontSize = 14.sp)
            }
        }
    }
}
