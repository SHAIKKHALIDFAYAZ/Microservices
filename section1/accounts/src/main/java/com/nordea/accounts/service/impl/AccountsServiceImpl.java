package com.nordea.accounts.service.impl;

import com.nordea.accounts.constants.AccountsConstants;
import com.nordea.accounts.dto.AccountsDto;
import com.nordea.accounts.dto.CustomerDto;
import com.nordea.accounts.entity.Accounts;
import com.nordea.accounts.entity.Customer;
import com.nordea.accounts.exception.CustomerAlreadyExistsException;
import com.nordea.accounts.exception.ResourceNotFoundException;
import com.nordea.accounts.mapper.AccountsMapper;
import com.nordea.accounts.mapper.CustomerMapper;
import com.nordea.accounts.repository.AccountsRepository;
import com.nordea.accounts.repository.CustomerRepository;
import com.nordea.accounts.service.IAccountsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl  implements IAccountsService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;

    /**
     * @param customerDto - CustomerDto Object
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "
                    +customerDto.getMobileNumber());
        }
        Customer savedCustomer = customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));
    }

    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Accounts Details based on a given mobileNumber
     */
    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto.getAccountNumber() != null) {
        Accounts accounts = accountsRepository.findById(customerDto.getAccountsDto().getAccountNumber()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
        );
        AccountsMapper.mapToAccounts(accountsDto, accounts);
        accounts = accountsRepository.save(accounts);

        Long customerId = accounts.getCustomerId();
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "customerId", customerId.toString())
        );
        CustomerMapper.mapToCustomer(customerDto, customer);
        customer = customerRepository.save(customer);
        isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());

        return true;
    }
}
