package de.sko.distributedlocking.controllers;


import de.sko.distributedlocking.repositories.OTPRepository;
import de.sko.distributedlocking.services.OTPService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( path = "otp", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
class OTPController
{
   public static final int OTP_TTL = 60;
   private final OTPService otpService;
   private final OTPRepository otpRepository;

   OTPController(
      OTPService otpService,
      OTPRepository otpRepository )
   {
      this.otpService = otpService;
      this.otpRepository = otpRepository;
   }

   @GetMapping
   ResponseEntity< Collection< ClientOTP > > getOtps()
   {
      final var response =
         otpRepository.findAll().stream().map( otp -> new ClientOTP( otp.getClientId(), otp.getOtp() ) ).toList();

      return ResponseEntity.ok( response );
   }

   /**
    * This would normally be an internal process where the expiry time is exposed to the clients.
    * Maybe think about setting expire time from client.
    */
   @PostMapping( "refresh" )
   ResponseEntity< NextRefresh > refresh()
   {
      otpService.refreshAll();
      final var expiryTime = LocalDateTime.now( Clock.systemUTC() ).plusSeconds( OTP_TTL );
      return ResponseEntity.ok( new NextRefresh( expiryTime ) );
   }

   @PostMapping( "register-client" )
   ResponseEntity< Void > registerClient( @RequestBody String clientId )
   {
      otpService.updateOtp( clientId );

      return ResponseEntity.ok().build();
   }

   @PostMapping( "use" )
   ResponseEntity< Void > useOnetimePassword( @RequestBody OTPPayload otpPayload )
   {
      if( otpService.isOneTimePasswordValid( otpPayload.clientId, otpPayload.oneTimePassword ) ) {
         return ResponseEntity.ok().build();
      }

      return ResponseEntity.status( HttpStatus.FORBIDDEN ).build();
   }

   public record OTPPayload( String clientId, String oneTimePassword )
   {
   }

   public record ClientOTP( String clientId, String oneTimePassword )
   {
   }

   public record NextRefresh( LocalDateTime next )
   {
   }
}
