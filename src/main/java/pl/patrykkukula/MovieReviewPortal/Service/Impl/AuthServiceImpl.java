package pl.patrykkukula.MovieReviewPortal.Service.Impl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.patrykkukula.MovieReviewPortal.Dto.PasswordResetDto;
import pl.patrykkukula.MovieReviewPortal.Dto.UserEntityDto;
import pl.patrykkukula.MovieReviewPortal.Exception.ResourceNotFoundException;
import pl.patrykkukula.MovieReviewPortal.Model.PasswordResetToken;
import pl.patrykkukula.MovieReviewPortal.Model.Role;
import pl.patrykkukula.MovieReviewPortal.Model.UserEntity;
import pl.patrykkukula.MovieReviewPortal.Model.VerificationToken;
import pl.patrykkukula.MovieReviewPortal.Repository.PasswordResetRepository;
import pl.patrykkukula.MovieReviewPortal.Repository.RoleRepository;
import pl.patrykkukula.MovieReviewPortal.Repository.VerificationTokenRepository;
import pl.patrykkukula.MovieReviewPortal.Repository.UserEntityRepository;
import pl.patrykkukula.MovieReviewPortal.Service.IAuthService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static java.lang.System.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final UserEntityRepository userRepository;
    private final VerificationTokenRepository verificationRepository;
    private final PasswordResetRepository pwdRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public String register(UserEntityDto userDto) {
        Optional<UserEntity> existingUser = userRepository.findUserByUsernameOrEmail(userDto.getUsername(), userDto.getEmail());
        if (existingUser.isPresent()) throw new IllegalStateException("Username or email already exists");

        String hashedPassword = encoder.encode(userDto.getPassword());
        Role role = roleRepository.findRoleByRoleName("USER").orElseThrow(() -> new RuntimeException("Role USER not found. Please contact technical support"));

       UserEntity user = UserEntity.builder()
                        .username(userDto.getUsername())
                        .password(hashedPassword)
                        .email(userDto.getEmail())
                        .roles(List.of(role))
                        .build();
        userRepository.save(user);

       return generateVerificationToken(user);
    }
    @Override
    @Transactional
    public void verifyAccount(String token) {
        if (token == null || token.isEmpty()) throw new IllegalArgumentException("token cannot be null or empty");
        VerificationToken verificationToken = verificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired token"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) throw new IllegalStateException("Token expired");
        UserEntity userEntity = verificationToken.getUser();
        if (userEntity.isEnabled()) throw new IllegalStateException("Account already verified");
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        verificationRepository.delete(verificationToken);
    }
    @Override
    @Transactional
    public String resendVerificationToken(String email) {
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("email cannot be null or empty");
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Account", "email", email));
        return generateVerificationToken(user);
    }
    @Override
    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        PasswordResetToken token = pwdRepository.findByToken(passwordResetDto.getPwdResetToken())
                .orElseThrow(() -> new IllegalStateException("Invalid token"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) throw new IllegalStateException("Token expired");
        UserEntity user = userRepository.findByEmail(passwordResetDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email", passwordResetDto.getEmail()));

        if (encoder.matches(passwordResetDto.getNewPassword(), user.getPassword())) throw new IllegalStateException("New password cannot be same as old password");
        String hashedNewPassword = encoder.encode(passwordResetDto.getNewPassword());
        user.setPassword(hashedNewPassword);
        user.setPasswordResetToken(null);
        userRepository.save(user);
    }
    @Override
    @Transactional
    public String generatePasswordResetToken(String email){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Optional<PasswordResetToken> existingToken = pwdRepository.findByUser(user);
        existingToken.ifPresent(token -> {
            user.setPasswordResetToken(null);
            userRepository.save(user);
            pwdRepository.delete(token);
            pwdRepository.flush();
        });

        String token = UUID.randomUUID().toString();
        PasswordResetToken pwdToken = PasswordResetToken.builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(12))
                .user(user)
                .build();
        pwdRepository.save(pwdToken);
        return "Password reset token has been created and will be valid for 12 hours: " + token + lineSeparator() +
                "To reset your password, send a POST request to the following URL: " +
                "http://localhost:8081/auth/reset with the following JSON body: " +
                "{\"password\":" + "your new password" + lineSeparator() +
                "\"token\": \"" + token + "\"" + "}";
    }

    private String generateVerificationToken(UserEntity user){
        if (user.isEnabled()) throw new IllegalStateException("Account is verified");
        user.setVerificationToken(null);
        userRepository.save(user);
        pwdRepository.flush();

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken
                .builder()
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(12))
                .user(user)
                .build();
        verificationRepository.save(verificationToken);
        return  "Verification token has been created and will be valid for 12 hours: " + lineSeparator() +
                "To activate your account, send a POST request to the following URL: " +
                "http://localhost:8081/auth/register/confirm with the following JSON body: " +
                "{ \"token\": \"" + token + "\" }";
    }
}
