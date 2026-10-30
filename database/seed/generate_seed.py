#!/usr/bin/env python3
"""
Sinh dữ liệu demo cho Fastshop (MySQL).

Đọc danh mục / sản phẩm / thuộc tính gốc từ database/script_mysql_inserts.sql,
làm sạch và sinh thêm tài khoản, đơn hàng, đánh giá, voucher... trải dài tới ngày
TODAY để các trang thống kê luôn có số liệu.

Chạy:  python3 database/seed/generate_seed.py
Kết quả: src/main/resources/db/seed.sql (được DataSeeder nạp khi DB còn trống)
"""
import datetime as dt
import os
import random
import re
import unicodedata

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC_SQL = os.path.join(ROOT, "database", "script_mysql_inserts.sql")
OUT_SQL = os.path.join(ROOT, "src", "main", "resources", "db", "seed.sql")
IMG_DIR = os.path.join(ROOT, "src", "main", "resources", "static", "dist", "img", "products")

TODAY = dt.date(2026, 10, 30)
rnd = random.Random(int(os.environ.get("SEED", "11")))
PASSWORD = "123456"


# ----------------------------------------------------------------------------- helpers
def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, (int, float)):
        return repr(round(v, 2)) if isinstance(v, float) else str(v)
    if isinstance(v, (dt.date, dt.datetime)):
        return "'" + v.isoformat(sep=" ") + "'" if isinstance(v, dt.datetime) else "'" + v.isoformat() + "'"
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


def insert(table, cols, rows):
    out = []
    for i in range(0, len(rows), 200):
        chunk = rows[i:i + 200]
        values = ",\n".join("(" + ", ".join(q(v) for v in r) + ")" for r in chunk)
        out.append(f"INSERT INTO {table} ({', '.join(cols)}) VALUES\n{values};")
    return "\n".join(out)


def strip_accents(s):
    s = s.replace("đ", "d").replace("Đ", "D")
    return "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn")


EMOJI_RE = re.compile("[\U0001F000-\U0001FAFF☀-➿️‍♂♀]+")


def clean_text(s):
    if s is None:
        return None
    s = EMOJI_RE.sub(" ", s)
    s = re.sub(r"\s+", " ", s).strip()
    return s or None


def parse_values(line):
    """Tách phần VALUES (...) của một câu INSERT một dòng thành list python."""
    body = line[line.index("VALUES (") + 8: line.rstrip().rindex(")")]
    vals, cur, in_str, i = [], "", False, 0
    while i < len(body):
        c = body[i]
        if in_str:
            if c == "'" and i + 1 < len(body) and body[i + 1] == "'":
                cur += "'"
                i += 1
            elif c == "'":
                in_str = False
            else:
                cur += c
        elif c == "'":
            in_str = True
        elif c == ",":
            vals.append(cur.strip() if cur.strip() != "NULL" else None)
            cur = ""
        else:
            cur += c
        i += 1
    vals.append(cur.strip() if cur.strip() != "NULL" else None)
    return vals


def rand_date(start, end):
    return start + dt.timedelta(days=rnd.randint(0, (end - start).days))


# ----------------------------------------------------------------------------- source data
src_lines = open(SRC_SQL, encoding="utf-8").read().splitlines()
src_products = [parse_values(l) for l in src_lines if l.startswith("INSERT INTO products")]
src_catdetails = [parse_values(l) for l in src_lines if l.startswith("INSERT INTO categorydetails")]
src_proddetails = [parse_values(l) for l in src_lines if l.startswith("INSERT INTO productdetails")]
images = set(os.listdir(IMG_DIR))

CATEGORIES = {
    # id: (tên, đơn vị, khoảng giá USD)
    "B": ("Bia", "thùng", (14, 32)),
    "BD": ("Bóng đèn", "cái", (3, 28)),
    "BL": ("Balo", "cái", (19, 89)),
    "CMR": ("Camera", "cái", (25, 189)),
    "DG": ("Dầu gội", "chai", (4, 16)),
    "DH": ("Đồng hồ", "cái", (19, 249)),
    "D": ("Đèn ngủ", "cái", (6, 39)),
    "DT": ("Điện thoại", "cái", (149, 899)),
    "GD": ("Giày dép", "đôi", (6, 69)),
    "GV": ("Gia vị", "gói", (1.5, 9)),
    "H": ("Hộp nhựa", "bộ", (3, 12)),
    "LT": ("Laptop", "cái", (499, 1399)),
    "QAF": ("Quần áo nữ", "bộ", (12, 39)),
    "QAM": ("Quần áo nam", "cái", (8, 29)),
    "RC": ("Rau củ", "gói", (0.9, 4.5)),
}

