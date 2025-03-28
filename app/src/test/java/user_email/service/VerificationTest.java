package user_email.service;

import org.junit.jupiter.api.Test;

import user_email.api.VerificationIf;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationTest {

    @Test
    public void testVerification() throws Exception {
        VerificationIf verificationService = new VerificationImpl(2);
        String record = verificationService.createRecord("dummy");
        verificationService.createRecord("dummy1");
        verificationService.createRecord("dummy2");
        Thread.sleep(1000);
        String record1 = verificationService.createRecord("dummy");
        assertEquals(record, record1);
        String res = verificationService.verifyRecord(record);
        assertEquals("dummy", res);

        record = verificationService.createRecord("dummy");
        assertNotEquals(record, record1);
        Thread.sleep(3000);
        res = verificationService.verifyRecord(record);
        assertNull(res);
    }
}
