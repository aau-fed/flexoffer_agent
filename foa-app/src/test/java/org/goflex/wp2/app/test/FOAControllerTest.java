package org.goflex.wp2.app.test;

import org.goflex.wp2.foa.interfaces.FOAService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by bijay on 11/28/17.
 */
//@RunWith(SpringRunner.class)
//@WebMvcTest(FOARestController.class)
//@ContextConfiguration(classes = FOARestController.class)
public class FOAControllerTest {


    String authorization = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJGTUFORk1BUi5pbyIsImV4cCI6MTUxMjg2NTA1OCwiaWF0IjoxNTEyMDAxMDU4OTU3LCJUaGVTZWNyZXRLZXlGb3JGT0EiOiJhZG1pbiJ9.0XQE3uJAsDgrNU3XiOzKo5HflyziuMLy52x2Jey-xJWAKpxEX2tMGYy8hiFsiGTK4IaWDCmYLLwDlQJxdEXl8g";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;
    @MockBean
    private FOAService foaService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /*@Test
    public void shouldReturnString() throws Exception{
        this.mockMvc.perform(get("/api/uriForTest")
                .header("Authorization", "Bearer" + authorization)
                .accept("application/text;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ok i am returning")));

    }
    @Test
    public void givenFO_whenGetFO_theReturnJsonArray() throws Exception{
        Date dateT =  new Date();
        List<FlexOfferTempT> fos = Arrays.asList(new FlexOfferTempT("TestUser", "TestPlug", dateT,"initial", "Test_FO1"),
                        new FlexOfferTempT("TestUser", "TestPlug", dateT,"initial", "Test_FO2"));

        when(foaService.getFlexOffer("TestUser")).thenReturn(fos);

        this.mockMvc.perform(get("/api/flexoffers/TestUser")
                .header("Authorization", "Bearer" + authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .accept("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].flexoffer", is(fos.get(0).getFlexoffer())));
    }*/

}
