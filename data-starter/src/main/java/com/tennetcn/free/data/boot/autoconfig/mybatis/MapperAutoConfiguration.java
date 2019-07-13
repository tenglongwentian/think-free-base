package com.tennetcn.free.data.boot.autoconfig.mybatis;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.sql.DataSource;

import com.tennetcn.free.core.utils.CommonApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.spring.annotation.BaseProperties;
import tk.mybatis.spring.mapper.ClassPathMapperScanner;
import tk.mybatis.spring.mapper.MapperFactoryBean;
import tk.mybatis.spring.mapper.SpringBootBindUtil;

@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({com.tennetcn.free.data.boot.autoconfig.mybatis.MybatisProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@AutoConfigureBefore(
		name = {"org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"}
)
@Slf4j
public class MapperAutoConfiguration implements InitializingBean, ApplicationContextAware {
	private final MybatisProperties properties;
	private final Interceptor[] interceptors;
	private final ResourceLoader resourceLoader;
	private final DatabaseIdProvider databaseIdProvider;
	private final List<ConfigurationCustomizer> configurationCustomizers;

	public MapperAutoConfiguration(MybatisProperties properties, ObjectProvider<Interceptor[]> interceptorsProvider, ResourceLoader resourceLoader, ObjectProvider<DatabaseIdProvider> databaseIdProvider, ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
		this.properties = properties;
		this.interceptors = (Interceptor[])interceptorsProvider.getIfAvailable();
		this.resourceLoader = resourceLoader;
		this.databaseIdProvider = (DatabaseIdProvider)databaseIdProvider.getIfAvailable();
		this.configurationCustomizers = (List)configurationCustomizersProvider.getIfAvailable();
	}

	public void afterPropertiesSet() {
		this.checkConfigFileExists();
	}

	private void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
			Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
			Assert.state(resource.exists(), "Cannot find config location: " + resource + " (please add config file or check your Mybatis configuration)");
		}

	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setVfs(SpringBootVFS.class);
		if (StringUtils.hasText(this.properties.getConfigLocation())) {
			factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
		}

		this.applyConfiguration(factory);
		if (this.properties.getConfigurationProperties() != null) {
			factory.setConfigurationProperties(this.properties.getConfigurationProperties());
		}

		if (!ObjectUtils.isEmpty(this.interceptors)) {
			factory.setPlugins(this.interceptors);
		}

		if (this.databaseIdProvider != null) {
			factory.setDatabaseIdProvider(this.databaseIdProvider);
		}

		if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
			factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
		}

		if (this.properties.getTypeAliasesSuperType() != null) {
			factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
		}

		if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
			factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
		}

		if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
			factory.setMapperLocations(this.properties.resolveMapperLocations());
		}

		return factory.getObject();
	}

	private void applyConfiguration(SqlSessionFactoryBean factory) {
		org.apache.ibatis.session.Configuration configuration = this.properties.getConfiguration();
		if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
			configuration = new org.apache.ibatis.session.Configuration();
		}

		if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
			Iterator var3 = this.configurationCustomizers.iterator();

			while(var3.hasNext()) {
				ConfigurationCustomizer customizer = (ConfigurationCustomizer)var3.next();
				customizer.customize(configuration);
			}
		}

		factory.setConfiguration(configuration);
	}

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		ExecutorType executorType = this.properties.getExecutorType();
		return executorType != null ? new SqlSessionTemplate(sqlSessionFactory, executorType) : new SqlSessionTemplate(sqlSessionFactory);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CommonApplicationContextUtil.setCurrentContext(applicationContext);
	}

	@Configuration
	@ConditionalOnProperty(
			prefix = "spring.devtools.restart",
			name = {"enabled"},
			matchIfMissing = true
	)
	static class RestartConfiguration {
		RestartConfiguration() {
		}

		@Bean
		public MapperCacheDisabler mapperCacheDisabler() {
			return new MapperCacheDisabler();
		}
	}

	@Configuration
	@Import({MapperAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class})
	@ConditionalOnMissingBean({MapperFactoryBean.class})
	public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {
		public MapperScannerRegistrarNotFoundConfiguration() {
		}

		public void afterPropertiesSet() {
			MapperAutoConfiguration.log.debug("No {} found.", MapperFactoryBean.class.getName());
		}
	}

	public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
		private BeanFactory beanFactory;
		private ResourceLoader resourceLoader;
		private Environment environment;

		public AutoConfiguredMapperScannerRegistrar() {
		}

		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			MapperAutoConfiguration.log.debug("Searching for mappers annotated with @Mapper");
			ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
			scanner.setMapperProperties(this.environment);

			try {
				if (this.resourceLoader != null) {
					scanner.setResourceLoader(this.resourceLoader);
				}

				List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
				if (MapperAutoConfiguration.log.isDebugEnabled()) {
					Iterator var5 = packages.iterator();

					while(var5.hasNext()) {
						String pkg = (String)var5.next();
						MapperAutoConfiguration.log.debug("Using auto-configuration base package '{}'", pkg);
					}
				}

				BaseProperties properties = (BaseProperties)SpringBootBindUtil.bind(this.environment, BaseProperties.class, "mybatis");
				if (properties != null && properties.getBasePackages() != null && properties.getBasePackages().length > 0) {
					packages.addAll(Arrays.asList(properties.getBasePackages()));
				} else {
					scanner.setAnnotationClass(Mapper.class);
				}

				scanner.registerFilters();
				scanner.doScan(StringUtils.toStringArray(packages));
			} catch (IllegalStateException var7) {
				MapperAutoConfiguration.log.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.", var7);
			}

		}

		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		public void setEnvironment(Environment environment) {
			this.environment = environment;
		}

		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}



}