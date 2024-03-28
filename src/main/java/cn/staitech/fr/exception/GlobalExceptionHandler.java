package cn.staitech.fr.exception;

import cn.staitech.common.core.domain.R;
import cn.staitech.common.core.exception.ServiceException;
import cn.staitech.common.core.exception.auth.NotPermissionException;
import cn.staitech.common.core.utils.StringUtils;
import cn.staitech.common.core.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

import static cn.staitech.common.core.constant.HttpStatus.FORBIDDEN;

/**
 * @title: GlobalExceptionHandler  配置全局异常捕获
 * @Author wangfeng
 * @Date: 2022-05-06
 * @Description：全局异常处理，参考：https://blog.csdn.net/Li_Ya_Fei/article/details/105609630
 * @RestControllerAdvice
 */
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 全局异常-未定义异常
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(HttpServletRequest request, Exception e) {
        // 请求状态
        HttpStatus status = getStatus(request);
        // 返回错误信息：失败代码、失败信息、具体代码、具体信息
        int code = status.value();
        String msg = status.getReasonPhrase();
        log.error(new StringBuilder(code).append(msg).toString());
        return R.fail(code, msg);
    }

    /**
     * 约束验证异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Object> validatorExceptionHandler(Exception e) {
        String msg = msgConvertor((ConstraintViolationException) e);

        log.error(msg);
        return R.fail(msg);
    }

    /**
     * 方法参数验证异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error(msg);
        return R.fail(msg);
    }

    private String msgConvertor(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> set = e.getConstraintViolations();
        StringBuilder stringBuilder = new StringBuilder();
        for (ConstraintViolation constraintViolation : set) {
            stringBuilder.append(constraintViolation.getMessage() + ",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        log.error(stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     * 获取请求状态
     *
     * @param request
     * @return
     */
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

    /**
     * 业务异常
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Object> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
        return R.fail(FORBIDDEN, "没有访问权限，请联系管理员授权");
    }
}