DESC_TEMPLATES = {
    "B": "{name} được ủ theo công nghệ lên men truyền thống, vị đậm đà và hậu vị êm. Ngon nhất khi uống lạnh ở 6–8°C, thích hợp cho tiệc tùng, họp mặt bạn bè và gia đình.",
    "BD": "{name} tiết kiệm điện, ánh sáng ổn định không nhấp nháy, bảo vệ mắt. Tuổi thọ cao, dễ lắp đặt cho phòng khách, phòng ngủ và không gian trang trí.",
    "BL": "{name} thiết kế nhiều ngăn tiện dụng, chất liệu chống thấm nước, quai đeo êm vai. Phù hợp đi học, đi làm và du lịch ngắn ngày.",
    "CMR": "{name} ghi hình sắc nét, hỗ trợ xem trực tuyến qua điện thoại, cảnh báo chuyển động và đàm thoại hai chiều. Lắp đặt nhanh, bảo hành chính hãng 12 tháng.",
    "DG": "{name} làm sạch nhẹ nhàng, nuôi dưỡng tóc mềm mượt và giảm gãy rụng. Hương thơm dễ chịu, phù hợp sử dụng hằng ngày cho cả gia đình.",
    "DH": "{name} thiết kế tinh tế, mặt kính chống trầy, khả năng chống nước sinh hoạt. Món phụ kiện lịch lãm cho đi làm, dạo phố hay làm quà tặng.",
    "D": "{name} ánh sáng dịu nhẹ, không gây chói mắt, tạo cảm giác thư giãn dễ ngủ. Tiết kiệm điện, an toàn cho phòng ngủ và phòng em bé.",
    "DT": "{name} màn hình sắc nét, hiệu năng mượt mà cho nhu cầu giải trí và làm việc. Pin bền bỉ, camera chụp ảnh đẹp, máy nguyên seal bảo hành 12 tháng.",
    "GD": "{name} đế êm, chống trơn trượt, ôm chân thoải mái khi di chuyển cả ngày. Kiểu dáng trẻ trung, dễ phối đồ.",
    "GV": "{name} được chế biến từ nguyên liệu tự nhiên, đóng gói tiện lợi, giúp món ăn đậm đà chuẩn vị chỉ trong vài phút.",
    "H": "{name} làm từ nhựa nguyên sinh an toàn thực phẩm, nắp kín chống tràn, dùng được trong tủ lạnh và lò vi sóng.",
    "LT": "{name} cấu hình mạnh mẽ cho học tập, văn phòng và đồ hoạ cơ bản. Thiết kế mỏng nhẹ, bàn phím êm, thời lượng pin dài.",
    "QAF": "{name} chất vải mềm mát, form chuẩn tôn dáng, đường may tỉ mỉ. Phù hợp đi chơi, dạo phố và chụp ảnh.",
    "QAM": "{name} chất liệu cotton thoáng khí, thấm hút mồ hôi tốt, form regular dễ mặc. Thích hợp đi làm, đi chơi và thể thao nhẹ.",
    "RC": "{name} được trồng theo tiêu chuẩn hữu cơ, thu hoạch trong ngày và giao tươi tận nhà. Giàu vitamin, an toàn cho cả gia đình.",
}

STATUSES = [(-1, "Đã hủy đơn"), (0, "Chờ xác nhận"), (1, "Đang tiếp nhận"), (2, "Đang đóng gói"),
            (3, "Đang giao hàng"), (4, "Đã giao hàng")]
ROLES = [("ADMIN", "Quản trị viên", None), ("STAFF", "Nhân viên", None), ("USER", "Khách hàng", None)]

# ----------------------------------------------------------------------------- products
products = []
for v in src_products:
    pid, created, image, name, price, cat, number, available, desc = v
    image = image.strip()
    if cat not in CATEGORIES or image not in images:
        continue
    name = re.sub(r"\s+", " ", name).strip()
    if name.isupper():
        name = name.capitalize()
    lo, hi = CATEGORIES[cat][2]
    old = float(price)
    if lo <= old <= hi:
        new_price = old
    else:
        new_price = rnd.uniform(lo, hi)
    new_price = round(new_price) - 0.01 if new_price > 5 else round(new_price, 1)
    stock = rnd.choice([0, 0] + list(range(8, 260))) if rnd.random() < 0.06 else rnd.randint(12, 320)
    desc = clean_text(desc)
    if not desc or len(desc) < 40:
        desc = DESC_TEMPLATES[cat].format(name=name)
    products.append({
        "id": int(pid), "name": name[:50], "image": image, "price": new_price, "cat": cat,
        "number": stock, "available": stock > 0,
        "created": rand_date(dt.date(2024, 1, 5), TODAY - dt.timedelta(days=3)),
        "desc": desc,
    })
