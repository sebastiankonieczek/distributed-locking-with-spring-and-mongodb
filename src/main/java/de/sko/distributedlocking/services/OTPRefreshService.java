package de.sko.distributedlocking.services;

import de.sko.distributedlocking.entities.OTPNextRefresh;
import de.sko.distributedlocking.repositories.OTPNextRefreshRepository;
import de.sko.distributedlocking.repositories.OTPRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OTPRefreshService
{
   private final OTPRepository otpRepository;
   private final OTPService otpService;
   private final OTPNextRefreshRepository otpNextRefreshRepository;
   private final JdbcLockRegistry otpRefreshLockRegistry;

   public OTPRefreshService(
      OTPRepository otpRepository,
      OTPService otpService,
      OTPNextRefreshRepository otpNextRefreshRepository,
      @Qualifier( "otpRefreshLockRegistry" ) JdbcLockRegistry otpRefreshLockRegistry )
   {
      this.otpRepository = otpRepository;

      this.otpService = otpService;
      this.otpNextRefreshRepository = otpNextRefreshRepository;
      this.otpRefreshLockRegistry = otpRefreshLockRegistry;

      initializeRefresh();
   }

   private void initializeRefresh()
   {
      final var lock = otpRefreshLockRegistry.obtain( "OTP_REFRESH_INIT" );
      lock.lock();
      try {
         final var otpNextRefresh = otpNextRefreshRepository.find();
         if( otpNextRefresh.isEmpty() ) {
            otpNextRefreshRepository.save( new OTPNextRefresh( getNextRefreshTime() ) );
         }
      }
      finally {
         lock.unlock();
      }
   }

   @Scheduled( fixedRate = 1000 )
   public void refreshAll()
   {
      final var lock = otpRefreshLockRegistry.obtain( "OTP_REFRESH" );
      lock.lock();
      try {
         final var otpNextRefresh = otpNextRefreshRepository.find();
         if( !isRefreshNecessary( otpNextRefresh ) ) {
            return;
         }

         otpRepository.findAll().forEach( client -> otpService.updateOtp( client.getClientId() ) );

         saveNextRefreshTime( otpNextRefresh );
      }
      finally {
         lock.unlock();
      }
   }

   private void saveNextRefreshTime( final Optional< OTPNextRefresh > otpNextRefresh )
   {
      final var nextRefreshTime = getNextRefreshTime();
      otpNextRefresh
         .ifPresentOrElse( refresh -> {
                              refresh.setNextRefreshTime( nextRefreshTime );
                              otpNextRefreshRepository.save( refresh );
                           },
                           () -> otpNextRefreshRepository.save( new OTPNextRefresh( nextRefreshTime ) ) );
   }

   private static Boolean isRefreshNecessary( final Optional< OTPNextRefresh > otpNextRefresh )
   {
      return otpNextRefresh.map( refresh -> Instant.now( Clock.systemUTC() ).isAfter( refresh.getNextRefresh() ) )
                           .orElse( true );
   }

   private static Instant getNextRefreshTime()
   {
      return Instant.now( Clock.systemUTC() ).plusSeconds( 60 );
   }
}
