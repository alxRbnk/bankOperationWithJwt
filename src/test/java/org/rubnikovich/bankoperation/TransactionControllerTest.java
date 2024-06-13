package org.rubnikovich.bankoperation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rubnikovich.bankoperation.dto.TransactionDto;
import org.rubnikovich.bankoperation.entity.Transaction;
import org.rubnikovich.bankoperation.entity.User;
import org.rubnikovich.bankoperation.repository.TransactionRepository;
import org.rubnikovich.bankoperation.repository.UserRepository;
import org.rubnikovich.bankoperation.security.JwtUtil;
import org.rubnikovich.bankoperation.service.TransactionService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TransactionService transactionService;

    private User sender;
    private User recipient;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setLogin("sender");
        sender.setBalance(BigDecimal.valueOf(1000));
        recipient = new User();
        recipient.setId(2L);
        recipient.setLogin("recipient");
        recipient.setBalance(BigDecimal.valueOf(500));
        transactionDto = new TransactionDto();
        transactionDto.setAmount(BigDecimal.valueOf(100));
        transactionDto.setSender(1L);
        transactionDto.setRecipientId(2L);
    }

    @Test
    void testMakeTransaction_Success() {
        String token = "test_token";
        when(jwtUtil.getLogin(token)).thenReturn("sender");
        when(userRepository.findByLogin("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        ResponseEntity<String> response = transactionService.makeTransaction(transactionDto, token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Transaction made successfully ", response.getBody());
        assertEquals(BigDecimal.valueOf(900), sender.getBalance());
        assertEquals(BigDecimal.valueOf(600), recipient.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void testMakeTransaction_UserNotFound() {
        String token = "test_token";
        when(jwtUtil.getLogin(token)).thenReturn("sender");
        when(userRepository.findByLogin("sender")).thenReturn(Optional.empty());
        ResponseEntity<String> response = transactionService.makeTransaction(transactionDto, token);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("User not found"));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testMakeTransaction_RecipientNotFound() {
        String token = "test_token";
        when(jwtUtil.getLogin(token)).thenReturn("sender");
        when(userRepository.findByLogin("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            if (id.equals(1L)) {
                return Optional.of(sender);
            } else {
                return Optional.empty();
            }
        });
        ResponseEntity<String> response = transactionService.makeTransaction(transactionDto, token);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("User not found"));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testMakeTransaction_NegativeAmount() {
        String token = "test_token";
        when(jwtUtil.getLogin(token)).thenReturn("sender");
        User sender = new User();
        sender.setId(1L);
        sender.setLogin("sender");
        sender.setBalance(BigDecimal.valueOf(100));
        User recipient = new User();
        recipient.setId(2L);
        recipient.setLogin("recipient");
        recipient.setBalance(BigDecimal.valueOf(50));
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setSender(sender.getId());
        transactionDto.setRecipientId(recipient.getId());
        transactionDto.setAmount(BigDecimal.valueOf(-10));
        when(userRepository.findByLogin("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        ResponseEntity<String> response = transactionService.makeTransaction(transactionDto, token);
        assertEquals(400, response.getStatusCodeValue());
        String responseBody = response.getBody();
        assertTrue(responseBody.contains("Failed to make transaction, insufficient funds for the transaction"));
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(userRepository, times(0)).save(any(User.class));
    }
}