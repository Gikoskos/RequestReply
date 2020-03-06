package distsys.rr;

import java.util.concurrent.locks.*;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

class ServiceHandler {
    private static final int[] services = new int[GlobalLimits.MAX_SERVICES];
    private static final ReentrantReadWriteLock services_rwl = new ReentrantReadWriteLock();
    private static final Hashtable<Integer, Semaphore> unansweredRequests = new Hashtable<Integer, Semaphore>();
    private static int registered_cnt = 0;

    public static int register(int svcid) {
        if (registered_cnt >= GlobalLimits.MAX_SERVICES) {
            System.out.println("Can't register more services");
            return -1;
        }

        if (svcid < 1) {
            System.out.println("Valid service numbers are 1 and up");
            return -1;
        }

        if (exists(svcid)) {
            System.out.println("Service " + svcid + " is already registered");
            return -1;
        }

        services_rwl.writeLock().lock();

        services[registered_cnt++] = svcid;

        Semaphore sem = new Semaphore(GlobalLimits.BUFFER_SIZE, true);
        sem.drainPermits();

        unansweredRequests.put(svcid, sem);

        services_rwl.writeLock().unlock();

        return 0;
    }

    public static int unregister(int svcid) {
        if (svcid < 1) {
            System.out.println("Valid service numbers are 1 and up");
            return -1;
        }

        if (exists(svcid)) {
            System.out.println("Service " + svcid + " is already registered");
            return -1;
        }

        services_rwl.writeLock().lock();
        for (int i = services.length - 1; i >= 0; i--) {
            if (services[i] == svcid) {
                for (int j = i; j < services.length - 1; j++) {
                    services[j] = services[j + 1];
                }
            }
        }

        unansweredRequests.remove(svcid);
        registered_cnt--;

        services_rwl.writeLock().unlock();

        return 0;
    }

    public static void releaseServiceRequest(int svcid) {
        Semaphore sem;

        services_rwl.readLock().lock();
        sem = unansweredRequests.get(svcid);
        services_rwl.readLock().unlock();

        try {
            sem.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean waitForServiceRequest(int svcid) {
        Semaphore sem;

        services_rwl.readLock().lock();
        sem = unansweredRequests.get(svcid);
        services_rwl.readLock().unlock();

        if (sem == null) {
            return false;
        }

        try {
            sem.acquire();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean exists(int svcid) {
        boolean res = false;

        services_rwl.readLock().lock();

        for (int service: services) {
             if (service == svcid) {
                res = true;
                break;
            }
        }
        services_rwl.readLock().unlock();

        return res;
    }
}
