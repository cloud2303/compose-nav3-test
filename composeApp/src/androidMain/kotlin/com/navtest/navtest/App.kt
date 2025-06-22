package com.navtest.navtest

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import navtest.composeapp.generated.resources.Res
import navtest.composeapp.generated.resources.compose_multiplatform

@Serializable
private data object RouteA : NavKey

@Serializable private data object Home : NavKey
@Serializable private data object ChatList : NavKey
@Serializable private data object Camera : NavKey
@Serializable private data object ChatDetail : NavKey
@Serializable private data class RouteB(val id: String) : NavKey

val tabRoutes = listOf(Home, ChatList, Camera)

@Serializable
data class User(val name: String, val age: Int)


@Composable
@Preview
fun App() {
    val backStack = rememberNavBackStack(Home)
    val currentTab = tabRoutes.find { it == backStack.lastOrNull() } ?: Home
  LaunchedEffect(currentTab) {
      val user = User(name = "Alice", age = 28)
      val jsonString = Json.encodeToString(user)
      Log.d("NAV3",jsonString)
  }


    NavDisplay(
        backStack=backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            // TAB 层
            entry<Home> {
                Scaffold(bottomBar = {
                    BottomBar(currentTab) { selected ->
                        backStack.clear()
                        backStack.add(selected)
                    }
                }) {innerPadding->
                    ContentRed("Home", modifier = Modifier.padding(innerPadding))
                }
            }
            entry<ChatList> {
                Scaffold(bottomBar = {
                    BottomBar(currentTab) { selected ->
                        backStack.clear()
                        backStack.add(selected)
                    }
                }) {innerPadding->
                    ContentGreen("ChatList", modifier = Modifier.padding(innerPadding)) {
                        Button(onClick = { backStack.add(ChatDetail) }) {
                            Text("Go to ChatDetail")
                        }
                    }
                }
            }
            entry<Camera> {
                Scaffold(bottomBar = {
                    BottomBar(currentTab) { selected ->
                        backStack.clear()
                        backStack.add(selected)
                    }
                }) {
                    innerPadding->
                    ContentPurple("Camera", modifier = Modifier.padding(innerPadding))
                }
            }

            // 非 TAB 层
            entry<ChatDetail>(
                metadata = NavDisplay.transitionSpec {
                    // Slide new content up, keeping the old content in place underneath
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300)
                    ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                } + NavDisplay.popTransitionSpec {
                    // Slide old content down, revealing the new content in place underneath
                    EnterTransition.None togetherWith
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(300)
                            )
                } + NavDisplay.predictivePopTransitionSpec {
                    // Slide old content down, revealing the new content in place underneath
                    EnterTransition.None togetherWith
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(300)
                            )
                }
            ) {
                ContentBlue("Chat Detail")
            }

            entry<RouteB> { key ->
                ContentBlue("RouteB: ${key.id}")
            }
        }
    )

}
@Composable
fun BottomBar(
    selected: NavKey,
    onSelect: (NavKey) -> Unit
) {
    NavigationBar {
        tabRoutes.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    val icon = when (tab) {
                        is Home -> Icons.Default.Home
                        is ChatList -> Icons.Default.Face
                        is Camera -> Icons.Default.PlayArrow
                        else -> Icons.Default.Home
                    }
                    Icon(imageVector = icon, contentDescription = null)
                }
            )
        }
    }
}
class TopLevelBackStack<T:Any>(startKey: T){
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )
    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T){

        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null){
            topLevelStacks.put(key, mutableStateListOf(key))
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }
    fun add(key: T){
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }
    fun removeLast(){
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}


