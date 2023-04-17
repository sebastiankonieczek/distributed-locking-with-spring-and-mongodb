package de.sko.distributedlocking.services;


import de.sko.distributedlocking.entities.OTP;
import de.sko.distributedlocking.repositories.OTPRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.stereotype.Service;

@Service
public class OTPService
{
   public static final int RANDOM_SEED_SIZE = 100;
   public static final int RANDOM_BOUND = 999_999;
   private final JdbcLockRegistry otpLockRegistry;
   private final OTPRepository otpRepository;

   OTPService(
      @Qualifier( "otpLockRegistry" ) JdbcLockRegistry otpLockRegistry,
      OTPRepository otpRepository )
   {
      this.otpLockRegistry = otpLockRegistry;
      this.otpRepository = otpRepository;
   }

   public void updateOtp( String clientId )
   {
      var otpLock = otpLockRegistry.obtain( clientId );
      otpLock.lock();
      try {
         otpRepository.save( new OTP( clientId, generateOTP(), true, Instant.now( Clock.systemUTC() ) ) );
      }
      finally {
         otpLock.unlock();
      }
   }

   /**
    * Maybe a bit too expensive to initialize {@link SecureRandom} for every OTP generation,
    * but it is acceptable for this demo.
    */
   private String generateOTP()
   {
      final var secureRandom = new SecureRandom();
      secureRandom.setSeed( secureRandom.generateSeed( RANDOM_SEED_SIZE ) );
      return String.format( "%06d", secureRandom.nextInt( RANDOM_BOUND ) );
   }

   public boolean isOneTimePasswordValid( String clientId, String otp )
   {
      var otpLock = otpLockRegistry.obtain( clientId );
      otpLock.lock();
      try {
         final Optional< OTP > currentClientOtp =
            otpRepository.findByClientIdAndOtp( clientId, otp );
         if( currentClientOtp.isEmpty() || !currentClientOtp.get().isValid() ) {
            return false;
         }

         invalidateOtp( currentClientOtp.get() );

         return true;
      }
      finally {
         otpLock.unlock();
      }
   }

   private void invalidateOtp( final OTP otp )
   {
      otp.setValid( false );
      otpRepository.save( otp );
   }
}
