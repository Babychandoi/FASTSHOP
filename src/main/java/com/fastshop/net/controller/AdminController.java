package com.fastshop.net.controller;

import com.fastshop.net.service.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Authority;
import com.fastshop.net.model.Order;
import com.fastshop.net.model.WaitForStaff;

@Controller
public class AdminController {
    @Autowired
    _CookieService cookie;
    @Autowired
    _SessionService session;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    AccountService accountService;
    @Autowired
    HistoryService historyService;
    @Autowired
    OrderService orderService;
    @Autowired
    DiscountService discountService;
    @Autowired
    CommentService commentService;
    @Autowired
    OrderDetailService orderDetailService;
    @Autowired
    NotifyService notifyService;

    @Autowired
    WaitForStaffService waitForStaffService;

    @RequestMapping("/admin")
    public String admin() {
        return "redirect:/admin/home";
    }

    @RequestMapping("/admin/home")
    public String home(Model model, @ModelAttribute("auth") Authority authority) {
        try {
            String title_main = "Admin - Thống kê doanh số bán hàng";
            model.addAttribute("page", "admin.home");
            model.addAttribute("title_main", title_main);
            addSalesStatistics(model);
            List<String> top = orderDetailService.getTop3BestSelling();
            model.addAttribute("top3Product", top.subList(0, Math.min(3, top.size())));

            // thông số số lượng của admin
            model.addAttribute("members", authorityService.countByRoleEqualsUser());
            model.addAttribute("memeberOrdered", orderService.getListUsernameOrdered().size());
            model.addAttribute("histories", historyService.findAll());
            model.addAttribute("voucherAll", discountService.findByAll().size());
            model.addAttribute("voucherUnexpiry", discountService.findByDiscountUnexpiry().size());
            model.addAttribute("orderAll", orderService.findAll().size());
            model.addAttribute("orderSuccess", orderService.countOrderSuccess());
            model.addAttribute("commentsAll", commentService.findAll().size());
            model.addAttribute("comments5", commentService.countByRateEquals5());

            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login.fastshop.com";
        }
    }

    // Số liệu doanh thu / đơn hàng (không tính đơn đã hủy) cho các thẻ thống kê
    private void addSalesStatistics(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        YearMonth lastMonth = thisMonth.minusMonths(1);
        LocalDate from30 = today.minusDays(29);
        LocalDate from60 = today.minusDays(59);

        double revenueMonth = 0, revenueLastMonth = 0, revenue30 = 0, revenuePrev30 = 0;
        int ordersMonth = 0, ordersLastMonth = 0, orders30 = 0, ordersPrev30 = 0, ordersToday = 0, pending = 0;
        Set<String> customers30 = new HashSet<>();
        Set<String> customersPrev30 = new HashSet<>();

        for (Order order : orderService.findAll()) {
            if (order.getCreateDate() == null || order.getStatus() == null) {
                continue;
            }
            if (order.getStatus().getId() == 0) {
                pending++;
            }
            if (order.getStatus().getId() == -1) {
                continue;
            }
            LocalDate created = new java.sql.Date(order.getCreateDate().getTime()).toLocalDate();
            double total = order.getTotal() == null ? 0 : order.getTotal();
            String username = order.getAccount() == null ? "" : order.getAccount().getUsername();
            YearMonth month = YearMonth.from(created);
            if (month.equals(thisMonth)) {
                revenueMonth += total;
                ordersMonth++;
            } else if (month.equals(lastMonth)) {
                revenueLastMonth += total;
                ordersLastMonth++;
            }
            if (!created.isBefore(from30)) {
                revenue30 += total;
                orders30++;
                customers30.add(username);
            } else if (!created.isBefore(from60)) {
                revenuePrev30 += total;
                ordersPrev30++;
                customersPrev30.add(username);
            }
            if (created.equals(today)) {
                ordersToday++;
            }
        }

        model.addAttribute("revenueMonth", revenueMonth);
        model.addAttribute("revenueLastMonth", revenueLastMonth);
        model.addAttribute("revenueGrowth", growth(revenueMonth, revenueLastMonth));
        model.addAttribute("ordersMonth", ordersMonth);
        model.addAttribute("ordersLastMonth", ordersLastMonth);
        model.addAttribute("ordersGrowth", growth(ordersMonth, ordersLastMonth));
        model.addAttribute("revenue30", revenue30);
        model.addAttribute("revenue30Growth", growth(revenue30, revenuePrev30));
        model.addAttribute("orders30", orders30);
        model.addAttribute("orders30Growth", growth(orders30, ordersPrev30));
        model.addAttribute("customers30", customers30.size());
        model.addAttribute("customers30Growth", growth(customers30.size(), customersPrev30.size()));
        model.addAttribute("ordersToday", ordersToday);
        model.addAttribute("pendingOrders", pending);
    }

