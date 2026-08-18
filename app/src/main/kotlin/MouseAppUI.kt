package com.momos.mouseandroid

import EventType.SearchButtonType
import MouseViewModel
import interfaces.UIListener
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
fun ComponentActivity.setMouseContent(listener: UIListener, viewModel: MouseViewModel) {
    val density = resources.displayMetrics.density
    val padding = (24 * density).toInt()
    val buttonTopMargin = (16 * density).toInt()

    val statusText = TextView(this).apply {
        text = "PCに接続されていません"
        textSize = 18f
        setTextColor(Color.DKGRAY)
        gravity = Gravity.CENTER
    }

    val searchButton = Button(this).apply {
        setOnClickListener {
            if (!isAccessibilityServiceEnabled(MouseAccessibilityService::class.java)) {
                AlertDialog.Builder(this@setMouseContent)
                    .setTitle("ユーザー補助サービスについて")
                    .setMessage(
                        "PCから受信したマウス操作を、端末上のタップや" +
                                "スワイプとして実行するために使用します。" +
                                "\n\nダウンロードしたアプリ → Mouse Android操作サービス → サービスを使用"
                    )
                    .setNegativeButton("キャンセル", null)
                    .setPositiveButton("設定を開く") { _, _ ->
                        startActivity(
                            Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                            )
                        )
                    }
                    .show()
            } else {
                listener.ClickedSearchiButton()
            }
        }
    }

    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(padding, padding, padding, padding)
        addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        addView(
            searchButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = buttonTopMargin
                gravity = Gravity.CENTER_HORIZONTAL
            },
        )
    }
    setContentView(content)

    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.connectStatus.collect { connectStatus ->
                when (connectStatus) {
                    SearchButtonType.IDLING -> {
                        if (isAccessibilityServiceEnabled(MouseAccessibilityService::class.java)) {
                            searchButton.text = "検索"
                        } else {
                            searchButton.text = "設定"
                        }
                    }

                    SearchButtonType.SEARCHING -> {
                        searchButton.text = "中止"
                        statusText.text = "検索中"
                    }

                    SearchButtonType.STOP -> {
                        searchButton.text = "検索"
                        statusText.text = "サーバーを停止しました"
                    }

                    SearchButtonType.SUCCESS -> {
                        searchButton.text = "切断"
                        statusText.text = "接続に成功しました"

                    }
                }
            }
        }
    }
}


