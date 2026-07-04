package com.example.digitalbankapi.repository;

import com.example.digitalbankapi.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
