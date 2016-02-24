package com.qing.widget;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by zwq on 2014/10/03 14:17.<br/><br/>
 * 具有下拉刷新、加载更多的ListView
 */
public class XListView extends ListView implements OnScrollListener {

	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// -- header view
	private XListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// -- footer view
	private XListViewFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	// scroll back duration
	private final static int SCROLL_DURATION = 400;
	// when pull up >= 50px at bottom, trigger load more.
	private final static int PULL_LOAD_MORE_DELTA = 50;
	// support iOS like pull feature.
	private final static float OFFSET_RADIO = 1.8f;

	/**
	 * @param context
	 */
	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// initView header view
		mHeaderView = new XListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView
				.findViewById(XListViewHeader.RELATIVELAYOUT_ID);
		mHeaderTimeView = (TextView) mHeaderView
				.findViewById(XListViewHeader.HEAD_TIME_VIEW_ID);
		addHeaderView(mHeaderView);

		// initView footer view
		mFooterView = new XListViewFooter(context);

		// initView header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
		}
	}

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(String time) {
		mHeaderTimeView.setText(time);
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more.
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		// setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(XListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1
					&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
					mPullRefreshing = true;
					mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					}
				}
				resetHeaderHeight();
			}
			if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePullLoad
						&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
					startLoadMore();
				}
				resetFooterHeight();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();

		public void onLoadMore();
	}

    class XListViewHeader extends LinearLayout {
        private LinearLayout mContainer;
        private ImageView mArrowImageView;
        private ProgressBar mProgressBar;
        private TextView mHintTextView;
        private int mState = STATE_NORMAL;

        private Animation mRotateUpAnim;
        private Animation mRotateDownAnim;

        private final int ROTATE_ANIM_DURATION = 180;

        public final static int STATE_NORMAL = 0;
        public final static int STATE_READY = 1;
        public final static int STATE_REFRESHING = 2;
        public static final int LINEARLAYOUT_1_ID = 1;
        public static final int RELATIVELAYOUT_ID = 2;
        public static final int HEAD_TIME_VIEW_ID = 3;

        public XListViewHeader(Context context) {
            super(context);
            initView(context);
        }

        /**
         * @param context
         * @param attrs
         */
        public XListViewHeader(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        private void initView(Context context) {
            // 根布局
            mContainer = new LinearLayout(context);
            mContainer.setGravity(Gravity.BOTTOM);
            // 初始情况，设置下拉刷新view高度为0
            LayoutParams linearParams1 = new LayoutParams(
                    LayoutParams.MATCH_PARENT, 0);
            // 将根布局加入该LinearLayout中
            addView(mContainer, linearParams1);
            setGravity(Gravity.TOP);

            // ----------------------------------------------------------------
            // 里面的相对布局
            RelativeLayout relativeLayout = new RelativeLayout(context);
            relativeLayout.setId(RELATIVELAYOUT_ID);
            LayoutParams linearParams2 = new LayoutParams(
                    LayoutParams.MATCH_PARENT, 80);
            // 将里面的相对布局加入根布局中
            mContainer.addView(relativeLayout, linearParams2);

            // ----------------------------------------------------------------
            // 相对布局里的子线性布局1
            LinearLayout linearLayout1 = new LinearLayout(context);
            linearLayout1.setId(LINEARLAYOUT_1_ID);

            RelativeLayout.LayoutParams relativeParams1 = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            relativeParams1.addRule(RelativeLayout.CENTER_IN_PARENT);

            linearLayout1.setGravity(Gravity.CENTER);
            linearLayout1.setOrientation(LinearLayout.VERTICAL);
            // 将子布局linear1加入relayout中
            relativeLayout.addView(linearLayout1, relativeParams1);

            // 下拉刷新提示TextView
            mHintTextView = new TextView(context);
            mHintTextView.setText("下拉刷新");
            LayoutParams linearParams3 = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // 将TextView加入linear1中
            linearLayout1.addView(mHintTextView, linearParams3);

            // 子线性布局1（linear1）里的线性布局linear12
            LinearLayout linearLayout1_2 = new LinearLayout(context);
            LayoutParams linearParams4 = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearParams4.setMargins(0, 3, 0, 0);
            // 将linear12加入linear1中
            linearLayout1.addView(linearLayout1_2, linearParams4);

            // 提示时间TextView
            TextView tv = new TextView(context);
            tv.setText("更新于：");
            tv.setTextSize(12);
            // 将提示时间TextView加入linear12
            linearLayout1_2.addView(tv, linearParams3);

            // 时间值TextView
            TextView tv2 = new TextView(context);
            tv2.setText("刚刚");
            tv2.setId(HEAD_TIME_VIEW_ID);
            tv2.setTextSize(12);
            // 将时间值TextView加入linear12
            linearLayout1_2.addView(tv2, linearParams3);

            // ----------------------------------------------------------------
            // ImageView
            mArrowImageView = new ImageView(context);
            RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            relativeParams2.rightMargin = 20;
            relativeParams2.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeParams2.addRule(RelativeLayout.LEFT_OF, LINEARLAYOUT_1_ID);
            mArrowImageView.setImageBitmap(readAssetImage(context));
            // 将ImageView加到相对布局relayout中
            relativeLayout.addView(mArrowImageView, relativeParams2);

            // ProgressBar
            mProgressBar = new ProgressBar(context);
            RelativeLayout.LayoutParams relativeParams3 = new RelativeLayout.LayoutParams(
                    45, 45);
            relativeParams3.addRule(RelativeLayout.LEFT_OF, LINEARLAYOUT_1_ID);
            relativeParams3.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeParams3.rightMargin = 20;
            mProgressBar.setVisibility(View.INVISIBLE);
            // 将mProgressBar加到相对布局relayout中
            relativeLayout.addView(mProgressBar, relativeParams3);

            // ----------------------------------------------------------------
            mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateUpAnim.setFillAfter(true);
            mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
            mRotateDownAnim.setFillAfter(true);
        }

        public void setState(int state) {
            if (state == mState)
                return;
            if (state == STATE_REFRESHING) { // 显示进度
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            } else { // 显示箭头图片
                mArrowImageView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            switch (state) {
                case STATE_NORMAL:
                    if (mState == STATE_READY) {
                        mArrowImageView.startAnimation(mRotateDownAnim);
                    }
                    if (mState == STATE_REFRESHING) {
                        mArrowImageView.clearAnimation();
                    }
                    mHintTextView.setText("下拉刷新");
                    break;
                case STATE_READY:
                    if (mState != STATE_READY) {
                        mArrowImageView.clearAnimation();
                        mArrowImageView.startAnimation(mRotateUpAnim);
                        mHintTextView.setText("松开刷新");
                    }
                    break;
                case STATE_REFRESHING:
                    mHintTextView.setText("正在加载...");
                    break;
                default:
            }
            mState = state;
        }

        public void setVisiableHeight(int height) {
            if (height < 0)
                height = 0;
            LayoutParams lp = (LayoutParams) mContainer
                    .getLayoutParams();
            lp.height = height;
            mContainer.setLayoutParams(lp);
        }

        public int getVisiableHeight() {
            return mContainer.getHeight();
        }

        /**
         * 读取assets里面文件名为xlistview_arrow.png的图片
         * @param context
         * @return bitmap
         */
        private Bitmap readAssetImage(Context context) {
            AssetManager asset = context.getAssets();
            InputStream assetFile = null;
            Bitmap bitmap = null;
            try {
                assetFile = asset.open("xlistview_arrow.png");
                bitmap = BitmapFactory.decodeStream(assetFile);
                assetFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    class XListViewFooter extends LinearLayout {
        public final static int STATE_NORMAL = 0;
        public final static int STATE_READY = 1;
        public final static int STATE_LOADING = 2;

        private Context mContext;

        private RelativeLayout mContentView;
        private LinearLayout mLoadingLayout;
        private ProgressBar mProgressBar;
        private TextView mLoadingView;
        private TextView mHintView;

        public XListViewFooter(Context context) {
            super(context);
            initView(context);
        }

        public XListViewFooter(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        public void setState(int state) {
            mHintView.setVisibility(View.INVISIBLE);
            mLoadingLayout.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.INVISIBLE);
            if (state == STATE_READY) {
                mHintView.setVisibility(View.VISIBLE);
                mHintView.setText("松开载入更多");
            } else if (state == STATE_LOADING) {
                mLoadingLayout.setVisibility(View.VISIBLE);
            } else {
                mHintView.setVisibility(View.VISIBLE);
                mHintView.setText("查看更多");
            }
        }

        public void setBottomMargin(int height) {
            if (height < 0)
                return;
            LayoutParams lp = (LayoutParams) mContentView
                    .getLayoutParams();
            lp.bottomMargin = height;
            mContentView.setLayoutParams(lp);
        }

        public int getBottomMargin() {
            LayoutParams lp = (LayoutParams) mContentView
                    .getLayoutParams();
            return lp.bottomMargin;
        }

        /**
         * normal status
         */
        public void normal() {
            mHintView.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);
        }

        /**
         * loading status
         */
        public void loading() {
            mHintView.setVisibility(View.GONE);
            mLoadingLayout.setVisibility(View.VISIBLE);
        }

        /**
         * hide footer when disable pull load more
         */
        public void hide() {
            LayoutParams lp = (LayoutParams) mContentView
                    .getLayoutParams();
            lp.height = 0;
            mContentView.setLayoutParams(lp);
        }

        /**
         * show footer
         */
        public void show() {
            LayoutParams lp = (LayoutParams) mContentView
                    .getLayoutParams();
            lp.height = LayoutParams.WRAP_CONTENT;
            mContentView.setLayoutParams(lp);
        }

        private void initView(Context context) {
            mContext = context;
            // 根布局
            LinearLayout moreView = new LinearLayout(mContext);
            moreView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // 将根布局加到该LinearLayout中
            addView(moreView);

            // 根布局里面的相对布局
            mContentView = new RelativeLayout(mContext);
            LayoutParams linearParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mContentView.setPadding(10, 10, 10, 10);
            // 将相对布局RelativeLayout加到moreView根布局中
            moreView.addView(mContentView, linearParams);

            // ----------------------------------------------------------------
            // 显示正在加载的布局
            mLoadingLayout = new LinearLayout(mContext);
            mLoadingLayout.setOrientation(LinearLayout.HORIZONTAL);
            mLoadingLayout.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams relativeParams1 = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, 50);
//		relativeParams1.setLayoutDirection(LinearLayout.HORIZONTAL);
            relativeParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
            mContentView.addView(mLoadingLayout, relativeParams1);

            mProgressBar = new ProgressBar(mContext);
            LayoutParams linearParams1 = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // 将mProgressBar加入相对布局LinearLayout中
            mLoadingLayout.addView(mProgressBar, linearParams1);

            // 正在加载的TextView
            mLoadingView = new TextView(mContext);
            mLoadingView.setText("正在加载...");
            LayoutParams linearParams2 = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearParams2.gravity = Gravity.CENTER_VERTICAL;
            // 左边距
            linearParams2.leftMargin = 30;
            // 将其加入相对布局LinearLayout中
            mLoadingLayout.addView(mLoadingView, linearParams2);

            // ----------------------------------------------------------------
            // 查看更多的TextView
            mHintView = new TextView(mContext);
            mHintView.setText("查看更多");
            mHintView.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            relativeParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
            // 将其加入相对布局RelativeLayout中
            mContentView.addView(mHintView, relativeParams2);
        }
    }
}
