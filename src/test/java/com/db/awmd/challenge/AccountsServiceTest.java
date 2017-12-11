package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transaction;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
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

    //test the transfer works happy case
    @Test
    public void transferAmount() throws Exception {
      Account account1 = new Account("Id-124");
      Account account2 = new Account("Id-125");
      account1.setBalance(new BigDecimal(1000));
      account2.setBalance(new BigDecimal(2000));
      this.accountsService.createAccount(account1);
      this.accountsService.createAccount(account2);

      this.accountsService.transfer(new Transaction(account1.getAccountId(), account2.getAccountId(), new BigDecimal(100)));


      assertThat(this.accountsService.getAccount("Id-124").getBalance().intValue()).isEqualTo(900);
      assertThat(this.accountsService.getAccount("Id-125").getBalance().intValue()).isEqualTo(2100);
    }

    //test the transfer throws exception on account not found
    @Test
    public void transferAmount_failsOnAccountNotFound() throws Exception {
    Account fromAccount = new Account("Id-128");
    Account toAccount = new Account("Id-129");


      try {
        this.accountsService.transfer(new Transaction(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal(100)));
        fail("Should have failed when transferring amount from accounts which dont exist");
      } catch (InvalidAccountException ex) {
        assertThat(ex.getMessage()).isEqualTo("Account"+ fromAccount.getAccountId() + "not present in repository");
      }
    }

  //test the transfer throws exception on insufficient balance
  @Test
  public void transferAmount_failsOnInsufficentBalance() throws Exception {
    Account fromAccount1 = new Account("Id-131");
    Account toAccount1 = new Account("Id-132");

    fromAccount1.setBalance(new BigDecimal(50));
    toAccount1.setBalance(new BigDecimal(200));
    this.accountsService.createAccount(fromAccount1);
    this.accountsService.createAccount(toAccount1);

    BigDecimal amt = new BigDecimal(100);

    try {
      this.accountsService.transfer(new Transaction(fromAccount1.getAccountId(), toAccount1.getAccountId(), amt));
      fail("Should have failed when transferring amount from account with insufficient balance");
    } catch (InsufficientBalanceException ex) {
      assertThat(ex.getMessage()).isEqualTo("Balance not sufficient to transfer from account" + fromAccount1.getAccountId());
    }
  }

    //Test the account transfer works in multithreaded scenario
    @Test
    public void transferAmountWithMultipleThreads() throws Exception {
      Account account1 = new Account("Id-32");
      Account account2 = new Account("Id-34");
      account1.setBalance(new BigDecimal(1000));
      account2.setBalance(new BigDecimal(2000));
      this.accountsService.createAccount(account1);
      this.accountsService.createAccount(account2);

      BigDecimal amt = new BigDecimal(100);

      Thread t1 = new Thread(new Runnable() {
        @Override
        public void run() {
          accountsService.transfer(new Transaction(account1.getAccountId(), account2.getAccountId(), amt));
        }
      });

      Thread t2 = new Thread(new Runnable() {
        @Override
        public void run() {
          accountsService.transfer(new Transaction(account1.getAccountId(), account2.getAccountId(), amt));
        }
      });

      t1.start();
      t2.start();
      t2.join();

      assertThat(this.accountsService.getAccount("Id-32").getBalance().intValue()).isEqualTo(800);
      assertThat(this.accountsService.getAccount("Id-34").getBalance().intValue()).isEqualTo(2200);
    }

}
