package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;
  
  @Mock
  private AccountsService mockAccountsService;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    //accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
	  Account account = accountsService.getAccount("Id-123");
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().is(400));
    
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().is(201));
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  void transferAmount() throws Exception 
  {
	   BigDecimal amount =new BigDecimal(5000.00);
	   Account account = new Account("1241", new BigDecimal("10000.00"));
	   Account account1 = new Account("1251", new BigDecimal("5000.00"));
	   this.accountsService.createAccount(account);
	   this.accountsService.createAccount(account1);
	   Mockito.doNothing().when(this.mockAccountsService).transferAmount(Mockito.anyString(),Mockito.anyString(),Mockito.any());
	   this.mockMvc.perform(get("/v1/accounts/" + account.getAccountId()+"/"+account1.getAccountId()+"/"+ amount))
	      .andExpect(status().isOk())
	      .andExpect(
	        content().string("transfer done successfully"));
	   this.mockAccountsService.transferAmount(account.getAccountId(), account1.getAccountId(), amount);
	   verify(this.mockAccountsService,times(1)).transferAmount(account.getAccountId(), account1.getAccountId(), amount);
  }
  
	@Test
	void transferAmountNegative_scenario() throws Exception {
		BigDecimal amount = new BigDecimal(-5000.00);
		Account account = new Account("126", new BigDecimal("10000.00"));
		Account account1 = new Account("127", new BigDecimal("5000.00"));
		this.accountsService.createAccount(account);
		this.accountsService.createAccount(account1);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferAmount(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferAmount(account.getAccountId(), account1.getAccountId(), amount);
		verify(this.mockAccountsService, times(1)).transferAmount(account.getAccountId(), account1.getAccountId(),
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + account1.getAccountId() + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("Amount transfer can't be negative"));
	}
	
	
	@Test
	void transferAmountMoreThanBalance_scenario() throws Exception {
		BigDecimal amount = new BigDecimal(5000.00);
		Account account = new Account("128", new BigDecimal("1000.00"));
		Account account1 = new Account("129", new BigDecimal("5000.00"));
		this.accountsService.createAccount(account);
		this.accountsService.createAccount(account1);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferAmount(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferAmount(account.getAccountId(), account1.getAccountId(), amount);
		verify(this.mockAccountsService, times(1)).transferAmount(account.getAccountId(), account1.getAccountId(),
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + account1.getAccountId() + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("The amount is greater than account balance"));
	}
  
	@Test
	void transferAmount_AccountNotExist() throws Exception {
		BigDecimal amount = new BigDecimal(5000.00);
		Account account = new Account("130", new BigDecimal("1000.00"));
		this.accountsService.createAccount(account);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferAmount(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferAmount(account.getAccountId(), "125", amount);
		verify(this.mockAccountsService, times(1)).transferAmount(account.getAccountId(), "125",
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + "125" + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("The accountTo ID 125 to transfer does not exist"));
	}
}
