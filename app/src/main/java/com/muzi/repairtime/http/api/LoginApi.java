package com.muzi.repairtime.http.api;

import com.muzi.repairtime.entity.BaseEntity;
import com.muzi.repairtime.entity.GroupEntity;
import com.muzi.repairtime.entity.LoginEntity;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 作者: lipeng
 * 时间: 2019/3/4
 * 邮箱: lipeng@moyi365.com
 * 功能:
 * 测试账号：
 * 15350831515-维修工
 * 18732850775
 * 11111111111
 * 密码:123456
 */
public interface LoginApi {

    /**
     * 登录接口
     *
     * @param phone
     * @param psd
     * @return
     */
    @FormUrlEncoded
    @POST("user/login")
    Observable<LoginEntity> login(@Field("phone") String phone,
                                  @Field("pass") String psd,
                                  @Field("phoneId") String phoneId);

    /**
     * 获取科室信息
     *
     * @return
     */
    @POST("user/getGroups")
    Observable<GroupEntity> getGroups();

    /**
     * 验证手机号是否注册
     *
     * @param phone
     * @return
     */
    @FormUrlEncoded
    @POST("user/decidePhone")
    Observable<BaseEntity> decidePhone(@Field("phone") String phone);

    /**
     * 注册
     *
     * @param name
     * @param pass
     * @param group-科室
     * @param phone
     * @return
     */
    @FormUrlEncoded
    @POST("user/register")
    Observable<BaseEntity> register(@Field("name") String name,
                                    @Field("phone") String phone,
                                    @Field("pass") String pass,
                                    @Field("group") String group);

    /**
     * 退出登录
     *
     * @return
     */
    @POST("user/androidLogout")
    Observable<BaseEntity> logout();

}
