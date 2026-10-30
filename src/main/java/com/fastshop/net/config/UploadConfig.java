package com.fastshop.net.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * File người dùng upload (ảnh sản phẩm, file báo cáo) được lưu ra thư mục ngoài jar
 * (app.upload-dir) và phục vụ chung URL với tài nguyên tĩnh có sẵn trong classpath.
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {
    private static Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads");

    @Value("${app.upload-dir:}")
    public void setUploadDir(String dir) {
        if (dir != null && !dir.isBlank()) {
            uploadRoot = Paths.get(dir);
        }
    }

    public static Path productImageDir() {
        return uploadRoot.resolve("products").toAbsolutePath();
    }

    public static Path reportFileDir() {
        return uploadRoot.resolve("files").toAbsolutePath();
    }

    /**
     * Tên file an toàn để lưu: bỏ đường dẫn, chỉ giữ ký tự an toàn, thêm tiền tố thời gian để không ghi đè,
     * và kiểm tra phần mở rộng nằm trong danh sách cho phép.
     */
    public static String safeFileName(String original, Set<String> allowedExtensions) {
        String name = original == null ? "" : Paths.get(original.replace("\\", "/")).getFileName().toString();
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ: " + ext);
        }
        String base = name.substring(0, dot).replaceAll("[^A-Za-z0-9_-]+", "-");
        return System.currentTimeMillis() + "-" + (base.isEmpty() ? "file" : base) + "." + ext;
    }

    public static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    public static final Set<String> REPORT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "csv", "txt");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dist/img/products/**")
                .addResourceLocations(productImageDir().toUri().toString(), "classpath:/static/dist/img/products/");
        registry.addResourceHandler("/files/**")
                .addResourceLocations(reportFileDir().toUri().toString(), "classpath:/static/files/");
    }
}
