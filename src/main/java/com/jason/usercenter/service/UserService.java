package com.jason.usercenter.service;

import com.jason.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
* @author Jason
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-10-05 11:02:04
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 新用户 ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return 暂时返回一个数字，无意义
     */
    int UserLogout(HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 待脱敏用户对象
     * @return 脱敏后的用户对象
     */
    User getSafetyUser(User originUser);
}
