package df.open.support.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 说明:
 * <p/>
 * Copyright: Copyright (c)
 * <p/>
 * Company:
 * <p/>
 *
 * @author 付亮
 * @version 1.0.0
 * @date 2016/11/9
 */
@Component
public class ApplicationRefresnListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //TODO : Refresh Backup when config refreshed
        System.out.println("ApplicationRefresnListener###");
    }
}
