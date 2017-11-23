package cn.xzr.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by xzr on 2017/7/12.
 */
class DefaultHead: IRefresh {

    private lateinit var text: TextView

    override fun getView(inflater: LayoutInflater, viewGroup: ViewGroup): View {
        val view = inflater.inflate(R.layout.refresh_default,viewGroup,true)
        text = view.findViewById(R.id.text)
        return view
    }

    override fun beforeRefresh(v: View, progress: Int) {

    }

    override fun onChangeType(v: View, state: Int) {
        when(state){
            IRefresh.TYPE_UP -> text.text = "下拉刷新"
            IRefresh.TYPE_DOWN -> text.text = "松手刷新"
            IRefresh.TYPE_FINISH -> text.text = "完成刷新"
            IRefresh.TYPE_START_REFRESH -> text.text = "正在刷新"
        }
    }

    override fun getRefreshHeight(): Int {
        return 0
    }


}