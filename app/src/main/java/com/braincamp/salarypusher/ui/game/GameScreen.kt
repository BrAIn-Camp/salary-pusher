package com.braincamp.salarypusher.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.game.engine.GameViewModel
import com.braincamp.salarypusher.game.engine.SimCoin
import com.braincamp.salarypusher.game.physics.CoinPhysicsConstants
import com.braincamp.salarypusher.game.physics.PhysicsConstants
import com.braincamp.salarypusher.game.physics.PusherConstants
import io.github.sceneview.SceneView
import androidx.compose.ui.graphics.Color as ComposeColor
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.SphereNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader

/**
 * The main game screen — 3D coin pusher with HUD overlay.
 */
@Composable
fun GameScreen(
    onNavigateToEarnings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val coins by gameViewModel.coins.collectAsStateWithLifecycle()
    val pusherZ by gameViewModel.pusherZ.collectAsStateWithLifecycle()
    val queueCount by gameViewModel.dropQueueCount.collectAsStateWithLifecycle()
    val denomination by gameViewModel.currentDenomination.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        CoinPusherScene(
            coins = coins,
            pusherZ = pusherZ,
            onTap = { normalizedX -> gameViewModel.onTap(normalizedX) },
            onFrame = { frameTimeNanos -> gameViewModel.onFrame(frameTimeNanos) },
            modifier = Modifier.fillMaxSize()
        )
        GameHud(
            queueCount = queueCount,
            denomination = denomination,
            onNavigateToEarnings = onNavigateToEarnings,
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3D Scene
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoinPusherScene(
    coins: List<SimCoin>,
    pusherZ: Float,
    onTap: (normalizedX: Float) -> Unit,
    onFrame: (frameTimeNanos: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)

    val platformMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(ComposeColor(red = 0.15f, green = 0.13f, blue = 0.28f))
    }
    val wallMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(ComposeColor(red = 0.2f, green = 0.17f, blue = 0.35f))
    }
    val pegMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(ComposeColor(red = 0.7f, green = 0.7f, blue = 0.75f))
    }
    val pusherMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(ComposeColor(red = 0.4f, green = 0.35f, blue = 0.6f))
    }

    SceneView(
        engine = engine,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val normalizedX = (offset.x / size.width.toFloat()) * 2f - 1f
                onTap(normalizedX)
            }
        },
        onFrame = { frameTimeNanos -> onFrame(frameTimeNanos) }
    ) {
        // Lighting
        LightNode(
            type = com.google.android.filament.LightManager.Type.DIRECTIONAL,
            intensity = 80_000f,
            position = Position(0f, 3f, -1f)
        )

        // Platform floor
        CubeNode(
            materialInstance = platformMaterial,
            size = Size(x = PhysicsConstants.PLATFORM_WIDTH, y = 0.005f, z = PhysicsConstants.PLATFORM_DEPTH),
            position = Position(x = 0f, y = -0.0025f, z = PhysicsConstants.PLATFORM_DEPTH / 2f - 0.05f)
        )

        // Left wall
        CubeNode(
            materialInstance = wallMaterial,
            size = Size(x = PhysicsConstants.WALL_THICKNESS, y = PhysicsConstants.WALL_HEIGHT, z = PhysicsConstants.PLATFORM_DEPTH),
            position = Position(
                x = -(PhysicsConstants.PLATFORM_WIDTH / 2f + PhysicsConstants.WALL_THICKNESS / 2f),
                y = PhysicsConstants.WALL_HEIGHT / 2f,
                z = PhysicsConstants.PLATFORM_DEPTH / 2f - 0.05f
            )
        )

        // Right wall
        CubeNode(
            materialInstance = wallMaterial,
            size = Size(x = PhysicsConstants.WALL_THICKNESS, y = PhysicsConstants.WALL_HEIGHT, z = PhysicsConstants.PLATFORM_DEPTH),
            position = Position(
                x = PhysicsConstants.PLATFORM_WIDTH / 2f + PhysicsConstants.WALL_THICKNESS / 2f,
                y = PhysicsConstants.WALL_HEIGHT / 2f,
                z = PhysicsConstants.PLATFORM_DEPTH / 2f - 0.05f
            )
        )

        // Back wall
        CubeNode(
            materialInstance = wallMaterial,
            size = Size(x = PhysicsConstants.PLATFORM_WIDTH, y = PhysicsConstants.WALL_HEIGHT, z = PhysicsConstants.WALL_THICKNESS),
            position = Position(x = 0f, y = PhysicsConstants.WALL_HEIGHT / 2f, z = PhysicsConstants.PLATFORM_DEPTH - 0.05f)
        )

        // Peg field — staggered rows
        val pegCols = PhysicsConstants.PEG_COLS.toInt()
        val pegRows = PhysicsConstants.PEG_ROWS.toInt()
        val pegStartX = -(pegCols - 1) * PhysicsConstants.PEG_SPACING_X / 2f

        repeat(pegRows) { row ->
            val rowOffset = if (row % 2 == 1) PhysicsConstants.PEG_SPACING_X / 2f else 0f
            repeat(pegCols) { col ->
                CylinderNode(
                    materialInstance = pegMaterial,
                    radius = PhysicsConstants.PEG_RADIUS,
                    height = PhysicsConstants.PEG_HEIGHT,
                    position = Position(
                        x = pegStartX + col * PhysicsConstants.PEG_SPACING_X + rowOffset,
                        y = PhysicsConstants.PEG_FIELD_Y,
                        z = PhysicsConstants.DROP_SPAWN_Z + row * PhysicsConstants.PEG_SPACING_Z
                    )
                )
            }
        }

        // Pusher plate — position driven by simulation
        CubeNode(
            materialInstance = pusherMaterial,
            size = Size(x = PusherConstants.PLATE_WIDTH, y = PusherConstants.PLATE_HEIGHT, z = PusherConstants.PLATE_THICKNESS),
            position = Position(x = 0f, y = PusherConstants.PLATE_HEIGHT / 2f, z = pusherZ)
        )

        // Live coins — one SphereNode per active SimCoin
        coins.forEach { coin ->
            SphereNode(
                materialInstance = materialLoader.createColorInstance(coinComposeColor(coin.denomination)),
                radius = CoinPhysicsConstants.COIN_RADIUS,
                position = Position(x = coin.x, y = coin.y, z = coin.z)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HUD Overlay (Task 5.7)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GameHud(
    queueCount: Int,
    denomination: CoinDenomination,
    onNavigateToEarnings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onNavigateToEarnings) {
                Icon(Icons.Default.BarChart, contentDescription = "Earnings", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🪙 ${denomination.formatCount(queueCount)} ready",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (queueCount > 0) "Tap anywhere to drop" else "Coins drop as you earn them",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun coinComposeColor(denomination: CoinDenomination): ComposeColor =
    when (denomination) {
        CoinDenomination.PENNY   -> ComposeColor(red = 0.72f, green = 0.45f, blue = 0.20f)
        CoinDenomination.NICKEL  -> ComposeColor(red = 0.75f, green = 0.75f, blue = 0.75f)
        CoinDenomination.DIME    -> ComposeColor(red = 0.82f, green = 0.82f, blue = 0.85f)
        CoinDenomination.QUARTER -> ComposeColor(red = 0.78f, green = 0.78f, blue = 0.80f)
        CoinDenomination.DOLLAR  -> ComposeColor(red = 1.0f,  green = 0.84f, blue = 0.0f)
    }
