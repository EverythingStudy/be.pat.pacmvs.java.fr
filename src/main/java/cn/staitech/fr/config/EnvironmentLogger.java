package cn.staitech.fr.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentLogger {

    private final ConfigurableEnvironment env;

    public EnvironmentLogger(ConfigurableEnvironment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printPropertySources() {
        System.out.println("========== 所有 PropertySource 列表 ==========");
        env.getPropertySources().forEach(ps ->
                System.out.println(ps.getName() + " -> " + ps)
        );
        System.out.println("==========================================");
    }
}
