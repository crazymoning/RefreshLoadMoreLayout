package cn.xzr.refreshloadmorelayout

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.util.concurrent.TimeUnit

import cn.xzr.library.RefreshLoadMoreLayout
import cn.xzr.refreshloadmorelayout.view.QQHead
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : Activity() {

    internal lateinit var mRefresh: RefreshLoadMoreLayout
    private lateinit var mRecycler:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRefresh = findViewById(R.id.refresh)
        mRecycler = findViewById(R.id.recycle)
        val adapter = TestAdapter()
        mRecycler.adapter = adapter
        mRecycler.layoutManager = LinearLayoutManager(this)
        mRefresh.setHeadView(QQHead())
        mRefresh.setNeedUp(false)
        mRefresh.setListener(object : RefreshLoadMoreLayout.OnRefreshListener {
            override fun refresh() {
                Observable.timer(7000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe {
                            mRefresh.resetRefresh()
                        }
            }

            override fun loadMore() {
                Observable.timer(3000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe {
                    mRefresh.resetLoadMore()
                }
            }
        })

    }
}
