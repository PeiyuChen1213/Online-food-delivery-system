package com.chenpeiyu.filter;

import com.alibaba.fastjson.JSON;
import com.chenpeiyu.common.BaseContext;
import com.chenpeiyu.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 配置一个过滤器，检查用户是否已经完成登录，如果登录完成，则正常显示其他页面
 * 如果未登录，则跳转到登录界面
 */
//将所有的url都拦截然后再进行判断
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j//日志相关的注解
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        String requestURI = request.getRequestURI();
        //设置拦截请求，将请求输出至后台的控制台
        log.info("拦截到请求" + requestURI);

        //定义不需要经过过滤器处理的url

        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
        };

        //2 判断本次请求是否需要被处理

        boolean check = check(urls, requestURI);

        //如果请求路径在数组的范围里，则不需要处理，直接放行

        if (check) {
            log.info("请求不需要处理" + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //同时，还要判断登录状态，如果已经登录，则直接放行
        //根据浏览器内部储存的session来进行判断

        if (request.getSession().getAttribute("employee") != null) {
            log.info("说明用户已经登录，可以获得其用户id：" + request.getSession().getAttribute("employee"));

            //在过滤器上设置这个方法更有通用性，这样不用在每个控制器上都添加相关的方法
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            //用户已经登录就可以直接放行
            filterChain.doFilter(request, response);
            log.info("放行" + requestURI);
            //拦截各个请求之后，获取每个请求的uri
            return;
        }

        //判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        //如果用户未登录，则通过输出流的方式向页面响应数据,因为这个项目前端自带了一个拦截器
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }


    /**
     * 进行路径匹配，确认是否直接放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
