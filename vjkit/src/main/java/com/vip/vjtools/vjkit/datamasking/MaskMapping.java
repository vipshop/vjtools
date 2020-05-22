package com.vip.vjtools.vjkit.datamasking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 加载系统定义的和用户定义的mapping配置
 */
public class MaskMapping {

	private static final Logger logger = LoggerFactory.getLogger(MaskMapping.class);

	private static final String SYS_MASK_MAPPING = "sys_data_mask.properties";//系统定义的
	private static final String USER_MASK_MAPPING = "data_mask.properties";//用户自定义的

	private static final Map<String, SensitiveType> mappings = new HashMap<>();

	static {
		init();
	}

	/**
	 * 初始化
	 * 加载系统定义的和用户自定义的掩码字段映射
	 */
	private static void init() {
		loadPropertyFromFile(SYS_MASK_MAPPING);
		loadPropertyFromFile(USER_MASK_MAPPING);
	}

	/**
	 * 加载掩码映射文件
	 */
	private static void loadPropertyFromFile(String resourcePath) {


		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		boolean isUserMapping = resourcePath.equals(USER_MASK_MAPPING);

		try {
			//加载所有的用户自定义mapping配置
			Enumeration<URL> paths = loader.getResources(resourcePath);

			//没有配置文件
			if (paths == null || !paths.hasMoreElements()) {
				logger.info("data-masking property file don't exist:{}", resourcePath);
				return;
			}

			while (paths.hasMoreElements()) {
				URL path = paths.nextElement();
				//打印路径
				logger.info("load data-masking property file:{},path is {}", resourcePath, path);


				try (InputStream resourceStream = path.openStream()) {

					if (resourceStream == null) {
						logger.info("data-masking property file's stream is null:{},{}", path, resourcePath);
						return;
					}

					props.load(resourceStream);


					for (Map.Entry<Object, Object> prop : props.entrySet()) {
						String type = prop.getKey().toString().trim();
						String values = prop.getValue().toString().trim();
						String[] maps = values.split(",");
						SensitiveType sensitiveType = SensitiveType.valueOf(type);

						for (String map : maps) {
							mappings.put(map, sensitiveType);

							if (isUserMapping) {
								logger.info("load user mask mapping {}={},from path:{}", sensitiveType, map, path);
							}
						}

					}
				} catch (IOException e) {
					logger.warn("load data-masking property file error!,{}", path, e);
				}
			}


		} catch (IOException e) {
			logger.warn("load data-masking property file error!", e);
		}

	}


	public static SensitiveType getMaskTypeMapping(String fieldName) {
		return mappings.get(fieldName);
	}

}