RENAME = {
    42: "iPhone 7 128GB - Đỏ (Product RED)",
    43: "iPhone 8 64GB - Đen bóng",
    44: "Huawei Mate 10 Pro 8GB/128GB",
    45: "Samsung Galaxy A7 4GB/64GB",
    46: "LG G6 4GB/64GB - Bạc",
    47: "MacBook Pro 13 inch M1 8GB/256GB",
    48: "Laptop MSI GF63 Thin i7 RTX",
    49: "Laptop MSI Modern 14 B11MOU",
    61: "Camera hành trình GoPro Hero 4K",
    34: "Đèn ngủ gắn tường đế gỗ cao cấp",
    37: "Đèn tường chao vải phong cách cổ điển",
    146: "Áo thun nam viền đen trắng",
    141: "Set áo & chân váy thủy thủ nữ",
}
for p in products:
    if p["id"] in RENAME:
        p["name"] = RENAME[p["id"]]
        p["desc"] = DESC_TEMPLATES[p["cat"]].format(name=p["name"])
prod_by_id = {p["id"]: p for p in products}
sellable = [p for p in products if p["available"]]

# ----------------------------------------------------------------------------- accounts
HO = ["Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý", "Trương", "Đinh", "Mai", "Lâm"]
DEM_NAM = ["Văn", "Minh", "Quốc", "Đức", "Gia", "Hoàng", "Thành", "Anh", "Tuấn", "Hữu", "Công", "Trung"]
DEM_NU = ["Thị", "Ngọc", "Thu", "Thanh", "Mai", "Bảo", "Khánh", "Phương", "Hồng", "Diệu", "Minh", "Kim"]
TEN_NAM = ["Huy", "Khang", "Nam", "Phúc", "Long", "Quân", "Bảo", "Dũng", "Hải", "Hiếu", "Khoa", "Tài", "Thịnh", "Vinh", "Sơn", "Tùng", "Đạt", "Kiên"]
TEN_NU = ["Linh", "Trang", "Hương", "Ngân", "Vy", "Anh", "Thảo", "Hà", "My", "Nhi", "Yến", "Trâm", "Quỳnh", "Hân", "Uyên", "Thư", "Lan", "Chi"]
PHONE_PREFIX = ["032", "033", "034", "035", "036", "037", "038", "039", "056", "058", "070", "076", "077", "078", "079",
                "081", "082", "083", "084", "085", "086", "088", "089", "090", "091", "093", "094", "096", "097", "098", "099"]
MAIL_DOMAINS = ["gmail.com", "gmail.com", "gmail.com", "yahoo.com", "outlook.com", "icloud.com"]

used_users, used_mails, used_phones = set(), set(), set()


def gen_name():
    male = rnd.random() < 0.5
    while True:
        ho = rnd.choice(HO)
        dem = rnd.choice(DEM_NAM if male else DEM_NU)
        ten = rnd.choice(TEN_NAM if male else TEN_NU)
        if len({ho, dem, ten}) == 3:
            return f"{ho} {dem} {ten}"


def gen_phone():
    while True:
        p = rnd.choice(PHONE_PREFIX) + "".join(str(rnd.randint(0, 9)) for _ in range(7))
        if p not in used_phones:
            used_phones.add(p)
            return p


def make_account(fullname, username=None, email=None, active=True):
    parts = strip_accents(fullname).lower().split()
    if username is None:
        base = parts[-1] + "".join(w[0] for w in parts[:-1])
        username = base
        while username in used_users:
            username = base + str(rnd.randint(10, 999))
    if email is None:
        email = f"{parts[-1]}.{parts[0]}{rnd.randint(80, 2005)}@{rnd.choice(MAIL_DOMAINS)}"
        while email in used_mails:
            email = f"{parts[-1]}.{parts[0]}{rnd.randint(80, 2005)}@{rnd.choice(MAIL_DOMAINS)}"
    used_users.add(username)
    used_mails.add(email)
    return {"username": username, "fullname": fullname, "email": email, "phone": gen_phone(),
            "password": PASSWORD, "active": active}


admin = make_account("Trần Minh Quân", "admin", "admin@fastshop.vn")
staff_demo = make_account("Lê Thu Hà", "staff", "staff@fastshop.vn")
user_demo = make_account("Phạm Gia Huy", "user", "user@fastshop.vn")

