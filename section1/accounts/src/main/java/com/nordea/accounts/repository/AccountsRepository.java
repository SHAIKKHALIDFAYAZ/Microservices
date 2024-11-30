package com.nordea.accounts.repository;


import com.nordea.accounts.entity.Accounts;
import com.nordea.accounts.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, Long> {

    Optional<Customer> findByMobileNumber(String mobileNumber);


}