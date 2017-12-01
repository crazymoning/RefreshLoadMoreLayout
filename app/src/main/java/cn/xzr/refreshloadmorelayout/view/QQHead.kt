package cn.xzr.refreshloadmorelayout.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import cn.xzr.library.IRefresh
import cn.xzr.refreshloadmorelayout.R

/**
 * Created by xzr on 2017/11/28.
 */

class QQHead : IRefresh {

    private var viscosity: ViscosityView? = null
    private var progressbar: ProgressBar? = null

    override fun getView(inflater: LayoutInflater, viewGroup: ViewGroup): View? {
        val v = inflater.inflate(R.layout.qq_head_view, viewGroup, true)
        viscosity = v.findViewById(R.id.viscosity_view)
        progressbar = v.findViewById(R.id.progress_bar)
        return v
    }

    override fun beforeRefresh(v: View, progress: Int) {
        if (progress < 50) {
            viscosity!!.top = -50 + progress
            v.requestLayout()
        } else {
            viscosity!!.setProgress((progress - 50) * 2.toFloat())
        }


    }

    override fun onChangeType(v: View, state: Int) {
        when (state) {
            IRefresh.TYPE_UP -> {
                viscosity!!.visibility = View.VISIBLE
                progressbar!!.visibility = View.GONE
                viscosity!!.setProgress(0f)
            }
            IRefresh.TYPE_FINISH -> progressbar!!.visibility = View.VISIBLE
            IRefresh.TYPE_START_REFRESH -> {
                viscosity!!.visibility = View.GONE
                progressbar!!.visibility = View.VISIBLE
            }
        }
    }

    override fun getRefreshHeight(): Int {
        return 0
    }
}
