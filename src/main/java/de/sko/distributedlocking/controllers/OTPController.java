package de.sko.distributedlocking.controllers;


import de.sko.distributedlocking.entities.OTPNextRefresh;
import de.sko.distributedlocking.repositories.OTPNextRefreshRepository;
import de.sko.distributedlocking.repositories.OTPRepository;
import de.sko.distributedlocking.services.OTPService;
import java.time.Instant;
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
   private final OTPService otpService;
   private final OTPRepository otpRepository;
   private final OTPNextRefreshRepository otpNextRefreshRepository;

   OTPController(
      OTPService otpService,
      OTPRepository otpRepository,
      OTPNextRefreshRepository otpNextRefreshRepository )
   {
      this.otpService = otpService;
      this.otpRepository = otpRepository;
      this.otpNextRefreshRepository = otpNextRefreshRepository;
   }

   @GetMapping
   ResponseEntity< OTPs > getOtps()
   {
      final var response =
         otpRepository.findAll().stream().map( otp -> new ClientOTP( otp.getClientId(), otp.getOtp() ) ).toList();

      final var nextRefresh =
         otpNextRefreshRepository.find()
                                 .map( OTPNextRefresh::getNextRefresh )
                                 .orElseThrow( () -> new IllegalStateException( "next refresh time not yet known" ) );

      return ResponseEntity.ok( new OTPs( response, nextRefresh ) );
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

   public record OTPs( Collection< ClientOTP > otps, Instant nextRefresh )
   {
   }
}
