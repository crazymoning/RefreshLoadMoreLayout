package cn.xzr.library

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Scroller

/**
 * Created by xzr on 2017/6/5.
 */
class RefreshLoadMoreLayout(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        val TAG = "RefreshLoadMoreLayout"

        private val STATE_UP = 0
        private val STATE_DOWN = 1
        private val STATE_FINISH = 2

        private val TYPE_FOLLOW = 0
        private val TYPE_OVERLAP = 1

    }

    /**
     * 滑动时HeadView和FootView的状态
     * TYPE_FOLLOW  HeadView和FootView跟随手指滑动
     * TYPE_OVERLAP HeadView和FootView不跟随手指滑动，保持在顶部和底部
     */
    private var scrollType: Int = TYPE_FOLLOW

    /**
     * 当scrollType为TYPE_OVERLAP，滑动的距离，不需要刷新滑动时为0
     */
    private var scrollTop = 0

    private var state = STATE_UP

    private var contentView: View? = null

    private var headView: View? = null
    private var footView: View? = null
    private var headId = 0
    private var footId = 0

    /**
     * 刷新滑动的状态改变时的监听
     */
    private var headRefresh: IRefresh? = null
    private var footRefresh: IRefresh? = null

    /**
     * 顶部是否需要滑动，值为false下拉刷新不能使用，值为true，headview为null也可以下拉，但是没有下拉加载的功能
     */
    private var topScroll = true

    /**
     * 底部是否需要滑动，值为false上拉加载不能使用，值为true，footview为null也可以上拉，但是没有上拉加载的功能
     */
    private var bottomScroll = true

    private var mLastY: Float = 0f
    private var startY: Float = 0f
    private var dy = 0

    /**
     * touch事件是否已经被RefreshLoadMoreLayout拦截,当RefreshLoadMoreLayout本身没有滑动，
     * 也就是scrollTop和scrollY都为0的时候isControl为false，反之为true
     */
    private var isControl = false

    /**
     * touch事件是否需要被RefreshLoadMoreLayout拦截
     */
    private var needIntercept: Boolean = false

    /**
     * 最短滑动距离
     */
    private var TOUCH_SLOP = 0

    private var mScroller: Scroller
    private var mAnim: ValueAnimator? = null

    private var mListener: OnRefreshListener? = null

    var animValue = 0

    /**
     * 是否正在动画
     */
    private var isAnim = false

    /**
     * 下拉刷新的临界值
     */
    private var headRefreshHeight = 0
    /**
     * 上拉加载的临界值
     */
    private var footLoadingHeight = 0

    private var isChangeControl = false
    private var isCancelByTouch = false

    private var isRefreshing = false
    private var isLoading = false
    private var noMore = false

    /**
     * 达到刷新临界值后是否需要touch up后再刷新
     */
    private var refreshNeedUp = true

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    init {
        TOUCH_SLOP = ViewConfiguration.get(context).scaledTouchSlop
        mScroller = Scroller(context)
        val t: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.RefreshLoadMoreLayout)
        if (t.hasValue(R.styleable.RefreshLoadMoreLayout_refresh_type)) {
            scrollType = t.getInt(R.styleable.RefreshLoadMoreLayout_refresh_type, 0)
        }
        if (t.hasValue(R.styleable.RefreshLoadMoreLayout_head)) {
            headId = t.getResourceId(R.styleable.RefreshLoadMoreLayout_head, 0)
        }
        if (t.hasValue(R.styleable.RefreshLoadMoreLayout_foot)) {
            footId = t.getResourceId(R.styleable.RefreshLoadMoreLayout_foot, 0)
        }
        if (t.hasValue(R.styleable.RefreshLoadMoreLayout_topScroll)) {
            topScroll = t.getBoolean(R.styleable.RefreshLoadMoreLayout_topScroll, true)
        }
        if (t.hasValue(R.styleable.RefreshLoadMoreLayout_bottomScroll)) {
            bottomScroll = t.getBoolean(R.styleable.RefreshLoadMoreLayout_bottomScroll, true)
        }
        t.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        if (headRefresh != null && topScroll) {
            headRefreshHeight = headRefresh!!.getRefreshHeight()
            headRefreshHeight = when {
                headRefreshHeight > 0 -> headRefreshHeight
                headView == null -> 0
                else -> headView!!.measuredHeight
            }
        } else {
            headRefreshHeight = if (headView == null || !topScroll) 0 else headView!!.measuredHeight
        }
        if (footRefresh != null && bottomScroll) {
            footLoadingHeight = footRefresh!!.getRefreshHeight()
            footLoadingHeight = when {
                footLoadingHeight > 0 -> footLoadingHeight
                footView == null -> 0
                else -> footView!!.measuredHeight
            }
        } else {
            footLoadingHeight = if (footView == null || !bottomScroll) 0 else footView!!.measuredHeight
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (scrollType == TYPE_FOLLOW) {
            if (headView != null && topScroll) {
                headView!!.layout(0, -headView!!.measuredHeight, measuredWidth, 0)
            }
            if (footView != null && bottomScroll) {
                footView!!.layout(0, measuredHeight, measuredWidth, measuredHeight + footView!!.measuredHeight)
            }
        } else if (scrollType == TYPE_OVERLAP) {
            if (headView != null && topScroll) {
                headView!!.layout(0, 0, measuredWidth, headView!!.measuredHeight)
            }
            if (footView != null && bottomScroll) {
                footView!!.layout(0, measuredHeight - footView!!.measuredHeight, measuredWidth, measuredHeight)
            }

        }
        if (contentView != null) {
            contentView!!.layout(0, scrollTop, contentView!!.measuredWidth, scrollTop + contentView!!.measuredHeight)
        }


    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount == 0) {
            throw IllegalStateException("at last one child view")
        }
        if (childCount > 3) {
            throw IllegalStateException("most 3 child view")
        }
        contentView = getChildAt(0)

        headView = getChildAt(1)
        if (headView == null && headId != 0 && topScroll) {
            LayoutInflater.from(context).inflate(headId, this, true)
            headView = getChildAt(1)
        }

        footView = getChildAt(2)
        if (footView == null && footId != 0 && bottomScroll) {
            LayoutInflater.from(context).inflate(footId, this, true)
            footView = getChildAt(childCount - 1)
        }
        contentView!!.bringToFront()

    }

    private var downNeedIntercept = false
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        dealMulTouchEvent(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                moveY = 0
                needIntercept = isAnim  //当回弹动画没有结束的时候需要拦截touch事件
                downNeedIntercept = isControl
                isCancelByTouch = false
                if (state == STATE_FINISH && (scrollY == 0 || scrollTop == 0)) {
                    state = STATE_UP
                    if (headRefresh != null && topScroll) {
                        headRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
                    }
                    if (footRefresh != null && bottomScroll && !noMore) {
                        footRefresh!!.onChangeType(this, IRefresh.TYPE_UP)

                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(ev.y - startY) < TOUCH_SLOP && !isChangeControl) {
                    return super.dispatchTouchEvent(ev)
                }
                needIntercept = needControl()
                if (needIntercept && !isControl) {//重置action_down，让事件被refreshLoadMoreLayout拦截
                    isControl = true
                    isChangeControl = true
                    isCancelByTouch = true
                    ev.action = MotionEvent.ACTION_CANCEL
                    val ev2: MotionEvent = MotionEvent.obtain(ev)
                    this.dispatchTouchEvent(ev)
                    ev2.action = MotionEvent.ACTION_DOWN
                    return this.dispatchTouchEvent(ev2)
                }else if (downNeedIntercept && !isControl){ //重置action_down，refreshLoadMoreLayout不再拦截事件
                    isCancelByTouch = true
                    ev.action = MotionEvent.ACTION_CANCEL
                    val ev2: MotionEvent = MotionEvent.obtain(ev)
                    this.dispatchTouchEvent(ev)
                    ev2.action = MotionEvent.ACTION_DOWN
                    return this.dispatchTouchEvent(ev2)
                }
            }
            MotionEvent.ACTION_UP -> {
                isChangeControl = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun needControl(): Boolean {
        return (!contentView!!.canScrollVertically(-1) && dy > 0 && topScroll) ||
                ((!contentView!!.canScrollVertically(1) && dy < 0 && bottomScroll))||
                        Math.abs(scrollY) > 0 ||
                        Math.abs(scrollTop) > 0
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return needIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (state != STATE_FINISH) {
                    isAnim = false
                    if (!mScroller.isFinished) mScroller.abortAnimation()
                    if (mAnim != null && mAnim!!.isRunning) mAnim!!.cancel()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!needIntercept){
                    return false
                }
                parent.requestDisallowInterceptTouchEvent(true)
                if (state == STATE_FINISH) {
                    return true
                }
                if (onScrollMove(event)) {
                    return true
                }
                changeUpOrDown()
                if (scrollTop > 0 || scrollY < 0 && topScroll) {
                    if (headView != null) headView!!.visibility = View.VISIBLE
                    if (footView != null) footView!!.visibility = View.GONE
                } else if (scrollTop < 0 || scrollY > 0 && bottomScroll) {
                    if (headView != null) headView!!.visibility = View.GONE
                    if (footView != null) footView!!.visibility = View.VISIBLE
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!isCancelByTouch) {
                    isAnim = false
                    if (!mScroller.isFinished) mScroller.abortAnimation()
                    if (mAnim != null && mAnim!!.isRunning) mAnim!!.cancel()
                    scrollTo(0, 0)
                    scrollTop = 0
                    requestLayout()
                } else isCancelByTouch = false
            }
            MotionEvent.ACTION_UP -> {
                if (state == STATE_FINISH) {
                    return true
                }
                onScrollUp()
            }
        }
        return true
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.currY)
            if (scrollY in -headRefreshHeight..-1 && !isRefreshing && headRefresh != null){
                headRefresh!!.beforeRefresh(this,Math.abs(mScroller.currY) * 100 / headRefreshHeight)
            }
            postInvalidate()
            if (scrollY == 0) {
                isControl = false
                isAnim = false
            }
            if (isAnim && mScroller.isFinished) {
                isAnim = false
            }
        }
        super.computeScroll()
    }

    private fun startScrollY(startY: Int, dy: Int) {
        mScroller.startScroll(0, startY, 0, dy, 500)
        isAnim = true
        invalidate()
    }

    private fun startBackAnim(start: Int, end: Int) {
        mAnim = ValueAnimator.ofInt(start, end)
        mAnim!!.addUpdateListener { animation ->
            kotlin.run {
                animValue = animation.animatedValue as Int
                scrollTop = animValue
                requestLayout()
                if (scrollTop in 1..headRefreshHeight && !isRefreshing && headRefresh != null){
                    headRefresh!!.beforeRefresh(this,scrollTop*100/headRefreshHeight)
                }
            }
        }
        mAnim!!.interpolator = AccelerateInterpolator()
        mAnim!!.addListener(RefreshAnimListener())
        mAnim!!.start()
    }

    private fun changeUpOrDown() {
        if (scrollType == TYPE_OVERLAP) {
            if (scrollTop > 0 &&!isRefreshing && topScroll) {
                upOrDown(scrollTop, headRefreshHeight, headRefresh)
            } else if (scrollTop < 0 && !isLoading && !noMore && bottomScroll) {
                upOrDown(scrollTop, footLoadingHeight, footRefresh)
            }
        } else if (scrollType == TYPE_FOLLOW) {
            if (scrollY < 0 && !isRefreshing && topScroll) {
                upOrDown(scrollY, headRefreshHeight, headRefresh)
            } else if (scrollY > 0 && !isLoading && !noMore && bottomScroll) {
                upOrDown(scrollY, footLoadingHeight, footRefresh)
            }
        }
    }

    private fun upOrDown(scroll: Int, limitHeight: Int, refresh: IRefresh?) {
        if (Math.abs(scroll) in 1..limitHeight) {
            if (state != STATE_UP && refresh != null) {
                state = STATE_UP
                refresh.onChangeType(this, IRefresh.TYPE_UP)
            }
            if (limitHeight != 0 && refresh != null) {
                refresh.beforeRefresh(this, Math.abs(scroll) * 100 / limitHeight)
            }
        } else if (Math.abs(scroll) > limitHeight) {
            if (state != STATE_DOWN) {
                state = STATE_DOWN
                if (limitHeight != 0 && refresh != null) {
                    refresh.beforeRefresh(this, 100)
                }
                if (!refreshNeedUp &&  (scrollTop > 0 || scrollY < 0)) {
                    startRefresh()
                }else refresh?.onChangeType(this, IRefresh.TYPE_DOWN)
            }
        }
    }

    var moveY = 0
    private fun onScrollMove(event: MotionEvent): Boolean {
        if (scrollType == TYPE_FOLLOW) {
            moveY += dy
            dy = if (moveY > 0) {
                ((height + scrollY) / height.toFloat() * moveY / 2).toInt()
            } else {
                ((height - scrollY) / height.toFloat() * moveY / 2).toInt()
            }
            if (dy != 0) {
                moveY = 0
            }
            if (scrollY < 0 && scrollY - dy > 0 || (scrollY > 0 && scrollY - dy < 0)) {
                dy = scrollY
            }
            scrollBy(0, -dy)
            if (scrollY == 0) {
                isChangeControl = true
                isControl = false
                event.action = MotionEvent.ACTION_DOWN
                this.dispatchTouchEvent(event)
                return true
            }
        } else if (scrollType == TYPE_OVERLAP) {
            moveY += dy
            dy = if (dy > 0) {
                ((height - contentView!!.top) / height.toFloat() * moveY / 2).toInt()
            } else {
                (contentView!!.bottom / height.toFloat() * moveY / 2).toInt()
            }
            if (dy != 0) {
                moveY = 0
            }
            if (scrollTop < 0 && scrollTop + dy > 0 || (scrollTop > 0 && scrollTop + dy < 0)) {
                scrollTop = 0
            } else {
                scrollTop += dy
            }
            requestLayout()
            if (scrollTop == 0) {
                isChangeControl = true
                isControl = false
                event.action = MotionEvent.ACTION_DOWN
                this.dispatchTouchEvent(event)
                return true
            }
        }
        return false
    }

    private fun onScrollUp() {
        if (scrollType == TYPE_FOLLOW) {
            if ((scrollY in 1..footLoadingHeight && !isLoading && !noMore) ||
                    (scrollY in -headRefreshHeight..-1 && !isRefreshing)) {
                startScrollY(scrollY, -scrollY)
            } else if (scrollY < -headRefreshHeight) {
                if (!isRefreshing && refreshNeedUp) {
                    startRefresh()
                }
                startScrollY(scrollY, -headRefreshHeight - scrollY)
            } else if (scrollY > footLoadingHeight) {
                if (!isLoading) {
                    startLoadMore()
                }
                startScrollY(scrollY, footLoadingHeight - scrollY)
            }
        } else if (scrollType == TYPE_OVERLAP) {
            if ((scrollTop in 1..headRefreshHeight && !isRefreshing) ||
                    (scrollTop in -footLoadingHeight..-1 && !isLoading && !noMore)) {
                startBackAnim(scrollTop, 0)
            } else if (scrollTop > headRefreshHeight) {
                if (!isRefreshing && refreshNeedUp) {
                    startRefresh()
                }
                startBackAnim(scrollTop, headRefreshHeight)
            } else if (scrollTop < -footLoadingHeight) {
                if (!isLoading) {
                    startLoadMore()
                }
                startBackAnim(scrollTop, -footLoadingHeight)
            }
        }
    }

    private fun startRefresh() {
        if (mListener != null && headView != null && !isRefreshing) {
            isRefreshing = true
            if (topScroll){
                mListener!!.refresh()
            }
            isLoading = false
            if (headRefresh != null && topScroll) {
                headRefresh!!.onChangeType(this, IRefresh.TYPE_START_REFRESH)
            }
        }
    }

    private fun startLoadMore() {
        if (mListener != null && footView != null && !isLoading && !noMore) {
            isLoading = true
            if (bottomScroll){
                mListener!!.loadMore()
            }
            isRefreshing = false
            if (footRefresh != null && bottomScroll) {
                footRefresh!!.onChangeType(this, IRefresh.TYPE_START_REFRESH)
            }
        }
    }

    fun resetRefresh() {
        isRefreshing = false
        if (scrollType == TYPE_FOLLOW) {
            if (scrollY < 0) {
                state = STATE_FINISH
                if (headRefresh != null && topScroll) {
                    headRefresh!!.onChangeType(this, IRefresh.TYPE_FINISH)
                }
                startScrollY(scrollY, -scrollY)
            } else {
                state = STATE_UP
                if (headRefresh != null && topScroll) {
                    headRefresh!!.onChangeType(this, IRefresh.TYPE_UP)

                }
            }
        } else if (scrollType == TYPE_OVERLAP) {
            if (scrollTop > 0) {
                state = STATE_FINISH
                if (headRefresh != null && topScroll) {
                    headRefresh!!.onChangeType(this, IRefresh.TYPE_FINISH)
                }
                startBackAnim(scrollTop, 0)
            } else {
                state = STATE_UP
                if (headRefresh != null && topScroll) {
                    headRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
                }
            }
        }
    }

    fun resetLoadMore() {
        if (noMore) {
            return
        }
        isLoading = false
        if (scrollType == TYPE_FOLLOW) {
            if (scrollY > 0) {
                state = STATE_FINISH
                if (footRefresh != null && bottomScroll) {
                    footRefresh!!.onChangeType(this, IRefresh.TYPE_FINISH)
                }
                startScrollY(scrollY, -scrollY)
            } else {
                state = STATE_UP
                if (footRefresh != null && bottomScroll) {
                    footRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
                }
            }
        } else if (scrollType == TYPE_OVERLAP) {
            if (scrollTop < 0) {
                state = STATE_FINISH
                if (footRefresh != null && bottomScroll) {
                    footRefresh!!.onChangeType(this, IRefresh.TYPE_FINISH)
                }
                startBackAnim(scrollTop, 0)
            } else {
                state = STATE_UP
                if (footRefresh != null && bottomScroll) {
                    footRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
                }
            }
        }
    }

    fun setNoMore(noMore: Boolean) {
        this.noMore = noMore
        if (footRefresh != null && noMore && bottomScroll) {
            footRefresh!!.onChangeType(this, IRefresh.TYPE_NO_MORE)
        } else if (footRefresh != null && !noMore &&
                (scrollType == TYPE_OVERLAP && scrollTop >= 0) ||
                (scrollType == TYPE_FOLLOW && scrollY <= 0)) {
            state = STATE_UP
            if (bottomScroll) {
                footRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
            }
        }
    }

    fun setHeadView(refresh: IRefresh?) {
        headRefresh = refresh
        if (headView != null) {
            removeView(headView)
        }
        if (refresh != null && topScroll) {
            headView = if (refresh.getView(LayoutInflater.from(context), this) != null) {
                getChildAt(childCount - 1)
            } else {
                null
            }
        }

        state = STATE_UP
        if (headRefresh != null && topScroll) {
            headRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
        }
        isControl = false
        contentView!!.bringToFront()
        if (scrollTop > 0) {
            scrollTop = 0
            requestLayout()
        }
        if (scrollY < 0) {
            scrollTo(0, 0)
        }

    }

    fun setFootView(loadMore: IRefresh?) {
        if (loadMore == null) {
            return
        }
        footRefresh = loadMore
        if (footView != null) {
            removeView(footView)
        }

        footView = if (loadMore.getView(LayoutInflater.from(context), this) != null) {
            getChildAt(childCount - 1)
        } else {
            null
        }
        contentView!!.bringToFront()
        if (bottomScroll) {
            footRefresh!!.onChangeType(this, IRefresh.TYPE_UP)
            state = STATE_UP

            if (scrollTop < 0) {
                scrollTop = 0
                requestLayout()
            }
            if (scrollY > 0) {
                scrollTo(0, 0)
            }
            isControl = false
        }

    }

    fun setListener(listener: OnRefreshListener) {
        mListener = listener
    }

    inner class RefreshAnimListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {

        }

        override fun onAnimationEnd(animation: Animator?) {
            isAnim = false
            if (scrollTop == 0) {
//                state = STATE_UP
//                if (headRefresh != null && topScroll && isRefreshing) {
//                    headRefresh!!.onChangeType(this@RefreshLoadMoreLayout, IRefresh.TYPE_UP)
//                }
//                if (footRefresh != null && bottomScroll && !noMore && isLoading) {
//                    footRefresh!!.onChangeType(this@RefreshLoadMoreLayout, IRefresh.TYPE_UP)
//
//                }
                isControl = false
            }
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
            isAnim = true
        }
    }

    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private fun dealMulTouchEvent(ev: MotionEvent) {
        val action = MotionEventCompat.getActionMasked(ev)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val y = MotionEventCompat.getY(ev, pointerIndex)
                mLastY = y
                startY = mLastY
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                val y = MotionEventCompat.getY(ev, pointerIndex)
                dy = (y - mLastY).toInt()
                mLastY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mActivePointerId = MotionEvent.INVALID_POINTER_ID
            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                if (pointerId != mActivePointerId) {
                    mLastY = MotionEventCompat.getY(ev, pointerIndex)
                    startY = mLastY
                    mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastY = MotionEventCompat.getY(ev, newPointerIndex)
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                }
            }
        }
    }

    fun setBottomScroll(bottomScroll: Boolean) {
        if (this.bottomScroll == bottomScroll) {
            return
        }
        this.bottomScroll = bottomScroll
        if (footView != null) {
            if (!bottomScroll){
                removeView(footView)
            }else{
                setFootView(footRefresh)
            }
        }
        requestLayout()
    }

    fun setTopScroll(topScroll: Boolean) {
        if (this.topScroll == topScroll) {
            return
        }
        this.topScroll = topScroll
        if (headView != null) {
            if (!topScroll){
                removeView(headView)
            }else{
                setHeadView(headRefresh)
            }
        }
        requestLayout()
    }

    fun setNeedUp(needUp:Boolean){
        this.refreshNeedUp = needUp
    }

    interface OnRefreshListener {
        fun refresh()

        fun loadMore()
    }
}