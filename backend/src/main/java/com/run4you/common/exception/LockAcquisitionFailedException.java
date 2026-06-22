package com.run4you.common.exception;

/** Redisson 분산 락 획득 실패 */
public class LockAcquisitionFailedException extends RuntimeException {
    public LockAcquisitionFailedException(String message) { super(message); }
}
