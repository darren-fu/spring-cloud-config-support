package df.open.support.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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
@Configuration
@EnableConfigurationProperties(CloudConfigSupportProperties.class)
public class CloudConfigSupportConfiguration implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static Logger logger = LoggerFactory.getLogger(CloudConfigSupportConfiguration.class);

    private int order = Ordered.HIGHEST_PRECEDENCE + 11;

    @Autowired(required = false)
    private List<PropertySourceLocator> propertySourceLocators = Collections.EMPTY_LIST;


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!isHasCloudConfigLocator(this.propertySourceLocators)) {
            logger.info("未启用Config Server管理配置");
            return;
        }


        logger.info("检查Config Service配置资源");

        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        MutablePropertySources propertySources = environment.getPropertySources();
        logger.info("加载PropertySources源：" + propertySources.size() + "个");

        CloudConfigSupportProperties configSupportProperties = new CloudConfigSupportProperties();
        new RelaxedDataBinder(configSupportProperties, CloudConfigSupportProperties.CONFIG_PREFIX)
                .bind(new PropertySourcesPropertyValues(propertySources));
        if (!configSupportProperties.isEnable()) {
            logger.warn("未启用配置备份功能，可使用{}.enable打开", CloudConfigSupportProperties.CONFIG_PREFIX);
            return;
        }


        if (isCloudConfigLoaded(propertySources)) {
            PropertySource cloudConfigSource = getLoadedCloudPropertySource(propertySources);
            logger.info("成功获取ConfigService配置资源");
            //备份
            Map<String, Object> backupPropertyMap = makeBackupPropertyMap(cloudConfigSource);
            doBackup(backupPropertyMap, configSupportProperties.getFile());

        } else {
            logger.error("获取ConfigService配置资源失败");

            Properties backupProperty = loadBackupProperty(configSupportProperties.getFile());
            HashMap backupSourceMap = new HashMap<>(backupProperty);

            PropertySource backupSource = new MapPropertySource("backupSource", backupSourceMap);
            propertySources.addFirst(backupSource);

        }
    }

    /**
     * 是否启用了Spring Cloud Config获取配置资源
     *
     * @param propertySourceLocators
     * @return
     */
    private boolean isHasCloudConfigLocator(List<PropertySourceLocator> propertySourceLocators) {
        for (PropertySourceLocator sourceLocator : propertySourceLocators) {
            if (sourceLocator instanceof ConfigServicePropertySourceLocator) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否启用Cloud Config
     *
     * @param propertySources
     * @return
     */
    private boolean isCloudConfigLoaded(MutablePropertySources propertySources) {
        if (getLoadedCloudPropertySource(propertySources) == null) {
            return false;
        }
        return true;
    }

    /**
     * 获取加载的Cloud Config 配置项
     *
     * @param propertySources
     * @return
     */
    private PropertySource getLoadedCloudPropertySource(MutablePropertySources propertySources) {
        if (!propertySources.contains(PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            return null;
        }
        PropertySource propertySource = propertySources.get(PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME);
        if (propertySource instanceof CompositePropertySource) {
            for (PropertySource<?> source : ((CompositePropertySource) propertySource).getPropertySources()) {
                if (source.getName().equals("configService")) {
                    return source;
                }
            }
        }
        return null;
    }


    /**
     * 生成备份的配置数据
     *
     * @param propertySource
     * @return
     */
    private Map<String, Object> makeBackupPropertyMap(PropertySource propertySource) {
//        PropertySource backupSource = new MapPropertySource("backupSource", backupSourceMap);
        Map<String, Object> backupSourceMap = new HashMap<>();

        if (propertySource instanceof CompositePropertySource) {
            CompositePropertySource composite = (CompositePropertySource) propertySource;
            for (PropertySource<?> source : composite.getPropertySources()) {
                if (source instanceof MapPropertySource) {
                    MapPropertySource mapSource = (MapPropertySource) source;
                    for (String propertyName : mapSource.getPropertyNames()) {
                        // 前面的配置覆盖后面的配置
                        if (!backupSourceMap.containsKey(propertyName)) {
                            backupSourceMap.put(propertyName, mapSource.getProperty(propertyName));
                        }
                    }
                }
            }
        }
        return backupSourceMap;
    }

    private void doBackup(Map<String, Object> backupPropertyMap, String filePath) {
        FileSystemResource fileSystemResource = new FileSystemResource(filePath);
        File backupFile = fileSystemResource.getFile();
        try {
            if (!backupFile.exists()) {
                backupFile.createNewFile();
            }
            if (!backupFile.canWrite()) {
                logger.error("无法读写文件：{}", fileSystemResource.getPath());
            }

            Properties properties = new Properties();
            Iterator<String> keyIterator = backupPropertyMap.keySet().iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                properties.setProperty(key, String.valueOf(backupPropertyMap.get(key)));
            }

            FileOutputStream fos = new FileOutputStream(fileSystemResource.getFile());
            properties.store(fos, "Backup Cloud Config");
        } catch (IOException e) {
            logger.error("文件操作失败：{}", fileSystemResource.getPath());
            e.printStackTrace();
        }
    }

    private Properties loadBackupProperty(String filePath) {
        PropertiesFactoryBean propertiesFactory = new PropertiesFactoryBean();
        Properties props = new Properties();
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(filePath);
            propertiesFactory.setLocation(fileSystemResource);

            propertiesFactory.afterPropertiesSet();
            props = propertiesFactory.getObject();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }


    @Override
    public int getOrder() {
        return this.order;
    }
}
