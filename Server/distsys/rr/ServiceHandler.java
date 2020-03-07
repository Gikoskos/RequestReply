package distsys.rr;

import java.util.concurrent.locks.*;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ArrayBlockingQueue;

class ServiceHandler {
    private static final int[] services = new int[GlobalLimits.MAX_SERVICES];
    private static final ReentrantReadWriteLock services_rwl = new ReentrantReadWriteLock();
    private static final Hashtable<Integer, ArrayBlockingQueue<RequestData>> unansweredRequests = new Hashtable<Integer, ArrayBlockingQueue<RequestData>>();
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

        services[registered_cnt++] = svcid;

        ArrayBlockingQueue<RequestData> queue = new ArrayBlockingQueue<RequestData>(GlobalLimits.BUFFER_SIZE, true);

        unansweredRequests.put(svcid, queue);


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

        for (int i = services.length - 1; i >= 0; i--) {
            if (services[i] == svcid) {
                for (int j = i; j < services.length - 1; j++) {
                    services[j] = services[j + 1];
                }
            }
        }

        unansweredRequests.remove(svcid);
        registered_cnt--;

        return 0;
    }

    public static void putServiceRequest(int svcid, RequestData data) {
        ArrayBlockingQueue<RequestData> queue = unansweredRequests.get(svcid);

        try {
            queue.put(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RequestData takeServiceRequest(int svcid) {
        ArrayBlockingQueue<RequestData> queue = unansweredRequests.get(svcid);
        RequestData data = null;

        if (queue != null) {
            try {
                data = queue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public static boolean exists(int svcid) {
        boolean res = false;

        for (int service: services) {
             if (service == svcid) {
                res = true;
                break;
            }
        }

        return res;
    }
}
