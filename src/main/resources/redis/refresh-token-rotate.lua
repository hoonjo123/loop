local storedHash = redis.call('GET', KEYS[1])
if not storedHash or storedHash ~= ARGV[1] then
    return 0
end

redis.call('DEL', KEYS[1])
redis.call('SREM', KEYS[3], ARGV[2])
redis.call('SET', KEYS[2], ARGV[4], 'PX', ARGV[5])
redis.call('SADD', KEYS[3], ARGV[3])
redis.call('PEXPIRE', KEYS[3], ARGV[5])
return 1
