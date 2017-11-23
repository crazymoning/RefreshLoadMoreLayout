package cn.xzr.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by xzr on 2017/7/11.
 */
interface IRefresh {

    companion object{
        val TYPE_UP = 0
        val TYPE_DOWN = 1
        val TYPE_FINISH = 2
        val TYPE_NO_MORE =3
        val TYPE_START_REFRESH = 4
    }

    fun getView(inflater: LayoutInflater,viewGroup: ViewGroup): View

    fun beforeRefresh(v: View,progress: Int)

    fun onChangeType(v: View, state: Int)

    fun getRefreshHeight(): Int

}