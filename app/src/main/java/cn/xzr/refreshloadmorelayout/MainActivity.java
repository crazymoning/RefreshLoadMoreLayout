package cn.xzr.refreshloadmorelayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.concurrent.TimeUnit;

import cn.xzr.library.DefaultFoot;
import cn.xzr.library.DefaultHead;
import cn.xzr.library.RefreshLoadMoreLayout;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    RefreshLoadMoreLayout mRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefresh = findViewById(R.id.refresh);
        mRefresh.setHeadView(new DefaultHead());
        mRefresh.setFootView(new DefaultFoot());
        mRefresh.setListener(new RefreshLoadMoreLayout.OnRefreshListener() {
            @Override
            public void refresh() {
                Observable.timer(3000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mRefresh.resetRefresh();
                    }
                });
            }

            @Override
            public void loadMore() {
                Observable.timer(3000, TimeUnit.MILLISECONDS).
                        subscribeOn(Schedulers.newThread()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                mRefresh.resetLoadMore();
                            }
                        });
            }
        });

    }
}
