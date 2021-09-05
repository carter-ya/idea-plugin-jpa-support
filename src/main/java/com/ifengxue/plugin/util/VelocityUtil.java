package com.ifengxue.plugin.util;

import com.ifengxue.plugin.i18n.LocaleContextHolder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

public enum VelocityUtil {
    ;
    private static volatile VelocityEngine instance;

    public static VelocityEngine getInstance() {
        if (instance == null) {
            synchronized (VelocityUtil.class) {
                if (instance == null) {
                    instance = new VelocityEngine();
                    // rewrite LogChute Avoid access denied exceptions (velocity.log)
                    // link: https://github.com/carter-ya/idea-plugin-jpa-support/issues/4
                    instance.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new MyLogChute());
                    String encoding = StandardCharsets.UTF_8.name();
                    instance.addProperty("input.encoding", encoding);
                    instance.addProperty("output.encoding", encoding);
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static void fillContext(VelocityContext ctx) {
        ctx.put("USER", System.getProperty("user.name"));
        LocalDateTime now = LocalDateTime.now();
        ctx.put("DATE", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        ctx.put("TIME", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        ctx.put("YEAR", now.format(DateTimeFormatter.ofPattern("yyyy")));
        ctx.put("MONTH", now.format(DateTimeFormatter.ofPattern("MM")));
        ctx.put("DAY", now.format(DateTimeFormatter.ofPattern("dd")));
        ctx.put("HOUR", now.format(DateTimeFormatter.ofPattern("HH")));
        ctx.put("MINUTE", now.format(DateTimeFormatter.ofPattern("mm")));
        ctx.put("SECOND", now.format(DateTimeFormatter.ofPattern("ss")));

        StringHelper helper = new StringHelper();
        ctx.put("stringHelper", helper);
        ctx.put("StringHelper", helper);

        StringUtils stringUtils = new StringUtils();
        ctx.put("StringUtils", stringUtils);

        ctx.put("LocaleContextHolder", new LocaleContextHolder());
    }
}