staffs = [staff_demo]
for i in range(9):
    a = make_account(gen_name(), active=(i < 7))
    a["email"] = f"{a['username']}@fastshop.vn"
    staffs.append(a)
customers = [user_demo] + [make_account(gen_name()) for _ in range(85)]
accounts = [admin] + staffs + customers

authorities = [(admin["username"], "ADMIN")] + [(s["username"], "STAFF") for s in staffs] + \
              [(c["username"], "USER") for c in customers]

# ----------------------------------------------------------------------------- addresses & ATM
STREETS = ["Nguyễn Văn Linh", "Lê Duẩn", "Trần Phú", "Hùng Vương", "Điện Biên Phủ", "Nguyễn Huệ", "Lê Lợi", "Hai Bà Trưng",
           "Võ Văn Tần", "Pasteur", "Nguyễn Thị Minh Khai", "Cách Mạng Tháng Tám", "Phan Đăng Lưu", "Tôn Đức Thắng",
           "Nguyễn Trãi", "Lý Thường Kiệt", "Hoàng Diệu", "Bạch Đằng", "Ngô Quyền", "Trường Chinh"]
PLACES = [("P. Bến Nghé", "Q.1", "TP.HCM"), ("P. 7", "Q.3", "TP.HCM"), ("P. Tân Định", "Q.1", "TP.HCM"),
          ("P. 15", "Q. Bình Thạnh", "TP.HCM"), ("P. Thảo Điền", "TP. Thủ Đức", "TP.HCM"),
          ("P. Hải Châu 1", "Q. Hải Châu", "Đà Nẵng"), ("P. Hòa Minh", "Q. Liên Chiểu", "Đà Nẵng"),
          ("P. An Hải Bắc", "Q. Sơn Trà", "Đà Nẵng"), ("P. Tràng Tiền", "Q. Hoàn Kiếm", "Hà Nội"),
          ("P. Dịch Vọng", "Q. Cầu Giấy", "Hà Nội"), ("P. Láng Hạ", "Q. Đống Đa", "Hà Nội"),
          ("P. Xuân Khánh", "Q. Ninh Kiều", "Cần Thơ"), ("P. Lộc Thọ", "TP. Nha Trang", "Khánh Hòa"),
          ("P. Vĩnh Ninh", "TP. Huế", "Thừa Thiên Huế"), ("P. Trần Phú", "TP. Quy Nhơn", "Bình Định")]


def gen_address():
    ward, district, city = rnd.choice(PLACES)
    num = rnd.choice([str(rnd.randint(1, 450)), f"{rnd.randint(10, 300)}/{rnd.randint(1, 40)}", f"K{rnd.randint(100, 500)}/{rnd.randint(1, 60)}"])
    return f"{num} {rnd.choice(STREETS)}, {ward}, {district}, {city}"


addresses = []
addr_of = {}
for c in customers:
    n = 2 if c is user_demo else rnd.choice([1, 1, 1, 2])
    for k in range(n):
        place = gen_address()
        addresses.append((place, k == 0, c["username"]))
        if k == 0:
            addr_of[c["username"]] = place

BANKS = ["Vietcombank", "Techcombank", "MB Bank", "ACB", "VPBank", "Sacombank", "BIDV", "Agribank", "TPBank"]
atms = []
for c in customers:
    if c is user_demo or rnd.random() < 0.45:
        atms.append((strip_accents(c["fullname"]).upper(), rnd.choice(BANKS),
                     "".join(str(rnd.randint(0, 9)) for _ in range(rnd.choice([10, 12, 13]))), c["username"], True))

# ----------------------------------------------------------------------------- orders
month_weight = {}
d = dt.date(2023, 1, 1)
while d <= TODAY:
    base = {2023: 9, 2024: 13, 2025: 19, 2026: 26}[d.year]
    if d.month in (11, 12):
        base = int(base * 1.4)
    month_weight[(d.year, d.month)] = base + rnd.randint(-3, 3)
    d = (d.replace(day=28) + dt.timedelta(days=4)).replace(day=1)
month_weight[(TODAY.year, TODAY.month)] = 16

buyer_weights = [8 if c is user_demo else rnd.choice([1, 1, 1, 2, 3, 5]) for c in customers]

orders, orderdetails = [], []
oid, odid = 0, 0


