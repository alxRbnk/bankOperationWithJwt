//package org.rubnikovich.bankoperation; //fixme
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.rubnikovich.bankoperation.controller.TransactionController;
//import org.rubnikovich.bankoperation.dto.TransactionDto;
//import org.rubnikovich.bankoperation.security.JwtUtil;
//import org.rubnikovich.bankoperation.service.TransactionService;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//public class TransactionControllerTest {
//
//    @Mock
//    private TransactionService transactionService;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @InjectMocks
//    private TransactionController transactionController;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
//    }
//
//    @Test
//    public void testGetAllTransactions() throws Exception {
//        given(transactionService.getAllTransactions()).willReturn(Collections.emptyList());
//
//        mockMvc.perform(get("/transactions"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testGetAllUserTransactions() throws Exception {
//        String token = "Bearer sampleToken";
//        String login = "userLogin";
//        given(jwtUtil.validateTokenAndGetClaim(anyString())).willReturn(login);
//        given(transactionService.getAllUserTransactions(anyString())).willReturn(Collections.emptyList());
//
//        mockMvc.perform(get("/transactions/user")
//                        .header("Authorization", token))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testMakeTransaction() throws Exception {
//        String token = "Bearer sampleToken";
//        TransactionDto transactionDto = new TransactionDto();
//        transactionDto.setSender(1L);
//        transactionDto.setRecipient(2L);
//        transactionDto.setAmount(new BigDecimal("100.00"));
//
//        given(jwtUtil.validateTokenAndGetClaim(anyString())).willReturn("userLogin");
//        given(transactionService.makeTransaction(any(TransactionDto.class))).willReturn(true);
//
//        mockMvc.perform(post("/transactions")
//                        .header("Authorization", token)
//                        .contentType("application/json")
//                        .content("{\"sender\":1,\"recipient\":2,\"amount\":100.00}"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testMakeTransactionInsufficientFunds() throws Exception {
//        String token = "Bearer sampleToken";
//        TransactionDto transactionDto = new TransactionDto();
//        transactionDto.setSender(1L);
//        transactionDto.setRecipient(2L);
//        transactionDto.setAmount(new BigDecimal("1000.00"));
//
//        given(jwtUtil.validateTokenAndGetClaim(anyString())).willReturn("userLogin");
//        given(transactionService.makeTransaction(any(TransactionDto.class))).willReturn(false);
//
//        mockMvc.perform(post("/transactions")
//                        .header("Authorization", token)
//                        .contentType("application/json")
//                        .content("{\"sender\":1,\"recipient\":2,\"amount\":1000.00}"))
//                .andExpect(status().isBadRequest());
//    }
//}
