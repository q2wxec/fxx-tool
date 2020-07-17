
package com.fxx.common.tools.concurrence;

/**
 * @author wangxiao1
 * @date 2020/7/16
 */
public interface DistributLockInter {

    Object getDistributLock(String key, long timeoutSeconds);

    void releaseLock(String key, String value);
}
