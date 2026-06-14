package co.ke.xently.demo.soap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/v1/soap")
public class SoapNumberController {
    private final SoapNumberService soapNumberService;

    public SoapNumberController(SoapNumberService soapNumberService) {
        this.soapNumberService = soapNumberService;
    }

    @GetMapping("/number-to-words")
    public String convertNumberToWords(@RequestParam BigInteger number) {
        return soapNumberService.convertNumberToWords(number);
    }
}
