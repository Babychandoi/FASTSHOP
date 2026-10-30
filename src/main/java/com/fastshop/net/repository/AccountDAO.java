package com.fastshop.net.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastshop.net.model.Account;

public interface AccountDAO extends JpaRepository<Account, String>{
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsernameOrEmail(String username, String email);
    Optional<Account> findByUsername(String username);
    Boolean existsByUsernameOrEmail(String username, String email);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);


    @Query("SELECT DISTINCT ar.account FROM Authority ar WHERE ar.role.id IN('ADMIN','USER')")
    List<Account> getAdministrators();
}