def add_order(created, status, buyer):
    global oid, odid
    oid += 1
    # hàng giá rẻ được mua nhiều hơn hàng đắt
    items = []
    for _ in range(rnd.choice([1, 1, 2, 2, 3, 4])):
        p = rnd.choices(sellable, [1 / (pp["price"] ** 0.3 + 1) for pp in sellable])[0]
        if p not in items:
            items.append(p)
    subtotal = 0
    for p in items:
        qty = rnd.choice([1, 1, 1, 2, 2, 3]) if p["price"] > 5 else rnd.randint(2, 6)
        odid += 1
        orderdetails.append((odid, qty, p["id"], oid))
        subtotal += qty * p["price"]
    total = round(subtotal + 1.2, 2)
    confirm = None
    if status != 0:
        confirm = min(created + dt.timedelta(days=rnd.randint(0, 3)), TODAY)
    orders.append((oid, addr_of.get(buyer["username"]) or gen_address(), created, buyer["username"], status, confirm, total))


for (y, m), cnt in sorted(month_weight.items()):
    first = dt.date(y, m, 1)
    last = (first.replace(day=28) + dt.timedelta(days=4)).replace(day=1) - dt.timedelta(days=1)
    last = min(last, TODAY - dt.timedelta(days=1))
    if last < first:
        continue
    for _ in range(cnt):
        created = rand_date(first, last)
        age = (TODAY - created).days
        if age > 7:
            status = 4 if rnd.random() < 0.9 else -1
        else:
            status = rnd.choice([1, 2, 2, 3, 3, 4, 4, -1])
        add_order(created, status, rnd.choices(customers, buyer_weights)[0])

# đơn hôm nay đang chờ nhân viên xác nhận
for _ in range(7):
    add_order(TODAY, 0, rnd.choices(customers, buyer_weights)[0])
# đảm bảo tài khoản demo có đơn ở mọi trạng thái
for st in (0, 1, 2, 3):
    add_order(TODAY - dt.timedelta(days=max(0, st - 1)), st, user_demo)

orders.sort(key=lambda o: o[0])

# ----------------------------------------------------------------------------- comments
FEEDBACK = {
    5: ["Sản phẩm y hình, đóng gói cẩn thận, giao hàng nhanh. Sẽ ủng hộ shop dài dài!",
        "Chất lượng tuyệt vời so với giá tiền, rất đáng mua.",
        "Giao hàng siêu nhanh, mới đặt hôm qua hôm nay đã nhận. Shop tư vấn nhiệt tình.",
        "Hàng chính hãng, dùng rất thích. Cho shop 5 sao!",
        "Mua lần thứ 3 rồi, chất lượng vẫn ổn định như ngày đầu.",
        "Đóng gói chắc chắn, có cả quà tặng kèm. Cảm ơn shop nhiều ạ.",
        "Rất hài lòng, sẽ giới thiệu cho bạn bè cùng mua."],
    4: ["Sản phẩm tốt, giao hơi chậm một chút nhưng vẫn chấp nhận được.",
        "Hàng ổn trong tầm giá, đóng gói kỹ.",
        "Dùng ok, màu sắc hơi khác ảnh một chút xíu.",
        "Chất lượng khá, shop phản hồi nhanh. Trừ 1 sao vì hộp hơi móp.",
        "Tốt, đúng mô tả. Lần sau mong shop có thêm khuyến mãi."],
    3: ["Sản phẩm tạm được, chưa thật sự nổi bật.",
        "Giao hàng chậm hơn dự kiến 2 ngày, chất lượng bình thường.",
        "Dùng được, nhưng giá hơi cao so với mặt bằng chung."],
    2: ["Hàng không giống mô tả lắm, hơi thất vọng.",
        "Đóng gói sơ sài, sản phẩm bị trầy nhẹ."],
}
comments = []
delivered = {o[0]: o for o in orders if o[4] == 4}
seen = set()
for od in orderdetails:
    o = delivered.get(od[3])
    if not o or rnd.random() > 0.3:
        continue
    key = (o[3], od[2])
    if key in seen:
        continue
    seen.add(key)
    rate = rnd.choices([5, 4, 3, 2], [58, 28, 10, 4])[0]
    post = min(o[5] + dt.timedelta(days=rnd.randint(1, 6)), TODAY)
    comments.append((rate, rnd.choice(FEEDBACK[rate]), post, od[2], o[3]))

# ----------------------------------------------------------------------------- discounts
def code():
    return "-".join("".join(str(rnd.randint(1, 9)) for _ in range(4)) for _ in range(4))


