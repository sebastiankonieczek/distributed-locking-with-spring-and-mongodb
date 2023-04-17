package de.sko.distributedlocking.services;


import de.sko.distributedlocking.entities.OTP;
import de.sko.distributedlocking.repositories.OTPRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
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
      JdbcLockRegistry otpLockRegistry,
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

   private String generateOTP()
   {
      final var secureRandom = new SecureRandom();
      secureRandom.setSeed( secureRandom.generateSeed( RANDOM_SEED_SIZE ) );
      return String.format( "%06d", secureRandom.nextInt( RANDOM_BOUND ) );
   }

   public boolean isOneTimePasswordValid(
      String clientId,
      String otp )
   {
      var otpLock = otpLockRegistry.obtain( clientId );
      otpLock.lock();
      try {
         final var currentClientOtp = otpRepository.findByClientIdAndOtp( clientId, otp );
         if( !currentClientOtp.map( OTP::isValid ).orElse( false ) ) {
            return false;
         }

         currentClientOtp.ifPresent(
            theOtp -> {
               theOtp.setValid( false );
               otpRepository.save( theOtp );
            }
         );

         return true;
      }
      finally {
         otpLock.unlock();
      }
   }

   public void refreshAll()
   {
      otpRepository.findAll().forEach( client -> updateOtp( client.getClientId() ) );
   }
}
