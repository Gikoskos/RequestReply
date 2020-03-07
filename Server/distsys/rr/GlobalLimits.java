package distsys.rr;

class GlobalLimits {
    public static final int BUFFER_SIZE = 32,
                            POOL_SIZE = 4,
                            MAX_SERVICES = 64,
                            DGRAM_LEN = 32,
                            DGRAM_BIG_LEN = 512,
                            SOCKET_TIMEOUT = 2 * 1000,
                            SEND_REPLY_TRIES = 3,
                            GET_REQUEST_TRIES = 3;
}