    private static double growth(double current, double previous) {
        return previous == 0 ? 0 : (current - previous) * 100 / previous;
    }

    @RequestMapping("/admin/employee")
    public String customer(Model model, @ModelAttribute("auth") Authority authority) {
        try {
            String title_main = "Admin - Thống kê danh sách nhân viên";
            model.addAttribute("staff", new Account());
            model.addAttribute("title_main", title_main);

            List<Account> waitForStaffList = new ArrayList<>();
            List<WaitForStaff> waitForStaffs = authorityService.getWaitForStaff();
            for (WaitForStaff waitForStaff : waitForStaffs) {
                Account account = waitForStaff.getAccount();
                waitForStaffList.add(account);
            }
            model.addAttribute("waitForStaffList", waitForStaffList);
            model.addAttribute("employees", authorityService.getListStaff());

            model.addAttribute("page", "admin.employee");
            model.addAttribute("histories", historyService.findAll());
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));

            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }


    @RequestMapping("/admin/history")
    public String history(Model model, @ModelAttribute("auth") Authority authority) {
        try {
            String title_main = "Admin - Lịch sử hoạt động";
            model.addAttribute("histories", historyService.findAll());
            model.addAttribute("page", "admin.history");
            model.addAttribute("title_main", title_main);
            model.addAttribute("histories", historyService.findAll());
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));

            return "index";
        } catch (Exception e) {
            return "redirect:/login.fastshop.com";
        }
    }


    @RequestMapping("/admin/help/docs")
    public String docs(Model model, @ModelAttribute("auth") Authority authority) {
        String title_main = "Tài liệu hỗ trợ phần mềm";
        model.addAttribute("page", "admin.docs");
        model.addAttribute("title_main", title_main);
        model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));
        model.addAttribute("histories", historyService.findAll());

        return "index";
    }


    @RequestMapping("/admin/help/license")
    public String license(Model model, @ModelAttribute("auth") Authority authority) {
        String title_main  = "Tài liệu thông tin pháp lý";
        model.addAttribute("page", "admin.license");
        model.addAttribute("title_main", title_main);
        model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));
        model.addAttribute("histories", historyService.findAll());

        return "index";
    }

    @RequestMapping("/admin/help/contact")
    public String contact(Model model, @ModelAttribute("auth") Authority authority) {
        String title_main = "Thông tin liên hệ chúng tôi";
        model.addAttribute("page", "admin.contact");
        model.addAttribute("title_main", title_main);
        model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(authority.getAccount()));
        model.addAttribute("histories", historyService.findAll());

        return "index";
    }

    /**
     * this is model of authority
     * @return
     */
    @ModelAttribute("auth")
    public Authority getAuth() {
        Authority auth = null;
        String username = cookie.getValue("username");
        if (username != null) {
            Account account = accountService.findByUsernameOrEmail(username, username);
            auth = authorityService.findByAccount(account);
        }
        return auth;
    }
}