discounts = []
for free, dolar, start_off, days, number in [
    (10, None, -10, 30, 300), (15, None, -5, 20, 200), (20, None, -2, 14, 150), (30, None, 0, 7, 80),
    (50, None, -1, 3, 20), (None, 5, -15, 45, 500), (None, 10, -3, 25, 120), (None, 20, 0, 10, 40),
    (15, None, -60, 20, 100), (30, None, -45, 10, 60), (None, 10, -90, 30, 200), (50, None, -120, 5, 30),
]:
    start = TODAY + dt.timedelta(days=start_off)
    discounts.append((code(), start, start + dt.timedelta(days=days), free, dolar, number))

# ----------------------------------------------------------------------------- product ↔ staff
product_account = []
active_staffs = [s for s in staffs if s["active"]]
for i, p in enumerate(products):
    owner = staff_demo if i % 2 == 0 else active_staffs[1 + (i // 2) % (len(active_staffs) - 1)]
    product_account.append((p["id"], owner["username"]))

wait_for_staff = [c["username"] for c in customers[5:8]]

# ----------------------------------------------------------------------------- histories & notifications
REPORT_FILES = ["Bao cao doanh thu tung san pham.pdf", "Bao cao ton kho.pdf", "4122022.Bao cao luong nhan vien.pdf",
                "Bao cao 2022.12.22.pdf"]
histories, notifications = [], []
for k in range(14):
    s = rnd.choice(active_staffs[:5])
    when = dt.datetime.combine(TODAY - dt.timedelta(days=k * 2 + rnd.randint(0, 1)), dt.time(rnd.randint(8, 18), rnd.randint(0, 59)))
    f = rnd.choice(REPORT_FILES)
    title = f"Nhân viên {s['fullname']} đã gửi báo cáo {when:%d/%m/%Y}"
    histories.append((title, "/files/" + f, when, k > 2, s["username"]))
    notifications.append((title, f, when.date(), k < 4, admin["username"]))
    notifications.append((f"Đã gửi báo cáo thành công {when:%d/%m/%Y}", f, when.date(), False, s["username"]))
for k, s in enumerate(staffs[7:]):
    when = dt.datetime.combine(TODAY - dt.timedelta(days=9 + k * 5), dt.time(10, 15 + k))
    histories.append((f"Bạn đã thay đổi trạng thái nhân viên {s['fullname']} thành Dừng làm việc", None, when, True, admin["username"]))

for o in [o for o in orders if o[3] == user_demo["username"]][-8:]:
    st = dict(STATUSES)[o[4]]
    notifications.append((f"Đơn hàng #{o[0]} của bạn: {st.lower()}.", "- Tin nhắn thông báo -", o[5] or o[2],
                          o[4] in (0, 1, 2, 3), user_demo["username"]))
notifications.append(("Chào mừng bạn đến với Fastshop! Nhập mã voucher ở trang Ưu đãi để được giảm giá.",
                      "- Tin nhắn thông báo -", TODAY - dt.timedelta(days=30), False, user_demo["username"]))
for o in [o for o in orders if o[4] == 0][:5]:
    notifications.append((f"Có đơn hàng mới #{o[0]} cần xác nhận", "- Tin nhắn thông báo -", TODAY, True, staff_demo["username"]))

# ----------------------------------------------------------------------------- write SQL
kept_cats = set(CATEGORIES)
catdetails = [(cid, prop.strip(), cat) for cid, prop, cat in src_catdetails if cat in kept_cats]
catdetail_ids = {c[0] for c in catdetails}
catdetails += [("CL_QA", "Chất liệu vải", "QAM"), ("SZ_QA", "Kích cỡ", "QAM"), ("XX_QA", "Xuất xứ", "QAM"),
               ("CL_QF", "Chất liệu vải", "QAF"), ("SZ_QF", "Kích cỡ", "QAF"), ("XX_QF", "Xuất xứ", "QAF")]
SPEC = {
    "Xuất xứ": ["Việt Nam", "Thái Lan", "Nhật Bản", "Hàn Quốc", "Đức", "Trung Quốc", "Mỹ"],
    "Dung tích của bia": ["330 ml/lon", "500 ml/lon", "250 ml/chai", "330 ml/chai"],
    "Hướng dẫn sử dụng": ["Dùng ngon nhất khi ướp lạnh 6–8°C", "Lắc đều trước khi dùng", "Dùng trực tiếp hoặc pha loãng tùy khẩu vị"],
    "Nồng độ cồn": ["4.3%", "4.9%", "5%", "5.9%", "7%"],
    "Thành phần bia": ["Nước, malt đại mạch, hoa bia, men bia", "Nước, lúa mì, malt, hoa bia"],
    "Hướng dẫn cách bảo quản": ["Nơi khô ráo, tránh ẩm", "Tránh va đập mạnh"],
    "Model bóng đèn": ["LED A60", "LED Bulb E27", "Tapo L530E", "RD-A60/9W"],
    "Trọng lượng": ["120 g", "250 g", "450 g", "800 g", "1.2 kg", "1.6 kg"],
    "Tuổi thọ": ["15.000 giờ", "20.000 giờ", "25.000 giờ"],
    "Vật liệu": ["Nhựa PC chống cháy", "Gỗ sồi tự nhiên", "Hợp kim nhôm", "Vải canvas"],
    "Chất liệu vải": ["Polyester chống thấm", "Vải canvas", "Cotton 100%", "Da PU cao cấp", "Kaki co giãn"],
    "Kích thước sản phẩm": ["30 x 12 x 42 cm", "28 x 14 x 45 cm", "32 x 15 x 48 cm"],
    "Mục đích sử dụng": ["Đi học, đi làm", "Du lịch, dã ngoại", "Đựng laptop 15.6 inch"],
    "Sức chứa tối đa": ["15 lít", "20 lít", "25 lít"],
    "Tổng số ngăn": ["3 ngăn", "4 ngăn", "5 ngăn"],
    "Loại màn hình": ["Không màn hình", "LCD 2 inch"],
    "Model của camera": ["Ezviz C6N", "Imou IPC-C22EP", "Tapo C310", "V380 E06"],
    "Độ phân giải của camera": ["1080p Full HD", "2K", "4K"],
    "Dung lượng thẻ nhớ": ["Hỗ trợ tối đa 128 GB", "Hỗ trợ tối đa 256 GB"],
    "Cách bảo quản sản phẩm": ["Đậy nắp kín sau khi sử dụng, tránh ánh nắng trực tiếp"],
    "Cách sử dụng": ["Làm ướt tóc, thoa dầu gội, massage nhẹ rồi xả sạch"],
    "Thành phần dầu gội": ["Chiết xuất bưởi, vitamin B5, keratin", "Bạc hà, zinc pyrithione", "Chiết xuất bồ kết, hà thủ ô"],
    "Khả năng chịu đựng": ["Chống nước 3 ATM", "Chống nước 5 ATM", "Chống nước sinh hoạt"],
    "Chất liệu dây đồng hồ": ["Dây da bò thật", "Dây thép không gỉ", "Dây silicone"],
    "Chất liệu kim loại": ["Thép không gỉ 316L", "Hợp kim mạ vàng"],
    "Chất liệu kính đồng hồ": ["Kính khoáng", "Kính sapphire"],
    "Model đồng hồ": ["MTP-V004L", "MTP-E320D", "T7015-1", "DH44"],
    "Chạy bằng năng lượng": ["Pin quartz", "Cơ tự động", "Năng lượng ánh sáng"],
    "Bluetooth điện thoại": ["Bluetooth 5.0", "Bluetooth 5.2"],
    "Chip set": ["Apple A11 Bionic", "Snapdragon 835", "Kirin 970", "Exynos 7885"],
    "Độ phân giải màn hình": ["Full HD+ (1080 x 2160)", "HD+ (750 x 1334)", "QHD+ (1440 x 2880)"],
    "Loại cổng sạc": ["USB Type-C", "Lightning"],
    "Loại/Công nghệ màn hình": ["IPS LCD", "OLED", "Super AMOLED"],
    "Model của điện thoại": ["2023", "2024"],
    "Loại pin điện thoại": ["Li-Ion 3000 mAh", "Li-Po 4000 mAh", "Li-Po 3300 mAh"],
    "Tốc độ CPU": ["2.39 GHz", "2.45 GHz", "2.36 GHz"],
    "Công suất tối đa": ["3 W", "5 W", "9 W"],
    "Điện áp cần thiết": ["220 V", "5 V USB"],
    "Kích thước chiều cao": ["15 cm", "22 cm", "30 cm"],
    "Kích thước giày dép": ["Size 36 – 44", "Size 38 – 43", "Size 35 – 40"],
    "Vật liệu thiết kế": ["Da tổng hợp, đế cao su", "Vải lưới thoáng khí, đế EVA", "Cao su đúc nguyên khối"],
    "Cách bảo quản gia vị": ["Bảo quản nơi khô ráo, thoáng mát"],
    "Dung tích dung lượng": ["100 g", "200 g", "350 g", "500 g"],
    "Thành phần gia vị": ["Muối, đường, tỏi, ớt, tiêu, hành", "Hồi, quế, thảo quả, đinh hương"],
    "Chất liệu làm từ": ["Nhựa PP nguyên sinh, không BPA"],
    "Dung tích": ["500 ml", "750 ml", "1 lít"],
    "Kích thước M": ["12 x 12 x 6 cm", "16 x 16 x 8 cm", "20 x 20 x 10 cm"],
    "Tính năng": ["Dùng được trong lò vi sóng và tủ lạnh"],
    "Chíp CPU laptop": ["Intel Core i7-10750H", "Apple M1", "Intel Core i5-1155G7"],
    "Chip card của laptop": ["NVIDIA RTX 3050", "Intel Iris Xe", "Apple GPU 8 nhân"],
    "Tên hãng laptop": ["Apple", "MSI"],
    "Kích thước màn hình laptop": ["13.3 inch", "14 inch", "15.6 inch"],
    "Loại pin của laptop": ["3 cell 52 Wh", "Li-Po 58 Wh"],
    "Dung lượng Ram": ["8 GB", "16 GB"],
    "Lợi ích của rau củ": ["Giàu vitamin và chất xơ, tốt cho tiêu hóa"],
    "Khối lượng rau củ": ["250 g", "300 g", "500 g", "1 kg"],
    "Nơi cung cấp rau củ": ["Nông trại hữu cơ Đà Lạt", "HTX rau sạch Củ Chi"],
    "Kích cỡ": ["S, M, L, XL", "M, L, XL, XXL"],
}


def spec_value(prop):
    prop = prop.strip()
    for key, values in SPEC.items():
        if prop.startswith(key) or key in prop:
            return rnd.choice(values)
    return None


catdetail_ids = {c[0] for c in catdetails}
existing = {(int(pid), cdid): info.strip() for _id, cdid, info, pid in src_proddetails
            if int(pid) in prod_by_id and cdid in catdetail_ids and info}
proddetails = []
for p in products:
    for cdid, prop, cat in catdetails:
        if cat != p["cat"]:
            continue
        info = existing.get((p["id"], cdid))
        if not info:
            info = p["name"].split(" - ")[0] if prop.startswith("Model") else spec_value(prop)
        if info:
            proddetails.append((info, cdid, p["id"]))

sql = [
    "-- Dữ liệu demo Fastshop. Sinh tự động bởi database/seed/generate_seed.py — không sửa tay.",
    "SET NAMES utf8mb4;",
    "SET FOREIGN_KEY_CHECKS = 0;",
    insert("roles", ["id", "name", "level"], ROLES),
    insert("status", ["id", "kind"], STATUSES),
    insert("accounts", ["username", "email", "fullname", "password", "phone", "active"],
           [(a["username"], a["email"], a["fullname"], a["password"], a["phone"], a["active"]) for a in accounts]),
    insert("authorities", ["username", "roleid"], authorities),
    insert("categories", ["id", "name", "status", "unit"], [(k, v[0], True, v[1]) for k, v in CATEGORIES.items()]),
    insert("categorydetails", ["id", "property", "category_id"], catdetails),
    insert("products", ["id", "Createdate", "image", "name", "price", "categoryid", "number", "available", "description"],
           [(p["id"], p["created"], p["image"], p["name"], p["price"], p["cat"], p["number"], p["available"], p["desc"]) for p in products]),
    insert("productdetails", ["info", "categoryDetailId", "productId"], proddetails),
    insert("product_account", ["product_id", "account_username"], product_account),
    insert("addresses", ["place", "is_choose", "username"], addresses),
    insert("atm", ["name", "company", "number", "username", "is_valid"], atms),
    insert("orders", ["id", "address", "Createdate", "Username", "statusId", "dateConfirm", "total"], orders),
    insert("orderdetails", ["id", "quantity", "Productid", "Orderid"], orderdetails),
    insert("comments", ["rate", "feedback", "datePost", "Productid", "Username"], comments),
    insert("discounts", ["id", "DateFrom", "DateEnd", "free", "dolar", "number"], discounts),
    insert("histories", ["title", "link", "schedual", "status", "username"], histories),
    insert("notifications", ["title", "fileName", "sentDate", "status", "AccountId"], notifications),
    insert("waitforstaff", ["account_username"], [(u,) for u in wait_for_staff]),
    "SET FOREIGN_KEY_CHECKS = 1;",
]
os.makedirs(os.path.dirname(OUT_SQL), exist_ok=True)
with open(OUT_SQL, "w", encoding="utf-8") as fh:
    fh.write("\n\n".join(sql) + "\n")

print(f"accounts={len(accounts)} products={len(products)} orders={len(orders)} details={len(orderdetails)} "
      f"comments={len(comments)} discounts={len(discounts)} -> {os.path.relpath(OUT_SQL, ROOT)}")
