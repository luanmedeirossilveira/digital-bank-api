package com.example.digitalbankapi.repository;

import com.example.digitalbankapi.entity.AccountMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountMovementRepository extends JpaRepository<AccountMovement, Long> {

    List<AccountMovement> findByAccountIdOrderByCreatedAtDescIdDesc(Long accountId);
}
