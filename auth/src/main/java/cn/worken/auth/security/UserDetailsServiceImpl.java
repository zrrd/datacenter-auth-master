package cn.worken.auth.security;

import cn.worken.auth.security.dto.LoginParam;
import cn.worken.auth.security.dto.LoginUserInfo;
import cn.worken.auth.security.dto.ResDto;
import cn.worken.auth.security.dto.ResTypeDto;
import cn.worken.auth.security.dto.UserAuthenticationDetails;
import cn.worken.auth.security.exception.ServiceException;
import cn.worken.auth.security.userloader.UserLoaderFactory;
import cn.worken.auth.security.validations.ValidationFactory;
import cn.worken.auth.service.entity.SysCom;
import cn.worken.auth.service.entity.SysUser;
import cn.worken.auth.service.mapper.ResMapper;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author shaoyijiong
 * @date 2020/6/30
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserLoaderFactory userLoaderFactory;
    private final ResMapper resMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ValidationFactory validationFactory;
    private final String redisResPrefix;
    private final String redisUserInfo;


    /**
     * 注入
     */
    public UserDetailsServiceImpl(UserLoaderFactory userLoaderFactory, StringRedisTemplate stringRedisTemplate,
        ResMapper resMapper, ValidationFactory validationFactory) {
        this.userLoaderFactory = userLoaderFactory;
        this.stringRedisTemplate = stringRedisTemplate;
        this.resMapper = resMapper;
        this.validationFactory = validationFactory;
        this.redisResPrefix = "oauth:res:";
        this.redisUserInfo = "sys:user-info:";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = SysUser.loadUserByUsername(username);
        return new UserAuthenticationDetails(sysUser.getLoginName(), sysUser.getId(), sysUser.getComId());
    }

    /**
     * 实际用户登录
     *
     * @return 返回给前端的用户信息
     */
    public LoginUserInfo login(LoginParam loginParam) {
        // 查找用户
        SysUser sysUser = userLoaderFactory.loadUser(loginParam);
        if (sysUser == null) {
            throw new ServiceException("该用户不存在!");
        }
        SysCom sysCom = new SysCom().selectById(sysUser.getComId());
        if (sysCom == null || !sysCom.checkServer(loginParam.getLoginType().getBelongServe().name())) {
            throw new ServiceException("该用户不存在!");
        }
        // 做密码等方式校验
        validationFactory.doValidation(sysUser, loginParam);
        // 封装前台响应
        LoginUserInfo userInfo = LoginUserInfo.builder().userId(sysUser.getId()).loginName(sysUser.getLoginName())
            .userName(sysUser.getUserName()).comId(sysUser.getComId()).companyName(sysCom.getComName())
            .userType(sysUser.getUserType()).loginType(loginParam.getLoginType()).productId(sysUser.getProductId())
            .build();
        Set<ResDto> res =
            res(sysUser.getId(), sysUser.getComId(), 1, loginParam.getLoginType().getBelongServe().name());
        userInfo.setRes(new ResTypeDto(res));
        log.info("用户登录成功,账户[{}],用户信息[{}]", sysUser.getLoginName(), JSON.toJSONString(userInfo));
        // 删除该用户 cache
        stringRedisTemplate.delete(redisUserInfo + userInfo.getUserId());
        userInfo.checkSuccess();
        return userInfo;
    }

    /**
     * 获得用户权限并且缓存
     */
    private Set<ResDto> res(Integer userId, String comId, Integer resType, String server) {
        Set<ResDto> res = getRes(userId, comId, resType, server);
        if (res.isEmpty()) {
            throw new ServiceException("请先联系管理员配置权限再登陆 🚀");
        }
        cacheRes(res, userId);
        return res;
    }

    private Set<ResDto> getRes(Integer userId, String comId, Integer resType, String server) {
        // 获取公司所有权限列表  排除限制的列表
        Set<ResDto> resComSet = resMapper.getAllResUnderTheCompany(comId, resType, server);
        // 获得用户角色或者直接关联的资源
        Set<String> resCd = resMapper.getResCd(userId);
        // 为空 获取组织角色或者直接关联的资源
        if (resCd.isEmpty()) {
            resCd = resMapper.getResCdByCom(comId);
        }
        // 都没有关联到角色
        if (resCd.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ResDto> resSet = resMapper.getResByResCd(resCd);
        // 返回关联
        return Sets.intersection(resComSet, resSet);
    }

    private void cacheRes(Set<ResDto> res, Integer userId) {
        //删除原来的key
        stringRedisTemplate.delete(redisResPrefix + userId);
        String[] r = res.stream().map(ResDto::getResCd).toArray(String[]::new);
        if (r.length > 0) {
            stringRedisTemplate.opsForSet().add(redisResPrefix + userId, r);
            // 缓存30天
            stringRedisTemplate.expire(redisResPrefix + userId, 30, TimeUnit.DAYS);
        }
    }
}
