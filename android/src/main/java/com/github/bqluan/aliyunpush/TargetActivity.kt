package com.github.bqluan.aliyunpush

import com.github.bqluan.aliyunpush.R
import android.os.Bundle
import android.util.Log
import android.app.Activity
import android.content.Intent
import org.json.JSONObject
import com.alibaba.sdk.android.push.popup.OnPushParseFailedListener
import com.alibaba.sdk.android.push.popup.PopupNotifyClick
import com.alibaba.sdk.android.push.popup.PopupNotifyClickListener
import android.widget.Toast
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.os.Handler
import android.os.Looper

class TargetActivity : Activity(), PopupNotifyClickListener, OnPushParseFailedListener {
    private val mPopupNotifyClick = PopupNotifyClick(this)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
        }
        val scale = resources.displayMetrics.density
        val imageSizeInPx = (100 * scale + 0.5f).toInt()
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.splashlogo)
            layoutParams = LinearLayout.LayoutParams(imageSizeInPx, imageSizeInPx)
        }

        rootLayout.addView(imageView)
        setContentView(rootLayout)

        mPopupNotifyClick.onCreate(this, intent)

        val title = intent?.getStringExtra("notification_title")
        val content = intent?.getStringExtra("notification_content")
        if (title != null && content != null) {
            handler.postDelayed({
                val fallbackIntent =
                    Intent(this, Class.forName("org.ccpit.dendenmushi.MainActivity"))
                fallbackIntent.putExtra("title", title)
                fallbackIntent.putExtra("content", content)
                startActivity(fallbackIntent)
                finish()
            }, 300)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mPopupNotifyClick.onNewIntent(intent)
    }

    /**
     * 实现通知打开回调方法，获取通知相关信息
     * @param title     标题
     * @param summary   内容
     * @param extMap    额外参数
     */
    override fun onSysNoticeOpened(title: String, summary: String, extMap: Map<String, String>) {
        Log.d("onSysNoticeOpened", "title: $title, summary: $summary, extMap: $extMap")
        handler.postDelayed({
            val fallbackIntent = Intent(this, Class.forName("org.ccpit.dendenmushi.MainActivity"))
            fallbackIntent.putExtra("title", title)
            fallbackIntent.putExtra("content", summary)
            startActivity(fallbackIntent)
            finish()
        }, 300)
    }

    /**
     * 不是推送数据的回调
     *
     * @param intent
     */
    override fun onNotPushData(intent: Intent) {
        //TODO 没有推送数据，可能是异常调用，需要异常处理
    }

    /**
     * 是推送数据，但是又解密失败时的回调
     *
     * @param intent
     */
    override fun onParseFailed(intent: Intent) {
        //TODO 推送数据解密异常，需要异常处理
    }
}
