package distsys.rr;

import java.util.concurrent.locks.ReentrantReadWriteLock;

class PacketBuffer {
    private static final ReentrantReadWriteLock packetBufferLock = new ReentrantReadWriteLock();
    private static final ProtocolPacket[] packetBuffer = new ProtocolPacket[GlobalLimits.BUFFER_SIZE];

    public static int insert(ProtocolPacket packet) {
        int id = GlobalLimits.INVALID_PACKET_ID;

        for (int i = 0; i < packetBuffer.length; i++) {
            if (packetBuffer[i] == null) {
                packet.setPacketId(i);
                packetBuffer[i] = packet;
                id = i;
                break;
            } else if (packetBuffer[i].getNetworkId() == packet.getNetworkId()) {
                break;
            }
        }

        return id;
    }

    public static void remove(int id) {
        if (id >= 0 && id < packetBuffer.length) {
            packetBuffer[id] = null;
        }
    }

    public static ProtocolPacket getPacket(RequestState state) {
        ProtocolPacket p = null;

        rLock();
        for (ProtocolPacket packet: packetBuffer) {
            if (packet != null && packet.getState() == state) {
                p = packet;
                break;
            }
        }
        rUnlock();

        return p;
    }

    public static ProtocolPacket getPacket(RequestState state, int svcid) {
        ProtocolPacket p = null;

        rLock();
        for (ProtocolPacket packet: packetBuffer) {
            if (packet != null && packet.getState() == state && packet.getServiceId() == svcid) {
                p = packet;
                break;
            }
        }
        rUnlock();

        return p;
    }

    public static ProtocolPacket getPacket(int reqid) {
        ProtocolPacket p = null;

        rLock();
        if (reqid >= 0 && reqid < packetBuffer.length) {
            p = packetBuffer[reqid];
        }
        rUnlock();

        return p;
    }

    public static boolean checkDuplicates(ProtocolPacket packet) {
        boolean duplicatesExist = false;
        int netid = packet.getNetworkId(), packetid = packet.getPacketId();

        for (int i = 0; i < packetBuffer.length; i++) {
            //check if we have a duplicate
            if (
                packetBuffer[i] != null &&
                packetBuffer[i].getNetworkId() == netid &&
                packetid != packetBuffer[i].getPacketId()
               ) {

                //discard the packet that is at an earlier stage of processing
                if (packet.getState().compareTo(packetBuffer[i].getState()) >= 0) {
                    packetBuffer[i] = null;
                } else {
                    packetBuffer[packetid] = null;
                }

                duplicatesExist = true;
                break;
            }
        }

        return duplicatesExist;
    }

    public static void rLock() {
        packetBufferLock.readLock().lock();
    }

    public static void rUnlock() {
        packetBufferLock.readLock().unlock();
    }

    public static void wLock() {
        packetBufferLock.writeLock().lock();
    }

    public static void wUnlock() {
        packetBufferLock.writeLock().unlock();
    }
}