package com.example.batch.core.domain.accounts;

import com.example.batch.core.domain.orders.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Accounts, Integer> {
}
