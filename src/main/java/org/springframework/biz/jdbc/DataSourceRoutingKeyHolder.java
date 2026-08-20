/*
 * Copyright 2021 easy4j.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.biz.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceRoutingKeyHolder {

    public static final String MASTER_DATASOURCE = "defaultDataSource";

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRoutingKeyHolder.class);

    private static final Random RANDOM = new Random();

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final ThreadLocal<String> CONTEXT_HOLDER = ThreadLocal.withInitial(
            DataSourceRoutingKeyHolder::useMasterDataSourceInternal);

    public static List<Object> dataSourceKeys = new ArrayList<>();

    private static String useMasterDataSourceInternal() {
        return MASTER_DATASOURCE;
    }

    public static void setDataSourceKey(String dataSourceKey) {
        if (dataSourceKey == null || dataSourceKey.trim().isEmpty()) {
            CONTEXT_HOLDER.set(MASTER_DATASOURCE);
        } else {
            CONTEXT_HOLDER.set(dataSourceKey);
        }
    }

    private static void useMasterDataSource() {
        CONTEXT_HOLDER.set(MASTER_DATASOURCE);
    }

    public static void useSlaveDataSource() {
        LOCK.lock();
        try {
            if (dataSourceKeys.isEmpty()) {
                CONTEXT_HOLDER.set(MASTER_DATASOURCE);
                return;
            }
            List<Object> slaveKeys = new ArrayList<>();
            for (Object k : dataSourceKeys) {
                if (!MASTER_DATASOURCE.equals(String.valueOf(k))) {
                    slaveKeys.add(k);
                }
            }
            if (slaveKeys.isEmpty()) {
                CONTEXT_HOLDER.set(MASTER_DATASOURCE);
                return;
            }
            Object picked = slaveKeys.get(RANDOM.nextInt(slaveKeys.size()));
            CONTEXT_HOLDER.set(String.valueOf(picked));
        } finally {
            LOCK.unlock();
        }
    }

    public static String getDataSourceKey() {
        String key = CONTEXT_HOLDER.get();
        if (key == null) {
            return MASTER_DATASOURCE;
        }
        return key;
    }
    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }

    public static boolean containDataSourceKey(String dataSourceKey) {
        if (dataSourceKey == null) {
            return false;
        }
        for (Object k : dataSourceKeys) {
            if (dataSourceKey.equals(String.valueOf(k))) {
                return true;
            }
        }
        return false;
    }
}
