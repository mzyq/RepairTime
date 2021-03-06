package com.muzi.repairtime.fragment.employee;

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
import com.muzi.repairtime.activity.applydetail.ApplyItemDetailActivity;
import com.muzi.repairtime.activity.base.BaseFragment;
import com.muzi.repairtime.activity.base.BaseViewModel;
import com.muzi.repairtime.adapter.ApplyTakeAdapter;
import com.muzi.repairtime.databinding.FragmentItemApplyBinding;
import com.muzi.repairtime.entity.RepairEntity;
import com.muzi.repairtime.http.RxHttp;
import com.muzi.repairtime.http.RxUtils;
import com.muzi.repairtime.http.api.RepairApi;
import com.muzi.repairtime.manager.ExLinearLayoutManger;
import com.muzi.repairtime.observer.EntityObserver;
import com.muzi.repairtime.widget.CustomLoadMoreView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者: lipeng
 * 时间: 2019/3/13
 * 邮箱: lipeng@moyi365.com
 * 功能: 我的申请Item
 */
public class ApplyItemFragment extends BaseFragment<FragmentItemApplyBinding, BaseViewModel> {

    private String status;
    private int currentPage = 1;
    private int totalPage = 1;
    private ApplyTakeAdapter adapter;
    private List<RepairEntity.PagesBean.ListBean> listBeans = new ArrayList<>();

    public static ApplyItemFragment getInstance(String status) {
        ApplyItemFragment fragment = new ApplyItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString("status", status);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_item_apply;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void initParam(Bundle bundle) {
        super.initParam(bundle);
        status = bundle.getString("status");
    }

    @Override
    public void initView() {
        super.initView();
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(Color.WHITE);
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        binding.recycelView.setLayoutManager(new ExLinearLayoutManger(getContext()));
        adapter = new ApplyTakeAdapter(R.layout.layout_item_apply_take, listBeans);
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

        binding.recycelView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, final int position) {
                ApplyItemDetailActivity.startActivity(getContext(), listBeans.get(position));
            }
        });

    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        refresh();
    }

    public void refresh() {
        if (!listBeans.isEmpty()) {
            currentPage = 1;
            totalPage = 1;
            listBeans.clear();
            adapter.setNewData(listBeans);
        }
        binding.refreshLayout.setRefreshing(true);
        getData();
    }

    /**
     * 获取信息
     */
    private void getData() {
        RxHttp.getApi(RepairApi.class)
                .getMyRepair(currentPage, status)
                .compose(RxUtils.<RepairEntity>exceptionTransformer())
                .compose(RxUtils.<RepairEntity>scheduling())
                .compose(this.<RepairEntity>bindUntilEvent())
                .subscribe(new EntityObserver<RepairEntity>(this) {
                    @Override
                    public void onSuccess(RepairEntity repairEntity) {
                        totalPage = repairEntity.getPages().getTotalPage();
                        currentPage = repairEntity.getPages().getCurrentPage();
                        listBeans.addAll(repairEntity.getPages().getList());
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
