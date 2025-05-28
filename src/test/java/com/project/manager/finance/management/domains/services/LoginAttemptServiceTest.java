package com.project.manager.finance.management.domains.services;

import com.project.manager.finance.management.common.TestUtils;
import com.project.manager.finance.management.domains.models.authentication.LoginAttempt;
import com.project.manager.finance.management.domains.repositories.redis.LoginAttemptRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class LoginAttemptServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private AutoCloseable closeable;

    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        this.closeable.close();
    }

    @Test
    @DisplayName("Should increment attempts and save LoginAttempt")
    void incrementAttempts_shouldIncrementAndSave() {
        LoginAttempt initialAttempt = new LoginAttempt(TEST_EMAIL);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(initialAttempt));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        loginAttemptService.incrementAttempts(TEST_EMAIL);

        assertEquals(1, initialAttempt.getAttempts());
        verify(loginAttemptRepository).save(initialAttempt);
    }

    @Test
    @DisplayName("Should create new LoginAttempt if not exists, increment and save")
    void incrementAttempts_shouldCreateIncrementAndSave() {
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.empty());
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        loginAttemptService.incrementAttempts(TEST_EMAIL);

        verify(loginAttemptRepository).save(any(LoginAttempt.class));
    }

    @Test
    @DisplayName("Should block user when max attempts reached")
    void incrementAttempts_shouldBlockUser() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        attempt.setAttempts(TestUtils.maxAttempts - 1);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        loginAttemptService.incrementAttempts(TEST_EMAIL);

        long expectedBlockUntil = TestUtils.calculateExpectedBlockUntil(attempt.getAttempts());
        assertNotNull(attempt.getBlockUntil());
        TestUtils.assertTimeEqualsWithTolerance(expectedBlockUntil, attempt.getBlockUntil(), "Block until time should be calculated correctly");
        verify(loginAttemptRepository).save(attempt);
    }

    @Test
    @DisplayName("Should reset attempts by deleting LoginAttempt")
    void resetAttempts_shouldDelete() {
        loginAttemptService.resetAttempts(TEST_EMAIL);
        verify(loginAttemptRepository).deleteById(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should return true if user is blocked")
    void isBlocked_shouldReturnTrueIfBlocked() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        attempt.setBlockUntil(System.currentTimeMillis() + 60000);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));

        assertTrue(loginAttemptService.isBlocked(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should return false if user is not blocked")
    void isBlocked_shouldReturnFalseIfNotBlocked() {
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.empty());
        assertFalse(loginAttemptService.isBlocked(TEST_EMAIL));

        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(new LoginAttempt(TEST_EMAIL)));
        assertFalse(loginAttemptService.isBlocked(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should block user until specified time")
    void blockUntil_shouldBlockUntil() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        loginAttemptService.blockUntil(TEST_EMAIL);

        long expectedBlockUntil = TestUtils.calculateExpectedBlockUntil(attempt.getAttempts());

        assertNotNull(attempt.getBlockUntil());
        TestUtils.assertTimeEqualsWithTolerance(expectedBlockUntil, attempt.getBlockUntil(), "Block until time should be calculated and set");
        verify(loginAttemptRepository).save(attempt);
    }

    @Test
    @DisplayName("Should get block until time")
    void getBlockUntil_shouldGetBlockUntil() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        long blockUntil = System.currentTimeMillis() + 60000;
        attempt.setBlockUntil(blockUntil);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));

        Long result = loginAttemptService.getBlockUntil(TEST_EMAIL);

        assertEquals(blockUntil, result);
    }

    @Test
    @DisplayName("Should return 0 if LoginAttempt not found for getBlockUntil")
    void getBlockUntil_shouldReturnZeroIfNotFound() {
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.empty());
        assertEquals(0L, loginAttemptService.getBlockUntil(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should get number of attempts")
    void getAttempts_shouldGetAttempts() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        attempt.setAttempts(2);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));

        int attempts = loginAttemptService.getAttempts(TEST_EMAIL);

        assertEquals(2, attempts);
    }

    @Test
    @DisplayName("Should return 0 attempts if LoginAttempt not found")
    void getAttempts_shouldReturnZeroIfNotFound() {
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.empty());
        assertEquals(0, loginAttemptService.getAttempts(TEST_EMAIL));
    }

    @Test
    @DisplayName("Should calculate block time correctly")
    void calculateBlockTime_shouldCalculateCorrectly() {
        LoginAttempt attempt = new LoginAttempt(TEST_EMAIL);
        attempt.setAttempts(TestUtils.maxAttempts);
        when(loginAttemptRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(attempt));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        loginAttemptService.incrementAttempts(TEST_EMAIL);

        long expectedBlockUntil = TestUtils.calculateExpectedBlockUntil(attempt.getAttempts());

        assertNotNull(attempt.getBlockUntil());
        TestUtils.assertTimeEqualsWithTolerance(expectedBlockUntil, attempt.getBlockUntil(), "Block until time should match calculated time");
    }
}