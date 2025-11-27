package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

// Constante pentru joc
val BIRD_SIZE = 50.dp
val GRAVITY = 1.dp
val JUMP_STRENGTH = (-150).dp // Ajustat pentru un salt mai fluid
const val GAME_TICK_RATE = 15L // Rata de actualizare a jocului

val PIPE_WIDTH = 80.dp
val PIPE_GAP = 200.dp // Spațiul dintre țevi
val PIPE_SPEED = 5.dp
const val PIPE_SPAWN_INTERVAL = 2000L // Cât de des apar țevi noi (ms)

// Stările jocului
enum class GameState {
    WaitingToStart,
    Playing,
    GameOver
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FlappyBirdGame()
            }
        }
    }
}

// Reprezintă o țeavă (obstacol)
data class Pipe(
    var xOffset: Dp,
    val topHeight: Dp, // Înălțimea părții de sus a țevii
    val id: Int = Random.nextInt() // ID unic pentru a diferenția țevile
) {
    // Returnează o reprezentare a limitelor țevii superioare pentru detectarea coliziunilor
    // density este acum pasat ca parametru
    fun getTopBounds(screenWidth: Dp, density: Density): Rect {
        return with(density) {
            Rect(
                left = xOffset.toPx(),
                top = 0f,
                right = (xOffset + PIPE_WIDTH).toPx(),
                bottom = topHeight.toPx()
            )
        }
    }

    // Returnează o reprezentare a limitelor țevii inferioare pentru detectarea coliziunilor
    // density este acum pasat ca parametru
    fun getBottomBounds(screenWidth: Dp, screenHeight: Dp, density: Density): Rect {
        val bottomPipeY = topHeight + PIPE_GAP
        return with(density) {
            Rect(
                left = xOffset.toPx(),
                top = bottomPipeY.toPx(),
                right = (xOffset + PIPE_WIDTH).toPx(),
                bottom = screenHeight.toPx()
            )
        }
    }
}

