package com.example.tcpconverter.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiConvertControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("parseTcpMessageExample")
    void parseTcpMessageExample() throws Exception {
        // 테스트 요청: COMMON 영역 + ApiSvcCd 포함 (길이 맞춰 임의 데이터)
        // 실제 테스트시 DB의 COMMON, ApiSvcCd 스펙에 맞게 더 길이 조정 필요
        String tcpMsg = "000657" + // MsgLen(6)
                " ISATKKEYEXAMPLE1234" + // FintechIsatk(20)
                "ISCD01" + // FintechIscd(6)
                "001" + // FintechApsno(3)
                "APINAMEEXAMPLE" + " ".repeat(86) + // ApiNm(100) - 요청 14글자 + 86공백
                "QSD_501" + " ".repeat(13) + // ApiSvcCd(20)
                "FISDSCD1" + " ".repeat(12) + // FisDscd(20)
                "FIS" + " ".repeat(17) + // FisCd(20)
                "20240601" + // Tsymd(8)
                "123456" + // Trtm(6)
                "00000000000000000000" + // IsTuno(20)
                "RPCD1" + " ".repeat(15) + // Rpcd(20)
                "RSMS1" + " ".repeat(95) + // Rsms(100)
                "RTNURL1" + " ".repeat(93) + // RtnUrl(100)
                "Y" + " ".repeat(9) + // AsyncYn(10)
                "IPADDR1" + " ".repeat(3) + // IpInfo(10)
                "001" + // FintechMarkChnlCd(3)
                "FILLER" + " ".repeat(75)+  // FillerC(81)
                "1"+
                "A234"+ " ".repeat(16)+
                "B234"+ " ".repeat(9)+
                "02"+
                "C234"+ " ".repeat(6)+
                "D234"+ " ".repeat(4)+
                "E234"+ " ".repeat(4)+
                "F234"+ " ".repeat(6)+
                "G234"+ " ".repeat(4)+
                "H234"+ " ".repeat(4)+
                "010100" + "    " +
                "01"+
                "01"+
                "A0";
        MvcResult result = mockMvc.perform(post("/api/convert/parse")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(tcpMsg))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 Body 로그 출력
        String responseBody = result.getResponse().getContentAsString();
        System.out.println("[응답 Body]: " + responseBody);

    }
} 
