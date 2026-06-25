package com.run4you.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 사용자(userId)별 SSE Emitter 보관소.
 *
 * <p>한 사용자가 여러 탭/기기로 접속할 수 있으므로 userId → Emitter 리스트(멀티맵)로 관리한다.
 * 단일 인스턴스 메모리 저장이며, 스케일아웃 시에는 Redis Pub/Sub 등으로 팬아웃을 확장한다.
 */
@Slf4j
@Repository
public class SseEmitterRepository {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> store = new ConcurrentHashMap<>();

    public void add(Long userId, SseEmitter emitter) {
        store.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();          // onCompletion 콜백에서 remove 됨
        });
        emitter.onError(e -> remove(userId, emitter));

        log.debug("[SSE] 구독 등록 userId={}, 현재 연결 수={}", userId, count(userId));
    }

    public List<SseEmitter> get(Long userId) {
        return store.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    public void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = store.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                store.remove(userId, list);
            }
        }
    }

    public int count(Long userId) {
        CopyOnWriteArrayList<SseEmitter> list = store.get(userId);
        return list == null ? 0 : list.size();
    }

    /** 현재 구독 중인 사용자 id 스냅샷 (하트비트 순회용) */
    public Set<Long> userIds() {
        return new HashSet<>(store.keySet());
    }
}
