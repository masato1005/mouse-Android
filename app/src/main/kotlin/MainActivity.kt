package com.momos.mouseandroid

import MouseViewModel
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: MouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMouseContent(viewModel, viewModel)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val enabled = isAccessibilityServiceEnabled(MouseAccessibilityService::class.java)

        viewModel.refreshAccessibilityState(enabled)
        if (enabled) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("ユーザー補助サービスについて")
            .setMessage(
                "PCから受信したマウス操作を、端末上のタップや" +
                        "スワイプとして実行するために使用します。" +
                        "\n\nダウンロードしたアプリ → Mouse Android操作サービス → サービスを使用"
            )
            .setNegativeButton("キャンセル", null)
            .setPositiveButton("設定を開く") { _, _ ->
                openAccessibilitySettings()
            }
            .show()
    }
}
