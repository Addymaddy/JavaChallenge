package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService= notificationService;
  }


  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transfer(Transaction txn) throws InsufficientBalanceException, InvalidAccountException {
    String fromAccountId = txn.getFromAccountId();
    String toAccountId = txn.getToAccountId();
    BigDecimal amount = txn.getAmount();

    Account fromAccount = this.accountsRepository.getAccount(fromAccountId);
    Account toAccount = this.accountsRepository.getAccount(toAccountId);
    if( fromAccount != null &&  toAccount != null)
    {
      fromAccount.transferAmount(toAccount, amount);
    }
    else
    {
      throw new InvalidAccountException("Account"+ fromAccountId + "not present in repository");
    }

    //send notification
    notificationService.notifyAboutTransfer(fromAccount, amount.doubleValue() + "is transferrred to account" + toAccount.getAccountId());
    notificationService.notifyAboutTransfer(toAccount, amount.doubleValue() + "is transferred from account" + fromAccount.getAccountId());
  }
}
