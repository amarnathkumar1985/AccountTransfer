package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AmountTransferException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  
  @Mock
  private AccountsService mockAccountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }
  
  @Test
  void transferAmount() throws Exception {
	   BigDecimal amount =new BigDecimal(5000);
	   Account account = new Account("123", new BigDecimal("10000"));
	   Account account1 = new Account("124", new BigDecimal("5000"));
	   this.accountsService.createAccount(account);
	   this.accountsService.createAccount(account1);
	   this.accountsService.transferAmount("123", "124", amount);
	   assertEquals(account.getBalance(),new BigDecimal(5000.00));
	   assertEquals(account1.getBalance(),new BigDecimal(10000.00));
	  
  }
 
  
  @Test
  void transferAmount_failsOnNegativeAmountTransfer() throws Exception {
	   BigDecimal amount =new BigDecimal(-5000);
	   Account account = new Account("127", new BigDecimal("10000"));
	   Account account1 = new Account("128", new BigDecimal("5000"));
	   this.accountsService.createAccount(account);
	   this.accountsService.createAccount(account1);
	   try {
		   this.accountsService.transferAmount("127", "128", amount);
		      fail("Should have failed when transfer amount is negative");
		    } catch (AmountTransferException ex) {
		      assertThat(ex.getMessage()).isEqualTo("Amount transfer can't be negative");
		    }
	  
  }
  
  @Test
  void transferAmount_failsOnTransferAmountMore() throws Exception {
	   BigDecimal amount =new BigDecimal(20000);
	   Account account = new Account("129", new BigDecimal("10000"));
	   Account account1 = new Account("130", new BigDecimal("5000"));
	   this.accountsService.createAccount(account);
	   this.accountsService.createAccount(account1);
	   try {
		   this.accountsService.transferAmount("129", "130", amount);
		      fail("Should have failed when transfer amount is more than balance");
		    } catch (AmountTransferException ex) {
		      assertThat(ex.getMessage()).isEqualTo("The amount is greater than account balance");
		    }
	  
  }
  
  @Test
  void transferAmount_failsOnAccountNotExist() throws Exception {
	   BigDecimal amount =new BigDecimal(20000);
	   String accountToId = "132";
	   Account account = new Account("131", new BigDecimal("10000"));
	   this.accountsService.createAccount(account);
	   try {
		   this.accountsService.transferAmount("131", accountToId, amount);
		      fail("Should have failed account does not exist");
		    } catch (AmountTransferException ex) {
		      assertThat(ex.getMessage()).isEqualTo("The accountTo ID "+accountToId +" to transfer does not exist");
		    }
	  
  }
}
