package co.ke.xently.demo.soap;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "numberToWordsResult"
})
@XmlRootElement(name = "NumberToWordsResponse", namespace = "http://www.dataaccess.com/webservicesserver/")
public class NumberToWordsResponse {

    @XmlElement(name = "NumberToWordsResult", namespace = "http://www.dataaccess.com/webservicesserver/", required = true)
    protected String numberToWordsResult;
}
