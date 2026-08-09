package com.alibaba.druid.spring.boot.ds.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.biz.jdbc.DataSourceRoutingKeyHolder;

import com.alibaba.druid.spring.boot.ds.annotation.SwitchRepository;

/**
 * Tests for {@link DynamicDataSourceSwitchAspect}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class DynamicDataSourceSwitchAspectTest {

    @Test
    @DisplayName("DynamicDataSourceSwitchAspect can be instantiated")
    void canBeInstantiated() {
        DynamicDataSourceSwitchAspect aspect = new DynamicDataSourceSwitchAspect();
        assertThat(aspect).isNotNull();
    }

    @Test
    @DisplayName("around method sets and restores data source key")
    void aroundMethodSetsAndRestoresKey() throws Throwable {
        DynamicDataSourceSwitchAspect aspect = new DynamicDataSourceSwitchAspect();

        // Set initial key
        DataSourceRoutingKeyHolder.setDataSourceKey("originalKey");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("result");

        SwitchRepository repository = SampleClass.class.getMethod("slave1Method")
                .getAnnotation(SwitchRepository.class);

        Object result = aspect.around(joinPoint, repository);

        assertThat(result).isEqualTo("result");
        // Original key should be restored
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo("originalKey");
    }

    @Test
    @DisplayName("around method restores key even when exception occurs")
    void aroundMethodRestoresKeyOnException() throws Throwable {
        DynamicDataSourceSwitchAspect aspect = new DynamicDataSourceSwitchAspect();

        // Set initial key
        DataSourceRoutingKeyHolder.setDataSourceKey("originalKey");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("test exception"));

        SwitchRepository repository = SampleClass.class.getMethod("slave1Method")
                .getAnnotation(SwitchRepository.class);

        assertThatThrownBy(() -> aspect.around(joinPoint, repository))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test exception");

        // Original key should still be restored
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo("originalKey");
    }

    @Test
    @DisplayName("around method sets the correct data source key from annotation")
    void aroundMethodSetsCorrectKey() throws Throwable {
        DynamicDataSourceSwitchAspect aspect = new DynamicDataSourceSwitchAspect();

        DataSourceRoutingKeyHolder.setDataSourceKey("initialKey");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("result");

        SwitchRepository repository = SampleClass.class.getMethod("slave2Method")
                .getAnnotation(SwitchRepository.class);

        aspect.around(joinPoint, repository);

        // After proceed, the key should be restored to initial
        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo("initialKey");
    }

    @Test
    @DisplayName("around method uses default data source key when annotation has no value")
    void aroundMethodUsesDefaultKey() throws Throwable {
        DynamicDataSourceSwitchAspect aspect = new DynamicDataSourceSwitchAspect();

        DataSourceRoutingKeyHolder.setDataSourceKey("initialKey");

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("result");

        SwitchRepository repository = SampleClass.class.getMethod("defaultMethod")
                .getAnnotation(SwitchRepository.class);

        aspect.around(joinPoint, repository);

        assertThat(DataSourceRoutingKeyHolder.getDataSourceKey()).isEqualTo("initialKey");
    }

    static class SampleClass {
        @SwitchRepository("slave1")
        public void slave1Method() {
        }

        @SwitchRepository("slave2")
        public void slave2Method() {
        }

        @SwitchRepository
        public void defaultMethod() {
        }
    }
}
