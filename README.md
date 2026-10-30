# spring-thymeleaf-fastshop
This is an e-commerce sales page. We desgined it with 4 technology include
* AngularJs
* Thymeleaf framework
* Spring boot framework
* Java core

Website just concenstrate 3 main roles in role hirarchies is: Admin, Staff and User

1. Admin is highest role. It can see and look up all of accounts and watch report, statistical follow a month
2. Staff is role which can see orders everyday. They can write some reports and send it or manager list products or categories of Fastshop
3. User is role which depend customers. Customers can buy and pay products follow their bill

## Chạy bằng Docker

Yêu cầu: Docker Desktop (hoặc Docker Engine + Compose v2).

```bash
docker compose up -d --build      # build app + khởi động MySQL 8
docker compose logs -f app        # đợi dòng "Started NetApplication"
```

Mở http://localhost:8000. Lần chạy đầu, khi database còn trống, app tự nạp dữ liệu demo từ
`src/main/resources/db/seed.sql` (bật bởi biến `APP_SEED_ENABLED=true` trong `docker-compose.yml`).

| Vai trò    | Đăng nhập tại                           | Tài khoản | Mật khẩu |
|------------|-----------------------------------------|-----------|----------|
| Admin      | http://localhost:8000/login.fastshop.com | `admin`   | `123456` |
| Nhân viên  | http://localhost:8000/login.fastshop.com | `staff`   | `123456` |
| Khách hàng | http://localhost:8000/login              | `user`    | `123456` |

Các tài khoản demo khác (nhân viên, ~85 khách hàng) cũng dùng mật khẩu `123456`.

- Nạp lại dữ liệu demo từ đầu: `docker compose down -v && docker compose up -d`
- Sinh lại file seed (sau khi sửa script): `python3 database/seed/generate_seed.py`
- MySQL được mở ở cổng `3307` trên máy host (user `root`, mật khẩu `12345678`, database `fast_shop`).
- Ảnh sản phẩm / file báo cáo upload được lưu ở volume `uploads` (`/app/uploads` trong container).
- Email (quên mật khẩu, thông báo hủy đơn) khi chạy bằng Docker được gửi vào **Mailpit**, xem tại http://localhost:8025 — không gửi ra Internet.
- Cấu hình bí mật đặt trong file `.env` (không commit, xem mẫu `.env.example`): `APP_SECRET` (khóa ký cookie đăng nhập), Gmail, Google/Facebook OAuth.
- Mật khẩu được lưu dạng băm BCrypt; mật khẩu dạng chữ thường trong DB cũ sẽ tự được băm khi app khởi động.
