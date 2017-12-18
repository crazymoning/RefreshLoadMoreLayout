package cn.xzr.refreshloadmorelayout.qq

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import cn.xzr.library.RefreshLoadMoreLayout
import cn.xzr.refreshloadmorelayout.R
import cn.xzr.refreshloadmorelayout.TestAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_qqrefresh.*

class QQRefreshActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qqrefresh)

        val adapter = TestAdapter()
        recycle.adapter = adapter
        recycle.layoutManager = LinearLayoutManager(this)
        refresh.setHeadView(QQHead())
        refresh.setNeedUp(false)
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

            }
        })

    }
}
