package com.alibaba.druid.spring.boot.ds.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SwitchRepository}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class SwitchRepositoryTest {

    @Test
    @DisplayName("SwitchRepository annotation exists and is retained at runtime")
    void annotationExists() throws Exception {
        Method method = SampleClass.class.getMethod("annotatedMethod");
        SwitchRepository annotation = method.getAnnotation(SwitchRepository.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Default value is MASTER_DATASOURCE")
    void defaultValue() throws Exception {
        Method method = SampleClass.class.getMethod("annotatedMethod");
        SwitchRepository annotation = method.getAnnotation(SwitchRepository.class);
        assertThat(annotation.value()).isEqualTo("defaultDataSource");
    }

    @Test
    @DisplayName("Custom value is set correctly")
    void customValue() throws Exception {
        Method method = SampleClass.class.getMethod("customAnnotatedMethod");
        SwitchRepository annotation = method.getAnnotation(SwitchRepository.class);
        assertThat(annotation.value()).isEqualTo("slave1");
    }

    @Test
    @DisplayName("Annotation is retained at runtime")
    void retentionPolicy() {
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Retention.class))
                .isNotNull();
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Retention.class).value())
                .isEqualTo(java.lang.annotation.RetentionPolicy.RUNTIME);
    }

    @Test
    @DisplayName("Annotation targets methods")
    void targetPolicy() {
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Target.class))
                .isNotNull();
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Target.class).value())
                .containsExactly(java.lang.annotation.ElementType.METHOD);
    }

    @Test
    @DisplayName("Annotation is documented")
    void documented() {
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Documented.class)).isNotNull();
    }

    @Test
    @DisplayName("Annotation is inherited")
    void inherited() {
        assertThat(SwitchRepository.class.getAnnotation(java.lang.annotation.Inherited.class)).isNotNull();
    }

    static class SampleClass {
        @SwitchRepository
        public void annotatedMethod() {
        }

        @SwitchRepository("slave1")
        public void customAnnotatedMethod() {
        }
    }
}
