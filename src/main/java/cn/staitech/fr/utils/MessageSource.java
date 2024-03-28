package cn.staitech.fr.utils;

import cn.staitech.common.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化工具
 */
@Slf4j
public class MessageSource {

    private static org.springframework.context.MessageSource messageSource;

    public static void init(org.springframework.context.MessageSource messageSource) {
        MessageSource.messageSource = messageSource;
    }

    public static String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, "");
    }

    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }


    /**
     * 自定义Local Language
     * zh-cn 中文  en-us 英文
     *
     * @param code
     * @return
     */
    public static String M(String code) {
        String language = "";
        String response = "";

        if (SecurityUtils.getLoginUser() == null) {
            language = "zh";
        } else {
            if (SecurityUtils.getLoginUser().getLanguage() != null) {
                language = SecurityUtils.getLoginUser().getLanguage();
            }

            if ("en-us".equals(language)) {
                language = "en";
            } else {
                language = "zh";
            }
        }
        Locale locale = new Locale(language);
        try {
            response = messageSource.getMessage(code, null, locale);
        } catch (Exception e) {
            response = code;
            // log.info("{}", e);
        }

        return response;
    }

}
