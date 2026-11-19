package co.ke.xently.demo.soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.math.BigInteger;

@Slf4j
@Service
public class SoapNumberService {
    private final WebServiceTemplate webServiceTemplate;

    public SoapNumberService(WebServiceTemplateBuilder builder) {
        var marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(NumberToWords.class, NumberToWordsResponse.class);
        this.webServiceTemplate = builder
                .setMarshaller(marshaller)
                .setUnmarshaller(marshaller)
                .setDefaultUri("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
                .build();
    }

    public String convertNumberToWords(BigInteger number) {
        var request = new NumberToWords();
        request.setUbiNum(number);

        try {
            var response = (NumberToWordsResponse) webServiceTemplate
                    .marshalSendAndReceive(request);

            return response.getNumberToWordsResult();
        } catch (Exception e) {
            log.error("Error converting number to words", e);
            return "";
        }
    }
}
