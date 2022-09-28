package com.zhang.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.zhang.reggie.common.BaseContext;
import com.zhang.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 目的：为了拦截向controller发起的请求 页面想看就看
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器, 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到的请求:"+request.getRequestURI());

        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login",

        };

        //2、判断本次请求是否需要处理

        boolean check = check(urls, requestURI);

        //3、如果不需要处理，直接放行
        if (check){
            log.info("本次请求不需要处理:"+request.getRequestURI());
            filterChain.doFilter(request,response);//放行
            return;
        }

        //4、判断登入状态，如果已经登入，直接放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已经登入，用户id为："+ request.getSession().getAttribute("employee"));

            long id = (long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);

           /* long id = Thread.currentThread().getId();
            log.info("线程id为{}",id);*/

            filterChain.doFilter(request,response);//放行
            return;
        }
        //4-2 判断移动端用户登入状态   移动端
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已经登入，用户id为："+ request.getSession().getAttribute("user"));

            long userId = (long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

           /* long id = Thread.currentThread().getId();
            log.info("线程id为{}",id);*/

            filterChain.doFilter(request,response);//放行
            return;
        }

        log.info("用户未登入");
        //5、如果未登入则返回未登入结果,通过输出流当时向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){//匹配上了 直接返回
                return true;
            }
        }
        return false;
    }
}
