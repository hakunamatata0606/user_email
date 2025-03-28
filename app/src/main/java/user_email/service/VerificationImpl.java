package user_email.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import user_email.api.VerificationIf;

public class VerificationImpl implements VerificationIf{

    private static record UserInfo(String username, Instant issued) {}
    private final int timeout;

    private final HashMap<String, UserInfo> holderMap;

    public VerificationImpl(int timeout) {
        this.timeout = timeout;
        this.holderMap = new HashMap<>();
    }

    @Override
    public String createRecord(String username) {
        synchronized(holderMap) {
            List<Entry<String, UserInfo>> records = holderMap.entrySet().stream()
            .filter(entry -> entry.getValue().username() == username)
            .collect(Collectors.toList());
            assert records.size() <= 1;
            if (records.size() == 0) {
                return createRecordInternal(username);
            }
            Entry<String, UserInfo> entry = records.get(0);
            if (isExpired(entry.getValue().issued())) {
                holderMap.remove(entry.getKey());
                return createRecordInternal(username);
            }
            return entry.getKey();
        }
    }

    private String createRecordInternal(String username) {
        // only call when already hold lock
        Instant now = Instant.now();
        String record = UUID.randomUUID().toString();
        holderMap.put(record, new UserInfo(username, now));
        return record;
    }

    private boolean isExpired(Instant time) {
        return time.plusSeconds(this.timeout).isBefore(Instant.now());
    }

    @Override
    public String verifyRecord(String record) {
        String result = null;
        synchronized(holderMap) {
            UserInfo info = holderMap.get(record);
            if (info == null) {
                return null;
            }
            if (isExpired(info.issued())) {
                result = null;
            }else {
                result = info.username();
            }
            holderMap.remove(record);
        }
        return result;
    }

    @Override
    public boolean hasRecord(String username) {
        synchronized (holderMap) {
            for (UserInfo info : holderMap.values()) {
                if (username.equals(info.username())) {
                    return true;
                }
            }
        }
        return false;
    }
}
