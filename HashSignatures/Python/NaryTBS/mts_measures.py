

class MTSMeasures:
    def __init__(self, hash_function_name, arity, use_proxy_node):
        self.hash_function_name = hash_function_name
        self.arity = arity
        self.use_proxy_node = use_proxy_node

        self.sign_time = []
        self.sign_creation_time = []
        self.vrfy_time = []
        self.sign_size = []
