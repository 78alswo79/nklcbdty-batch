package com.nklcbdty.batch.nklcbdty.batch.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CustomBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // jobRegistryBeanPostProcessor 빈 정의를 제거
        if (registry.containsBeanDefinition("jobRegistryBeanPostProcessor")) {
            registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
        // Nothing to do here
    }

}
