import redis

class SBP_Redis_Wrapper:
    def __init__(self, host='localhost', port=6379, db=0, debug=False):
        self.host = host
        self.port = port
        self.db = 0
        self.r = redis.Redis(host='localhost', port=6379, db=0)
        self.debug = debug

    def set(self, key, value):
        status = self.r.set(key, value)
        if self.debug:
            print("[REDIS] set: " + key + " (" + str(value) + ")")
        return 

    def get(self, key):
        if self.debug:
            print("[REDIS] get: " + key)
        return self.r.get(key)