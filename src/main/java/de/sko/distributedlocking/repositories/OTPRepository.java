package de.sko.distributedlocking.repositories;

import de.sko.distributedlocking.entities.OTP;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OTPRepository
   extends MongoRepository< OTP, String >
{
   Optional< OTP > findByClientIdAndOtp(
      final String client,
      final String otp );
}
