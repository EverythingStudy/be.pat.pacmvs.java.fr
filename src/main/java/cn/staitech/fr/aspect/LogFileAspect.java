package cn.staitech.fr.aspect;

import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author wudi
 * @Date 2023/6/25 9:29
 * @desc 日志文件打印
 */
@Aspect
@Component
public class LogFileAspect {
    private final static Logger logger = LoggerFactory.getLogger(LogFileAspect.class);

    public static HttpServletResponse response = null;
//
//    /**
//     * 过滤MultipartFile类型入参
//     *
//     * @param args joinPoint.getArgs()
//     * @return arguments 参数
//     */
//    public static Object[] filterParams(Object[] args) {
//        Object[] arguments = new Object[args.length];
//        for (int i = 0; i < args.length; i++) {
//            if (args[i] instanceof MultipartFile) {
//                continue;
//            }
//            arguments[i] = args[i];
//        }
//        return arguments;
//    }
//
//    /**
//     * 以自定义切点
//     */
//    @Pointcut("execution(* cn.staitech.fr.controller..*.*(..))")
//    public void logFile() {
//    }
//
//    /**
//     * 在切点之前织入
//     *
//     * @param joinPoint
//     * @throws Throwable
//     */
//    @Before("logFile()")
//    public void doBefore(JoinPoint joinPoint) throws Throwable {
//        // 开始打印请求日志
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        response = attributes.getResponse();
//        String requestURI = request.getRequestURI();
//        String[] split = requestURI.split("/");
//        MDC.put("logFileName", split[split.length - 2]);
//
//        // 打印请求相关参数
//        logger.info("========================================== Start ==========================================");
//        // 打印请求 url
//        logger.info("URL            : {}", request.getRequestURL().toString());
//        // 打印 Http method
//        logger.info("HTTP Method    : {}", request.getMethod());
//        // 打印调用 controller 的全路径以及执行方法
//        logger.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
//        // 打印请求的 IP
//        logger.info("IP             : {}", request.getRemoteAddr());
//        // 打印请求入参
//        Object[] res = filterParams(joinPoint.getArgs());
//        logger.info("Request Args   : {}", (JSONObject.toJSONString(res)));
//
//
//    }
//
//    /**
//     * 在切点之后织入
//     *
//     * @throws Throwable
//     */
//    @After("logFile()")
//    public void doAfter() throws Throwable {
//        // 接口结束后换行，方便分割查看
//        logger.info("=========================================== End ===========================================" + System.lineSeparator());
//    }
//
//    /**
//     * 环绕
//     *
//     * @param proceedingJoinPoint
//     * @return
//     * @throws Throwable
//     */
//    @Around("logFile()")
//    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//        Object result = proceedingJoinPoint.proceed();
//        // 打印出参
//        logger.info("Response Args  : {}", JSONObject.toJSONString(result));
//        // 执行耗时
//        logger.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
//        //清空防止内存泄漏
//        MDC.clear();
//        return result;
//    }
}
