package com.bx.implatform.session;

import com.bx.imcommon.enums.IMTerminalType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 会话上下文
 */
public class SessionContext {

//    // 使用ThreadLocal来存储UserSession
//    private static final ThreadLocal<UserSession> sessionThreadLocal = new ThreadLocal<>();  // 原本是直接获取 现在是 存储 UserSession 对象，方便添加ai的会话
//
//    public static UserSession getSession() {
//        // 首先尝试从ThreadLocal中获取
//        UserSession session = sessionThreadLocal.get();
//        if (session == null) {
//            // 如果ThreadLocal中没有，再尝试从请求上下文中获取
//            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//            if (requestAttributes != null) {
//                session = (UserSession) requestAttributes.getRequest().getAttribute("session");
//            }
//        }
//        return session;
//    }
//
//    public static void setSession(UserSession session) {
//        sessionThreadLocal.set(session); // 将UserSession存储到ThreadLocal中
//    }
//
//    public static void removeSession() {
//        sessionThreadLocal.remove(); // 从ThreadLocal中移除UserSession
//    }



    public static UserSession getSession() {
        // 从请求上下文里获取Request对象

        UserSession session = null;

        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            session = (UserSession) request.getAttribute("session");

        }catch (Exception e){
            System.out.println("获取session失败");
        }

        if(session == null){
            session = new UserSession();
            session.setUserId(1L);
            session.setTerminal(IMTerminalType.WEB.code()); // 默认web端
        }


        return session;
    }






}