import org.junit.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 说明:
 * <p/>
 * Copyright: Copyright (c)
 * <p/>
 * Company: 江苏千米网络科技有限公司
 * <p/>
 *
 * @author 付亮(OF2101)
 * @version 1.0.0
 * @date 2016/11/9
 */
public class TestProperty {

    @Test
    public void testWithPropertiesFile() throws Exception {
        PropertiesFactoryBean pfb = new PropertiesFactoryBean();
        FileSystemResource pathResource = new FileSystemResource("test.properties");
        File file = pathResource.getFile();
        if (!file.exists()) {
            file.createNewFile();
        }
        System.out.println("pathResource:" + pathResource.getPath());
        System.out.println("pathResource:" + pathResource.getFile());
        pfb.setLocation(pathResource);
        pfb.afterPropertiesSet();
        Properties props = (Properties) pfb.getObject();
        System.out.println(props.get("a"));
        props.setProperty("a", "DDD");
        System.out.println(props.get("a"));
        FileOutputStream oFile = new FileOutputStream(pathResource.getFile());
        props.store(oFile, "Comment");
        oFile.close();
        file.deleteOnExit();
    }
}
