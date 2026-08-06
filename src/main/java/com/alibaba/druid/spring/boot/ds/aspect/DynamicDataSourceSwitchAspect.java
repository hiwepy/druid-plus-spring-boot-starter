package com.alibaba.druid.spring.boot.ds.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.biz.jdbc.DataSourceRoutingKeyHolder;
import org.springframework.stereotype.Component;

import com.alibaba.druid.spring.boot.ds.annotation.SwitchRepository;

/**
 * 数据源自动切换切面
 * @author 		： <a href="https://github.com/easy-4-java">hiwepy</a>
 */
@Aspect
@Component
public class DynamicDataSourceSwitchAspect {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	//环绕通知   
	@Around("@annotation(com.alibaba.druid.spring.boot.ds.annotation.SwitchRepository) && @annotation(repository)")
	public Object around(ProceedingJoinPoint joinPoint, SwitchRepository repository) throws Throwable {
		String oldRepository = DataSourceRoutingKeyHolder.getDataSourceKey();
    	try {
    		DataSourceRoutingKeyHolder.setDataSourceKey(repository.value());
    		return joinPoint.proceed();
        } finally {
        	if (logger.isDebugEnabled()) {
        		logger.debug("invoke(ProceedingJoinPoint) - end"); //$NON-NLS-1$
            }
    		DataSourceRoutingKeyHolder.setDataSourceKey(oldRepository);
        }
    }  
	
}
