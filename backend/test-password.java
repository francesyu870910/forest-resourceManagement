import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成admin123的哈希
        String adminHash = encoder.encode("admin123");
        System.out.println("admin123 hash: " + adminHash);
        
        // 验证现有的哈希
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLyifVPoCBZu";
        boolean matches = encoder.matches("admin123", existingHash);
        System.out.println("admin123 matches existing hash: " + matches);
        
        // 生成user123的哈希
        String userHash = encoder.encode("user123");
        System.out.println("user123 hash: " + userHash);
        
        // 验证现有的user哈希
        String existingUserHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        boolean userMatches = encoder.matches("user123", existingUserHash);
        System.out.println("user123 matches existing hash: " + userMatches);
    }
}