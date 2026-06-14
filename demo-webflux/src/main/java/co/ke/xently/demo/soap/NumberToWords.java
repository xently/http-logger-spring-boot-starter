package co.ke.xently.demo.soap;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "ubiNum"
})
@XmlRootElement(name = "NumberToWords", namespace = "http://www.dataaccess.com/webservicesserver/")
public class NumberToWords {

    @XmlElement(namespace = "http://www.dataaccess.com/webservicesserver/", required = true)
    protected BigInteger ubiNum;
}
