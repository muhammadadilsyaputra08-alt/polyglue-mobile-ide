package com.polyglue.ide.editor

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyglue.ide.PolyGlueApplication
import com.polyglue.ide.ui.theme.PolyGlueTheme

/**
 * Full-screen activity untuk menjalankan preview game (mode "Play") di luar editor.
 * Menggunakan NativeEngine instance yang sama dengan yang di-init di PolyGlueApplication.
 *
 * Orientasi preview MENGIKUTI pengaturan project (BuildConfigEntity.targetOrientation),
 * dikirim lewat Intent extra [EXTRA_ORIENTATION] oleh pemanggil — BUKAN dikunci di
 * AndroidManifest, karena tiap project bisa punya orientasi berbeda.
 */
class GamePreviewActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ORIENTATION = "orientation"

        /** Helper agar pemanggil (mis. EditorScreen) konsisten membuat Intent-nya. */
        fun createIntent(context: Context, projectId: String, orientation: String): Intent {
            return Intent(context, GamePreviewActivity::class.java).apply {
                putExtra("projectId", projectId)
                putExtra(EXTRA_ORIENTATION, orientation)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Terapkan orientasi SEBELUM setContent, sesuai targetOrientation project.
        // Nilai valid dikirim dari BuildConfigEntity.targetOrientation: "landscape"
        // (default), "portrait", "sensorLandscape", "sensorPortrait".
        requestedOrientation = when (intent.getStringExtra(EXTRA_ORIENTATION)) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "sensorPortrait" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            "sensorLandscape" -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE // default aman untuk game 2D
        }

        enableEdgeToEdge()

        val engine = (application as PolyGlueApplication).engine

        setContent {
            PolyGlueTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: hubungkan ke GLSurfaceView yang memanggil
                        // engine.nativeRenderFrame(deltaTime) per frame.
                        Text(
                            text = "Game Preview",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
