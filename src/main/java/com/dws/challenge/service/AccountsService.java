package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AmountTransferException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;
	
	private final EmailNotificationService emailNotificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,EmailNotificationService emailNotificationService) {
		this.accountsRepository = accountsRepository;
		this.emailNotificationService = emailNotificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/** This method is used to transfer the amount from one account to another account
	 * with no deadlock multithreading process account transfer with multiple account 
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 */
	public void transferAmount(String accountFromId, String accountToId, BigDecimal amount) throws Exception {
		checkValidations(accountFromId,accountToId,amount);
		Account accountFrom = getAccount(accountFromId);
		Account accountTo =   getAccount(accountToId);
		final long fromId = Long.parseLong(accountFromId);
		final long toId=  Long.parseLong(accountToId);
		Object lock1 = fromId < toId ? accountFrom:accountTo ;
		Object lock2 = fromId > toId ? accountFrom:accountTo ;
		
		synchronized (lock1) {
			synchronized (lock2) {
				withdraw(accountFromId,accountToId, amount);
				deposit(accountFromId,accountToId, amount);
			}
			
		}
		
	}

	/** This method is used to check all validations including( negative balance, no account exist & transfer amount
	 * more than account balance) throws the specific message
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 * @throws Exception
	 */
	private void checkValidations(String accountFromId, String accountToId, BigDecimal amount)throws Exception  {
	  Account accountFrom = getAccount(accountFromId);
	  Account accountTo = getAccount(accountToId);
	  if(amount.compareTo(BigDecimal.ZERO)<0) {
		  throw new AmountTransferException("Amount transfer can't be negative");
	  }
	  if(accountFrom == null) {
		  throw new AmountTransferException("The accountFrom ID "+accountFromId +" to transfer does not exist");
	  }
	  if(accountTo == null) {
		  throw new AmountTransferException("The accountTo ID "+accountToId +" to transfer does not exist");
	  }
      if (amount.compareTo(accountFrom.getBalance()) == 1) {
		  throw new AmountTransferException("The amount is greater than account balance");
	  }
  }

	/**
	 * This method is used to withdraw the amount while transfer from account and get the notification after transfer
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 */
	private void withdraw(String accountFromId,String accountToId, BigDecimal amount ) {
		Account account = getAccount(accountFromId);
		BigDecimal balance =  account.getBalance();
		account.setBalance(balance.subtract(amount));
		accountsRepository.getAccounts().put(accountFromId, account);
		emailNotificationService.notifyAboutTransfer(account, " "+ amount+ " transfer to "+ accountToId);
	}
	
	/**
	 * This method is used to deposit  the amount after  transfer from another
	 * account and get the notification after transfer
	 * @param accountFromId
	 * @param accountToId
	 * @param amount
	 */
	
	private void deposit(String accountFromId,String accountToId, BigDecimal amount ) {
		Account account = getAccount(accountToId);
		BigDecimal balance =  account.getBalance();
		account.setBalance(balance.add(amount));
		accountsRepository.getAccounts().put(accountToId, account);
		emailNotificationService.notifyAboutTransfer(account, " " + amount+ " transfer from "+ accountFromId);
	}
 
}
