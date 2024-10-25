package com.jason.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.usercenter.common.ErrorCode;
import com.jason.usercenter.exception.BusinessException;
import com.jason.usercenter.model.domain.User;
import com.jason.usercenter.service.UserService;
import com.jason.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jason.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
* @author Jason
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-10-05 11:02:04
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    // 只能有字母和数字：private static final String REGEX = "^[a-zA-Z0-9]+$";

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，用于密码加密混淆
     */
    private static final String SALT = "gongSum";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {

        // 1. 校验：非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数未空");
        }

        // 2. 账户长度不能少于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 3. 密码长度不能少于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 4. 星球编号长度不超过5
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }

        /*
            账户不能包含特殊字符，包含特殊字符则返回 false
        if (!Pattern.matches(REGEX, userAccount)) {}
        */

        // 5. 账户不能包含特殊字符，下划线除外
        String validPattern = "^[a-zA-Z0-9_]*$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {  // 如果匹配到特殊字符则失败，下划线除外
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码包含不支持的字符，不支持除下划线_以外的特殊字符");
        }

        // 6. 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "再次输入的密码不一致");
        }

        // 6.1 账户不能重复 (即：检查输入的账号是否已存在)
        // 这一步需要 SQL 查询，会产生更多延迟，因此需放在最后检查
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {    // 如果用户已注册
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号用户已存在！");
        }

        // 6.2 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {    // 如果用户已注册
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号已被占用，请更换编号！");
        }

        // 7. 对密码进行 MD5 加密，加盐
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 8. 向用户数据库插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);// 保存到数据库中
        if (!saveResult) {  // 如果保存失败
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常！");
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验：非空判断
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "输入不能为空！");
        }

        // 2. 账户长度不能少于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 3. 密码长度不能少于8
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 4. 账户不能包含特殊字符，下划线除外
        String validPattern = "^[a-zA-Z0-9_]*$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {  // 如果匹配到特殊字符则失败，下划线除外
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含除下划线_以外的特殊字符");
        }

        // 5. 对密码进行 MD5 加密，加盐
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 6. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {     // 用户不存在
            log.info("user login failed, userAccount cannot match userPassword.");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 7. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 8. 记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int UserLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    /**
     * 用户脱敏
     *
     * @param originUser 待脱敏用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        return safetyUser;
    }
}




