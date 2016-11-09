package df.open.support.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
@ConfigurationProperties(CloudConfigSupportProperties.CONFIG_PREFIX)
@Data
public class CloudConfigSupportProperties {

    public static final String CONFIG_PREFIX = "spring.cloud.config.backup";

    private boolean enable = false;

    private String file = "backup.properties";

}
