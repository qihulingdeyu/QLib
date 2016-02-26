package com.qing.widget.horizontallistview;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.qing.widget.horizontallistview.HorizontalListView.FixedViewInfo;

import java.util.ArrayList;
import java.util.Iterator;

public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
    private final ListAdapter mAdapter;
    ArrayList<FixedViewInfo> mHeaderViewInfos;
    ArrayList<FixedViewInfo> mFooterViewInfos;
    static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList();
    boolean mAreAllFixedViewsSelectable;
    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        this.mAdapter = adapter;
        this.mIsFilterable = adapter instanceof Filterable;
        if (headerViewInfos == null) {
            this.mHeaderViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mHeaderViewInfos = headerViewInfos;
        }

        if (footerViewInfos == null) {
            this.mFooterViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mFooterViewInfos = footerViewInfos;
        }

        this.mAreAllFixedViewsSelectable = this.areAllListInfosSelectable(this.mHeaderViewInfos) && this.areAllListInfosSelectable(this.mFooterViewInfos);
    }

    public int getHeadersCount() {
        return this.mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean isEmpty() {
        return this.mAdapter == null || this.mAdapter.isEmpty();
    }

    private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            Iterator i$ = infos.iterator();

            while (i$.hasNext()) {
                FixedViewInfo info = (FixedViewInfo) i$.next();
                if (!info.isSelectable) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean removeHeader(View v) {
        for (int i = 0; i < this.mHeaderViewInfos.size(); ++i) {
            FixedViewInfo info = (FixedViewInfo) this.mHeaderViewInfos.get(i);
            if (info.view == v) {
                this.mHeaderViewInfos.remove(i);
                this.mAreAllFixedViewsSelectable = this.areAllListInfosSelectable(this.mHeaderViewInfos) && this.areAllListInfosSelectable(this.mFooterViewInfos);
                return true;
            }
        }

        return false;
    }

    public boolean removeFooter(View v) {
        for (int i = 0; i < this.mFooterViewInfos.size(); ++i) {
            FixedViewInfo info = (FixedViewInfo) this.mFooterViewInfos.get(i);
            if (info.view == v) {
                this.mFooterViewInfos.remove(i);
                this.mAreAllFixedViewsSelectable = this.areAllListInfosSelectable(this.mHeaderViewInfos) && this.areAllListInfosSelectable(this.mFooterViewInfos);
                return true;
            }
        }

        return false;
    }

    public int getCount() {
        return this.mAdapter != null ? this.getFootersCount() + this.getHeadersCount() + this.mAdapter.getCount() : this.getFootersCount() + this.getHeadersCount();
    }

    public boolean areAllItemsEnabled() {
        return this.mAdapter == null ? true : this.mAreAllFixedViewsSelectable && this.mAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int position) {
        int numHeaders = this.getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).isSelectable;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.isEnabled(adjPosition);
                }
            }

            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).isSelectable;
        }
    }

    public Object getItem(int position) {
        int numHeaders = this.getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).data;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.getItem(adjPosition);
                }
            }

            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).data;
        }
    }

    public long getItemId(int position) {
        int numHeaders = this.getHeadersCount();
        if (this.mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getItemId(adjPosition);
            }
        }

        return -1L;
    }

    public boolean hasStableIds() {
        return this.mAdapter != null ? this.mAdapter.hasStableIds() : false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int numHeaders = this.getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).view;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.getView(adjPosition, convertView, parent);
                }
            }

            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).view;
        }
    }

    public int getItemViewType(int position) {
        int numHeaders = this.getHeadersCount();
        if (this.mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            int adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getItemViewType(adjPosition);
            }
        }

        return -2;
    }

    public int getViewTypeCount() {
        return this.mAdapter != null ? this.mAdapter.getViewTypeCount() : 1;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.registerDataSetObserver(observer);
        }

    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(observer);
        }

    }

    public Filter getFilter() {
        return this.mIsFilterable ? ((Filterable) this.mAdapter).getFilter() : null;
    }

    public ListAdapter getWrappedAdapter() {
        return this.mAdapter;
    }
}
