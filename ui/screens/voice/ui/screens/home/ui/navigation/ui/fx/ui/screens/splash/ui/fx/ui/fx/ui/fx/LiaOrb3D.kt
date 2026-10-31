package com.Lia.assistant.ui.fx

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.shaders.RuntimeShader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.Lia.assistant.ui.theme.NovaThemeTokens
import com.Lia.assistant.voice.NovaOrbState
import kotlin.math.sin

@Composable
fun LiaOrb3D(
    state: NovaOrbState,
    size: Dp,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reducedMotion = NovaThemeTokens.reducedMotion

    // Tilt sensor state
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    DisposableEffect(context, reducedMotion) {
        if (reducedMotion) {
            onDispose {}
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                        tiltX = event.values[0] * 0.1f
                        tiltY = event.values[1] * 0.1f
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            accel?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Orb3D")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    val activeTime = if (reducedMotion) 0f else time

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LiaAGSLOrb(state, size, amplitude, activeTime, tiltX, tiltY, modifier)
    } else {
        NovaOrb(state = state, style = "NOVA", size = size, amplitude = amplitude, modifier = modifier)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LiaAGSLOrb(
    state: NovaOrbState,
    size: Dp,
    amplitude: Float,
    time: Float,
    tiltX: Float,
    tiltY: Float,
    modifier: Modifier
) {
    val shaderSource = """
        uniform float uTime;
        uniform vec2 uResolution;
        uniform vec3 uCoreColor;
        uniform vec3 uRimColor;
        uniform float uAmplitude;
        uniform vec2 uTilt;

        vec4 main(vec2 fragCoord) {
            vec2 uv = (fragCoord - 0.5 * uResolution) / min(uResolution.x, uResolution.y);
            float dist = length(uv);
            float radius = 0.4 + uAmplitude * 0.15;

            if (dist > radius + 0.1) {
                return vec4(0.0);
            }

            // Analytic sphere normal
            float z = sqrt(max(0.0, radius * radius - uv.x * uv.x - uv.y * uv.y));
            vec3 normal = normalize(vec3(uv, z));

            // Light direction with tilt
            vec3 lightDir = normalize(vec3(0.5 + uTilt.x, 0.5 + uTilt.y, 0.8));
            float diff = max(0.0, dot(normal, lightDir));

            // Fresnel rim
            float fresnel = pow(1.0 - max(0.0, normal.z), 3.0);

            vec3 color = mix(uCoreColor, uRimColor, diff) + fresnel * uRimColor * 1.5;
            float alpha = smoothstep(radius + 0.1, radius - 0.05, dist);

            return vec4(color * alpha, alpha);
        }
    """.trimIndent()

    val runtimeShader = remember { RuntimeShader(shaderSource) }

    val (coreCol, rimCol) = when (state) {
        NovaOrbState.IDLE -> Pair(Color(0xFFFF6FAE), Color(0xFF8B5CF6))
        NovaOrbState.LISTENING -> Pair(Color(0xFF3FE0D0), Color(0xFF155E75))
        NovaOrbState.THINKING -> Pair(Color(0xFF8B5CF6), Color(0xFF3FE0D0))
        NovaOrbState.SPEAKING -> Pair(Color(0xFFFFB547), Color(0xFFFF6FAE))
        NovaOrbState.CONNECTING -> Pair(Color(0xFFF59E0B), Color(0xFFB45309))
        NovaOrbState.ERROR -> Pair(Color(0xFFFF7A85), Color(0xFF991B1B))
    }

    Canvas(modifier = modifier.size(size)) {
        runtimeShader.setFloatUniform("uTime", time)
        runtimeShader.setFloatUniform("uResolution", size.toPx(), size.toPx())
        runtimeShader.setFloatUniform("uCoreColor", coreCol.red, coreCol.green, coreCol.blue)
        runtimeShader.setFloatUniform("uRimColor", rimCol.red, rimCol.green, rimCol.blue)
        runtimeShader.setFloatUniform("uAmplitude", amplitude)
        runtimeShader.setFloatUniform("uTilt", tiltX, tiltY)

        drawRect(brush = ShaderBrush(runtimeShader))
    }
}