@Composable
fun FlappyBirdGame() {
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current // Obținem Density o singură dată aici

    val birdY = remember { Animatable(0.dp, Dp.VectorConverter) }
    var gameState by remember { mutableStateOf(GameState.WaitingToStart) }

    var screenWidthDp by remember { mutableStateOf(0.dp) }
    var screenHeightDp by remember { mutableStateOf(0.dp) }

    val pipes = remember { mutableStateListOf<Pipe>() }
    var score by remember { mutableIntStateOf(0) }
    val passedPipes = remember { mutableStateSetOf<Int>() } // Țevile de care pasărea a trecut deja

    // Lansați logica jocului atunci când starea jocului este "Playing"
    LaunchedEffect(gameState) {
        if (gameState == GameState.Playing) {
            // Resetare stare joc
            birdY.snapTo(0.dp)
            pipes.clear()
            score = 0
            passedPipes.clear()

            // Corutina pentru mișcarea păsării și generarea țevilor
            launch {
                var timeSinceLastPipe = 0L
                while (isActive && gameState == GameState.Playing) {
                    birdY.snapTo(birdY.value + GRAVITY) // Aplică gravitația

                    // Verifică coliziunea cu marginile ecranului
                    val maxNegativeY = -screenHeightDp / 2 + BIRD_SIZE / 2
                    val maxPositiveY = screenHeightDp / 2 - BIRD_SIZE / 2
                    if (birdY.value < maxNegativeY || birdY.value > maxPositiveY) {
                        gameState = GameState.GameOver
                        break
                    }

                    // Generează țevi noi
                    timeSinceLastPipe += GAME_TICK_RATE
                    if (timeSinceLastPipe >= PIPE_SPAWN_INTERVAL) {
                        val minTopHeight = BIRD_SIZE // Asigură că țeava superioară nu e prea mică
                        val maxTopHeight = screenHeightDp - PIPE_GAP - BIRD_SIZE // Asigură că țeava inferioară nu e prea mică
                        val randomTopHeight = with(localDensity) {
                            Random.nextInt(minTopHeight.roundToPx(), maxTopHeight.roundToPx()).toDp()
                        }
                        pipes.add(Pipe(xOffset = screenWidthDp / 2 + PIPE_WIDTH / 2, topHeight = randomTopHeight))
                        timeSinceLastPipe = 0L
                    }

                    // Mișcă țevile și verifică coliziunile
                    // Convertim limitele păsării în pixeli folosind localDensity
                    val birdBounds = with(localDensity) {
                        Rect(
                            left = (screenWidthDp / 2 - BIRD_SIZE / 2).toPx(),
                            top = (screenHeightDp / 2 + birdY.value - BIRD_SIZE / 2).toPx(),
                            right = (screenWidthDp / 2 + BIRD_SIZE / 2).toPx(),
                            bottom = (screenHeightDp / 2 + birdY.value + BIRD_SIZE / 2).toPx()
                        )
                    }


                    val pipesToRemove = mutableListOf<Pipe>()
                    pipes.forEach { pipe ->
                        pipe.xOffset -= PIPE_SPEED

                        // Detectare coliziuni cu țevile
                        // Pasăm localDensity către metodele getTopBounds și getBottomBounds
                        if (pipe.getTopBounds(screenWidthDp, localDensity).overlaps(birdBounds) ||
                            pipe.getBottomBounds(screenWidthDp, screenHeightDp, localDensity).overlaps(birdBounds)
                        ) {
                            gameState = GameState.GameOver
                        }

                        // Verifică dacă pasărea a trecut de țeavă pentru scor
                        if (pipe.xOffset + PIPE_WIDTH < screenWidthDp / 2 - BIRD_SIZE / 2 && !passedPipes.contains(pipe.id)) {
                            score++
                            passedPipes.add(pipe.id)
                        }

                        // Elimină țevile ieșite din ecran
                        if (pipe.xOffset + PIPE_WIDTH < -screenWidthDp / 2) {
                            pipesToRemove.add(pipe)
                        }
                    }
                    pipes.removeAll(pipesToRemove)

                    delay(GAME_TICK_RATE)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEEB)) // Culoare cer
            .clickable {
                when (gameState) {
                    GameState.WaitingToStart -> gameState = GameState.Playing
                    GameState.Playing -> {
                        scope.launch {
                            val maxNegativeY = -screenHeightDp / 2 + BIRD_SIZE / 2
                            birdY.animateTo(
                                targetValue = (birdY.value + JUMP_STRENGTH).coerceAtLeast(maxNegativeY),
                                animationSpec = tween(durationMillis = 150, easing = LinearEasing)
                            )
                        }
                    }
                    GameState.GameOver -> {
                        gameState = GameState.WaitingToStart // Repornește jocul
                    }
                }
            }
            .onSizeChanged { size ->
                screenWidthDp = with(localDensity) { size.width.toDp() }
                screenHeightDp = with(localDensity) { size.height.toDp() }
            },
        contentAlignment = Alignment.Center
    ) {
        // Aici se desenează țevile
        pipes.forEach { pipe ->
            Pipe(
                modifier = Modifier.offset(x = pipe.xOffset),
                topHeight = pipe.topHeight,
                screenHeight = screenHeightDp
            )
        }

        Bird(
            modifier = Modifier
                .offset(y = birdY.value)
                .zIndex(2f)
        )

        // Afișează mesaje în funcție de starea jocului
        when (gameState) {
            GameState.WaitingToStart -> {
                Text(
                    text = "Atinge ecranul pentru a începe!",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.zIndex(3f)
                )
            }
            GameState.Playing -> {
                Text(
                    text = "Scor: $score",
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .zIndex(3f)
                )
            }
            GameState.GameOver -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.zIndex(3f)
                ) {
                    Text(
                        text = "Joc Terminat!",
                        color = Color.Red,
                        fontSize = 36.sp
                    )
                    Text(
                        text = "Scor final: $score",
                        color = Color.White,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Atinge ecranul pentru a reîncerca",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Bird(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(BIRD_SIZE)
            .clip(CircleShape)
            .background(Color(0xFFFFD700)) // Culoare aurie
            .border(2.dp, Color.Red, CircleShape)
    )
}

@Composable
fun Pipe(
    modifier: Modifier = Modifier,
    topHeight: Dp,
    screenHeight: Dp
) {
    // Folosim Layout personalizat pentru a poziționa cele două părți ale țevii
    Layout(
        content = {
            // Partea de sus a țevii
            Box(
                modifier = Modifier
                    .width(PIPE_WIDTH)
                    .height(topHeight)
                    .background(Color(0xFF00FF00)) // Culoare verde pentru țevi
                    .border(2.dp, Color(0xFF006400)) // Bordură verde închis
            )
            // Partea de jos a țevii
            Box(
                modifier = Modifier
                    .width(PIPE_WIDTH)
                    .height(screenHeight - topHeight - PIPE_GAP)
                    .background(Color(0xFF00FF00)) // Culoare verde pentru țevi
                    .border(2.dp, Color(0xFF006400)) // Bordură verde închis
            )
        },
        modifier = modifier
            .width(PIPE_WIDTH)
            .fillMaxHeight()
            .zIndex(1f) // Țevile sunt sub pasăre, dar peste fundal
    ) { measurables, constraints ->
        val topPipe = measurables[0]
        val bottomPipe = measurables[1]

        val placeableTop = topPipe.measure(constraints.copy(maxHeight = topHeight.roundToPx()))
        val placeableBottom = bottomPipe.measure(constraints.copy(maxHeight = (screenHeight - topHeight - PIPE_GAP).roundToPx()))

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeableTop.placeRelative(0, 0)
            placeableBottom.placeRelative(0, (topHeight + PIPE_GAP).roundToPx())
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun FlappyBirdPreview() {
    MyApplicationTheme {
        FlappyBirdGame()
    }
}