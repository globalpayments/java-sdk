package com.global.api.tests.network.nts;

import com.global.api.utils.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Tests to verify that the NTS masking logic is thread-safe.
 * Each thread should see only its own sensitive data via ThreadLocal storage.
 */
public class NtsMaskingThreadSafetyTest {

    private static final int THREAD_COUNT = 50;
    private static final int ITERATIONS_PER_THREAD = 100;

    @Test
    public void test_accNo_threadIsolation() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final String threadAccNo = "4000001234560" + String.format("%03d", i);
            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // all threads start together
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        StringUtils.setAccNo(threadAccNo);
                        Thread.yield(); // encourage interleaving
                        String retrieved = StringUtils.getAccNo();
                        if (!threadAccNo.equals(retrieved)) {
                            failures.add("Expected: " + threadAccNo + " but got: " + retrieved);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    StringUtils.setAccNo(null); // cleanup
                }
            }));
        }

        latch.countDown(); // release all threads
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertTrue("Thread isolation failures for accNo: " + failures, failures.isEmpty());
    }

    @Test
    public void test_expDate_threadIsolation() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final String threadExpDate = "12" + String.format("%02d", i % 100);
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        StringUtils.setExpDate(threadExpDate);
                        Thread.yield();
                        String retrieved = StringUtils.getExpDate();
                        if (!threadExpDate.equals(retrieved)) {
                            failures.add("Expected: " + threadExpDate + " but got: " + retrieved);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    StringUtils.setExpDate(null);
                }
            }));
        }

        latch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertTrue("Thread isolation failures for expDate: " + failures, failures.isEmpty());
    }

    @Test
    public void test_trackData_threadIsolation() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final String threadTrackData = ";4000001234560" + String.format("%03d", i) + "=2512101000000000000?";
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        StringUtils.setTrackData(threadTrackData);
                        Thread.yield();
                        String retrieved = StringUtils.getTrackData();
                        if (!threadTrackData.equals(retrieved)) {
                            failures.add("Expected: " + threadTrackData + " but got: " + retrieved);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    StringUtils.setTrackData(null);
                }
            }));
        }

        latch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertTrue("Thread isolation failures for trackData: " + failures, failures.isEmpty());
    }

    @Test
    public void test_maskRequest_threadIsolation() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final String threadMsg = "MASKED_REQUEST_THREAD_" + i;
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        StringBuilder sb = new StringBuilder(threadMsg);
                        StringUtils.setMaskRequest(sb);
                        Thread.yield();
                        StringBuilder retrieved = StringUtils.getMaskRequest();
                        if (retrieved == null || !threadMsg.equals(retrieved.toString())) {
                            failures.add("Expected: " + threadMsg + " but got: " + retrieved);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    StringUtils.setMaskRequest(null);
                }
            }));
        }

        latch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertTrue("Thread isolation failures for maskRequest: " + failures, failures.isEmpty());
    }

    @Test
    public void test_allFields_concurrentMasking() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            final String accNo = "4000001234560" + String.format("%03d", i);
            final String expDate = String.format("%04d", i);
            final String trackData = ";4000001234560" + String.format("%03d", i) + "=2512101?";

            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        // Simulate the masking workflow
                        StringUtils.setAccNo(accNo);
                        StringUtils.setExpDate(expDate);
                        StringUtils.setTrackData(trackData);

                        StringBuilder maskedRequest = new StringBuilder("RAW_" + threadId + "_" + accNo + "_" + expDate + "_" + trackData);
                        StringUtils.setMaskRequest(maskedRequest);

                        Thread.yield(); // encourage interleaving

                        // Verify each field still belongs to this thread
                        if (!accNo.equals(StringUtils.getAccNo())) {
                            failures.add("Thread " + threadId + ": accNo mismatch");
                        }
                        if (!expDate.equals(StringUtils.getExpDate())) {
                            failures.add("Thread " + threadId + ": expDate mismatch");
                        }
                        if (!trackData.equals(StringUtils.getTrackData())) {
                            failures.add("Thread " + threadId + ": trackData mismatch");
                        }
                        StringBuilder retrieved = StringUtils.getMaskRequest();
                        if (retrieved == null || !retrieved.toString().contains("RAW_" + threadId + "_")) {
                            failures.add("Thread " + threadId + ": maskRequest mismatch");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    StringUtils.setAccNo(null);
                    StringUtils.setExpDate(null);
                    StringUtils.setTrackData(null);
                    StringUtils.setMaskRequest(null);
                }
            }));
        }

        latch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertTrue("Thread isolation failures in combined masking: " + failures, failures.isEmpty());
    }

    @Test
    public void test_cleanup_doesNotAffectOtherThreads() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch thread1Set = new CountDownLatch(1);
        CountDownLatch thread2Verified = new CountDownLatch(1);
        List<String> failures = Collections.synchronizedList(new ArrayList<>());

        // Thread 1: sets values then clears them
        executor.submit(() -> {
            StringUtils.setAccNo("4111111111111111");
            StringUtils.setExpDate("1225");
            StringUtils.setTrackData(";4111111111111111=1225?");
            thread1Set.countDown();
            try {
                thread2Verified.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Now clear
            StringUtils.setAccNo(null);
            StringUtils.setExpDate(null);
            StringUtils.setTrackData(null);
        });

        // Thread 2: sets its own values, verifies thread 1's cleanup doesn't affect it
        executor.submit(() -> {
            try {
                thread1Set.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            StringUtils.setAccNo("5500000000000004");
            StringUtils.setExpDate("0326");
            StringUtils.setTrackData(";5500000000000004=0326?");

            thread2Verified.countDown();

            // Small delay to let thread 1 clear its values
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            // Verify thread 2's values are still intact after thread 1 cleanup
            if (!"5500000000000004".equals(StringUtils.getAccNo())) {
                failures.add("accNo was affected by other thread's cleanup");
            }
            if (!"0326".equals(StringUtils.getExpDate())) {
                failures.add("expDate was affected by other thread's cleanup");
            }
            if (!";5500000000000004=0326?".equals(StringUtils.getTrackData())) {
                failures.add("trackData was affected by other thread's cleanup");
            }

            StringUtils.setAccNo(null);
            StringUtils.setExpDate(null);
            StringUtils.setTrackData(null);
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertTrue("Cleanup affected other threads: " + failures, failures.isEmpty());
    }

    @Test
    public void test_maskAccountNumber_shortPan_doesNotThrow() {
        // PANs shorter than 10 chars should not throw exceptions
        String result = StringUtils.maskAccountNumber("12345");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertTrue("Short PAN should be fully masked", result.matches("\\*+"));

        // Null should return empty
        String nullResult = StringUtils.maskAccountNumber(null);
        assertNotNull(nullResult);
        assertEquals(0, nullResult.length());
    }

    @Test
    public void test_maskAccountNumber_normalPan() {
        String result = StringUtils.maskAccountNumber("4111111111111111");
        // First 6 + masked middle + last 4
        assertEquals(16, result.length());
        assertTrue(result.startsWith("411111"));
        assertTrue(result.endsWith("1111"));
        assertTrue(result.substring(6, 12).matches("\\*+"));
    }
}

