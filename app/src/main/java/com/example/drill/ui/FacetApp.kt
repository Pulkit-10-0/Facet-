package com.example.drill.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.drill.ArDrillActivity
import com.example.drill.domain.AppContainer
import com.example.drill.ui.screens.ChatScreen
import com.example.drill.ui.screens.HomeScreen
import com.example.drill.ui.screens.ModelSetupScreen
import com.example.drill.ui.screens.ProfileOverviewScreen
import com.example.drill.ui.screens.ScanScreen
import com.example.drill.ui.screens.SplashScreen
import com.example.drill.ui.viewmodel.ChatViewModel
import com.example.drill.ui.viewmodel.HomeViewModel
import com.example.drill.ui.viewmodel.ModelSetupViewModel
import com.example.drill.ui.viewmodel.ProfileViewModel
import com.example.drill.ui.viewmodel.ScanViewModel
import kotlinx.coroutines.launch

@Composable
fun FacetApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = remember { AppContainer(context) }
    var screen: Screen by remember { mutableStateOf(Screen.Splash) }
    val uiScope = rememberCoroutineScope()

    val deepLink by DeepLinkBus.deepLink.collectAsState()
    LaunchedEffect(deepLink) {
        val link = DeepLinkBus.consume() ?: return@LaunchedEffect
        screen = Screen.Chat(facetId = link.facetId, initialMessage = link.initialMessage)
    }

    Surface(modifier = modifier, color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
        when (val s = screen) {
            Screen.Splash -> {
                SplashScreen(
                        onRoute = { hasModel ->
                            screen = if (hasModel) Screen.Home else Screen.ModelSetup
                        }
                )
            }
            Screen.ModelSetup -> {
                val vm = remember { ModelSetupViewModel() }
                ModelSetupScreen(viewModel = vm, onReady = { screen = Screen.Home })
            }
            Screen.Home -> {
                val vm = remember { HomeViewModel(container.facetRepository) }
                HomeScreen(
                        viewModel = vm,
                        onScan = { screen = Screen.Scan() },
                        onOpenFacet = { id -> screen = Screen.ProfileOverview(id) }
                )
            }
            is Screen.Scan -> {
                val vm = remember { ScanViewModel(container.facetRepository) }
                LaunchedEffect(s.prefillUrl) {
                    if (s.prefillUrl.isNotBlank()) vm.setUrl(s.prefillUrl)
                }
                ScanScreen(
                        viewModel = vm,
                        onBack = { screen = Screen.Home },
                        onImported = { entity -> screen = Screen.ProfileOverview(entity.id) }
                )
            }
            is Screen.ProfileOverview -> {
                val vm = remember { ProfileViewModel(container.facetRepository) }
                ProfileOverviewScreen(
                        facetId = s.facetId,
                        viewModel = vm,
                        onBack = { screen = Screen.Home },
                        onOpenAr = { glbPath ->
                            val intent =
                                    Intent(context, ArDrillActivity::class.java).apply {
                                        putExtra(ArDrillActivity.EXTRA_GLB_PATH, glbPath)
                                        putExtra(ArDrillActivity.EXTRA_FACET_ID, s.facetId)
                                    }
                            context.startActivity(intent)
                        },
                        onTalk = { screen = Screen.Chat(s.facetId) },
                        onDelete = {
                            val id = vm.entity.value?.id
                            if (id != null)
                                    uiScope.launch { container.facetRepository.deleteFacet(id) }
                            screen = Screen.Home
                        }
                )
            }
            is Screen.Chat -> {
                val vm = remember { ChatViewModel(container.facetRepository) }
                BackHandler { screen = Screen.ProfileOverview(s.facetId) }
                ChatScreen(
                        facetId = s.facetId,
                        initialMessage = s.initialMessage,
                        viewModel = vm,
                        onBack = { screen = Screen.ProfileOverview(s.facetId) }
                )
            }
        }
    }
}
