local sessionIds = redis.call('SMEMBERS', KEYS[1])
for _, sessionId in ipairs(sessionIds) do
    redis.call('DEL', ARGV[1] .. sessionId)
end
redis.call('DEL', KEYS[1])
return #sessionIds
