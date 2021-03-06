package com.muzi.repairtime.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.muzi.repairtime.R;
import com.muzi.repairtime.activity.base.BaseFragment;
import com.muzi.repairtime.activity.base.BaseViewModel;
import com.muzi.repairtime.activity.detail.NoticeDetailActivity;
import com.muzi.repairtime.adapter.NoticeAdapter;
import com.muzi.repairtime.databinding.FragmentNoticeBinding;
import com.muzi.repairtime.entity.NoticeEntity;
import com.muzi.repairtime.http.RxHttp;
import com.muzi.repairtime.http.RxUtils;
import com.muzi.repairtime.http.api.NoticeApi;
import com.muzi.repairtime.manager.ExLinearLayoutManger;
import com.muzi.repairtime.observer.EntityObserver;
import com.muzi.repairtime.widget.CustomLoadMoreView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者: lipeng
 * 时间: 2019/3/13
 * 邮箱: lipeng@moyi365.com
 * 功能: 公告
 */
public class NoticeFragment extends BaseFragment<FragmentNoticeBinding, BaseViewModel> {

    private int currentPage = 1;
    private int totalPage = 1;
    private NoticeAdapter adapter;
    private List<NoticeEntity.PagesBean.ListBean> listBeans = new ArrayList<>();

    public static NoticeFragment getInstance() {
        NoticeFragment fragment = new NoticeFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_notice;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void initView() {
        super.initView();
        binding.toolbar.setTitle("公告");
        initToolbarNav(binding.toolbar);
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!listBeans.isEmpty()) {
                    currentPage = 1;
                    totalPage = 1;
                    listBeans.clear();
                    adapter.setNewData(listBeans);
                }
                getData();
            }
        });
        binding.recycelView.setLayoutManager(new ExLinearLayoutManger(getContext()));
        adapter = new NoticeAdapter(R.layout.layout_item_notice, listBeans);
        adapter.bindToRecyclerView(binding.recycelView);
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setEmptyView(R.layout.layout_recyclerview_empty);
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (currentPage < totalPage) {
                    currentPage++;
                    getData();
                } else {
                    adapter.loadMoreEnd();
                }
            }
        }, binding.recycelView);

        //查看详情
        binding.recycelView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                NoticeEntity.PagesBean.ListBean listBean = listBeans.get(position);
                NoticeDetailActivity.startActivity(getContext(), listBean.getTitle(), listBean.getContent());
            }
        });

    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        binding.refreshLayout.setRefreshing(true);
        getData();
    }

    private void getData() {
        RxHttp.getApi(NoticeApi.class)
                .getAnnouncements(currentPage)
                .compose(RxUtils.<NoticeEntity>scheduling())
                .compose(RxUtils.<NoticeEntity>exceptionTransformer())
                .compose(this.<NoticeEntity>bindUntilEvent())
                .subscribe(new EntityObserver<NoticeEntity>(this) {
                    @Override
                    public void onSuccess(NoticeEntity noticeEntity) {
                        totalPage = noticeEntity.getPages().getTotalPage();
                        currentPage = noticeEntity.getPages().getCurrentPage();
                        listBeans.addAll(noticeEntity.getPages().getList());
                        adapter.notifyDataSetChanged();
                        if (adapter.isLoading()) {
                            adapter.loadMoreComplete();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        if (adapter.isLoading()) {
                            adapter.loadMoreFail();
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (binding.refreshLayout.isRefreshing()) {
                            binding.refreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

}
