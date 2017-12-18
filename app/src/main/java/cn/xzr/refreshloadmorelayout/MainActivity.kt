package cn.xzr.refreshloadmorelayout

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.xzr.refreshloadmorelayout.qq.QQRefreshActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        open_qq_refresh.setOnClickListener {
            startActivity(Intent(this@MainActivity,
                    QQRefreshActivity::class.java))
        }

        open_default_refresh.setOnClickListener {
            startActivity(Intent(this@MainActivity,DefaultActivity::class.java))
        }


    }
}
