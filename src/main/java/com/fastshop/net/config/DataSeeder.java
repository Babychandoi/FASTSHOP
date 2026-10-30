package com.fastshop.net.config;

import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.fastshop.net.model.Account;
import com.fastshop.net.repository.AccountDAO;
import com.fastshop.net.security.Passwords;

/**
 * Nạp dữ liệu demo (classpath:db/seed.sql) khi bật app.seed.enabled và database chưa có tài khoản nào,
 * sau đó băm (BCrypt) các mật khẩu còn lưu dạng chữ thường.
 */
@Component
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DataSource dataSource;
    private final AccountDAO accountDAO;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    public DataSeeder(DataSource dataSource, AccountDAO accountDAO) {
        this.dataSource = dataSource;
        this.accountDAO = accountDAO;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (enabled && accountDAO.count() == 0) {
            seed();
        }
        hashPlainPasswords();
    }

    private void seed() {
        log.info("Database trống - đang nạp dữ liệu demo từ db/seed.sql ...");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("db/seed.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
        log.info("Đã nạp dữ liệu demo: {} tài khoản.", accountDAO.count());
    }

    private void hashPlainPasswords() {
        List<Account> plain = accountDAO.findAll().stream()
                .filter(a -> !Passwords.isHashed(a.getPassword()))
                .collect(Collectors.toList());
        if (plain.isEmpty()) {
            return;
        }
        plain.forEach(a -> a.setPassword(Passwords.hash(a.getPassword())));
        accountDAO.saveAll(plain);
        log.info("Đã băm mật khẩu của {} tài khoản.", plain.size());
    }
}
