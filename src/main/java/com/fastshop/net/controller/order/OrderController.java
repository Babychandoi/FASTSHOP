package com.fastshop.net.controller.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fastshop.net.model.Account;
import com.fastshop.net.model.Authority;
import com.fastshop.net.model.Order;
import com.fastshop.net.repository.OrderDetailDAO;
import com.fastshop.net.service.ProductService;
import com.fastshop.net.service.StatusService;
import com.fastshop.net.service._CookieService;
import com.fastshop.net.service._GetListFile;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.AuthorityService;
import com.fastshop.net.service.CategoryDetailService;
import com.fastshop.net.service.CategoryService;
import com.fastshop.net.service.NotifyService;
import com.fastshop.net.service.OrderDetailService;
import com.fastshop.net.service.OrderService;
import com.fastshop.net.service.ProductDetailService;

@Controller
public class OrderController {
    @Autowired
    _CookieService cookie;
    @Autowired
    _GetListFile getListFile;
    @Autowired
    AuthorityService authorityService;
    @Autowired
    AccountService accountService;
    @Autowired
    ProductService productSevice;
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryDetailService categoryDetailService;
    @Autowired
    ProductDetailService productDetailService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderDetailService orderDetailService;
    @Autowired
    OrderDetailDAO orderDetailDAO;
    @Autowired
    StatusService statusService;
    @Autowired
    NotifyService notifyService;

    // xem mục quản lý hóa đơn của user
    @RequestMapping("/user/order/{username}/{id}")
    public String getAll(Model model, @ModelAttribute("auth") Authority auth, @PathVariable("id") Integer id, @PathVariable("username") String username) {
        if (auth == null) {
            return "redirect:/login";
        }
        // chỉ cho xem đơn hàng của chính mình
        Account account = auth.getAccount();
        if (!account.getUsername().equals(username)) {
            return "redirect:/user/order/" + account.getUsername() + "/" + id;
        }
        List<Order> orders = orderService.findByStatusAndAccount(id, account);
        try {
            model.addAttribute("page", "user.myorder");
            model.addAttribute("title_main", "Fastshop - Quản lý hóa đơn");
            model.addAttribute("orders", orders);
            model.addAttribute("status", id);
            model.addAttribute("address", orderService.findAddressByUsername(auth.getAccount()));
            model.addAttribute("count_notify", notifyService.findAllByAccAndNowAndStatusTrueOrderBy(auth.getAccount()));

            return "index";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }


    // cập nhật trạng thái hàng của user
    // id là id của đơn hàng
    @RequestMapping("/user/order/update/{orderid}")
    public String update(@ModelAttribute("auth") Authority auth, @PathVariable("orderid") Long id) {
        if (auth == null) {
            return "redirect:/login";
        }
        Order order = orderService.findById(id);
        // khách chỉ được hủy đơn của mình khi đơn chưa được giao đi
        if (order != null && order.getAccount().getUsername().equals(auth.getAccount().getUsername())
                && order.getStatus().getId() >= 0 && order.getStatus().getId() <= 2) {
            orderDetailService.restoreStock(order);
            order.setStatus(statusService.findById(-1));
            orderService.save(order);
        }
        return "redirect:/user/myorder";
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
