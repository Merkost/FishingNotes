package com.mobileprism.fishing.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.ui.utils.AnimatedResource
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class OnboardingPageData(
    val titleRes: StringResource,
    val descRes: StringResource,
    val lottieRes: String,
    val gradientColors: List<Color>,
    val buttonTextRes: StringResource,
)

private val pages = listOf(
    OnboardingPageData(
        titleRes = Res.string.onboarding_title_1,
        descRes = Res.string.onboarding_desc_1,
        lottieRes = "marker",
        gradientColors = listOf(Color(0xFFA5D6A7), Color(0xFF2E7D32)),
        buttonTextRes = Res.string.onboarding_next,
    ),
    OnboardingPageData(
        titleRes = Res.string.onboarding_title_2,
        descRes = Res.string.onboarding_desc_2,
        lottieRes = "walking_fish",
        gradientColors = listOf(Color(0xFF90CAF9), Color(0xFF1565C0)),
        buttonTextRes = Res.string.onboarding_next,
    ),
    OnboardingPageData(
        titleRes = Res.string.onboarding_title_3,
        descRes = Res.string.onboarding_desc_3,
        lottieRes = "clouds",
        gradientColors = listOf(Color(0xFFFFE082), Color(0xFFE65100)),
        buttonTextRes = Res.string.onboarding_get_started,
    ),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            OnboardingPage(
                data = pages[pageIndex],
                isCurrentPage = pagerState.currentPage == pageIndex,
                onButtonClick = {
                    if (pageIndex < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(top = 8.dp, end = 8.dp),
        ) {
            TextButton(onClick = onFinished) {
                Text(
                    text = stringResource(Res.string.onboarding_skip),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        OnboardingPageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = pages.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(bottom = 120.dp),
        )
    }
}

@Composable
private fun OnboardingPage(
    data: OnboardingPageData,
    isCurrentPage: Boolean,
    onButtonClick: () -> Unit,
) {
    val titleAlpha by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0f,
        animationSpec = tween(400),
    )
    val descAlpha by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0f,
        animationSpec = tween(400, delayMillis = 100),
    )
    val titleOffsetY by animateDpAsState(
        targetValue = if (isCurrentPage) 0.dp else 30.dp,
        animationSpec = tween(400),
    )
    val descOffsetY by animateDpAsState(
        targetValue = if (isCurrentPage) 0.dp else 20.dp,
        animationSpec = tween(400, delayMillis = 100),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(data.gradientColors)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedResource(
                    resName = data.lottieRes,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(data.titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAlpha
                        translationY = titleOffsetY.toPx()
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(data.descRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            alpha = descAlpha
                            translationY = descOffsetY.toPx()
                        },
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = data.gradientColors.last(),
                    ),
                ) {
                    Text(
                        text = stringResource(data.buttonTextRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun OnboardingPageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(300),
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                animationSpec = tween(300),
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
