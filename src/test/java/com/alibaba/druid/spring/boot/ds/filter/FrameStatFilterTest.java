package com.alibaba.druid.spring.boot.ds.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.druid.filter.stat.StatFilter;

/**
 * Tests for {@link FrameStatFilter}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class FrameStatFilterTest {

    @Test
    @DisplayName("FrameStatFilter extends StatFilter")
    void extendsStatFilter() {
        FrameStatFilter filter = new FrameStatFilter();
        assertThat(filter).isInstanceOf(StatFilter.class);
    }

    @Test
    @DisplayName("FrameStatFilter can be instantiated")
    void canBeInstantiated() {
        FrameStatFilter filter = new FrameStatFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("FrameStatFilter inherits StatFilter methods")
    void inheritsStatFilterMethods() {
        FrameStatFilter filter = new FrameStatFilter();
        // Verify default values from StatFilter
        assertThat(filter.isMergeSql()).isFalse();
        assertThat(filter.isConnectionStackTraceEnable()).isFalse();
    }
}
