package com.xxx.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 配置类
 *
 * @author click33
 */
@Configuration
public class SaTokenConfigure {

    @Value("${common.public.urls}")
    private String[] urls;

    @Bean
    public SaReactorFilter getSaReactorFilter() {

        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(urls)
                .setAuth(obj -> {
                    SaRouter.match("/**",  r -> StpUtil.checkLogin());
                })
                .setError(e -> SaResult.error(e.getMessage()));
    }
}
