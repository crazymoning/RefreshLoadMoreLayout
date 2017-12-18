package cn.xzr.refreshloadmorelayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import cn.xzr.library.DefaultFoot
import cn.xzr.library.DefaultHead
import cn.xzr.library.RefreshLoadMoreLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_default.*
import java.util.concurrent.TimeUnit

class DefaultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)

        recycle.layoutManager = LinearLayoutManager(this)
        recycle.adapter = TestAdapter()

        refresh.setHeadView(DefaultHead())
        refresh.setFootView(DefaultFoot())
        refresh.setListener(object : RefreshLoadMoreLayout.OnRefreshListener {
            override fun refresh() {
                Observable.timer(3000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe {
                            refresh.resetRefresh()
                        }
            }

            override fun loadMore() {
                Observable.timer(3000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe {
                            refresh.resetLoadMore()
                            refresh.setNoMore(true)
                        }
            }

        })

    }
}
