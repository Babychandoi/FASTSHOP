var host_ = "/rest";
var number = document.getElementById("number");       // _mode_cart.html
var username = document.getElementById("username");   
var account_ = document.getElementById("username_");  // _account.html

var id = account_ ? account_.innerHTML : null;
var app = angular.module("app", []);
if (number && id) {
    number.innerHTML = (localStorage.getItem(id) == undefined) ? 0 : JSON.parse(localStorage.getItem(id)).length;
}
if (localStorage.getItem("rate") != undefined) {
    localStorage.removeItem("rate");
};

function getDateNow(days) {
    var date = new Date();
    var year = date.getFullYear().toString();
    var month = date.getMonth() + 1;
    var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    var strDate = year + "-" + month + "-" + day;
    if (days == null || days == 0) {
        return strDate;   
    }
    else {
        date.setDate(date.getDate() + days);
        var year1 = date.getFullYear().toString();
        var month1 = date.getMonth() + 1;
        var day1 = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
        var strDate1 = year1 + "-" + month1 + "-" + day1;
        return strDate1;   
    }
};

function convert_vi_to_en(str) {
    str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
    str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
    str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
    str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
    str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
    str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
    str = str.replace(/đ/g, "d");
    str = str.replace(/À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, "A");
    str = str.replace(/È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, "E");
    str = str.replace(/Ì|Í|Ị|Ỉ|Ĩ/g, "I");
    str = str.replace(/Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, "O");
    str = str.replace(/Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, "U");
    str = str.replace(/Ỳ|Ý|Ỵ|Ỷ|Ỹ/g, "Y");
    str = str.replace(/Đ/g, "D");
    str = str.replace(/!|@|%|\^|\*|\(|\)|\+|\=|\<|\>|\?|\/|,|\.|\:|\;|\'|\"|\&|\#|\[|\]|~|\$|_|`|-|{|}|\||\\/g, " ");
    str = str.replace(/  +/g, ' ');
    return str;
};

function changeFree() {
    var v = document.getElementById("codeFree").value.trim();
    $("#success_find").css("opacity", "0");
    $("#error_find").css("opacity", "0");
    if (v) {
        $("#btn-free").removeAttr("disabled");
    } else {
        $("#btn-free").attr("disabled", "disabled");
    }
}

function copyFree(index) {
    let code = document.getElementById("voucher-" + index);
    console.log(code.value);
    code.select();
    code.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(code.value);
    if (document.execCommand("copy")) {
        $("#link-voucher-" + index).css({"color":"green"});
        $("#link-voucher-" + index).text("Copied");
    }
}

app.controller("admin-employee", ($scope, $http) => {
    
});

app.controller("history-ctrl", ($scope, $http) => {
    $scope.histories = [];
    $scope.selected = {};
    $scope.all = false;
    $scope.loaded = false;

    $scope.load_histories = () => {
        var url = `${host_}/history/all`;
        $http.get(url).then((resp) => {
            // mới nhất lên đầu
            $scope.histories = (resp.data || []).sort((a, b) => String(b.schedual).localeCompare(String(a.schedual)));
            $scope.loaded = true;
        }).catch((err) => {
            $scope.loaded = true;
            console.log("Error load histories", err);
        });
    };

    $scope.selectedIds = () => $scope.histories.filter(h => $scope.selected[h.id]).map(h => h.id);

    $scope.choose_all = () => {
        $scope.histories.forEach(h => { $scope.selected[h.id] = $scope.all; });
    };

    $scope.syncAll = () => {
        $scope.all = $scope.histories.length > 0 && $scope.selectedIds().length == $scope.histories.length;
    };

    var drop = (id) => {
        var index = $scope.histories.findIndex(item => item.id == id);
        if (index >= 0) $scope.histories.splice(index, 1);
        delete $scope.selected[id];
        $scope.syncAll();
    };

    $scope.remove = (id) => {
        if (!confirm("Xóa mục lịch sử này?")) return;
        $http.delete(`${host_}/history/${id}`).then(() => drop(id))
            .catch(() => alert("Không xóa được, vui lòng thử lại."));
    };

    $scope.delete = () => {
        var ids = $scope.selectedIds();
        if (ids.length == 0) return;
        if (!confirm("Xóa " + ids.length + " mục lịch sử đã chọn?")) return;
        ids.forEach(id => {
            $http.delete(`${host_}/history/${id}`).then(() => drop(id))
                .catch(() => alert("Không xóa được mục #" + id + "."));
        });
    };

    $scope.load_histories();
});

app.controller('chart-ctrl', ($scope, $http) => {
	$scope.titles = [];
	$scope.load_title = () => {
		var url = `${host_}/orderdetails/category/report`;
		$http
			.get(url)
			.then((resp) => {
				$scope.titles = resp.data;
				setTimeout(() => {
					var ctx = document.getElementById('myChart');
					if (ctx && typeof Chart !== 'undefined') {
						// Destroy existing chart if any
						if ($scope.chartInstance) {
							$scope.chartInstance.destroy();
						}
						$scope.chartInstance = new Chart(ctx, {
							type: 'bar',
							data: {
								labels: $scope.titles.map(item => (item.group && item.group.name) || item.group),
								datasets: [
									{
										label: 'Số lượt bán theo loại hàng',
										data: $scope.titles.map(item => item.count),
										backgroundColor: 'rgba(32, 107, 196, 0.75)',
										borderRadius: 4,
										maxBarThickness: 42,
									},
								],
							},
							options: {
								responsive: true,
								maintainAspectRatio: true,
								plugins: { legend: { display: false } },
								scales: { y: { beginAtZero: true } }
							}
						});
					} else {
						console.log('Canvas element not found or Chart.js not loaded');
					}
				}, 100);
			})
			.catch((err) => {
				console.log('Error load items', err);
			});
	};

	// Wait for DOM to be ready
	setTimeout(() => {
		$scope.load_title();
	}, 200);
});

app.controller('chart2-ctrl', ($scope, $http) => {
	$scope.titles = [];
    $scope.revenue = {
        item: [],
        get total() {
            var sum = this.item
                        .map(item => item || 0)
                        .reduce((total, r) => total + r, 0);
            return sum > 0 ? sum : 1; // Avoid division by zero
        }
    };
    
    $scope.loadData = () => {
        // Load data first
        var promises = [];
        for (let index = 0; index < 5; index++) {
            var year = new Date().getFullYear() - index;
            var urlRevenue = `${host_}/orders/year/${year}`;
            promises.push(
                $http.get(urlRevenue).then((resp) => {
                    $scope.revenue.item[index] = resp.data || 0;
                }).catch((err) => {
                    $scope.revenue.item[index] = 0;
                    console.log('Error loading revenue for year ' + year, err);
                })
            );
        }
        
        // Wait for all data to load, then load Google Charts
        Promise.all(promises).then(() => {
            if (typeof google !== 'undefined' && google.charts) {
                google.charts.load('current', {'packages':['corechart']});
                google.charts.setOnLoadCallback(() => {
                    setTimeout(() => {
                        $scope.drawChart();
                    }, 200);
                });
            } else {
                console.log('Google Charts not loaded');
            }
        });
    };
    
    $scope.drawChart = () => {
        var chartDiv = document.getElementById('myChart23');
        if (!chartDiv) {
            console.log('Chart div not found');
            return;
        }
        
        try {
            var data = google.visualization.arrayToDataTable([
                ['Năm', 'Doanh thu ($)'],
                ['Năm ' + (new Date().getFullYear() - 4), $scope.revenue.item[4] || 0],
                ['Năm ' + (new Date().getFullYear() - 3), $scope.revenue.item[3] || 0],
                ['Năm ' + (new Date().getFullYear() - 2), $scope.revenue.item[2] || 0],
                ['Năm ' + (new Date().getFullYear() - 1), $scope.revenue.item[1] || 0],
                ['Năm ' + (new Date().getFullYear() - 0), $scope.revenue.item[0] || 0]
            ]);
            var chart = new google.visualization.PieChart(chartDiv);
            chart.draw(data, {
                height: 360,
                pieHole: 0.45,
                chartArea: { left: 16, top: 16, width: '92%', height: '88%' },
                legend: { position: 'right', alignment: 'center', textStyle: { fontSize: 13 } },
                colors: ['#a8c5ea', '#79a6dc', '#4a86cf', '#206bc4', '#174e8f'],
                fontName: 'inherit'
            });
        } catch (error) {
            console.log('Error drawing chart:', error);
        }
    };
    
    // Initialize after DOM is ready
    setTimeout(() => {
        $scope.loadData();
    }, 300);
});

app.controller("category-ctrl", ($scope, $http) => {
    $scope.categories = [];
    $scope.index = 0;
    $("#prev").hide();

    $scope.load_categories = () => {
        var url = `${host_}/categories`;
        $http.get(url).then((resp) => {
            $scope.categories = resp.data;
        }).catch((err) => {
            console.log("Error load items", err);
        });
    };
    
    $scope.prev = () => {
        $scope.index = 0;
        $("#next").show();
        $("#prev").hide();
    };

    $scope.next = () => {
        $scope.index = 1;
        $("#next").hide();
        $("#prev").show();
    };


    $scope.load_categories();
});

app.controller("atm-ctrl", ($scope, $http) => {
    $scope.atm = {
        "name":"",
        "company": "",
        "number": "",
        "account":{},
        "valid":false
    };

    $http.get(`${host_}/accounts/${id}`).then((resp) => {
        $scope.atm.account = resp.data;
        $scope.atm.name = convert_vi_to_en(String($scope.atm.account.fullname).toUpperCase());
    }).catch((err) => {
        console.log("ATM cannot account", err);
    });

    $scope.them = () => {
        $scope.errorNumber = "";
        $scope.errorSelect = "";
        if ($scope.atm.company == "" || $scope.atm.company == undefined) {
            $scope.errorSelect = "(*) Vui lòng chọn ngân hàng";
        }
        if ($scope.atm.number == "" || $scope.atm.number == undefined) {
            $scope.errorNumber = "(*) Vui lòng nhập số tài khoản";
        }
        else if (!/^[0-9 ]{6,25}$/.test($scope.atm.number)) {
            $scope.errorNumber = "(*) Số tài khoản chỉ gồm chữ số (6–25 ký tự)";
        }
        if ($scope.errorNumber || $scope.errorSelect) {
            return;
        }
        else {
            $scope.errorNumber = "";
            $scope.errorSelect = "";
            var url_c = `${host_}/atm`;
            $("#load-page").css("display", "block");


            $http.post(url_c, $scope.atm).then((resp) => {
                window.location.href = "/user/wallet";
            }).catch((err) => {
                $("#load-page").attr("style", "display: none !important;");
                $scope.errorNumber = "(*) Không thể lưu thẻ, số tài khoản có thể đã tồn tại.";
            });
        }
    };

});

// cart là cái chỉ để thêm sản phẩm vào giỏ hàng
app.controller("cart-ctrl", ($scope, $http) => {

    // luôn lấy dữ liệu từ trong localStorage để khi bấm F5 ko bị mất
    if (!account_ || !number) {
        return; // Thoát nếu không có element cần thiết (trang admin có thể không có)
    }
    
    var id = account_.innerHTML;
    number.innerHTML = (localStorage.getItem(id) == undefined) ? 0 : JSON.parse(localStorage.getItem(id)).length;

    $scope.qty = 1;

    $scope.number_orgin = Number(document.getElementById("numberProduct").innerHTML);

    $scope.minus = () => {
        $scope.qty = ($scope.qty < 2) ? 1 : $scope.qty-1;
    };

    $scope.plus = () => {
        $scope.qty++;
        if ($scope.qty > $scope.number_orgin) {
            $scope.qty = $scope.number_orgin;
            $('.toast-warning').toast('show');
            setTimeout(() => {
                $('.toast-warning').toast('hide');
            }, 1100);
        }
    };

    $scope.cart = {
        items: [],

        // goCheckout = true: "Mua ngay" -> chỉ chuyển trang sau khi đã lưu giỏ hàng
        add(id_product, goCheckout) {
            var done = () => {
                if (goCheckout) {
                    window.location.href = "/user/checkout";
                } else {
                    if (typeof window.add === "function") window.add(true);   // toast "Thêm sản phẩm thành công" (all_feature.js)
                }
            };
            var item = this.items.find(item => item.id == id_product);
            if (item) {
                item.qty += $scope.qty;
                if (item.number > 0 && item.qty > item.number) {
                    item.qty = item.number;
                }
                this.saveToLocalStorage();
                done();
            }
            else {
                $http.get(`${host_}/products/${id_product}`).then(resp => {
                    resp.data.qty = $scope.qty;
                    this.items.push(resp.data);
                    this.saveToLocalStorage();
                    number.innerHTML = Number(number.innerHTML) + 1;   // để mặc định number sẽ là $scope.count (+1) để hiện 1 ngay - nó sẽ là localStorage đang giữ bao nhiêu item, nên ko nhất định là 1;
                    done();
                }).catch(() => alert("Không thêm được sản phẩm vào giỏ hàng, vui lòng thử lại."));
            }
        },

        //Lưu giỏ hàng vào local storage
        saveToLocalStorage(){
            var json = JSON.stringify(angular.copy(this.items));
            localStorage.setItem(id, json);
        },

        //Đọc giỏ hàng từ local storage
        loadFromLocalStorage(){
            var json = localStorage.getItem(id);
            this.items = json ? JSON.parse(json) : [];
        }
    }

    $scope.show_discount = (id) => {
        document.getElementById("voucher-" + id).style.display = "block";
        document.getElementById("btn-voucher-" + id).style.display = "none";
        document.getElementById("link-voucher-" + id).style.display = "block";
    }

    $scope.cart.loadFromLocalStorage();
});

// cái này là để hiển thị danh sách sản phẩm và thanh toán luôn
var SHIP_FEE = 1.2;
app.controller("checkout-ctrl", ($scope, $http) => {
    var date = new Date().toLocaleDateString("en-CA").toString();

    $scope.qty = 1;

    $scope.discount_add_voucher = {};

    $scope.cart = {
        items: [],

        minus(id_product) {
            var item = this.items.find(item => item.id == id_product);
            item.qty = (item.qty < 2) ? 1 : item.qty - 1;
            this.saveToLocalStorage();
        },
    
        plus(id_product) {
            var item = this.items.find(item => item.id == id_product);
            item.qty++;
            if (item.qty > item.number) {
                item.qty = item.number;
            }
            this.saveToLocalStorage();
        },


        // Thêm sản phẩm vào giỏ hàng
        add(id_product){
            var item = this.items.find(item => item.id == id_product);
            if(item) {
                item.qty++;
                this.saveToLocalStorage();
            }
            else {
                $http.get(`${host_}/products/${id_product}`).then(resp => {
                    resp.data.qty = 1;
                    this.items.push(resp.data);
                    this.saveToLocalStorage();
                })
            }
        },

        // Xóa sản phẩm khỏi giỏ hàng
        remove(id_product){
            var index = this.items.findIndex(item => item.id == id_product);
            this.items.splice(index, 1);
            number.innerHTML -= 1;
            this.saveToLocalStorage();
        },

        // Xóa sạch các mặt hàng trong giỏ
        clear(){
            this.items = [];
            number.innerHTML = 0;
            this.saveToLocalStorage();
        },

        // Tính thành tiền của 1 sản phẩm
        amt_of(item) {
            return item.price * item.qty;
        },

        // Tính tổng số lượng các mặt hàng trong giỏ
        get count() {
            return this.items
                       .map(item => item.qty)
                       .reduce((total, qty) => total += qty, 0);
        },

        // Tổng thành tiền các mặt hàng trong giỏ
        get amount() {
            return this.items
                       .map(item => item.qty*item.price)
                       .reduce((total, qty) => total += qty,0);
        },

        // Số tiền được giảm bởi voucher (theo % hoặc tiền mặt)
        get discountAmount() {
            var v = $scope.discount_add_voucher || {};
            var tongtien = Number(this.amount + SHIP_FEE);
            if (v.free > 0) {
                return tongtien * v.free / 100;
            }
            if (v.dolar > 0) {
                return Math.min(v.dolar, tongtien);
            }
            return 0;
        },

        get amountDeliver() {
            return Number(this.amount + SHIP_FEE) - this.discountAmount;
        },

        // Lưu giỏ hàng vào local storage
        saveToLocalStorage() {
            var json = JSON.stringify(angular.copy(this.items));
            localStorage.setItem(id, json);
        },

        // Đọc giỏ hàng từ local storage
        loadFromLocalStorage() {
            var json = localStorage.getItem(id);
            this.items= json ? JSON.parse(json):[];
        }
    }
    $scope.cart.loadFromLocalStorage();

    $scope.confirmFree = () => {
        var code = $("#codeFree").val().trim();
        var showError = (msg) => {
            $("#find-free").css("display", "none");
            $("#error_find").text(msg).css("opacity", "1");
            $("#success_find").css("opacity", "0");
        };
        if (!code) {
            return showError("(*) Bạn chưa nhập mã giảm giá.");
        }
        $("#error_find").css("opacity", "0");
        $("#find-free").css("display", "block");
        $http.get(`${host_}/discounts/${encodeURIComponent(code)}`).then((resp) => {
            var v = resp.data;
            var today = new Date().toLocaleDateString("en-CA");
            if (!v) {
                return showError("(*) Mã giảm giá không tồn tại.");
            }
            if (String(v.dateFrom).substring(0, 10) > today) {
                return showError("(*) Mã giảm giá chưa đến thời gian sử dụng.");
            }
            if (String(v.dateEnd).substring(0, 10) < today) {
                return showError("(*) Mã giảm giá đã hết hạn sử dụng.");
            }
            if (v.number != null && v.number <= 0) {
                return showError("(*) Mã giảm giá đã hết lượt sử dụng.");
            }
            $scope.discount_add_voucher = v;
            $("#find-free").css("display", "none");
            $("#success_find").text("(*) Đã áp dụng mã giảm " + (v.free > 0 ? v.free + "%" : "$" + v.dolar) + ".")
                              .css({"opacity": "1", "color": "#379237"});
            $("#btn-enter-free").text(v.id);
        }).catch(() => showError("(*) Không kiểm tra được mã giảm giá, vui lòng thử lại."));
    }

    // ---------------------------------------------------------
    $scope.form = {
        "address": document.getElementById("order-address-buy").innerHTML,
        "account": {},
        "createDate": date,
        "dateConfirm":null,
        "total" : Math.round($scope.cart.amountDeliver * 100) / 100, 
        "status": {}
    };

    $http.get(`${host_}/accounts/${id}`).then((resp) => {
        $scope.form.account = resp.data;
    }).catch((err) => {
        console.log("Order cannot account", err);
    });

    $http.get(`${host_}/status/0`).then((resp) => {
        $scope.form.status = resp.data;
    }).catch((err) => {
        console.log("Order cannot account", err);
    });


    $scope.orderNow = {};
    $scope.ordering = false;

    $scope.order = {
        thread1() {
            if ($scope.ordering) {
                return;
            }
            if ($scope.cart.items.length == 0) {
                alert("Giỏ hàng của bạn đang trống.");
                return;
            }
            if (!$scope.form.address || !$scope.form.address.trim()) {
                alert("Vui lòng chọn địa chỉ giao hàng trước khi đặt hàng.");
                return;
            }
            $scope.ordering = true;
            $("#load-page").attr("style", "background-color: rgba(0,0,0,.5); height: 100vh; position: fixed; top:0; left:0; z-index: 2000;");

            // server tự tính giá, tổng tiền, voucher và trừ tồn kho trong 1 giao dịch
            var request = {
                "address": $scope.form.address,
                "voucher": ($scope.discount_add_voucher && $scope.discount_add_voucher.id) || null,
                "items": $scope.cart.items.map((item) => ({ "productId": item.id, "quantity": item.qty }))
            };

            $http.post(`${host_}/orders/checkout`, request).then((resp) => {
                $scope.orderNow = resp.data;
                localStorage.removeItem(id);
                window.location.href = "/user/myorder";
            }).catch((err) => {
                console.log("Order failed", err);
                $scope.ordering = false;
                $("#load-page").attr("style", "display: none !important;");
                alert((err && typeof err.data === "string" && err.data) ? err.data : "Đặt hàng thất bại, vui lòng thử lại.");
            });
        }
    }
});

app.controller("report-staff-ctrl", ($scope, $http) => {
    $scope.items = [];
    $scope.stock = [];
    $scope.categories = [];
    $scope.authorities = [];
    $scope.discount = [];
    $scope.indexes_1 = [];
    $scope.indexes_2 = [];
    $scope.indexes_3 = [];
    $scope.indexes_4 = [];

    var url_product = `${host_}/products`;
    $http.get(url_product).then((resp) => {
        $scope.items = resp.data;
        $scope.stocks = resp.data;
    }).catch((err) => {});

    $scope.add_1 = (product) => {
        if ($("#pro-name-" + product.id).is(":checked")) {
            $scope.indexes_1.push(product);
        }
        else {
            var index = $scope.indexes_1.findIndex(item => item == product);
            $scope.indexes_1.splice(index, 1);
        }
    };

    // var url_productAll = `${host_}/staff/products/all`;
    // $http.get(url_productAll).then((resp) => {
    //     $scope.items = resp.data;
    // }).catch((err) => {
    //     console.error(err);
    // });

    // ------------------------------------
    var url_category = `${host_}/categories`;
    $http.get(url_category).then((resp) => {
        $scope.categories = resp.data;
    }).catch((err) => {});

    $scope.add_2 = (category) => {
        if ($("#cat-name-" + category.id).is(":checked")) {
            $scope.indexes_2.push(category);
        }
        else {
            var index = $scope.indexes_2.findIndex(item => item == category);
            $scope.indexes_2.splice(index, 1);
        }
    };

    // --------------------------------------
    var url_employee = `${host_}/authorities/staff/active`;
    $http.get(url_employee).then((resp) => {
        $scope.authorities = resp.data;
    }).catch((err) => {});
    
    $scope.add_3 = (auth) => {
        if ($("#emp-name-" + auth.account.username).is(":checked")) {
            $scope.indexes_3.push(auth);
        }
        else {
            var index = $scope.indexes_3.findIndex(item => item == auth);
            $scope.indexes_3.splice(index, 1);
        }
    };  

    // --------------------------------------
    $scope.add_4 = (product) => {
        if ($("#stock-name-" + product.id).is(":checked")) {
            $scope.indexes_4.push(product);
        }
        else {
            var index = $scope.indexes_4.findIndex(item => item == product);
            $scope.indexes_4.splice(index, 1);
        }
    }
});

app.controller("report-admin-ctrl", ($scope, $http) => {
    // chỗ này để sau này chèn thêm đồ thị
    // trong home admin html
});

app.controller("product-ctrl", ($scope, $http) => {
    $scope.form = {};
    $scope.items = [];
    $scope.now = getDateNow(null);

    var url1 = `${host_}/products`;
    $http.get(url1).then((resp) => {
        $scope.items = resp.data;
    }).catch((err) => {
        console.log("Error load items", err);
    });

    // ----------------------------------------------
    $scope.edit = (id) => {
        var url = `${host_}/products/${id}`;
        $http.get(url).then((resp) => {
            $scope.form = resp.data;
            console.log("edit ok", resp);
        }).catch((err) => {
            console.log("edit fail", err);
        });
    };

    $scope.update = () => {
        if (!$scope.form || !$scope.form.id) {
            console.error("Form data or ID is missing");
            alert("Không thể cập nhật: thiếu thông tin sản phẩm");
            return;
        }
        
        var item = angular.copy($scope.form);
        // Đảm bảo ID được set
        item.id = $scope.form.id;
        
        // Kiểm tra và xử lý category nếu cần
        if (item.category && typeof item.category === 'object' && item.category.id) {
            // Category đã là object, giữ nguyên
        } else if (item.category && typeof item.category === 'string') {
            // Nếu category là string (ID), cần tìm category object
            // Hoặc giữ nguyên nếu backend xử lý được
        }
        
        console.log("Updating product:", item);
        var url_p_u = `${host_}/products/${$scope.form.id}`;
        
        if (!url_p_u || url_p_u.includes('null') || url_p_u.includes('undefined')) {
            console.error("Invalid URL:", url_p_u);
            alert("Lỗi: URL không hợp lệ");
            return;
        }
        
        $http.put(url_p_u, item).then((resp) => {
            console.log("Update ok", resp);
            window.location.href = "/staff/products";
        }).catch((err) => {
            console.error("Error updating product:", err);
            var errorMsg = "Lỗi khi cập nhật sản phẩm";
            if (err.data) {
                errorMsg += ": " + (err.data.message || JSON.stringify(err.data));
            } else if (err.status) {
                errorMsg += " (Status: " + err.status + ")";
            }
            alert(errorMsg);
        });
    };

    $scope.delete = (id) => {
        if (!confirm("Ngừng bán sản phẩm này? Sản phẩm sẽ bị ẩn khỏi cửa hàng và tồn kho về 0.")) return;
        var item = angular.copy($scope.form);
        item.available = false;
        item.number = 0;
        var url = `${host_}/products/${id}`;
        $http.put(url, item).then((resp) => {
            window.location.href = "/staff/products";
        }).catch((err) => {
            alert("Không thể ngừng bán sản phẩm này.");
        });
    };
});

app.controller("order-ctrl", ($scope, $http) => {
    $scope.orders = [];
    $scope.orderdetails = [];
    $scope.bill = {};
    $scope.sum = 0;

    // load all of order
    $scope.load_orders = () => {
        var url_order = `${host_}/orders`;
        $http.get(url_order).then((resp) => {
            $scope.orders = resp.data;
            console.log("load order", resp);
        }).catch((err) => {
            console.log("error order", err);
        });
    };

    // load all of detail
    $scope.load_orderdetails = () => {
        var url_orderdetails = `${host_}/orderdetails`;
        $http.get(url_orderdetails).then((resp) => {
            $scope.orderdetails = resp.data;
            console.log("load order detail", resp);
        }).catch((err) => {
            console.log("error order detail", err);
        });
    };

    // edit order to show product and qua
    $scope.edit = (id) => {
        var url_orderdetails = `${host_}/details/order/${id}`;
        var s = 0;
        $http.get(url_orderdetails).then((resp) => {
            $scope.bill = resp.data;
            for (let index = 0; index < $scope.bill.length; index++) {
                s += Number($scope.bill[index].quantity) * Number($scope.bill[index].product.price);
            }
            $scope.sum = s;
            console.log("load form detail " + $scope.sum, resp);
        }).catch((err) => {
            console.log("error order detail", err);
        });
    };


    $scope.load_orders();
    $scope.load_orderdetails();
});

app.controller("keyword-ctrl", ($scope, $http) => {
    $scope.keysearch = "";
    $scope.keywords = [];

    var keywords_load = localStorage.getItem("keywords");
    if (keywords_load == undefined) {
        localStorage.setItem("keywords", JSON.stringify($scope.keywords));
    }
    else {
        $scope.keywords = JSON.parse(keywords_load);
    }


    // dang bi loi cho nay khi add va delete
    $scope.add = function () {
        var search_input = document.getElementById("search-gallery");
        search_input.addEventListener("keypress", function(event) {
            if (event.key === "Enter" && $scope.keysearch != "") {
                $scope.keywords.push($scope.keysearch);
                // this is create new set to access not duplicate data
                var set = new Set($scope.keywords);

                // this is convert Array from to Set before
                $scope.keywords = Array.from(set);

                // Save a array keywords after converted
                localStorage.setItem("keywords", JSON.stringify($scope.keywords));

                // go head a any page with URL + kw
window.location.href = "/user/search?keyword=" + encodeURIComponent($scope.keysearch);
            }
       });
    };

    $scope.delete = (kw) => {
        var index = $scope.keywords.findIndex(item => item == kw);
        $scope.keywords.splice(index, 1);
        localStorage.setItem("keywords", JSON.stringify($scope.keywords));
    };


    // -----------------------------------------------------------------------------------------------
    // do không thể dùng id của thymeleaf cho ng-click nên đổi category thành angular để lấy id
    $scope.categories = [];
    $scope.load_categories = () => {
        var url = `${host_}/categories`;
        $http.get(url).then((resp) => {
            $scope.categories = resp.data;
        }).catch((err) => {
            console.log("Error load items", err);
        });
    };
    $scope.load_categories();


    $scope.priceFrom = 0;
    $scope.priceTo   = 1000000;
    $scope.map_filter = {"rate": 0, "cateId": "", "priceFrom": $scope.priceFrom, "priceTo": $scope.priceTo};

    $scope.add_item_filter = (id) => {
        var item = String(id);
        var index = item.indexOf("-");
        var fStr = item.substring(0, index);
        var lstr = item.substring(index+1, item.length);
        $scope.map_filter[fStr] = lstr;
        console.log($scope.map_filter);
    };

    $scope.add_price_filter = () => {
        if ($scope.priceFrom < $scope.priceTo) {
            $scope.map_filter["priceFrom"] = $scope.priceFrom;
            $scope.map_filter["priceTo"] = $scope.priceTo;
        }
        console.log($scope.map_filter);
    };

    $scope.filter_submit = () => {
        var rate = Number($scope.map_filter["rate"]);
        var cateId = String($scope.map_filter["cateId"]);
        var priceFrom = Number($scope.map_filter["priceFrom"]);
        var priceTo = Number($scope.map_filter["priceTo"]);
var url = `/user/filter?rate=${rate}&cateId=${encodeURIComponent(cateId)}&priceFrom=${priceFrom}&priceTo=${priceTo}`;
        window.location.href = url;
    };

});

app.controller("changed-ctrl", ($scope, $http) => {
    $scope.pass1 = "";
    $scope.pass2 = "";
    $scope.pass3 = "";
    $scope.error1 = "";
    $scope.error2 = "";
    $scope.error3 = "";

    $scope.change = () => {
        $scope.error1 = !$scope.pass1 ? "(*) Vui lòng nhập mật khẩu hiện tại." : "";
        $scope.error2 = !$scope.pass2 ? "(*) Vui lòng nhập mật khẩu mới." :
                        ($scope.pass2.length < 6) ? "(*) Mật khẩu mới phải có ít nhất 6 ký tự." : "";
        $scope.error3 = !$scope.pass3 ? "(*) Vui lòng nhập lại mật khẩu mới." :
                        ($scope.pass2 != $scope.pass3) ? "(*) Mật khẩu xác nhận không trùng nhau." : "";
        if ($scope.error1 || $scope.error2 || $scope.error3) {
            return;
        }
        var url = `${host_}/accounts/${account_.innerHTML}/password`;
        $http.post(url, { currentPassword: $scope.pass1, newPassword: $scope.pass2 }).then(() => {
            alert("Đổi mật khẩu thành công");
            window.location.reload();
        }).catch((err) => {
            $scope.error1 = (err && typeof err.data === "string") ? err.data : "(*) Không đổi được mật khẩu, vui lòng thử lại.";
        });
    };
});

app.controller("comment-ctrl", ($scope, $http) => {
	let productid;
	let username;
    localStorage.removeItem("rate");   // bắt buộc khách chọn số sao trước khi gửi
	$scope.form = {
		product: {},
		account: {},
		datePost: getDateNow(),
		feedback: '',
		rate: Number(localStorage.getItem("rate"))
	};

	productid = (document.getElementById('productId') || {}).value;
	username = (document.getElementById('username') || {}).value;

	if (productid && username) {
		$http
			.get(`${host_}/products/${productid}`)
			.then((resp) => {
				$scope.form.product = resp.data;
			})
			.catch((err) => {
				console.log('Product cannot account', err);
			});

		$http
			.get(`${host_}/accounts/${username}`)
			.then((resp) => {
				$scope.form.account = resp.data;
			})
				.catch((err) => {
					console.log('Account cannot account', err);
				});
	}

	$scope.create = () => {
        var url = `${host_}/comments`;
        $scope.form.rate = Number(localStorage.getItem("rate")) || 0;
        $scope.form.feedback = document.getElementById('content-evaluate').value.trim();
        $scope.form.datePost = getDateNow();
        if ($scope.form.rate < 1 || $scope.form.rate > 5) {
            alert("Vui lòng chọn số sao đánh giá.");
            return;
        }
        if (!$scope.form.feedback) {
            alert("Vui lòng nhập nội dung đánh giá.");
            return;
        }
        var item = angular.copy($scope.form);

        $http.post(url, item).then((resp) => {
                localStorage.removeItem("rate");
                window.location.href = '/user/detail/' + productid;
            })
            .catch((err) => {
                console.log('Comment failed !', err);
                alert("Không gửi được đánh giá, vui lòng thử lại.");
            });
	};
});

app.controller("detail-staff", ($scope, $http) => {

    var productId = $("#id_product_staff").val();
    $scope.productdetails = [];
    $scope.saving = false;
    $scope.saved = false;
    try {
        $scope.saved = sessionStorage.getItem("detailSaved") == productId;
        sessionStorage.removeItem("detailSaved");
    } catch (e) {}
    var reloadAfterSave = () => {
        try { sessionStorage.setItem("detailSaved", productId); } catch (e) {}
        window.location.href = "/staff/detail/" + productId;
    };


    // false là mặc định chưa có gì trong info
    $scope.confirm = false;
    $http.get(`${host_}/product/detail/exist/${productId}`).then((resp) => {
        $scope.confirm = resp.data;
    }).catch((err) => {
        $scope.confirm = false;
    });

    // add product detail
    var url = `${host_}/product/detail/${productId}`;
    $http.get(url).then((resp) => {
        $scope.productdetails = resp.data;
    }).catch((err) => {
    });

    $scope.add = () => {
        $scope.saving = true;
        var requests = $scope.productdetails.map(item => $http.post(`${host_}/product/detail`, {
            categoryDetailId: item.categoryDetailId,
            info: item.info,
            productId: item.productId
        }));
        Promise.all(requests).then(reloadAfterSave).catch(() => {
            alert("Không lưu được thông số, vui lòng thử lại.");
            window.location.href = "/staff/detail/" + productId;
        });
    };


    // đây là hàm update từng productdetail
    $scope.update = () => {
        $scope.saving = true;
        var requests = $scope.productdetails.map(item => {
            var productDetail = {
                id: item.id,
                categoryDetailId: item.categoryDetailId,
                info: item.info,
                productId: item.productId
            };
            return item.id
                ? $http.put(`${host_}/product/detail/update/${item.id}`, productDetail)
                : $http.post(`${host_}/product/detail`, productDetail);
        });
        Promise.all(requests).then(reloadAfterSave).catch(() => {
            alert("Không lưu được thông số, vui lòng thử lại.");
            window.location.href = "/staff/detail/" + productId;
        });
    };
});

app.controller("discount-ctrl", ($scope, $http) => {
    // ngày dạng yyyy-MM-dd theo giờ máy (tránh lệch múi giờ khi gửi lên server)
    var isoDate = (days) => {
        var d = new Date();
        d.setDate(d.getDate() + (Number(days) || 0));
        var m = d.getMonth() + 1, day = d.getDate();
        return d.getFullYear() + "-" + (m < 10 ? "0" + m : m) + "-" + (day < 10 ? "0" + day : day);
    };
    $scope.today = isoDate(0);
    $scope.discounts = [];
    $scope.error = "";
    $scope.numberDay = 7;
    $scope.saving = false;

    var all = `${host_}/discounts/all`;
    $http.get(all).then((resp) => {
        $scope.discounts = resp.data;
    }).catch((err) => {});

    // trạng thái hiệu lực của mã
    $scope.state = (n) => {
        var from = String(n.dateFrom || "").substring(0, 10), end = String(n.dateEnd || "").substring(0, 10);
        if (end && end < $scope.today) return "expired";
        if (from && from > $scope.today) return "upcoming";
        return "active";
    };

    $scope.code = {
        "id": "",
        "dateFrom": isoDate(0),
        "dateEnd": isoDate($scope.numberDay),
        "free": "",
        "dolar": null,
        "number": null
    };

    $scope.randomCode = () => {
        var code = "";
        for (var i = 1; i <= 19; i++) {
            code += (i % 5 == 0) ? "-" : String(Math.floor((Math.random() * 9) + 1));
        }
        $scope.code.id = code;
    };

    // this is function to update days when change number discount
    $scope.changeDate = () => {
        $scope.code.dateEnd = isoDate($scope.numberDay);
    };

    $scope.check = () => {
        $scope.error = "";
        if (!$scope.code.id) {
            $scope.error = "Vui lòng bấm “Tạo mã nhanh” để sinh mã giảm giá.";
        }
        else if ($scope.code.number == null || $scope.code.number <= 0) {
            $scope.error = "Số lượng voucher phải lớn hơn 0.";
        }
        else if ($scope.numberDay == null || $scope.numberDay <= 0) {
            $scope.error = "Số ngày hiệu lực phải lớn hơn 0.";
        }
        else if ($scope.code.free == null || $scope.code.free === "") {
            $scope.error = "Vui lòng chọn mức giảm giá.";
        }
        else if ($scope.code.free == "money" && ($scope.code.dolar == null || $scope.code.dolar <= 0)) {
            $scope.error = "Vui lòng nhập số tiền giảm (USD) lớn hơn 0.";
        }
        else {
            var item = angular.copy($scope.code);
            if (item.free == "money") { item.free = null; } else { item.free = Number(item.free); item.dolar = null; }
            item.dateFrom = isoDate(0);
            item.dateEnd = isoDate($scope.numberDay);
            $scope.saving = true;
            $http.post(`${host_}/discounts`, item).then((resp) => {
                window.location.href = "/staff/discount";
            }).catch((err) => {
                $scope.saving = false;
                $scope.error = "Không thể tạo mã giảm giá, vui lòng thử lại.";
            });
        }
    };
});

app.controller("staffhome-ctrl", ($scope, $http) => {
    $scope.orders = [];
    $scope.results = [];
    $scope.counts = {};
    $scope.loaded = false;
    $scope.search = { keyword: "", from: null, to: null };
    $scope.statusClass = { "0": "bg-azure-lt", "1": "bg-blue-lt", "2": "bg-orange-lt", "3": "bg-purple-lt", "4": "bg-green-lt", "-1": "bg-red-lt" };

    var toIso = (d) => {
        if (!d) return null;
        var m = d.getMonth() + 1, day = d.getDate();
        return d.getFullYear() + "-" + (m < 10 ? "0" + m : m) + "-" + (day < 10 ? "0" + day : day);
    };

    // tìm kiếm đơn hàng ở mọi trạng thái (mới nhất trước)
    $scope.doSearch = () => {
        var kw = ($scope.search.keyword || "").trim().toLowerCase().replace(/^#/, "");
        var from = toIso($scope.search.from), to = toIso($scope.search.to);
        $scope.results = $scope.orders.filter(o => {
            var date = String(o.createDate || "").substring(0, 10);
            if (from && date < from) return false;
            if (to && date > to) return false;
            if (!kw) return true;
            var acc = o.account || {};
            return [o.id, acc.fullname, acc.username, acc.email, acc.phone, o.address]
                .join(" ").toLowerCase().indexOf(kw) >= 0;
        });
        // đơn trùng đúng mã được đưa lên đầu
        var exact = $scope.results.findIndex(o => String(o.id) === kw);
        if (exact > 0) $scope.results.unshift($scope.results.splice(exact, 1)[0]);
    };

    var url = `${host_}/orders`;
    $http.get(url).then((resp) => {
        $scope.orders = (resp.data || []).slice().sort((a, b) => b.id - a.id);
        $scope.orders.forEach(o => {
            var s = o.status ? o.status.id : null;
            $scope.counts[s] = ($scope.counts[s] || 0) + 1;
        });
        $scope.loaded = true;
        $scope.doSearch();
    }).catch((err) => {
        $scope.loaded = true;
    });
});

app.controller("mode-cart-ctrl", ($scope, $http) => {
    $scope.count_notify = [];
    $scope.count_true = [];
    $scope.count_false = [];
    
    // Kiểm tra id trước khi sử dụng
    if (!id) {
        return; // Thoát nếu không có id (trang admin có thể không có)
    }
    
    var url_all  = `${host_}/notifications/${id}`; // id of account
    var url_true = `${host_}/notifications/${id}/true`; // id of account
    var url_false = `${host_}/notifications/${id}/false`; // id of account

    $http.get(url_all).then((resp) => { $scope.count_notify = resp.data; }).catch((err) => {});
    $http.get(url_true).then((resp) => { $scope.count_true = resp.data; }).catch((err) => {});
    $http.get(url_false).then((resp) => { $scope.count_false = resp.data; }).catch((err) => {});

    // id of nofity
    $scope.update_status = (id) => {
        var url_id = `${host_}/notifications/notify/${id}`;

        $http.put(url_id).then(() => {
            if (window.location.href.includes("admin")) {
                window.location.href = "/admin/history";
            }
            else {
                window.location.reload();
            }
        }).catch((err) => {});
    };
});

// danh sách mã giảm giá còn dùng được cho khách hàng (trang Mã giảm giá / Khuyến mãi)
app.controller("voucher-ctrl", ($scope, $http) => {
    $scope.vouchers = [];
    $scope.loaded = false;
    $scope.copied = null;

    $http.get(`${host_}/discounts/unexpiry`).then((resp) => {
        var today = new Date().toLocaleDateString("en-CA");
        $scope.vouchers = (resp.data || []).filter((v) =>
            String(v.dateFrom).substring(0, 10) <= today && (v.number == null || v.number > 0));
    }).catch(() => {}).finally(() => {
        $scope.loaded = true;
    });

    $scope.copy = (code) => {
        var done = () => { $scope.$applyAsync(() => { $scope.copied = code; }); };
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(code).then(done, done);
        } else {
            var t = document.createElement("textarea");
            t.value = code;
            document.body.appendChild(t);
            t.select();
            try { document.execCommand("copy"); } catch (e) {}
            document.body.removeChild(t);
            done();
        }
    };
});
