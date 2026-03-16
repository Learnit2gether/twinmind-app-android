package io.twinmind.app.ui.navigator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.twinmind.app.ui.details.DetailScreen
import io.twinmind.app.ui.details.DetailViewModel
import io.twinmind.app.ui.memories.MemoriesScreen
import io.twinmind.app.ui.memories.MemoriesViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Memories: NavKey
@Serializable
data class Detail(val meetingId: String): NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigatorScreen(viewModel: NavigatorViewModel) {
    val uiModel by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val backstack: SnapshotStateList<NavKey> = remember { mutableStateListOf(Memories) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("TwinMind DashBoard")
            })
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiModel.isRecording) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.stopRecording() },
                        icon = {
                            Icon(Icons.Default.Close, contentDescription = "Stop")
                        }, text = {
                            Text("Stop")
                        }
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.startRecording() },
                        icon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Record")
                        }, text = {
                            Text("Record")
                        }
                    )
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                AppNavigationContent(backstack)
            }
        })
}

@Composable
fun AppNavigationContent(backstack: SnapshotStateList<NavKey> ) {

    val current = backstack.last()

    AnimatedContent(
        targetState = current,
        label = "navigation",
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
        }
    ) { _ ->
        NavDisplay(
            backStack = backstack,
            onBack = { backstack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Memories> {
                    val memoriesViewModel = hiltViewModel<MemoriesViewModel>()
                    MemoriesScreen(memoriesViewModel) { id ->
                        backstack.add(Detail(id))
                    }
                }
                entry<Detail> { key ->
                    val detailViewModel = hiltViewModel<DetailViewModel>()
                    DetailScreen(key.meetingId, detailViewModel)
                }
            })
    }
}


