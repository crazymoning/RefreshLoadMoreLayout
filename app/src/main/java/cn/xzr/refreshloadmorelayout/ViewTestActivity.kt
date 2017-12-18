package cn.xzr.refreshloadmorelayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import cn.xzr.refreshloadmorelayout.qq.ViscosityView

class ViewTestActivity : AppCompatActivity(){

    var viscosity: ViscosityView? = null
    private var seek:SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_test)

        viscosity = findViewById(R.id.viscosity)
        seek = findViewById(R.id.seek_bar)

        seek!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viscosity!!.setProgress(progress.toFloat())
            }

        })
    }
}
